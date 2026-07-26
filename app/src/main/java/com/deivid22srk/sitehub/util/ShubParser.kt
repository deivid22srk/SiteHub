package com.deivid22srk.sitehub.util

import android.content.Context
import android.net.Uri
import com.deivid22srk.sitehub.data.model.UserscriptEntity
import org.json.JSONObject
import java.io.BufferedInputStream
import java.util.zip.ZipInputStream

data class ShubManifest(
    val name: String,
    val version: String,
    val description: String,
    val match: List<String>
)

object ShubParser {

    fun parse(context: Context, uri: Uri, siteId: Long): Result<UserscriptEntity> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("Não foi possível abrir o arquivo"))

            var manifest: ShubManifest? = null
            var scriptContent: String? = null

            ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val content = zis.readBytes().decodeToString()
                    when {
                        entry.name.equals("manifest.json", ignoreCase = true) -> {
                            val json = JSONObject(content)
                            val matchArray = json.optJSONArray("match")
                            val matchList = mutableListOf<String>()
                            if (matchArray != null) {
                                for (i in 0 until matchArray.length()) {
                                    matchList.add(matchArray.getString(i))
                                }
                            }
                            manifest = ShubManifest(
                                name = json.optString("name", "Sem nome"),
                                version = json.optString("version", "1.0"),
                                description = json.optString("description", ""),
                                match = matchList
                            )
                        }
                        entry.name.endsWith(".js", ignoreCase = true) -> {
                            scriptContent = content
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            val m = manifest ?: return Result.failure(Exception("manifest.json não encontrado no pacote .shub"))
            val script = scriptContent ?: return Result.failure(Exception("Nenhum arquivo .js encontrado no pacote .shub"))

            Result.success(
                UserscriptEntity(
                    siteId = siteId,
                    name = m.name,
                    version = m.version,
                    description = m.description,
                    matchPatterns = m.match.joinToString("\n"),
                    scriptContent = script
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao importar: ${e.message}"))
        }
    }
}
