package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ContentItem
import com.example.data.model.ContentType
import com.example.data.model.ContentTone
import com.example.data.model.GeneratedCreation
import com.example.data.model.GenerationType
import com.example.data.model.TargetPlatform

@Entity(tableName = "contents")
data class ContentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val typeName: String,
    val authorOrSource: String,
    val summary: String,
    val contentBody: String,
    val tagsRaw: String,
    val sourceUrl: String = "",
    val fileSize: String = "",
    val dateAdded: Long = System.currentTimeMillis()
) {
    fun toDomain(): ContentItem {
        val type = try {
            ContentType.valueOf(typeName)
        } catch (_: Exception) {
            ContentType.BOOK
        }
        val tags = if (tagsRaw.isBlank()) emptyList() else tagsRaw.split(",").map { it.trim() }
        return ContentItem(
            id = id,
            title = title,
            type = type,
            authorOrSource = authorOrSource,
            summary = summary,
            contentBody = contentBody,
            tags = tags,
            sourceUrl = sourceUrl,
            fileSize = fileSize,
            dateAdded = dateAdded
        )
    }

    companion object {
        fun fromDomain(item: ContentItem): ContentEntity {
            return ContentEntity(
                id = item.id,
                title = item.title,
                typeName = item.type.name,
                authorOrSource = item.authorOrSource,
                summary = item.summary,
                contentBody = item.contentBody,
                tagsRaw = item.tags.joinToString(","),
                sourceUrl = item.sourceUrl,
                fileSize = item.fileSize,
                dateAdded = item.dateAdded
            )
        }
    }
}

@Entity(tableName = "creations")
data class GeneratedCreationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceContentId: Long?,
    val sourceContentTitle: String,
    val typeName: String,
    val title: String,
    val headline: String,
    val mainContent: String,
    val visualPrompts: String,
    val extraDetails: String,
    val toneName: String,
    val platformName: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): GeneratedCreation {
        val type = try {
            GenerationType.valueOf(typeName)
        } catch (_: Exception) {
            GenerationType.DARK_SCRIPT
        }
        val tone = try {
            ContentTone.valueOf(toneName)
        } catch (_: Exception) {
            ContentTone.DARK_MISTERIOSO
        }
        val platform = try {
            TargetPlatform.valueOf(platformName)
        } catch (_: Exception) {
            TargetPlatform.TIKTOK_REELS_SHORTS
        }
        return GeneratedCreation(
            id = id,
            sourceContentId = sourceContentId,
            sourceContentTitle = sourceContentTitle,
            type = type,
            title = title,
            headline = headline,
            mainContent = mainContent,
            visualPrompts = visualPrompts,
            extraDetails = extraDetails,
            tone = tone,
            platform = platform,
            isFavorite = isFavorite,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(item: GeneratedCreation): GeneratedCreationEntity {
            return GeneratedCreationEntity(
                id = item.id,
                sourceContentId = item.sourceContentId,
                sourceContentTitle = item.sourceContentTitle,
                typeName = item.type.name,
                title = item.title,
                headline = item.headline,
                mainContent = item.mainContent,
                visualPrompts = item.visualPrompts,
                extraDetails = item.extraDetails,
                toneName = item.tone.name,
                platformName = item.platform.name,
                isFavorite = item.isFavorite,
                createdAt = item.createdAt
            )
        }
    }
}
