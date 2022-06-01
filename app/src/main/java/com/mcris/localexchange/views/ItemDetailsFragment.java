package com.mcris.localexchange.views;

import static com.mcris.localexchange.helpers.Utils.getFriendlyDate;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.mcris.localexchange.databinding.FragmentItemDetailsBinding;
import com.mcris.localexchange.viewmodels.MainViewModel;

import java.io.IOException;
import java.util.Locale;

public class ItemDetailsFragment extends Fragment {

    private FragmentItemDetailsBinding binding;
    private MainViewModel mainViewModel;
    private Geocoder geocoder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        geocoder = new Geocoder(getActivity(), Locale.getDefault());
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentItemDetailsBinding.inflate(inflater, container, false);
        binding.itemDetailsScrollView.setVisibility(View.GONE);
        mainViewModel.downloadSelectedItem(item ->
                getActivity().runOnUiThread(() -> {
                    if (item != null) {
                        try {
                            Address address = geocoder.getFromLocation(item.getLatitude(), item.getLongitude(), 1).get(0);
                            binding.locationTextView.setText(address.getLocality());
                        } catch (IOException | IndexOutOfBoundsException e) {
                            binding.locationTextView.setText("");
                            e.printStackTrace();
                        }
                        if (item.getPicture() != null) {
                            binding.itemImageView.setImageBitmap(item.getPicture());
                        }
                        binding.itemDateTextView.setText(getFriendlyDate(item.getDate()));
                        binding.itemNameTextView.setText(item.getName());
                        binding.itemPriceTextView.setText(String.format(Locale.getDefault(),
                                "%.2f €", item.getPrice()));
                        binding.itemTypeTextView.setText(item.getTypology().toString());
                        TypedValue typedValue = new TypedValue();
                        switch (item.getTypology()) {
                            case SELL:
                                getActivity().getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true);
                                break;
                            case BUY:
                                getActivity().getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
                                break;
                            default:
                                break;
                        }
                        binding.itemPriceTextView.setTextColor(AppCompatResources.getColorStateList(getActivity(), typedValue.resourceId));
                        binding.itemTypeTextView.setBackgroundTintList(AppCompatResources.getColorStateList(getActivity(), typedValue.resourceId));
                        binding.itemDescriptionTextView.setText(item.getDescription());
                        binding.ownerButton.setText(item.getOwnerName());
                        binding.itemCategoryTextView.setText(mainViewModel.getCategoryFromId(item.getCategoryId()).getName());
                        binding.getDirectionsButton.setOnClickListener(v -> {
                            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + item.getLatitude() + "," + item.getLongitude() + "(" + item.getName() + ")");
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                startActivity(mapIntent);
                            } else {
                                Toast.makeText(getActivity(), "Installa l'app Google Maps per visualizzare l'oggetto", Toast.LENGTH_LONG).show();
                            }
                        });
                        binding.ownerButton.setOnClickListener(v -> {
                            if (getActivity() != null && getActivity() instanceof MainActivity) {
                                MainActivity mainActivity = (MainActivity) getActivity();
                                mainActivity.navigateToFragment(UserInfoFragment.class);
                                mainActivity.expandBottomSheet();
                            }
                        });
                        binding.loadingIndicator.setVisibility(View.GONE);
                        binding.itemDetailsScrollView.setVisibility(View.VISIBLE);
                    }
                }));
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}