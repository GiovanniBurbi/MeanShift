package com.apt.project.mean_shift.parallel;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.apt.project.mean_shift.model.Point;

public class MeanShiftThread implements Runnable{
	
	private List<Point<Double>> shiftedPoints;
	private CountDownLatch latch;
	
	public MeanShiftThread(CountDownLatch latch) {
		// TODO initialize list with the size of the chuck
		// TODO copy chunk from origin and write it on local list (try directly on result list)
	}

	@Override
	public void run() {
		// TODO
		latch.countDown();
		
	}

}
