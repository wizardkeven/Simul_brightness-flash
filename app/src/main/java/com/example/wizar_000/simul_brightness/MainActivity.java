package com.example.wizar_000.simul_brightness;

import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {
    private SeekBar seekBar;
    private Button  flash_on, flash_off;
    private int MAX_FLASHLIGHT_ON_TIME = 10000;//by millisecond
    private int TIMER_UNIT = 1000;// by second
    CountDownTimer timer;
    private boolean isFlashON;

    //    Context context;
    Camera camera;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isFlashON = false;
        seekBar = (SeekBar) findViewById(R.id.brightness_sb);
        flash_off = (Button) findViewById(R.id.flash_off_BTN);
        flash_on = (Button) findViewById(R.id.flash_on_BTN);
        flash_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
                    Toast.makeText(getBaseContext(),"There is not flash installed",Toast.LENGTH_LONG).show();
                }else{
                    if (null == camera && !isFlashON){
                        camera = Camera.open();
                        Camera.Parameters parameters = camera.getParameters();
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        camera.setParameters(parameters);
                        try {
                            camera.setPreviewTexture(new SurfaceTexture(0));
                        }
                        catch (Exception e) {
                            Log.d("LED", "ohhhh");
                        }
                        timer = new CountDownTimer(MAX_FLASHLIGHT_ON_TIME,TIMER_UNIT){
                            @Override
                            public void onTick(long millisUntilFinished) {
                                camera.startPreview();
                                isFlashON = true;
                            }

                            @Override
                            public void onFinish() {
                                if (camera !=null && isFlashON){
                                    isFlashON = false;
                                    camera.stopPreview();
                                    camera.release();
                                    camera = null;
                                }
                            }
                        }.start();

                    }
                }
            }
        });
        flash_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camera != null && isFlashON){
                    isFlashON = false;
                    timer.cancel();
                    timer = null;
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                }
            }
        });
        seekBar.setMax(255);

        float curBrightnessValue = 0;
        try{
            curBrightnessValue = android.provider.Settings.System.getInt(
                    getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS
            );
        }catch (Settings.SettingNotFoundException e){
            e.printStackTrace();
        }
        int screen_brightness = (int)curBrightnessValue;
        seekBar.setProgress(screen_brightness);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                android.provider.Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS,
                        progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        quitLedApp();
    }
    /**
     * Quit ledapp after checking the timer and camera validity
     */
    private void quitLedApp() {
        if (null != timer){
            timer.cancel();
            timer = null;
        }
        if (null != camera){
            if (isFlashON)
                camera.stopPreview();
            camera.release();
        }
    }
}
