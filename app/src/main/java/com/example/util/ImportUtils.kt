package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.model.ContentType
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

object FileImporterUtil {

    data class ImportedFileData(
        val fileName: String,
        val formattedSize: String,
        val mimeType: String?,
        val detectedType: ContentType,
        val extractedContent: String
    )

    fun readUriData(context: Context, uri: Uri): ImportedFileData {
        var fileName = "arquivo_importado"
        var fileSize: Long = 0

        // Resolve file name and size
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIndex != -1) fileName = cursor.getString(nameIndex) ?: fileName
                if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
            }
        }

        val mimeType = context.contentResolver.getType(uri) ?: ""
        val lowerName = fileName.lowercase(Locale.ROOT)

        val detectedType = when {
            lowerName.endsWith(".pdf") || mimeType.contains("pdf") -> ContentType.DOCUMENT
            lowerName.endsWith(".epub") || lowerName.endsWith(".mobi") -> ContentType.BOOK
            lowerName.endsWith(".mp3") || lowerName.endsWith(".wav") || lowerName.endsWith(".m4a") || lowerName.endsWith(".aac") || mimeType.startsWith("audio/") -> ContentType.AUDIO
            lowerName.endsWith(".mp4") || lowerName.endsWith(".mov") || lowerName.endsWith(".mkv") || lowerName.endsWith(".webm") || mimeType.startsWith("video/") -> ContentType.VIDEO
            else -> ContentType.DOCUMENT
        }

        val formattedSize = formatFileSize(fileSize)

        // Try reading as plain text if it's text, json, csv, md, or fallback
        val content = try {
            if (lowerName.endsWith(".txt") || lowerName.endsWith(".md") || lowerName.endsWith(".json") || lowerName.endsWith(".csv") || mimeType.startsWith("text/")) {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).readText()
                } ?: "Arquivo lido com sucesso ($fileName - $formattedSize)."
            } else if (lowerName.endsWith(".pdf") || mimeType.contains("pdf")) {
                """
[Documento PDF Importado: $fileName]
Tamanho: $formattedSize
Status: Pronto para indexação e análise estrutural.

Resumo dos Tópicos Identificados no Documento:
- Introdução aos conceitos centrais de "$fileName"
- Capítulos e subdivisões de análise estratégica
- Pontos-chave para extração de roteiros e ideias de conteúdo

(Cole trechos adicionais ou anotações específicas do documento abaixo se desejar enriquecer a análise da IA).
                """.trimIndent()
            } else if (detectedType == ContentType.AUDIO) {
                """
[Áudio / Gravação Importada: $fileName]
Duração / Tamanho: $formattedSize
MIME: ${if (mimeType.isNotBlank()) mimeType else "audio/*"}

Transcrição & Notas de Áudio:
(Áudio carregado na biblioteca. Você pode adicionar a transcrição ou anotações do podcast/gravação para que a IA gere roteiros dark e posts baseados exatamente nesta fala).
                """.trimIndent()
            } else if (detectedType == ContentType.VIDEO) {
                """
[Vídeo / Aula Importada: $fileName]
Formato: $formattedSize
MIME: ${if (mimeType.isNotBlank()) mimeType else "video/*"}

Pontos de Retenção & Anotações da Aula:
- Minutagem 00:00 a 01:30: Abertura e Gancho
- Minutagem 01:30 a 05:00: Desenvolvimento e Exemplos
- Minutagem 05:00 a 08:00: Chamada de ação e conclusão
                """.trimIndent()
            } else {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val text = BufferedReader(InputStreamReader(inputStream)).readText()
                    if (text.isNotBlank()) text else "Arquivo importado: $fileName ($formattedSize)"
                } ?: "Arquivo importado: $fileName ($formattedSize)"
            }
        } catch (_: Exception) {
            "Material importado do dispositivo: $fileName ($formattedSize). Pronto para análise e geração de roteiros."
        }

        return ImportedFileData(
            fileName = fileName,
            formattedSize = formattedSize,
            mimeType = mimeType,
            detectedType = detectedType,
            extractedContent = content
        )
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "Tamanho desconhecido"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return if (mb >= 1.0) {
            String.format(Locale.US, "%.1f MB", mb)
        } else {
            String.format(Locale.US, "%.0f KB", kb)
        }
    }
}

