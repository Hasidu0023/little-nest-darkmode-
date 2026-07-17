package com.littlenest.nursery.ui.journal

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentJournalAddBinding
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.ui.group.Group
import com.littlenest.nursery.ui.student.Student
import com.littlenest.nursery.ui.curriculumNew.*
import com.littlenest.nursery.viewmodel.group.GroupViewModel
import com.littlenest.nursery.viewmodel.student.StudentViewModel
import androidx.recyclerview.widget.LinearLayoutManager

class EditJournalFragment : BaseFragment(R.layout.fragment_journal_add) {

    private var _binding: FragmentJournalAddBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JournalViewModel by viewModels()
    private val groupViewModel: GroupViewModel by viewModels()
    private val studentViewModel: StudentViewModel by viewModels()
    private val curriculumViewModel: CurriculumNewViewModel by viewModels()

    private val args: EditJournalFragmentArgs by navArgs()

    // STATE
    private var selectedGroup: Group? = null
    private val selectedStudents = mutableListOf<Student>()

    private var curriculumId: Int? = null
    private var selectedSubTopic: SubTopicDetail? = null
    private val selectedActivities = mutableListOf<ActivityDetail>()



    private var curriculumDetail: CurriculumDetail? = null
    private var currentPost: JournalPost? = null

    private lateinit var token: String
    private lateinit var apiKey: String

    private lateinit var imageAdapter: SelectedImageEditAdapter

    private val existingImages = mutableListOf<String>() // server images
    private val newImages = mutableListOf<Uri>()         // picked images
    private val selectedImages = mutableListOf<Any>() // UI only (String + Uri)

    // IMAGE PICKER
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->

            if (selectedImages.size + uris.size > 5) {
                Toast.makeText(requireContext(), "Max 5 images allowed", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            newImages.addAll(uris)
            selectedImages.addAll(uris)
            imageAdapter.notifyDataSetChanged()

            binding.textImageCount.text = "${selectedImages.size} image(s)"
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentJournalAddBinding.bind(view)

        token = getToken() ?: ""
        apiKey = getApiKey()

        setupUI()
        observeData()

        groupViewModel.fetchGroups(token, apiKey)
        studentViewModel.fetchStudents(token, apiKey)
        curriculumViewModel.fetchCurriculums(token, apiKey, 1)

        viewModel.loadPostById(token, apiKey, args.postId)
    }

    // UI
    private fun setupUI() {

        binding.btnPost.text = "Update Post"

        binding.btnSelectGroup.setOnClickListener { showGroupPicker() }
        binding.btnTagStudents.setOnClickListener { showStudentPicker() }
        binding.btnSelectCurriculum.setOnClickListener { showCurriculumPicker() }

        binding.btnSelectSubTopic.setOnClickListener {
            showSubTopicPicker()
        }

        binding.btnSelectActivities.setOnClickListener {
            showActivityPicker()
        }

        // ✅ Adapter setup
        imageAdapter = SelectedImageEditAdapter(selectedImages) { position ->

            val item = selectedImages[position]

            when (item) {

                is String -> {
                    val baseUrl = uploadsBaseUrl()

                    // 🔥 CONVERT FULL URL → BACK TO PATH
                    val originalPath = item.removePrefix(baseUrl)

                    existingImages.remove(originalPath)
                }

                is Uri -> {
                    newImages.remove(item)
                }
            }

            selectedImages.removeAt(position)
            imageAdapter.notifyItemRemoved(position)
            imageAdapter.notifyItemRangeChanged(position, selectedImages.size)

            binding.textImageCount.text = "${selectedImages.size} image(s)"
        }

        binding.recyclerImages.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = imageAdapter
        }

        binding.btnPickImages.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.btnPost.setOnClickListener { updatePost() }
    }

    // OBSERVE
    private fun observeData() {

        viewModel.selectedPost.observe(viewLifecycleOwner) { post ->
            if (post == null) return@observe
            currentPost = post

            binding.editDescription.setText(post.description)

            curriculumId = post.curriculumId

            if (curriculumId != null) {
                curriculumViewModel.fetchCurriculumById(token, apiKey, curriculumId!!)
            }

            // 🔥 CHANGED: RESET EVERYTHING CLEANLY
            existingImages.clear()
            newImages.clear()
            selectedImages.clear()

            val uploadsBaseUrl = uploadsBaseUrl()
            post.images.forEach { path ->
                existingImages.add(path)
                //selectedImages.add(path) // String
                selectedImages.add(uploadsBaseUrl + path) // ✅ FULL URL
            }
            imageAdapter.notifyDataSetChanged()

            binding.textImageCount.text = "${selectedImages.size} image(s)"

            syncSelections()
        }

        groupViewModel.groups.observe(viewLifecycleOwner) {
            syncSelections()
        }

        studentViewModel.students.observe(viewLifecycleOwner) {
            syncSelections()
        }

        curriculumViewModel.curriculumDetail.observe(viewLifecycleOwner) { detail ->
            curriculumDetail = detail

            val post = currentPost
            if (post != null) {

                selectedSubTopic = detail.subTopics.find {
                    it.id == post.subTopicId
                }

                val allActivities = detail.subTopics.flatMap { it.activities }

                selectedActivities.clear()
                selectedActivities.addAll(
                    allActivities.filter { a ->
                        post.activities?.contains(a.id) == true
                    }
                )
            }

            syncSelections()
        }
    }

