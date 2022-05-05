package com.apt.project.mean_shift.model;

import java.util.ArrayList;
import java.util.List;

public class PointsSoA<T> {
	private List<T> d1;
	private List<T> d2;
	private List<T> d3;
	
	public PointsSoA() {
		d1 = new ArrayList<>();
		d2 = new ArrayList<>();
		d3 = new ArrayList<>();
	}
	
	public PointsSoA(List<T> d1, List<T> d2, List<T> d3) {
		this.d1 = d1;
		this.d2 = d2;
		this.d3 = d3;
	}
	
	public void addAll(PointsSoA<T> points) {
		this.d1.addAll(points.d1);
		this.d2.addAll(points.d2);
		this.d3.addAll(points.d3);
	}
	
	public void addAll(int index, PointsSoA<T> points) {
		this.d1.addAll(index, points.d1);
		this.d2.addAll(index, points.d2);
		this.d3.addAll(index, points.d3);
	}

	public List<T> getD1() {
		return d1;
	}

	public void setD1(List<T> d1) {
		this.d1 = d1;
	}

	public List<T> getD2() {
		return d2;
	}

	public void setD2(List<T> d2) {
		this.d2 = d2;
	}

	public List<T> getD3() {
		return d3;
	}

	public void setD3(List<T> d3) {
		this.d3 = d3;
	}
	
	public int size() {
		return d1.size();
	}
}
