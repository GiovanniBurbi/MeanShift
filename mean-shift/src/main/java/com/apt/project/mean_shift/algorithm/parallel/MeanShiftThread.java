package com.apt.project.mean_shift.algorithm.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.logging.Logger;

import com.apt.project.mean_shift.model.Point;
import com.apt.project.mean_shift.model.PointsSoA;

public class MeanShiftThread implements Runnable{
	
	private static final Logger LOGGER = Logger.getLogger(MeanShiftThread.class.getName());
	
	private int tid;
	private int nThreads;
	private int maxIter;
	private double kernelDen;
	private List<Point<Double>> originPoints;
	private PointsSoA<Double> originPointsSoA;
	private List<Point<Double>> resultPoints;
	private PointsSoA<Double> resultPointsSoA;
	private Phaser ph;
	private boolean isAoS;
	
	public MeanShiftThread(int tid, int nThreads, int maxIter, float bandwidth, List<Point<Double>> originPoints,
			List<Point<Double>> resultPoints, Phaser ph) {
		this.tid = tid;
		this.nThreads = nThreads;
		this.maxIter = maxIter;
		this.kernelDen = 2 * Math.pow(bandwidth, 2);
		this.originPoints = originPoints;
		this.resultPoints = resultPoints;
		this.ph = ph;
		this.isAoS = true;
		ph.register();
	}
	
	public MeanShiftThread(int tid, int nThreads, int maxIter, float bandwidth, PointsSoA<Double> originPoints,
			PointsSoA<Double> resultPoints, Phaser ph) {
		this.tid = tid;
		this.nThreads = nThreads;
		this.maxIter = maxIter;
		this.kernelDen = 2 * Math.pow(bandwidth, 2);
		this.originPointsSoA = originPoints;
		this.resultPointsSoA = resultPoints;
		this.ph = ph;
		this.isAoS = false;
		ph.register();
	}
		
	private double euclideanDistancePow2(Point<Double> shiftPoint, Point<Double> originPoint) {
		double distX = Math.pow(shiftPoint.getD1() - originPoint.getD1(), 2);
		double distY = Math.pow(shiftPoint.getD2() - originPoint.getD2(), 2);
		double distZ = Math.pow(shiftPoint.getD3() - originPoint.getD3(), 2);
		return distX + distY + distZ;
	}
	
	private double euclideanDistancePow2SoA(Double[] shiftPoint, Double[] originPoint) {
		double distX = Math.pow(shiftPoint[0] - originPoint[0], 2);
		double distY = Math.pow(shiftPoint[1] - originPoint[1], 2);
		double distZ = Math.pow(shiftPoint[2] - originPoint[2], 2);
		return distX + distY + distZ;
	}
	
	private double kernel(double dist) {
		double pow = - (dist / (2 * kernelDen));
		return Math.exp(pow);
	}
	
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
			double dist = this.euclideanDistancePow2SoA(p, new Double[]{originX,  originY, originZ});
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
	
	private void algorithmAoS(int chunkSize, int startChunk, int endChunk) {
		ArrayList<Point<Double>> shiftedPoints = new ArrayList<>(chunkSize);
//		deep copy of origin points
		for (int i = startChunk; i < endChunk; i++) {
			shiftedPoints.add(new Point<>(originPoints.get(i)));
		}

//		algorithm main loop
		for (int i = 0; i < this.maxIter; i++) {
//			LOGGER.info("iterazione: " + i);
			for (int j = 0; j < chunkSize; j++) {
				shiftedPoints.set(j, this.shiftPoint(shiftedPoints.get(j)));
//				When it's the last iteration of the algorithm store the points inside the shared list
				if (i == this.maxIter - 1) {
					resultPoints.set(startChunk + j, shiftedPoints.get(j));
				}
			}
		}
	}
	
	private void algorithmSoA(int chunkSize, int startChunk, int endChunk) {
		ArrayList<Double> shiftedX = new ArrayList<>(chunkSize);
		ArrayList<Double> shiftedY = new ArrayList<>(chunkSize);
		ArrayList<Double> shiftedZ = new ArrayList<>(chunkSize);
		
//		deep copy of origin points
		for (int i = startChunk; i < endChunk; i++) {
			shiftedX.add(originPointsSoA.getD1().get(i));
			shiftedY.add(originPointsSoA.getD2().get(i));
			shiftedZ.add(originPointsSoA.getD3().get(i));
		}
		
//		algorithm main loop
		for (int i = 0; i < this.maxIter; i++) {
//			LOGGER.info("iterazione: " + i);
			for (int j = 0; j < chunkSize; j++) {
				Double[] shiftedPoint = shiftPointSoA(shiftedX.get(j), shiftedY.get(j), shiftedZ.get(j));
				shiftedX.set(j, shiftedPoint[0]);
				shiftedY.set(j, shiftedPoint[1]);
				shiftedZ.set(j, shiftedPoint[2]);
//				When it's the last iteration of the algorithm store the points inside the shared list
				if (i == this.maxIter - 1) {
					resultPointsSoA.getD1().set(startChunk + j, shiftedPoint[0]);
					resultPointsSoA.getD2().set(startChunk + j, shiftedPoint[1]);
					resultPointsSoA.getD3().set(startChunk + j, shiftedPoint[2]);
				}
			}
		}
	}

	@Override
	public void run() {
//		Calculate the chunk for the thread. The chunks are evenly distributed between threads. max variance is 1
		int numberOfElements = isAoS ? originPoints.size() : originPointsSoA.size();
		
		int minElementsPerThread = numberOfElements / nThreads;
		int threadsWithMoreElements = numberOfElements - nThreads * minElementsPerThread;
		int chunkSize;
		int startChunk;
		if (tid < threadsWithMoreElements) {
			chunkSize = minElementsPerThread + 1;
			startChunk = tid * chunkSize;
		} else {
			chunkSize = minElementsPerThread;
			startChunk = threadsWithMoreElements * (chunkSize + 1) + (tid - threadsWithMoreElements) * chunkSize;
		}
		int endChunk = startChunk + chunkSize;
		
//		Apply one of the algorithms based on a flag defined in the constructor
		if (isAoS) algorithmAoS(chunkSize, startChunk, endChunk);
		else algorithmSoA(chunkSize, startChunk, endChunk);
		
//		Signal the completion of the work
		ph.arriveAndDeregister();
	}
}
