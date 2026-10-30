package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DarkCreatorDatabase
import com.example.data.model.ContentItem
import com.example.data.model.ContentType
import com.example.data.model.ContentTone
import com.example.data.model.GeneratedCreation
import com.example.data.model.GenerationType
import com.example.data.model.TargetPlatform
import com.example.data.repository.DarkCreatorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "DarkCreatorVM"

sealed interface GenerationUiState {
    data object Idle : GenerationUiState
    data object Generating : GenerationUiState
    data class Success(val creation: GeneratedCreation) : GenerationUiState
    data class Error(val message: String) : GenerationUiState
}

class DarkCreatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DarkCreatorRepository

    val allContents: StateFlow<List<ContentItem>>
    val allCreations: StateFlow<List<GeneratedCreation>>

    // Navigation and filters
    private val _currentTab = MutableStateFlow(0) // 0: Home, 1: Biblioteca, 2: Gerador, 3: Cofre
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _contentFilter = MutableStateFlow<ContentType?>(null)
    val contentFilter: StateFlow<ContentType?> = _contentFilter.asStateFlow()

    private val _creationFilter = MutableStateFlow<GenerationType?>(null)
    val creationFilter: StateFlow<GenerationType?> = _creationFilter.asStateFlow()

    // Studio / Generator Config State
    private val _selectedContentForGen = MutableStateFlow<ContentItem?>(null)
    val selectedContentForGen: StateFlow<ContentItem?> = _selectedContentForGen.asStateFlow()

    private val _genType = MutableStateFlow(GenerationType.DARK_SCRIPT)
    val genType: StateFlow<GenerationType> = _genType.asStateFlow()

    private val _genTone = MutableStateFlow(ContentTone.DARK_MISTERIOSO)
    val genTone: StateFlow<ContentTone> = _genTone.asStateFlow()

    private val _genPlatform = MutableStateFlow(TargetPlatform.TIKTOK_REELS_SHORTS)
    val genPlatform: StateFlow<TargetPlatform> = _genPlatform.asStateFlow()

    private val _genCustomNotes = MutableStateFlow("")
    val genCustomNotes: StateFlow<String> = _genCustomNotes.asStateFlow()

    private val _generationUiState = MutableStateFlow<GenerationUiState>(GenerationUiState.Idle)
    val generationUiState: StateFlow<GenerationUiState> = _generationUiState.asStateFlow()

    private val _inspectedCreation = MutableStateFlow<GeneratedCreation?>(null)
    val inspectedCreation: StateFlow<GeneratedCreation?> = _inspectedCreation.asStateFlow()

    private val _inspectedContent = MutableStateFlow<ContentItem?>(null)
    val inspectedContent: StateFlow<ContentItem?> = _inspectedContent.asStateFlow()

    init {
        val database = DarkCreatorDatabase.getDatabase(application)
        repository = DarkCreatorRepository(database.dao())

        allContents = repository.allContents.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allCreations = repository.allCreations.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        viewModelScope.launch {
            try {
                repository.seedInitialDataIfEmpty()
            } catch (e: Exception) {
                Log.e(TAG, "Error seeding initial data", e)
            }
        }
    }

    fun setTab(tabIndex: Int) {
        _currentTab.value = tabIndex
    }

    fun setContentFilter(filter: ContentType?) {
        _contentFilter.value = filter
    }

    fun setCreationFilter(filter: GenerationType?) {
        _creationFilter.value = filter
    }

    fun selectContentForGeneration(item: ContentItem?) {
        _selectedContentForGen.value = item
    }

    fun setGenType(type: GenerationType) {
        _genType.value = type
    }

    fun setGenTone(tone: ContentTone) {
        _genTone.value = tone
    }

    fun setGenPlatform(platform: TargetPlatform) {
        _genPlatform.value = platform
    }

    fun setGenCustomNotes(notes: String) {
        _genCustomNotes.value = notes
    }

    fun inspectCreation(creation: GeneratedCreation?) {
        _inspectedCreation.value = creation
    }

    fun inspectContent(content: ContentItem?) {
        _inspectedContent.value = content
    }

    fun triggerGeneration() {
        viewModelScope.launch {
            _generationUiState.value = GenerationUiState.Generating
            try {
                val result = repository.generateCreation(
                    contentItem = _selectedContentForGen.value,
                    type = _genType.value,
                    tone = _genTone.value,
                    platform = _genPlatform.value,
                    customInstructions = _genCustomNotes.value
                )
                _generationUiState.value = GenerationUiState.Success(result)
            } catch (e: Exception) {
                Log.e(TAG, "Failed generation", e)
                _generationUiState.value = GenerationUiState.Error(
                    e.localizedMessage ?: "Falha ao gerar conteúdo. Tente novamente."
                )
            }
        }
    }

    fun resetGenerationState() {
        _generationUiState.value = GenerationUiState.Idle
    }

    fun saveCreation(creation: GeneratedCreation) {
        viewModelScope.launch {
            try {
                val id = repository.saveCreation(creation)
                Log.d(TAG, "Saved creation with id: $id")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving creation", e)
            }
        }
    }

    fun toggleFavorite(creationId: Long, currentFav: Boolean) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(creationId, !currentFav)
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling favorite", e)
            }
        }
    }

    fun deleteCreation(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteCreation(id)
                if (_inspectedCreation.value?.id == id) {
                    _inspectedCreation.value = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting creation", e)
            }
        }
    }

    fun addContent(
        title: String,
        type: ContentType,
        author: String,
        summary: String,
        body: String,
        tags: List<String>,
        sourceUrl: String = "",
        fileSize: String = ""
    ) {
        viewModelScope.launch {
            try {
                val item = ContentItem(
                    title = title,
                    type = type,
                    authorOrSource = author.ifBlank { "Minha Coleção" },
                    summary = summary.ifBlank { "Sem resumo informado" },
                    contentBody = body,
                    tags = tags,
                    sourceUrl = sourceUrl,
                    fileSize = fileSize
                )
                repository.saveContent(item)
            } catch (e: Exception) {
                Log.e(TAG, "Error adding content", e)
            }
        }
    }

    fun addContentAndSelectForGen(
        title: String,
        type: ContentType,
        author: String,
        summary: String,
        body: String,
        tags: List<String>,
        sourceUrl: String = "",
        fileSize: String = ""
    ) {
        viewModelScope.launch {
            try {
                val item = ContentItem(
                    title = title,
                    type = type,
                    authorOrSource = author.ifBlank { "Minha Coleção" },
                    summary = summary.ifBlank { "Sem resumo informado" },
                    contentBody = body,
                    tags = tags,
                    sourceUrl = sourceUrl,
                    fileSize = fileSize
                )
                val id = repository.saveContent(item)
                val savedItem = item.copy(id = id)
                selectContentForGeneration(savedItem)
                setTab(2) // Jump to Generator tab
            } catch (e: Exception) {
                Log.e(TAG, "Error adding content and selecting", e)
            }
        }
    }

    fun deleteContent(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteContent(id)
                if (_selectedContentForGen.value?.id == id) {
                    _selectedContentForGen.value = null
                }
                if (_inspectedContent.value?.id == id) {
                    _inspectedContent.value = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting content", e)
            }
        }
    }
}
