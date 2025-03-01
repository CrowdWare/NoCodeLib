/*
 * Copyright (C) 2025 CrowdWare
 *
 * This file is part of NoCodeLib.
 *
 *  NoCodeLib is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  NoCodeLib is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NoCodeLib.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.crowdware.nocodelib.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size


class Collisions {
    fun detect(dragObjectOffset: Offset, dragListenerOffset: Offset, dragListenerSize: Size): Boolean {
        var hasCollided = false
        if (dragObjectOffset.x in dragListenerOffset.x /*- 15*/ .. dragListenerOffset.x /*+ 15*/ + dragListenerSize.width &&
            dragObjectOffset.y in dragListenerOffset.y /*- 15*/ .. dragListenerOffset.y /*+ 15*/ + dragListenerSize.height) {
            hasCollided = true
        }
        return hasCollided
    }
}