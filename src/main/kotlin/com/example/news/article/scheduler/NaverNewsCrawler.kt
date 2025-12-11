package com.example.news.article.scheduler

import com.example.news.article.domain.Article
import com.example.news.article.domain.ArticleSection
import com.example.news.article.repository.ArticleRepository
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * 네이버 뉴스 크롤러 (헤드라인 + 많이 본 뉴스 중심)
 * - 5분마다 각 섹션의 주요 뉴스를 크롤링하여 DB에 저장
 * - 헤드라인 뉴스 + 많이 본 뉴스 위주로 수집
 * - 이미 존재하는 URL은 스킵 (중복 방지)
 */
@Component
class NaverNewsCrawler(
    private val articleRepository: ArticleRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // 네이버 뉴스 섹션 ID -> ArticleSection 매핑
    private val sectionMap = mapOf(
        "100" to ArticleSection.POLITICS,    // 정치
        "101" to ArticleSection.ECONOMY,     // 경제
        "102" to ArticleSection.SOCIAL,      // 사회
        "105" to ArticleSection.TECHNOLOGY   // IT/과학
    )

    private val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    /**
     * 5분마다 실행 (이전 작업 종료 후 5분 뒤 실행)
     */
    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    fun crawlNewsBatch() {
        log.info("🔄 [Batch] 네이버 뉴스 크롤링 시작: ${LocalDateTime.now()}")

        var totalSaved = 0

        // 1. 각 섹션별 헤드라인 + 주요 뉴스 크롤링
        sectionMap.forEach { (sectionId, sectionType) ->
            try {
                totalSaved += crawlSectionHeadlines(sectionId, sectionType)
            } catch (e: Exception) {
                log.error("❌ 섹션($sectionType) 크롤링 실패: ${e.message}")
            }
        }

        // 2. 많이 본 뉴스 랭킹 크롤링
        try {
            totalSaved += crawlPopularNews()
        } catch (e: Exception) {
            log.error("❌ 많이 본 뉴스 크롤링 실패: ${e.message}")
        }

        log.info("🏁 [Batch] 크롤링 종료 - 총 ${totalSaved}건 저장")
    }

    /**
     * 섹션별 헤드라인 뉴스 크롤링
     * - 상단 주요 기사 (헤드라인 영역)
     * - 에디터 추천 기사
     */
    @Transactional
    fun crawlSectionHeadlines(sectionId: String, sectionType: ArticleSection): Int {
        val listUrl = "https://news.naver.com/section/$sectionId"

        val doc = Jsoup.connect(listUrl)
            .userAgent(userAgent)
            .timeout(10000)
            .get()

        val collectedUrls = mutableSetOf<String>()
        var savedCount = 0

        // 1. 헤드라인 영역 (상단 대형 기사)
        val headlineItems = doc.select(".sa_item_flex, .section_article.as_headline .sa_item, .ct_head .sa_item")
        log.debug("   헤드라인 기사 수: ${headlineItems.size}")

        for (item in headlineItems) {
            val saved = processArticleItem(item, sectionType, collectedUrls)
            if (saved) savedCount++
        }

        // 2. 주요 기사 영역 (상위 10개만)
        val mainItems = doc.select(".sa_list .sa_item").take(10)
        for (item in mainItems) {
            val saved = processArticleItem(item, sectionType, collectedUrls)
            if (saved) savedCount++
        }

        if (savedCount > 0) {
            log.info("   ✅ [$sectionType] 헤드라인/주요 기사 ${savedCount}건 저장")
        }

        return savedCount
    }

    /**
     * 많이 본 뉴스 크롤링 (전체 섹션)
     */
    @Transactional
    fun crawlPopularNews(): Int {
        var savedCount = 0
        val collectedUrls = mutableSetOf<String>()

        // 각 섹션별 많이 본 뉴스
        sectionMap.forEach { (sectionId, sectionType) ->
            try {
                val rankingUrl = "https://news.naver.com/section/$sectionId"
                val doc = Jsoup.connect(rankingUrl)
                    .userAgent(userAgent)
                    .timeout(10000)
                    .get()

                // 많이 본 뉴스 영역 선택
                val popularItems = doc.select(".section_article.as_main_popular .sa_item, .ranking_item, .section_main_popular .sa_item")

                for (item in popularItems.take(10)) {  // 상위 10개만
                    val saved = processArticleItem(item, sectionType, collectedUrls)
                    if (saved) savedCount++
                }
            } catch (e: Exception) {
                log.warn("   ⚠️ [$sectionType] 인기 뉴스 크롤링 실패: ${e.message}")
            }
        }

        // 전체 랭킹 페이지도 크롤링
        try {
            val count = crawlRankingPage(collectedUrls)
            savedCount += count
        } catch (e: Exception) {
            log.warn("   ⚠️ 랭킹 페이지 크롤링 실패: ${e.message}")
        }

        if (savedCount > 0) {
            log.info("   ✅ [인기뉴스] ${savedCount}건 저장")
        }

        return savedCount
    }

    /**
     * 네이버 뉴스 랭킹 페이지 크롤링
     */
    private fun crawlRankingPage(collectedUrls: MutableSet<String>): Int {
        var savedCount = 0

        // 랭킹 페이지 URL들
        val rankingUrls = listOf(
            "https://news.naver.com/main/ranking/popularDay.naver",
            "https://news.naver.com/main/ranking/popularMemo.naver"
        )

        for (rankingUrl in rankingUrls) {
            try {
                val doc = Jsoup.connect(rankingUrl)
                    .userAgent(userAgent)
                    .timeout(10000)
                    .get()

                // 랭킹 기사 선택
                val rankingItems = doc.select(".rankingnews_box .list_content a, .ranking_list .list_title")

                for (item in rankingItems.take(20)) {
                    val link = item.attr("href").let {
                        if (it.startsWith("/")) "https://news.naver.com$it" else it
                    }

                    if (link.isBlank() || collectedUrls.contains(link) || articleRepository.existsByUrl(link)) {
                        continue
                    }

                    collectedUrls.add(link)

                    // 상세 페이지에서 정보 추출
                    val detail = getArticleDetail(link) ?: continue
                    val sectionType = detectSectionFromUrl(link) ?: ArticleSection.SOCIAL

                    val article = Article(
                        section = sectionType,
                        title = item.text().ifBlank { detail.title },
                        content = detail.content,
                        url = link,
                        thumbnailUrl = detail.thumbnailUrl,
                        source = "NAVER",
                        publisher = detail.publisher,
                        publishedAt = detail.publishedAt
                    )

                    articleRepository.save(article)
                    savedCount++
                    Thread.sleep(200)
                }
            } catch (e: Exception) {
                log.warn("   ⚠️ 랭킹 페이지 처리 실패: ${e.message}")
            }
        }

        return savedCount
    }

    /**
     * 기사 아이템 처리 및 저장
     */
    private fun processArticleItem(
        item: Element,
        sectionType: ArticleSection,
        collectedUrls: MutableSet<String>
    ): Boolean {
        try {
            // 제목 & 링크 추출
            val titleEl = item.selectFirst(".sa_text_title, .sa_text_strong, a[class*=title]")
                ?: item.selectFirst("a")
                ?: return false

            val link = titleEl.attr("href").let {
                if (it.startsWith("/")) "https://news.naver.com$it" else it
            }

            if (link.isBlank() || !link.contains("news.naver.com")) return false

            // 중복 체크
            if (collectedUrls.contains(link) || articleRepository.existsByUrl(link)) {
                return false
            }

            collectedUrls.add(link)

            val title = titleEl.text()

            // 썸네일
            val thumbEl = item.selectFirst("img")
            val thumbnail = thumbEl?.attr("data-src")?.ifBlank { thumbEl.attr("src") }

            // 상세 페이지에서 본문, 날짜, 언론사 가져오기
            val detail = getArticleDetail(link) ?: return false

            // Article 엔티티 생성 및 저장
            val article = Article(
                section = sectionType,
                title = title.ifBlank { detail.title },
                content = detail.content,
                url = link,
                thumbnailUrl = thumbnail ?: detail.thumbnailUrl,
                source = "NAVER",
                publisher = detail.publisher,
                publishedAt = detail.publishedAt
            )

            articleRepository.save(article)
            Thread.sleep(200)  // 차단 방지

            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * 상세 페이지에서 본문, 발행일, 언론사, 제목, 썸네일 추출
     */
    private fun getArticleDetail(url: String): ArticleDetailInfo? {
        return try {
            val doc = Jsoup.connect(url)
                .userAgent(userAgent)
                .timeout(5000)
                .get()

            // 1. 제목
            val title = doc.selectFirst(".media_end_head_headline, #title_area span, .end_tit")?.text() ?: ""

            // 2. 날짜 파싱
            val dateStr = doc.selectFirst(".media_end_head_info_datestamp_time")?.attr("data-date-time")
                ?: doc.selectFirst(".media_end_head_info_datestamp_time")?.text()
                ?: doc.selectFirst("span.media_end_head_info_datestamp_time")?.text()
            val publishedAt = parseNaverDate(dateStr)

            // 3. 언론사
            val publisher = doc.selectFirst(".media_end_head_top_logo img")?.attr("title")
                ?: doc.selectFirst(".media_end_linked_more_point")?.text()
                ?: doc.selectFirst(".media_end_head_top_logo_text")?.text()
                ?: "Unknown"

            // 4. 본문
            val contentEl = doc.selectFirst("#dic_area, #newsct_article, .newsct_article, #articeBody")

            // 불필요 태그 제거
            contentEl?.select(".img_desc, .byline, .copyright, .media_end_head_journalist_layer, script, style")?.remove()

            val content = contentEl?.text() ?: ""

            // 5. 썸네일
            val thumbnailUrl = doc.selectFirst("meta[property=og:image]")?.attr("content")
                ?: doc.selectFirst(".end_photo_org img, #img1")?.attr("src")

            ArticleDetailInfo(title, content, publishedAt, publisher, thumbnailUrl)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * URL에서 섹션 타입 감지
     */
    private fun detectSectionFromUrl(url: String): ArticleSection? {
        return when {
            url.contains("sid=100") || url.contains("/100/") -> ArticleSection.POLITICS
            url.contains("sid=101") || url.contains("/101/") -> ArticleSection.ECONOMY
            url.contains("sid=102") || url.contains("/102/") -> ArticleSection.SOCIAL
            url.contains("sid=105") || url.contains("/105/") -> ArticleSection.TECHNOLOGY
            else -> null
        }
    }

    /**
     * 네이버 뉴스 날짜 파싱
     */
    private fun parseNaverDate(dateStr: String?): LocalDateTime {
        if (dateStr.isNullOrBlank()) return LocalDateTime.now()

        return try {
            // data-date-time 속성 형식: "2025-12-11 14:30:00"
            if (dateStr.contains("-") && dateStr.contains(":") && dateStr.length >= 19) {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                return LocalDateTime.parse(dateStr.substring(0, 19), formatter)
            }

            // 텍스트 형식: "2025.12.11. 오후 2:30"
            val isPm = dateStr.contains("오후")
            val cleanStr = dateStr.replace("오전", "").replace("오후", "").trim()

            val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd. h:mm")
            var dt = LocalDateTime.parse(cleanStr, formatter)

            if (isPm && dt.hour != 12) {
                dt = dt.plusHours(12)
            } else if (!isPm && dt.hour == 12) {
                dt = dt.minusHours(12)
            }
            dt
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }

    /**
     * 상세 정보 DTO
     */
    data class ArticleDetailInfo(
        val title: String,
        val content: String,
        val publishedAt: LocalDateTime,
        val publisher: String,
        val thumbnailUrl: String?
    )
}
