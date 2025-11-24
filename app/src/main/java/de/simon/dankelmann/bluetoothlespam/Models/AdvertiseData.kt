package de.simon.dankelmann.bluetoothlespam.Models

import android.bluetooth.le.AdvertiseData
import android.util.Log
import java.io.Serializable

class AdvertiseData : Serializable {
    private var _logTag = "AdvertiseData"

    var id = 0
    var includeDeviceName = true
    var includeTxPower = true

    // BLE Advertisement Flags (AD Type 0x01)
    // Bit 0: LE Limited Discoverable Mode
    // Bit 1: LE General Discoverable Mode
    // Bit 2: BR/EDR Not Supported
    // Bit 3: Simultaneous LE and BR/EDR to Same Device Capable (Controller)
    // Bit 4: Simultaneous LE and BR/EDR to Same Device Capable (Host)
    // If null, Android will automatically set the flags based on advertisement settings
    var flags: Byte? = null

    var manufacturerData = mutableListOf<ManufacturerSpecificData>()
    var services = mutableListOf<ServiceData>()


    fun validate():Boolean{
        //@Todo: implement validation here
        return true
    }
    fun build() : AdvertiseData?{
        if(validate()){
            var builder = AdvertiseData.Builder()

            builder.setIncludeDeviceName(includeDeviceName)

            services.forEach {
                if(it.serviceUuid != null){
                    // Only add Service UUID if there's no Service Data
                    // addServiceData() already includes the UUID, so adding it separately causes duplication
                    if(it.serviceData != null){
                        builder.addServiceData(it.serviceUuid, it.serviceData)
                    } else {
                        builder.addServiceUuid(it.serviceUuid)
                    }
                }
            }

            builder.setIncludeTxPowerLevel(includeTxPower)

            manufacturerData.forEach {
                builder.addManufacturerData(it.manufacturerId, it.manufacturerSpecificData)
            }

            // Note: Android's AdvertiseData.Builder doesn't provide a direct API to set flags.
            // The BLE stack automatically sets flags based on the advertisement settings (connectable, scannable, etc.)
            // The flags field in this model is for documentation and potential future use.
            if(flags != null) {
                Log.d(_logTag, "Custom flags requested: 0x${flags!!.toString(16).uppercase()}, but Android BLE stack will auto-set flags")
            }

            return builder.build()
        } else {
            Log.d(_logTag, "AdvertiseDataModel could not be built because its invalid")
        }
        return null
    }
}