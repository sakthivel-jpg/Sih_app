package com.example.agarbattidryer.di

import com.example.agarbattidryer.data.repository.DeviceRepository
import com.example.agarbattidryer.data.repository.DeviceRepositoryImpl
import com.example.agarbattidryer.device.DeviceService
import com.example.agarbattidryer.device.MockDeviceService
import com.example.agarbattidryer.data.repository.DryingHistoryRepository

/**
 * Dependency container providing singleton instances across the app.
 */
interface AppContainer {
    val deviceService: DeviceService
    val deviceRepository: DeviceRepository
    val historyRepository: DryingHistoryRepository
}

class DefaultAppContainer(private val context: android.content.Context) : AppContainer {

    override val deviceService: com.example.agarbattidryer.device.DynamicDeviceService by lazy {
        com.example.agarbattidryer.device.DynamicDeviceService(
            com.example.agarbattidryer.device.WifiDeviceService(context)
        )
    }

    override val deviceRepository: DeviceRepository by lazy {
        DeviceRepositoryImpl(deviceService)
    }

    override val historyRepository: DryingHistoryRepository by lazy {
        DryingHistoryRepository(com.example.agarbattidryer.data.local.AppDatabase.getDatabase(context).dryingBatchDao())
    }
}
