package com.littlenest.nursery.ui.event

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentFamilyEventDetailBinding
import com.littlenest.nursery.ui.common.BaseFragment

class EventDetailFragment : BaseFragment(R.layout.fragment_family_event_detail) {

    private var _binding: FragmentFamilyEventDetailBinding? = null
    private val binding get() = _binding!!

    private val args: EventDetailFragmentArgs by navArgs()
    private val viewModel: EventDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFamilyEventDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI(view: View) {
        val event = args.event
        val isStudent = getUserRole() == "student"
        val token = getToken()
        val apiKey = getApiKey().ifEmpty { "your-very-secret-key" }

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token missing. Please login again.", Toast.LENGTH_SHORT).show()
            handleLogout()
            return
        }

        binding.textEventDetailTitle.text = event.eventName
        binding.textEventDetailDescription.text = event.eventDescription
        binding.textEventDetailLocation.text = event.eventLocation
        binding.textEventDetailDate.text = event.date
        binding.textEventDetailTime.text = "${event.starts} - ${event.ends}"

        // Default status until API response comes
        binding.textCurrentStatus.text = "Your response: ${event.status}"

        // Disable buttons if event is in the past
        val today = java.util.Calendar.getInstance().time
        val eventDate = try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            sdf.parse(event.date)
        } catch (e: Exception) {
            null
        }

        // ADMIN OR TEACHER → HIDE BUTTONS
        if (!isStudent) {
            binding.buttonAccept.visibility = View.GONE
            binding.buttonDecline.visibility = View.GONE
            binding.textCurrentStatus.visibility = View.GONE

            binding.summaryCard.visibility = View.VISIBLE
            viewModel.loadSummary(event.id, token, apiKey)

            // Observe summary results
            viewModel.summary.observe(viewLifecycleOwner) { response ->
                binding.recyclerEventSummary.layoutManager = LinearLayoutManager(requireContext())
                val adapter = EventSummaryAdapter(response.groups)
                binding.recyclerEventSummary.adapter = adapter
            }
        }else {
            if (eventDate != null && eventDate.before(today)) {
                binding.buttonAccept.visibility = View.GONE
                binding.buttonDecline.visibility = View.GONE
            } else {
                binding.buttonAccept.visibility = View.VISIBLE
                binding.buttonDecline.visibility = View.VISIBLE

                updateUiBasedOnStatus(event.status ?: "pending")

                binding.buttonAccept.setOnClickListener {
                    if (!binding.buttonAccept.isEnabled) return@setOnClickListener

                    updateUiBasedOnStatus("accepted")
                    viewModel.respondToEvent(getToken(), getApiKey(), event.id, "accepted")
                }

                binding.buttonDecline.setOnClickListener {
                    if (!binding.buttonDecline.isEnabled) return@setOnClickListener

                    updateUiBasedOnStatus("declined")
                    viewModel.respondToEvent(getToken(), getApiKey(), event.id, "declined")
                }
            }
        }

        // Observe API response
        viewModel.response.observe(viewLifecycleOwner) { resp ->
            val status = resp.participant.status
            updateUiBasedOnStatus(status)
            Toast.makeText(requireContext(), resp.message, Toast.LENGTH_SHORT).show()
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            Log.d("Event Detail Fragment Error", "${error}")
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUiBasedOnStatus(status: String) {
        val isStudent = getUserRole() == "student"

        if (!isStudent) {
            binding.buttonAccept.visibility = View.GONE
            binding.buttonDecline.visibility = View.GONE
            binding.textCurrentStatus.visibility = View.GONE
            return
        }

        binding.textCurrentStatus.text = "Your response: $status"

        when (status) {
            "accepted" -> {
                binding.buttonAccept.isEnabled = false
                binding.buttonDecline.isEnabled = true
            }
            "declined" -> {
                binding.buttonAccept.isEnabled = true
                binding.buttonDecline.isEnabled = false
            }
            else -> { // pending / not responded
                binding.buttonAccept.isEnabled = true
                binding.buttonDecline.isEnabled = true
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
