package com.app.shoppinglistapp.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.app.shoppinglistapp.data.ShoppingItem
import com.example.compose.backgroundDark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(modifier: Modifier = Modifier) {

    // ----------------------------
    // State
    // ----------------------------

    val items = remember { mutableStateListOf<ShoppingItem>() }

    var showInput by rememberSaveable { mutableStateOf(false) }
    var input by rememberSaveable { mutableStateOf("") }

    var editingId by rememberSaveable { mutableStateOf<Int?>(null) }
    var editingText by rememberSaveable { mutableStateOf("") }

    // Prevents key reuse bugs after clearing
    var nextId by rememberSaveable { mutableIntStateOf(1) }
    var listVersion by rememberSaveable { mutableIntStateOf(0) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val inputFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val noRippleInteraction = remember { MutableInteractionSource() }

    // ----------------------------
    // Helpers
    // ----------------------------

    fun closeOverlays() {
        showInput = false
        editingId = null
        editingText = ""
        focusManager.clearFocus()
        keyboard?.hide()
    }

    suspend fun scrollToBottom(showingInput: Boolean) {
        val inputCount = if (showingInput) 1 else 0
        val lastIndex = items.size + inputCount // points to the add button row
        listState.animateScrollToItem(lastIndex)
    }

    fun addItem(keepInputOpen: Boolean) {
        val name = input.trim()
        if (name.isEmpty()) return

        items.add(ShoppingItem(id = nextId++, name = name))
        input = ""
        showInput = keepInputOpen

        scope.launch { scrollToBottom(showingInput = keepInputOpen) }

        if (keepInputOpen) {
            // Keep cursor in the input for quick multi-add
            scope.launch {
                kotlinx.coroutines.delay(50)
                inputFocusRequester.requestFocus()
                keyboard?.show()
            }
        }
    }

    fun startEdit(item: ShoppingItem) {
        // Enter edit mode for this row
        showInput = false
        input = ""
        editingId = item.id
        editingText = item.name
    }

    fun commitEdit(itemId: Int) {
        val newName = editingText.trim()

        if (newName.isEmpty()) {
            items.removeAll { it.id == itemId }
        } else {
            val index = items.indexOfFirst { it.id == itemId }
            if (index != -1) {
                items[index] = items[index].copy(name = newName)
            }
        }

        editingId = null
        editingText = ""
    }

    fun deleteItem(itemId: Int) {
        items.removeAll { it.id == itemId }
        if (editingId == itemId) {
            editingId = null
            editingText = ""
        }
    }

    fun clearAll() {
        items.clear()
        listVersion++
        closeOverlays()
        input = ""
        // NOTE: nextId intentionally does NOT reset
    }

    // ----------------------------
    // UI
    // ----------------------------

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ShoppingTopBar(
                title = "Shopping List",
                canClear = items.isNotEmpty(),
                onClear = { clearAll() }
            )
        }
    ) { innerPadding ->

        // Tap on empty space closes input/edit and hides keyboard
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clickable(
                    interactionSource = noRippleInteraction,
                    indication = null
                ) { closeOverlays() }
        ) {
            ShoppingListContent(
                items = items,
                listVersion = listVersion,
                listState = listState,
                contentPadding = PaddingValues(bottom = 16.dp),
                rowSpacing = 4.dp,

                showInput = showInput,
                input = input,
                inputFocusRequester = inputFocusRequester,
                keyboard = keyboard,

                editingId = editingId,
                editingText = editingText,

                onInputChange = { input = it },
                onAddDone = { addItem(keepInputOpen = false) },

                onToggleChecked = { itemId ->
                    val index = items.indexOfFirst { it.id == itemId }
                    if (index != -1) {
                        items[index] = items[index].copy(checked = !items[index].checked)
                    }
                },
                onStartEdit = { item -> startEdit(item) },
                onEditingTextChange = { editingText = it },
                onCommitEdit = { itemId -> commitEdit(itemId) },
                onDelete = { itemId -> deleteItem(itemId) },

                onFabClick = {
                    // FAB behavior:
                    // - closed input -> open
                    // - open input -> add + keep open
                    editingId = null
                    editingText = ""

                    if (!showInput) {
                        showInput = true
                        scope.launch { scrollToBottom(showingInput = true) }
                    } else {
                        addItem(keepInputOpen = true)
                    }
                }
            )
        }
    }
}