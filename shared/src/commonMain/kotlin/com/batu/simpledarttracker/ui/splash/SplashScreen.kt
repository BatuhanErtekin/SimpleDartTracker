package com.batu.simpledarttracker.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.batu.simpledarttracker.ui.brand.DartMark
import com.batu.simpledarttracker.ui.theme.Brand
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import simpledarttracker.shared.generated.resources.Res
import simpledarttracker.shared.generated.resources.brand_line1
import simpledarttracker.shared.generated.resources.brand_line2

/**
 * Splash screen: the rings settle into place, the dart sticks in the bull at 45°, the
 * wordmark fades in and a green bar shows loading. Calls [onFinished] when it is done.
 */
@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rings = remember { Animatable(0f) }
    val dart = remember { Animatable(0f) }
    val word = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { rings.animateTo(1f, tween(600)) }
        launch { delay(280); dart.animateTo(1f, tween(600)) }
        launch { delay(520); word.animateTo(1f, tween(600)) }
        launch { delay(300); progress.animateTo(1f, tween(1300)) }
        delay(1750)
        onFinished()
    }

    Box(
        modifier = modifier.fillMaxSize().background(Brand.Slate),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DartMark(
                lineColor = Brand.Chalk,
                greenColor = Brand.Green,
                redColor = Brand.Red,
                haloColor = Brand.Slate,
                ringsAppear = rings.value,
                dartAppear = dart.value,
                modifier = Modifier.size(132.dp),
            )
            Spacer(Modifier.height(24.dp))
            Column(
                modifier = Modifier.alpha(word.value),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(Res.string.brand_line1),
                    color = Brand.Chalk,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    letterSpacing = 0.02.em,
                )
                Text(
                    text = stringResource(Res.string.brand_line2),
                    color = Brand.Chalk.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Light,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.em,
                )
            }
        }

        // Loading bar.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp)
                .width(132.dp)
                .height(3.dp)
                .background(Brand.Chalk.copy(alpha = 0.14f), RoundedCornerShape(2.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.value)
                    .fillMaxHeight()
                    .background(Brand.Green, RoundedCornerShape(2.dp)),
            )
        }
    }
}
