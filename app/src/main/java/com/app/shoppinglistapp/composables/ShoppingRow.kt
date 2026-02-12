package com.app.shoppinglistapp.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import com.app.shoppinglistapp.data.ShoppingItem

/**
 * Normal row (clickable for starting edit).
 * Adds strike-through when checkbox is checked.
 */
@Composable
fun ShoppingRow(
    item: ShoppingItem,
    onToggleChecked: () -> Unit,
    onRowClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = item.name,
                style = TextStyle(
                    textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None
                )
            )
        },
        leadingContent = {
            Checkbox(
                checked = item.checked,
                onCheckedChange = { onToggleChecked() }
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRowClick() } // Tap row to edit
    )
}