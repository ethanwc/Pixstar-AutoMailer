package ethanwc.tcss450.uw.edu.autopixstar;

import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_TAKE_PHOTO = 1;
    private String mPhotoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        parent.findViewById(R.id.button_image).setOnClickListener(this::pickImage);
        parent.findViewById(R.id.button_photo).setOnClickListener(this::uploadImage);

        return super.onCreateView(parent, name, context, attrs);
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

            case R.id.action_favorite:
                changeEmail();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void changeEmail() {

    }

    private void sendPhoto() throws JSONException {

        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.base))
                .appendPath(getString(R.string.send))
                .build();

        String recipient = "ethanwcheatham@gmail.com";

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("destination", recipient);
        jsonObj.put("url", url);

        new SendPostAsyncTask.Builder(uri.toString(), jsonObj)
                .onPreExecute(this::onWaitFragmentInteractionShow)
                .onPostExecute(this::handleConnectionGetDetailOnPostExecute)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }


    private void handleConnectionGetDetailOnPostExecute(String s) {

    }


    public void onWaitFragmentInteractionShow() {

    }

    private void handleErrorsInTask(String s) {

    }

    private void uploadImage(View view) {

    }

    private void pickImage(View view) {

    }
}