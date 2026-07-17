package com.littlenest.nursery.ui.event

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentEventAddBinding
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.ui.group.Group
import com.littlenest.nursery.viewmodel.group.GroupViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UpdateEventFragment : BaseFragment(R.layout.fragment_event_add) {

    private var _binding: FragmentEventAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var groupViewModel: GroupViewModel
    private var groupList: List<Group> = emptyList()
    //Multi-select: holds selected group IDs
    private val selectedGroupIds = mutableListOf<Int>()

    private val viewModel: EventViewModel by viewModels()
    private val args: UpdateEventFragmentArgs by navArgs()

    private lateinit var token: String
    private lateinit var apiKey: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI(view: View) {
        token = getToken() ?: ""
        apiKey = getApiKey().ifEmpty { "your-very-secret-key" }

        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "Token missing. Please login again.", Toast.LENGTH_SHORT).show()
            handleLogout()
            return
        }

        setupListeners()
        setupGroupViewModel()
        //prefillEventIfEditing()
        setupSubmitButton()
        observeViewModel()

    }

    private fun setupGroupViewModel() {
        groupViewModel = ViewModelProvider(this)[GroupViewModel::class.java]
        groupViewModel.fetchGroups(token, apiKey)
        observeGroupList()
    }

    private fun observeGroupList() {
        groupViewModel.groups.observe(viewLifecycleOwner) { groups ->
            if (groups.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "No groups available", Toast.LENGTH_SHORT).show()
                return@observe
            }

            groupList = groups
            //setup dialog selector once groups are loaded
            setupGroupSelection()

            // LOAD EVENT BEFORE SETTING SELECTED GROUPS
            prefillEventIfEditing()

            //prefill selected groups AFTER groupList is loaded
            prefillGroupSelection()
        }
        groupViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun prefillGroupSelection() {
        val event = try { args.event } catch (e: Exception) { null } ?: return
        selectedGroupIds.clear()
        selectedGroupIds.addAll(event.invitedGroups)

        val selectedGroupIds = event.invitedGroups.toMutableList()
        val selectedNames = groupList
            .filter { selectedGroupIds.contains(it.id) }
            .map { it.name }

        binding.btnSelectGroups.text =
            if (selectedNames.isNotEmpty()) selectedNames.joinToString(", ")
            else "Select Groups"
    }

    private fun prefillEventIfEditing(){
        val event = try { args.event } catch (e: Exception) { null } ?: return
        binding.btnSaveEvent.text = "Update Event"
        binding.etEventName.setText(event.eventName)
        binding.etEventDescription.setText(event.eventDescription)
        binding.etEventLocation.setText(event.eventLocation)
        binding.etEventDate.setText(event.date)
        binding.etStartTime.setText(event.starts)
        binding.etEndTime.setText(event.ends)

        //repeating switch and date
        val isRepeating = event.repeating == "1"
        binding.switchRepeating.isChecked = isRepeating
        binding.etRepeatingEndsDate.isEnabled = isRepeating
        binding.etRepeatingEndsDate.alpha = if (isRepeating) 1f else 0.5f
        binding.etRepeatingEndsDate.setText(event.repeatingEnds ?: "")
    }

    private fun setupSubmitButton() {
        val event = try { args.event } catch (e: Exception) { null }

        binding.btnSaveEvent.setOnClickListener {
            if (event != null) {
                saveEvent(event.id, token,apiKey)
            }
        }
    }

    // ✅ Multi-select group dialog
    private fun setupGroupSelection() {
        binding.btnSelectGroups.setOnClickListener {
            if (groupList.isEmpty()) {
                Toast.makeText(requireContext(), "No groups available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val groupNames = groupList.map { it.name }.toTypedArray()
            val checkedItems = BooleanArray(groupNames.size) { index ->
                selectedGroupIds.contains(groupList[index].id)
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Select Groups")
                .setMultiChoiceItems(groupNames, checkedItems) { _, which, isChecked ->
                    val groupId = groupList[which].id
                    if (isChecked) selectedGroupIds.add(groupId)
                    else selectedGroupIds.remove(groupId)
                }
                .setPositiveButton("OK") { dialog, _ ->
                    val selectedNames = selectedGroupIds.mapNotNull { id ->
                        groupList.find { it.id == id }?.name
                    }
                    binding.btnSelectGroups.text =
                        if (selectedNames.isNotEmpty()) selectedNames.joinToString(", ")
                        else "Select Groups"
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setupListeners(){
        // Enable/disable repeating fields

        binding.switchRepeating.setOnCheckedChangeListener { _, isChecked ->
            binding.etRepeatingEndsDate.isEnabled = isChecked

            if (!isChecked) {
                binding.etRepeatingEndsDate.text.clear()
            }
        }


        // Date picker
        binding.etEventDate.setOnClickListener {
            val dobText = binding.etEventDate.text.toString().trim()
            val calendar = Calendar.getInstance()

            // Try to parse existing DOB if available
            if (dobText.isNotEmpty()) {
                try {
                    val format = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                    val parsedDate = format.parse(dobText)
                    parsedDate?.let { calendar.time = it }
                } catch (e: Exception) {
                    Log.e("DatePicker", "Invalid DOB format: $dobText", e)
                }
            }

            val dpd = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                    val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        .format(selectedDate.time)
                    binding.etEventDate.setText(formatted)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            // 🚫 Disable past dates
            dpd.datePicker.minDate = System.currentTimeMillis()
            dpd.show()
        }

        //etRepeatingEndsDate
        binding.etRepeatingEndsDate.setOnClickListener {
            val etRepeatingEndsDate = binding.etRepeatingEndsDate.text.toString().trim()
            val calendar = Calendar.getInstance()

            // Try to parse existing DOB if available
            if (etRepeatingEndsDate.isNotEmpty()) {
                try {
                    val format = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                    val parsedDate = format.parse(etRepeatingEndsDate)
                    parsedDate?.let { calendar.time = it }
                } catch (e: Exception) {
                    Log.e("DatePicker", "Invalid DOB format: $etRepeatingEndsDate", e)
                }
            }

            val dpd = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                    val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        .format(selectedDate.time)
                    binding.etRepeatingEndsDate.setText(formatted)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // Optional: prevent selecting a date before the event start date
            val eventStartText = binding.etEventDate.text.toString().trim()
            if (eventStartText.isNotEmpty()) {
                try {
                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val eventStartDate = format.parse(eventStartText)
                    eventStartDate?.let { dpd.datePicker.minDate = it.time }
                } catch (e: Exception) {
                    Log.e("DatePicker", "Invalid event start date format: $eventStartText", e)
                }
            }
            dpd.show()
        }

        // Start time picker
        binding.etStartTime.setOnClickListener { showTimePicker(binding.etStartTime) }

        // End time picker
        binding.etEndTime.setOnClickListener { showTimePicker(binding.etEndTime) }

    }

    private fun showTimePicker(targetEditText: EditText) {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                (targetEditText as? android.widget.EditText)?.setText(formattedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePicker.show()
    }

    private fun saveEvent(eventId: Int, token: String, apiKey: String) {
        val eventName = binding.etEventName.text.toString().trim()
        val eventDescription = binding.etEventDescription.text.toString().trim()
        val eventLocation = binding.etEventLocation.text.toString().trim()
        val eventDate = binding.etEventDate.text.toString().trim()
        val startTime = binding.etStartTime.text.toString().trim()
        val endTime = binding.etEndTime.text.toString().trim()
        val repeatingEndsInput = binding.etRepeatingEndsDate.text.toString().trim()
        val repeating = binding.switchRepeating.isChecked

        // If repeating switch is OFF or input is empty → set null
        val repeatingEnds: String? = if (!repeating|| repeatingEndsInput.isEmpty()) {
            null
        } else {
            repeatingEndsInput
        }

        // ✅ Validation checks
        when {
            eventName.isEmpty() -> {
                Toast.makeText(requireContext(), "Enter event name", Toast.LENGTH_SHORT).show()
                return
            }
            eventDescription.isEmpty() -> {
                Toast.makeText(requireContext(), "Enter event description", Toast.LENGTH_SHORT).show()
                return
            }
            eventLocation.isEmpty() -> {
                Toast.makeText(requireContext(), "Enter event location", Toast.LENGTH_SHORT).show()
                return
            }
            eventDate.isEmpty() -> {
                Toast.makeText(requireContext(), "Enter event date", Toast.LENGTH_SHORT).show()
                return
            }
            startTime.isEmpty() -> {
                Toast.makeText(requireContext(), "Enter start time", Toast.LENGTH_SHORT).show()
                return
            }
            endTime.isEmpty() -> {
                Toast.makeText(requireContext(), "Enter end time", Toast.LENGTH_SHORT).show()
                return
            }
            selectedGroupIds.isEmpty() -> {
                Toast.makeText(requireContext(), "Please select at least one group", Toast.LENGTH_SHORT).show()
                return
            }
            repeating && repeatingEnds.isNullOrEmpty() -> {
                Toast.makeText(requireContext(), "Enter repeating end date", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val request = EventCreateRequest(
            eventName = eventName,
            eventDescription = eventDescription,
            eventLocation = eventLocation,
            date = eventDate,
            starts = startTime,
            ends = endTime,
            repeating = repeating,
            repeatingEnds = repeatingEnds,
            invitedGroups = selectedGroupIds
        )

        viewModel.updateEvent(
            eventId = eventId,
            request = request,
            token = token,
            apiKey = apiKey,
        )
    }

    private fun observeViewModel() {
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()

                // Return to event list
                findNavController().popBackStack()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.events.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), "Event updated successfully!", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
