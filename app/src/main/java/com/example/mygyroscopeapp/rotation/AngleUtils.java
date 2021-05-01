package com.example.mygyroscopeapp.rotation;

import android.util.Log;

public class AngleUtils {
    private static final String TAG = AngleUtils.class.getSimpleName();
    public static float[] getAngles(double w, double z, double x, double y) {
        double heading;
        double pitch;
        double roll;

        double test = x*y + z*w;
        if (test > 0.499) {
            heading = 2 * Math.atan2(x,w);
            pitch = -Math.PI/2;
            roll = 0;
            Log.e(TAG, "singularity at north pole");
            return new float[]{(float)heading, (float)pitch, (float)roll};
        }
        if (test < -0.499) {
            heading = -2 * Math.atan2(x,w);
            pitch = Math.PI/2;
            roll = 0;
            Log.e(TAG, "singularity at south pole");
            return new float[]{(float)heading, (float)pitch, (float)roll};
        }
        double sqx = x*x;
        double sqy = y*y;
        double sqz = z*z;
        heading = -Math.atan2(2*y*w-2*x*z , 1 - 2*sqy - 2*sqz);
        pitch = -Math.asin(2*test);
        roll = -Math.atan2(2*x*w-2*y*z , 1 - 2*sqx - 2*sqz);

        return new float[]{(float) heading, (float) pitch, (float) roll};
    }
}
