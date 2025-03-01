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

package at.crowdware.nocodelib.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import at.crowdware.nocodelib.SecretKey
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Serializable
data class State(
    val windowHeight: Int,
    val windowWidth: Int,
    val windowX: Int,
    val windowY: Int,
    val lastProject: String,
    val theme: String,
    val license : String
)

class AppState() {
    var windowWidth by mutableStateOf(0)
    var windowHeight by mutableStateOf(0)
    var windowX by mutableStateOf(0)
    var windowY by mutableStateOf(0)
    var lastProject by mutableStateOf("")
    var theme by mutableStateOf("")
    var license by mutableStateOf("")
    var licenseType by mutableStateOf(LicenseType.UNDEFINED)
    var license_publisher by mutableStateOf("")
    var license_date by mutableStateOf("")

    fun initLicense() {
        var data = ""
        if (license.isEmpty()) {
            licenseType = LicenseType.UNDEFINED
            return
        }
        try {
            data = decryptStringGCM(license.trim())
        } catch (e: Exception) {
            e.printStackTrace()
            licenseType = LicenseType.UNDEFINED
            return
        }
        val parts = data.split("|")
        val type = try {
            LicenseType.valueOf(parts[0])
        } catch (e: IllegalArgumentException) {
            println("Exception: ${e.message}")
            licenseType = LicenseType.UNDEFINED
            return
        }
        license_publisher = parts[1]
        license_date = parts[2]

        println("lic: $license_publisher $license_date ${licenseType}")
        // Überprüfen, ob die Lizenz abgelaufen ist
        val licenseDate = try {
            LocalDate.parse(license_date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } catch (e: DateTimeParseException) {
            licenseType = LicenseType.EXPIRED
            return
        }

        if (licenseType != LicenseType.FREE && licenseDate.isBefore(LocalDate.now())) {
            licenseType = LicenseType.EXPIRED
            return
        }
        licenseType = type
    }


    // Helper function to convert hex string to byte array
    fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
        }
        return data
    }

    fun decryptStringGCM(encryptedHex: String): String {
        try {
            // Entschlüsselten Hex-String in Byte-Array konvertieren
            val encryptedData = hexStringToByteArray(encryptedHex)

            // IV ist in den ersten 12 Bytes
            val iv = encryptedData.copyOfRange(0, 12)

            // Ciphertext enthält den Rest (inklusive Tag)
            val cipherText = encryptedData.copyOfRange(12, encryptedData.size)

            // AES Schlüssel vorbereiten
            val secretKey = SecretKey.SECRET_KEY
            val secretKeySpec = SecretKeySpec(secretKey.toByteArray(Charsets.UTF_8), "AES")

            // GCM Parameter mit IV und Tag-Länge
            val gcmParameterSpec = GCMParameterSpec(128, iv)

            // Cipher für AES/GCM/NoPadding initialisieren
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec)

            // Entschlüsseln
            val decryptedData = cipher.doFinal(cipherText)

            // Ergebnis als String zurückgeben
            return String(decryptedData, Charsets.UTF_8)
        } catch (e: Exception) {
            throw e
        }
    }
}

fun createAppState(): AppState {
    return AppState()
}

object GlobalAppState {
    var appState: AppState? = null
}