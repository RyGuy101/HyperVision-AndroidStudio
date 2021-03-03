package com.blogspot.mathjoy.hypervision;

public class Point {
    public static final int MAX_DIMENSIONS = 10;
    private double[] coords;
    private double displayXLeft;
    private double displayYLeft;
    private double displayPointRadiusLeft;
    private double displayXRight;
    private double displayYRight;
    private double displayPointRadiusRight;

    public Point(double[] coords) {
        if (coords.length > MAX_DIMENSIONS) {
            throw new NullPointerException();
        } else {
            this.coords = coords;
        }
    }

    public double getCoord(int i) {
        return coords[i];
    }

    public double[] getCoords() {
        return coords;
    }

    public void setCoord(int i, double coord) {
        coords[i] = coord;
    }

    public Point clone() {
        return new Point(coords.clone());
    }

    public void updateDisplayInfo4dLeft(double eyePosX, double eyePosZ, double camera4dPosW, double pointRadius) {
        double wMultiplier = camera4dPosW / (camera4dPosW - (coords[3] - 1));

        double realX = coords[0] * wMultiplier;
        double realY = coords[1] * wMultiplier;
        double realZ = coords[2] * wMultiplier;
        double realPointRadius = pointRadius * wMultiplier;

        double zMultiplier = eyePosZ / (eyePosZ - realZ);

        displayXLeft = (realX - eyePosX) * zMultiplier + eyePosX;
        displayYLeft = realY * zMultiplier;
        displayPointRadiusLeft = realPointRadius * zMultiplier;
    }

    public double getDisplayXLeft() {
        return displayXLeft;
    }

    public double getDisplayYLeft() {
        return displayYLeft;
    }

    public double getDisplayPointRadiusLeft() {
        return displayPointRadiusLeft;
    }

    public void updateDisplayInfo4dRight(double eyePosX, double eyePosZ, double camera4dPosW, double pointRadius) {
        double wMultiplier = camera4dPosW / (camera4dPosW - (coords[3] - 1));

        double realX = coords[0] * wMultiplier;
        double realY = coords[1] * wMultiplier;
        double realZ = coords[2] * wMultiplier;
        double realPointRadius = pointRadius * wMultiplier;

        double zMultiplier = eyePosZ / (eyePosZ - realZ);

        displayXRight = (realX - eyePosX) * zMultiplier + eyePosX;
        displayYRight = realY * zMultiplier;
        displayPointRadiusRight = realPointRadius * zMultiplier;
    }

    public double getDisplayXRight() {
        return displayXRight;
    }

    public double getDisplayYRight() {
        return displayYRight;
    }

    public double getDisplayPointRadiusRight() {
        return displayPointRadiusRight;
    }
}
