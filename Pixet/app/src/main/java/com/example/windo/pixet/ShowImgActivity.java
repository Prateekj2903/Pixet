package com.javahelps.prateekpixet;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import android.graphics.Bitmap;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ShowImgActivity extends AppCompatActivity {

    ImageView img;
    Button submitButton, againButton;
    Bitmap bitmap = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_img);

        img = (ImageView) findViewById(R.id.imageView);
        submitButton = (Button) findViewById(R.id.submitButton);
        againButton = (Button) findViewById(R.id.againButton);

//        ByteArrayInputStream bytes = new ByteArrayInputStream();
        Intent intent = getIntent();
        String currPath = intent.getStringExtra("path");

        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(currPath));
//            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
//            img.setImageDrawable(drawable);
            img.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 195, 240, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(mFile));
////            bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(mFile.toByteArray()));
////            bitmap = BitmapFactory.decodeFile(mFile.getAbsolutePath());
//            img.setImageBitmap(bitmap);
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inSampleSize =1;
//
//            final Bitmap b = BitmapFactory.decodeFile(mFile.getAbsolutePath(), options);
//
//            img.setImageBitmap(bitmap);

        Log.i("TAG", "decoded bitmap dimensions:" + bitmap.getWidth() + "x" + bitmap.getHeight());
//        }
//        catch (IOException e) {
//
//        }

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPic();
            }
        });

        againButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowImgActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

//        System.out.println(encodedImage.length());
        return encodedImage;
    }


    String url1 = "http://192.168.43.242:8079/image";//ip

    private void sendPic() {


        final ProgressDialog loading = ProgressDialog.show(this, "Uploading...", "Please wait...", false, false);
        Map<String, String> jsonParams = new HashMap<String, String>();
        String image = getStringImage(bitmap);

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

            Intent mintent = new Intent(ShowImgActivity.this, ConvTextActivity.class);
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
