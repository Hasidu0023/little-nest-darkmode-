package com.littlenest.nursery.ui.family

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentFamilyAboutBinding
import com.littlenest.nursery.model.Guardian
import com.littlenest.nursery.ui.student.Student
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.viewmodel.family.AboutViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.navigation.fragment.findNavController
import androidx.core.view.MenuProvider
import android.view.Menu
import android.view.MenuItem
import android.view.MenuInflater
import androidx.appcompat.widget.Toolbar


// For easy UI binding (Label | Value)
data class DetailItem(
    val label: String,
    val value: String,
    val isCheckbox: Boolean = false
)

class FamilyAboutFragment : BaseFragment(R.layout.fragment_family_about) {

    private var _binding: FragmentFamilyAboutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AboutViewModel by viewModels()

    //private fun getStudentId(): Int = 13
    private var studentId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        studentId = getProfileId()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFamilyAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        fetchStudentData()
        setupMenu()

        // Tabs: Student info / Guardian info
        binding.tabLayout.addOnTabSelectedListener(object :
            com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {

                // display pencil icon (edit student info)
                val pos = tab?.position ?: 0
                updateToolbarIconForTab(pos)

                when (tab?.position) {
                    0 -> {
                        showStudentDetailsTab()
                        binding.btnManageGuardians.visibility = View.GONE
                    }
                    1 -> {
                        showGuardianDetailsTab()
                        binding.btnManageGuardians.visibility = View.VISIBLE
                    }
                }

                binding.btnManageGuardians.setOnClickListener {
                    // Navigate to your Guardian List fragment
                    val action = FamilyAboutFragmentDirections
                        .actionFamilyAboutFragmentToGuardianListFragment(studentId = studentId)
                    findNavController().navigate(action)
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    //display pencil icon in the child info tab
    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_family_about, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_edit_student -> {
                        val student = viewModel.studentDetails.value
                        student?.let {
                            val bundle = Bundle().apply { putParcelable("student", it) }
                            findNavController().navigate(R.id.action_familyAboutFragment_to_updateStudentFragment, bundle)
                        }
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)
    }

    //Show/hide the edit icon based on the selected tab
    private fun updateToolbarIconForTab(tabPosition: Int) {
        val menu = requireActivity().findViewById<Toolbar>(R.id.toolbar)?.menu
        menu?.findItem(R.id.action_edit_student)?.isVisible = tabPosition == 0
    }

    private fun fetchStudentData() {
        val studentId = getProfileId()
        val authToken = getToken()
        val apiKey = getApiKey()

        if (authToken.isNullOrEmpty() || studentId == 0) {
            Toast.makeText(
                requireContext(),
                "Authentication token or Student ID missing.",
                Toast.LENGTH_LONG
            ).show()
            handleLogout()
        } else {
            viewModel.fetchStudentDetails(studentId, "Bearer $authToken", apiKey)
            viewModel.fetchGuardians("Bearer $authToken", apiKey)
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.detailsContainer.isVisible = !isLoading
            binding.headerContainer.isVisible = !isLoading
            binding.tabLayout.isVisible = !isLoading
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                viewModel.clearMessage()
            }
        }

        viewModel.studentDetails.observe(viewLifecycleOwner) { student ->
            if (binding.tabLayout.selectedTabPosition == 0) {
                student?.let { updateUiWithStudentDetails(it) }
            }
        }

        viewModel.guardians.observe(viewLifecycleOwner) { guardians ->
            if (binding.tabLayout.selectedTabPosition == 1) {
                displayGuardians(guardians)
            }
        }
    }

    /** ---------------- STUDENT INFO TAB ---------------- */
    private fun showStudentDetailsTab() {
        viewModel.studentDetails.value?.let { updateUiWithStudentDetails(it) }
    }

    private fun formatDateForDisplay(apiDate: String): String {
        return try {
            val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val uiFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            uiFormat.format(apiFormat.parse(apiDate)!!)
        } catch (e: Exception) {
            apiDate
        }
    }

    private fun formatTimeForDisplay(apiTime: String?): String {
        return apiTime?.substring(0, 5) ?: "--:--"
    }

    private fun updateUiWithStudentDetails(student: Student) {
        val extra = student.extraData
        binding.textFullName.text = extra.fullName

        // Load profile picture
        val profilePicturePath = extra.profilePicture ?: ""
        val profileImageUrl = uploadsBaseUrl() + profilePicturePath

            //if (profilePicturePath.startsWith("http")) profilePicturePath else getBaseUrl() + profilePicturePath

        if (profileImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(profileImageUrl)
                .transform(CircleCrop())
                .placeholder(R.drawable.avatar_placeholder)
                .into(binding.imageProfile)
        }

        val detailsList = listOf(
            DetailItem("Nickname", formatDateForDisplay(extra.nickname ?: "N/A")),
            DetailItem("Date of birth", formatDateForDisplay(extra.dateOfBirth ?: "N/A")),
            DetailItem("Photo consent", extra.photoConsent?.toString() ?: "No", isCheckbox = true),
            DetailItem("Native language", extra.nativeLanguage ?: "N/A"),
            DetailItem("Dropoff time", formatTimeForDisplay(extra.dropOffTime)),
            DetailItem("Pickup time", formatTimeForDisplay(extra.pickupTime)),
            DetailItem("Address", extra.address ?: "N/A"),
            DetailItem("City", extra.city ?: "N/A"),
            DetailItem("Allergies", extra.allergies ?: "N/A"),
            DetailItem("Comment", extra.comment ?: "N/A")
        )

        binding.detailsContainer.removeAllViews()
        detailsList.forEach { item ->
            createDetailRow(
                item.label,
                item.value,
                item.isCheckbox,
                checked = (item.label == "Photo consent" && extra.photoConsent == true)
            )
        }
    }

    /** ---------------- GUARDIAN INFO TAB ---------------- */
    private fun showGuardianDetailsTab() {
        val guardians = viewModel.guardians.value ?: emptyList()
        displayGuardians(guardians)
    }

    private fun displayGuardians(guardians: List<Guardian>) {
        binding.detailsContainer.removeAllViews()

        guardians.forEach { guardian ->
            createDetailRow("Relation", guardian.relation, false)
            createDetailRow("Name", guardian.name, false)
            createDetailRow("Occupation", guardian.occupation ?: "N/A", false)
            createDetailRow("Working place", guardian.workPlace ?: "N/A", false)
            createDetailRow("Native language", guardian.nativeLanguage ?: "N/A", false)
            createDetailRow("Work phone", guardian.workPhone ?: "N/A", false)
            createDetailRow("Home phone", guardian.homePhone ?: "N/A", false)
            createDetailRow("Mobile phone", guardian.mobilePhone ?: "N/A", false)
            createDetailRow("Email", guardian.email ?: "N/A", false)
            createDetailRow(
                "Pickup permission",
                "",
                isCheckbox = true,
                checked = guardian.pickupPermission
            )

            // Separator between guardians
            val separator = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    8
                )
            }
            binding.detailsContainer.addView(separator)
        }
    }

    /** ---------------- UI HELPER ---------------- */
    private fun createDetailRow(
        label: String,
        value: String,
        isCheckbox: Boolean,
        checked: Boolean = false
    ) {
        val context = requireContext()
        val rowVerticalPadding = resources.getDimensionPixelSize(R.dimen.spacing_large)

        val row = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, rowVerticalPadding, 0, rowVerticalPadding)
        }

        val labelView = TextView(context).apply {
            text = label
            textSize = 16f
            setTextColor(Color.parseColor("#4A4A4A"))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.5f)
        }
        row.addView(labelView)

        if (isCheckbox) {
            val spacer = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, 0, 1f)
            }
            row.addView(spacer)

            val checkBox = CheckBox(context).apply {
                isChecked = checked
                isEnabled = false
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply { gravity = Gravity.END }
            }
            row.addView(checkBox)
        } else {
            val valueView = TextView(context).apply {
                text = value
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.BLACK)
                gravity = Gravity.END
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            row.addView(valueView)
        }

        binding.detailsContainer.addView(row)

        val separator = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1
            )
            setBackgroundColor(Color.parseColor("#E0E0E0"))
        }
        binding.detailsContainer.addView(separator)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
