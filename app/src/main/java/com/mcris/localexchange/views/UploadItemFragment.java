package com.mcris.localexchange.views;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.UploadTask;
import com.mcris.localexchange.BuildConfig;
import com.mcris.localexchange.databinding.FragmentUploadItemBinding;
import com.mcris.localexchange.models.entities.Category;
import com.mcris.localexchange.models.entities.Item;
import com.mcris.localexchange.viewmodels.MainViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UploadItemFragment extends Fragment {

    private FragmentUploadItemBinding binding;
    private MainViewModel mainViewModel;

    private Uri currentImageUri = null;

    private final ActivityResultLauncher<Uri> takePictureResult =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), isSuccess -> {
                if (isSuccess) {
                    binding.photoImageView.setImageBitmap(getCompressedImage(currentImageUri));
                }
            });

    private final ActivityResultLauncher<String> selectImageFromGalleryResult =
            registerForActivityResult(new ActivityResultContracts.GetContent(), imgUri -> {
                if (imgUri != null) {
                    currentImageUri = imgUri;
                    binding.photoImageView.setImageBitmap(getCompressedImage(imgUri));
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
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
            String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String imageFileName = "JPEG_" + timeStamp + "_";
            @SuppressWarnings("ConstantConditions")
            File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try {
                File image = File.createTempFile(imageFileName, ".jpg", storageDir);
                currentImageUri = FileProvider.getUriForFile(
                        getActivity(),
                        BuildConfig.APPLICATION_ID + ".fileprovider",
                        image);
                takePictureResult.launch(currentImageUri);
                image.deleteOnExit();
            } catch (IOException e) {
                Log.e("SHT", "takePictureResult.launch: ", e);
            }
        });

        binding.galleryButton.setOnClickListener(v -> {
            selectImageFromGalleryResult.launch("image/*");
        });

        binding.uploadItemButton.setOnClickListener(v -> {
            Item item = createItem();
            if (item != null) {
                if (currentImageUri != null) {
                    String fname = currentImageUri.getLastPathSegment();
                    Log.i("SHT", "getLastPathSegment: " + fname);
                    mainViewModel.uploadToFirebaseStorage(fname,
                            getCompressedImageByteArray(currentImageUri),
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    String p = taskSnapshot.getMetadata().getPath();
                                }
                            },
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //TODO: PROBLEMI DI PERMESSI
                                    Log.e("SHT", "onFailure: ", e);
                                }
                            });
                } else {
                    mainViewModel.uploadItem(item, i -> {
                        Log.i("SHT", "uploadItem: " + i.getName());
                    });
                }
            }
        });

        return binding.getRoot();
    }

    private Item createItem() {
        try {
            Item item = new Item();
            item.setName(binding.nameEditText.getText().toString());
            item.setPrice(Double.parseDouble(binding.priceEditText.getText().toString()));
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
            //noinspection ConstantConditions
            item.setLatLng(mainViewModel.getUserLocation().getValue());
            return item;
        } catch (NumberFormatException e) {
            binding.priceEditText.setText("");
            Toast.makeText(getActivity(), "Formato prezzo non corretto", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private Bitmap getCompressedImage(Uri uri) {
        byte[] data = getCompressedImageByteArray(uri);
        if (data != null) {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return null;
    }

    private Bitmap getThumbnail(Uri uri) {
        byte[] data = getThumbnailByteArray(uri);
        if (data != null) {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return null;
    }

    private byte[] getCompressedImageByteArray(Uri uri) {
        return getCompressedImageByteArray(uri, 1280, 40, false);
    }

    private byte[] getThumbnailByteArray(Uri uri) {
        return getCompressedImageByteArray(uri, 256, 50, true);
    }

    private byte[] getCompressedImageByteArray(Uri uri, double maxHeight, int jpegQuality, boolean square) {
        try {
            @SuppressWarnings("ConstantConditions")
            Bitmap bmp = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
            if (square) {
                if (bmp.getWidth() > bmp.getHeight()) {
                    bmp = Bitmap.createBitmap(bmp,
                            bmp.getWidth() / 2 - bmp.getHeight() / 2,
                            0,
                            bmp.getHeight(),
                            bmp.getHeight());
                } else {
                    bmp = Bitmap.createBitmap(bmp,
                            0,
                            bmp.getHeight() / 2 - bmp.getWidth() / 2,
                            bmp.getWidth(),
                            bmp.getWidth());
                }
            }
            if (bmp.getHeight() > maxHeight) {
                double scale = ((double) bmp.getHeight()) / maxHeight;
                double width = ((double) bmp.getWidth()) / scale;
                bmp = Bitmap.createScaledBitmap(bmp, (int) width, (int) maxHeight, true);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, jpegQuality, baos);
            byte[] data = baos.toByteArray();
            Log.i("SHT", "img size KB: " + (data.length / 1024));
            return data;
        } catch (IOException e) {
            return null;
        }
    }
}