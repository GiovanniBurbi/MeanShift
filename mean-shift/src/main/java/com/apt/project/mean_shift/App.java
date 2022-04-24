package com.apt.project.mean_shift;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.apt.project.mean_shift.algorithm.MeanShift;
import com.apt.project.mean_shift.algorithm.parallel.MeanShiftThread;
import com.apt.project.mean_shift.model.Point;
import com.apt.project.mean_shift.utils.ColorConverter;
import com.apt.project.mean_shift.utils.ImageParser;

public class App 
{
	private static final Logger LOGGER = Logger.getLogger(App.class.getName());
	private static final int ITER = 1;
	private static final int N_THREAD = 2;
	private static final float BANDWIDTH = 12f;
//	private static final float BANDWIDTH_CSV = 2f;
	private static final int ALGORITHM_ITER = 5;

	
	public static void main( String[] args )
    {
    	
//    	For images
		
		ImageParser ip = new ImageParser("/images/benzina.jpg");
    	List<Point<Integer>> rgbPoints = ip.extractRGBPoints();
    	List<Point<Double>> luvPoints = ColorConverter.convertToLUVPoints(rgbPoints);
    	List<Point<Integer>> rgbShiftedPoints = null;
    	long allTimes = 0;
    	
    	
//    	Sequential version
    	
    	MeanShift meanShift = new MeanShift(BANDWIDTH, ALGORITHM_ITER, luvPoints);
    	for (int i = 0; i < ITER; i++) {
    		long startTime = System.currentTimeMillis();
    		List<Point<Double>> shiftedPoints = meanShift.meanShiftAlgorithm();
    		rgbShiftedPoints = ColorConverter.convertToRGBPoints(shiftedPoints);
    		long endTime = System.currentTimeMillis();
    		allTimes += endTime - startTime;
    	}
    	LOGGER.info("Sequential version took " + (allTimes / ITER) + " milliseconds");
    	ip.renderImage(rgbShiftedPoints, "results/resultBenzinaBW12Iter5.jpg");
		
		
//		Parallel version
		
    	
    	allTimes = 0;

    	for (int k = 0; k < ITER; k++) {
    		long startTime = System.currentTimeMillis();
    		ExecutorService executor = Executors.newFixedThreadPool(N_THREAD);
        	CountDownLatch latch = new CountDownLatch(N_THREAD);

    		rgbShiftedPoints = new ArrayList<>();
    		for (int i = 0; i < luvPoints.size(); i++) {
        		rgbShiftedPoints.add(new Point<>(0, 0, 0));
        	}
    		
			for (int i = 0; i < N_THREAD; i++) {
				executor.execute(new MeanShiftThread(i, N_THREAD, ALGORITHM_ITER, BANDWIDTH, luvPoints, rgbShiftedPoints, latch));
			}
			
			try {
				latch.await();
			} catch (InterruptedException e) {
				LOGGER.log(Level.WARNING, e.getMessage(), e);
				Thread.currentThread().interrupt();
			}
			
			executor.shutdown();
			try {
			    if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
			    	LOGGER.info("Executor timeout up, now forcing the shutdown...");
			        executor.shutdownNow();
			    } 
			} catch (InterruptedException e) {
				LOGGER.info("Executor shutdown exception, now forcing the shutdown...");
			    executor.shutdownNow();
			    Thread.currentThread().interrupt();
			}

			long endTime = System.currentTimeMillis();
			allTimes += endTime - startTime;
    	}

    	LOGGER.info("Parallel version took " + (allTimes / ITER) + " milliseconds");
    	ip.renderImage(rgbShiftedPoints, "results/resultBenzinaBW12Iter5Parallel.jpg");
    	
    	
    	
    	
//    	For csv files
    	
//		CSVParser p = new CSVParser();
//    	p.fetchCSVFile("/points/10000.csv");
//    	List<Point<Double>> points = p.extractPoints();    	
//    	List<Point<Double>> shiftedPoints = null;
//    	long allTimes = 0;
    	
    	
    	
//    	Sequential version
    	

//    	MeanShift meanShift = new MeanShift(BANDWIDTH_CSV, ALGORITHM_ITER, points);
//    	for (int i = 0; i < ITER; i++) {
//    		long startTime = System.currentTimeMillis();
//    		shiftedPoints = meanShift.meanShiftAlgorithm();
//    		long endTime = System.currentTimeMillis();
//    		allTimes += endTime - startTime;
//    	}
//    	LOGGER.info("Sequential version took " + (allTimes / ITER) + " milliseconds");
//    	p.write(shiftedPoints, "shiftedPoints.csv");
    	
    	
//    	Parallel version
    	
    	
//    	allTimes = 0;//
//    	for (int k = 0; k < ITER; k++) {
//    		long startTime = System.currentTimeMillis();
//    		ExecutorService executor = Executors.newFixedThreadPool(N_THREAD);
//        	CountDownLatch latch = new CountDownLatch(N_THREAD);
//
//    		shiftedPoints = new ArrayList<>();
//    		for (int i = 0; i < points.size(); i++) {
//        		shiftedPoints.add(new Point<>(0.0, 0.0, 0.0));
//        	}
//    		
//			for (int i = 0; i < N_THREAD; i++) {
//				executor.execute(new MeanShiftThread(i, N_THREAD, ALGORITHM_ITER, BANDWIDTH_CSV, points, shiftedPoints, latch));
//			}
//			
//			try {
//				latch.await();
//			} catch (InterruptedException e) {
//				LOGGER.log(Level.WARNING, e.getMessage(), e);
//				Thread.currentThread().interrupt();
//			}
//			
//			executor.shutdown();
//			try {
//			    if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
//			    	LOGGER.info("Executor timeout up, now forcing the shutdown..");
//			        executor.shutdownNow();
//			    } 
//			} catch (InterruptedException e) {
//				LOGGER.info("Executor shutdown exception, now forcing the shutdown..");
//			    executor.shutdownNow();
//			    Thread.currentThread().interrupt();
//			}
//
//			long endTime = System.currentTimeMillis();
//			allTimes += endTime - startTime;
//    	}
//
//    	LOGGER.info("Parallel version took " + (allTimes / ITER) + " milliseconds");
//    	p.write(shiftedPoints, "shiftedPoints.csv");    	
    }
	
}
