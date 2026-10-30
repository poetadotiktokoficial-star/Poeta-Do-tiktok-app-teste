package com.example.data.model

enum class ContentType(val label: String, val iconName: String) {
    BOOK("Livro / E-book", "book"),
    DOCUMENT("Documento / PDF", "description"),
    AUDIO("Áudio / Podcast", "mic"),
    VIDEO("Vídeo / Aula", "play_circle")
}

enum class GenerationType(val title: String, val shortDesc: String) {
    DARK_SCRIPT(
        title = "Roteiro Vídeo Dark",
        shortDesc = "Hook viral 3s, retenção, locução, cenas de B-roll e sonoplastia"
    ),
    POST_IMAGE(
        title = "Post & Prompts de Imagem",
        shortDesc = "Carrosséis, legendas, hashtags e prompts para Midjourney/DALL-E"
    ),
    NICHE(
        title = "Nichos & Ângulos Blue Ocean",
        shortDesc = "Subnichos ocultos, dores do público e estratégia de posicionamento"
    ),
    INFOPRODUCT(
        title = "Ideias de Infoprodutos",
        shortDesc = "Formatos (e-books, templates, kits), promessa PUV, módulos e preços"
    )
}

enum class ContentTone(val label: String, val description: String) {
    DARK_MISTERIOSO("Sombrio & Misterioso", "Clima de revelação secreta, mistério e suspense intenso"),
    PERSUASIVO_AFILIADO("Persuasivo & Conversão", "Foco em gatilhos de urgência, dor e solução rápida"),
    HISTORIAS_IMPACTO("Storytelling Cinematográfico", "Narrativa envolvente com jornada do herói e clímax"),
    EDUCATIVO_DIRETO("Autoridade & Direto", "Informação densa e clara sem enrolação"),
    PROVOCATIVO_CURIOSO("Desafiador & Provocativo", "Questiona certezas comuns e fisga pelo ego")
}

enum class TargetPlatform(val label: String) {
    TIKTOK_REELS_SHORTS("Short-form (TikTok, Reels, Shorts)"),
    YOUTUBE_LONGO("YouTube (Vídeos Longos 3-8min)"),
    INSTAGRAM_CARROSSEL("Instagram Carrossel"),
    OMNICHANNEL("Todas as Redes")
}

data class ContentItem(
    val id: Long = 0,
    val title: String,
    val type: ContentType,
    val authorOrSource: String,
    val summary: String,
    val contentBody: String,
    val tags: List<String> = emptyList(),
    val sourceUrl: String = "",
    val fileSize: String = "",
    val dateAdded: Long = System.currentTimeMillis()
) {
    val wordCount: Int
        get() = contentBody.split("\\s+".toRegex()).count { it.isNotBlank() }

    val readingTimeMinutes: Int
        get() = maxOf(1, (wordCount / 200.0).toInt())

    val voiceoverTimeMinutes: Double
        get() = String.format(java.util.Locale.US, "%.1f", wordCount / 140.0).toDoubleOrNull() ?: 1.0
}

data class GeneratedCreation(
    val id: Long = 0,
    val sourceContentId: Long? = null,
    val sourceContentTitle: String = "Material Geral",
    val type: GenerationType,
    val title: String,
    val headline: String,
    val mainContent: String,
    val visualPrompts: String = "",
    val extraDetails: String = "",
    val tone: ContentTone = ContentTone.DARK_MISTERIOSO,
    val platform: TargetPlatform = TargetPlatform.TIKTOK_REELS_SHORTS,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
