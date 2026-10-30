package com.example.data.repository

import com.example.ai.GeminiContentGenerator
import com.example.data.local.ContentEntity
import com.example.data.local.DarkCreatorDao
import com.example.data.local.GeneratedCreationEntity
import com.example.data.model.ContentItem
import com.example.data.model.ContentType
import com.example.data.model.ContentTone
import com.example.data.model.GeneratedCreation
import com.example.data.model.GenerationType
import com.example.data.model.TargetPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DarkCreatorRepository(
    private val dao: DarkCreatorDao,
    private val generator: GeminiContentGenerator = GeminiContentGenerator()
) {

    val allContents: Flow<List<ContentItem>> = dao.getAllContents().map { list ->
        list.map { it.toDomain() }
    }

    val allCreations: Flow<List<GeneratedCreation>> = dao.getAllCreations().map { list ->
        list.map { it.toDomain() }
    }

    val favoriteCreations: Flow<List<GeneratedCreation>> = dao.getFavoriteCreations().map { list ->
        list.map { it.toDomain() }
    }

    fun getCreationsByType(type: GenerationType): Flow<List<GeneratedCreation>> {
        return dao.getCreationsByType(type.name).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getContentById(id: Long): ContentItem? = withContext(Dispatchers.IO) {
        dao.getContentById(id)?.toDomain()
    }

    suspend fun getCreationById(id: Long): GeneratedCreation? = withContext(Dispatchers.IO) {
        dao.getCreationById(id)?.toDomain()
    }

    suspend fun saveContent(item: ContentItem): Long = withContext(Dispatchers.IO) {
        dao.insertContent(ContentEntity.fromDomain(item))
    }

    suspend fun deleteContent(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteContentById(id)
    }

    suspend fun saveCreation(creation: GeneratedCreation): Long = withContext(Dispatchers.IO) {
        dao.insertCreation(GeneratedCreationEntity.fromDomain(creation))
    }

    suspend fun toggleFavorite(id: Long, isFav: Boolean) = withContext(Dispatchers.IO) {
        dao.toggleFavorite(id, isFav)
    }

    suspend fun deleteCreation(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteCreationById(id)
    }

    suspend fun generateCreation(
        contentItem: ContentItem?,
        type: GenerationType,
        tone: ContentTone,
        platform: TargetPlatform,
        customInstructions: String = ""
    ): GeneratedCreation = withContext(Dispatchers.IO) {
        generator.generate(contentItem, type, tone, platform, customInstructions)
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val currentContents = dao.getAllContents().first()
        if (currentContents.isEmpty()) {
            val starterContents = listOf(
                ContentItem(
                    title = "As Leis Secretas da Persuasão & Manipulação Sutil",
                    type = ContentType.BOOK,
                    authorOrSource = "Dr. Robert S. & Análises Comportamentais",
                    summary = "Estudo profundo sobre psicologia reversa, quebra de padrão, ancoragem mental e vieses cognitivos na tomada de decisão.",
                    contentBody = """
Capítulo 1: O Efeito da Ilusão de Escolha
Quando você oferece duas opções estrategicamente selecionadas, a mente humana foca em escolher entre A e B, esquecendo completamente que a opção C (não comprar ou não agir) sequer existe.

Capítulo 2: Quebra de Padrão no Feed Moderno
O cérebro em repouso scaneia telas em frações de segundos. Para reter a atenção, você deve violar a expectativa inicial nos primeiros 2.5 segundos com uma afirmação contraintuitiva ou um visual que desafie a física cotidiana.

Capítulo 3: O Gatilho da Escassez Artificial
Tudo que é abundante perde valor perceptivo. Ao condicionar acesso restrito ou conhecimento para poucos selecionados, ativa-se o medo primitivo da perda (FOMO).
                    """.trimIndent(),
                    tags = listOf("Persuasão", "Psicologia", "Vendas", "Dark"),
                    dateAdded = System.currentTimeMillis() - 86400000 * 3
                ),
                ContentItem(
                    title = "Dossiê Confidencial: Mistérios e Sociedades Ocultas da História",
                    type = ContentType.DOCUMENT,
                    authorOrSource = "Arquivo Histórico Investigativo",
                    summary = "Pesquisa detalhada sobre manuscritos perdidos, conhecimentos herméticos esquecidos e teorias misteriosas.",
                    contentBody = """
Seção 1: O Manuscrito Enigmático
Descoberto em um mosteiro europeu, contém ilustrações botânicas de espécies que nunca existiram na Terra catalogada e uma linguagem cifrada que nem supercomputadores decifraram.

Seção 2: A Biblioteca Proibida
Registros que sobreviveram ao incêndio de Alexandria detalhavam métodos de orientação estelar e matemática avançada séculos antes do Renascimento.

Seção 3: Ciclos Civilizatórios Ocultos
A cada 2.000 anos, impérios inteiros colapsam repentinamente sob circunstâncias semelhantes: perda de foco coletivo, hedonismo e esquecimento das lições dos antepassados.
                    """.trimIndent(),
                    tags = listOf("Mistérios", "História Oculta", "Documento", "Conspirações"),
                    dateAdded = System.currentTimeMillis() - 86400000 * 2
                ),
                ContentItem(
                    title = "Podcast Mente Implacável: O Código da Dopamina & Hábitos Dark",
                    type = ContentType.AUDIO,
                    authorOrSource = "Episódio #42 - Biohacking & Neurociência",
                    summary = "Transcrição e notas de áudio sobre o sequestro dopaminérgico pelos algoritmos e construção de disciplina extrema.",
                    contentBody = """
(Transcrição de Áudio - Bloco 01: 04:15 - 12:30)
"Atenção não é um recurso renovável infinito. Quando você acorda e toca no celular antes de colocar os pés no chão, sua dopamina basal despenca pelas próximas 8 horas.
Os maiores estrategistas do mundo trabalham no que chamo de 'Modo Monge Escuro': 4 horas de foco ininterrupto com zero luz azul, música binaural e celular em outra sala.
Quem ganha o jogo nos próximos 5 anos não é o mais inteligente, é o que consegue ficar 2 horas sem olhar para uma notificação."
                    """.trimIndent(),
                    tags = listOf("Dopamina", "Biohacking", "Podcast", "Disciplina"),
                    dateAdded = System.currentTimeMillis() - 86400000 * 1
                ),
                ContentItem(
                    title = "Engenharia Reversa dos Canais Dark Mais Virais do Mundo",
                    type = ContentType.VIDEO,
                    authorOrSource = "Workshop Creator Pro 2026",
                    summary = "Desconstrução frame-a-frame de vídeos virais sem rosto de 60 segundos com retenção acima de 85%.",
                    contentBody = """
(Análise Técnica de Vídeo)
Estrutura do Vídeo Dark de 60 Segundos:
00:00 - 00:03: Hook de Alta Tensão (Pergunta ou afirmação paradoxal que desafia o bom senso)
00:03 - 00:15: O Loop Aberto (Promessa de revelar algo que 'eles não querem que você saiba')
00:15 - 00:45: Desenvolvimento em 3 Atos Rápidos com corte a cada 2.2 segundos e B-roll dramático
00:45 - 00:55: Conclusão impactante com lição moral ou insight prático
00:55 - 00:60: Chamada para ação silenciosa (texto na tela pedindo comentário com palavra-chave para receber material no direct)
                    """.trimIndent(),
                    tags = listOf("Canais Dark", "Viralidade", "Vídeo", "Retenção"),
                    dateAdded = System.currentTimeMillis()
                )
            )

            dao.insertContents(starterContents.map { ContentEntity.fromDomain(it) })

            // Also seed a couple of finished creations so user has instant examples!
            val starterCreations = listOf(
                GeneratedCreation(
                    sourceContentId = 1,
                    sourceContentTitle = "As Leis Secretas da Persuasão & Manipulação Sutil",
                    type = GenerationType.DARK_SCRIPT,
                    title = "Roteiro Dark: O Truque dos 3 Segundos Que Controla Sua Mente",
                    headline = "Você já comprou algo que não precisava só por causa desse truque?",
                    mainContent = """
[00:00 - 00:03] HOOK VIRAL
(Locutor misterioso)
"Existe uma razão pela qual você não consegue largar o telefone... e ela foi desenhada de propósito."

[00:04 - 00:15] O MISTÉRIO
"No livro 'As Leis Secretas da Persuasão', pesquisadores descobriram que 95% das suas decisões diárias são tomadas pelo seu cérebro reptiliano antes mesmo do seu lado racional perceber."

[00:16 - 00:40] O CORPO
"Eles usam um gatilho chamado 'Recompensa Variável'. É o mesmo mecanismo das máquinas de cassino em Las Vegas: quando você arrasta a tela, não sabe o que vai vir. Essa incerteza inunda seu cérebro de dopamina instantânea."

[00:41 - 00:58] DESFECHO & CTA
"Se você quer aprender a usar essa mesma psicologia para atrair clientes em vez de ser atraído, comente 'PSICOLOGIA' que eu te envio o mapa mental secreto."
                    """.trimIndent(),
                    visualPrompts = "Cena 1: Close-up em olho humano com reflexo de linhas binárias neon. Cena 2: Mão girando roleta de cassino em câmera lenta e névoa roxa.",
                    extraDetails = "SFX: Batida pesada de coração em 00:00, som de moedas caindo em 00:25, trilha de sintetizador retrô-dark.",
                    tone = ContentTone.DARK_MISTERIOSO,
                    platform = TargetPlatform.TIKTOK_REELS_SHORTS,
                    isFavorite = true
                ),
                GeneratedCreation(
                    sourceContentId = 4,
                    sourceContentTitle = "Engenharia Reversa dos Canais Dark Mais Virais do Mundo",
                    type = GenerationType.INFOPRODUCT,
                    title = "Infoproduto: 'Fábrica de Vídeos Dark 10k'",
                    headline = "O Pack de 100 Roteiros Prontos + Prompts Visuais para Canais Faceless",
                    mainContent = """
• PRODUTO: Pack Digital de Alta Conversão para Criadores de Conteúdo Dark
• PROMESSA: "Monte seu canal sem mostrar o rosto e publique 3 vídeos virais por dia em menos de 20 minutos com roteiros pré-testados."
• MÓDULOS:
  1. O Banco de 100 Ganchos (Hooks) com mais de 1M de views
  2. Guia de Ferramentas Gratuitas de IA (Voz, Imagem e Edição)
  3. A Fórmula Secreta de Roteiro em 4 Blocos
  4. Checklist de Monetização: Como vender e-books no link da bio
• OFERTA: Preço Front-end R$ 37,00 (Ticket baixo com taxa de conversão acima de 4%).
                    """.trimIndent(),
                    visualPrompts = "Mockup 3D de caixa de software dark com logo holográfico brilhante.",
                    extraDetails = "Upsell: Mentoria gravada de edição de cortes no CapCut por R$ 67,00.",
                    tone = ContentTone.PERSUASIVO_AFILIADO,
                    platform = TargetPlatform.OMNICHANNEL,
                    isFavorite = true
                )
            )

            dao.insertCreations(starterCreations.map { GeneratedCreationEntity.fromDomain(it) })
        }
    }
}
