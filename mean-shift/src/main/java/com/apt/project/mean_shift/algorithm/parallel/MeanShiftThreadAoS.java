package com.apt.project.mean_shift.algorithm.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import com.apt.project.mean_shift.model.Point;

public class MeanShiftThreadAoS implements Callable<List<Point<Double>>>{
	
	private static final Logger LOGGER = Logger.getLogger(MeanShiftThreadAoS.class.getName());
	
	private int tid;
	private int maxIter;
	private double kernelDen;
	private List<List<Point<Double>>> originPointsList;
	
	public MeanShiftThreadAoS(int tid, int maxIter, float bandwidth,
			List<List<Point<Double>>> originPointsList) {
		this.tid = tid;
		this.maxIter = maxIter;
		this.originPointsList = originPointsList;
		this.kernelDen = 2 * Math.pow(bandwidth, 2);
	}

	
	private double euclideanDistancePow2(Point<Double> shiftPoint, Point<Double> originPoint) {
		double distX = Math.pow(shiftPoint.getD1() - originPoint.getD1(), 2);
		double distY = Math.pow(shiftPoint.getD2() - originPoint.getD2(), 2);
		double distZ = Math.pow(shiftPoint.getD3() - originPoint.getD3(), 2);
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
		
		for(int i = 0; i < originPointsList.size(); i++) {
			List<Point<Double>> originPointChunk = originPointsList.get(i);
			for (Point<Double> point : originPointChunk) {
				double dist = this.euclideanDistancePow2(p, point);
				double weight = this.kernel(dist);
				
				// numerator
				shiftX += point.getD1() * weight;
				shiftY += point.getD2() * weight;
				shiftZ += point.getD3() * weight;
				
				// denominator
				scaleFactor += weight;
			}
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
	public List<Point<Double>> call() throws Exception {		
		
//		Get the list based on thread id
		List<Point<Double>> originPointsChunk = originPointsList.get(tid);
		int chunkSize = originPointsChunk.size();
		
//		Apply algorithm to chunk
		List<Point<Double>> shiftedPoints = new ArrayList<>(chunkSize);

//		deep copy of origin points
		for (int i = 0; i < chunkSize; i++) {
			shiftedPoints.add(new Point<>(originPointsChunk.get(i)));
		}

//		algorithm main loop
		for (int i = 0; i < this.maxIter; i++) {
//			LOGGER.info("iteration: " + i);
			for (int j = 0; j < chunkSize; j++) {
				shiftedPoints.set(j, this.shiftPoint(shiftedPoints.get(j)));
			}
		}
		
		return shiftedPoints;

	}

}
