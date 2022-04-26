package com.apt.project.mean_shift.model;

import java.util.List;

public class PointsSoA<T> {
	private List<T> d1;
	private List<T> d2;
	private List<T> d3;
	
	public PointsSoA() {}
	
	public PointsSoA(List<T> d1, List<T> d2, List<T> d3) {
		this.d1 = d1;
		this.d2 = d2;
		this.d3 = d3;
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
