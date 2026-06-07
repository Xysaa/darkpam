package com.example.nutriscan.presentation.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutriscan.presentation.theme.AppGradients

/**
 * Gold coin balance pill, with the count animating when it changes.
 */
@Composable
fun CoinBadge(
    coins: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val animated by animateIntAsState(
        targetValue = coins,
        animationSpec = tween(500),
        label = "coinCount"
    )
    val base = modifier
        .clip(RoundedCornerShape(50))
        .background(AppGradients.coin)
    val clickable = if (onClick != null) base.clickable { onClick() } else base

    Row(
        modifier = clickable.padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.MonetizationOn,
            contentDescription = "Coin",
            tint = Color(0xFF7A4E00),
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "$animated",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5C3B00)
        )
    }
}
