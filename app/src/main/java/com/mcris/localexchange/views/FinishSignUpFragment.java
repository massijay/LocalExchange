package com.mcris.localexchange.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mcris.localexchange.databinding.FragmentFinishSignUpBinding;
import com.mcris.localexchange.viewmodels.MainViewModel;

public class FinishSignUpFragment extends Fragment {

    private FragmentFinishSignUpBinding binding;
    private MainViewModel mainViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFinishSignUpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.finishSignUpButton.setOnClickListener(
                v -> mainViewModel.registerLoggedUserInDatabase(binding.phoneNumberEditText.getText().toString(),
                        user -> {
                            MainActivity mainActivity = (MainActivity) getActivity();
                            if (mainActivity != null) {
                                mainActivity.setSheetBehaviorState(BottomSheetBehavior.STATE_COLLAPSED);
                                mainActivity.onBackPressed();
                            }
                        }));
    }
}