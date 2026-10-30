package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GenerationType
import com.example.ui.components.AppHeader
import com.example.ui.components.CreationCard
import com.example.ui.components.generationTypeColor
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary
import com.example.viewmodel.DarkCreatorViewModel

@Composable
fun SavedCreationsScreen(
    viewModel: DarkCreatorViewModel,
    onCopyText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val creations by viewModel.allCreations.collectAsState()
    val activeFilter by viewModel.creationFilter.collectAsState()
    var onlyFavorites by remember { mutableStateOf(false) }

    val filteredList = creations.filter { creation ->
        val matchesType = activeFilter == null || creation.type == activeFilter
        val matchesFav = !onlyFavorites || creation.isFavorite
        matchesType && matchesFav
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        AppHeader(
            title = "Cofre de Criações",
            subtitle = "Seus roteiros dark, posts, nichos e produtos salvos"
        )

        // Filter and Favorites Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Favorites filter pill
            item {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (onlyFavorites) AmberTertiary else DarkSurfaceElevated)
                        .border(
                            1.dp,
                            if (onlyFavorites) AmberTertiary else DarkSurfaceBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onlyFavorites = !onlyFavorites }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("filter_favorites")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = if (onlyFavorites) Color.Black else AmberTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Favoritos",
                            color = if (onlyFavorites) Color.Black else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (onlyFavorites) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // All Filter
            item {
                val isAllSelected = activeFilter == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isAllSelected) VioletPrimary else DarkSurfaceElevated)
                        .border(
                            1.dp,
                            if (isAllSelected) VioletPrimary else DarkSurfaceBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { viewModel.setCreationFilter(null) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Todos (${creations.size})",
                        color = if (isAllSelected) Color.Black else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            items(GenerationType.values()) { type ->
                val isSelected = activeFilter == type
                val count = creations.count { it.type == type }
                val color = generationTypeColor(type)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) color.copy(alpha = 0.2f) else DarkSurfaceElevated)
                        .border(
                            1.dp,
                            if (isSelected) color else DarkSurfaceBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { viewModel.setCreationFilter(if (isSelected) null else type) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${type.title.split(" ")[0]} ($count)",
                        color = if (isSelected) color else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Nenhuma criação encontrada neste filtro",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Vá até a aba 'Estúdio' para gerar roteiros dark, posts e infoprodutos.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredList) { creation ->
                    CreationCard(
                        creation = creation,
                        onClick = { viewModel.inspectCreation(creation) },
                        onToggleFavorite = { viewModel.toggleFavorite(creation.id, creation.isFavorite) },
                        onCopyClick = {
                            val formatted = buildString {
                                appendLine("=== ${creation.title} ===")
                                appendLine("HOOK / HEADLINE: ${creation.headline}")
                                appendLine()
                                appendLine("CONTEÚDO PRINCIPAL:")
                                appendLine(creation.mainContent)
                                appendLine()
                                appendLine("DIREÇÃO VISUAL & PROMPTS IA:")
                                appendLine(creation.visualPrompts)
                                appendLine()
                                appendLine("DETALHES / SFX / MONETIZAÇÃO:")
                                appendLine(creation.extraDetails)
                            }
                            onCopyText(formatted)
                        },
                        onDeleteClick = { viewModel.deleteCreation(creation.id) }
                    )
                }
            }
        }
    }
}
