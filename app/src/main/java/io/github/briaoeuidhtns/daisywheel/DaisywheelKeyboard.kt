package io.github.briaoeuidhtns.daisywheel

import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import android.view.KeyEvent
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

@Composable
fun DaisyWheelKeyboard(
    modifier: Modifier = Modifier,
    viewModel: DaisywheelViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .size(300.dp)
    ) {
        // Draw center circle
        drawCircle(
            color = if (state.selectedPetalIndex == -1) Color.Blue else Color.DarkGray,
            radius = 30f,
            center = center
        )

        // Draw petals
        state.petals.forEachIndexed { index, petal ->
            drawPetal(
                petal = petal,
                isSelected = index == state.selectedPetalIndex,
                textMeasurer = textMeasurer
            )
        }
    }
}

private fun DrawScope.drawPetal(
    petal: DaisyPetal,
    isSelected: Boolean,
    textMeasurer: TextMeasurer
) {
    val radius = size.minDimension / 3
    val angleInRadians = petal.angle * (PI / 180f)

    val petalCenter = Offset(
        x = center.x + (radius * cos(angleInRadians)).toFloat(),
        y = center.y + (radius * sin(angleInRadians)).toFloat()
    )

    // Draw petal background
    drawCircle(
        color = if (isSelected) Color.Blue else Color.Gray,
        radius = 40f,
        center = petalCenter
    )

    // Draw characters around the petal
    // Start from top (270 degrees) and go clockwise
    petal.characters.forEachIndexed { index, char ->
        val charAngle = ((180 + (index * 90)) % 360) * (PI / 180f)
        val charOffset = Offset(
            x = petalCenter.x + (30f * cos(charAngle)).toFloat(),
            y = petalCenter.y + (30f * sin(charAngle)).toFloat()
        )

        // Draw character circle background
        drawCircle(
            // color = if (isSelected && selectedCharIndex == index) Color.Green else Color.White,
            color = Color.White,
            radius = 15f,
            center = charOffset
        )

        // Draw the character
        val textStyle = TextStyle(
            fontSize = 14.sp,
            color = Color.Black
        )

        val text = char.toString()
        val textLayoutResult = textMeasurer.measure(text, textStyle)

        // Center the text in the circle
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                x = charOffset.x - textLayoutResult.size.width / 2,
                y = charOffset.y - textLayoutResult.size.height / 2
            )
        )
    }
}
