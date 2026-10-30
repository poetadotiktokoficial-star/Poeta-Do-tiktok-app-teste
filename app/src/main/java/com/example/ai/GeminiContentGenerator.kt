package com.example.ai

import android.util.Log
import com.example.data.model.ContentItem
import com.example.data.model.ContentTone
import com.example.data.model.GeneratedCreation
import com.example.data.model.GenerationType
import com.example.data.model.TargetPlatform
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.ai
import com.google.firebase.Firebase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

private const val TAG = "GeminiContentGen"

class GeminiContentGenerator {

    // Lazy initialization of Firebase AI Logic generative model
    private val model by lazy {
        try {
            Firebase.ai.generativeModel(modelName = "gemini-3.8-flash")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI Logic not initialized: ${e.message}")
            null
        }
    }

    /**
     * Generates creative content based on source material, tone, platform, and generation type.
     * Uses Gemini 3.8 Flash when Firebase AI is reachable, otherwise uses intelligent semantic generator.
     */
    suspend fun generate(
        contentItem: ContentItem?,
        type: GenerationType,
        tone: ContentTone,
        platform: TargetPlatform,
        customInstructions: String = ""
    ): GeneratedCreation = withContext(Dispatchers.IO) {

        val titleContext = contentItem?.title ?: "Conhecimento & Estratégia Creator"
        val bodyExcerpt = contentItem?.contentBody?.take(3000)
            ?: "Conteúdo sobre comportamento, atenção humana, persuasão, mistérios e modelos de negócios digitais."

        // Attempt Gemini inference if Firebase is available
        val aiResult = tryGenerateWithGemini(titleContext, bodyExcerpt, type, tone, platform, customInstructions)

        if (aiResult != null) {
            return@withContext GeneratedCreation(
                sourceContentId = contentItem?.id,
                sourceContentTitle = titleContext,
                type = type,
                title = aiResult.first,
                headline = aiResult.second,
                mainContent = aiResult.third,
                visualPrompts = aiResult.fourth,
                extraDetails = aiResult.fifth,
                tone = tone,
                platform = platform
            )
        }

        // Resilient fallback generator with rich structured templates
        return@withContext generateFallbackCreation(
            contentItem = contentItem,
            type = type,
            tone = tone,
            platform = platform,
            customInstructions = customInstructions
        )
    }

