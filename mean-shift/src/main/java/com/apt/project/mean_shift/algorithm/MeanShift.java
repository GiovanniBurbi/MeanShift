package com.apt.project.mean_shift.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.apt.project.mean_shift.model.Point;
import com.apt.project.mean_shift.model.PointsSoA;

public class MeanShift {
	private static final Logger LOGGER = Logger.getLogger(MeanShift.class.getName());
	
	private final double kernelDen;
	private final int maxIter;
	private List<Point<Double>> originPoints;
	private PointsSoA<Double> originPointsSoA;
	
	public MeanShift(float bandwidth, int maxIter, List<Point<Double>> originPoints) {
		this.kernelDen = 2 * Math.pow(bandwidth, 2);
		this.maxIter = maxIter;
		this.originPoints = originPoints;
	}
	
	public MeanShift(float bandwidth, int maxIter, PointsSoA<Double> originPoints) {
		this.kernelDen = 2 * Math.pow(bandwidth, 2);
		this.maxIter = maxIter;
		this.originPointsSoA = originPoints;
	}

//	Method to calculate the euclidean distance power 2 between two 3D points
	private double euclideanDistancePow2(Point<Double> shiftPoint, Point<Double> originPoint) {
		double distX = Math.pow(shiftPoint.getD1() - originPoint.getD1(), 2);
		double distY = Math.pow(shiftPoint.getD2() - originPoint.getD2(), 2);
		double distZ = Math.pow(shiftPoint.getD3() - originPoint.getD3(), 2);
		return distX + distY + distZ;
	}
	
//	Method to calculate the euclidean distance power 2 between two 3D points
	private double euclideanDistancePow2(Double[] shiftPoint, Double[] originPoint) {
		double distX = Math.pow(shiftPoint[0] - originPoint[0], 2);
		double distY = Math.pow(shiftPoint[1] - originPoint[1], 2);
		double distZ = Math.pow(shiftPoint[2] - originPoint[2], 2);
		return distX + distY + distZ;
	}
	
//	Method to calculate the gaussian kernel given the distance between two 3D points
	private double kernel(double dist) {
		double pow = - (dist / (2 * kernelDen));
		return Math.exp(pow);
	}
	
//	Method to shift a 3D point based on the mean shift algorithm
	private Point<Double> shiftPoint(Point<Double> p) {
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
	
	private Double[] shiftPointSoA(Double x, Double y, Double z) {
		double shiftX = 0;
		double shiftY = 0;
		double shiftZ = 0;
		double scaleFactor = 0;
		Double[] p = new Double[]{x, y, z};
		
		for (int i = 0; i < originPointsSoA.size(); i++) {
			Double originX = originPointsSoA.getD1().get(i);
			Double originY = originPointsSoA.getD2().get(i);
			Double originZ = originPointsSoA.getD3().get(i);
			double dist = this.euclideanDistancePow2(p, new Double[]{originX,  originY, originZ});
			double weight = this.kernel(dist);
			
// 			numerator
			shiftX += originX * weight;
			shiftY += originY * weight;
			shiftZ += originZ * weight;

// 			denominator
			scaleFactor += weight;
		}
		
		if (scaleFactor == 0) {
			LOGGER.warning("Scale factor is zero!");
			return new Double[]{};
		}
		shiftX = shiftX / scaleFactor;
		shiftY = shiftY / scaleFactor;
		shiftZ = shiftZ / scaleFactor;

		return new Double[]{shiftX, shiftY, shiftZ};
	}
	
//	Method that applies the mean shift algorithm to a list of 3D points
	public List<Point<Double>> meanShiftAlgorithm() {
		ArrayList<Point<Double>> shiftedPoints = new ArrayList<>(originPoints.size());
		
		// deep copy of origin points
		for (Point<Double> point : originPoints) {
			shiftedPoints.add(new Point<>(point));
		}
		
		// algorithm main loop
		for(int i = 0; i < this.maxIter; i++) {
//			LOGGER.info("iterazione: " + i);
			for (int j = 0; j < shiftedPoints.size(); j++) {
				shiftedPoints.set(j, this.shiftPoint(shiftedPoints.get(j)));
			}
		}
		return shiftedPoints;
	}
	
//	Method that applies the mean shift algorithm to 3D points stored as a structure of arrays (SoA)
	public PointsSoA<Double> meanShiftAlgorithmSoA() {
		ArrayList<Double> shiftedX = new ArrayList<>(originPointsSoA.size());
		ArrayList<Double> shiftedY = new ArrayList<>(originPointsSoA.size());
		ArrayList<Double> shiftedZ = new ArrayList<>(originPointsSoA.size());
		
		// deep copy of origin points
		for (int i = 0; i < originPointsSoA.size(); i++) {
			shiftedX.add(originPointsSoA.getD1().get(i));
			shiftedY.add(originPointsSoA.getD2().get(i));
			shiftedZ.add(originPointsSoA.getD3().get(i));
		}
		
		// algorithm main loop
		for(int i = 0; i < this.maxIter; i++) {
			LOGGER.info("iterazione: " + i);
			for (int j = 0; j < originPointsSoA.size(); j++) {
				Double[] shiftedPoint = shiftPointSoA(shiftedX.get(j), shiftedY.get(j), shiftedZ.get(j));
				shiftedX.set(j, shiftedPoint[0]);
				shiftedY.set(j, shiftedPoint[1]);
				shiftedZ.set(j, shiftedPoint[2]);			
			}
		}

		return new PointsSoA<>(shiftedX, shiftedY, shiftedZ);
	}
}
