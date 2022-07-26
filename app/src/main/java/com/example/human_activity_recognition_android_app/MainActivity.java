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
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final int N_SAMPLES = 100;


    private static List<Float> ax;
    private static List<Float> ay;
    private static List<Float> az;
    private static List<Float> mx;
    private static List<Float> my;
    private static List<Float> mz;
    private static List<Float> gx;
    private static List<Float> gy;
    private static List<Float> gz;

    private static List<Float> axList;
    private static List<Float> ayList;
    private static List<Float> azList;
    private static List<Float> mxList;
    private static List<Float> myList;
    private static List<Float> mzList;
    private static List<Float> gxList;
    private static List<Float> gyList;
    private static List<Float> gzList;

    private static Float GapAx;
    private static Float GapAy;
    private static Float GapAz;

    private double latitude, longitude; //GPS를 통해 측정된 위치 값을 다른 액티비티로 넘겨주기 위한 변수
    Handler handler = new Handler(Looper.getMainLooper()); // 메인 쓰레드
    private Handler mPeriodicEventHandler = new Handler(); // TIME OUT 쓰레드
    private final int PERIODIC_EVENT_TIMEOUT = 3000; // TIME OUT 세팅
    //서비스에서 작동될 쓰레드 시간 관리 타이머
    private Timer fuseTimer = new Timer();
    private int sendCount = 0;
    private char sentRecently = 'N';
    //자이로 센서의 각속도
    private float[] gyro = new float[3];
    //계산된 각속도
    private float degreeFloat;
    private float degreeFloat2;
    //자이로 센서 데이터의 회전 행렬
    private float[] gyroMatrix = new float[9];
    //자이로 행렬으로부터의 방위각
    private float[] gyroOrientation = new float[3];
    //자기장 벡터
    private float[] magnet = new float[3];
    //가속도계 벡터
    private float[] accel = new float[3];
    //가속도계와 자기장으로부터의 방위각
    private float[] accMagOrientation = new float[3];
    //3가지 센서를 합친 것의 방위각
    private float[] fusedOrientation = new float[3];
    //가속도계와 자기장센서의 기준 회전 행렬
    private float[] rotationMatrix = new float[9];
    //센서 값의 변화 크기를 비교하기 위한 변수
    public static final float EPSILON = 0.000000001f;
    //타이머의 쓰레드의 간격 설정용 변수
    public static final int TIME_CONSTANT = 30;
    //중력 가속도값
    public static final float FILTER_COEFFICIENT = 0.98f;
    //나노s -> s
    private static final float NS2S = 1.0f / 1000000000.0f;
    //시간으로 적분하기 위한 변수
    private float timestamp;
    //재실행을 위한 변수
    private boolean initState = true;



    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mMagneticField;
    private Runnable doPeriodicTask = new Runnable() {
        public void run() {
            sentRecently = 'N';
        }
    };

    private TextView axTextView;
    private TextView ayTextView;
    private TextView azTextView;
    private TextView mxTextView;
    private TextView myTextView;
    private TextView mzTextView;
    private TextView gxTextView;
    private TextView gyTextView;
    private TextView gzTextView;
    private TextView pdTextView;

    private TableRow axTableRow;
    private TableRow ayTableRow;
    private TableRow azTableRow;
    private TableRow mxTableRow;
    private TableRow myTableRow;
    private TableRow mzTableRow;
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

        ax = new ArrayList<>();ay = new ArrayList<>();az = new ArrayList<>();
        mx = new ArrayList<>();my = new ArrayList<>();mz = new ArrayList<>();
        gx = new ArrayList<>();gy = new ArrayList<>();gz = new ArrayList<>();

        axList= new ArrayList<>();ayList = new ArrayList<>();azList = new ArrayList<>();
        mxList= new ArrayList<>();myList = new ArrayList<>();mzList = new ArrayList<>();
        gxList = new ArrayList<>();gyList = new ArrayList<>();gzList = new ArrayList<>();

        axTextView = (TextView) findViewById(R.id.ax_prob);
        ayTextView = (TextView) findViewById(R.id.ay_prob);
        azTextView = (TextView) findViewById(R.id.az_prob);
        mxTextView = (TextView) findViewById(R.id.mx_prob);
        myTextView = (TextView) findViewById(R.id.my_prob);
        mzTextView = (TextView) findViewById(R.id.mz_prob);
        gxTextView = (TextView) findViewById(R.id.gx_prob);
        gyTextView = (TextView) findViewById(R.id.gy_prob);
        gzTextView = (TextView) findViewById(R.id.gz_prob);
        pdTextView = (TextView) findViewById(R.id.pd_activity);

        axTableRow = (TableRow) findViewById(R.id.ax_row);
        ayTableRow = (TableRow) findViewById(R.id.gx_row);
        azTableRow = (TableRow) findViewById(R.id.az_row);
        mxTableRow = (TableRow) findViewById(R.id.mx_row);
        myTableRow = (TableRow) findViewById(R.id.my_row);
        mzTableRow = (TableRow) findViewById(R.id.mz_row);
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

        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, mMagneticField , SensorManager.SENSOR_DELAY_GAME);
