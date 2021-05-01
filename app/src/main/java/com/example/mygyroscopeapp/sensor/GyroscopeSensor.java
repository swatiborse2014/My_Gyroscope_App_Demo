package com.example.mygyroscopeapp.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.mygyroscopeapp.rotation.RotationUtil;

public class GyroscopeSensor implements FSensor {

    private static final String TAG =GyroscopeSensor.class.getSimpleName();

    private final SensorManager sensorManager;
    private final GyroscopeSensor.SimpleSensorListener listener;
    private float startTime = 0;
    private int count = 0;

    private float[] magnetic = new float[3];
    private float[] acceleration = new float[3];
    private float[] rotation = new float[3];
    private float[] output = new float[4];

    private OrientationGyroscope orientationGyroscope;

    private int sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
    private int sensorType = Sensor.TYPE_GYROSCOPE;

    private final SensorSubject sensorSubject;

    public GyroscopeSensor(Context context) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.listener = new GyroscopeSensor.SimpleSensorListener();
        this.sensorSubject = new SensorSubject();
        initializeFSensorFusions();
    }

    @Override
    public void start() {
        startTime = 0;
        count = 0;
        registerSensors(sensorDelay);
    }

    @Override
    public void stop() {
        unregisterSensors();
    }

    @Override
    public void register(SensorSubject.SensorObserver sensorObserver) {
        sensorSubject.register(sensorObserver);
    }

    @Override
    public void unregister(SensorSubject.SensorObserver sensorObserver) {
        sensorSubject.unregister(sensorObserver);
    }

    public void reset() {
        stop();
        magnetic = new float[3];
        acceleration = new float[3];
        rotation = new float[3];
        output = new float[4];
        listener.reset();
        start();
    }

    private float calculateSensorFrequency() {
        if (startTime == 0) {
            startTime = System.nanoTime();
        }

        long timestamp = System.nanoTime();

        return (count++ / ((timestamp - startTime) / 1000000000.0f));
    }

    private void initializeFSensorFusions() {
        orientationGyroscope = new OrientationGyroscope();
    }

    private void processAcceleration(float[] rawAcceleration) {
        System.arraycopy(rawAcceleration, 0, this.acceleration, 0, this.acceleration.length);
    }

    private void processMagnetic(float[] magnetic) {
        System.arraycopy(magnetic, 0, this.magnetic, 0, this.magnetic.length);
    }

    private void processRotation(float[] rotation) {
        System.arraycopy(rotation, 0, this.rotation, 0, this.rotation.length);
    }

    private void registerSensors(int sensorDelay) {

        orientationGyroscope.reset();

        // Register for sensor updates.
        sensorManager.registerListener(listener, sensorManager
                        .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                sensorDelay);

        // Register for sensor updates.
        sensorManager.registerListener(listener, sensorManager
                        .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                sensorDelay);

        // Register for sensor updates.
        sensorManager.registerListener(listener,
                sensorManager.getDefaultSensor(sensorType),
                sensorDelay);

    }

    private void unregisterSensors() {
        sensorManager.unregisterListener(listener);
    }

    private void setOutput(float[] value) {
        System.arraycopy(value, 0, output, 0, value.length);
        output[3] = calculateSensorFrequency();
        sensorSubject.onNext(output);
    }

    private class SimpleSensorListener implements SensorEventListener {

        private boolean hasAcceleration = false;
        private boolean hasMagnetic = false;

        private void reset() {
            hasAcceleration = false;
            hasMagnetic = false;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                processAcceleration(event.values);
                hasAcceleration = true;
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                processMagnetic(event.values);
                hasMagnetic = true;
            } else if (event.sensor.getType() == sensorType) {
                processRotation(event.values);

                if (!orientationGyroscope.isBaseOrientationSet()) {
                    if (hasAcceleration && hasMagnetic) {
                        orientationGyroscope.setBaseOrientation(RotationUtil.getOrientationVectorFromAccelerationMagnetic(acceleration, magnetic));
                    }
                } else {
                    setOutput(orientationGyroscope.calculateOrientation(rotation, event.timestamp));
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}



