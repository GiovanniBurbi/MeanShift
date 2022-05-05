package com.apt.project.mean_shift.algorithm.parallel;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import com.apt.project.mean_shift.model.PointsSoA;

public class MeanShiftThreadSoA implements Callable<PointsSoA<Double>> {
	
private static final Logger LOGGER = Logger.getLogger(MeanShiftThreadSoA.class.getName());
	
	private int tid;
	private int nThreads;
	private int maxIter;
	private double kernelDen;
	private PointsSoA<Double> originPointsSoA;
	
		
	public MeanShiftThreadSoA(int tid, int nThreads, int maxIter, float bandwidth, PointsSoA<Double> originPointsSoA) {
		super();
		this.tid = tid;
		this.nThreads = nThreads;
		this.maxIter = maxIter;
		this.originPointsSoA = originPointsSoA;
		this.kernelDen = 2 * Math.pow(bandwidth, 2);
	}
	
	private double euclideanDistancePow2(Double pointX, Double pointY, Double pointZ, Double originX, Double originY, Double originZ) {
		double distX = Math.pow(pointX - originX, 2);
		double distY = Math.pow(pointY - originY, 2);
		double distZ = Math.pow(pointZ - originZ, 2);
		return distX + distY + distZ;
	}
	
	private double kernel(double dist) {
		double pow = - (dist / (2 * kernelDen));
		return Math.exp(pow);
	}
	
	private Double[] shiftPoint(Double pX, Double pY, Double pZ) {
		double shiftX = 0;
		double shiftY = 0;
		double shiftZ = 0;
		double scaleFactor = 0;
		
		for (int i = 0; i < originPointsSoA.size(); i++) {
			Double originX = originPointsSoA.getD1().get(i);
			Double originY = originPointsSoA.getD2().get(i);
			Double originZ = originPointsSoA.getD3().get(i);
			double dist = this.euclideanDistancePow2(pX, pY, pZ, originX,  originY, originZ);
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

	@Override
	public PointsSoA<Double> call() throws Exception {
//		Calculate the chunk for the thread. The chunks are evenly distributed between threads. max variance is 1
		int numberOfElements = originPointsSoA.size();
		
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
		
//		Apply algorithm to chunk
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
//			LOGGER.info("iteration: " + i);
			for (int j = 0; j < chunkSize; j++) {
				Double[] shiftedPoint = shiftPoint(shiftedX.get(j), shiftedY.get(j), shiftedZ.get(j));
				shiftedX.set(j, shiftedPoint[0]);
				shiftedY.set(j, shiftedPoint[1]);
				shiftedZ.set(j, shiftedPoint[2]);
			}
		}
		
		return new PointsSoA<>(shiftedX, shiftedY, shiftedZ);
	}

}
