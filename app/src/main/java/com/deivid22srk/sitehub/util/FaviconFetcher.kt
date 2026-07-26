package com.deivid22srk.sitehub.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object FaviconFetcher {

    fun getFaviconUrl(siteUrl: String): String {
        val domain = extractDomain(siteUrl)
        return "https://www.google.com/s2/favicons?domain=$domain&sz=128"
    }

    fun extractDomain(url: String): String {
        return try {
            val cleaned = url.removePrefix("https://").removePrefix("http://")
            cleaned.split("/").firstOrNull() ?: cleaned
        } catch (e: Exception) {
            url
        }
    }

    fun extractTitleFromUrl(url: String): String? {
        val xboxProduct = Regex("""play\.xbox\.com/products/[^/]+/(.+)""", RegexOption.IGNORE_CASE)
        xboxProduct.find(url)?.let { match ->
            val slug = match.groupValues[1].split("?").first()
            return slug.replace("-", " ")
                .split(" ")
                .joinToString(" ") { word ->
                    if (word.length <= 2 && word.lowercase() !in listOf("of", "the", "a", "an")) {
                        word.uppercase()
                    } else {
                        word.replaceFirstChar { it.uppercase() }
                    }
                }
        }

        val steamApp = Regex("""store\.steampowered\.com/app/\d+/(.+)""", RegexOption.IGNORE_CASE)
        steamApp.find(url)?.let { match ->
            val slug = match.groupValues[1].split("/").first().split("?").first()
            return slug.replace("_", " ").replace("-", " ")
                .split(" ")
                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
        }

        return null
    }

    suspend fun fetchBestFavicon(url: String): String = withContext(Dispatchers.IO) {
        val domain = extractDomain(url)
        val baseUrl = url.substringBefore("/", url).let {
            if (it.startsWith("http")) it else "https://$it"
        }

        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
            connection.instanceFollowRedirects = true

            val html = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            val appleTouch = Regex("""<link[^>]+rel=["']apple-touch-icon["'][^>]+href=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                .find(html)?.groupValues?.get(1)
            if (appleTouch != null) return@withContext resolveUrl(baseUrl, appleTouch)

            val appleTouch2 = Regex("""<link[^>]+href=["']([^"']+)["'][^>]+rel=["']apple-touch-icon["']""", RegexOption.IGNORE_CASE)
                .find(html)?.groupValues?.get(1)
            if (appleTouch2 != null) return@withContext resolveUrl(baseUrl, appleTouch2)

            val iconLink = Regex("""<link[^>]+rel=["'](?:shortcut )?icon["'][^>]+href=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                .find(html)?.groupValues?.get(1)
            if (iconLink != null) return@withContext resolveUrl(baseUrl, iconLink)

            val iconLink2 = Regex("""<link[^>]+href=["']([^"']+)["'][^>]+rel=["'](?:shortcut )?icon["']""", RegexOption.IGNORE_CASE)
                .find(html)?.groupValues?.get(1)
            if (iconLink2 != null) return@withContext resolveUrl(baseUrl, iconLink2)
        } catch (_: Exception) {}

        try {
            val favUrl = "$baseUrl/favicon.ico"
            val conn = URL(favUrl).openConnection() as HttpURLConnection
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.requestMethod = "HEAD"
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
            val code = conn.responseCode
            conn.disconnect()
            if (code == 200) return@withContext favUrl
        } catch (_: Exception) {}

        getFaviconUrl(url)
    }

    private fun resolveUrl(baseUrl: String, href: String): String {
        return when {
            href.startsWith("http") -> href
            href.startsWith("//") -> "https:$href"
            href.startsWith("/") -> baseUrl + href
            else -> "$baseUrl/$href"
        }
    }

    suspend fun fetchTitle(url: String): String = withContext(Dispatchers.IO) {
        val fromUrl = extractTitleFromUrl(url)
        if (fromUrl != null) return@withContext fromUrl

        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
            connection.instanceFollowRedirects = true

            val html = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            val ogTitle = Regex("""<meta[^>]+property=["']og:title["'][^>]+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                .find(html)?.groupValues?.get(1)?.trim()
            if (ogTitle != null) return@withContext ogTitle

            val titleRegex = Regex("<title[^>]*>([^<]+)</title>", RegexOption.IGNORE_CASE)
            val rawTitle = titleRegex.find(html)?.groupValues?.get(1)?.trim()
            if (rawTitle != null) {
                val cleaned = rawTitle
                    .replace(Regex("""\s*[|\-–—]\s*(Xbox|Play|Steam|Official|Site).*$""", RegexOption.IGNORE_CASE), "")
                    .trim()
                return@withContext if (cleaned.isNotBlank()) cleaned else rawTitle
            }

            extractDomain(url)
        } catch (e: Exception) {
            extractDomain(url)
        }
    }
}
