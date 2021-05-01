package com.example.mygyroscopeapp;

import androidx.appcompat.app.AppCompatActivity;


import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;

import com.example.mygyroscopeapp.sensor.FSensor;
import com.example.mygyroscopeapp.sensor.GyroscopeSensor;
import com.example.mygyroscopeapp.sensor.SensorSubject;
import com.example.mygyroscopeapp.view.GaugeRotation;

public class MainActivity extends AppCompatActivity {
    private float[] fusedOrientation = new float[3];
    protected Handler uiHandler;
    protected Runnable uiRunnable;
    private FSensor fSensor;
    private Dialog helpDialog;
    private GaugeRotation gaugeTiltCalibrated;

    private SensorSubject.SensorObserver sensorObserver = new SensorSubject.SensorObserver() {
        @Override
        public void onSensorChanged(float[] values) {
            updateValues(values);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        uiHandler = new Handler();
        uiRunnable = new Runnable() {
            @Override
            public void run() {
                uiHandler.postDelayed(this, 100);
                updateGauges();
            }
        };

        initUI();
    }

    private void initUI() {
        gaugeTiltCalibrated = findViewById(R.id.gauge_tilt_calibrated);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fSensor = new GyroscopeSensor(this);
        fSensor.register(sensorObserver);
        fSensor.start();
        uiHandler.post(uiRunnable);
    }

    @Override
    public void onPause() {
        if (helpDialog != null && helpDialog.isShowing()) {
            helpDialog.dismiss();
        }

        fSensor.unregister(sensorObserver);
        fSensor.stop();
        uiHandler.removeCallbacksAndMessages(null);

        super.onPause();
    }

    private void updateValues(float[] values) {
        fusedOrientation = values;
    }

    private void updateGauges() {
        gaugeTiltCalibrated.updateRotation(fusedOrientation[1], fusedOrientation[2]);
    }
}