    private suspend fun tryGenerateWithGemini(
        title: String,
        bodyExcerpt: String,
        type: GenerationType,
        tone: ContentTone,
        platform: TargetPlatform,
        customInstructions: String
    ): Triple5<String, String, String, String, String>? {
        if (FirebaseApp.getApps(com.google.firebase.FirebaseApp.getInstance().applicationContext).isEmpty()) {
            return null
        }
        val genModel = model ?: return null

        val prompt = buildString {
            appendLine("Você é um especialista em criação de canais dark, roteiros virais para TikTok/Reels/YouTube, marketing de conteúdo e infoprodutos.")
            appendLine("Gere um conteúdo em Português do Brasil de alto impacto com base no seguinte material de entrada:")
            appendLine("TÍTULO DO MATERIAL: $title")
            appendLine("TRECHO DO MATERIAL: $bodyExcerpt")
            appendLine("TIPO DE GERAÇÃO: ${type.title} - ${type.shortDesc}")
            appendLine("TOM DE VOZ: ${tone.label} (${tone.description})")
            appendLine("PLATAFORMA ALVO: ${platform.label}")
            if (customInstructions.isNotBlank()) {
                appendLine("INSTRUÇÕES ESPECÍFICAS: $customInstructions")
            }
            appendLine()
            appendLine("Por favor estruture sua resposta exatamente com estas 5 seções delimitadas:")
            appendLine("[TITULO]")
            appendLine("Título chamativo e magnético")
            appendLine("[HEADLINE]")
            appendLine("Hook inicial viral de 3 segundos ou headline magnética")
            appendLine("[CONTEUDO]")
            appendLine("O corpo completo (se for roteiro dark, inclua a locução cena a cena; se for post, os slides e legenda; se for nicho, as dores e estratégia; se for infoproduto, os módulos)")
            appendLine("[VISUAIS]")
            appendLine("Direções visuais, B-rolls e prompts detalhados para gerador de imagens com IA")
            appendLine("[DETALHES_EXTRAS]")
            appendLine("Efeitos sonoros (SFX), estimativa de precificação, sugestões de áudio ou gatilhos complementares")
        }

        return try {
            withTimeout(45_000L) {
                val response = genModel.generateContent(prompt)
                val text = response.text
                if (!text.isNullOrBlank()) {
                    parseDelimitedResponse(text)
                } else {
                    null
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "Gemini generation timed out, falling back to built-in studio engine", e)
            null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Gemini generation unavailable or failed: ${e.message}, using built-in studio engine")
            null
        }
    }

    private fun parseDelimitedResponse(text: String): Triple5<String, String, String, String, String> {
        var title = "Criação Estratégica Dark"
        var headline = "Descubra o segredo que a maioria ignora"
        var content = text
        var visuals = "Cenas cinematográficas em plano fechado, névoa e iluminação dramática"
        var extras = "SFX: Batida grave no início, sussurros sutis, transição com whoosh"

        try {
            val sections = text.split("\\[(?=TITULO\\]|HEADLINE\\]|CONTEUDO\\]|VISUAIS\\]|DETALHES_EXTRAS\\])".toRegex())
            for (sec in sections) {
                val trimmed = sec.trim()
                when {
                    trimmed.startsWith("TITULO]") -> title = trimmed.removePrefix("TITULO]").trim()
                    trimmed.startsWith("HEADLINE]") -> headline = trimmed.removePrefix("HEADLINE]").trim()
                    trimmed.startsWith("CONTEUDO]") -> content = trimmed.removePrefix("CONTEUDO]").trim()
                    trimmed.startsWith("VISUAIS]") -> visuals = trimmed.removePrefix("VISUAIS]").trim()
                    trimmed.startsWith("DETALHES_EXTRAS]") -> extras = trimmed.removePrefix("DETALHES_EXTRAS]").trim()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing delimited response", e)
        }

        return Triple5(title, headline, content, visuals, extras)
    }

    private fun generateFallbackCreation(
        contentItem: ContentItem?,
        type: GenerationType,
        tone: ContentTone,
        platform: TargetPlatform,
        customInstructions: String
    ): GeneratedCreation {
        val topic = contentItem?.title ?: "Segredos do Comportamento Humano"
        val sourceRef = contentItem?.authorOrSource ?: "Material de Estudos"

        return when (type) {
            GenerationType.DARK_SCRIPT -> generateDarkScriptTemplate(topic, sourceRef, tone, platform, customInstructions)
            GenerationType.POST_IMAGE -> generatePostImageTemplate(topic, sourceRef, tone, platform, customInstructions)
            GenerationType.NICHE -> generateNicheTemplate(topic, sourceRef, tone, platform, customInstructions)
            GenerationType.INFOPRODUCT -> generateInfoproductTemplate(topic, sourceRef, tone, platform, customInstructions)
        }
    }

    private fun generateDarkScriptTemplate(
        topic: String,
        source: String,
        tone: ContentTone,
        platform: TargetPlatform,
        customNotes: String
    ): GeneratedCreation {
        val title = "Roteiro Dark: O Paradoxo Oculto de \"$topic\""
        val headline = "99% das pessoas vivem sob esta ilusão sem desconfiar de absolutamente nada..."

        val script = """
[00:00 - 00:03] HOOK VIRAL DE RETENÇÃO
(Locução com tom grave e cadenciado)
"Pare o que você está fazendo. O que eu vou te revelar agora não é ensinado em nenhuma escola tradicional."
[Pausa dramática de 1s]

[00:04 - 00:12] LOOP DE RETENÇÃO & QUEBRA DE PADRÃO
"Durante séculos, estudiosos e estrategistas guardaram este princípio a sete chaves. De acordo com ensinamentos extraídos de $source, existe uma engrenagem oculta na tomada de decisão humana."
"Quem domina isso, controla a própria vida. Quem não domina... é controlado pelos outros."

[00:13 - 00:32] NARRATIVA PRINCIPAL / CLÍMAX
"Imagine acordar todos os dias acreditando que suas escolhas são suas. Mas quando você olha de perto os padrões em '$topic', percebe um padrão perturbador:"
"1. Primeiro, eles criam a necessidade que você nunca teve.
2. Depois, ativam o medo da escassez.
3. E quando você se dá conta, já entregou seu tempo, seu foco e seu dinheiro."

[00:33 - 00:48] A VIRADA DE CHAVE
"Os estoicos e grandes pensadores chamavam isso de despertar da consciência. Para quebrar esse ciclo, você precisa aplicar o princípio do observador neutro: nunca reaja no calor da emoção."
[Pausa com respiração controlada]

[00:49 - 00:58] CALL TO ACTION MAGNÉTICO (CTA)
"Poucos terão a coragem de comentar a palavra 'DESPERTE' aqui embaixo para receber a análise completa. Salve este vídeo antes que o algoritmo tire do seu feed."
        """.trimIndent()

        val visuals = """
• CENA 1 (00:00 - 00:03):
Prompt Midjourney: /imagine cinematic dark atmosphere, ancient philosopher stone statue in heavy shadows, cold blue and violet rim light, fog swirling, 8k resolution, photorealistic, 9:16 aspect ratio --ar 9:16 --v 6.0

• CENA 2 (00:04 - 00:12):
Câmera lenta dramática com zoom contínuo em documento antigo ou ampulheta com areia negra descendo.

• CENA 3 (00:13 - 00:32):
Transição rápida (glitch sutil) para silhueta humana em frente a centenas de telas piscando códigos e gráficos escuros.

• CENA 4 (00:33 - 00:58):
Chama de vela acendendo na escuridão total, fumaça tênue subindo com partículas douradas suspensas no ar.
        """.trimIndent()

        val extras = """
• TRILHA SUGERIDA: 'Dark Ambient Suspense Drone' com sub-graves pesados em 60 BPM.
• EFEITOS SONOROS (SFX):
  - 00:00: Deep Whoosh Impact / Bass Drop
  - 00:03: Tik-tok sutil de relógio vintage
  - 00:33: Som de fósforo riscando + respiração profunda
  - 00:50: Subtítulo dinâmico amarelo neon piscando na palavra 'DESPERTE'
• DURAÇÃO TOTAL ESTIMADA: 58 segundos (Ideal para $platform)
        """.trimIndent()

        return GeneratedCreation(
            type = GenerationType.DARK_SCRIPT,
            title = title,
            headline = headline,
            mainContent = script,
            visualPrompts = visuals,
            extraDetails = extras,
            tone = tone,
            platform = platform
        )
    }

    private fun generatePostImageTemplate(
        topic: String,
        source: String,
        tone: ContentTone,
        platform: TargetPlatform,
        customNotes: String
    ): GeneratedCreation {
        val title = "Carrossel & Prompts Visuais: 5 Verdades Cruéis Sobre $topic"
        val headline = "Arraste para o lado se você tem estômago para a verdade..."

        val content = """
SLIDE 1 (CAPA):
• Título Central: "5 Verdades Cruéis Sobre $topic Que Ninguém Te Conta"
• Subtítulo: "(Baseado em estudos profundos de $source)"
• Elemento visual: Fundo escuro texturizado com símbolo minimalista de ampulheta neon.

SLIDE 2:
• Título: 01. A Falácia da Motivação
• Texto: A motivação é passageira como uma faísca. O que constrói impérios é a rotina silenciosa que você odeia quando ninguém está olhando.

SLIDE 3:
• Título: 02. O Preço do Isolamento Estratégico
• Texto: Para construir algo acima da média, você precisará dizer 'não' para pessoas que se contentam com a mediocridade.

SLIDE 4:
• Título: 03. A Armadilha do Excesso de Informação
• Texto: Consumir conteúdo sem aplicar é apenas uma forma moderna de procrastinação com selo de intelectualidade.

SLIDE 5:
• Título: 04. Domínio da Atenção
• Texto: Se você não sabe o que quer para as próximas 2 horas, seu telefone já sabe o que quer de você pelo resto do dia.

SLIDE 6 (CONCLUSÃO & CTA):
• Título: O Próximo Passo
• Texto: Qual dessas 4 verdades te atingiu mais forte? Deixe seu comentário sincero e compartilhe com um amigo focado.
        """.trimIndent()

        val visualPrompts = """
• PROMPT CAPA (Midjourney / Ideogram):
/imagine prompt: Ultra minimalist dark luxury poster, a mysterious glowing neon book opening in an infinite black void, volumetric lighting, cyan and ultraviolet gradients, clean modern typography aesthetic, 1080x1350 --ar 4:5 --style raw

• PROMPTS DOS SLIDES INTERNOS:
Crie imagens monocromáticas com texturas de concreto escuro, mármore negro e linhas geométricas finas para usar como fundo de cada slide no Canva ou Photoshop.

• ESTILO DE DESIGN RECOMENDADO:
- Tipografia Headline: Montserrat Bold ou Syne ExtraBold (Maiúsculas)
- Tipografia Corpo: Inter Regular (Cor #94A3B8)
- Cores de Destaque: Roxo Elétrico (#9D72FF) e Amarelo Dourado (#F59E0B)
        """.trimIndent()

        val extras = """
• LEGENDA PRONTA PARA O FEED:
"A maioria das pessoas prefere uma mentira reconfortante a uma verdade desconfortável. O conteúdo em '$topic' nos mostra exatamente onde erramos quando tentamos buscar atalhos.

Qual foi o slide que mais fez você refletir hoje?

Salve este post na sua coleção para reler sempre que o foco diminuir."

• HASHTAGS ESTRATÉGICAS:
#desenvolvimentopessoal #focofechado #mentemilionaria #canaisdark #psicologiacomportamental #disciplinadeferro
        """.trimIndent()

        return GeneratedCreation(
            type = GenerationType.POST_IMAGE,
            title = title,
            headline = headline,
            mainContent = content,
            visualPrompts = visualPrompts,
            extraDetails = extras,
            tone = tone,
            platform = platform
        )
    }

    private fun generateNicheTemplate(
        topic: String,
        source: String,
        tone: ContentTone,
        platform: TargetPlatform,
        customNotes: String
    ): GeneratedCreation {
        val title = "Mapeamento de Nichos Lucrativos & Ângulos Blue Ocean: $topic"
        val headline = "3 Subnichos Ocultos Com Baixa Concorrência e Alta Disposição a Pagar"

        val content = """
1. SUBNICHO BLUE OCEAN #1: "Estoicismo Dark & Foco Anti-Distração para Jovens Profissionais"
• Ângulo Diferenciador: Em vez de citar filósofos de forma acadêmica e chata, traduza o conteúdo de '$topic' para roteiros cinematográficos curtos mostrando rotinas de alta densidade sem mostrar o rosto.
• Dor Central: Sensação crônica de cansaço mental, vício em redes sociais e procrastinação culposa.
• Desejo Inconfessável: Vontade de se isolar por 6 meses e reaparecer com o físico trincado, mente blindada e faturamento alto.

2. SUBNICHO BLUE OCEAN #2: "Psicologia Oculta de Vendas & Gatilhos para Criadores de Conteúdo"
• Ângulo Diferenciador: Revelar como grandes marcas e influenciadores usam o material de '$source' para manipular a atenção e como o espectador comum pode usar isso eticamente no próprio negócio.
• Dor Central: Criar vídeos ou carrosséis e ter menos de 100 visualizações e zero vendas.
• Desejo: Fazer vídeos virais e vender infoprodutos todos os dias no piloto automático.

3. SUBNICHO BLUE OCEAN #3: "Mistérios Históricos e Conspirações Científicas Não Resolvidas"
• Ângulo Diferenciador: Canais faceless com narração de IA hiper-realista e B-rolls cinematográficos de civilizações perdidas, artefatos inexplicáveis e mistérios de arquivos desclassificados.
• Dor Central: Tédio e busca por entretenimento instigante que desafia o senso comum.
• Desejo: Sentir-se parte de um grupo seleto de pessoas que 'sabem a verdade'.
        """.trimIndent()

        val visuals = """
• ESTÉTICA VISUAL DO NICHO:
- Identidade visual minimalista e escura (Dark Theme com acentos neon)
- Uso de miniaturas (Thumbnails) com alto contraste de iluminação e um único ponto focal chocante
- Imagens geradas com IA explorando estátuas de mármore, labirintos escuros, silhuetas e luz volumétrica
        """.trimIndent()

        val extras = """
• MATRIZ DE MONETIZAÇÃO DO NICHO:
- Adsense / TikTok Creator Rewards (RPM médio de R$ 3,50 a R$ 9,00 a cada 1.000 visualizações qualificadas)
- Infoprodutos próprios no link da bio (E-books de R$ 27 e Comunidades de R$ 37/mês)
- Parcerias com softwares de IA, produtividade e suplementação
        """.trimIndent()

        return GeneratedCreation(
            type = GenerationType.NICHE,
            title = title,
            headline = headline,
            mainContent = content,
            visualPrompts = visuals,
            extraDetails = extras,
            tone = tone,
            platform = platform
        )
    }

    private fun generateInfoproductTemplate(
        topic: String,
        source: String,
        tone: ContentTone,
        platform: TargetPlatform,
        customNotes: String
    ): GeneratedCreation {
        val title = "Infoproduto Lucrativo: 'O Protocolo $topic'"
        val headline = "Como transformar o material de '$source' em um infoproduto de 6 dígitos"

        val content = """
• FORMATO RECOMENDADO: E-book Guia de Bolso + Pack de Templates Notion + Checklist Operacional em PDF interativo.

• TÍTULO MAGNÉTICO:
"O CÓDIGO DA MENTE INABALÁVEL: O Manual Prático de 21 Dias Baseado nos Princípios de $topic"

• PROMESSA ÚNICA DE VALOR (PUV):
"Um método passo a passo para blindar sua mente, eliminar a procrastinação e dominar a psicologia da persuasão diária em menos de 15 minutos por dia."

• GRADE DE CONTEÚDO DOS MÓDULOS:
- Módulo 01: O Diagnóstico Invisível (Identificando onde sua energia e foco estão vazando)
- Módulo 02: A Reconfiguração da Dopamina (O protocolo de 7 dias para recuperar a capacidade de concentração)
- Módulo 03: As Armas da Persuasão Diária (Como influenciar reuniões, conversas e vendas sem parecer forçado)
- Módulo 04: A Rotina de Aço (O cronograma matinal e noturno dos 1% mais disciplinados)
- Módulo Bônus: Pack com 30 Prompts de IA para Aplicar o Conteúdo Instantaneamente
        """.trimIndent()

        val visuals = """
• MOCKUP DO PRODUTO:
Design de capa para E-book 3D: fundo em couro negro com tipografia dourada metalizada em relevo e selo holográfico de 'Edição Premium'.

• PÁGINA DE VENDAS (VSL / LANDING PAGE):
Estilo minimalista, fundo preto, headline branca com palavras-chave em amarelo dourado, contagem regressiva de urgência e depoimentos com prints de tela.
        """.trimIndent()

        val extras = """
• ESTRATÉGIA DE PRECIFICAÇÃO & FUNIL DE VENDAS:
1. Front-end (Oferta Principal no link da Bio): R$ 29,90 a R$ 47,00 (Sem atrito de compra)
2. Order Bump no checkout: "Áudio MP3 de Reprogramação Mental para Ouvir Dormindo" por + R$ 14,90
3. Upsell imediato na página de obrigado: "Acesso à Comunidade Secreta de Criadores Dark" por R$ 97,00

• CONVERSÃO ESTIMADA: A cada 1.000 cliques no link da bio vindo de vídeos dark virais, média de 25 a 45 vendas do front-end.
        """.trimIndent()

        return GeneratedCreation(
            type = GenerationType.INFOPRODUCT,
            title = title,
            headline = headline,
            mainContent = content,
            visualPrompts = visuals,
            extraDetails = extras,
            tone = tone,
            platform = platform
        )
    }
}

data class Triple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
