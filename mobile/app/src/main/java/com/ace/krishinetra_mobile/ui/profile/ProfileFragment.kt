package com.ace.krishinetra_mobile.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ace.krishinetra_mobile.R
import com.ace.krishinetra_mobile.databinding.FragmentProfileBinding
import com.ace.krishinetra_mobile.utils.ToastType
import com.ace.krishinetra_mobile.utils.Toaster
import com.ace.krishinetra_mobile.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        binding.btnSignOut.setOnClickListener {
            viewModel.signOut()
            Toaster.show(binding.root, "Signed out successfully", ToastType.INFO)
        }

        binding.btnSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_signIn)
        }

        binding.btnSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_signUp)
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.observe(viewLifecycleOwner) { state ->
                    if (state.isLoggedIn) {
                        binding.profileName.text = state.userName
                        binding.profileEmail.text = state.userEmail
                        binding.profileName.visibility = View.VISIBLE
                        binding.profileEmail.visibility = View.VISIBLE
                        binding.btnSignOut.visibility = View.VISIBLE
                        binding.authSection.visibility = View.GONE
                        binding.statsAnalyses.text = "${state.analysisCount}"
                    } else {
                        binding.profileName.text = getString(R.string.profile_title)
                        binding.profileEmail.visibility = View.GONE
                        binding.btnSignOut.visibility = View.GONE
                        binding.authSection.visibility = View.VISIBLE
                        binding.statsAnalyses.text = "0"
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}