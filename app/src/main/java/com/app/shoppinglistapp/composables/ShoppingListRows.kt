package com.app.shoppinglistapp.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.shoppinglistapp.data.ShoppingItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ShoppingListContent(
    items: List<ShoppingItem>,
    listVersion: Int,
    listState: LazyListState,
    contentPadding: PaddingValues,
    rowSpacing: Dp,

    showInput: Boolean,
    input: String,
    inputFocusRequester: androidx.compose.ui.focus.FocusRequester,
    keyboard: androidx.compose.ui.platform.SoftwareKeyboardController?,

    editingId: Int?,
    editingText: String,

    onInputChange: (String) -> Unit,
    onAddDone: () -> Unit,

    onToggleChecked: (Int) -> Unit,
    onStartEdit: (ShoppingItem) -> Unit,
    onEditingTextChange: (String) -> Unit,
    onCommitEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit,

    onFabClick: () -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(rowSpacing)
    ) {
        items(items, key = { "${listVersion}_${it.id}" }) { item ->
            val isEditing = editingId == item.id

            ShoppingSwipeRow(
                item = item,
                isEditing = isEditing,
                editingText = if (isEditing) editingText else "",
                onEditingTextChange = onEditingTextChange,
                onCommitEdit = { onCommitEdit(item.id) },

                onToggleChecked = { onToggleChecked(item.id) },
                onStartEdit = { onStartEdit(item) },

                onDelete = { onDelete(item.id) }
            )
        }

        if (showInput) {
            item(key = "input_row") {
                AddItemRow(
                    value = input,
                    onValueChange = onInputChange,
                    onDone = onAddDone,
                    focusRequester = inputFocusRequester,
                    keyboard = keyboard
                )
            }
        }

        item(key = "add_button") {
            AddFabRow(onClick = onFabClick)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShoppingSwipeRow(
    item: ShoppingItem,
    isEditing: Boolean,
    editingText: String,
    onEditingTextChange: (String) -> Unit,
    onCommitEdit: () -> Unit,
    onToggleChecked: () -> Unit,
    onStartEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var visible by remember(item.id) { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart && !isEditing) {
                // Play exit animation, then delete
                visible = false
                scope.launch {
                    delay(180)
                    onDelete()
                }
                true
            } else {
                false
            }
        }
    )

    AnimatedVisibility(
        visible = visible,
        exit = shrinkVertically() + fadeOut()
    ) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = false,
            gesturesEnabled = !isEditing,
            backgroundContent = {
                // Hide the delete background while editing
                if (!isEditing) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            tint = Color.Red
                        )
                    }
                }
            }
        ) {
            if (isEditing) {
                // Your existing composable
                EditableShoppingRow(
                    text = editingText,
                    onTextChange = onEditingTextChange,
                    onDone = onCommitEdit
                )
            } else {
                // Your existing composable
                ShoppingRow(
                    item = item,
                    onToggleChecked = onToggleChecked,
                    onRowClick = onStartEdit
                )
            }
        }
    }
}