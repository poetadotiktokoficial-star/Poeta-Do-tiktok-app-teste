package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ContentEntity
import com.example.data.model.ContentItem
import com.example.data.model.ContentType
import com.example.data.model.ContentTone
import com.example.data.model.GenerationType
import com.example.data.model.TargetPlatform
import com.example.util.LinkExtractorUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("DarkCreator", appName)
    }

    @Test
    fun `test content entity mapping with sourceUrl and fileSize`() {
        val entity = ContentEntity(
            id = 1,
            title = "Manual de Persuasão",
            typeName = ContentType.BOOK.name,
            authorOrSource = "Autor Teste",
            summary = "Resumo teste",
            contentBody = "Corpo de teste com várias palavras para contar",
            tagsRaw = "dark,viral",
            sourceUrl = "https://youtube.com/watch?v=123",
            fileSize = "2.4 MB"
        )
        val domain = entity.toDomain()
        assertEquals("Manual de Persuasão", domain.title)
        assertEquals(ContentType.BOOK, domain.type)
        assertEquals(2, domain.tags.size)
        assertEquals("https://youtube.com/watch?v=123", domain.sourceUrl)
        assertEquals("2.4 MB", domain.fileSize)
        assertTrue(domain.wordCount > 5)
        assertTrue(domain.voiceoverTimeMinutes > 0.0)
    }

    @Test
    fun `test link analyzer for youtube and tiktok`() {
        val ytResult = LinkExtractorUtil.analyzeLink("https://www.youtube.com/watch?v=video_dark_123")
        assertEquals("YouTube", ytResult.platformName)
        assertEquals(ContentType.VIDEO, ytResult.detectedType)

        val ttResult = LinkExtractorUtil.analyzeLink("https://www.tiktok.com/@creator/video/999")
        assertEquals("TikTok", ttResult.platformName)
        assertEquals(ContentType.VIDEO, ttResult.detectedType)
    }

    @Test
    fun `test generation types and tone definitions`() {
        val darkScript = GenerationType.DARK_SCRIPT
        assertNotNull(darkScript.title)
        assertNotNull(ContentTone.DARK_MISTERIOSO.label)
        assertNotNull(TargetPlatform.TIKTOK_REELS_SHORTS.label)
    }
}
