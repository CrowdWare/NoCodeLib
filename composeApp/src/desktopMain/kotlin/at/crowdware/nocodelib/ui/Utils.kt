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

package at.crowdware.nocodelib.ui

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual fun getMatchRange(matchGroup: MatchGroup?): IntRange {
    return matchGroup?.range ?: IntRange.EMPTY
}

actual fun createCodeBlockRegex(): Regex {
    return Regex("```(.*?)```", RegexOption.DOT_MATCHES_ALL)
}

actual fun ioDispatcher(): CoroutineDispatcher {
    return Dispatchers.IO
}

/*
@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.pointerMoveHandler(
    onEnter: () -> Boolean,
    onExit: () -> Boolean
): Modifier {
    return this.pointerMoveFilter(
        onEnter = { onEnter() },
        onExit = { onExit() }
    )
}

 */