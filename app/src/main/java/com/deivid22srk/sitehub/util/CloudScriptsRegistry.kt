package com.deivid22srk.sitehub.util

object CloudScriptsRegistry {

    data class CloudScript(
        val id: String,
        val name: String,
        val version: String,
        val description: String,
        val fileName: String,
        val matchDomains: List<String>
    )

    val scripts = listOf(
        CloudScript(
            id = "auto-clique-streaming-xbox",
            name = "Auto-Clique Streaming Xbox",
            version = "1.0",
            description = "Clica automaticamente no botão 'Prepare-se para o Streaming' na página de um jogo do Xbox Play.",
            fileName = "auto-clique-streaming-xbox.shub",
            matchDomains = listOf("play.xbox.com", "www.xbox.com")
        )
    )

    fun getScriptsForUrl(url: String): List<CloudScript> {
        val domain = try {
            url.removePrefix("https://").removePrefix("http://").split("/").first()
        } catch (_: Exception) { "" }

        return scripts.filter { script ->
            script.matchDomains.any { domain.endsWith(it) }
        }
    }

    fun isCloudSupported(url: String): Boolean = getScriptsForUrl(url).isNotEmpty()
}
