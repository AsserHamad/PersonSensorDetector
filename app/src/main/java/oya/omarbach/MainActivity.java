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

import com.kircherelectronics.fsensor.filter.BaseFilter;
import com.kircherelectronics.fsensor.filter.averaging.LowPassFilter;
import com.kircherelectronics.fsensor.filter.averaging.MeanFilter;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements SensorEventListener {
    private float [] prev = new float [1030];
    private float [] filteredAcceleration = new float [3] ;
    private BaseFilter LPfilter = new LowPassFilter();
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
    private float[] arrayx = new float [1030];
    private float[] arrayy = new float [10000];
    private float[] arrayz = new float [10000];
    private float[] arraygx = new float [10000];
    private float[] arraygy = new float [10000];
    private float[] arraygz = new float [10000];private float[] acceleration = new float [3];
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER && !accFinished) {


            System.arraycopy(sensorEvent.values, 0, acceleration, 0, sensorEvent.values.length);
            filteredAcceleration = LPfilter.filter(acceleration);
//            filteredAcceleration = lowPass(sensorEvent.values.clone(), filteredAcceleration);


                x+= filteredAcceleration[0];
                y+= filteredAcceleration[1];
                z+= filteredAcceleration[2];

            arrayx[i] = filteredAcceleration[0];
            arrayy[i] = filteredAcceleration[1];
            arrayz[i] = filteredAcceleration[2];


            i++;
                ((TextView) findViewById(R.id.speed)).setText("AX: " + x + "\nAY: " + y + "\nAZ: " + z);

            } else if (flag == 0) {  //register

//                super.onPause();
//                sensorManager.unregisterListener(this);
//                accFinished = true;

                prev = arrayx;

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
                login();


            } else if (flag == 1) {  //login
//                super.onPause();
//                sensorManager.unregisterListener(this);
                accFinished = true;

                x /= i;
                y /= i;
                z /= i;
                SharedPreferences sharedPref = getSharedPreferences("mypref", 0);
                String savedName = sharedPref.getString("name", "");
                Float xx = sharedPref.getFloat("x", 0);
                Float yy = sharedPref.getFloat("y", 0);
                Float zz = sharedPref.getFloat("z", 0);

                double euc = Math.sqrt(Math.pow(xx - x, 2) + Math.pow(yy - y, 2) + Math.pow(zz - z, 2));

                float distance = DTW(prev,arrayx);

                if (name.equals(savedName) && 1 / (1 + euc) > 0.9) {
                    ((TextView) findViewById(R.id.speed)).setText(""+ distance);
                } else {
                    ((TextView) findViewById(R.id.speed)).
                            setText(""+ distance);
                }

            sensorManager.unregisterListener(this);


            }




//        if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE && !gyroFinished) {
//
//            if (j < 2000) {
//                arraygx[j]=sensorEvent.values[0];
//                arraygy[j]=sensorEvent.values[1];
//                arraygz[j]=sensorEvent.values[2];
//                j++;
//                ((TextView) findViewById(R.id.gyro)).setText("GX: " + sensorEvent.values[0] + "\n GY: " + sensorEvent.values[1] + "\nGZ: " + sensorEvent.values[2]);
//            } else if (flag == 0) {                       //regigster
////                sensorManager.unregisterListener(this);
//                gyroFinished = true;
//                Gfilter();
//                gx /= j;
//                gy /= j;
//                gz /= j;
//                ((TextView) findViewById(R.id.gyro)).setText("GX: " + gx + "\nGY: " + gy + "\nGZ: " + gz);
//                SharedPreferences sharedPref = getSharedPreferences("mypref", 0);
//                SharedPreferences.Editor editor = sharedPref.edit();
//                editor.putString("name", name);
//                editor.putFloat("gx", gx);
//                editor.putFloat("gy", gy);
//                editor.putFloat("gz", gz);
//                editor.apply();
//
//
//            } else {                          //login
////                super.onPause();
////                sensorManager.unregisterListener(this);
//                gyroFinished = true;
//                Gfilter();
//                gx /= j;
//                gy /= j;
//                gz /= j;
//                SharedPreferences sharedPref = getSharedPreferences("mypref", 0);
//                String savedName = sharedPref.getString("name", "");
//                Float gxx = sharedPref.getFloat("gx", 0);
//                Float gyy = sharedPref.getFloat("gy", 0);
//                Float gzz = sharedPref.getFloat("gz", 0);
//
//                double eucg = Math.sqrt(Math.pow(gxx - gx, 2) + Math.pow(gyy - gy, 2) + Math.pow(gzz - gz, 2));
//
//                if (name.equals(savedName) && 1 / (1 + eucg) > 0.9) {
//                    ((TextView) findViewById(R.id.gyro)).setText("Welcome " + savedName + "\n your euc is "+eucg);
//                } else {
//                    ((TextView) findViewById(R.id.gyro)).setText("fail" +eucg);
//                }
//
//
//
//
//            }
//        }

