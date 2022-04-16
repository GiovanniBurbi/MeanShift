package com.apt.project.mean_shift.algorithm;

import java.util.ArrayList;

import com.apt.project.mean_shift.model.Point;

public class MeanShift {
	
	private float bandwidth;
	private int maxIter;
	private ArrayList<Point> originPoints;
	
	public MeanShift(float bandwidth, int maxIter, ArrayList<Point> originPoints) {
		this.bandwidth = bandwidth;
		this.maxIter = maxIter;
		this.originPoints = originPoints;
	}

	public double euclideanDistancePow2(Point shiftPoint, Point originPoint) {
		double distX = Math.pow(shiftPoint.getX() - originPoint.getX(), 2);
		double distY = Math.pow(shiftPoint.getY() - originPoint.getY(), 2);
		double distZ = Math.pow(shiftPoint.getZ() - originPoint.getZ(), 2);
		return distX + distY + distZ;
	}
	
	public double kernel(double dist) {
		double den = 2 * Math.pow(bandwidth, 2);
		double pow = - (dist / (2 * den));
		return Math.exp(pow);
	}
	
	public Point shiftPoint(Point p) {
		double shiftX = 0;
		double shiftY = 0;
		double shiftZ = 0;
		double scaleFactor = 0;
		
		for (int i = 0; i < originPoints.size(); i++) {
			Point originPoint = originPoints.get(i);
			double dist = this.euclideanDistancePow2(p, originPoint);
			double weight = this.kernel(dist);
//			numerator
			shiftX += originPoint.getX() * weight;
			shiftY += originPoint.getY() * weight;
			shiftZ += originPoint.getZ() * weight;
//			denominator
			scaleFactor += weight;
		}
		
		if (scaleFactor == 0) {
			System.out.println("Scale factor is zero!");
			return null; 
		}
		shiftX = shiftX / scaleFactor;
		shiftY = shiftY / scaleFactor;
		shiftZ = shiftZ / scaleFactor;

		return new Point(shiftX, shiftY, shiftZ);
	}
	
	public ArrayList<Point> meanShiftAlgorithm() {
		ArrayList<Point> shiftedPoints = new ArrayList<>(originPoints);
		for(int i = 0; i < this.maxIter; i++) {
			for (int j = 0; j < shiftedPoints.size(); j++) {
				shiftedPoints.get(j).replace(this.shiftPoint(shiftedPoints.get(j)));
			}
		}
		return shiftedPoints;
	}
}
