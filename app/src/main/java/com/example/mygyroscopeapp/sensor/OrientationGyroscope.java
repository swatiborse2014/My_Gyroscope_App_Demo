package com.example.mygyroscopeapp.sensor;

import com.example.mygyroscopeapp.rotation.AngleUtils;
import com.example.mygyroscopeapp.rotation.RotationUtil;

import org.apache.commons.math3.complex.Quaternion;

public class OrientationGyroscope extends BaseFilter {

    private static final String TAG = OrientationGyroscope.class.getSimpleName();
    private static final float NS2S = 1.0f / 1000000000.0f;
    private static final float EPSILON = 0.000000001f;
    private Quaternion rotationVectorGyroscope;
    private float[] output;
    private long timestamp = 0;

    public OrientationGyroscope() {
        output = new float[3];
    }

    @Override
    public float[] getOutput() {
        return output;
    }

    public float[] calculateOrientation(float[] gyroscope, long timestamp) {
        if (isBaseOrientationSet()) {

            if (this.timestamp != 0) {
                final float dT = (timestamp - this.timestamp) * NS2S;
                rotationVectorGyroscope = RotationUtil.integrateGyroscopeRotation(rotationVectorGyroscope, gyroscope, dT, EPSILON);
                output = AngleUtils.getAngles(rotationVectorGyroscope.getQ0(), rotationVectorGyroscope.getQ1(), rotationVectorGyroscope.getQ2(), rotationVectorGyroscope.getQ3());
            }

            this.timestamp = timestamp;

            return output;
        } else {
            throw new IllegalStateException("You must call setBaseOrientation() before calling calculateFusedOrientation()!");
        }
    }

    public void setBaseOrientation(Quaternion baseOrientation) {
        rotationVectorGyroscope = baseOrientation;
    }

    public void reset() {
        rotationVectorGyroscope = null;
        timestamp = 0;
    }

    public boolean isBaseOrientationSet() {
        return rotationVectorGyroscope != null;
    }
}

