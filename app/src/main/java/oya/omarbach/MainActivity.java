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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.kircherelectronics.fsensor.filter.BaseFilter;
import com.kircherelectronics.fsensor.filter.averaging.LowPassFilter;
import com.kircherelectronics.fsensor.filter.averaging.MeanFilter;
import com.kircherelectronics.fsensor.filter.averaging.MedianFilter;
import com.kircherelectronics.fsensor.filter.fusion.OrientationComplimentaryFusion;
import com.kircherelectronics.fsensor.filter.fusion.OrientationFusion;
import com.kircherelectronics.fsensor.linearacceleration.LinearAcceleration;
import com.kircherelectronics.fsensor.linearacceleration.LinearAccelerationAveraging;
import com.kircherelectronics.fsensor.linearacceleration.LinearAccelerationFusion;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements SensorEventListener {
    private BaseFilter LPfilter = new LowPassFilter();
    private float timestampOld;
    int flag;
    private String name;
    boolean gyroFinished = false;
    boolean accFinished = false;
    private int i = 0;
    private int j = 0;
    private double x = 0;
    private double y = 0;
    private double z = 0;
    private float gx = 0;
    private float gy = 0;
    private float gz = 0;
    private SensorManager sensorManager;
    private Sensor senAccelerometer;
    private Sensor gyro;
    double[] prev;
    ArrayList<Double> tmp = new ArrayList<Double>();
    ArrayList<Double> samples = new ArrayList<Double>();
    ArrayList<ArrayList<Double>> cycles = new ArrayList<ArrayList<Double>>();
    private double[] arrayx = new double[5000];
    private double[] arrayy = new double[5000];
    private double[] arrayz = new double[5000];
    int s;
    double magnitude;
    ArrayList<Double> AverageCycle = new ArrayList<Double>();
    double distance;
    private float[] arraygx = new float[10000];
    private float[] arraygy = new float[10000];
    private float[] arraygz = new float[10000];
    private float[] filteredAcceleration = new float[3];
    ArrayList<Long> time = new ArrayList<Long>();

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION && !accFinished) {

            filteredAcceleration = lowPass(sensorEvent.values.clone(), filteredAcceleration);
            time.add(sensorEvent.timestamp);
            magnitude = Math.sqrt(filteredAcceleration[0] * filteredAcceleration[0] + filteredAcceleration[1] * filteredAcceleration[1] + filteredAcceleration[2] * filteredAcceleration[2]);
            samples.add(magnitude);
            arrayx[i] = filteredAcceleration[0];
            arrayy[i] = filteredAcceleration[1];
            arrayz[i] = filteredAcceleration[2];
            i++;
            ((TextView) findViewById(R.id.speed)).setText("AX: " + x + "\nAY: " + y + "\nAZ: " + z);

        } else if (flag == 0) {  //register
            sensorManager.unregisterListener(this);
            s = i;
//            x /= i;
//            y /= i;
//            z /= i;
//            System.out.println("starts indices: "+cycleStarts(samples));
            ArrayList<Double> newSamples = skipIrregularCycles(samples,cycleStarts(samples));
            double templateDistance = average(distances(newSamples,cycleStarts(newSamples)));
            System.out.println(templateDistance);
            ArrayList<Double> Template = cycle(newSamples,cycleStarts(newSamples));
            System.out.println(Template.size());
            ((TextView) findViewById(R.id.speed)).setText("registered" +"\n"+Template.size()+"\n"+templateDistance );
            SharedPreferences sharedPref = getSharedPreferences("mypref", 0);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("name", name);
             putDouble(editor,"DTW",templateDistance);
                String json = new Gson().toJson(Template);
               editor.putString("Template", json);
//            putDouble(editor, "x", x);
//            putDouble(editor, "y", y);
//            putDouble(editor, "z", z);
            editor.apply();


        } else if (flag == 1) {  //login
//                super.onPause();
            sensorManager.unregisterListener(this);
            WMAfilter();
            x /= i;
            y /= i;
            z /= i;
            ArrayList<Double> newSamples = skipIrregularCycles(samples,cycleStarts(samples));
            ArrayList<Double> template = cycle(newSamples,cycleStarts(newSamples));
            System.out.println(template.size());

            SharedPreferences sharedPref = getSharedPreferences("mypref", 0);
            String savedName = sharedPref.getString("name", "");
//            double xx = getDouble(sharedPref, "x", 0);
//            double yy = getDouble(sharedPref, "y", 0);
//            double zz = getDouble(sharedPref, "z", 0);

            Gson gson = new Gson();
            String json = sharedPref.getString("Template", null);
            Type type = new TypeToken<ArrayList<Double>>() {}.getType();
            ArrayList<Double> template2 = gson.fromJson(json, type);
            double templateDistance =getDouble(sharedPref,"DTW",0);
//            System.out.println("distance of T1: "+templateDistance);
//            ///// calculation for time between samples
//            long times = 0;
//            for (int i = 0; i < time.size() - 1; i++) {
//                times += time.get(i + 1) - time.get(i);
//                System.out.println(time.get(i + 1) - time.get(i));
//            }
//            times /= time.size();
//            System.out.println(times);
//            ////////
            double ratio = DTW(template2,template)/templateDistance;
            System.out.println("dtw "+DTW(template2,template));


            int c = estimateCycleLength(samples);
            if (name.equals(savedName) && (ratio <= 1.1 && ratio >= 0.9)) {
//                ((TextView) findViewById(R.id.speed)).setText("cycleLength" + c + "" + "\n" + i2 + "\n" + s + "\n" + x + "\n" + y + "\n" + z + "\n" + xx + "\n" + yy + "\n" + zz + "\n");
                ((TextView) findViewById(R.id.speed)).setText("ratio:  "+ratio +"\n"+template.size()+"\n"+DTW(template2,template) );
            } else {
//                ((TextView) findViewById(R.id.speed)).
//                        setText("cycleLength" + c + "" + "\n" + i2 + "\n" + s + "\n" + x + "\n" + y + "\n" + z + "\n" + xx + "\n" + yy + "\n" + zz + "\n");
                ((TextView) findViewById(R.id.speed)).setText("ratio:  "+ratio +"\n"+template.size()+"\n"+ DTW(template2,template) );

            }

            GraphView graph = (GraphView) findViewById(R.id.graph);
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
            for (int i=0;i<samples.size();i++){
                series.appendData(new DataPoint(i,samples.get(i)),true,samples.size());
            }
            graph.addSeries(series);

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

        LPfilter.setTimeConstant(0.18f);
    }


    protected float[] lowPass(float[] input, float[] output) {
        float timestamp = System.nanoTime();
        float timeConstant = 0.297f;
        float dt = 1 / (i / ((timestamp - timestampOld) / 1000000000.0f));
        float alpha = timeConstant / (timeConstant + dt);
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = alpha * output[i] + (1 - alpha) * input[i];
        }
        return output;
    }

    public static double DTW(ArrayList<Double> template,ArrayList<Double> sample) {
        Double[] s = template.toArray(new Double[template.size()])
                , t= sample.toArray(new Double[sample.size()]);
        double[][] DTW = new double[s.length][t.length];
        DTW[0][0] = 0;
        for (int i = 1; i < s.length; i++) {
            DTW[i][0] = Math.abs(s[i] - t[0]) + DTW[i - 1][0];
        }
        for (int i = 1; i < t.length; i++) {
            DTW[0][i] = Math.abs(s[0] - t[i]) + DTW[0][i - 1];
        }
        for (int i = 1; i < s.length; i++) {
            for (int j = 1; j < t.length; j++) {
                double cost = Math.abs(s[i] - t[j]);
                DTW[i][j] = cost + Math.min(DTW[i - 1][j], Math.min(DTW[i][j - 1], DTW[i - 1][j - 1]));
            }
        }
        double sum = DTW[s.length-1][t.length-1];
        int i = s.length - 1;
        int j = t.length - 1;
        double min;
        while (i > 0 && j > 0) {
            if (DTW[i - 1][j - 1] < DTW[i - 1][j] && DTW[i - 1][j - 1] < DTW[i][j - 1]) {
                min = DTW[i - 1][j - 1];
                i --;
                j --;
            } else if (DTW[i - 1][j] < DTW[i][j - 1] && DTW[i - 1][j] < DTW[i - 1][j - 1]) {
                min = DTW[i - 1][j];
                i -- ;
            } else {
                min = DTW[i][j - 1];
                j --;
            }
            sum += min;
        }
        if (i == 0) {
            while (j != 0) {
                j--;
                sum += DTW[i][j];
            }
            return sum;
        } else if (j == 0) {
            while (i != 0) {
                i--;
                sum += DTW[i][j];
            }
            return sum;
        }
        return sum;
    }

    public SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    public double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }


