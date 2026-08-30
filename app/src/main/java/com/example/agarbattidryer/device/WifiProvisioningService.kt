package com.example.agarbattidryer.device

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object WifiProvisioningService {

    private const val PROVISION_IP = "192.168.4.1"
    private const val PORT = "80"

    suspend fun checkProvisioningStatus(): Boolean = withContext(Dispatchers.IO) {
        try {
            val urlString = "http://$PROVISION_IP:$PORT/provision/status"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                // true if we can read it and it responds
                return@withContext json.optString("mode") == "PROVISIONING"
            }
            false
        } catch (e: Exception) {
            Log.e("WifiProvisioning", "Failed to check status", e)
            false
        }
    }

    suspend fun provisionWifi(ssid: String, password: String): ProvisioningResult = withContext(Dispatchers.IO) {
        try {
            val urlString = "http://$PROVISION_IP:$PORT/provision/wifi"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 15000 // Give ESP32 time to try connection
            connection.readTimeout = 15000

            val jsonBody = JSONObject().apply {
                put("ssid", ssid)
                put("password", password)
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val responseJson = JSONObject(response)
                
                if (responseJson.optBoolean("success", false)) {
                    val ip = responseJson.optString("ip")
                    return@withContext ProvisioningResult.Success(ip)
                } else {
                    return@withContext ProvisioningResult.Error(responseJson.optString("status", "Unknown error"))
                }
            } else {
                return@withContext ProvisioningResult.Error("HTTP ${connection.responseCode}")
            }
        } catch (e: Exception) {
            Log.e("WifiProvisioning", "Provisioning failed", e)
            return@withContext ProvisioningResult.Error("Connection failed: ${e.message}")
        }
    }
}

sealed class ProvisioningResult {
    data class Success(val newIp: String) : ProvisioningResult()
    data class Error(val message: String) : ProvisioningResult()
}
