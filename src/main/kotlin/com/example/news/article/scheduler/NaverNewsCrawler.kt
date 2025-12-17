package com.example.news.article.scheduler

import com.example.news.article.domain.Article
import com.example.news.article.domain.ArticleSection
import com.example.news.article.repository.ArticleRepository
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min

@Component
class NaverNewsCrawler(
    private val articleRepository: ArticleRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /* =========================
     * 고정 설정값 (환경변수 분리 X)
     * ========================= */
    private val userAgent =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    private val connectTimeoutMs = 10_000
    private val detailTimeoutMs = 12_000

    private val maxListItems = 25
    private val maxPopularItems = 10

    // 요청 속도 제한(초당 N회)
    private val requestsPerSecond = 3.0
    private val minIntervalNs =
        if (requestsPerSecond <= 0) 0L else (1_000_000_000L / requestsPerSecond).toLong()
    private val lastRequestNs = AtomicLong(0L)

    private fun rateLimit() {
        if (minIntervalNs <= 0) return
        while (true) {
            val now = System.nanoTime()
            val prev = lastRequestNs.get()
            if (now >= prev + minIntervalNs) {
                if (lastRequestNs.compareAndSet(prev, now)) return
            } else {
                Thread.sleep(1)
            }
        }
    }

    /* =========================
     * 섹션 매핑 (4개 섹션 공용)
     * ========================= */
    private val sectionMap = linkedMapOf(
        "100" to ArticleSection.POLITICS,
        "101" to ArticleSection.ECONOMY,
        "102" to ArticleSection.SOCIAL,
        "105" to ArticleSection.TECHNOLOGY
    )

    /* =========================
     * Scheduler
     * ========================= */
    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.MINUTES)
    fun crawl() {
        log.info("🔄 [Batch] 네이버 뉴스 크롤링 시작")

        var totalSaved = 0
        sectionMap.forEach { (sectionId, sectionType) ->
            runCatching {
                val saved = crawlSection(sectionId, sectionType)
                totalSaved += saved
                if (saved > 0) log.info("   ✅ [$sectionType] 저장 ${saved}건")
            }.onFailure {
                log.error("❌ 섹션($sectionType) 실패: ${it.message}", it)
            }
        }

        log.info("🏁 [Batch] 종료 - 총 ${totalSaved}건 저장")
    }

    /* =========================
     * 섹션 크롤링 (랭킹 페이지 제외)
     * ========================= */
    private fun crawlSection(sectionId: String, defaultSection: ArticleSection): Int {
        val sectionUrl = "https://news.naver.com/section/$sectionId"

        rateLimit()
        val doc = getWithRetry(sectionUrl, connectTimeoutMs)

        // 섹션 리스트 영역
        val listItems = doc.select(".sa_item, .sa_item_flex, .section_article .sa_item")
            .take(maxListItems)

        // 섹션 내 인기 영역(랭킹 페이지 아님)
        val popularItems = doc.select(".section_article.as_main_popular .sa_item, .section_main_popular .sa_item")
            .take(maxPopularItems)

        val candidates = buildCandidates(listItems + popularItems, defaultSection)
        return saveArticles(candidates)
    }

    /* =========================
     * Candidate 생성
     * ========================= */
    private fun buildCandidates(items: List<Element>, defaultSection: ArticleSection): List<Candidate> {
        val byUrl = LinkedHashMap<String, Candidate>()

        for (item in items) {
            val link = extractLink(item) ?: continue
            if (!link.contains("news.naver.com")) continue
            if (byUrl.containsKey(link)) continue

            val listTitle = extractTitle(item).trim()
            val listThumb = extractThumbnail(item)

            val detail = runCatching {
                rateLimit()
                val detailDoc = getWithRetry(link, detailTimeoutMs)
                parseDetail(detailDoc)
            }.getOrNull() ?: continue

            val section = detectSectionFromUrl(link) ?: defaultSection

            byUrl[link] = Candidate(
                url = link,
                section = section,
                title = listTitle.ifBlank { detail.title },
                content = detail.content,
                thumbnailUrl = listThumb ?: detail.thumbnailUrl,
                publisher = detail.publisher,
                reporter = detail.reporter, // ✅ 기자명(이름만)
                publishedAt = detail.publishedAt
            )
        }

        return byUrl.values.toList()
    }

    /* =========================
     * 저장 (트랜잭션)
     * ========================= */
    @Transactional
    fun saveArticles(candidates: List<Candidate>): Int {
        if (candidates.isEmpty()) return 0

        val urls = candidates.map { it.url }.toSet()
        val existing = articleRepository.findExistingUrls(urls)

        val toSave = candidates
            .filterNot { existing.contains(it.url) }
            .map {
                Article(
                    section = it.section,
                    title = it.title,
                    content = it.content.ifBlank { null },
                    url = it.url,
                    thumbnailUrl = it.thumbnailUrl,
                    // ✅ reporter가 있으면 source에 기자명 저장, 없으면 NAVER
                    source = it.reporter ?: "NAVER",
                    publisher = it.publisher,
                    publishedAt = it.publishedAt,
                    likes = 0L,
                    dislikes = 0L
                )
            }

        if (toSave.isEmpty()) return 0

        return try {
            articleRepository.saveAll(toSave)
            toSave.size
        } catch (e: DataIntegrityViolationException) {
            log.warn("⚠️ UNIQUE(url) 충돌 가능: ${e.message}")
            0
        }
    }

    /* =========================
     * Jsoup + Retry
     * ========================= */
    private fun getWithRetry(url: String, timeoutMs: Int): Document {
        var attempt = 0
        var backoff = 300L

        while (true) {
            try {
                return Jsoup.connect(url)
                    .userAgent(userAgent)
                    .referrer("https://news.naver.com")
                    .timeout(timeoutMs)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .method(Connection.Method.GET)
                    .get()
            } catch (e: IOException) {
                attempt++
                if (attempt >= 3) throw e
                Thread.sleep(backoff)
                backoff = min(backoff * 2, 3000L)
            }
        }
    }

    /* =========================
     * 리스트 아이템 파싱
     * ========================= */
    private fun extractLink(item: Element): String? {
        val a = item.selectFirst("a[href]") ?: return null
        val href = a.attr("href").trim()
        if (href.isBlank()) return null
        return if (href.startsWith("/")) "https://news.naver.com$href" else href
    }

    private fun extractTitle(item: Element): String =
        item.selectFirst(".sa_text_title, .sa_text_strong, a[class*=title], a[href]")?.text().orEmpty()

    private fun extractThumbnail(item: Element): String? {
        val img = item.selectFirst("img") ?: return null
        return img.attr("data-src").ifBlank { img.attr("src") }.ifBlank { null }
    }

    /* =========================
     * 상세 페이지 파싱 (PC/모바일 공용)
     * ========================= */
    private fun parseDetail(doc: Document): Detail {
        doc.outputSettings().prettyPrint(false)

        val title = doc.selectFirst(".media_end_head_headline, #title_area span, .end_tit")
            ?.text().orEmpty()

        val dateStr = doc.selectFirst(".media_end_head_info_datestamp_time")?.attr("data-date-time")
            ?.ifBlank { null }
            ?: doc.selectFirst(".media_end_head_info_datestamp_time")?.text()
            ?: doc.selectFirst("span._ARTICLE_DATE_TIME")?.attr("data-date-time") // 모바일
            ?: doc.selectFirst("span._ARTICLE_DATE_TIME")?.text()

        val publishedAt = parseNaverDate(dateStr)

        val publisher = doc.selectFirst(".media_end_head_top_logo img")?.attr("title")
            ?.ifBlank { null }
            ?: doc.selectFirst(".media_end_head_top_logo_text")?.text()
            ?: doc.selectFirst(".media_end_linked_more_point")?.text()
            ?: "Unknown"

        // ✅ PC/모바일 본문 컨테이너 후보를 넓게 잡음(경제 뉴스 케이스 대응)
        val contentEl = doc.selectFirst(
            "#dic_area, #newsct_article, .newsct_article, #articeBody, article._article_content, article#dic_area"
        )

        val content = extractFormattedContent(contentEl)

        val thumb = doc.selectFirst("meta[property=og:image]")?.attr("content")?.ifBlank { null }

        // ✅ 기자명(이름만) 추출
        val reporter = extractReporterNameOnly(doc)

        return Detail(
            title = title,
            content = content,
            publishedAt = publishedAt,
            publisher = publisher,
            thumbnailUrl = thumb,
            reporter = reporter
        )
    }

    /**
     * ✅ 기자명 "이름만" 추출
     * - 다양한 케이스: "홍길동 기자", "홍길동 기자 = ..."
     * - 이메일/부서/직함/문구가 붙어도 이름만 최대한 남김
     * - 못 찾으면 null
     */
    private fun extractReporterNameOnly(doc: Document): String? {
        // 우선 DOM에서 기자 관련 영역을 넓게 탐색
        val raw = doc.selectFirst(
            ".media_end_head_journalist_name, " +
                    ".media_end_head_journalist, " +
                    ".byline, " +
                    ".journalistcard_summary_name, " +
                    ".reporter_area, " +
                    ".reporter, " +
                    "span.byline_s"
        )?.text()?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: doc.selectFirst("meta[name=author]")?.attr("content")?.trim()
                ?.takeIf { it.isNotBlank() }
            ?: return null

        // 공백 정리
        val s = raw.replace(Regex("\\s+"), " ").trim()

        // 1) "홍길동 기자" / "홍길동기자" / "홍길동 기자=" 등에서 이름만
        // - "기자" 앞을 우선 추출
        val beforeGija = s.substringBefore("기자", missingDelimiterValue = s).trim()
        if (beforeGija.isNotBlank() && beforeGija != s) {
            // "홍길동" 형태가 됨
            return beforeGija
                .substringAfterLast(" ") // 혹시 "정치부 홍길동" 같은 경우 마지막 토큰
                .trim()
                .takeIf { it.isNotBlank() }
        }

        // 2) 괄호/이메일/부서/슬래시/점 등 뒤에 붙는 정보 제거 시도
        // ex) "홍길동" "(서울=연합뉴스)" 같은 이상 케이스 정리
        val cleaned = s
            .substringBefore("(")
            .substringBefore("[")
            .substringBefore("<")
            .substringBefore("|")
            .substringBefore("/")
            .substringBefore("·")
            .trim()

        // 3) 마지막 토큰을 이름 후보로
        val lastToken = cleaned.split(" ").lastOrNull()?.trim().orEmpty()
        return lastToken.takeIf { it.isNotBlank() }
    }

    /**
     * ✅ 4개 섹션 공용 본문 포맷터
     *
     * 목표:
     * - 경제 기사처럼 p/br 구조가 이상해도 문단을 "무조건" 만들기
     * - \n 남발 방지: 최종 결과는 문단 구분 \n\n 까지만 허용
     * - strong/특수기호 소제목 문단 분리
     */
    private fun extractFormattedContent(contentEl: Element?): String {
        if (contentEl == null) return ""

        // 0) 불필요 제거 (광고/기자 박스/스크립트 등)
        contentEl.select(
            "script, style, figure, figcaption, iframe, " +
                    ".img_desc, .byline, .copyright, " +
                    ".media_end_head_journalist_layer, .reporter_area"
        ).remove()

        // 1) 구조 기반 개행 삽입(진짜 \n)
        // br -> \n
        contentEl.select("br").forEach { br ->
            br.after(TextNode("\n"))
        }

        // p -> 문단 경계 \n\n (빈 p는 제외)
        contentEl.select("p").forEach { p ->
            if (p.text().trim().isNotEmpty()) {
                p.before(TextNode("\n\n"))
                p.after(TextNode("\n\n"))
            }
        }

        // strong 소제목 처리:
        // - p 밖에서 단독/소제목처럼 등장하면 문단 분리
        // - 길이가 너무 짧으면(예: 1~2자) 문단 분리 안 함
        contentEl.select("strong").forEach { s ->
            val t = s.text().trim()
            if (t.isEmpty()) return@forEach

            val parentTag = s.parent()?.tagName()?.lowercase()
            val insideP = parentTag == "p"

            if (!insideP && t.length >= 5) {
                s.before(TextNode("\n\n"))
                s.after(TextNode("\n\n"))
            }
        }

        // div/section/article 같은 블록도 일부 기사에서 문단 역할
        // - 안에 p가 없고
        // - 자기 텍스트(ownText)가 어느 정도 길면 문단 경계 부여
        contentEl.select("div, section, article, li").forEach { b ->
            val hasNestedP = b.selectFirst("p") != null
            val own = b.ownText().trim()

            if (!hasNestedP && own.length >= 40) {
                b.after(TextNode("\n\n"))
            }
        }

        // 2) 텍스트 추출 (wholeText로 \n 유지)
        val raw = contentEl.wholeText()

        // 3) 특수기호 기반 소제목 문단 분리 (▶※■□◆◇ 등)
        val withMarkers = raw
            .replace(Regex("(?m)^[ \\t]*([▶▷※■□◆◇•●])"), "\n\n$1")
            .replace(Regex("\\s*([▶▷※■□◆◇•●])\\s*"), "\n\n$1 ")

        val normalized = normalizeNewlines(withMarkers)

        // 4) 경제 기사처럼 개행이 0개로 끝나는 케이스 -> fallback 문단화
        return if (normalized.contains('\n')) normalized else fallbackParagraphize(normalized)
    }

    /**
     * ✅ 개행 남발 방지 핵심
     * - 3줄 이상 -> 무조건 2줄(\n\n)
     * - 공백만 있는 줄 정리
     * - 줄 시작/끝 공백 정리
     */
    private fun normalizeNewlines(input: String): String {
        var s = input

        s = s.replace('\u00A0', ' ')
        s = s.replace("\r\n", "\n").replace("\r", "\n")

        // 줄 끝 공백 제거 / 줄 시작 공백 제거
        s = s.replace(Regex("[ \\t]+\\n"), "\n")
        s = s.replace(Regex("\\n[ \\t]+"), "\n")

        // 공백만 있는 줄 -> 문단 구분으로 정리
        s = s.replace(Regex("\\n\\s*\\n"), "\n\n")

        // 3개 이상 연속 개행은 2개로 강제
        s = s.replace(Regex("\\n{3,}"), "\n\n")

        // 연속 공백 축소
        s = s.replace(Regex("[ \\t]{2,}"), " ")

        return s.trim()
    }

    /**
     * p/br 없이 텍스트 덩어리로 오는 기사(경제에서 자주 발생)용 fallback
     * - 문장 단위로 분리 후
     * - 2~3문장 또는 길이 기준으로 문단 생성
     * - 결과는 normalizeNewlines로 마무리
     */
    private fun fallbackParagraphize(text: String): String {
        val t = text.trim()
        if (t.isEmpty()) return t

        // 먼저 기호/대괄호 소제목을 문단 시작으로 강제
        val pre = t
            .replace(Regex("\\s*([▶▷※■□◆◇•●])\\s*"), "\n\n$1 ")
            .replace(Regex("\\s*(\\[[^\\]]+\\])\\s*"), "\n\n$1 ")

        // 문장 경계 분리(한국어/영문 혼합)
        val sentences = pre
            .split(Regex("(?<=[.!?])\\s+|(?<=[다요함])\\s+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (sentences.isEmpty()) return pre

        val sb = StringBuilder()
        var count = 0
        var len = 0

        fun flush() {
            if (sb.isNotEmpty() && !sb.endsWith("\n\n")) sb.append("\n\n")
            count = 0
            len = 0
        }

        for (s in sentences) {
            if (sb.isNotEmpty() && sb.last() != '\n' && !sb.endsWith("\n\n")) sb.append(' ')
            sb.append(s)
            count++
            len += s.length

            // 문단 분리 기준(너무 잘게 나뉘지 않게)
            if (count >= 3 || len >= 220) {
                flush()
            }
        }

        return normalizeNewlines(sb.toString())
    }

    private fun StringBuilder.endsWith(s: String): Boolean {
        if (this.length < s.length) return false
        for (i in s.indices) {
            if (this[this.length - s.length + i] != s[i]) return false
        }
        return true
    }

    /* =========================
     * URL에서 섹션 타입 감지
     * ========================= */
    private fun detectSectionFromUrl(url: String): ArticleSection? =
        when {
            url.contains("sid=100") || url.contains("/100/") -> ArticleSection.POLITICS
            url.contains("sid=101") || url.contains("/101/") -> ArticleSection.ECONOMY
            url.contains("sid=102") || url.contains("/102/") -> ArticleSection.SOCIAL
            url.contains("sid=105") || url.contains("/105/") -> ArticleSection.TECHNOLOGY
            else -> null
        }

    /**
     * 네이버 뉴스 날짜 파싱
     */
    private fun parseNaverDate(dateStr: String?): LocalDateTime {
        if (dateStr.isNullOrBlank()) return LocalDateTime.now()

        return try {
            // "2025-12-11 14:30:00"
            if (dateStr.contains("-") && dateStr.contains(":") && dateStr.length >= 19) {
                LocalDateTime.parse(
                    dateStr.substring(0, 19),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
            } else {
                // "2025.12.11. 오후 2:30"
                val isPm = dateStr.contains("오후")
                val clean = dateStr.replace("오전", "").replace("오후", "").trim()

                var dt = LocalDateTime.parse(
                    clean,
                    DateTimeFormatter.ofPattern("yyyy.MM.dd. h:mm")
                )

                if (isPm && dt.hour != 12) dt = dt.plusHours(12)
                else if (!isPm && dt.hour == 12) dt = dt.minusHours(12)

                dt
            }
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }

    /* =========================
     * DTO
     * ========================= */
    data class Candidate(
        val url: String,
        val section: ArticleSection,
        val title: String,
        val content: String,
        val thumbnailUrl: String?,
        val publisher: String?,
        val reporter: String?, // ✅ 추가
        val publishedAt: LocalDateTime
    )

    data class Detail(
        val title: String,
        val content: String,
        val publishedAt: LocalDateTime,
        val publisher: String,
        val thumbnailUrl: String?,
        val reporter: String? // ✅ 추가
    )
}
