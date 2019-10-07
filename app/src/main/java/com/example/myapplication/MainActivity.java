package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import static android.hardware.Sensor.TYPE_ALL;

public class MainActivity extends AppCompatActivity {
    //sensor manager
    private SensorManager mSManager;
    public List<Sensor> listofSensor;
    public BatteryManager mBManager;

    //views
    private TextView DebugInfoText;
    //private LinearLayout fstLayout;
    private TextView fstLayoutType;
    private TextView tAccelerationData;
    String strAccelerationData;
    private TextView sndLayoutType;
    private TextView tLightSensorData;
    private TextView trdLayoutType;
    private TextView tProximityData;
    String strGyroscopeData;
    private TextView fourthLayoutType;
    private TextView tGyroscopeData;
    String strRotationData;
    private TextView fifthLayoutType;
    private TextView tRotationData;

    private TextView sixthLayoutType;
    private TextView tBatteryData;

    //color for multiple chart
    private final int[] colors = new int[] {
            ColorTemplate.VORDIPLOM_COLORS[0],
            ColorTemplate.VORDIPLOM_COLORS[1],
            ColorTemplate.VORDIPLOM_COLORS[2]
    };
    //charts
    private LineChart AccelerationChart_0,AccelerationChart_1,AccelerationChart_2;
    private ArrayList<Entry> accXData,accYData,accZData;
    //Acceleration usually refresh to fast to show on the chart,set a count to delay the refresh
    private int nAccDelay = 0;
    private LineChart LightChart;
    private ArrayList<Entry> lightSensorValues;
    private LineChart GyroscopeChart_0,GyroscopeChart_1,GyroscopeChart_2;
    private ArrayList<Entry> gyroXData,gyroYData,gyroZData;
    private LineChart RotationChart_0,RotationChart_1,RotationChart_2;
    private ArrayList<Entry> rtXData,rtYData,rtZData;
    //rotation delay
    private int nRtDelay = 0;


    //debug flag
    //static boolean IfDebug = false;
    private CheckBox checkIfDebug;
    //int ntype;


    //information
    String strSensorList = "Sensor List:\n";
    int nSensorCount = 0;
    //to deal with some phone have 2 similar sensor object
    int nWakeupFlag = 0;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //find views
        checkIfDebug = findViewById(R.id.checkIfDebug);
        checkIfDebug.setChecked(true);
        DebugInfoText = findViewById(R.id.teststring);
        fstLayoutType = findViewById(R.id.firstLayoutType);
        tAccelerationData = findViewById(R.id.accelerationData);
        sndLayoutType = findViewById(R.id.secondLayoutType);
        tLightSensorData = findViewById(R.id.lightSensorData);
        trdLayoutType = findViewById(R.id.trdLayoutType);
        tProximityData = findViewById(R.id.proximityData);
        fourthLayoutType = findViewById(R.id.fourthLayoutType);
        tGyroscopeData = findViewById(R.id.gyroscopeData);
        sixthLayoutType = findViewById(R.id.sixthLayoutType);
        tBatteryData = findViewById(R.id.batterySensors);
        fifthLayoutType = findViewById(R.id.fifthLayoutType);
        tRotationData = findViewById(R.id.rotationData);

        //chart colors
        //charts
        LightChart = findViewById(R.id.chart1);
        lightSensorValues = new ArrayList<>();
        AccelerationChart_0 = findViewById(R.id.accelerationChart_0);
        AccelerationChart_1 = findViewById(R.id.accelerationChart_1);
        AccelerationChart_2 = findViewById(R.id.accelerationChart_2);
        accXData = new ArrayList<>();
        accYData = new ArrayList<>();
        accZData = new ArrayList<>();
        GyroscopeChart_0 = findViewById(R.id.gyroscopeChart_0);
        GyroscopeChart_1 = findViewById(R.id.gyroscopeChart_1);
        GyroscopeChart_2 = findViewById(R.id.gyroscopeChart_2);
        gyroXData = new ArrayList<>();
        gyroYData = new ArrayList<>();
        gyroZData = new ArrayList<>();
        RotationChart_0 = findViewById(R.id.rotationChart_0);
        RotationChart_1 = findViewById(R.id.rotationChart_1);
        RotationChart_2 = findViewById(R.id.rotationChart_2);
        rtXData = new ArrayList<>();
        rtYData = new ArrayList<>();
        rtZData = new ArrayList<>();

