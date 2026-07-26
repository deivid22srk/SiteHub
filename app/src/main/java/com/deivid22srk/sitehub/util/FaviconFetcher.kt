package com.deivid22srk.sitehub.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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

    suspend fun fetchTitle(url: String): String = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")

            val html = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            val titleRegex = Regex("<title[^>]*>([^<]+)</title>", RegexOption.IGNORE_CASE)
            titleRegex.find(html)?.groupValues?.get(1)?.trim() ?: extractDomain(url)
        } catch (e: Exception) {
            extractDomain(url)
        }
    }
}
