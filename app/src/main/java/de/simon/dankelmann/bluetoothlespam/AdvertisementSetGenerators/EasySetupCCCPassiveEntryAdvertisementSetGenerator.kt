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
import java.util.UUID

class EasySetupCCCPassiveEntryAdvertisementSetGenerator : IAdvertisementSetGenerator {

    // CCC (Car Connectivity Consortium) Passive Entry Protocol
    // Reference: https://carconnectivity.org/

    // CCC Digital Key UUID: 0xfff5
    private val _cccServiceUuid16bit = ParcelUuid.fromString("0000fff5-0000-1000-8000-00805f9b34fb")

    // CCC Service Data Intent UUID: 5810bbc0-b499-11e9-a2a3-2a2ae2dbcce4
    private val _cccServiceDataUuid = ParcelUuid.fromString("5810bbc0-b499-11e9-a2a3-2a2ae2dbcce4")

    // Vehicle brand identifiers
    val _vehicleBrands = mapOf(
        "0100aa" to "Hyundai Passive Entry (Default)",
        "010001" to "Hyundai Passive Entry (Alt 1)",
        "010002" to "Hyundai Passive Entry (Alt 2)",
        "010003" to "Hyundai Passive Entry (Alt 3)",
        "0100ff" to "Hyundai Passive Entry (Alt 4)"
    )

    override fun getAdvertisementSets(inputData: Map<String, String>?): List<AdvertisementSet> {
        var advertisementSets: MutableList<AdvertisementSet> = mutableListOf()

        val data = inputData ?: _vehicleBrands

        data.map {
            var advertisementSet: AdvertisementSet = AdvertisementSet()
            advertisementSet.target = AdvertisementTarget.ADVERTISEMENT_TARGET_SAMSUNG
            advertisementSet.type = AdvertisementSetType.ADVERTISEMENT_TYPE_EASY_SETUP_CCC_PASSIVE_ENTRY
            advertisementSet.range = AdvertisementSetRange.ADVERTISEMENTSET_RANGE_CLOSE

            // Advertise Settings
            advertisementSet.advertiseSettings.advertiseMode = AdvertiseMode.ADVERTISEMODE_LOW_LATENCY
            advertisementSet.advertiseSettings.txPowerLevel = TxPowerLevel.TX_POWER_HIGH
            advertisementSet.advertiseSettings.connectable = true
            advertisementSet.advertiseSettings.timeout = 0

            // Advertising Parameters
            advertisementSet.advertisingSetParameters.legacyMode = true
            advertisementSet.advertisingSetParameters.interval = AdvertisingSetParameters.INTERVAL_MIN
            advertisementSet.advertisingSetParameters.txPowerLevel = TxPowerLevel.TX_POWER_HIGH
            advertisementSet.advertisingSetParameters.primaryPhy = PrimaryPhy.PHY_LE_1M
            advertisementSet.advertisingSetParameters.secondaryPhy = SecondaryPhy.PHY_LE_1M

            // AdvertiseData
            advertisementSet.advertiseData.includeDeviceName = false
            advertisementSet.advertiseData.includeTxPower = false

            // Add 16-bit Service UUID (0xfff5)
            val serviceData16bit = ServiceData()
            serviceData16bit.serviceUuid = _cccServiceUuid16bit
            advertisementSet.advertiseData.services.add(serviceData16bit)

            // Add 128-bit Service Data
            val serviceData128bit = ServiceData()
            serviceData128bit.serviceUuid = _cccServiceDataUuid
            // Service Data format: IntentConfiguration (1 byte) + Vehicle Brand Identifier (2+ bytes)
            serviceData128bit.serviceData = StringHelpers.decodeHex(it.key)
            advertisementSet.advertiseData.services.add(serviceData128bit)

            // General Data
            advertisementSet.title = it.value

            // Callbacks
            advertisementSet.advertisingSetCallback = GenericAdvertisingSetCallback()
            advertisementSet.advertisingCallback = GenericAdvertisingCallback()

            advertisementSets.add(advertisementSet)
        }

        return advertisementSets.toList()
    }
}