        //set listener for debug control
        checkIfDebug.setChecked(false);
        ShowDebugInfo();
        checkIfDebug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowDebugInfo();
            }
        });

        //init sensor manager
        mSManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        listofSensor = mSManager.getSensorList(TYPE_ALL);
        mBManager = (BatteryManager) getSystemService(BATTERY_SERVICE);

        //show sensors
        for(Sensor sensor : listofSensor)
        {
            if(sensor.getStringType() != null) strSensorList = strSensorList + sensor.getStringType() + "\t";
            if(sensor.getName() != null) strSensorList = strSensorList + "Detail: "+ sensor.getName() + "\n" + "------------------------------\n";
            if(sensor.getName().indexOf("WAKE_UP") > 0) nWakeupFlag = 1;
            nSensorCount++;
        }
        ShowDebugInfo();

    //1st sensor : acceleration sensor
        //chart
        InitChart(AccelerationChart_0,20,5);
        InitChart(AccelerationChart_1,20,5);
        InitChart(AccelerationChart_2,20,5);

        fstLayoutType.setText("Acceleration Sensor: (this sensor refresh to quickly to show on the chart so I give it a delay)");
        //set the listener of acceleration sensor
        SensorEventListener SEVforAS = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                strAccelerationData = "X: " + sensorEvent.values[0]
                        + "\nY: "+ sensorEvent.values[1]
                        + "\nZ: "+ sensorEvent.values[2]  + "\n";
                tAccelerationData.setText(strAccelerationData);
                if(nAccDelay < 5 && accXData.size() != 0){
                    nAccDelay++;
                }else {
                    nAccDelay = 0;
                    SetChartData(AccelerationChart_0, accXData, sensorEvent.values[0], 1, colors[0], "Acc X",0,true);
                    SetChartData(AccelerationChart_1, accYData, sensorEvent.values[1], 1, colors[1], "Acc Y",0,true);
                    SetChartData(AccelerationChart_2, accZData, sensorEvent.values[2], 1, colors[2], "Acc Z",0,true);
                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        //find the acceleration sensor
        //!!!!!!!!!
        //Notice: the acceleration sensor can be registered
        // as "TYPE_LINEAR_ACCELERATION" or "TYPE_ACCELEROMETER"
        Sensor accelerationSensor;
        for(Sensor sensor :listofSensor)
        {
            if(sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION || sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerationSensor = sensor;
                mSManager.registerListener(SEVforAS,accelerationSensor,SensorManager.SENSOR_DELAY_NORMAL);
                break;
            }
        }

    //2nd sensor: light sensor
        sndLayoutType.setText("Light Sensor");
        //set chart
        InitChart(LightChart,0,0);

        //set the listener of light sensor
        SensorEventListener SEVforLS = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                tLightSensorData.setText("Light: " + sensorEvent.values[0]);
                SetChartData(LightChart, lightSensorValues,sensorEvent.values[0],10,Color.RED,"Light Value",20,true);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        Sensor lightSensor;
        for(Sensor sensor :listofSensor)
        {
            if(sensor.getType() == Sensor.TYPE_LIGHT) {
                lightSensor = sensor;
                mSManager.registerListener(SEVforLS,lightSensor,SensorManager.SENSOR_DELAY_NORMAL);
                break;
            }
        }


        //3rd sensor : Proximity Sensor
        trdLayoutType.setText("Proximity Sensor");
        //set the listener of Orientation sensor
        SensorEventListener SEVforPS = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                tProximityData.setText("Proximity:" + sensorEvent.values[0] + "cm");
                if(sensorEvent.values[0] == 0.0) tProximityData.setTextColor(0xffff0000);
                if(sensorEvent.values[0] != 0.0) tProximityData.setTextColor(0xff0000ff);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        Sensor proximitySensor;
        for(Sensor sensor :listofSensor)
        {
            if(sensor.getType() == Sensor.TYPE_PROXIMITY) {
                proximitySensor = sensor;
                mSManager.registerListener(SEVforPS,proximitySensor,SensorManager.SENSOR_DELAY_NORMAL);
                break;
            }
        }

        //4th sensor : gyroscope sensor
        //chart
        InitChart(GyroscopeChart_0,0,0);
        InitChart(GyroscopeChart_1,0,0);
        InitChart(GyroscopeChart_2,0,0);

        fourthLayoutType.setText("Gyroscope Sensor:");
        //set the listener of gyroscope sensor
        SensorEventListener SEVforGY = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                strGyroscopeData = "X: " + sensorEvent.values[0]
                        + "\nY: "+ sensorEvent.values[1]
                        + "\nZ: "+ sensorEvent.values[2]  + "\n";
                tGyroscopeData.setText(strGyroscopeData);
                SetChartData(GyroscopeChart_0, gyroXData, sensorEvent.values[0], 1, colors[0], "Acc X",0,true);
                SetChartData(GyroscopeChart_1, gyroYData, sensorEvent.values[1], 1, colors[1], "Acc Y",0,true);
                SetChartData(GyroscopeChart_2, gyroZData, sensorEvent.values[2], 1, colors[2], "Acc Z",0,true);

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        //find the gyroscope sensor
        Sensor gyroscopeSensor;
        for(Sensor sensor :listofSensor)
        {
            if(sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                gyroscopeSensor = sensor;
                mSManager.registerListener(SEVforGY,gyroscopeSensor,SensorManager.SENSOR_DELAY_NORMAL);
                break;
            }
        }

        //5th sensor : rotation sensor
        //chart
        InitChart(RotationChart_0,0,0);
        InitChart(RotationChart_1,0,0);
        InitChart(RotationChart_2,0,0);

        fifthLayoutType.setText("Rotation Sensor: (this sensor refresh to quickly to show on the chart so I give it a delay)");
        //set the listener of rotation sensor
        SensorEventListener SEVforRS = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                strRotationData = "X: " + sensorEvent.values[0]
                        + "\nY: "+ sensorEvent.values[1]
                        + "\nZ: "+ sensorEvent.values[2]
                        + "\ncos(θ/2): " + sensorEvent.values[3]
                        + "\nestimated heading Accuracy: " + sensorEvent.values[4]
                        + "\n(in radians) (-1 if unavailable)\n";
                tRotationData.setText(strRotationData);
                if(nRtDelay < 5 && rtXData.size() != 0){
                    nRtDelay++;
                }else {
                    nRtDelay = 0;
                    SetChartData(RotationChart_0, rtXData, sensorEvent.values[0], 1, colors[0], "Acc X",50,false);
                    SetChartData(RotationChart_1, rtYData, sensorEvent.values[1], 1, colors[1], "Acc Y",50,false);
                    SetChartData(RotationChart_2, rtZData, sensorEvent.values[2], 1, colors[2], "Acc Z",50,false);
                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        Sensor rotationSensor;
        for(Sensor sensor :listofSensor)
        {
            if(sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                rotationSensor = sensor;
                mSManager.registerListener(SEVforRS,rotationSensor,SensorManager.SENSOR_DELAY_NORMAL);
                break;
            }
        }

        //sensors about battery manage
        sixthLayoutType.setText("Battery Manager and Sensors");
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(Intent.ACTION_BATTERY_CHANGED);
        String strBatteryState;
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String strBattery;
                //接收到广播后，用getIntExtra("level")和getIntExtra("scale")获得相应值
                strBattery = "Sensors:-----------------\n";
                strBattery += "Battery remaining: " + intent.getIntExtra("level", 0);    ///电池剩余电量
                strBattery += "%";
                int nScale = intent.getIntExtra("scale", 0);  ///获取电池满电量数值
                if (nScale != 100) strBattery += "\nTotal capability: " + nScale;
                strBattery +="\nvoltage: " + intent.getIntExtra("voltage", 0);  ///获取电池电压
                strBattery += " mV";
                int nTemperature = intent.getIntExtra("temperature", 0);  ///获取电池温度
                double dTemperature = nTemperature / 10.0;
                strBattery += "\ntemperature: " + dTemperature + "℃\n";
                strBattery += "More Info:------------------\n";
                strBattery += "technology: " + intent.getStringExtra("technology");  ///获取电池技术支持
                strBattery += "\nstatus: ";
                int nStatus = intent.getIntExtra("status",BatteryManager.BATTERY_STATUS_UNKNOWN); ///获取电池状态
                String[] strStatus = {"NULL","Unknown","Charging","DisCharging","Not charging","Full"};
                strBattery += strStatus[nStatus] + "\n";
                int nPlugged = intent.getIntExtra("plugged", 0);  ///获取电源信息
                String[] strPlugged = {"NULL","AC","USB","NULL","Wireless"};
                strBattery += "Power source: " + strPlugged[nPlugged] + "\n";
                int nHealth = intent.getIntExtra("health",BatteryManager.BATTERY_HEALTH_UNKNOWN);  ///获取电池健康度
                String[] strHealth = {"NULL","Unknown","Good","Overheat","Dead","Unspecified Failure","Cold"};
                strBattery += "Battery health: " + strHealth[nHealth] + "\n";
                tBatteryData.setText(strBattery);
            }
        };
        registerReceiver(receiver,filter2);


    }

    //as this part has been used twice, I block it in a function
    public void ShowDebugInfo(){
        if(checkIfDebug.isChecked()) DebugInfoText.setText("This device has" + nSensorCount + " sensors\n" + strSensorList);
        else{
            String strSensorCount = "This device has" + nSensorCount + " sensors\n";
            DebugInfoText.setText(strSensorCount);
        }
    }
    public void InitChart(LineChart chart, float Max, float Min)
    {
        YAxis accy = chart.getAxisLeft();
        chart.getAxisRight().setEnabled(false);
        chart.setTouchEnabled(false);
        accy.enableGridDashedLine(10f, 10f, 0f);
        accy.setAxisMaximum(Max);
        accy.setAxisMinimum(Min);
    }
    /*  Function Name: SetData
        Usage: Refresh the Chart with new data
        parameters:
            chart, the chart need to be refreshed
            Values, the ArrayList of the Chart
            value, the value need to be add in the chart
            nExpend, expend the Axis range of max and min value in the chart
            color, the color of the line in the chart( can be null if not at the initialize time
            label, the label of dataset( can be null if not at the initialize time
            nClearSize, when number of data in the list reach the size, clear the list
            isShowValueText, show value on the line or not
     ************************************************/
    public float SetChartData(LineChart chart, ArrayList<Entry> Values, float value, float nExpend, int color, String label,int nClearSize, boolean isShowValueText)
    {
        if(nClearSize == 0) nClearSize = 20;
        if (Values.size() > nClearSize)
        {
            Values.clear();
            YAxis lighty = chart.getAxisLeft();
            lighty.setAxisMaximum(0);
            lighty.setAxisMinimum(0);
        }
        Values.add(new Entry(Values.size(),value));
        LineDataSet set1;
        if (chart.getData() != null &&
                chart.getData().getDataSetCount() >= 0) {
            set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(Values);
            set1.notifyDataSetChanged();
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            if(color == 0) color = Color.BLACK;
            if(label.isEmpty()) label = "text";
            set1 = new LineDataSet(Values, label);
            set1.setDrawIcons(false);
            set1.setCircleColor(color);
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            if(isShowValueText == false) set1.setValueTextSize(0);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            LineData lightData;
            lightData = new LineData(dataSets);
            chart.setData(lightData);
        }
        //set Max and Min Y value
        YAxis lighty = chart.getAxisLeft();
        float nMax = lighty.getAxisMaximum();
        float nMin = lighty.getAxisMinimum();
        if(nMax == 0 && nMin == 0){
            lighty.setAxisMaximum(value + nExpend);
            lighty.setAxisMinimum(value - nExpend);
        }else {
            if (value > nMax) {
                lighty.setAxisMaximum(value + nExpend);
            }
            if (value < nMin) {
                lighty.setAxisMinimum(value - nExpend);
            }
        }
        //redraw
        chart.invalidate();
        return nMax;
    }

}