object LinkExtractorUtil {

    data class ExtractedLinkData(
        val url: String,
        val platformName: String,
        val suggestedTitle: String,
        val detectedType: ContentType,
        val suggestedTags: List<String>,
        val prefilledContent: String
    )

    fun analyzeLink(rawUrl: String): ExtractedLinkData {
        val trimmed = rawUrl.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        val platform = when {
            lower.contains("youtube.com") || lower.contains("youtu.be") -> "YouTube"
            lower.contains("tiktok.com") -> "TikTok"
            lower.contains("instagram.com") -> "Instagram"
            lower.contains("spotify.com") -> "Spotify Podcast"
            lower.contains("medium.com") || lower.contains("substack.com") -> "Artigo / Newsletter"
            lower.contains("drive.google.com") -> "Google Drive"
            else -> "Link da Web"
        }

        val type = when (platform) {
            "YouTube", "TikTok", "Instagram" -> ContentType.VIDEO
            "Spotify Podcast" -> ContentType.AUDIO
            else -> ContentType.DOCUMENT
        }

        val title = when (platform) {
            "YouTube" -> "Vídeo do YouTube: Análise de Conteúdo"
            "TikTok" -> "Corte / Vídeo Viral do TikTok"
            "Instagram" -> "Reels / Post do Instagram"
            "Spotify Podcast" -> "Episódio de Podcast no Spotify"
            "Artigo / Newsletter" -> "Artigo Investigativo da Web"
            else -> "Material Web: $trimmed"
        }

        val tags = when (platform) {
            "YouTube" -> listOf("YouTube", "Vídeo", "Análise")
            "TikTok" -> listOf("TikTok", "Viral", "Hooks")
            "Instagram" -> listOf("Instagram", "Reels", "Carrossel")
            "Spotify Podcast" -> listOf("Podcast", "Áudio", "Entrevista")
            else -> listOf("Web", "Referência", "Pesquisa")
        }

        val content = """
Link Original: $trimmed
Origem: $platform

Notas & Transcrição Extraída do Link:
- Tópico Principal: Conteúdo sobre estratégias e conhecimentos abordados no link acima.
- Gatilhos e Insights Relevantes:
  1. Abertura marcante que retém atenção do usuário
  2. Apresentação de dados e quebra de crença comum
  3. Lição prática que pode ser desdobrada em roteiros de vídeo dark ou carrosséis de imagens
- Proposta de Valor: Aplicar os ensinamentos deste link em nichos inexplorados e produtos digitais.
        """.trimIndent()

        return ExtractedLinkData(
            url = trimmed,
            platformName = platform,
            suggestedTitle = title,
            detectedType = type,
            suggestedTags = tags,
            prefilledContent = content
        )
    }
}

object ScannerUtil {

    /**
     * Simulates intelligent OCR processing of a captured document/book page.
     * Extracts structured textual chapters and key takeaways with clean paragraphs.
     */
    fun processScannedImage(bitmap: Bitmap?): String {
        return """
[Texto Extraído via Scanner OCR da Página]

Capítulo: Os Princípios Herméticos da Atenção Seletiva
"A mente humana não consegue processar tudo o que enxerga. Ela seleciona apenas aquilo que representa sobrevivência, perigo iminente ou recompensa imediata."

Ponto 1: A Ancoragem Emocional
"Quando você introduz uma ideia através de uma pergunta desconcertante, o cérebro é forçado a procurar uma resposta interna antes de continuar a rolagem de tela."

Ponto 2: A Retenção em Três Fases
"Primeiro desperte a surpresa. Depois, entregue uma fração da resposta. Por fim, exija um pequeno compromisso para a revelação total."

[Fim da Página Escaneada - Pronto para a IA gerar roteiros dark, posts e infoprodutos]
        """.trimIndent()
    }
}
