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
	
	public String toCSVString() {
		return r + "," + g + "," + b + "\n";
	}

	public int getR() {
		return r;
	}
	public void setR(int r) {
		this.r = r;
	}
	public int getG() {
		return g;
	}
	public void setG(int g) {
		this.g = g;
	}
	public int getB() {
		return b;
	}
	public void setB(int b) {
		this.b = b;
	}
	
	
}
