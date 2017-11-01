package com.example.windo.pixet;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.master.permissionhelper.PermissionHelper;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    ImageView cameraBt;
    PermissionHelper mPermissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraBt = (ImageView) findViewById(R.id.btnCapture);
        cameraBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFromCamera();
            }});

    }

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap mImageBitmap;
    private String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }


    File photoFile = null;
    private void selectFromCamera() {
        mPermissionHelper = new PermissionHelper(MainActivity.this, new String[]
                {android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);


        mPermissionHelper.request(new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted()
            {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
//                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Log.i("TAG", "IOException");
                    }
                }
                if (photoFile != null) {
                    Log.i("TAG", "onPermissionGranted: not null");
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                }
            }

            @Override
            public void onPermissionDenied() {

            }

            @Override
            public void onPermissionDeniedBySystem() {

            }
        });

    }

    /*
    Camera Result parsed and saved
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

//        Log.i("TAG", "onActivityResult: " + " On activity entered");
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

//            Log.i("TAG", "onActivityResult: if entered");
//            if (data != null) {
                try {
                    mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mCurrentPhotoPath));
                    Log.i("TAG", "onActivityResult: " + mImageBitmap.getWidth() + " " + mImageBitmap.getHeight());
//                    Intent intent = new Intent(MainActivity.this, ShowImgActivity.class);
//                    intent.putExtra("path", mCurrentPhotoPath);
//                    startActivity(intent);
//                    mImageView.setImageBitmap(mImageBitmap);

                        sendPic();

                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }



    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }


    String url1 = "http://192.168.1.7:8079/image";//ip

    private void sendPic() {


        final ProgressDialog loading = ProgressDialog.show(this, "Uploading...", "Please wait...", false, false);
        Map<String, String> jsonParams = new HashMap<String, String>();
        String image = getStringImage(mImageBitmap);

        jsonParams.put("imageFile", image);
        JsonObjectRequest myRequest = new JsonObjectRequest(
                Request.Method.POST,
                url1,
                new JSONObject(jsonParams),

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                                verificationSuccess(response);
                        loading.dismiss();
//                                Toast.makeText(SpeachQuestionActivity.this, response + "", Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
//                                verificationFailed(error);
                        volleyError.printStackTrace();
                        loading.dismiss();

//                                Toast.makeText(SpeachQuestionActivity.this, "" + volleyError, Toast.LENGTH_LONG).show();
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
//                MyApplication.getInstance().addToRequestQueue(myRequest, "tag");
        myRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(myRequest);
    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String ans = intent.getExtras().getString("ans");

//            Toast.makeText(getApplicationContext(), ans, Toast.LENGTH_SHORT);

            Intent mintent = new Intent(MainActivity.this, ConvTextActivity.class);
            intent.putExtra("str", ans);
            startActivity(mintent);
            finish();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver),
                new IntentFilter("Answer")
        );
    }

    @Override
    protected void onStop() {

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onStop();
    }
}
