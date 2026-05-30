package com.example.nutriscan.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.nutriscan.presentation.theme.AppGradients
import com.example.nutriscan.presentation.theme.softShadow

data class TabItem(
    val route: Route,
    val name: String,            // matches routeNameOf(...)
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val UserTabs = listOf(
    TabItem(Route.Home, "Home", "Beranda", Icons.Filled.Home, Icons.Outlined.Home),
    TabItem(Route.History, "History", "Riwayat", Icons.Filled.History, Icons.Outlined.History),
    TabItem(Route.Consultation, "Consultation", "Konsultasi", Icons.Filled.Forum, Icons.Outlined.Forum),
    TabItem(Route.Profile, "Profile", "Profil", Icons.Filled.Person, Icons.Outlined.Person),
)

val NutritionistTabs = listOf(
    TabItem(Route.NutritionistHome, "NutritionistHome", "Konsultasi", Icons.Filled.Forum, Icons.Outlined.Forum),
    TabItem(Route.Profile, "Profile", "Profil", Icons.Filled.Person, Icons.Outlined.Person),
)

/**
 * Bottom navigation bar. When [withCenterGap] is true, a blank slot is reserved
 * in the middle (between item 2 and 3) for the floating Scan button.
 */
@Composable
fun NutriBottomBar(
    tabs: List<TabItem>,
    currentRouteName: String?,
    onSelect: (Route) -> Unit,
    withCenterGap: Boolean = false,
    onScanClick: (() -> Unit)? = null
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        val firstHalf = if (withCenterGap) tabs.take(2) else tabs
        val secondHalf = if (withCenterGap) tabs.drop(2) else emptyList()

        firstHalf.forEach { tab -> BarItem(tab, currentRouteName, onSelect) }
        if (withCenterGap) {
            // Center slot hosting the raised Scan button.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                ScanFab(onClick = { onScanClick?.invoke() })
            }
            secondHalf.forEach { tab -> BarItem(tab, currentRouteName, onSelect) }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.BarItem(
    tab: TabItem,
    currentRouteName: String?,
    onSelect: (Route) -> Unit
) {
    val selected = currentRouteName == tab.name
    NavigationBarItem(
        selected = selected,
        onClick = { if (!selected) onSelect(tab.route) },
        icon = {
            Icon(
                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                contentDescription = tab.label
            )
        },
        label = {
            Text(
                text = tab.label,
                maxLines = 1,
                softWrap = false,
                style = MaterialTheme.typography.labelSmall
            )
        },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

/** The raised, gradient Scan button that floats in the centre of the bottom bar. */
@Composable
fun ScanFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(54.dp)
            .softShadow(elevation = 12.dp, shape = CircleShape, alpha = 0.4f)
            .clip(CircleShape)
            .background(AppGradients.brand)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.QrCodeScanner,
            contentDescription = "Scan Barcode",
            tint = Color.White,
            modifier = Modifier.size(26.dp)
        )
    }
}
