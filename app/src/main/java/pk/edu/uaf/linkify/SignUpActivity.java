package pk.edu.uaf.linkify;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import pk.edu.uaf.linkify.ChatDB.ChatDataBase;
import pk.edu.uaf.linkify.Fragments.PermissionFragment;
import pk.edu.uaf.linkify.HomeActivity.MainActivity2;
import pk.edu.uaf.linkify.Modal.LinkifyUser;
import pk.edu.uaf.linkify.ServicesAndThreads.AppExecutor;
import pk.edu.uaf.linkify.Utils.PrefUtils;
import pk.edu.uaf.linkify.Utils.UtilsFunctions;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static pk.edu.uaf.linkify.Utils.AppConstant.MY_PERMISSIONS_REQUEST_READ_CONTACTS;
import static pk.edu.uaf.linkify.Utils.AppConstant.PICK_IMAGE_GALLERY;
import static pk.edu.uaf.linkify.Utils.AppConstant.REQUEST_CAMERA;
import static pk.edu.uaf.linkify.Utils.AppConstant.SIGNED_UP_STATUS;
import static pk.edu.uaf.linkify.Utils.AppConstant.USER_IMAGE_PATH;
import static pk.edu.uaf.linkify.Utils.AppConstant.USER_NAME;
import static pk.edu.uaf.linkify.Utils.AppConstant.USER_NUMBER;
import static pk.edu.uaf.linkify.Utils.AppConstant.USER_SERVICE_NAME;
import static pk.edu.uaf.linkify.Utils.UtilsFunctions.getName;
import static pk.edu.uaf.linkify.Utils.UtilsFunctions.getSurname;

public class SignUpActivity extends AppCompatActivity {

    @BindView(R.id.sign_btn)
    Button btnSignIn;
    @BindView(R.id.edit_name)
    TextInputEditText editName;
    @BindView(R.id.edit_bumber)
    TextInputEditText editNumber;
    @BindView(R.id.cam_icon)
    ImageView dpImage;
    @BindView(R.id.imagePerson)
    CircleImageView circleImageViewDP;

    @BindView(R.id.edit_last_name)
    TextInputEditText lastName;
    private String imgPathCam = null;
    Context context;
    private static final String IMAGE_DIRECTORY = "/Linkify/.Images";
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        ButterKnife.bind(this);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PERMISSION_GRANTED) {
            getSupportFragmentManager().beginTransaction().add(R.id.container_layout, new PermissionFragment(), "permission_fragment").addToBackStack(null).commit();
        }
        btnSignIn.setEnabled(true);

//        UtilsFunctions.requestPermissions(SignUpActivity.this);

        context = getApplicationContext();


        dpImage.setOnClickListener((View v) -> {
            if (UtilsFunctions.requestPermissions(this)) {
                selectImage();
            }
        });

        btnSignIn.setOnClickListener(v -> {
            boolean status = false;
            String name = editName.getText().toString().trim();
            if (name.length() < 2) {
                editName.setError("Please Enter Correct Name");
                status = true;
            } else
                editName.setError(null);
            String number = editNumber.getText().toString().trim();
            if (editNumber.length() < 10) {
                editNumber.setError("Please Enter valid phone number");
                status = true;
            } else editNumber.setError(null);

            String last_name = lastName.getText().toString().trim();
            if (last_name.length() < 2) {
                lastName.setError("Please Enter Correct Name");

                status = true;
            } else lastName.setError(null);
            if (!status) {
                String fullName = name + " " + last_name;
                PrefUtils.setStringPref(this, USER_NAME, fullName);
                PrefUtils.setStringPref(this, USER_NUMBER, number);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    PrefUtils.setStringPref(this, USER_SERVICE_NAME, fullName + "/" + number + "/" + Build.getSerial());
                }else
                    PrefUtils.setStringPref(this, USER_SERVICE_NAME, fullName + "/" + number + "/" + Build.SERIAL);
                PrefUtils.setBooleanPref(this, SIGNED_UP_STATUS, true);
                AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
                    ChatDataBase db = ChatDataBase.getInstance(this);
                    String avatar = getName(fullName).substring(0, 1) +
                            getSurname(fullName).substring(0, 1);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        db.myDao().insertUser(new LinkifyUser(Build.getSerial(), fullName, avatar, number));
                    }else {
                        db.myDao().insertUser(new LinkifyUser(Build.SERIAL, fullName, avatar, number));
                    }
                });

                setResult(RESULT_OK);

                Intent start = new Intent(this, MainActivity2.class);
                startActivity(start);
                this.finish();
            }

        });
    }

    // Select image from camera and gallery
    private void selectImage() {
//        try {
        final CharSequence[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Camera")) {
                    dialog.dismiss();
                    dispatchTakePictureIntent();
                } else if (options[item].equals("Gallery")) {
                    dialog.dismiss();
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, PICK_IMAGE_GALLERY);
                }
            }
        });
        builder.setNegativeButton("cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
//        } catch (Exception e) {
//            Toast.makeText(this, "Camera Permission error", Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == REQUEST_CAMERA) {

            Glide.with(getApplicationContext())
                    .load(imgPathCam)
                    .into(circleImageViewDP);


            Bitmap bitmap = BitmapFactory.decodeFile(imgPathCam);

            ByteArrayOutputStream imgBytes = new ByteArrayOutputStream();
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imgBytes);

                imgPathCam = saveImage(imgBytes);

                PrefUtils.setStringPref(this, USER_IMAGE_PATH, imgPathCam);

            }
        } else if (requestCode == PICK_IMAGE_GALLERY && data != null) {
            Uri selectedImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
                Log.e("Activity", "Pick from Gallery::>>> ");

                String imgPath = getRealPathFromURI(selectedImage);

                ByteArrayOutputStream imgBytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imgBytes);

                imgPath = saveImage(imgBytes);

                Glide.with(this).load(imgPath).into(circleImageViewDP);


                PrefUtils.setStringPref(this, USER_IMAGE_PATH, imgPath);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();


            } catch (IOException ex) {
                Toast.makeText(context, "Error occurred while creating the File", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "pk.edu.uaf.linkify.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name

        String imageFileName = "JPEG_" + timeStamp + "_";
        File dir = new File(getFilesDir(), "");
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        imgPathCam = image.getAbsolutePath();


        return image;
    }

    public String saveImage(ByteArrayOutputStream bytes) {

        File linkifyPictureStorageDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.
        if (!linkifyPictureStorageDirectory.exists()) {
            linkifyPictureStorageDirectory.mkdirs();
        }
        try {
            File imageFile = new File(linkifyPictureStorageDirectory, timeStamp + ".jpg");
            imageFile.createNewFile();
            FileOutputStream fo = new FileOutputStream(imageFile);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{imageFile.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::--->" + imageFile.getAbsolutePath());

            return imageFile.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS) {
            for (int grantResult : grantResults) {
                if (grantResult != PERMISSION_GRANTED) {
                    return;
                }
            }
            Fragment f = getSupportFragmentManager().findFragmentByTag("permission_fragment");
            if (f != null)
                getSupportFragmentManager().popBackStack();
        }
    }
}
