package io.github.briaoeuidhtns.daisywheel

import android.util.Log
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

data class DaisyPetal(
    val characters: List<Char>,
    val angle: Float,
    val isSelected: Boolean = false
)

@Composable
fun DaisyWheelKeyboard(
    onCharacterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPetalIndex by remember { mutableIntStateOf(0) }
    val selectedCharIndex by remember { mutableIntStateOf(-1) }
    val textMeasurer = rememberTextMeasurer()
    val view = LocalView.current.rootView

    // Define the petals with their characters, starting from top (0 degrees)
    val petals = remember {
        listOf(
            DaisyPetal(listOf('a', 'b', 'c', 'd'), 270f),  // Top
            DaisyPetal(listOf('e', 'f', 'g', 'h'), 315f),  // Top-right
            DaisyPetal(listOf('i', 'j', 'k', 'l'), 0f),    // Right
            DaisyPetal(listOf('m', 'n', 'o', 'p'), 45f),   // Bottom-right
            DaisyPetal(listOf('q', 'r', 's', 't'), 90f),   // Bottom
            DaisyPetal(listOf('u', 'v', 'w', 'x'), 135f),  // Bottom-left
            DaisyPetal(listOf('y', 'z', ',', '.'), 180f),  // Left
            DaisyPetal(listOf(':', '/', '@', '-'), 225f)   // Top-left
        )
    }

    DisposableEffect(view) {
        val callback = View.OnGenericMotionListener { _, event ->
            if (event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK) {
                when (event.action) {
                    MotionEvent.ACTION_MOVE -> {
                        // Get joystick position
                        val xAxis = event.getAxisValue(MotionEvent.AXIS_X)
                        val yAxis = event.getAxisValue(MotionEvent.AXIS_Y)

                        // Only update if stick is moved beyond dead zone
                        if (sqrt(xAxis * xAxis + yAxis * yAxis) > 0.5f) {
                            // Calculate angle in degrees
                            val angle = (Math.toDegrees(atan2(yAxis, xAxis).toDouble()).toFloat() + 360) % 360

                            // Find closest petal
                            val newIndex = petals.indices.minBy { index ->
                                val diff = abs(angle - petals[index].angle)
                                min(diff, 360 - diff)
                            }
                            selectedPetalIndex = newIndex
                        }
                        true
                    }
                    else -> false
                }
            } else false
        }

        // Add the motion listener to the view
        view.setOnGenericMotionListener(callback)

        onDispose {
            view.setOnGenericMotionListener(null)
        }
    }

    Canvas(
        modifier = modifier
            .size(300.dp)
    ) {
        // Draw center circle
        drawCircle(
            color = Color.DarkGray,
            radius = 30f,
            center = center
        )

        // Draw petals
        petals.forEachIndexed { index, petal ->
            drawPetal(
                petal = petal,
                isSelected = index == selectedPetalIndex,
                selectedCharIndex = selectedCharIndex,
                textMeasurer = textMeasurer
            )
        }
    }
}

private fun DrawScope.drawPetal(
    petal: DaisyPetal,
    isSelected: Boolean,
    selectedCharIndex: Int,
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
            color = if (isSelected && selectedCharIndex == index) Color.Green else Color.White,
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