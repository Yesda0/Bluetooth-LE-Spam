package de.simon.dankelmann.bluetoothlespam.AdvertisementSetGenerators

import android.bluetooth.le.AdvertisingSetParameters
import android.os.ParcelUuid
import de.simon.dankelmann.bluetoothlespam.Callbacks.GenericAdvertisingCallback
import de.simon.dankelmann.bluetoothlespam.Callbacks.GenericAdvertisingSetCallback
import de.simon.dankelmann.bluetoothlespam.Enums.AdvertiseMode
import de.simon.dankelmann.bluetoothlespam.Enums.AdvertisementSetRange
import de.simon.dankelmann.bluetoothlespam.Enums.AdvertisementSetType
import de.simon.dankelmann.bluetoothlespam.Enums.AdvertisementTarget
import de.simon.dankelmann.bluetoothlespam.Enums.PrimaryPhy
import de.simon.dankelmann.bluetoothlespam.Enums.SecondaryPhy
import de.simon.dankelmann.bluetoothlespam.Enums.TxPowerLevel
import de.simon.dankelmann.bluetoothlespam.Helpers.StringHelpers
import de.simon.dankelmann.bluetoothlespam.Models.AdvertiseData
import de.simon.dankelmann.bluetoothlespam.Models.AdvertisementSet
import de.simon.dankelmann.bluetoothlespam.Models.ServiceData

class HyundaiPassiveEntryAdvertisementSetGenerator : IAdvertisementSetGenerator {

    // Hyundai Passive Entry uses CCC (Car Connectivity Consortium) Digital Key Protocol
    // Based on Wireshark capture analysis

    // CCC Digital Key UUID: 0xfff5 (16-bit Service UUID)
    // This appears in the 16-bit Service Class UUIDs list (AD Type 0x03)
    private val _cccServiceUuid16bit = ParcelUuid.fromString("0000fff5-0000-1000-8000-00805f9b34fb")

    // 128-bit Service Data UUID (from Wireshark capture)
    // UUID: 5810bbc0-b499-11e9-a2a3-2a2ae2dbcce4
    // This is used with AD Type 0x21 (Service Data - 128-bit UUID)
    private val _serviceDataUuid128bit = ParcelUuid.fromString("5810bbc0-b499-11e9-a2a3-2a2ae2dbcce4")

    // Service Data values for Hyundai Passive Entry
    // Format from Wireshark: 0100aa (3 bytes)
    val _serviceDataVariants = mapOf(
        "0100aa" to "Hyundai Passive Entry (Default)",
        "010001" to "Hyundai Passive Entry (Variant 1)",
        "010002" to "Hyundai Passive Entry (Variant 2)",
        "010003" to "Hyundai Passive Entry (Variant 3)",
        "0100ff" to "Hyundai Passive Entry (Variant 4)"
    )

    override fun getAdvertisementSets(inputData: Map<String, String>?): List<AdvertisementSet> {
        val advertisementSets: MutableList<AdvertisementSet> = mutableListOf()

        val data = inputData ?: _serviceDataVariants

        data.forEach { (serviceDataHex, title) ->
            val advertisementSet = AdvertisementSet()

            // Basic configuration
            advertisementSet.target = AdvertisementTarget.ADVERTISEMENT_TARGET_HYUNDAI
            advertisementSet.type = AdvertisementSetType.ADVERTISEMENT_TYPE_HYUNDAI_PASSIVE_ENTRY
            advertisementSet.range = AdvertisementSetRange.ADVERTISEMENTSET_RANGE_CLOSE
            advertisementSet.title = title

            // Advertise Settings
            advertisementSet.advertiseSettings.advertiseMode = AdvertiseMode.ADVERTISEMODE_LOW_LATENCY
            advertisementSet.advertiseSettings.txPowerLevel = TxPowerLevel.TX_POWER_HIGH
            advertisementSet.advertiseSettings.connectable = true
            advertisementSet.advertiseSettings.timeout = 0

            // Advertising Parameters
            // IMPORTANT: Using Legacy Mode to ensure ADV_IND packet type (not ADV_SCAN_IND)
            // connectable=true, scannable=true creates ADV_IND
            advertisementSet.advertisingSetParameters.legacyMode = true
            advertisementSet.advertisingSetParameters.interval = AdvertisingSetParameters.INTERVAL_MIN
            advertisementSet.advertisingSetParameters.txPowerLevel = TxPowerLevel.TX_POWER_HIGH
            advertisementSet.advertisingSetParameters.primaryPhy = PrimaryPhy.PHY_LE_1M
            advertisementSet.advertisingSetParameters.secondaryPhy = SecondaryPhy.PHY_LE_1M
            advertisementSet.advertisingSetParameters.connectable = true
            advertisementSet.advertisingSetParameters.scanable = true

            // Advertise Data
            advertisementSet.advertiseData.includeDeviceName = false
            advertisementSet.advertiseData.includeTxPower = false

            // BLE Advertisement Flags (from Wireshark: 0x04)
            // Bit 2 (0x04): BR/EDR Not Supported = true
            // Bit 1 (0x02): LE General Discoverable Mode = false
            // This creates: AD Length=2, AD Type=0x01, Flags=0x04
            advertisementSet.advertiseData.flags = 0x04.toByte()

            // Add 16-bit Service UUID (0xfff5) to Service UUID List
            // This creates: AD Length=3, AD Type=0x03, UUID=0xfff5
            val serviceUuidEntry = ServiceData()
            serviceUuidEntry.serviceUuid = _cccServiceUuid16bit
            serviceUuidEntry.serviceData = null  // No data, just UUID in the list
            advertisementSet.advertiseData.services.add(serviceUuidEntry)

            // Add Service Data with 128-bit UUID
            // This creates: AD Length=20, AD Type=0x21, UUID=128-bit, Data=0100aa
            // From Wireshark: 14 21 e4ccdbe22a2aa3a2e91199b4c0bb1058 0100aa
            val serviceDataEntry = ServiceData()
            serviceDataEntry.serviceUuid = _serviceDataUuid128bit
            serviceDataEntry.serviceData = StringHelpers.decodeHex(serviceDataHex)
            advertisementSet.advertiseData.services.add(serviceDataEntry)

            // Callbacks
            advertisementSet.advertisingSetCallback = GenericAdvertisingSetCallback()
            advertisementSet.advertisingCallback = GenericAdvertisingCallback()

            advertisementSets.add(advertisementSet)
        }

        return advertisementSets.toList()
    }
}
