package io.github.briaoeuidhtns.daisywheel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
    var selectedCharIndex by remember { mutableIntStateOf(-1) }
    val textMeasurer = rememberTextMeasurer()

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

    Canvas(
        modifier = modifier
            .size(300.dp)
            .focusable()
            .onKeyEvent { keyEvent ->
                when {
                    keyEvent.type == KeyEventType.KeyDown -> {
                        when (keyEvent.key) {
                            Key.DirectionRight -> {
                                selectedPetalIndex = (selectedPetalIndex + 1) % petals.size
                                true
                            }
                            Key.DirectionLeft -> {
                                selectedPetalIndex = (selectedPetalIndex - 1 + petals.size) % petals.size
                                true
                            }
                            Key.DirectionUp -> {
                                if (selectedCharIndex == -1) selectedCharIndex = 0
                                else selectedCharIndex = (selectedCharIndex + 1) % 4
                                true
                            }
                            Key.DirectionDown -> {
                                if (selectedCharIndex > 0) {
                                    selectedCharIndex--
                                } else {
                                    selectedCharIndex = -1
                                }
                                true
                            }
                            Key.Enter -> {
                                if (selectedCharIndex != -1) {
                                    onCharacterSelected(petals[selectedPetalIndex].characters[selectedCharIndex])
                                    selectedCharIndex = -1
                                }
                                true
                            }
                            else -> false
                        }
                    }
                    else -> false
                }
            }
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
                isCharacterSelected = selectedCharIndex != -1,
                selectedCharIndex = selectedCharIndex,
                textMeasurer = textMeasurer
            )
        }
    }
}

private fun DrawScope.drawPetal(
    petal: DaisyPetal,
    isSelected: Boolean,
    isCharacterSelected: Boolean,
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