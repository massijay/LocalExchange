package com.mcris.localexchange.views;

import static com.mcris.localexchange.helpers.Utils.getFriendlyDate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.mcris.localexchange.databinding.FragmentUserInfoBinding;
import com.mcris.localexchange.viewmodels.MainViewModel;

public class UserInfoFragment extends Fragment {

    private FragmentUserInfoBinding binding;
    private MainViewModel mainViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserInfoBinding.inflate(inflater, container, false);
        binding.userInfoScrollView.setVisibility(View.GONE);
        mainViewModel.downloadUser(user ->
                getActivity().runOnUiThread(() -> {
                    if (user != null) {
                        binding.contactNameTextView.setText(user.getName());

                        binding.emailTextView.setText(user.getEmailAddress());
                        binding.smsTextView.setText("SMS: " + user.getPhoneNumber());
                        binding.callTextView.setText("Chiama: " + user.getPhoneNumber());
                        binding.signUpDateTextView.setText(getFriendlyDate(user.getDate()));

                        binding.sendMailButton.setOnClickListener(v -> {
                            Intent intent = new Intent(Intent.ACTION_SENDTO);
                            intent.setData(Uri.parse("mailto:"));
                            intent.putExtra(Intent.EXTRA_EMAIL, user.getEmailAddress());
                            intent.putExtra(Intent.EXTRA_SUBJECT, "Informazioni su " + mainViewModel.getSelectedItem().getName());
                            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                                startActivity(intent);
                            }
                        });
                        binding.sendSmsButton.setOnClickListener(v -> {
                            Intent intent = new Intent(Intent.ACTION_SENDTO);
                            intent.setData(Uri.parse("smsto:" + user.getPhoneNumber()));
                            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                                startActivity(intent);
                            }
                        });
                        binding.callButton.setOnClickListener(v -> {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + user.getPhoneNumber()));
                            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                                startActivity(intent);
                            }
                        });
                        binding.addContactButton.setOnClickListener(v -> {
                            Intent intent = new Intent(Intent.ACTION_INSERT);
                            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                            intent.putExtra(ContactsContract.Intents.Insert.NAME, user.getName());
                            intent.putExtra(ContactsContract.Intents.Insert.PHONE, user.getPhoneNumber());
                            intent.putExtra(ContactsContract.Intents.Insert.EMAIL, user.getEmailAddress());
                            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                                startActivity(intent);
                            }
                        });

                        binding.loadingIndicator.setVisibility(View.GONE);
                        binding.userInfoScrollView.setVisibility(View.VISIBLE);
                    }
                }));
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}