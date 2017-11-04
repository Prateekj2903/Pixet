package com.example.windo.pixet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ConvTextActivity extends AppCompatActivity {

    TextView text;
    Button tryAgainBt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conv_text);

        text = (TextView) findViewById(R.id.textView);
        tryAgainBt = (Button) findViewById(R.id.tryagainbutton);

        Intent intent = getIntent();
        String ans = intent.getStringExtra("Result");
        int len = intent.getIntExtra("len", -1);
        Log.i("RESULT", "onResponse: " + ans);

        if(len == 0){
            text.setText("No Text found in Image");
            text.setTextSize(25);
        }
        else {
            ans = ans.substring(0, ans.length()-1);
            text.setText(ans);
        }


        tryAgainBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConvTextActivity.this, MainActivity.class);
                startActivity(intent);
            }});
    }
}
