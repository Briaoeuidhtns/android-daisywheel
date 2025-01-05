package io.github.briaoeuidhtns.daisywheel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DaisyWheelKeyboard(
    modifier: Modifier = Modifier,
    viewModel: DaisywheelViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val textMeasurer = rememberTextMeasurer()

    val screenWidth = LocalView.current.resources.displayMetrics.widthPixels.dp
    val screenHeight = LocalView.current.resources.displayMetrics.heightPixels.dp
    
    // Get Material You colors
    val colors = MaterialTheme.colorScheme
    val primaryColor = colors.primary
    val surfaceVariantColor = colors.surfaceVariant
    val selectedPetalColor = colors.primary  // Use primary for selected petals
    val unselectedPetalColor = colors.surfaceVariant
    val surfaceColor = colors.surface
    val onSurfaceColor = colors.onSurface
    
    // Calculate the keyboard size based on screen dimensions
    // Use 40% of the smaller screen dimension, but cap at 400.dp
    val maxSize = 400.dp
    val keyboardSize = minOf(
        minOf(screenWidth, screenHeight) * 0.4f,
        maxSize
    )
    
    Canvas(
        modifier = modifier
            .size(keyboardSize)
    ) {
        val centerCircleRadius = size.minDimension * 0.08f

        // Draw center circle using Material You colors
        drawCircle(
            color = if (state.selectedPetalIndex == -1) 
                primaryColor
            else 
                surfaceVariantColor,
            radius = centerCircleRadius,
            center = center
        )

        // Draw petals
        state.petals.forEachIndexed { index, petal ->
            drawPetal(
                petal = petal,
                isSelected = index == state.selectedPetalIndex,
                index = index,
                textMeasurer = textMeasurer,
                canvasSize = size.minDimension,
                selectedPetalColor = selectedPetalColor,
                unselectedPetalColor = unselectedPetalColor,
                surfaceColor = surfaceColor,
                onSurfaceColor = onSurfaceColor
            )
        }
    }
}

private fun DrawScope.drawPetal(
    petal: DaisyPetal,
    isSelected: Boolean,
    index: Int,
    textMeasurer: TextMeasurer,
    canvasSize: Float,
    selectedPetalColor: Color,
    unselectedPetalColor: Color,
    surfaceColor: Color,
    onSurfaceColor: Color
) {
    // Calculate sizes to prevent overlap
    // For 8 petals, the minimum angle between centers is 45 degrees (2π/8)
    // To prevent overlap, petal diameter must be less than the arc length at their radius
    val petalSpacing = 1.2f  // Safety factor > 1 to ensure clear separation

    val radius = canvasSize * 0.35f  // Distance from center to petal center
    val maxPetalSize = (2 * PI * radius / 8) / petalSpacing  // Maximum size that prevents overlap
    val petalRadius = minOf(canvasSize * 0.1f, maxPetalSize.toFloat())  // Use smaller of calculated max or desired size
    
    val angleInRadians = ((index + 6) % 8) * (2 * PI / 8)

    val petalCenter = Offset(
        x = center.x + (radius * cos(angleInRadians)).toFloat(),
        y = center.y + (radius * sin(angleInRadians)).toFloat()
    )

    // Character circles should not overlap within their petal
    val charCircleRadius = petalRadius * 0.35f  // Reduced relative to petal size
    val charDistance = petalRadius * 0.65f  // Closer to petal center to prevent overlap

    // Draw petal background using Material You colors
    drawCircle(
        color = if (isSelected) 
            selectedPetalColor
        else 
            unselectedPetalColor,
        radius = petalRadius,
        center = petalCenter
    )

    // Draw characters around the petal
    petal.characters.forEachIndexed { charIndex, char ->
        val charAngle = ((180 + (charIndex * 90)) % 360) * (PI / 180f)
        val charOffset = Offset(
            x = petalCenter.x + (charDistance * cos(charAngle)).toFloat(),
            y = petalCenter.y + (charDistance * sin(charAngle)).toFloat()
        )

        // Draw character circle background using Material You colors
        drawCircle(
            color = surfaceColor,
            radius = charCircleRadius,
            center = charOffset
        )

        // Draw the character with Material You colors
        val textStyle = TextStyle(
            fontSize = (charCircleRadius * 1.2f).sp,  // Scale font with circle size
            color = onSurfaceColor
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