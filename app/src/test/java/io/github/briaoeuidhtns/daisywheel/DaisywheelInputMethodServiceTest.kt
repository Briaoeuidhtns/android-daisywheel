package io.github.briaoeuidhtns.daisywheel

import android.view.InputDevice
import android.view.MotionEvent
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.Assert.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class DaisywheelInputMethodServiceTest {

    @Test
    fun `calculatePetalIndex returns correct indices for cardinal directions`() {
        // Up (270°)
        assertEquals(0, DaisywheelInputMethodService.calculatePetalIndex(0f, -1f))
        // Up-Right (315°)
        assertEquals(1, DaisywheelInputMethodService.calculatePetalIndex(0.707f, -0.707f))
        // Right (0°)
        assertEquals(2, DaisywheelInputMethodService.calculatePetalIndex(1f, 0f))
        // Down-Right (45°)
        assertEquals(3, DaisywheelInputMethodService.calculatePetalIndex(0.707f, 0.707f))
        // Down (90°)
        assertEquals(4, DaisywheelInputMethodService.calculatePetalIndex(0f, 1f))
        // Down-Left (135°)
        assertEquals(5, DaisywheelInputMethodService.calculatePetalIndex(-0.707f, 0.707f))
        // Left (180°)
        assertEquals(6, DaisywheelInputMethodService.calculatePetalIndex(-1f, 0f))
        // Up-Left (225°)
        assertEquals(7, DaisywheelInputMethodService.calculatePetalIndex(-0.707f, -0.707f))
    }

    @Test
    fun `calculatePetalIndex returns -1 within deadzone`() {
        // Test points within deadzone (magnitude <= 0.5)
        assertEquals(-1, DaisywheelInputMethodService.calculatePetalIndex(0.3f, 0.3f))
        assertEquals(-1, DaisywheelInputMethodService.calculatePetalIndex(0f, 0f))
        assertEquals(-1, DaisywheelInputMethodService.calculatePetalIndex(-0.4f, 0.2f))
    }

    @Test
    fun `calculatePetalIndex handles edge cases correctly`() {
        // Map angles to their expected indices based on the layout
        val angleToIndex = mapOf(
            270.0 to 0, // Top
            315.0 to 1, // Top-right
            0.0 to 2,   // Right
            45.0 to 3,  // Bottom-right
            90.0 to 4,  // Bottom
            135.0 to 5, // Bottom-left
            180.0 to 6, // Left
            225.0 to 7  // Top-left
        )
        
        angleToIndex.forEach { (degrees, expectedIndex) ->
            val angle = Math.toRadians(degrees)
            val x = cos(angle).toFloat()
            val y = sin(angle).toFloat()
            
            // Test exact angle
            assertEquals(
                "Expected index $expectedIndex at angle ${degrees}°",
                expectedIndex,
                DaisywheelInputMethodService.calculatePetalIndex(x, y)
            )
            
            // Test slightly before and after each boundary
            val beforeAngle = Math.toRadians(degrees - 22.0) // Test 22° before
            val afterAngle = Math.toRadians(degrees + 22.0)  // Test 22° after
            val xBefore = cos(beforeAngle).toFloat()
            val yBefore = sin(beforeAngle).toFloat()
            val xAfter = cos(afterAngle).toFloat()
            val yAfter = sin(afterAngle).toFloat()
            
            // Get expected indices for before and after, handling wraparound
            val expectedBeforeIndex = angleToIndex[((degrees - 45 + 360) % 360)] ?: 
                                    if (degrees == 0.0) 1 else 0
            val expectedAfterIndex = angleToIndex[((degrees + 45) % 360)] ?:
                                   if (degrees == 315.0) 2 else 0
            
            assertEquals(
                "Expected index $expectedIndex at angle just before ${degrees}°",
                expectedIndex,
                DaisywheelInputMethodService.calculatePetalIndex(xBefore, yBefore)
            )
            
            assertEquals(
                "Expected index $expectedIndex at angle just after ${degrees}°",
                expectedIndex,
                DaisywheelInputMethodService.calculatePetalIndex(xAfter, yAfter)
            )
        }
    }

    @Test
    fun `processJoystickEvent handles non-joystick events correctly`() {
        val nonJoystickEvent = mockk<MotionEvent> {
            every { source } returns InputDevice.SOURCE_TOUCHSCREEN
        }
        val (isJoystickMove, petalIndex) = DaisywheelInputMethodService.processJoystickEvent(nonJoystickEvent)
        assertFalse(isJoystickMove)
        assertEquals(-1, petalIndex)
    }

    @Test
    fun `processJoystickEvent handles non-move actions correctly`() {
        val nonMoveEvent = mockk<MotionEvent> {
            every { source } returns InputDevice.SOURCE_JOYSTICK
            every { action } returns MotionEvent.ACTION_UP
        }
        val (isJoystickMove, petalIndex) = DaisywheelInputMethodService.processJoystickEvent(nonMoveEvent)
        assertFalse(isJoystickMove)
        assertEquals(-1, petalIndex)
    }

    @Test
    fun `processJoystickEvent processes valid joystick moves correctly`() {
        val validMoveEvent = mockk<MotionEvent> {
            every { source } returns InputDevice.SOURCE_JOYSTICK
            every { action } returns MotionEvent.ACTION_MOVE
            every { getAxisValue(MotionEvent.AXIS_X) } returns 1f
            every { getAxisValue(MotionEvent.AXIS_Y) } returns 0f
        }
        val (isJoystickMove, petalIndex) = DaisywheelInputMethodService.processJoystickEvent(validMoveEvent)
        assertTrue(isJoystickMove)
        assertEquals(2, petalIndex) // Should be right direction (2nd petal)
    }

    @Test
    fun `processJoystickEvent handles deadzone correctly`() {
        val deadzoneEvent = mockk<MotionEvent> {
            every { source } returns InputDevice.SOURCE_JOYSTICK
            every { action } returns MotionEvent.ACTION_MOVE
            every { getAxisValue(MotionEvent.AXIS_X) } returns 0.3f
            every { getAxisValue(MotionEvent.AXIS_Y) } returns 0.3f
        }
        val (isJoystickMove, petalIndex) = DaisywheelInputMethodService.processJoystickEvent(deadzoneEvent)
        assertTrue(isJoystickMove)
        assertEquals(-1, petalIndex)
    }
}