package io.sharif.pavilion.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.sharif.pavilion.R;

public class SendProblemActivity extends AppCompatActivity {
    TextView subjectTv,modelTv,apiTv;
    EditText problemEtv;
    Button sendButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_problem);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ارسال مشکل");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        subjectTv= (TextView) findViewById(R.id.subject);
        modelTv= (TextView) findViewById(R.id.model);
        apiTv= (TextView) findViewById(R.id.androidapi);
        problemEtv= (EditText) findViewById(R.id.problem_etv);
        sendButton= (Button) findViewById(R.id.send_with_email);
        //set model and api
        apiTv.setText("ورژن اندروید: "+ Build.VERSION.SDK_INT);
        modelTv.setText("مدل دستگاه: "+getDeviceName());

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "ایمیل ارسال شد", Toast.LENGTH_SHORT).show();
                //send email with intent
                String subject="مشکل در برنامه "+ getResources().getString(R.string.app_name);
                String body=problemEtv.getText().toString()+"\n"
                        +modelTv.getText().toString()+"\n"
                        +apiTv.getText().toString()+"\n"
                        +getAppVersion();
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", getResources().getString(R.string.email), null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                emailIntent.putExtra(Intent.EXTRA_TEXT, body);

                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"baran.androidteam@gmail.com"});
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });

    }
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        String phrase = "";
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }
        return phrase;
    }

    private  String getAppVersion(){
        PackageManager manager =getPackageManager();
        PackageInfo info = null;
        String version="unknown";
        try {
            info = manager.getPackageInfo( getPackageName(), 0);
            version =""+( info.versionCode)+"";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return "نسخه برنامه: "+version;
    }

}
