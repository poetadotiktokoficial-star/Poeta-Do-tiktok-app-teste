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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContentItem
import com.example.data.model.GeneratedCreation
import com.example.data.model.GenerationType
import com.example.ui.components.AppHeader
import com.example.ui.components.CreationCard
import com.example.ui.components.contentTypeColor
import com.example.ui.components.contentTypeIcon
import com.example.ui.components.generationTypeColor
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary
import com.example.viewmodel.DarkCreatorViewModel

import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.dialogs.ImportLinkDialog
import com.example.ui.dialogs.ScanDocumentDialog
import com.example.ui.dialogs.UploadFileDialog

@Composable
fun HomeScreen(
    viewModel: DarkCreatorViewModel,
    onNavigateToGenerator: (ContentItem?, GenerationType) -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToVault: () -> Unit,
    onCopyText: (String) -> Unit
) {
    val contents by viewModel.allContents.collectAsState()
    val creations by viewModel.allCreations.collectAsState()

    var showUploadDialog by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var showScanDialog by remember { mutableStateOf(false) }

    val darkScriptsCount = creations.count { it.type == GenerationType.DARK_SCRIPT }
    val postsCount = creations.count { it.type == GenerationType.POST_IMAGE }
    val nichesCount = creations.count { it.type == GenerationType.NICHE }
    val infoCount = creations.count { it.type == GenerationType.INFOPRODUCT }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        AppHeader(
            title = "DarkCreator AI",
            subtitle = "Fábrica de Roteiros, Posts, Nichos e Infoprodutos"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Action Banner
            item {
                HeroBanner(
                    onStartDarkScript = {
                        onNavigateToGenerator(contents.firstOrNull(), GenerationType.DARK_SCRIPT)
                    },
                    onOpenLibrary = onNavigateToLibrary
                )
            }

            // Quick Stats Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatMetricCard(
                        title = "Materiais",
                        value = "${contents.size}",
                        iconColor = CyanSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Roteiros Dark",
                        value = "$darkScriptsCount",
                        iconColor = VioletPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Posts & Imagens",
                        value = "$postsCount",
                        iconColor = EmeraldAccent,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Infoprodutos",
                        value = "$infoCount",
                        iconColor = AmberTertiary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Import Bar (Upload, Link, Scanner OCR)
            item {
                Column {
                    Text(
                        text = "BASE DE ANÁLISE: CARREGAR MATERIAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyanSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickImportChip(
                            label = "Upload Arquivo",
                            icon = Icons.Default.FileUpload,
                            color = VioletPrimary,
                            onClick = { showUploadDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                        QuickImportChip(
                            label = "Inserir Link",
                            icon = Icons.Default.Link,
                            color = CyanSecondary,
                            onClick = { showLinkDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                        QuickImportChip(
                            label = "Escanear Livro",
                            icon = Icons.Default.Scanner,
                            color = AmberTertiary,
                            onClick = { showScanDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 4 Specialized Creation Modules Shortcut
            item {
                Text(
                    text = "O QUE VOCÊ DESEJA CRIAR HOJE?",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = VioletPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickModuleButton(
                            title = "Roteiro Vídeo Dark",
                            desc = "Hook 3s, retenção e B-rolls",
                            type = GenerationType.DARK_SCRIPT,
                            onClick = { onNavigateToGenerator(contents.firstOrNull(), GenerationType.DARK_SCRIPT) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickModuleButton(
                            title = "Post & Imagens",
                            desc = "Carrosséis e prompts IA",
                            type = GenerationType.POST_IMAGE,
                            onClick = { onNavigateToGenerator(contents.firstOrNull(), GenerationType.POST_IMAGE) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickModuleButton(
                            title = "Nichos Lucrativos",
                            desc = "Ângulos Blue Ocean",
                            type = GenerationType.NICHE,
                            onClick = { onNavigateToGenerator(contents.firstOrNull(), GenerationType.NICHE) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickModuleButton(
                            title = "Ideias de Infoproduto",
                            desc = "E-books, kits e funil de vendas",
                            type = GenerationType.INFOPRODUCT,
                            onClick = { onNavigateToGenerator(contents.firstOrNull(), GenerationType.INFOPRODUCT) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Quick Materials Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MATERIAIS DE BASE (${contents.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyanSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Ver todos",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = VioletPrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable { onNavigateToLibrary() }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(contents) { item ->
                        MiniContentCard(
                            item = item,
                            onClick = {
                                viewModel.inspectContent(item)
                            },
                            onGenerate = {
                                onNavigateToGenerator(item, GenerationType.DARK_SCRIPT)
                            }
                        )
                    }
                }
            }

            // Recent Saved Creations
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CRIAÇÕES RECENTES NO COFRE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = EmeraldAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Abrir cofre",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = VioletPrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable { onNavigateToVault() }
                    )
                }
            }

            if (creations.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurface)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhuma criação salva ainda. Selecione um material acima para gerar seu primeiro roteiro!",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(creations.take(4)) { creation ->
                    CreationCard(
                        creation = creation,
                        onClick = { viewModel.inspectCreation(creation) },
                        onToggleFavorite = { viewModel.toggleFavorite(creation.id, creation.isFavorite) },
                        onCopyClick = {
                            val formatted = "${creation.title}\n\n${creation.headline}\n\n${creation.mainContent}\n\n${creation.visualPrompts}\n\n${creation.extraDetails}"
                            onCopyText(formatted)
                        }
                    )
                }
            }
        }
    }

    if (showUploadDialog) {
        UploadFileDialog(
            onDismiss = { showUploadDialog = false },
            onSaveFile = { title, type, source, summary, body, tags, size ->
                viewModel.addContentAndSelectForGen(title, type, source, summary, body, tags, "", size)
            }
        )
    }

    if (showLinkDialog) {
        ImportLinkDialog(
            onDismiss = { showLinkDialog = false },
            onSaveLink = { title, type, sourceUrl, summary, body, tags ->
                viewModel.addContentAndSelectForGen(title, type, "Link Web", summary, body, tags, sourceUrl, "")
            }
        )
    }

    if (showScanDialog) {
        ScanDocumentDialog(
            onDismiss = { showScanDialog = false },
            onSaveScannedText = { title, type, summary, body, tags ->
                viewModel.addContentAndSelectForGen(title, type, "Scanner Câmera OCR", summary, body, tags, "", "")
            }
        )
    }
}

@Composable
private fun QuickImportChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun HeroBanner(
    onStartDarkScript: () -> Unit,
    onOpenLibrary: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, VioletPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF2E1955),
                            Color(0xFF151828)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(VioletPrimary)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "NOVIDADE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Transforme Conhecimento em Audiência",
                        color = CyanSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "De Livros a Vídeos Dark Virais e Infoprodutos",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Extraia hooks magnéticos de 3 segundos, roteiros de locução sem rosto, carrosséis, nichos inexplorados e ideias de infoprodutos a partir de qualquer material.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onStartDarkScript,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VioletPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_hero_dark_script")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Criar Roteiro", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = onOpenLibrary,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurfaceElevated,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Meus Materiais", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMetricCard(
    title: String,
    value: String,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = iconColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QuickModuleButton(
    title: String,
    desc: String,
    type: GenerationType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = generationTypeColor(type)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MiniContentCard(
    item: ContentItem,
    onClick: () -> Unit,
    onGenerate: () -> Unit
) {
    val color = contentTypeColor(item.type)

    Card(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = contentTypeIcon(item.type),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.type.label.split("/")[0].trim().uppercase(),
                        color = color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(VioletPrimary)
                        .clickable { onGenerate() }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "USAR",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.authorOrSource,
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted),
                maxLines = 1
            )
        }
    }
}