//    public ArrayList<Double> Average(ArrayList<Double> a1, ArrayList<Double> a2) {
//        ArrayList<Double> temp = new ArrayList<Double>();
//        int length = a1.size() > a2.size() ? a2.size() : a1.size();
//        for (int i = 0; i < length; i++) {
//            temp.add((a1.get(i) + a2.get(i)) / 2);
//        }
//        return temp;
//    }


    public double D(ArrayList<Double> array1, ArrayList<Double> array2) {
        double[][] dtw = new double[array1.size()][array2.size()];
        dtw[0][0] = 0;
        for (int i = 1; i < array1.size(); i++) {
            dtw[i][0] = Math.abs(array1.get(i) - array2.get(0)) + dtw[i - 1][0];
        }
        for (int i = 1; i < array2.size(); i++) {
            dtw[0][i] = Math.abs(array2.get(i) - array1.get(0)) + dtw[0][i - 1];
        }

        for (int i = 1; i < dtw.length; i++) {
            for (int j = 1; j < dtw[i].length; j++) {
                dtw[i][j] = Math.abs(array1.get(i) - array2.get(0)) + Math.min(dtw[i - 1][j - 1], Math.min(dtw[i - 1][j], dtw[i][j - 1]));
            }
        }
        int i = dtw.length - 1,
                j = dtw[0].length - 1;
        double sum = dtw[i][j];
        while (i > 0 && j > 0) {
            double min = Math.min(dtw[i - 1][j - 1], Math.min(dtw[i][j - 1], dtw[i - 1][j]));

            sum += min;
            if (min == dtw[i - 1][j - 1]) {
                i--;
                j--;
            } else if (min == dtw[i][j - 1]) {
                j--;
            } else if (min == dtw[i - 1][j]) {
                i--;
            }
        }
        if (i == 0)
            while (j > 0) {
                sum += dtw[0][j];
                j--;
            }
        else
            while (i > 0) {
                sum += dtw[i][0];
                i--;
            }

        return sum;
    }

    //          cycle length estimation and step detection
