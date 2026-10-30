package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.ContentTone
import com.example.data.model.GeneratedCreation
import com.example.data.model.GenerationType
import com.example.data.model.TargetPlatform
import com.example.ui.components.AppHeader
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
import com.example.viewmodel.GenerationUiState

import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Scanner
import com.example.ui.dialogs.ImportLinkDialog
import com.example.ui.dialogs.ScanDocumentDialog
import com.example.ui.dialogs.UploadFileDialog

@Composable
fun GeneratorScreen(
    viewModel: DarkCreatorViewModel,
    onCopyText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val contents by viewModel.allContents.collectAsState()
    val selectedContent by viewModel.selectedContentForGen.collectAsState()
    val genType by viewModel.genType.collectAsState()
    val genTone by viewModel.genTone.collectAsState()
    val genPlatform by viewModel.genPlatform.collectAsState()
    val genCustomNotes by viewModel.genCustomNotes.collectAsState()
    val uiState by viewModel.generationUiState.collectAsState()

    var savedInVault by remember { mutableStateOf(false) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var showScanDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        AppHeader(
            title = "Estúdio de Criação",
            subtitle = "Transforme qualquer material em conteúdo viral e produtos"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // STEP 1: Select Material
            item {
                StepHeader(number = "1", title = "Selecione o Material de Entrada")
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Option: General idea without specific book
                    item {
                        val isSelected = selectedContent == null
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) VioletPrimary.copy(alpha = 0.2f) else DarkSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) VioletPrimary else DarkSurfaceBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.selectContentForGeneration(null) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isSelected) VioletPrimary else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Conhecimento Geral",
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    items(contents) { item ->
                        val isSelected = selectedContent?.id == item.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CyanSecondary.copy(alpha = 0.2f) else DarkSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) CyanSecondary else DarkSurfaceBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.selectContentForGeneration(item) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("select_material_${item.id}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = contentTypeIcon(item.type),
                                    contentDescription = null,
                                    tint = if (isSelected) CyanSecondary else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.title.take(24) + if (item.title.length > 24) "..." else "",
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Import Buttons directly inside Studio
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, VioletPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable { showUploadDialog = true }
                            .padding(vertical = 8.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                tint = VioletPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Upload",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, CyanSecondary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable { showLinkDialog = true }
                            .padding(vertical = 8.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = CyanSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Link",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, AmberTertiary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable { showScanDialog = true }
                            .padding(vertical = 8.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Scanner,
                                contentDescription = null,
                                tint = AmberTertiary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Escanear",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Active Selected Content Card
                if (selectedContent != null) {
                    val sel = selectedContent!!
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurface)
                            .border(1.dp, CyanSecondary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "MATERIAL ATIVO SELECIONADO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanSecondary
                                )
                                Text(
                                    text = "🎙️ ~${sel.voiceoverTimeMinutes} min locução",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = sel.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            if (sel.summary.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = sel.summary,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // STEP 2: Choose Output Format
            item {
                StepHeader(number = "2", title = "Escolha o Formato da Criação")
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GenerationType.values().forEach { type ->
                        val isSelected = genType == type
                        val color = generationTypeColor(type)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    1.5.dp,
                                    if (isSelected) color else DarkSurfaceBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { viewModel.setGenType(type) }
                                .testTag("format_card_${type.name}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) color.copy(alpha = 0.12f) else DarkSurface
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) color else DarkSurfaceElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.Black else TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = type.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) TextPrimary else TextSecondary
                                        )
                                    )
                                    Text(
                                        text = type.shortDesc,
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                                        fontSize = 11.sp
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // STEP 3: Tone & Target Platform
            item {
                StepHeader(number = "3", title = "Tom de Voz & Plataforma")
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "TOM DE VOZ",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ContentTone.values()) { tone ->
                        val isSelected = genTone == tone
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) VioletPrimary.copy(alpha = 0.2f) else DarkSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) VioletPrimary else DarkSurfaceBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setGenTone(tone) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = tone.label,
                                color = if (isSelected) VioletPrimary else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "PLATAFORMA PRINCIPAL",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TargetPlatform.values()) { platform ->
                        val isSelected = genPlatform == platform
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CyanSecondary.copy(alpha = 0.2f) else DarkSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) CyanSecondary else DarkSurfaceBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setGenPlatform(platform) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = platform.label,
                                color = if (isSelected) CyanSecondary else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Optional custom notes
            item {
                OutlinedTextField(
                    value = genCustomNotes,
                    onValueChange = { viewModel.setGenCustomNotes(it) },
                    label = { Text("Instruções Específicas / Ângulo Desejado (Opcional)") },
                    placeholder = { Text("Ex: Foque no público jovem que sofre de procrastinação; termine com gancho para e-book de R$27.") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VioletPrimary,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 3
                )
            }

            // Generate Trigger Button
            item {
                val isGenerating = uiState is GenerationUiState.Generating

                Button(
                    onClick = {
                        savedInVault = false
                        viewModel.triggerGeneration()
                    },
                    enabled = !isGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_trigger_generation"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VioletPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Analisando material e gerando...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gerar ${genType.title}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // Generation Result Section
            when (val state = uiState) {
                is GenerationUiState.Generating -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(DarkSurface)
                                .border(1.dp, VioletPrimary.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                                .padding(30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = VioletPrimary)
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Criando roteiro com gatilhos de retenção...",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Montando locução, B-rolls e sugestão de monetização",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                is GenerationUiState.Error -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF3B151E))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Aviso: ${state.message}",
                                color = Color(0xFFFCA5A5),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                is GenerationUiState.Success -> {
                    item {
                        GenerationResultView(
                            creation = state.creation,
                            isSaved = savedInVault,
                            onSaveToVault = {
                                viewModel.saveCreation(state.creation)
                                savedInVault = true
                            },
                            onCopyAll = {
                                val fullText = buildString {
                                    appendLine("=== ${state.creation.title} ===")
                                    appendLine("HOOK / HEADLINE: ${state.creation.headline}")
                                    appendLine()
                                    appendLine("CONTEÚDO PRINCIPAL:")
                                    appendLine(state.creation.mainContent)
                                    appendLine()
                                    appendLine("DIREÇÃO VISUAL & PROMPTS IA:")
                                    appendLine(state.creation.visualPrompts)
                                    appendLine()
                                    appendLine("DETALHES / SFX / MONETIZAÇÃO:")
                                    appendLine(state.creation.extraDetails)
                                }
                                onCopyText(fullText)
                            }
                        )
                    }
                }

                else -> {}
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
private fun StepHeader(number: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(VioletPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )
    }
}

@Composable
private fun GenerationResultView(
    creation: GeneratedCreation,
    isSaved: Boolean,
    onSaveToVault: () -> Unit,
    onCopyAll: () -> Unit
) {
    var selectedResultTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Roteiro / Texto", "Prompts Visuais", "SFX & Oferta")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, VioletPrimary.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmeraldAccent.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "GERAÇÃO CONCLUÍDA",
                        color = EmeraldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onSaveToVault,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSaved) DarkSurfaceElevated else VioletPrimary,
                            contentColor = if (isSaved) TextPrimary else Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_save_to_vault")
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Bookmark,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSaved) "Salvo no Cofre" else "Salvar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onCopyAll,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurfaceElevated,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_copy_generated")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copiar", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = creation.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Hook: \"${creation.headline}\"",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = VioletPrimary
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Result Sub-tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceElevated)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isTabSelected = selectedResultTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isTabSelected) VioletPrimary else Color.Transparent)
                            .clickable { selectedResultTab = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isTabSelected) Color.Black else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isTabSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Content Body
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkBackground)
                    .padding(14.dp)
            ) {
                val displayContent = when (selectedResultTab) {
                    0 -> creation.mainContent
                    1 -> creation.visualPrompts.ifBlank { "Prompts visuais não gerados para esta categoria." }
                    else -> creation.extraDetails.ifBlank { "Sem detalhes adicionais." }
                }

                Text(
                    text = displayContent,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )
                )
            }
        }
    }
}
