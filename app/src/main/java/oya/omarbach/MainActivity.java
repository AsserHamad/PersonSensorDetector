package oya.omarbach;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements SensorEventListener {
    private int i = 1;
    private float x=0;
    private float y=0;
    private float z=0;
    private SensorManager sensorManager;
    private Sensor senAccelerometer;
    final float alpha = 0.8f;
    private float [] gravity = new float [3];
    private float [] linear_acceleration = new float [3];
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if(mySensor.getType() == Sensor.TYPE_ACCELEROMETER ){
            gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorEvent.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorEvent.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorEvent.values[2];

            // Remove the gravity contribution with the high-pass filter.
            linear_acceleration[0] = sensorEvent.values[0] - gravity[0];
            linear_acceleration[1] = sensorEvent.values[1] - gravity[1];
            linear_acceleration[2] = sensorEvent.values[2] - gravity[2];
            x += linear_acceleration[0];
            y += linear_acceleration[1];
            z += linear_acceleration[2];
            x/=i;
            y/=i;
            z/=i;
            i++;
            ((TextView) findViewById(R.id.speed)).setText("X: "+linear_acceleration[0]+"                       Y: "+linear_acceleration[1]+"                           Z: "+linear_acceleration[2]);




        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final Button start = findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            start();
            }
        });


        final Button stop = findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stop();
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


    private void start(){
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }


    private void stop(){

        ((TextView) findViewById(R.id.speed)).setText("X: "+x+"          Y: "+y+"              Z: "+z);
        super.onPause();
        sensorManager.unregisterListener(this);
        gravity[0] = 0;
        gravity[1] = 0;
        gravity[2] = 0;
        linear_acceleration[0] = 0;
        linear_acceleration[1] = 0;
        linear_acceleration[2] = 0;
        x=0;
        y=0;
        z=0;
        i=1;
        
    }



    protected void onPause(Bundle savedInstanceState) {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    protected void onResume(Bundle savedInstanceState) {
        super.onResume();
        sensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

}
