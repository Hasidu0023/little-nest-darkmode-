package com.littlenest.nursery.ui.settings

import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentNurseryUpdateBinding
import com.littlenest.nursery.ui.common.BaseFragment
import java.io.File
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody

class UpdateNurseryFragment : BaseFragment(R.layout.fragment_nursery_update) {

    private var _binding: FragmentNurseryUpdateBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private val viewModel: SettingsViewModel by viewModels()

    private lateinit var token: String
    private lateinit var apiKey: String
    private var nurseryId: Int = 0

    // Modern image picker
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                binding.ivNurseryImage.setImageURI(it)
            }
        }

    override fun setupUI(view: View) {
        _binding = FragmentNurseryUpdateBinding.bind(view)

        token = getToken() ?: ""
        apiKey = getApiKey().ifEmpty { "your-very-secret-key" }
        nurseryId = getNurseryId()

        setupLanguageSpinner()
        loadNurseryData()
        setupListeners()
        observeViewModel()
    }

    private fun setupLanguageSpinner() {
        val languages = listOf("English", "Sinhala", "Tamil")
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            languages
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spLanguage.adapter = spinnerAdapter
    }

    private fun loadNurseryData() {
        val baseUrl = getBaseUrl()

        viewModel.fetchNursery("Bearer $token", apiKey, nurseryId)
        viewModel.nursery.observe(viewLifecycleOwner) { nursery ->
            nursery?.let {
                binding.etNurseryName.setText(it.name)
                binding.etNurseryDescription.setText(it.description)
                binding.etNurseryEmail.setText(it.nursery_email)
                binding.etNurseryAddress.setText(it.address)

                // Set language in spinner
                val position =
                    (binding.spLanguage.adapter as ArrayAdapter<String>).getPosition(it.language)
                if (position >= 0) binding.spLanguage.setSelection(position)

                Glide.with(requireContext())
                    .load(baseUrl + it.image)
                    .placeholder(R.drawable.ic_app_logo_placeholder)
                    .error(R.drawable.ic_app_logo_placeholder)
                    .into(binding.ivNurseryImage)
            }
        }
    }

    private fun setupListeners() {

        // Image selector
        binding.btnSelectImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        // Save
        binding.btnSaveNursery.setOnClickListener {
            val name = binding.etNurseryName.text.toString().trim()
            val description = binding.etNurseryDescription.text.toString().trim()
            val email = binding.etNurseryEmail.text.toString().trim()
            val address = binding.etNurseryAddress.text.toString().trim()
            val language = binding.spLanguage.selectedItem.toString()

            if (name.isEmpty() || description.isEmpty() ||
                email.isEmpty() || address.isEmpty() || language.isEmpty()
            ) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

//            viewModel.updateNursery(
//                nurseryId = nurseryId,
//                name = name,
//                description = description,
//                email = email,
//                address = address,
//                language = language,
//                imageUri = selectedImageUri,
//                token = token,
//                apiKey = apiKey
//            )


            val request = UpdateNurseryRequest(
                name = name,
                description = description,
                nursery_email = email,
                address = address,
                language = language
            )

            if (selectedImageUri == null) {
                // ✅ NO IMAGE (JSON)
                viewModel.updateNursery(
                    nurseryId = nurseryId,
                    request = request,
                    token = token,
                    apiKey = apiKey
                )
            } else {
                // ✅ WITH IMAGE (MULTIPART)
                val imagePart = uriToMultipart(selectedImageUri!!)

                viewModel.updateNurseryWithImage(
                    nurseryId = nurseryId,
                    name = name,
                    description = description,
                    email = email,
                    address = address,
                    language = language,
                    imagePart = imagePart,
                    token = token,
                    apiKey = apiKey
                )
            }
        }
    }

    private fun observeViewModel() {
        viewModel.successMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Log.d("udpateerror", "$error")

                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun uriToMultipart(uri: Uri): MultipartBody.Part {
        val inputStream = requireContext().contentResolver.openInputStream(uri)!!
        val file = File(requireContext().cacheDir, "nursery_${System.currentTimeMillis()}.jpg")

        file.outputStream().use {
            inputStream.copyTo(it)
        }

        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())

        return MultipartBody.Part.createFormData("image", file.name, requestBody)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
