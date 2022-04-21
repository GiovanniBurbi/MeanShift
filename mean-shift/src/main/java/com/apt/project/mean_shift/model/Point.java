package com.apt.project.mean_shift.model;

public class Point<T> {
	private T d1;
	private T d2;
	private T d3;
	
	public Point(T d1, T d2, T d3) {
		super();
		this.d1 = d1;
		this.d2 = d2;
		this.d3 = d3;
	}
	public T getD1() {
		return d1;
	}
	public void setD1(T d1) {
		this.d1 = d1;
	}
	public T getD2() {
		return d2;
	}
	public void setD2(T d2) {
		this.d2 = d2;
	}
	public T getD3() {
		return d3;
	}
	public void setD3(T d3) {
		this.d3 = d3;
	}
	
	@Override
	public String toString() {
		return "Point [d1=" + d1 + ", d2=" + d2 + ", d3=" + d3 + "]";
	}
	
	public String toCSVString() {
		return d1 + "," + d2 + "," + d3 + "\n";
	}
	public void replace(Point<T> p) {
		this.d1 = p.d1;
		this.d2 = p.d2;
		this.d3 = p.d3;
	}
}