    // SYNC UI
    private fun syncSelections() {

        val post = currentPost ?: return

        // GROUP
        val groups = groupViewModel.groups.value
        selectedGroup = groups?.find { it.id == post.groupId }

        binding.btnSelectGroup.text =
            selectedGroup?.name ?: "Select Group"

        // STUDENTS
        val students = studentViewModel.students.value
        if (students != null) {

            selectedStudents.clear()
            selectedStudents.addAll(
                students.filter {
                    post.taggedStudents?.contains(it.studentId) == true
                }
            )

            binding.btnTagStudents.text =
                if (selectedStudents.isEmpty()) "Tag Students"
                else "${selectedStudents.size} selected"
        }

        // CURRICULUM UI
        binding.btnSelectCurriculum.text =
            curriculumDetail?.mainTopic ?: "Select Curriculum"

        binding.btnSelectSubTopic.text =
            selectedSubTopic?.name ?: "Select Sub Topic"

        binding.btnSelectActivities.text =
            if (selectedActivities.isEmpty()) "Select Activities"
            else "${selectedActivities.size} selected"
    }

    // PICKERS
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

    private fun showStudentPicker() {
        val students = studentViewModel.students.value ?: return
        val names = students.map { it.extraData.fullName }.toTypedArray()

        val checked = students.map {
            selectedStudents.any { s -> s.studentId == it.studentId }
        }.toBooleanArray()

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

    private fun showCurriculumPicker() {

        val list = curriculumViewModel.curriculums.value ?: return
        val names = list.map { it.mainTopic }.toTypedArray()

        // ✅ PRESELECT CURRENT VALUE
        var selectedIndex = list.indexOfFirst { it.id == curriculumId }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Curriculum")

            // ✅ RADIO WITH PRESELECT
            .setSingleChoiceItems(names, selectedIndex) { _, which ->
                selectedIndex = which
            }

            .setPositiveButton("Select") { _, _ ->

                if (selectedIndex == -1) {
                    Toast.makeText(requireContext(), "Please select curriculum", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val selected = list[selectedIndex]
                curriculumId = selected.id

                binding.btnSelectCurriculum.text = selected.mainTopic

                // fetch details
                curriculumViewModel.fetchCurriculumById(token, apiKey, selected.id)
            }

            .setNegativeButton("Cancel", null)
            .show()
    }

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

        var selectedIndex = subTopics.indexOfFirst {
            it.id == selectedSubTopic?.id
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Sub Topic")
            .setSingleChoiceItems(names, selectedIndex) { _, which ->
                selectedIndex = which
            }
            .setPositiveButton("Next") { _, _ ->
                if (selectedIndex == -1) return@setPositiveButton
                selectedSubTopic = subTopics[selectedIndex]
                showActivityPicker()
            }
            .show()
    }

    private fun showActivityPicker() {

        val subTopic = selectedSubTopic ?: return
        val activities = subTopic.activities

        val names = activities.map { it.name }.toTypedArray()

        val checked = activities.map {
            selectedActivities.any { a -> a.id == it.id }
        }.toBooleanArray()

        val tempSelected = selectedActivities.toMutableList()

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Activities")
            .setMultiChoiceItems(names, checked) { _, i, isChecked ->
                val activity = activities[i]
                if (isChecked) tempSelected.add(activity)
                else tempSelected.removeAll { it.id == activity.id }
            }
            .setPositiveButton("Done") { _, _ ->
                selectedActivities.clear()
                selectedActivities.addAll(tempSelected)

                binding.btnSelectActivities.text =
                    if (selectedActivities.isEmpty()) "Select Activities"
                    else "${selectedActivities.size} selected"
            }
            .show()
    }

    // UPDATE
    private fun updatePost() {

        val description = binding.editDescription.text.toString().trim()


        if (description.isEmpty()) {
            Toast.makeText(requireContext(), "Description required", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedGroup == null) {
            Toast.makeText(requireContext(), "Select group", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedStudents.isEmpty()) {
            Toast.makeText(requireContext(), "Please tag at least one student", Toast.LENGTH_SHORT).show()
            return
        }

        if (curriculumId == null) {
            Toast.makeText(requireContext(), "Please select curriculum", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedSubTopic == null) {
            Toast.makeText(requireContext(), "Please select subtopic", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.updatePost(
            context = requireContext(),
            postId = args.postId,
            token = token,
            apiKey = apiKey,
            description = description,
            groupId = selectedGroup?.id ?: currentPost?.groupId ?: 0,

            curriculumId = curriculumId,
            subTopicId = selectedSubTopic!!.id,
            selectedActivitiesIds = selectedActivities.map { it.id },

            taggedStudentIds = selectedStudents.map { it.studentId },

            newImages = newImages,            // ✅ NEW UPLOADED IMAGES
            existingImages = existingImages,  // ✅ OLD SERVER IMAGES

            onSuccess = {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Post updated", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            },
            onError = {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}