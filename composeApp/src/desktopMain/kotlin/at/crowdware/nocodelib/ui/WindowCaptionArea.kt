/*
 * Copyright 2021 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Copyright 2025 CrowdWare
 * Licensed under the GNU General Public License, Version 3 (the "GPL");
 * You may obtain a copy of the GPL at
 *
 *     http://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Modifications made by CrowdWare.
 * - Added double-click detection for maximization and restoration
 */

package at.crowdware.nocodelib.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.WindowScope
import java.awt.Frame
import java.awt.MouseInfo
import java.awt.Point
import java.awt.Window
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter

/**
 * WindowCaptionArea is a component that allows you to drag the window using the mouse
 * and also double click to maximize and restore the window.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param content The content lambda.
 */
@Composable
fun WindowScope.WindowCaptionArea(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val handler = remember { DragHandler(window) }
    var lastClickTime by remember { mutableStateOf(0L) }
    val frame = window as? Frame ?: return

    Box(
        modifier = modifier.pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown()

                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime < 300) {
                    // Double-click detected, maximize/restore window
                    val isMaximized = frame.extendedState == Frame.MAXIMIZED_BOTH
                    frame.extendedState = if (isMaximized) Frame.NORMAL else Frame.MAXIMIZED_BOTH
                    lastClickTime = 0L // Reset click time after double-click
                } else {
                    lastClickTime = currentTime
                    // Continue with drag logic
                    handler.onDragStarted()
                }
            }
        }
    ) {
        content()
    }
}

/**
 * Converts AWT [Point] to compose [IntOffset]
 */
private fun Point.toComposeOffset() = IntOffset(x = x, y = y)

/**
 * Returns the position of the mouse pointer, in screen coordinates.
 */
private fun currentPointerLocation(): IntOffset? {
    return MouseInfo.getPointerInfo()?.location?.toComposeOffset()
}

private class DragHandler(
    private val window: Window
) {
    private var windowLocationAtDragStart: IntOffset? = null
    private var dragStartPoint: IntOffset? = null

    private val dragListener = object : MouseMotionAdapter() {
        override fun mouseDragged(event: MouseEvent) = onDrag()
    }
    private val removeListener = object : MouseAdapter() {
        override fun mouseReleased(event: MouseEvent) {
            window.removeMouseMotionListener(dragListener)
            window.removeMouseListener(this)
        }
    }

    fun onDragStarted() {
        dragStartPoint = currentPointerLocation() ?: return
        windowLocationAtDragStart = window.location.toComposeOffset()

        window.addMouseListener(removeListener)
        window.addMouseMotionListener(dragListener)
    }

    private fun onDrag() {
        val windowLocationAtDragStart = this.windowLocationAtDragStart ?: return
        val dragStartPoint = this.dragStartPoint ?: return
        val point = currentPointerLocation() ?: return
        val newLocation = windowLocationAtDragStart + (point - dragStartPoint)
        window.setLocation(newLocation.x, newLocation.y)
    }
}