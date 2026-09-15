package com.batu.simpledarttracker.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch

// The flare is deliberately quick and deliberately bigger than the key. Anything slower reads as
// lag rather than as an answer, and anything that stops at the key's edge is just a fill.
private const val DIP = 0.955f          // how far the surface sinks under the finger
private const val FLARE_MILLIS = 380    // the light running out past the key
private const val FADE_MILLIS = 260     // and clearing again
private const val REACH = 1.85f         // how far past the key the light carries
private const val CORE = 0.55f          // its strength where the finger landed

/**
 * A surface that answers the finger the way a calculator key does: it sinks slightly, and a
 * bright flare bursts out of the point that was touched — *past* the key's own edges, over
 * whatever is around it — then clears.
 *
 * Two details carry the whole effect. The flare starts at the touch point rather than the middle,
 * so a wide key feels like a surface and not an animating rectangle. And nothing here clips it:
 * the light spilling over the neighbouring keys is the part that makes it read as light rather
 * than as a fill, so callers must round their own background instead of clipping this modifier.
 *
 * [flare] is the colour of that light. Give a dark key a bright one and a lit key a deeper one:
 * the flare has to be brighter or darker than what it lands on, never the same value.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.pressable(
    onClick: () -> Unit,
    enabled: Boolean = true,
    flare: Color = Brand.Gold,
    onLongClick: (() -> Unit)? = null,
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val dip by animateFloatAsState(
        targetValue = if (pressed && enabled) DIP else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 1500f),
        label = "keyDip",
    )
    val spread = remember { Animatable(0f) }
    val fade = remember { Animatable(0f) }
    var origin by remember { mutableStateOf(Offset.Unspecified) }
    val scope = rememberCoroutineScope()

    // Each interaction starts its own animation rather than queueing behind the last one: a
    // release that arrives mid-flare has to be able to interrupt it.
    LaunchedEffect(interaction) {
        interaction.interactions.collect { event ->
            when (event) {
                is PressInteraction.Press -> {
                    origin = event.pressPosition
                    scope.launch {
                        spread.snapTo(0f)
                        spread.animateTo(1f, tween(FLARE_MILLIS, easing = FastOutSlowInEasing))
                    }
                    scope.launch { fade.snapTo(1f) }
                }
                else -> scope.launch { fade.animateTo(0f, tween(FADE_MILLIS)) }
            }
        }
    }

    return this
        .graphicsLayer {
            scaleX = dip
            scaleY = dip
            // The flare has to escape the key, so this layer must not crop it.
            clip = false
        }
        .drawWithContent {
            drawContent()
            val strength = fade.value * (1f - 0.35f * spread.value)
            if (strength > 0f && origin.isSpecified) {
                val radius = size.maxDimension * REACH * spread.value
                if (radius > 0f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            0f to flare.copy(alpha = CORE * strength),
                            0.55f to flare.copy(alpha = 0.30f * strength),
                            1f to Color.Transparent,
                            center = origin,
                            radius = radius,
                        ),
                        radius = radius,
                        center = origin,
                    )
                }
            }
        }
        .combinedClickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            onClick = onClick,
            onLongClick = onLongClick,
        )
}
