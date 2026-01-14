package com.example.momentia.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController

import com.example.momentia.R
import com.example.momentia.presentation.model.NavItem
import com.google.firebase.auth.FirebaseAuth


@Composable
fun MyNavigationBar(auth: FirebaseAuth,selectedIndex:Int, itemList: List<NavItem>, onItemSelected: (Int) -> Unit) {
    var selectedIndex by remember { mutableIntStateOf(2) }

    val itemList = listOf(
        NavItem("Inicio", R.drawable.home, "home"),
        NavItem("Notificaciones", R.drawable.notifications, "notifications"),
        NavItem("Perfil", R.drawable.profile, "profile")
    )

    NavigationBar(
        containerColor = Color.Red, tonalElevation = 1.dp
    ) {
        itemList.forEachIndexed { index, item ->
            Items(navItem = item, isSelected = index == selectedIndex) {
                selectedIndex = index
                onItemSelected(index)
            }
        }
    }
}

@Composable
fun RowScope.Items(navItem: NavItem, isSelected: Boolean, onItemClick: () -> Unit) {
    NavigationBarItem(
        selected = isSelected,
        onClick = { onItemClick() },
        icon = { Icon(painter = painterResource(navItem.icon), contentDescription = "") },
        label = { Text(navItem.name) },
        alwaysShowLabel = false,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.Red,
            selectedTextColor = Color.White,
            indicatorColor = Color.White,
            unselectedIconColor = Color.White,
            unselectedTextColor = Color.White,
            disabledIconColor = Color.Gray,
            disabledTextColor = Color.Gray

        )
    )
}


