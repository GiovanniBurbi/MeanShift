package com.apt.project.mean_shift.algorithm.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.logging.Logger;

import com.apt.project.mean_shift.model.Point;
import com.apt.project.mean_shift.utils.ColorConverter;

public class MeanShiftThread implements Runnable{
	
	private static final Logger LOGGER = Logger.getLogger(MeanShiftThread.class.getName());
	
	private int tid;
	private int nThreads;
	private int maxIter;
	private double kernelDen;
	private List<Point<Double>> originPoints;
	private List<Point<Integer>> finalPoints;
	private Phaser ph;
	
	public MeanShiftThread(int tid, int nThreads, int maxIter, float bandwidth, List<Point<Double>> originPoints,
			List<Point<Integer>> finalPoints, Phaser ph) {
		this.tid = tid;
		this.nThreads = nThreads;
		this.maxIter = maxIter;
		this.kernelDen = 2 * Math.pow(bandwidth, 2);
		this.originPoints = originPoints;
		this.finalPoints = finalPoints;
		this.ph = ph;
		ph.register();
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

	@Override
	public void run() {
		int numberOfElements = originPoints.size();
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
		
		ArrayList<Point<Double>> shiftedPoints = new ArrayList<>(chunkSize);
//		deep copy of origin points
		for (int i = startChunk; i < endChunk; i++) {
			shiftedPoints.add(new Point<>(originPoints.get(i)));
		}
//		algorithm main loop
		
		for (int i = 0; i < this.maxIter; i++) {
			LOGGER.info("iterazione: " + i);
			for (int j = 0; j < chunkSize; j++) {
				Point<Double> point = shiftedPoints.get(j);
				point.replace(this.shiftPoint(point));
			}
		}
				
		for (int i = startChunk; i < endChunk; i++) {
			finalPoints.get(i).replace(ColorConverter.convertToRGBPoint(shiftedPoints.get(i-startChunk)));
		}
		
		ph.arriveAndDeregister();
	}

}
