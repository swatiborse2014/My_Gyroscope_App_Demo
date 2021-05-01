package com.example.mygyroscopeapp.rotation;

import android.hardware.SensorManager;
import android.renderscript.Matrix3f;


import org.apache.commons.math3.complex.Quaternion;

import java.util.Arrays;

public class RotationUtil {

    public static Quaternion integrateGyroscopeRotation(Quaternion previousRotationVector, float[] rateOfRotation, float dt, float epsilon) {
        float magnitude = (float) Math.sqrt(Math.pow(rateOfRotation[0], 2)
                + Math.pow(rateOfRotation[1], 2) + Math.pow(rateOfRotation[2], 2));

        if (magnitude > epsilon) {
            rateOfRotation[0] /= magnitude;
            rateOfRotation[1] /= magnitude;
            rateOfRotation[2] /= magnitude;
        }

        float thetaOverTwo = magnitude * dt / 2.0f;
        float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);

        double[] deltaVector = new double[4];

        deltaVector[0] = sinThetaOverTwo * rateOfRotation[0];
        deltaVector[1] = sinThetaOverTwo * rateOfRotation[1];
        deltaVector[2] = sinThetaOverTwo * rateOfRotation[2];
        deltaVector[3] = cosThetaOverTwo;

        return previousRotationVector.multiply(new Quaternion(deltaVector[3], Arrays.copyOfRange(
                deltaVector, 0, 3)));
    }

    public static Quaternion getOrientationVectorFromAccelerationMagnetic(float[] acceleration, float[] magnetic) {
        float[] rotationMatrix = new float[9];
        if (SensorManager.getRotationMatrix(rotationMatrix, null, acceleration, magnetic)) {
            double[] rotation = getQuaternion(new Matrix3f(rotationMatrix));
            return new Quaternion(rotation[0], rotation[1], rotation[2], rotation[3]);
        }

        return null;
    }

    private static double[] getQuaternion(Matrix3f m1) {
        double w = Math.sqrt(1.0 + m1.get(0,0) + m1.get(1,1) + m1.get(2,2)) / 2.0;
        double w4 = (4.0 * w);
        double x = (m1.get(2,1) - m1.get(1,2)) / w4 ;
        double y = (m1.get(0,2) - m1.get(2,0)) / w4 ;
        double z = (m1.get(1,0) - m1.get(0,1)) / w4 ;

        return new double[]{w,x,y,z};
    }

}