/*
        saveBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }else



                }


            }
        });
*/
    }

    protected void onResume() {
        super.onResume();
        getSensorManager().registerListener(this, getSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        getSensorManager().registerListener(this, getSensorManager().getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
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
            System.arraycopy(event.values, 0, accel, 0, 3);
            calculateAccMagOrientation();
        } else if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mx.add(event.values[0]);
            my.add(event.values[1]);
            mz.add(event.values[2]);
        } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gx.add(event.values[0]);
            gy.add(event.values[1]);
            gz.add(event.values[2]);
            System.arraycopy(event.values, 0, magnet, 0, 3);
            gyroFunction(event);
        }


    }
    public void calculateAccMagOrientation() {
        if(SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)){
            SensorManager.getOrientation(rotationMatrix, accMagOrientation);
        }

    }
    private void getRotationVectorFromGyro(float[] gyroValues,float[] deltaRotationVector, float timeFactor) {
        float[] normValues = new float[3];

        //샘플의 각속도를 계산한다.
        float omegaMagnitude =
                (float) Math.sqrt(gyroValues[0] * gyroValues[0] +
                        gyroValues[1] * gyroValues[1] +
                        gyroValues[2] * gyroValues[2]);

        //축을 얻기에 충분히 큰 경우, 회전 벡터를 표준화
        if (omegaMagnitude > EPSILON) {
            normValues[0] = gyroValues[0] / omegaMagnitude;
            normValues[1] = gyroValues[1] / omegaMagnitude;
            normValues[2] = gyroValues[2] / omegaMagnitude;
        }


               /*timestep에 의해 이 축을 중심으로 각속도와 통합한다.
               이 샘플에서 시간 경과에 따른 델타 값의 회전변환을 얻으려면 델타 회전의 축각 표현의 변환이 필요하다.
               즉, 회전 행렬로 변환하기 전에 쿼터니언으로 변환 */
        float thetaOverTwo = omegaMagnitude * timeFactor;
        float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
        deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
        deltaRotationVector[3] = cosThetaOverTwo;
    }

    private void gyroFunction(SensorEvent event) {
        //첫 번째 가속도계 / 자기장 방향이 획득 될 때까지 시작하지 않음.
        if (accMagOrientation == null)
            return;

        //자이로 회전 배열 값을 초기화한다.
        if (initState) {
            float[] initMatrix = new float[9];
            initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
            float[] test = new float[3];
            SensorManager.getOrientation(initMatrix, test);
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
            initState = false;
        }

        //새 자이로 값을 자이로 배열에 복사한다.
        //원래의 자이로 데이터를 회전 벡터로 변환한다.
        float[] deltaVector = new float[4];
        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            System.arraycopy(event.values, 0, gyro, 0, 3);
            getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
        }

        //측정 완료이 완료되면, 다음 시간 간격을 위해 현재 시간을 설정한다.
        timestamp = event.timestamp;

        //회전 벡터를 회전 행렬로 변환한다.
        float[] deltaMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);
        //회전 벡터를 회전 행렬로 변환한다.
        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);
        //회전 행렬에서 자이로 스코프 기반 방향을 얻는다.
        SensorManager.getOrientation(gyroMatrix, gyroOrientation);
    }
    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        float sinX = (float) Math.sin(o[1]);
        float cosX = (float) Math.cos(o[1]);
        float sinY = (float) Math.sin(o[2]);
        float cosY = (float) Math.cos(o[2]);
        float sinZ = (float) Math.sin(o[0]);
        float cosZ = (float) Math.cos(o[0]);

        //x 축 (피치)에 대한 회전배열
        xM[0] = 1.0f;
        xM[1] = 0.0f;
        xM[2] = 0.0f;
        xM[3] = 0.0f;
        xM[4] = cosX;
        xM[5] = sinX;
        xM[6] = 0.0f;
        xM[7] = -sinX;
        xM[8] = cosX;

        //y 축 (롤)에 대한 회전배열
        yM[0] = cosY;
        yM[1] = 0.0f;
        yM[2] = sinY;
        yM[3] = 0.0f;
        yM[4] = 1.0f;
        yM[5] = 0.0f;
        yM[6] = -sinY;
        yM[7] = 0.0f;
        yM[8] = cosY;

        //z 축에 대한 회전 (방위각)배열
        zM[0] = cosZ;
        zM[1] = sinZ;
        zM[2] = 0.0f;
        zM[3] = -sinZ;
        zM[4] = cosZ;
        zM[5] = 0.0f;
        zM[6] = 0.0f;
        zM[7] = 0.0f;
        zM[8] = 1.0f;

        //회전 순서는 y, x, z (롤, 피치, yaw)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);

        return resultMatrix;

    }
    private float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];
        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return result;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private SensorManager getSensorManager () {
        return (SensorManager) getSystemService(SENSOR_SERVICE);
    }








    private void activityPrediction() {

        if (ax.size() >= N_SAMPLES && ay.size() >= N_SAMPLES && az.size() >= N_SAMPLES
                && mx.size() >= N_SAMPLES && my.size() >= N_SAMPLES && mz.size() >= N_SAMPLES
                && gx.size() >= N_SAMPLES && gy.size() >= N_SAMPLES && gz.size() >= N_SAMPLES
        ) {


            setProbabilities();
            ax.clear(); ay.clear(); az.clear();
            mx.clear(); my.clear(); mz.clear();
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
        mxTextView.setText(Float.toString(mx.get(0)));
        myTextView.setText(Float.toString(my.get(0)));
        mzTextView.setText(Float.toString(mz.get(0)));
        //gxTextView.setText(Float.toString(gx.get(0)));
        //gyTextView.setText(Float.toString(gy.get(0)));
        //gzTextView.setText(Float.toString(gz.get(0)));

        /*if (axList.size() <= 2)  {
            axList.add(ax.get(0));
            ayList.add(ay.get(0));
            azList.add(az.get(0));
            mxList.add(mx.get(0));
            myList.add(my.get(0));
            mzList.add(mz.get(0));
            gxList.add(gx.get(0));
            gyList.add(gy.get(0));
            gzList.add(gz.get(0));
            //System.out.println("동작중1");







        }

        else if (axList.size() >= 2){
            //행동 출력
            //System.out.println("동작중2");

            predictActivity();

            axList.clear(); ayList.clear(); azList.clear();
            mxList.clear(); myList.clear(); mzList.clear();
            gxList.clear(); gyList.clear(); gzList.clear();        }


         */
        predictActivity();


    }
    public void predictActivity(){
        float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;
        fusedOrientation[0] =
                FILTER_COEFFICIENT * gyroOrientation[0]
                        + oneMinusCoeff * accMagOrientation[0];

        fusedOrientation[1] =
                FILTER_COEFFICIENT * gyroOrientation[1]
                        + oneMinusCoeff * accMagOrientation[1];

        fusedOrientation[2] =
                FILTER_COEFFICIENT * gyroOrientation[2]
                        + oneMinusCoeff * accMagOrientation[2];
        double SMV = Math.sqrt(ax.get(0)* ax.get(0)+ ay.get(0)* ay.get(0)+ az.get(0)* az.get(0));

        Toast.makeText(MainActivity.this, "동작 판별"+SMV,Toast.LENGTH_SHORT).show();


        if (SMV <= 5) {
            pdTextView.setText("정지");
        }
        else if ((SMV<20) && (SMV>5)) {
            pdTextView.setText("이동중");
        }
        else if (SMV >=25) {
            Toast.makeText(MainActivity.this, "넘어짐",Toast.LENGTH_SHORT).show();

            Log.d("Accelerometer vector:", "" + SMV);
            degreeFloat = (float) (fusedOrientation[1] * 180 / Math.PI);
            degreeFloat2 = (float) (fusedOrientation[2] * 180 / Math.PI);
            if (degreeFloat < 0)
                degreeFloat = degreeFloat * -1;
            if (degreeFloat2 < 0)
                degreeFloat2 = degreeFloat2 * -1;
            if (degreeFloat > 30 || degreeFloat2 > 30) {
                Log.d("Degree1:", "" + degreeFloat);
                Log.d("Degree2:", "" + degreeFloat2);
                pdTextView.setText("넘어짐");
            }
            else{
                pdTextView.setText("추락");

            }
        }
        /*else if ((SMV >=25) && (GapAy*4>=GapAy+GapAz)) {
            pdTextView.setText("추락");
        }

         */



    }

}


/*

변화량이 큰데 y축 변화량이 훨씬크면 추락으로 판단

계산해보자
 */