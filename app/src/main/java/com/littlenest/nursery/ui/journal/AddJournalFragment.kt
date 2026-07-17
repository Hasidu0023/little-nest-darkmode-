package com.littlenest.nursery.ui.journal

import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentJournalAddBinding
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.ui.group.Group
import com.littlenest.nursery.ui.student.Student
import com.littlenest.nursery.ui.curriculumNew.*
import com.littlenest.nursery.viewmodel.group.GroupViewModel
import com.littlenest.nursery.viewmodel.student.StudentViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.littlenest.nursery.ui.journal.SelectedImageAdapter


class AddJournalFragment : BaseFragment(R.layout.fragment_journal_add) {

    private lateinit var binding: FragmentJournalAddBinding

    private val groupViewModel: GroupViewModel by viewModels()
    private val studentViewModel: StudentViewModel by viewModels()
    private val journalViewModel: JournalViewModel by viewModels()
    private val curriculumViewModel: CurriculumNewViewModel by viewModels()

    private var selectedGroup: Group? = null
    private val selectedStudents = mutableListOf<Student>()

    private var selectedCurriculumId: Int? = null
    private var curriculumDetail: CurriculumDetail? = null

    private var selectedSubTopic: SubTopicDetail? = null
    private val selectedActivities = mutableListOf<ActivityDetail>()

    private val selectedImages = mutableListOf<Uri>()

    private lateinit var token: String
    private lateinit var apiKey: String

    private lateinit var imageAdapter: SelectedImageAdapter

    // ---------------- IMAGE PICKER ----------------
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->

            if (selectedImages.size + uris.size > 5) {
                Toast.makeText(requireContext(), "Max 5 images allowed", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            val start = selectedImages.size   // ✅ track insert start
            selectedImages.addAll(uris)

            //imageAdapter.notifyItemRangeInserted(start, uris.size) // ✅ instead of notifyDataSetChanged()
            imageAdapter.notifyDataSetChanged()

            // ✅ FIXED COUNT
            binding.textImageCount.text = "${selectedImages.size} image(s)"
        }

    override fun setupUI(view: View) {

        binding = FragmentJournalAddBinding.bind(view)

        token = getToken() ?: ""
        apiKey = getApiKey().ifEmpty { "your-secret-key" }

        // LOAD DATA
        groupViewModel.fetchGroups(token, apiKey)
        studentViewModel.fetchStudents(token, apiKey)
        curriculumViewModel.fetchCurriculums(token, apiKey, 1)

        // OBSERVE ONLY DATA (NO AUTO UI TRIGGERS)
        curriculumViewModel.curriculumDetail.observe(viewLifecycleOwner) {
            curriculumDetail = it
        }

        journalViewModel.success.observe(viewLifecycleOwner) { message ->
            if (message != null && isAdded) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack(R.id.nav_family_journal, false)
            }
        }

        //select image
        imageAdapter = SelectedImageAdapter(selectedImages) { position ->

            if (position < 0 || position >= selectedImages.size) return@SelectedImageAdapter // ✅ safety

            selectedImages.removeAt(position)

            imageAdapter.notifyItemRemoved(position)
            imageAdapter.notifyItemRangeChanged(position, selectedImages.size) // ✅ prevent crash

            // ✅ UPDATE COUNT
            binding.textImageCount.text = "${selectedImages.size} image(s)"
        }

        binding.textImageCount.text = "0 image(s)" // ✅ initial state

        binding.recyclerImages.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = imageAdapter
        }

        // BUTTONS
        binding.btnSelectGroup.setOnClickListener { showGroupPicker() }
        binding.btnTagStudents.setOnClickListener { showStudentPicker() }
        binding.btnSelectCurriculum.setOnClickListener { showCurriculumPicker() }

        binding.btnSelectSubTopic.setOnClickListener {
            if (curriculumDetail == null) {
                Toast.makeText(requireContext(), "Select curriculum first", Toast.LENGTH_SHORT).show()
            } else {
                showSubTopicPicker()
            }
        }

        binding.btnSelectActivities.setOnClickListener {
            if (selectedSubTopic == null) {
                Toast.makeText(requireContext(), "Select subtopic first", Toast.LENGTH_SHORT).show()
            } else {
                showActivityPicker()
            }
        }

