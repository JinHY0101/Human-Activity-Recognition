package com.example.human_activity_recognition_android_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final int N_SAMPLES = 100;


    private static List<Float> ax;
    private static List<Float> ay;
    private static List<Float> az;
    private static List<Float> gx;
    private static List<Float> gy;
    private static List<Float> gz;

    private static List<Float> axList;
    private static List<Float> ayList;
    private static List<Float> azList;
    private static List<Float> gxList;
    private static List<Float> gyList;
    private static List<Float> gzList;

    private static Float GapAx;
    private static Float GapAy;
    private static Float GapAz;
    private static Float GapGx;
    private static Float GapGy;
    private static Float GapGz;



    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    private TextView axTextView;
    private TextView ayTextView;
    private TextView azTextView;
    private TextView gxTextView;
    private TextView gyTextView;
    private TextView gzTextView;
    private TextView pdTextView;

    private TableRow axTableRow;
    private TableRow ayTableRow;
    private TableRow azTableRow;
    private TableRow gxTableRow;
    private TableRow gyTableRow;
    private TableRow gzTableRow;
    private TableRow pdTableRow;
    private Button saveBtn;
    private Button endBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ax = new ArrayList<>();
        ay = new ArrayList<>();
        az = new ArrayList<>();
        gx = new ArrayList<>();
        gy = new ArrayList<>();
        gz = new ArrayList<>();

        axList= new ArrayList<>();
        ayList = new ArrayList<>();
        azList = new ArrayList<>();
        gxList = new ArrayList<>();
        gyList = new ArrayList<>();
        gzList = new ArrayList<>();

        axTextView = (TextView) findViewById(R.id.ax_prob);
        ayTextView = (TextView) findViewById(R.id.ay_prob);
        azTextView = (TextView) findViewById(R.id.az_prob);
        gxTextView = (TextView) findViewById(R.id.gx_prob);
        gyTextView = (TextView) findViewById(R.id.gy_prob);
        gzTextView = (TextView) findViewById(R.id.gz_prob);
        pdTextView = (TextView) findViewById(R.id.pd_activity);

        axTableRow = (TableRow) findViewById(R.id.ax_row);
        ayTableRow = (TableRow) findViewById(R.id.gx_row);
        azTableRow = (TableRow) findViewById(R.id.az_row);
        gxTableRow = (TableRow) findViewById(R.id.gx_row);
        gyTableRow = (TableRow) findViewById(R.id.gy_row);
        gzTableRow = (TableRow) findViewById(R.id.gx_row);
        pdTableRow = (TableRow) findViewById(R.id.pd_row);
        saveBtn = (Button) findViewById(R.id.btn_start);
        endBtn = (Button) findViewById(R.id.btn_end);

        pdTextView.setText("동작 판별중");
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);

        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_GAME);

        saveBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }else
                {









                }


            }
        });

    }

    protected void onResume() {
        super.onResume();
        getSensorManager().registerListener(this, getSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        getSensorManager().registerListener(this, getSensorManager().getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }






    @Override
    public void onSensorChanged(SensorEvent event) {
        activityPrediction();

        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            ax.add(event.values[0]);
            ay.add(event.values[1]);
            az.add(event.values[2]);

        } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gx.add(event.values[0]);
            gy.add(event.values[1]);
            gz.add(event.values[2]);
        }

    }

    private void activityPrediction() {

        if (ax.size() >= N_SAMPLES && ay.size() >= N_SAMPLES && az.size() >= N_SAMPLES
                && gx.size() >= N_SAMPLES && gy.size() >= N_SAMPLES && gz.size() >= N_SAMPLES
        ) {


            setProbabilities();
            ax.clear(); ay.clear(); az.clear();
            gx.clear(); gy.clear(); gz.clear();

/*
여기 위치 아님
            Collections.sort(ax);
            Collections.sort(ay);
            Collections.sort(az);
            Collections.sort(gx);
            Collections.sort(gy);
            Collections.sort(gz);

*/
            // ax,ay,az 최대값- 최소값 ex)13이상이면 걷는다 20이상이면



        }
    }

    private void setProbabilities() {
        axTextView.setText(Float.toString(ax.get(0)));
        ayTextView.setText(Float.toString(ay.get(0)));
        azTextView.setText(Float.toString(az.get(0)));
        gxTextView.setText(Float.toString(gx.get(0)));
        gyTextView.setText(Float.toString(gy.get(0)));
        gzTextView.setText(Float.toString(gz.get(0)));
        if (axList.size() <= 3)  {
            axList.add(ax.get(0));
            ayList.add(ay.get(0));
            azList.add(az.get(0));
            gxList.add(gx.get(0));
            gyList.add(gy.get(0));
            gzList.add(gz.get(0));
            //System.out.println("동작중1");



        }

        else if (axList.size() >= 3){
            //행동 출력
            //System.out.println("동작중2");

            predictActivity();

            axList.clear(); ayList.clear(); azList.clear();
            gxList.clear(); gyList.clear(); gzList.clear();        }
    }
    public void predictActivity(){
        Collections.sort(axList);Collections.sort(ayList);Collections.sort(azList);
        Collections.sort(gxList);Collections.sort(gyList);Collections.sort(gzList);
        GapAx=axList.get(2)-axList.get(0);
        GapAy=ayList.get(2)-ayList.get(0);
        GapAz=azList.get(2)-azList.get(0);
        GapGx=gxList.get(2)-gxList.get(0);
        GapGy=gyList.get(2)-gyList.get(0);
        GapGz=gzList.get(2)-gzList.get(0);

        Toast.makeText(MainActivity.this, "동작 판별",Toast.LENGTH_SHORT).show();

        if ((3>GapAx+GapAy+GapAz) && (GapAx+GapAy+GapAz >=0)) {
            pdTextView.setText("정지");
        }
        else if ((13>GapAx+GapAy+GapAz) && (GapAx+GapAy+GapAz >=3)) {
            pdTextView.setText("이동중");
        }
        else if ((GapAx>8) && (GapAy+GapAz >=13)) {
            pdTextView.setText("넘어짐");
        }
        else if ((GapAy*3>GapAy+GapAz) && (GapAy+GapAz >=13)) {
            pdTextView.setText("추락");
        }



    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private SensorManager getSensorManager () {
        return (SensorManager) getSystemService(SENSOR_SERVICE);
    }
}


/*

변화량이 큰데 y축 변화량이 훨씬크면 추락으로 판단

계산해보자
 */