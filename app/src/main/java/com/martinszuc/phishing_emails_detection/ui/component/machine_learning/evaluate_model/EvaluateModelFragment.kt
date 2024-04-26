package com.martinszuc.phishing_emails_detection.ui.component.machine_learning.evaluate_model

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.martinszuc.phishing_emails_detection.databinding.FragmentMlEvaluationBinding
import com.martinszuc.phishing_emails_detection.ui.component.model_manager.ModelManagerViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONException
import org.json.JSONObject

private const val logTag = "EvaluateModelFragment"

/**
 * Authored by matoszuc@gmail.com
 */

@AndroidEntryPoint
class EvaluateModelFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentMlEvaluationBinding? = null
    private val binding get() = _binding!!

    private val modelManagerViewModel: ModelManagerViewModel by activityViewModels()
    private val evaluateModelViewModel: EvaluateModelViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMlEvaluationBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeModelSelection()
        initLoadingFrame()
        observeEvaluationResults()
    }

    private fun observeEvaluationResults() {
        evaluateModelViewModel.evaluationResults.observe(viewLifecycleOwner) { results ->
            updateEvaluationResultsUI(results)
        }
    }

    private fun updateEvaluationResultsUI(results: String) {
        try {
            val jsonResult = JSONObject(results)
            binding.confMatrix.visibility = View.VISIBLE

            // Update basic metrics TextViews with four decimal formatting
            binding.accuracy.text = String.format("%.4f", jsonResult.getDouble("accuracy"))
            binding.precision.text = String.format("%.4f", jsonResult.getDouble("precision"))
            binding.recall.text = String.format("%.4f", jsonResult.getDouble("recall"))
            binding.f1Score.text = String.format("%.4f", jsonResult.getDouble("f1-score"))
            binding.rocAuc.text = String.format("%.4f", jsonResult.getDouble("roc_auc"))

            // Confusion matrix handling
            if (jsonResult.has("confusion_matrix")) {
                val confusionMatrix = jsonResult.getJSONArray("confusion_matrix")
                if (confusionMatrix.length() == 2) {
                    // Update confusion matrix TextViews
                    binding.matrixTruePositive.text = confusionMatrix.getJSONArray(0).getInt(0).toString()
                    binding.matrixFalseNegative.text = confusionMatrix.getJSONArray(0).getInt(1).toString()
                    binding.matrixFalsePositive.text = confusionMatrix.getJSONArray(1).getInt(0).toString()
                    binding.matrixTrueNegative.text = confusionMatrix.getJSONArray(1).getInt(1).toString()
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            showToast("Failed to parse evaluation results: ${e.message}")
        }
    }


    private fun formatConfusionMatrix(matrix: Array<Array<Int>>): String {
        val builder = StringBuilder()
        matrix.forEach { row ->
            row.forEach { cell ->
                builder.append(cell.toString().padEnd(8))
            }
            builder.append("\n")
        }
        return builder.toString()
    }


    private fun observeModelSelection() {
            modelManagerViewModel.selectedModel.observe(viewLifecycleOwner) { model ->
                if (model != null) {
                    evaluateModelViewModel.initiateModelEvaluation(model)
                } else {
                    showToast("No model selected.")
                }
            }
        }

        private fun initLoadingFrame() {
            evaluateModelViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    binding.loadingSpinner.visibility = View.VISIBLE
                } else {
                    binding.loadingSpinner.visibility = View.GONE
                }
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

        private fun showToast(message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }