package com.bartvbl.shareasjpeg;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ConvertActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image")) {
                handleSendImage(intent); // Handle multiple images being sent
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            // toast("only send one image at a time!");
        }
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            Context context = getApplicationContext();
            FutureTarget<Bitmap> futureTarget =
                    Glide.with(context)
                            .asBitmap()
                            .load(imageUri)
                            .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);

            Glide.with(this)
            .asBitmap()
            .load(imageUri)
            .into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    // save bitmap to cache directory
                    try {
                        File cachePath = new File(getCacheDir(), "images");
                        if(cachePath.exists()) {
                            cachePath.delete();
                        }
                        cachePath.mkdirs();
                        File file = new File(cachePath, "image.jpeg");
                        FileOutputStream stream  = new FileOutputStream(file);
                        resource.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        stream.close();

                        Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID+".fileprovider", file);
                        if (contentUri != null) {
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                            shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                            startActivity(Intent.createChooser(shareIntent, "Share converted image"));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Glide.with(context).clear(futureTarget);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                }
            });
        }
    }
}
