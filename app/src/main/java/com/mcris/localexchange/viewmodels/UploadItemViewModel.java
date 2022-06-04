package com.mcris.localexchange.viewmodels;

import static com.mcris.localexchange.helpers.Utils.TAG;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.AndroidViewModel;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mcris.localexchange.BuildConfig;
import com.mcris.localexchange.models.GsonRequest;
import com.mcris.localexchange.models.entities.Item;
import com.mcris.localexchange.models.entities.Table;
import com.mcris.localexchange.services.AirtableApiService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class UploadItemViewModel extends AndroidViewModel {

    private Uri currentImageUri = null;
    private String currentImageFileName = null;

    public UploadItemViewModel(@NonNull Application application) {
        super(application);
    }

    public Uri getCurrentImageUri() {
        return currentImageUri;
    }

    public void setCurrentImageUri(Uri currentImageUri) {
        this.currentImageUri = currentImageUri;
    }

    public String getCurrentImageFileName() {
        return currentImageFileName;
    }

    public void setCurrentImageFileName(String currentImageFileName) {
        this.currentImageFileName = currentImageFileName;
    }

    private FirebaseUser getLoggedFirebaseUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public void uploadItemWithCurrentImage(Item item, Consumer<Item> afterUploadAction, Consumer<Exception> onFailure) {
        if (item != null) {
            if (getCurrentImageUri() != null) {
                ImageAndThumbnail imgthmb = new ImageAndThumbnail();
                Log.d(TAG, "Uploading images of item " + item.getName());
                imgthmb.upload(getCompressedImageByteArray(getCurrentImageUri()), true,
                        () -> imgthmb.upload(getThumbnailByteArray(getCurrentImageUri()), false,
                                () -> {
                                    String imgurl = imgthmb.imageUrl.toString();
                                    String thmburl = imgthmb.thumbnailUrl.toString();
                                    item.setPictureUrl(imgurl);
                                    item.setThumbnailUrl(thmburl);
                                    Log.d(TAG, "Uploading item " + item.getName());
                                    uploadItem(item, afterUploadAction);
                                },
                                onFailure),
                        onFailure);

            } else {
                uploadItem(item, afterUploadAction);
            }
        }
    }

    private void uploadItem(Item item, Consumer<Item> afterUploadAction) {
        RequestQueue queue = Volley.newRequestQueue(getApplication());
        GsonRequest<Table<Item>> itemUploadRequest = AirtableApiService.getInstance(getApplication())
                .addNewItem(item,
                        response -> {
                            Item downloaded = response.getRecords().get(0).getRow();
                            afterUploadAction.accept(downloaded);
                        },
                        error -> {
                            Toast.makeText(getApplication(), "Error uploading announce", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error uploading item: ", error);
                        });

        queue.add(itemUploadRequest);
    }

    private void uploadToFirebaseStorage(String filename, byte[] data,
                                         OnSuccessListener<UploadTask.TaskSnapshot> listener,
                                         OnFailureListener failureListener) {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference ref = storage.getReference(getLoggedFirebaseUser().getUid() + "/" + filename);
        ref.putBytes(data)
                .addOnSuccessListener(listener)
                .addOnFailureListener(failureListener);
    }

    public void setCurrentImageToTempFile() {
        setCurrentImageFileName(UploadItemViewModel.getImageFilenameWithoutExt());
        File storageDir = getApplication().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File image = File.createTempFile(getCurrentImageFileName() + "_", ".jpg", storageDir);
            setCurrentImageUri(FileProvider.getUriForFile(
                    getApplication(),
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    image));
            image.deleteOnExit();
        } catch (IOException e) {
            Log.e(TAG, "setCurrentImageToTempFile: ", e);
            Toast.makeText(getApplication(), "Error creating photo file", Toast.LENGTH_SHORT).show();
        }
    }

    public Bitmap getCompressedImage(Uri uri) {
        byte[] data = getCompressedImageByteArray(uri);
        if (data != null) {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return null;
    }

    public Bitmap getCurrentImageCompressed() {
        return getCompressedImage(currentImageUri);
    }

    private byte[] getThumbnailByteArray(Uri uri) {
        return getCompressedImageByteArray(uri, 256, 50, true);
    }

    private byte[] getCompressedImageByteArray(Uri uri) {
        return getCompressedImageByteArray(uri, 1280, 40, false);
    }

    private byte[] getCompressedImageByteArray(Uri uri, double maxHeight, int jpegQuality, boolean square) {
        try {
            Bitmap bmp = correctBitmapOrientation(uri);
            return getCompressedImageByteArray(bmp, maxHeight, jpegQuality, square);
        } catch (IOException e) {
            Log.e(TAG, "getCompressedImageByteArray: ", e);
            return null;
        }
    }

    private byte[] getCompressedImageByteArray(Bitmap source, double maxHeight, int jpegQuality, boolean square) {
        if (square) {
            if (source.getWidth() > source.getHeight()) {
                source = Bitmap.createBitmap(source,
                        source.getWidth() / 2 - source.getHeight() / 2,
                        0,
                        source.getHeight(),
                        source.getHeight());
            } else {
                source = Bitmap.createBitmap(source,
                        0,
                        source.getHeight() / 2 - source.getWidth() / 2,
                        source.getWidth(),
                        source.getWidth());
            }
        }
        if (source.getHeight() > maxHeight) {
            double scale = ((double) source.getHeight()) / maxHeight;
            double width = ((double) source.getWidth()) / scale;
            source = Bitmap.createScaledBitmap(source, (int) width, (int) maxHeight, true);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        source.compress(Bitmap.CompressFormat.JPEG, jpegQuality, baos);
        byte[] data = baos.toByteArray();
        Log.d(TAG, "img size KB: " + (data.length / 1024));
        return data;
    }

    private Bitmap correctBitmapOrientation(Uri imageUri) throws IOException {
        Bitmap source = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), imageUri);
        InputStream is = getApplication().getContentResolver().openInputStream(imageUri);
        ExifInterface ei = new ExifInterface(is);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Bitmap rotated;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                Log.d(TAG, "input image orientation 90 degrees");
                rotated = rotateImage(source, 90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                Log.d(TAG, "input image orientation 180 degrees");
                rotated = rotateImage(source, 180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                Log.d(TAG, "input image orientation 270 degrees");
                rotated = rotateImage(source, 270);
                break;
            case ExifInterface.ORIENTATION_NORMAL:
                Log.d(TAG, "input image orientation 0 degrees");
            default:
                rotated = source;
        }
        return rotated;
    }

    public static String getImageFilenameWithoutExt() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        boolean filter = ((int) angle) % 90 != 0;
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, filter);
    }

    private class ImageAndThumbnail {
        private Uri imageUrl = null;
        private Uri thumbnailUrl = null;

        private void upload(byte[] data, boolean isMainImage, Runnable onSuccess, Consumer<Exception> onFailure) {
            String imgName = getCurrentImageFileName();
            if (!isMainImage) {
                imgName += "_thumbnail";
            }
            imgName += ".jpg";
            uploadToFirebaseStorage(imgName,
                    data,
                    taskSnapshot -> taskSnapshot.getStorage()
                            .getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                if (isMainImage) {
                                    imageUrl = uri;
                                } else {
                                    thumbnailUrl = uri;
                                }
                                onSuccess.run();
                            })
                            .addOnFailureListener(e -> onFailure.accept(e)),
                    e -> onFailure.accept(e));
        }
    }
}