// estimation of cycle length
    public static int estimateCycleLength(ArrayList<Double> samples) {
        int diff = 20; //window size
        ArrayList<Double> score = new ArrayList<Double>();  //for absolute distance between windows
        ArrayList<Double> baseline = new ArrayList<Double>();
        ArrayList<Integer> minimas = new ArrayList<Integer>();  //indices of the locaal minimas of the score array
        ArrayList<Integer> differences = new ArrayList<Integer>();
        //get baseline
        for (int i = (samples.size() / 2) - (diff / 2); i < (samples.size() / 2) + (diff / 2); i++) {
            baseline.add(samples.get(i));
        }
        //compute absolute distance
        for (int i = 1; i < ((samples.size() / 2) - (diff / 2)) / diff; i++) {
            score.add(getAbsoluteDistance(true, i, baseline, samples, diff));
        }
        for (int i = 1; i < ((samples.size() / 2) - (diff / 2)) / diff; i++) {
            score.add(getAbsoluteDistance(false, i, baseline, samples, diff));
        }

        //compute local minimas
        for (int i = 2; i < score.size() - 2; i++) {
            if (score.get(i) < score.get(i + 1)   && score.get(i) < score.get(i + 2) &&   score.get(i) < score.get(i - 1)
                    &&   score.get(i) < score.get(i - 2))
                minimas.add(i);
        }

        //difference between adjacent elements

            for (int i = 0; i < minimas.size()-1; i += 2) {
                differences.add(Math.abs(minimas.get(i) - minimas.get(i + 1)));
            }

        //mode
        return diff*getMode(differences);


    }

    public static double getAbsoluteDistance(boolean forwards, int nth, ArrayList<Double> baseline, ArrayList<Double> samples, int diff) {
        //To check whether we're moving forwards or backwards with the samples
        double sum = 0;
        if (forwards) {
            //if moving forward
            int currentStartingIndex = (samples.size() / 2) - (diff / 2) + diff * nth;
            for (int i = 0; i < diff; i++) {
                sum += Math.abs((baseline.get(i) - samples.get(currentStartingIndex + i)));
            }
        } else {
            //if moving backward
            int currentStartingIndex = (samples.size() / 2) - (diff / 2) - diff * nth;
            for (int i = 0; i < diff; i++) {
                sum += Math.abs((baseline.get(i) - samples.get(currentStartingIndex + i)));
            }
        }

        return sum;
    }


    public static int getMode(ArrayList<Integer> differences) {
        int mode = 0;
        int maxCount = 0;
        for (int i = 0; i < differences.size(); i++) {
            int count = 0;
            for (int j = 0; j < differences.size(); j++) {
                if (differences.get(i) == differences.get(j))
                    count++;
            }
            if (count > maxCount) {
                maxCount = count;
                mode = differences.get(i);
            }
        }
        if (maxCount > 1) {
            return mode;
        }
        int avg=0;
        for(int i=0;i < differences.size();i++){
            avg+=differences.get(i);
        }
        avg/=differences.size();
        return avg;
    }



