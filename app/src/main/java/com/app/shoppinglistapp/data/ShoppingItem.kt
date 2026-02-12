package com.app.shoppinglistapp.data

data class ShoppingItem(
    val id: Int,
    val name: String,
    val checked: Boolean = false
)