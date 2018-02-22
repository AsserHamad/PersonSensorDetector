package oya.omarbach;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor senAccelerometer;
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if(mySensor.getType() == Sensor.TYPE_ACCELEROMETER){
            if(sensorEvent.values[0]+sensorEvent.values[1]>=5)
            ((TextView) findViewById(R.id.speed)).setText("X: "+sensorEvent.values[0]+"  Y: "+sensorEvent.values[1]+"   Z: "+sensorEvent.values[2]);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
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

    protected void onPause(Bundle savedInstanceState) {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    protected void onResume(Bundle savedInstanceState) {
        super.onResume();
        sensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

}