//            if (accFinished) {
//
//                sensorManager.unregisterListener(this);
//                if(flag == 0)login();
//            }
        }




    private void init() {
        LPfilter.setTimeConstant(0.1f);
    }



    protected float[] lowPass( float[] input, float[] output ) {

        float ALPHA = 0.297f/(0.297f+(1/100));
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

//    public void filter(){
//        for(int k=2,t=i;t>3;k++) {
//            arrayx[k] = (arrayx[k - 2] + 2 * arrayx[k - 1] + 3 * arrayx[k] + 2 * arrayx[k + 1] + arrayx[k + 2]) / 9;
//            arrayy[k] = (arrayy[k - 2] + 2 * arrayy[k - 1] + 3 * arrayy[k] + 2 * arrayy[k + 1] + arrayy[k + 2]) / 9;
//            arrayz[k] = (arrayz[k - 2] + 2 * arrayz[k - 1] + 3 * arrayz[k] + 2 * arrayz[k + 1] + arrayz[k + 2]) / 9;
//            t--;
//        }
//        for(int h=0,s=i;s!=0 ;h++){
//            x+=arrayx[h];
//            y+=arrayy[h];
//            z+=arrayz[h];
//            s--;
//        }



    public double DTWDistance(float[] s, float[] t) {
        double [][]DTW = new double[s.length][t.length];
        DTW[0][0] = 0;

        for (int i = 1; i < s.length; i++) {
            DTW[i][0] = Math.abs(s[i]-t[0])+ DTW[i-1][0];
        }
        for (int i = 1; i < t.length; i++) {
            DTW[0][i] = Math.abs(s[0]-t[i])+ DTW[0][i-1];
        }
        for(int i = 1;i<s.length;i++) {
            for(int j = 1;j<t.length;j++) {
                double cost = Math.abs(s[i] - t[j]);
                DTW[i][j] = cost + Math.min(DTW[i-1][j], Math.min(DTW[i][j-1], DTW[i-1][j-1]));
            }
        }
            float sum = 0;
            int i=0;
            int j=0;
            double min;


        while (i!=s.length-1 && j!=t.length-1){
            if( i == s.length-1){
                while (j!=t.length-1){
                    j++;
                    sum+=DTW[i][j];
                }
                return sum;
            }else if(j==t.length-1){
                while(i!=s.length-1){
                    i++;
                    sum+=DTW[i][j];
                }
                return sum;

            }
            if(DTW[i+1][j+1]<DTW[i+1][j] && DTW[i+1][j+1]<DTW[i][j+1]){
                min = DTW[i+1][j+1];
                i+=1;
                j+=1;

            }else if(DTW[i+1][j]<DTW[i][j+1] && DTW[i+1][j]<DTW[i+1][j+1]){
                min= DTW[i+1][j];
                i+=1;
            }else{

                min= DTW[i][j+1];
                j+=1;
            }

                sum+=min;


        }
        return sum;
    }








    public static float DTW(float [] ss, float [] tt) {
        float[] array1 = ss;
        float[] array2 = tt;
        float[][] dtw = new float[array1.length][array2.length];
        dtw[0][0] = 0;
        for (int i = 1; i < array1.length; i++) {
            dtw[i][0] = Math.abs(array1[i]-array2[0]) + dtw[i-1][0];
        }
        for (int i = 1; i < array2.length; i++) {
            dtw[0][i] = Math.abs(array2[i]-array1[0]) + dtw[0][i-1];
        }

        for (int i = 1; i < dtw.length; i++) {
            for (int j = 1; j < dtw[i].length; j++) {
                dtw[i][j] = Math.abs(array1[i] - array2[j]) + Math.min(dtw[i-1][j-1], Math.min(dtw[i-1][j], dtw[i][j-1]));
            }
        }
        int i = dtw.length-1,
                j = dtw[0].length-1;
        float sum = dtw[i][j];
        System.out.println("Sum is "+sum);
        while(i > 0 && j > 0) {
            float min = Math.min(dtw[i-1][j-1], Math.min(dtw[i][j-1], dtw[i-1][j]));
            sum+= min;
            System.out.println("Min is "+min);
            if(min==dtw[i-1][j-1]) {i--;j--;}
            else if(min==dtw[i][j-1]) {j--;}
            else if(min==dtw[i-1][j]) {i--;}
        }
        if(i==0)
            while(j>0) {
                sum+=dtw[0][j];
                j--;
            }
        else
            while(i>0) {
                sum+=dtw[i][0];
                i--;
            }

        return  sum;
    }













    public void Gfilter(){
        for(int j=2;j<998;j++) {
            arraygx[j] = (arraygx[j - 2] + 2 * arraygx[j - 1] + 3 * arraygx[j] + 2 * arraygx[j + 1] + arraygx[j + 2]) / 9;
            arraygy[j] = (arraygy[j - 2] + 2 * arraygy[j - 1] + 3 * arraygy[j] + 2 * arraygy[j + 1] + arraygy[j + 2]) / 9;
            arraygz[j] = (arraygz[j - 2] + 2 * arraygz[j - 1] + 3 * arraygz[j] + 2 * arraygz[j + 1] + arraygz[j + 2]) / 9;
        }
        for(int h=0;h<1000;h++){
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
            float [] filteredAcceleration = new float [3] ;
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

            new Thread(){
                @Override
                public void run() {
                    super.run();
                    try {
                        this.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    accFinished=true;

                }

            }.start();
//            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//            senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//            sensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
//            gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
//            gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
//            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);





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
        float [] filteredAcceleration = new float [3] ;
        flag=1;
        EditText edittext = (EditText) findViewById(R.id.name);
        String s = edittext.getText().toString();
        name = s;
        ((EditText) findViewById(R.id.name)).setText("");
//       try {
//            //sleep 2 seconds
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    this.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                accFinished=true;

            }

        }.start();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
//        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
//        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);











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
