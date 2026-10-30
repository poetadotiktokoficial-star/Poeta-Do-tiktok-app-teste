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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import com.example.data.model.ContentItem
import com.example.data.model.ContentType
import com.example.data.model.GenerationType
import com.example.ui.components.AddContentDialog
import com.example.ui.components.AppHeader
import com.example.ui.components.ContentItemCard
import com.example.ui.components.contentTypeColor
import com.example.ui.dialogs.ImportLinkDialog
import com.example.ui.dialogs.ScanDocumentDialog
import com.example.ui.dialogs.UploadFileDialog
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary
import com.example.viewmodel.DarkCreatorViewModel

@Composable
fun LibraryScreen(
    viewModel: DarkCreatorViewModel,
    onNavigateToGenerator: (ContentItem, GenerationType) -> Unit,
    modifier: Modifier = Modifier
) {
    val contents by viewModel.allContents.collectAsState()
    val activeFilter by viewModel.contentFilter.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Dialog state controllers
    var showUploadFileDialog by remember { mutableStateOf(false) }
    var showImportLinkDialog by remember { mutableStateOf(false) }
    var showScanDocumentDialog by remember { mutableStateOf(false) }
    var showManualAddDialog by remember { mutableStateOf(false) }

    val filteredList = contents.filter { item ->
        val matchesType = activeFilter == null || item.type == activeFilter
        val matchesQuery = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.authorOrSource.contains(searchQuery, ignoreCase = true) ||
                item.tags.any { it.contains(searchQuery, ignoreCase = true) }
        matchesType && matchesQuery
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showUploadFileDialog = true },
                containerColor = VioletPrimary,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_quick_upload")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = "Upload de Material",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Upload",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AppHeader(
                title = "Biblioteca de Materiais",
                subtitle = "Base de análise: Livros, Documentos, Áudios, Vídeos e Links"
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // CENTRAL DE ENTRADA / IMPORTAÇÃO (Botões de Ação Destacados)
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        Text(
                            text = "IMPORTAR MATERIAIS PARA ANÁLISE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = VioletPrimary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Grid of 3 Primary Import Buttons requested by user + Manual option
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 1. Upload Arquivo
                            ImportActionButton(
                                title = "Upload Arquivo",
                                subtitle = "PDF, TXT, Áudio, Vídeo",
                                icon = Icons.Default.FileUpload,
                                color = VioletPrimary,
                                onClick = { showUploadFileDialog = true },
                                modifier = Modifier.weight(1f),
                                testTag = "btn_action_upload_file"
                            )

                            // 2. Informar Link
                            ImportActionButton(
                                title = "Informar Link",
                                subtitle = "YouTube, TikTok, Web",
                                icon = Icons.Default.Link,
                                color = CyanSecondary,
                                onClick = { showImportLinkDialog = true },
                                modifier = Modifier.weight(1f),
                                testTag = "btn_action_import_link"
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 3. Scanear Documento / OCR
                            ImportActionButton(
                                title = "Escanear Livro",
                                subtitle = "Câmera com OCR",
                                icon = Icons.Default.Scanner,
                                color = AmberTertiary,
                                onClick = { showScanDocumentDialog = true },
                                modifier = Modifier.weight(1f),
                                testTag = "btn_action_scan_doc"
                            )

                            // 4. Escrever / Colar Texto
                            ImportActionButton(
                                title = "Digitar / Colar",
                                subtitle = "Editor Manual",
                                icon = Icons.Default.Edit,
                                color = EmeraldAccent,
                                onClick = { showManualAddDialog = true },
                                modifier = Modifier.weight(1f),
                                testTag = "btn_action_manual_text"
                            )
                        }
                    }
                }

                // Search Bar
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Pesquisar materiais por título, autor, assunto...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar",
                                    tint = TextSecondary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_search_library"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VioletPrimary,
                                unfocusedBorderColor = DarkSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface
                            ),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                    }
                }

                // Category Filter Chips
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                                    .clickable { viewModel.setContentFilter(null) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Todos (${contents.size})",
                                    color = if (isAllSelected) Color.Black else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }

                        items(ContentType.values()) { type ->
                            val isSelected = activeFilter == type
                            val count = contents.count { it.type == type }
                            val color = contentTypeColor(type)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) color.copy(alpha = 0.2f) else DarkSurfaceElevated)
                                    .border(
                                        1.dp,
                                        if (isSelected) color else DarkSurfaceBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.setContentFilter(if (isSelected) null else type) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "${type.label.split("/")[0].trim()} ($count)",
                                    color = if (isSelected) color else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // List of Materials
                if (filteredList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Nenhum material encontrado",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Use os botões de Upload, Link ou Scanner acima para carregar novos materiais.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                    fontSize = 13.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(filteredList) { item ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            ContentItemCard(
                                item = item,
                                onClick = { viewModel.inspectContent(item) },
                                onGenerateClick = { onNavigateToGenerator(item, GenerationType.DARK_SCRIPT) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Upload File Dialog (PDF, TXT, Audio, Video)
    if (showUploadFileDialog) {
        UploadFileDialog(
            onDismiss = { showUploadFileDialog = false },
            onSaveFile = { title, type, source, summary, body, tags, size ->
                viewModel.addContentAndSelectForGen(title, type, source, summary, body, tags, "", size)
            }
        )
    }

    // Import Link Dialog (YouTube, TikTok, Podcasts, Articles)
    if (showImportLinkDialog) {
        ImportLinkDialog(
            onDismiss = { showImportLinkDialog = false },
            onSaveLink = { title, type, sourceUrl, summary, body, tags ->
                viewModel.addContentAndSelectForGen(title, type, "Link Web", summary, body, tags, sourceUrl, "")
            }
        )
    }

    // Scan Document / Book Dialog (Camera OCR)
    if (showScanDocumentDialog) {
        ScanDocumentDialog(
            onDismiss = { showScanDocumentDialog = false },
            onSaveScannedText = { title, type, summary, body, tags ->
                viewModel.addContentAndSelectForGen(title, type, "Scanner Câmera OCR", summary, body, tags, "", "")
            }
        )
    }

    // Manual Add Dialog
    if (showManualAddDialog) {
        AddContentDialog(
            onDismiss = { showManualAddDialog = false },
            onSave = { title, type, author, summary, body, tags ->
                viewModel.addContent(title, type, author, summary, body, tags)
            }
        )
    }
}

@Composable
private fun ImportActionButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp)
            .testTag(testTag)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}
