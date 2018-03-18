package oya.omarbach;
import umich.cse.yctung.androidlibsvm.LibSVM;import umich.cse.yctung.androidlibsvm.LibSVM;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements SensorEventListener {
    int flag;
    private String name;
    boolean gyroFinished = false;
    boolean accFinished = false;
    private int i = 0;
    private int j = 0;
    private float x = 0;
    private float y = 0;
    private float z = 0;
    private float gx = 0;
    private float gy = 0;
    private float gz = 0;
    private SensorManager sensorManager;
    private Sensor senAccelerometer;
    private Sensor gyro;
    private float[] linear_acceleration = new float[3];
    private float[] arrayx = new float [2000];
    private float[] arrayy = new float [2000];
    private float[] arrayz = new float [2000];
    private float[] arraygx = new float [2000];
    private float[] arraygy = new float [2000];
    private float[] arraygz = new float [2000];

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER && !accFinished) {


            if (i < 2000) {

                arrayx[i]=sensorEvent.values[0];
                arrayy[i]=sensorEvent.values[1];
                arrayz[i]=sensorEvent.values[2];

//                x += sensorEvent.values[0];
//                y += sensorEvent.values[1];
//                z += sensorEvent.values[2];

                i++;
                ((TextView) findViewById(R.id.speed)).setText("AX: " + sensorEvent.values[0] + "\nAY: " + sensorEvent.values[1] + "\nAZ: " + sensorEvent.values[2]);

            } else if (flag == 0) {  //register

//                super.onPause();
//                sensorManager.unregisterListener(this);
                accFinished = true;
                filter();
                x /= i;
                y /= i;
                z /= i;

                ((TextView) findViewById(R.id.speed)).setText("AX: " + x + "\nAY: " + y + "\nAZ: " + z);


                SharedPreferences sharedPref = getSharedPreferences("mypref", 0);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("name", name);
                editor.putFloat("x", x);
                editor.putFloat("y", y);
                editor.putFloat("z", z);
                editor.apply();




            } else if (flag == 1) {  //login
//                super.onPause();
//                sensorManager.unregisterListener(this);
                accFinished = true;
                filter();
                x /= i;
                y /= i;
                z /= i;
                SharedPreferences sharedPref = getSharedPreferences("mypref", 0);
                String savedName = sharedPref.getString("name", "");
                Float xx = sharedPref.getFloat("x", 0);
                Float yy = sharedPref.getFloat("y", 0);
                Float zz = sharedPref.getFloat("z", 0);

                double euc = Math.sqrt(Math.pow(xx - x, 2) + Math.pow(yy - y, 2) + Math.pow(zz - z, 2));

                if (name.equals(savedName) && 1 / (1 + euc) > 0.9) {
                    ((TextView) findViewById(R.id.speed)).setText("Welcome " + savedName + "\n your euc is "+euc);
                } else {
                    ((TextView) findViewById(R.id.speed)).
                            setText("FAIL\nOld X: "+xx+"   New X: " + x + "\nOld Y: "+yy+"   New Y: " + y + "\nold Z:"+zz+"    New Z: " + z +"\n EUC: "+ euc);
                }

//                if (name.equals(savedName) && Math.abs((xx - x) / x) < 0.5 && Math.abs((yy - y) / y) < 0.5 && Math.abs((zz - z) / z) < 0.5) {
//
//                    ((TextView) findViewById(R.id.speed)).setText("welcome " + savedName);
//
//                } else {
//                    ((TextView) findViewById(R.id.speed)).setText("fail"+ x +"\n" + y +"\n"+ z);
//                }

//                gravity[0] = 0;
//                gravity[1] = 0;
//                gravity[2] = 0;
//                linear_acceleration[0] = 0;
//                linear_acceleration[1] = 0;
//                linear_acceleration[2] = 0;

            }


        }

        if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE && !gyroFinished) {

            if (j < 2000) {
                arraygx[j]=sensorEvent.values[0];
                arraygy[j]=sensorEvent.values[1];
                arraygz[j]=sensorEvent.values[2];
                j++;
                ((TextView) findViewById(R.id.gyro)).setText("GX: " + sensorEvent.values[0] + "\n GY: " + sensorEvent.values[1] + "\nGZ: " + sensorEvent.values[2]);
            } else if (flag == 0) {                       //regigster
//                sensorManager.unregisterListener(this);
                gyroFinished = true;
                Gfilter();
                gx /= j;
                gy /= j;
                gz /= j;
                ((TextView) findViewById(R.id.gyro)).setText("GX: " + gx + "\nGY: " + gy + "\nGZ: " + gz);
                SharedPreferences sharedPref = getSharedPreferences("mypref", 0);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("name", name);
                editor.putFloat("gx", gx);
                editor.putFloat("gy", gy);
                editor.putFloat("gz", gz);
                editor.apply();


            } else {                          //login
//                super.onPause();
//                sensorManager.unregisterListener(this);
                gyroFinished = true;
                Gfilter();
                gx /= j;
                gy /= j;
                gz /= j;
                SharedPreferences sharedPref = getSharedPreferences("mypref", 0);
                String savedName = sharedPref.getString("name", "");
                Float gxx = sharedPref.getFloat("gx", 0);
                Float gyy = sharedPref.getFloat("gy", 0);
                Float gzz = sharedPref.getFloat("gz", 0);

                double eucg = Math.sqrt(Math.pow(gxx - gx, 2) + Math.pow(gyy - gy, 2) + Math.pow(gzz - gz, 2));

                if (name.equals(savedName) && 1 / (1 + eucg) > 0.9) {
                    ((TextView) findViewById(R.id.gyro)).setText("Welcome " + savedName + "\n your euc is "+eucg);
                } else {
                    ((TextView) findViewById(R.id.gyro)).setText("fail" +eucg);
                }




            }
        }

            if (accFinished && gyroFinished) {

                sensorManager.unregisterListener(this);
            }
        }




    public void filter(){
        for(int k=2;k<1998;k++) {
            arrayx[k] = (arrayx[k - 2] + 2 * arrayx[k - 1] + 3 * arrayx[k] + 2 * arrayx[k + 1] + arrayx[k + 2]) / 9;
            arrayy[k] = (arrayy[k - 2] + 2 * arrayy[k - 1] + 3 * arrayy[k] + 2 * arrayy[k + 1] + arrayy[k + 2]) / 9;
            arrayz[k] = (arrayz[k - 2] + 2 * arrayz[k - 1] + 3 * arrayz[k] + 2 * arrayz[k + 1] + arrayz[k + 2]) / 9;
        }
            for(int h=0;h<2000;h++){
                x+=arrayx[h];
                y+=arrayy[h];
                z+=arrayz[h];
            }


    }


    public void Gfilter(){
        for(int j=2;j<1998;j++) {
            arraygx[j] = (arraygx[j - 2] + 2 * arraygx[j - 1] + 3 * arraygx[j] + 2 * arraygx[j + 1] + arraygx[j + 2]) / 9;
            arraygy[j] = (arraygy[j - 2] + 2 * arraygy[j - 1] + 3 * arraygy[j] + 2 * arraygy[j + 1] + arraygy[j + 2]) / 9;
            arraygz[j] = (arraygz[j - 2] + 2 * arraygz[j - 1] + 3 * arraygz[j] + 2 * arraygz[j + 1] + arraygz[j + 2]) / 9;
        }
        for(int h=0;h<2000;h++){
            gx+=arraygx[h];
            gy+=arraygy[h];
            gz+=arraygz[h];
        }


    }



    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        final Button butt = findViewById(R.id.button);
