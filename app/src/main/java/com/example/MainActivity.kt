package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.configureAppCheck
import com.example.data.model.GenerationType
import com.example.ui.dialogs.ContentDetailDialog
import com.example.ui.dialogs.CreationDetailDialog
import com.example.ui.screens.GeneratorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SavedCreationsScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCreatorTheme
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary
import com.example.viewmodel.DarkCreatorViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: DarkCreatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with launch intent before UI
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            DarkCreatorTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

data class NavigationTabItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun MainAppContent(viewModel: DarkCreatorViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val inspectedCreation by viewModel.inspectedCreation.collectAsState()
    val inspectedContent by viewModel.inspectedContent.collectAsState()

    // Handle Android system back button when not on Home screen
    if (currentTab != 0) {
        BackHandler {
            viewModel.setTab(0)
        }
    }

    val copyToClipboard: (String) -> Unit = { text ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("DarkCreator", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
    }

    val navItems = listOf(
        NavigationTabItem("Início", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
        NavigationTabItem("Biblioteca", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_library"),
        NavigationTabItem("Estúdio", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_generator"),
        NavigationTabItem("Cofre", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder, "nav_vault")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar")
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = currentTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setTab(index) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = VioletPrimary,
                            indicatorColor = VioletPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToGenerator = { content, type ->
                        viewModel.selectContentForGeneration(content)
                        viewModel.setGenType(type)
                        viewModel.setTab(2) // Generator tab
                    },
                    onNavigateToLibrary = { viewModel.setTab(1) },
                    onNavigateToVault = { viewModel.setTab(3) },
                    onCopyText = copyToClipboard
                )
                1 -> LibraryScreen(
                    viewModel = viewModel,
                    onNavigateToGenerator = { content, type ->
                        viewModel.selectContentForGeneration(content)
                        viewModel.setGenType(type)
                        viewModel.setTab(2) // Generator tab
                    }
                )
                2 -> GeneratorScreen(
                    viewModel = viewModel,
                    onCopyText = copyToClipboard
                )
                3 -> SavedCreationsScreen(
                    viewModel = viewModel,
                    onCopyText = copyToClipboard
                )
            }
        }
    }

    // Modal Inspection Dialogs
    inspectedCreation?.let { creation ->
        CreationDetailDialog(
            creation = creation,
            onDismiss = { viewModel.inspectCreation(null) },
            onCopy = copyToClipboard
        )
    }

    inspectedContent?.let { content ->
        ContentDetailDialog(
            item = content,
            onDismiss = { viewModel.inspectContent(null) },
            onGenerateWithContent = {
                viewModel.inspectContent(null)
                viewModel.selectContentForGeneration(content)
                viewModel.setGenType(GenerationType.DARK_SCRIPT)
                viewModel.setTab(2) // Generator tab
            },
            onDelete = {
                viewModel.deleteContent(content.id)
            }
        )
    }
}
