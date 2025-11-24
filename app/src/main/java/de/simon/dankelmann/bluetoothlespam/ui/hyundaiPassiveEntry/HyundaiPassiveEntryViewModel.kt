package de.simon.dankelmann.bluetoothlespam.ui.hyundaiPassiveEntry

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HyundaiPassiveEntryViewModel : ViewModel() {

    val isAdvertising = MutableLiveData<Boolean>(false)
    val selectedVariant = MutableLiveData<String>("0100aa")
    val customServiceData = MutableLiveData<String>("")
    val advertisementCount = MutableLiveData<Int>(0)

}
