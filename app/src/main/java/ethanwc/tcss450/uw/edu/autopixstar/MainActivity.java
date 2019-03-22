package ethanwc.tcss450.uw.edu.autopixstar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Credentials;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.cloudinary.Transformation;
import com.cloudinary.Url;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int TAKEN_PHOTO_UPLOAD = 444;
    private static int SELECT_IMAGE = 10;

    private String mPhotoPath;
    private String mEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MediaManager.init(this);

        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        findViewById(R.id.button_image).setOnClickListener(this::uploadImage);
        findViewById(R.id.button_photo).setOnClickListener(this::takeImage);

        SharedPreferences prefs = getSharedPreferences(
                getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);

        if (prefs.contains(getString(R.string.keys_shared_prefs))) {
            String email = prefs.getString(getString(R.string.keys_shared_prefs), "");
            ((EditText) findViewById(R.id.text_email)).setText(email);
            mEmail = email;
        } else changeEmail();

    }


    /**
     * Save the credentials locally
     *
     * @param email
     */
    private void saveEmail(final String email) {
        SharedPreferences prefs =
                getSharedPreferences(getString(R.string.keys_shared_prefs), Context.MODE_PRIVATE);
        //Store the credentials in SharedPrefs
        prefs.edit().putString(getString(R.string.keys_shared_prefs), email).apply();
        ((EditText) findViewById(R.id.text_email)).setText(prefs.getString(getString(R.string.keys_shared_prefs), ""));

        mEmail = email;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menubuttons, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_editemail:
                changeEmail();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK) {

            Uri selectedImage = data.getData();
            uploadPhoto(selectedImage);

        } else if (requestCode == TAKEN_PHOTO_UPLOAD && resultCode == RESULT_OK) {

            galleryAddPic();

        }
    }

    private void changeEmail() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Destination E-Mail");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveEmail(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void sendPhoto(String url) throws JSONException {

        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.base))
                .appendPath(getString(R.string.send))
                .build();

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("url", url);
        jsonObj.put("destination", mEmail);

        new SendPostAsyncTask.Builder(uri.toString(), jsonObj)
                .onPreExecute(this::onWaitFragmentInteractionShow)
                .onPostExecute(this::handleConnectionGetDetailOnPostExecute)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }


    private void handleConnectionGetDetailOnPostExecute(String s) {
        Toast.makeText(MainActivity.this, "Send Completed", Toast.LENGTH_SHORT).show();

    }


    public void onWaitFragmentInteractionShow() {

    }

    private void handleErrorsInTask(String s) {

    }

    private void takeImage(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            if (photoFile != null) {

                Uri photoURI = FileProvider.getUriForFile(this,
                        "ethanwc.tcss450.uw.edu.autopixstar.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, TAKEN_PHOTO_UPLOAD);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        uploadPhoto(contentUri);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void pickImageFromGallery() {
        Intent GalleryIntent = new Intent();
        GalleryIntent.setType("image/*");
        GalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(GalleryIntent, "select image"), SELECT_IMAGE);
    }

    private void uploadImage(View view) {
        pickImageFromGallery();
    }

    private void sendEmail(String url) {
        try {
            sendPhoto(url);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void uploadPhoto(Uri uri) {
        MediaManager.get()
                .upload(uri)
                .option("resource_type", "image")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Toast.makeText(MainActivity.this, "Starting Send...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {

                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {

                        String url = resultData.get("url").toString();
                        sendEmail(url);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {

                        Toast.makeText(MainActivity.this, "Error Occured", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {

                    }

                }).dispatch();
    }
}