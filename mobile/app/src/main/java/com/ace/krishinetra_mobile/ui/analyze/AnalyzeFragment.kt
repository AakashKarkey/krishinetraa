package com.ace.krishinetra_mobile.ui.analyze

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ace.krishinetra_mobile.R
import com.ace.krishinetra_mobile.databinding.FragmentAnalyzeBinding
import com.ace.krishinetra_mobile.utils.Constants
import com.ace.krishinetra_mobile.utils.ToastType
import com.ace.krishinetra_mobile.utils.Toaster
import com.ace.krishinetra_mobile.viewmodel.AnalyzeUiState
import com.ace.krishinetra_mobile.viewmodel.AnalyzeViewModel
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.io.File
import java.text.DecimalFormat

class AnalyzeFragment : Fragment() {
    private var _binding: FragmentAnalyzeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AnalyzeViewModel by viewModels()

    private var cameraImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.setImage(it)
            loadPreview(it)
        } ?: Toaster.show(binding.root, "No image selected", ToastType.INFO)
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri?.let { uri ->
                viewModel.setImage(uri)
                loadPreview(uri)
            }
        }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openCamera()
        else Toaster.show(binding.root, "Camera permission required", ToastType.ERROR)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyzeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGallery.setOnClickListener { openGallery() }
        binding.btnCamera.setOnClickListener { checkCameraPermission() }
        binding.btnAnalyze.setOnClickListener { viewModel.analyze() }
        binding.uploadArea.setOnClickListener { openGallery() }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.observe(viewLifecycleOwner) { state ->
                    updateUi(state)
                }
            }
        }
    }

    private fun loadPreview(uri: Uri) {
        binding.uploadPlaceholder.visibility = View.GONE
        binding.imagePreview.visibility = View.VISIBLE
        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(binding.imagePreview)
        binding.btnAnalyze.isEnabled = true
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> openCamera()
            else -> requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val photoFile = File.createTempFile(
            "camera_${System.currentTimeMillis()}",
            ".jpg",
            requireContext().cacheDir
        )
        cameraImageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        takePictureLauncher.launch(cameraImageUri)
    }

    private fun updateUi(state: AnalyzeUiState) {
        binding.btnAnalyze.isEnabled = state.selectedUri != null && !state.isUploading
        binding.btnAnalyze.text = if (state.isUploading) getString(R.string.btn_analyzing) else getString(R.string.btn_analyze)

        if (state.isUploading) {
            binding.progressSection.visibility = View.VISIBLE
            binding.uploadProgress.progress = state.uploadProgress
            binding.progressText.text = "${state.uploadProgress}%"
        } else {
            binding.progressSection.visibility = View.GONE
        }

        state.error?.let { error ->
            binding.errorCard.visibility = View.VISIBLE
            binding.errorText.text = error
            binding.resultCard.visibility = View.GONE
            Toaster.show(binding.root, error, ToastType.ERROR)
        }

        state.result?.let { result ->
            binding.errorCard.visibility = View.GONE
            binding.resultCard.visibility = View.VISIBLE
            displayResult(result)
            Toaster.show(binding.root, "Analysis complete!", ToastType.SUCCESS)
        }
    }

    private fun displayResult(result: com.ace.krishinetra_mobile.data.model.PredictionResponse) {
        val diseaseName = result.diseaseName
        binding.diseaseName.text = diseaseName

        val isUnrecognizable = diseaseName == "Unrecognizable"

        val confidencePercent = result.confidencePercent
        val badge = binding.confidenceBadge
        badge.text = "${confidencePercent}%"
        when {
            confidencePercent >= 90 -> badge.setBackgroundResource(R.drawable.bg_confidence_badge_green)
            confidencePercent >= 70 -> badge.setBackgroundResource(R.drawable.bg_confidence_badge_yellow)
            else -> badge.setBackgroundResource(R.drawable.bg_confidence_badge_red)
        }

        result.model?.let { model ->
            binding.modelName.text = "Model: $model"
            binding.modelName.visibility = View.VISIBLE
        }

        result.processingTimeS?.let { time ->
            val df = DecimalFormat("#.##")
            binding.processingTime.text = "${df.format(time)}s"
        }

        result.probabilities?.let { probs ->
            binding.probabilitiesContainer.removeAllViews()
            for ((label, prob) in probs.entries.sortedByDescending { it.value }) {
                val percent = if (prob <= 1.0) prob * 100 else prob
                val row = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = 8 }
                }

                val labelRow = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                labelRow.addView(TextView(requireContext()).apply {
                    text = label
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                    textSize = 13f
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                })

                labelRow.addView(TextView(requireContext()).apply {
                    text = "${DecimalFormat("#.#").format(percent)}%"
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
                    textSize = 12f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                })

                row.addView(labelRow)

                val progressContainer = android.widget.FrameLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 8
                    ).apply { topMargin = 4 }
                    setBackgroundResource(R.drawable.bg_probability_bar)
                }

                val fill = View(requireContext()).apply {
                    setBackgroundResource(R.drawable.bg_probability_fill)
                }
                progressContainer.addView(fill)
                fill.post {
                    val width = (progressContainer.width * (percent / 100.0).toFloat()).toInt()
                    fill.layoutParams = android.widget.FrameLayout.LayoutParams(
                        width, 8
                    )
                }

                row.addView(progressContainer)
                binding.probabilitiesContainer.addView(row)
            }
        }

        result.message?.let { msg ->
            binding.diseaseDescription.text = msg
            binding.treatmentText.text = ""
            binding.preventionContainer.removeAllViews()
            binding.treatmentSection.visibility = View.GONE
            binding.preventionSection.visibility = View.GONE
            binding.probabilitiesSection.visibility = View.GONE
            binding.dividerAfterProb.visibility = View.GONE
            binding.dividerAfterDesc.visibility = View.GONE
            binding.dividerAfterTreatment.visibility = View.GONE
            return
        }

        val diseaseInfo = Constants.Disease.DISEASE_INFO[diseaseName]
        if (diseaseInfo != null) {
            binding.diseaseDescription.text = diseaseInfo.description
            binding.treatmentText.text = diseaseInfo.treatment

            binding.preventionContainer.removeAllViews()
            for (tip in diseaseInfo.preventionTips) {
                val tipView = TextView(requireContext()).apply {
                    text = "\u2022 $tip"
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                    textSize = 13f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = 6 }
                }
                binding.preventionContainer.addView(tipView)
            }
        } else {
            result.description?.let { binding.diseaseDescription.text = it }
            result.treatment?.let { binding.treatmentText.text = it }
            result.preventionTips?.let { tips ->
                binding.preventionContainer.removeAllViews()
                for (tip in tips) {
                    binding.preventionContainer.addView(TextView(requireContext()).apply {
                        text = "\u2022 $tip"
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                        textSize = 13f
                    })
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}