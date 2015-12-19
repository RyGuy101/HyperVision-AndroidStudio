package com.blogspot.mathjoy.hypervision;

public class Point {
	public static final int MAX_DIMENSIONS = 10;
	private double[] coords;

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
}
