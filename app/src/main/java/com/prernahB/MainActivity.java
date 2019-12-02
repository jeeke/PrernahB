package com.prernahB;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ChildModel;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    ImageView mImage;
    Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        d = new ProgressDialog(this);
        login("gyanesh198@gmail.com","12345678");
        mImage = findViewById(R.id.image);
        findViewById(R.id.pick).setOnClickListener(v -> pickImage());
        findViewById(R.id.save).setOnClickListener(v -> uploadImage(mImage));
    }

    // Method 6
    public void login(String email, String password) {
        d.setTitle("Logging in.....");
        d.show();
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    d.dismiss();
                    Exception e = task.getException();
                    if (e != null) {
                        Toast.makeText(this, "Can't Login", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pickImage() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");
        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    public static final int PICK_IMAGE = 127;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mUri = data.getData();
            Glide.with(this).load(mUri).into(mImage);
        }
    }

    ProgressDialog d;

    //method 1
    private void uploadImage(ImageView image) {
        d.setTitle("Uploading data");
        d.show();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.setDrawingCacheEnabled(true);
            image.buildDrawingCache();
            BitmapDrawable drawable = (BitmapDrawable) image.getDrawable();

            drawable.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, baos);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            Random rand = new Random();
            int n = rand.nextInt(10);
            String path = "images/" + n + "/";
            n = rand.nextInt(10);
            path += n + "/";
            UploadTask uploadTask;
            path += new Date().getTime() + mUri.getLastPathSegment();
            StorageReference imageRef = storage.getReference().child(path);
            byte[] data = baos.toByteArray();
            uploadTask = imageRef.putBytes(data);
            uploadTask.addOnProgressListener(taskSnapshot -> {
            }).addOnPausedListener(taskSnapshot -> {
                uploadTask.cancel();
            }).addOnFailureListener(exception -> {
                uploadTask.cancel();
            }).addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri1 -> {
                postChild(uri1.toString());
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void postChild(String url) {
        EditText d = findViewById(R.id.desc);
        EditText n = findViewById(R.id.name);
        EditText c = findViewById(R.id.grade);
        DatabaseReference r = FirebaseDatabase.getInstance().getReference().child("Children").push();
        ChildModel feed = new ChildModel(
                r.getKey(),
                url,
                d.getText().toString(),
                n.getText().toString(),
                c.getText().toString()
        );
        r.setValue(feed).addOnCompleteListener(aVoid -> {
            this.d.dismiss();
            if (aVoid.isSuccessful())
                Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
        });
    }
}
