package com.littlenest.nursery.ui.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentFamilyEventBinding
import com.littlenest.nursery.ui.common.BaseFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener

class EventFragment : BaseFragment(R.layout.fragment_family_event) {
    private val viewModel: EventViewModel by viewModels()
    private lateinit var adapter: EventAdapter

    // Define constants for API query parameters matching the backend
    private val STATUS_COMING_UP = "future"
    private val STATUS_PAST = "past"

    private var _binding: FragmentFamilyEventBinding? = null
    private val binding get() = _binding!!

    private lateinit var token: String
    private lateinit var apiKey: String
    private lateinit var userRole: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFamilyEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI(view)
    }

    override fun setupUI(view: View) {
        token = getToken() ?: ""
        apiKey = getApiKey().ifEmpty { "your-very-secret-key" }
        userRole = getUserRole() ?: "student"

        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "Token missing. Please login again.", Toast.LENGTH_SHORT).show()
            handleLogout()
            return
        }

        adapter = EventAdapter(
            emptyList(),
            userRole,
            onItemClick = { event ->
                val action = EventFragmentDirections
                    .actionNavFamilyEventsToEventDetailFragment(event)
                findNavController().navigate(action)
            },
            onEditClick = {  event ->
                val bundle = Bundle().apply { putParcelable("event", event) }
                findNavController().navigate(
                    R.id.action_navFamilyEvents_to_updateEventFragment, bundle
                )
            },
            onDeleteClick = { event ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete ${event.eventName} event?")
                    .setPositiveButton("Yes") { _, _ ->
                        viewModel.deleteEvent(event.id, token, getApiKey())
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        )
        binding.recyclerViewEvents.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewEvents.adapter = adapter

        // 1. Observe events from the ViewModel
        viewModel.events.observe(viewLifecycleOwner) { events ->
            // The data received here is already filtered and sorted by the backend
            adapter.submitList(events)
        }

        // 2. Set up Tab Listener to trigger API calls
        binding.tabLayoutEvents.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // Determine which status to request based on tab index
                val statusToLoad = when (tab.position) {
                    0 -> STATUS_COMING_UP // Assuming the first tab (index 0) is "Coming up"
                    1 -> STATUS_PAST      // Assuming the second tab (index 1) is "Past"
                    else -> STATUS_COMING_UP
                }
                //Log.d("EventFragment", "Tab selected. Loading status: $statusToLoad")

                // Clear the list while fetching new data for a smoother UX
                adapter.submitList(emptyList())

                // Call the ViewModel with the selected status
                if (getUserRole() == "admin") {
                    viewModel.loadAdminEvents(getToken(), getApiKey(), statusToLoad)
                }else  if (getUserRole() == "teacher") {
                    viewModel.loadEventsforTeacher(getToken(), getApiKey(), statusToLoad)
                }else{
                    viewModel.loadEvents(getToken(), getApiKey(), statusToLoad)
                }
            }
            // Implementation for other listener methods is optional if you don't need logic there
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })


        // 3. Initial Data Load (Default to "Coming up" tab on startup)
        // Ensure the initial status matches the default selected tab (usually index 0)
        val initialStatus = when (binding.tabLayoutEvents.selectedTabPosition) {
            1 -> STATUS_PAST
            else -> STATUS_COMING_UP
        }
        if (getUserRole() == "admin") {
            viewModel.loadAdminEvents(getToken(), getApiKey(), initialStatus)
        }else  if (getUserRole() == "teacher") {
            viewModel.loadEventsforTeacher(getToken(), getApiKey(), initialStatus)
        }else{
            viewModel.loadEvents(getToken(), getApiKey(), initialStatus)
        }

        // Floating Add Button visibility
        when (userRole) {
            "admin" -> binding.EventAddFloatingIcon.visibility = View.VISIBLE
            else -> binding.EventAddFloatingIcon.visibility = View.GONE
        }

        // Floating Add Button
        binding.EventAddFloatingIcon.setOnClickListener {
            findNavController().navigate(R.id.action_navFamilyEvents_to_eventAddFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
