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

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class LicenseManager {
    suspend fun checkLicense(licenseKey: String): LicenseStatus {
        val client = HttpClient()
        return client.get("YOUR_SERVER_URL/check_license") {
            parameter("licenseKey", licenseKey)
        }.body()
    }
}

data class LicenseStatus(
    val isValid: Boolean = false,
    val licenseType: String = "",
    val daysRemaining: Int = 0
)

// Beispiel zum Verwenden:

suspend fun test() {
    val manager = LicenseManager()
    val licenseKey = "dein-lizenz-schluessel"
    val status = manager.checkLicense(licenseKey)
    println("License Status: ${status.isValid}, Type: ${status.licenseType}, Days Left: ${status.daysRemaining}")
}
