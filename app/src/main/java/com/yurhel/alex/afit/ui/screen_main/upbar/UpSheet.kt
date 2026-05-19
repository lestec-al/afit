package com.yurhel.alex.afit.ui.screen_main.upbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun UpSheet(
    onDismiss: () -> Unit,
    isVisible: Boolean,
    padding: PaddingValues,
    content: @Composable ColumnScope.() -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        // Bottom layer (in fullscreen)
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(
                    color = if (isSystemInDarkTheme()) {
                        MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    }
                )
                .clickable(
                    interactionSource = null,
                    indication = null,
                    onClick = onDismiss
                )
        ) {
            // Layer with content (half of screen)
            Column(
                modifier = Modifier
                    .animateEnterExit(
                        enter = slideInVertically() + expandVertically(),
                        exit = slideOutVertically() + shrinkVertically()
                    )
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.background)
                    .clickable(
                        interactionSource = null,
                        indication = null,
                        onClick = {}
                    ),
                content = content
            )
        }
    }
}