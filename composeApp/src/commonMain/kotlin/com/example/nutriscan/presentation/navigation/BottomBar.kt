package com.example.nutriscan.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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

private val BarHeight = 66.dp

/**
 * Self-contained, fixed-height bottom navigation bar.
 *
 * Built as a plain Box/Row (instead of Material's NavigationBar) so that its
 * height is always exactly [BarHeight] inside a Scaffold's bottomBar slot. When
 * [withCenterGap] is true a gap is left in the middle and a raised Scan button
 * is overlaid on top, poking slightly above the bar.
 */
@Composable
fun NutriBottomBar(
    tabs: List<TabItem>,
    currentRouteName: String?,
    onSelect: (Route) -> Unit,
    withCenterGap: Boolean = false,
    onScanClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(elevation = 14.dp, shape = RoundedCornerShape(0.dp), alpha = 0.18f)
            .background(MaterialTheme.colorScheme.surface)
            // Keep the bar above the system navigation buttons (edge-to-edge).
            .navigationBarsPadding()
            .height(BarHeight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(BarHeight)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val firstHalf = if (withCenterGap) tabs.take(2) else tabs
            val secondHalf = if (withCenterGap) tabs.drop(2) else emptyList()

            firstHalf.forEach { tab -> BarItem(Modifier.weight(1f), tab, currentRouteName, onSelect) }
            if (withCenterGap) {
                // Center slot hosting the raised Scan button (fully inside the bar).
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (onScanClick != null) ScanFab(onClick = onScanClick)
                }
                secondHalf.forEach { tab -> BarItem(Modifier.weight(1f), tab, currentRouteName, onSelect) }
            }
        }
    }
}

@Composable
private fun BarItem(
    modifier: Modifier,
    tab: TabItem,
    currentRouteName: String?,
    onSelect: (Route) -> Unit
) {
    val selected = currentRouteName == tab.name
    val contentColor = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { if (!selected) onSelect(tab.route) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
            contentDescription = tab.label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = tab.label,
            maxLines = 1,
            softWrap = false,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = contentColor
        )
    }
}

/** The raised, gradient Scan button that sits in the centre of the bottom bar. */
@Composable
fun ScanFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
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
            modifier = Modifier.size(27.dp)
        )
    }
}
