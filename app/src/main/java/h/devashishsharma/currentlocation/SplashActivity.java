package h.devashishsharma.currentlocation;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash);
//        ActionBar actionBar=getSupportActionBar();
//        actionBar.hide();
        Thread background = new Thread() {
            public void run() {
                try {
                    sleep(1 * 1000);
                    Intent i = new Intent(getBaseContext(), LoginActivity.class);
                    startActivity(i);
                    Log.d("here", "Application Started Successfully");
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("here", "Exception at splash screen" + e.getMessage());
                }
            }
        };
        background.start();
    }
}