//        butt.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                old();
//            }
//        });

        final Button register = findViewById(R.id.start);
        register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            register();
            }
        });


        final Button login = findViewById(R.id.stop);
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                login();
            }
        });
//        new Timer().scheduleAtFixedRate(new TimerTask(){
//            @Override
//            public void run() {
//                final int speed = Integer.parseInt(((TextView) findViewById(R.id.speed)).getText().toString());
//                runOnUiThread(new Runnable(){
//                    @Override
//                    public void run() {
//                        ((TextView) findViewById(R.id.speed)).setText((speed+50)+"");
//                    }
//                });
//            }
//        }, 0, 1000);
    }


    private void register() {
            gyroFinished=false;
            accFinished=false;
            x = 0;
            y = 0;
            z = 0;
            i = 0;
            gx=0;
            gy=0;
            gz=0;
            j=0;
            flag = 0;
            EditText edittext = (EditText) findViewById(R.id.name);
            String s = edittext.getText().toString();
            name = s;
            ((EditText) findViewById(R.id.name)).setText("");

            try {
                //sleep 2 seconds
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//
//            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//            senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//            sensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
//            gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
            gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);





    }


    private void login(){

        accFinished=false;
        gyroFinished=false;
        x = 0;
        y = 0;
        z = 0;
        i = 0;
        gx=0;
        gy=0;
        gz=0;
        j=0;

        flag=1;
        EditText edittext = (EditText) findViewById(R.id.name);
        String s = edittext.getText().toString();
        name = s;
        ((EditText) findViewById(R.id.name)).setText("");
       try {
            //sleep 2 seconds
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);











    }

//    protected void onPause(Bundle savedInstanceState) {
//        super.onPause();
//        sensorManager.unregisterListener(this);
//
//
//    }

    protected void onResume(Bundle savedInstanceState) {
        super.onResume();
        sensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);
    }

}
