package com.apt.project.mean_shift.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.apt.project.mean_shift.model.Point;

public class MeanShift {
	private static final Logger LOGGER = Logger.getLogger(MeanShift.class.getName());
	
	private double kernelDen;
	private int maxIter;
	private List<Point<Double>> originPoints;
	
	public MeanShift(float bandwidth, int maxIter, List<Point<Double>> originPoints) {
		this.kernelDen = 2 * Math.pow(bandwidth, 2);
		this.maxIter = maxIter;
		this.originPoints = originPoints;
	}

	public double euclideanDistancePow2(Point<Double> shiftPoint, Point<Double> originPoint) {
		double distX = Math.pow(shiftPoint.getD1() - originPoint.getD1(), 2);
		double distY = Math.pow(shiftPoint.getD2() - originPoint.getD2(), 2);
		double distZ = Math.pow(shiftPoint.getD3() - originPoint.getD3(), 2);
		return distX + distY + distZ;
	}
	
	public double kernel(double dist) {
		double pow = - (dist / (2 * kernelDen));
		return Math.exp(pow);
	}
	
	public Point<Double> shiftPoint(Point<Double> p) {
		double shiftX = 0;
		double shiftY = 0;
		double shiftZ = 0;
		double scaleFactor = 0;
		
		for (Point<Double> originPoint : originPoints) {
			double dist = this.euclideanDistancePow2(p, originPoint);
			double weight = this.kernel(dist);
// numerator
			shiftX += originPoint.getD1() * weight;
			shiftY += originPoint.getD2() * weight;
			shiftZ += originPoint.getD3() * weight;
// denominator
			scaleFactor += weight;
		}
		
		if (scaleFactor == 0) {
			LOGGER.warning("Scale factor is zero!");
			return null; 
		}
		shiftX = shiftX / scaleFactor;
		shiftY = shiftY / scaleFactor;
		shiftZ = shiftZ / scaleFactor;

		return new Point<>(shiftX, shiftY, shiftZ);
	}
	
	public List<Point<Double>> meanShiftAlgorithm() {
		ArrayList<Point<Double>> shiftedPoints = new ArrayList<>();
// deep copy of origin points
		for (Point<Double> point : originPoints) {
			shiftedPoints.add(new Point<>(point));
		}
// algorithm main loop
		for(int i = 0; i < this.maxIter; i++) {
			LOGGER.info("iterazione: " + i);
			for (int j = 0; j < shiftedPoints.size(); j++) {
				shiftedPoints.get(j).replace(this.shiftPoint(shiftedPoints.get(j)));
			}
		}
		return shiftedPoints;
	}
}
