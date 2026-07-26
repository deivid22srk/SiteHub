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
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
            connection.instanceFollowRedirects = true

            val html = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            val titleRegex = Regex("<title[^>]*>([^<]+)</title>", RegexOption.IGNORE_CASE)
            titleRegex.find(html)?.groupValues?.get(1)?.trim() ?: extractDomain(url)
        } catch (e: Exception) {
            extractDomain(url)
        }
    }
}
