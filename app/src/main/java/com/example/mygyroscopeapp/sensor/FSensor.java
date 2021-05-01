package com.example.mygyroscopeapp.sensor;

public interface FSensor {
    void register(SensorSubject.SensorObserver sensorObserver);
    void unregister(SensorSubject.SensorObserver sensorObserver);

    void start();
    void stop();
    void reset();
}
