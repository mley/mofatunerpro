package com.m303.mofatunerpro;

/**
 * Created by mley on 17.06.16.
 */
public class LogValue {



    public final int millis;
    public final float throttle;
    public final float lambda;
    public final int rpm;

    public LogValue(int m, String csv, int minThrottle, int maxThrottle) {
        millis = m;

        String[] split = csv.split(",");

        int t = Integer.parseInt(split[0]);
        t = t - minThrottle;
        throttle = ((float)t)/((float)maxThrottle-minThrottle);

        lambda = 0.12f*(Float.parseFloat(split[1])/1024f*5f)+0.7f;

        rpm = Integer.parseInt(split[2]);

    }

    public LogValue(String s) {
        String[] split = s.split(",");

        millis = Integer.parseInt(split[0]);
        throttle = Float.parseFloat(split[1]);
        lambda = Float.parseFloat(split[2]);
        rpm = Integer.parseInt(split[3]);

    }

    public int getThrottlePercent() {
        return (int)(throttle*100);
    }

    public int getLambdaPercent() {
        // lambda zwischen 0.7 und 1.3
        return (int)((lambda-0.7)*200);
    }

    public int getRpmPercent() {
        return rpm/150; // max 15000 min^-1
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(millis).append(",").append(throttle).append(",").append(lambda).append(",").append(rpm);
        return sb.toString();
    }
}
