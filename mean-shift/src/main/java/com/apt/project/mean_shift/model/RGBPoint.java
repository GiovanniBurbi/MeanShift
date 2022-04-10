package com.apt.project.mean_shift.model;

public class RGBPoint {
	private int r;
	private int g;
	private int b;

	public RGBPoint(int r, int g, int b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}
	@Override
	public String toString() {
		return "RGBPoint [r=" + r + ", g=" + g + ", b=" + b + "]";
	}
}
