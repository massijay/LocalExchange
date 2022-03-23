package com.mcris.localexchange.models;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mcris.localexchange.R;
import com.mcris.localexchange.models.entities.Item;

import java.util.Locale;

public class ItemMarkerGenerator {
    private final Context context;

    public ItemMarkerGenerator(Context context) {
        this.context = context;
    }

    public Bitmap drawMarker(Item item) {
        @SuppressLint("InflateParams") // The maps library default IconGenerator call inflate() in this way
        View markerLayout = LayoutInflater.from(context).inflate(R.layout.custom_marker_layout, null);
        ImageView imageView = markerLayout.findViewById(R.id.markerImageView);
        TextView mainTextView = markerLayout.findViewById(R.id.mainTextView);
        TextView priceTextView = markerLayout.findViewById(R.id.priceTextView);

        mainTextView.setText(item.getName());
        priceTextView.setText(String.format(Locale.getDefault(), "%.0f€", item.getPrice()));

        if (item.getThumbnailBitmap() == null) {
            imageView.setVisibility(View.GONE);
        } else {
            imageView.setImageBitmap(item.getThumbnailBitmap());
            imageView.setContentDescription(item.getName());
        }
        return renderBitmapWithRoundedCorners(markerLayout);
    }

    private static Bitmap renderBitmapWithRoundedCorners(View view) {
        view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(
                view.getMeasuredWidth(),
                view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        float radius = 15f;
        Path clipPath = new Path();
        RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        clipPath.addRoundRect(rect, radius, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);

        view.draw(canvas);
        return bitmap;
    }
}
