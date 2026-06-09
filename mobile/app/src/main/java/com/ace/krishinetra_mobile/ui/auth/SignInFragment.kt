package com.ace.krishinetra_mobile.ui.auth

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
import com.ace.krishinetra_mobile.databinding.FragmentSignInBinding
import com.ace.krishinetra_mobile.utils.ToastType
import com.ace.krishinetra_mobile.utils.Toaster
import com.ace.krishinetra_mobile.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class SignInFragment : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSignIn.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            authViewModel.signIn(email, password)
        }

        binding.btnGoSignUp.setOnClickListener {
            findNavController().navigateUp()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.uiState.observe(viewLifecycleOwner) { state ->
                    state.error?.let { error ->
                        Toaster.show(binding.root, error, ToastType.ERROR)
                        authViewModel.resetState()
                    }
                    if (state.isSuccess) {
                        Toaster.show(binding.root, "Signed in successfully!", ToastType.SUCCESS)
                        findNavController().navigateUp()
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