package com.mcris.localexchange.views;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.mcris.localexchange.R;
import com.mcris.localexchange.databinding.FragmentUploadItemBinding;
import com.mcris.localexchange.helpers.Utils;
import com.mcris.localexchange.models.entities.Category;
import com.mcris.localexchange.models.entities.Item;
import com.mcris.localexchange.viewmodels.MainViewModel;
import com.mcris.localexchange.viewmodels.UploadItemViewModel;

public class UploadItemFragment extends Fragment {

    private static final boolean USE_FAKE_COORDINATES = false;

    private FragmentUploadItemBinding binding;
    private MainViewModel mainViewModel;
    private UploadItemViewModel uploadItemVM;

    private final ActivityResultLauncher<Uri> takePictureResult =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), isSuccess -> {
                if (isSuccess) {
                    binding.photoImageView.setImageBitmap(uploadItemVM.getCurrentImageCompressed());
                }
            });

    private final ActivityResultLauncher<String> selectImageFromGalleryResult =
            registerForActivityResult(new ActivityResultContracts.GetContent(), imgUri -> {
                if (imgUri != null) {
                    uploadItemVM.setCurrentImageUri(imgUri);
                    uploadItemVM.setCurrentImageFileName(UploadItemViewModel.getImageFilenameWithoutExt());
                    binding.photoImageView.setImageBitmap(uploadItemVM.getCompressedImage(imgUri));
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        uploadItemVM = new ViewModelProvider(this).get(UploadItemViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUploadItemBinding.inflate(inflater, container, false);

        ArrayAdapter<Category> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item,
                mainViewModel.getAllCategories());

        binding.categoriesSpinner.setAdapter(adapter);
        binding.categoriesSpinner.setSelection(0);

        binding.cameraButton.setOnClickListener(v -> {
            uploadItemVM.setCurrentImageToTempFile();
            takePictureResult.launch(uploadItemVM.getCurrentImageUri());
        });

        binding.galleryButton.setOnClickListener(v -> selectImageFromGalleryResult.launch("image/*"));

        binding.uploadItemButton.setOnClickListener(v -> {
            startLoadingIndicator();
            Item item = parseItem();
            uploadItemVM.uploadItemWithCurrentImage(item, uploadedItem -> {
                        stopLoadingIndicator();
                        MainActivity mainActivity = (MainActivity) getActivity();
                        if (mainActivity != null) {
                            mainActivity.selectSearchTypology(uploadedItem.getTypology());
                            mainActivity.focusItemOnMap(uploadedItem);
                            mainActivity.goBackToRootFragment();
                        }
                    },
                    this::handleUploadError);
        });

        return binding.getRoot();
    }

    private void startLoadingIndicator() {
        //noinspection ConstantConditions
        getActivity().runOnUiThread(() -> binding.uploadingIndicator.setVisibility(View.VISIBLE));
    }

    private void stopLoadingIndicator() {
        //noinspection ConstantConditions
        getActivity().runOnUiThread(() -> binding.uploadingIndicator.setVisibility(View.GONE));
    }

    private void handleUploadError(Exception e) {
        stopLoadingIndicator();
    }

    private Item parseItem() {
        try {
            Item item = new Item();
            item.setName(binding.nameEditText.getText().toString());
            item.setPrice(Double.parseDouble(binding.priceEditText.getText().toString().replace(',', '.')));
            int checkedButtonId = binding.typologyButtonGroup.getCheckedButtonId();
            if (checkedButtonId == binding.demandButton.getId()) {
                item.setTypology(Item.Typology.BUY);
            } else {
                item.setTypology(Item.Typology.SELL);
            }
            item.setDescription(binding.descriptionEditText.getText().toString());
            Category category = (Category) binding.categoriesSpinner.getSelectedItem();
            item.setCategory(category);
            item.setOwner(mainViewModel.getLoggedUser());
            if (USE_FAKE_COORDINATES) {
                LatLng ne = new LatLng(45.705438, 13.827449);
                LatLng sw = new LatLng(45.598643, 13.753635);
                LatLngBounds bounds = new LatLngBounds(sw, ne);
                item.setLatLng(Utils.getRandomLocationInsideBounds(bounds));
            } else {
                //noinspection ConstantConditions
                item.setLatLng(mainViewModel.getUserLocation().getValue());
            }
            return item;
        } catch (NumberFormatException e) {
            stopLoadingIndicator();
            binding.priceEditText.setText("");
            Toast.makeText(getActivity(), R.string.wrong_price_format, Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}