// start points of cycles
    public ArrayList<Integer> cycleStarts(ArrayList<Double> samples){
        double offset = 0.2;
        int length = estimateCycleLength(samples);
        int start = minAtCenter(samples,length);
        ArrayList<Integer> indices = new ArrayList<Integer>();
        indices.add(start);
        double min = 0;
        int index=0;
//        System.out.println(((samples.size()/2)/length)-1);
//        System.out.println(start);
        for(int i=0;i<((samples.size()/2)/length);i++){
            start-=length;
            index=start;
            try {
                min = samples.get(index);
            }catch(IndexOutOfBoundsException e){
                break;
            }
            for(int j= (int)(start-offset*length);j<start+offset*length;j++){
                try{
                    if (samples.get(j)<min){
                        min=samples.get(j);
                        index=j;
                    }
                }catch(IndexOutOfBoundsException e){
                break;
            }
            }
            start=index;
            indices.add(index);
        }

        Collections.reverse(indices);
        start = minAtCenter(samples,length);
        for(int i=0;i<((samples.size()/2)/length);i++){

//            Log.v("tag","hi");
            start+=length;
            index=start;
            try {
                min = samples.get(index);
            }catch(IndexOutOfBoundsException e){
                break;
            }
            for(int j= (int)(start-offset*length);j<start+offset*length;j++){
                try{
                    if (samples.get(j)<min){
                        min=samples.get(j);
                        index=j;
                    }
                }catch(IndexOutOfBoundsException e){
                break;
            }
            }
            start=index;
            indices.add(index);
        }
        System.out.println("starts indices "+indices);
        return indices;
}

 public int minAtCenter(ArrayList<Double> samples,int length){
        double min = samples.get(samples.size()/2);
        int index=0;
     for(int i = (samples.size()/2)-length;i<(samples.size()/2)+length;i++){
            if (samples.get(i)<min){
                index = i;
                min = samples.get(i);
     }
        }
//        System.out.println("INDEX OF MIN"+index);
        return index;
 }


