package com.apt.project.mean_shift.parallel;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.apt.project.mean_shift.algorithm.MeanShift;
import com.apt.project.mean_shift.model.Point;

public class MeanShiftThread implements Runnable{
	
	private CountDownLatch latch;
	private int startIndex;
	private int endIndex;
	private MeanShift meanShift;
	private List<Point<Double>> result;
	
	public MeanShiftThread(CountDownLatch latch, int startIndex, int endIndex, MeanShift meanShift, List<Point<Double>> result) {
		this.latch = latch;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.meanShift = meanShift;
		this.result = result;
	}

	@Override
	public void run() {
		List<Point<Double>> shiftedPoints = meanShift.meanShiftAlgorithmParallel(startIndex, endIndex);
		for (int i = startIndex; i < endIndex; i++) {
			result.get(i).replace(shiftedPoints.get(i-startIndex));
		}
		latch.countDown();
		
	}

}
