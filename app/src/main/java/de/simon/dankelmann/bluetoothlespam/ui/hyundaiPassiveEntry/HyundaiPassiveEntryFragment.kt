package de.simon.dankelmann.bluetoothlespam.ui.hyundaiPassiveEntry

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import de.simon.dankelmann.bluetoothlespam.AdvertisementSetGenerators.HyundaiPassiveEntryAdvertisementSetGenerator
import de.simon.dankelmann.bluetoothlespam.BleSpamApplication
import de.simon.dankelmann.bluetoothlespam.Enums.AdvertisementQueueMode
import de.simon.dankelmann.bluetoothlespam.Enums.AdvertisementSetRange
import de.simon.dankelmann.bluetoothlespam.Models.AdvertisementSetCollection
import de.simon.dankelmann.bluetoothlespam.Models.AdvertisementSetList
import de.simon.dankelmann.bluetoothlespam.R
import de.simon.dankelmann.bluetoothlespam.databinding.FragmentHyundaiPassiveEntryBinding

class HyundaiPassiveEntryFragment : Fragment() {

    private val _logTag = "HyundaiPassiveEntryFragment"

    private var _viewModel: HyundaiPassiveEntryViewModel? = null
    private val viewModel get() = _viewModel!!

    private var _binding: FragmentHyundaiPassiveEntryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewModel = ViewModelProvider(this)[HyundaiPassiveEntryViewModel::class.java]
        _binding = FragmentHyundaiPassiveEntryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupUi(root.context)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        val app = (requireContext().applicationContext as BleSpamApplication)
        viewModel.isAdvertising.postValue(app.queueHandler.isActive())
    }

    private fun setupUi(context: Context) {
        // Title
        val titleTextView: TextView = binding.hyundaiPassiveEntryTitle
        titleTextView.text = getString(R.string.menu_hyundai_passive_entry)

        // Description
        val descriptionTextView: TextView = binding.hyundaiPassiveEntryDescription
        descriptionTextView.text = getString(R.string.hyundai_passive_entry_description)

        // Radio Group for variants
        val radioGroup: RadioGroup = binding.hyundaiPassiveEntryRadioGroup

        // Set default selection
        radioGroup.check(R.id.hyundai_passive_entry_radio_default)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.hyundai_passive_entry_radio_default -> viewModel.selectedVariant.postValue("0100aa")
                R.id.hyundai_passive_entry_radio_variant1 -> viewModel.selectedVariant.postValue("010001")
                R.id.hyundai_passive_entry_radio_variant2 -> viewModel.selectedVariant.postValue("010002")
                R.id.hyundai_passive_entry_radio_variant3 -> viewModel.selectedVariant.postValue("010003")
                R.id.hyundai_passive_entry_radio_variant4 -> viewModel.selectedVariant.postValue("0100ff")
            }
        }

        // Custom input
        val customInput: TextInputEditText = binding.hyundaiPassiveEntryCustomInput

        // Start button
        val startButton: Button = binding.hyundaiPassiveEntryStartButton
        startButton.setOnClickListener {
            val customData = customInput.text.toString().trim()
            startAdvertising(context, customData)
        }

        // Stop button
        val stopButton: Button = binding.hyundaiPassiveEntryStopButton
        stopButton.setOnClickListener {
            stopAdvertising(context)
        }

        // Observe advertising state
        viewModel.isAdvertising.observe(viewLifecycleOwner) { isAdvertising ->
            startButton.isEnabled = !isAdvertising
            stopButton.isEnabled = isAdvertising
        }
    }

    private fun startAdvertising(context: Context, customData: String) {
        val generator = HyundaiPassiveEntryAdvertisementSetGenerator()

        // Determine which data to use
        val advertisementData = if (customData.isNotEmpty()) {
            // Use custom data
            if (!isValidHex(customData)) {
                Toast.makeText(context, "Invalid hex string. Use format like: 0100aa", Toast.LENGTH_SHORT).show()
                return
            }
            mapOf(customData to "Hyundai Passive Entry (Custom)")
        } else {
            // Use selected variant
            val variant = viewModel.selectedVariant.value ?: "0100aa"
            mapOf(variant to "Hyundai Passive Entry (${variant})")
        }

        val advertisementSets = generator.getAdvertisementSets(advertisementData)

        if (advertisementSets.isEmpty()) {
            Toast.makeText(context, "Failed to generate advertisement sets", Toast.LENGTH_SHORT).show()
            return
        }

        // Create collection
        val collection = AdvertisementSetCollection()
        collection.title = "Hyundai Passive Entry"

        val list = AdvertisementSetList()
        list.title = "Hyundai Passive Entry Advertisements"
        list.advertisementSets = advertisementSets.toMutableList()

        collection.advertisementSetLists = mutableListOf(list)

        // Queue and start advertising
        val app = (context.applicationContext as BleSpamApplication)
        app.queueHandler.setAdvertisementQueueMode(AdvertisementQueueMode.ADVERTISEMENT_QUEUE_MODE_LINEAR_LOOP)
        app.queueHandler.setAdvertisementSetCollection(collection)
        app.queueHandler.activate(context)

        viewModel.isAdvertising.postValue(true)
        viewModel.advertisementCount.postValue(advertisementSets.size)

        Toast.makeText(context, "Started Hyundai Passive Entry advertising", Toast.LENGTH_SHORT).show()
    }

    private fun stopAdvertising(context: Context) {
        val app = (context.applicationContext as BleSpamApplication)
        app.queueHandler.deactivate(context)

        viewModel.isAdvertising.postValue(false)

        Toast.makeText(context, "Stopped advertising", Toast.LENGTH_SHORT).show()
    }

    private fun isValidHex(hex: String): Boolean {
        return hex.matches(Regex("^[0-9A-Fa-f]+$")) && hex.length % 2 == 0
    }
}
