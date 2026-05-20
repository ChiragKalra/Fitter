package com.bruhascended.fitapp.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Drop zone that slides down from the top when a card is being dragged.
 * When the dragged card hovers over this area, it highlights red to signal
 * that releasing will hide the card from the dashboard.
 */
@Composable
internal fun DashboardRemoveDropZone(
    visible: Boolean,
    isHovering: Boolean,
    onBoundsChanged: (Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(dampingRatio = 0.72f, stiffness = 600f),
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = spring(dampingRatio = 0.9f, stiffness = 800f),
        ),
        modifier = modifier,
    ) {
        val bgColor = if (isHovering) Color(0xFFE53935) else MaterialTheme.colors.surface.copy(alpha = 0.92f)
        val contentColor = if (isHovering) Color.White else MaterialTheme.colors.onSurface.copy(alpha = 0.7f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .onGloballyPositioned { coords ->
                    onBoundsChanged(coords.boundsInWindow())
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove from dashboard",
                        tint = contentColor,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = "Remove",
                        color = contentColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