        binding.btnPickImages.setOnClickListener { imagePicker.launch("image/*") }
        binding.btnPost.setOnClickListener { submitPost() }
    }

    // ---------------- GROUP ----------------
    private fun showGroupPicker() {
        val groups = groupViewModel.groups.value ?: return
        val names = groups.map { it.name }.toTypedArray()

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Group")
            .setItems(names) { _, i ->
                selectedGroup = groups[i]
                binding.btnSelectGroup.text = groups[i].name
            }
            .show()
    }

    // ---------------- STUDENTS ----------------
    private fun showStudentPicker() {
        val students = studentViewModel.students.value ?: return
        val names = students.map { it.extraData.fullName }.toTypedArray()
        val checked = BooleanArray(students.size)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Tag Students")
            .setMultiChoiceItems(names, checked) { _, i, isChecked ->
                val student = students[i]
                if (isChecked) selectedStudents.add(student)
                else selectedStudents.removeAll { it.studentId == student.studentId }
            }
            .setPositiveButton("Done") { _, _ ->
                binding.btnTagStudents.text =
                    if (selectedStudents.isEmpty()) "Tag Students"
                    else "${selectedStudents.size} selected"
            }
            .show()
    }

    // ---------------- CURRICULUM ----------------
    private fun showCurriculumPicker() {

        val list = curriculumViewModel.curriculums.value ?: return
        val names = list.map { it.mainTopic }.toTypedArray()

        var selectedIndex = -1 // ❌ no preselection for Add

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Curriculum")

            // ✅ RADIO BUTTON (no preselect)
            .setSingleChoiceItems(names, selectedIndex) { _, which ->
                selectedIndex = which
            }

            .setPositiveButton("OK") { _, _ ->

                if (selectedIndex == -1) {
                    Toast.makeText(requireContext(), "Please select curriculum", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val selected = list[selectedIndex]
                selectedCurriculumId = selected.id

                binding.btnSelectCurriculum.text = selected.mainTopic

                // ✅ RESET DEPENDENT FIELDS
                selectedSubTopic = null
                selectedActivities.clear()

                binding.btnSelectSubTopic.text = "Select Sub Topic"
                binding.btnSelectActivities.text = "Select Activities"

                // ✅ FETCH DETAILS
                curriculumViewModel.fetchCurriculumById(token, apiKey, selected.id)
            }

            .setNegativeButton("Cancel", null)
            .show()
    }

    // ---------------- SUBTOPIC ----------------
    private fun showSubTopicPicker() {

        val curriculum = curriculumDetail ?: return
        //val subTopics = curriculum.subTopics

        // ✅ FILTER OUT EMPTY ACTIVITIES SUBTOPICS
        val subTopics = curriculum.subTopics.filter { it.activities.isNotEmpty() }

        if (subTopics.isEmpty()) {
            Toast.makeText(requireContext(), "No valid subtopics available", Toast.LENGTH_SHORT).show()
            return
        }

        val names = subTopics.map { it.name }.toTypedArray()
        var selectedIndex = -1

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Subtopic")
            .setSingleChoiceItems(names, -1) { _, which ->
                selectedIndex = which
            }
            .setPositiveButton("Select") { _, _ ->

                if (selectedIndex == -1) {
                    Toast.makeText(requireContext(), "Select subtopic", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                selectedSubTopic = subTopics[selectedIndex]

                binding.btnSelectSubTopic.text = selectedSubTopic?.name

                // RESET ACTIVITIES
                selectedActivities.clear()
                binding.btnSelectActivities.text = "Select Activities"
            }
            .show()
    }

    // ---------------- ACTIVITIES ----------------
    private fun showActivityPicker() {

        val subTopic = selectedSubTopic ?: return
        val activities = subTopic.activities

        val names = activities.map { it.name }.toTypedArray()
        val checked = BooleanArray(activities.size)

        val temp = mutableListOf<ActivityDetail>()

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Activities")
            .setMultiChoiceItems(names, checked) { _, i, isChecked ->
                val item = activities[i]
                if (isChecked) temp.add(item)
                else temp.remove(item)
            }
            .setPositiveButton("Done") { _, _ ->

                selectedActivities.clear()
                selectedActivities.addAll(temp)

                binding.btnSelectActivities.text =
                    "${selectedActivities.size} selected"
            }
            .show()
    }

    // ---------------- SUBMIT ----------------
    private fun submitPost() {

        val description = binding.editDescription.text.toString().trim()

        if (description.isEmpty()) {
            Toast.makeText(requireContext(), "Description required", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedGroup == null) {
            Toast.makeText(requireContext(), "Select group", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ NEW VALIDATION
        if (selectedStudents.isEmpty()) {
            Toast.makeText(requireContext(), "Please tag at least one student", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedCurriculumId == null) {
            Toast.makeText(requireContext(), "Please select curriculum", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedSubTopic == null) {
            Toast.makeText(requireContext(), "Please select subtopic", Toast.LENGTH_SHORT).show()
            return
        }

        journalViewModel.createPost(
            context = requireContext(),
            token = token,
            apiKey = apiKey,
            description = description,
            groupId = selectedGroup!!.id,
            curriculumId = selectedCurriculumId,
            subTopicId = selectedSubTopic?.id,
            selectedActivitiesIds = selectedActivities.map { it.id },
            taggedStudentIds = selectedStudents.map { it.studentId },
            images = selectedImages,
            onError = { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        )
    }
}