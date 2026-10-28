package com.family.launcher;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Family Launcher B866F - MODDABLE\n\nUI kayak foto pink akan muncul di sini.\nBuild berhasil!");
        tv.setTextSize(24);
        tv.setPadding(50,50,50,50);
        setContentView(tv);
    }
          }