// remove unusual cycles
public ArrayList<Double> skipIrregularCycles(ArrayList<Double> samples, ArrayList<Integer> indices){
    ArrayList<Double> averages = distances (samples,indices);
    System.out.println("after first distances: "+ averages);
    double diffAverage = average(averages);
    //This function returns all the indices which are to be ommitted from further calculations
    return removeFromSamples(samples, indices, averages, diffAverage);
}

    public ArrayList<Double> distances(ArrayList<Double> samples, ArrayList<Integer> indices){
        ArrayList<Double> averages = new ArrayList<Double>();
        for(int index : indices){
//            Log.v("tag", "entered loop");
            if(index == indices.get(indices.size()-1))
                break;
            ArrayList<Double> window0 = new ArrayList<Double>(samples.subList(index, indices.get(indices.indexOf(index)+1)));
            ArrayList<Double> differences = new ArrayList<Double>();
//            Log.v("tag", "entered if");
            for(int _index : indices){
                //We don't want to compare it to itself

                if(!(_index==index) && !(_index == indices.get(indices.size()-1 ))) {

                    ArrayList<Double> window1 = new ArrayList<Double>(samples.subList(_index, indices.get(indices.indexOf(_index) + 1)));
                    differences.add(DTW(window0, window1));
            }
            }
            averages.add(average(differences));
        }
        if(averages.isEmpty()){
            System.out.println("EMPTY");
        }
        return averages;
    }



    public double average(ArrayList<Double> doubles){
        double sum = 0;
        for(double d : doubles)
            sum+=d;
        return sum/doubles.size();
    }

    public ArrayList<Double> removeFromSamples(ArrayList<Double> samples, ArrayList<Integer> indices, ArrayList<Double> averages, double average){
        ArrayList<Integer> removedIndices = new ArrayList<Integer>();
        ArrayList<Integer> list = new ArrayList<Integer>();
        for(int i=0;i<indices.size()-1;i++){
            if(averages.get(i)<average*0.8 || averages.get(i)>average*1.2){
                removedIndices.add(i);
            }
        }
        if(removedIndices.size()>0){
            for(int i=0;i<removedIndices.size();i++){
                for(int j=indices.get(removedIndices.get(i));j<indices.get(removedIndices.get(i)+1);j++){
                    list.add(j);
                }
            }
            Collections.sort(list,Collections.reverseOrder());
            for(int i : list)
                samples.remove(i);
            indices = cycleStarts(samples);
            averages = distances(samples, indices);
            System.out.println("after samples removed"+averages);
            System.out.println("cycle starts"+indices);

            return removeFromSamples(samples, indices, averages, average(averages));

        }
        else
            return samples;
    }


    public ArrayList<Double> cycle(ArrayList<Double> samples, ArrayList<Integer> indices){
        ArrayList<Double> cycle = new ArrayList<Double>();
        ArrayList<Double> averages = distances(samples,indices);
        int minIndex = averages.indexOf(Collections.min(averages));
        for(int i=indices.get(minIndex);i<indices.get(minIndex+1);i++){
            cycle.add(samples.get(i));
        }
        return cycle;
    }










    public void WMAfilter(){
//        for(int j=2;j<i-4;j++) {
//            arrayx[j] = (arrayx[j - 2] + 2 * arrayx[j - 1] + 3 * arrayx[j] + 2 * arrayx[j + 1] + arrayx[j + 2]) / 9;
//            arrayy[j] = (arrayy[j - 2] + 2 * arrayy[j - 1] + 3 * arrayy[j] + 2 * arrayy[j + 1] + arrayy[j + 2]) / 9;
//            arrayz[j] = (arrayz[j - 2] + 2 * arrayz[j - 1] + 3 * arrayz[j] + 2 * arrayz[j + 1] + arrayz[j + 2]) / 9;
//        }
        for (int i =0;i<arrayx.length-1;i++){
            x+=arrayx[i];
            y+=arrayy[i];
            z+=arrayz[i];
        }

    }



    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager   = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer= sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
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
            time = new ArrayList<Long>();
            samples = new ArrayList<Double>();
            AverageCycle = new ArrayList<Double>();
            gyroFinished=false;
            accFinished=false;
            filteredAcceleration = new float [3];
            x = 0;
            y = 0;
            z = 0;
            i = 0;
            gx=0;
            gy=0;
            gz=0;
            j=0;
            flag = 0;
            EditText edittext = findViewById(R.id.name);
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
                        sleep(25000);
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
             timestampOld =  System.nanoTime();
            sensorManager.registerListener(this, senAccelerometer, sensorManager.SENSOR_DELAY_FASTEST);
//            gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
//            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);





    }


    private void login(){
        time = new ArrayList<Long>();
        samples = new ArrayList<Double>();
        AverageCycle = new ArrayList<Double>();
        accFinished=false;
        gyroFinished=false;
        filteredAcceleration = new float [3];
        x = 0;
        y = 0;
        z = 0;
        i = 0;
        gx=0;
        gy=0;
        gz=0;
        j=0;
        filteredAcceleration = new float [3];
          arrayx = new double [5000];
          arrayy = new double [5000];
          arrayz = new double [5000];
        float [] filteredAcceleration = new float [3] ;
        flag=1;
        EditText edittext = findViewById(R.id.name);
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
                    sleep(15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                accFinished=true;

            }

        }.start();
        timestampOld =  System.nanoTime();
        sensorManager.registerListener(this, senAccelerometer, sensorManager.SENSOR_DELAY_FASTEST);
//        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
//        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);











    }

    protected void onPause(Bundle savedInstanceState) {
        super.onPause();
        sensorManager.unregisterListener(this);


    }

    protected void onResume(Bundle savedInstanceState) {
        super.onResume();
        sensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    //    sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);
    }

}
