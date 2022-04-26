package com.apt.project.mean_shift;

import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.apt.project.mean_shift.algorithm.parallel.MeanShiftThread;
import com.apt.project.mean_shift.model.Point;
import com.apt.project.mean_shift.model.PointsSoA;
import com.apt.project.mean_shift.utils.ImageParser;

public class App 
{
	private static final Logger LOGGER = Logger.getLogger(App.class.getName());
	private static final int ITER = 1;
	private static final int N_THREAD = 8;
	private static final float BANDWIDTH = 12f;
//	private static final float BANDWIDTH_CSV = 2f;
	private static final int ALGORITHM_ITER = 1;

	
	public static void main( String[] args )
    {
    	
//    	For images
		
		ImageParser ip = new ImageParser("/images/benzina400x300.jpg");
		List<Point<Double>> luvPoints = null;
    	List<Point<Integer>> rgbShiftedPoints = null;
    	
//    	BufferedImage resultImage = null;

    	long allTimes = 0;
    	
//    	List<Point<Integer>> rgbPoints = ip.extractRGBPoints();
    	
//    	Sequential version
    	
    	
//    	for (int i = 0; i < ITER; i++) {
//    		long startTime = System.currentTimeMillis();
//    		luvPoints = ip.extractLUVPoints();
//    		MeanShift meanShift = new MeanShift(BANDWIDTH, ALGORITHM_ITER, luvPoints);
//    		List<Point<Double>> shiftedPoints = meanShift.meanShiftAlgorithm();
//    		rgbShiftedPoints = ColorConverter.convertToRGBPoints(shiftedPoints);
    		
//    		ip.renderImageWithoutWriteOneLoop(rgbShiftedPoints);
//    		ip.renderImageWithoutWriteOneLoop(rgbPoints);
//    	
//    		long endTime = System.currentTimeMillis();
//    		allTimes += endTime - startTime;
//    	}
//
//    	LOGGER.info("Sequential version took " + (allTimes / ITER) + " milliseconds");

//    	ip.renderImage(rgbShiftedPoints, "results/resultBenzinaBW12Iter5.jpg");
		
		
//		Parallel version
    	
//    	allTimes = 0;
//    	
    	
    	if (false) {
			for (int k = 0; k < ITER; k++) {
		//    		long startTime = System.currentTimeMillis();
				    	
				Phaser ph = new Phaser(1);
				ExecutorService executor = Executors.newFixedThreadPool(N_THREAD);
				
				luvPoints = ip.extractLUVPoints();
				rgbShiftedPoints = new ArrayList<>();
				for (int i = 0; i < luvPoints.size(); i++) {
					rgbShiftedPoints.add(new Point<>(0, 0, 0));
				}
				
		//        	Raster raster = ip.getRaster();
		//
		//        	rgbShiftedPoints = new ArrayList<>();
		//        	luvPoints = new ArrayList<>();
		//        	for (int i = 0; i < raster.getHeight() * raster.getWidth(); i++) {
		//        		luvPoints.add(new Point<>(0.0, 0.0, 0.0));
		//        		rgbShiftedPoints.add(new Point<>(0, 0, 0));
		//        	}
		//        	
		//        	for (int i = 0; i < N_THREAD; i++) {
		//        		executor.execute(new PixelsExtractionThread(i, N_THREAD, raster, luvPoints, ph));
		//			}
		//			
		//        	ph.arriveAndAwaitAdvance();
		//    		
				long startTime = System.currentTimeMillis();
				for (int i = 0; i < N_THREAD; i++) {
					executor.execute(new MeanShiftThread(i, N_THREAD, ALGORITHM_ITER, BANDWIDTH, luvPoints, rgbShiftedPoints, ph));
				}
				
				ph.arriveAndAwaitAdvance();
							
		//			resultImage = new BufferedImage(raster.getWidth(), raster.getHeight(), BufferedImage.TYPE_INT_RGB);
		//			
		//			for (int i = 0; i < N_THREAD; i++) {
		//				executor.execute(new ImageRenderThread(i, N_THREAD, resultImage, rgbShiftedPoints, latchRender));
		//			}
		//			
		//			ph.arriveAndAwaitAdvance();
		//			ip.write(resultImage, "results/prova.jpg");
				
				ph.arriveAndDeregister();
		
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
		//    	
		//    	ip.renderImage(rgbShiftedPoints, "results/resultBenzinaBW12Iter5Parallel.jpg");
    	}
    	
    	
//    	Parallel SoA
    	

    	PointsSoA<Double> luvPointsSoA = ip.extractLUVPointsSoA();
    	
    	ArrayList<Integer> d1 = new ArrayList<>();
    	ArrayList<Integer> d2 = new ArrayList<>();
    	ArrayList<Integer> d3 = new ArrayList<>();
    	
    	for (int i = 0; i < luvPointsSoA.size(); i++) {
    		d1.add(0);
    		d2.add(0);
    		d3.add(0);
    	}
    	
    	PointsSoA<Integer> rgbShiftedPointsSoA = new PointsSoA<>(d1, d2, d3); 
    	
    	for (int k = 0; k < ITER; k++) {
    		
    		Phaser ph = new Phaser(1);
        	ExecutorService executor = Executors.newFixedThreadPool(N_THREAD);
    		
    		long startTime = System.currentTimeMillis();
    		    	
    		
			for (int i = 0; i < N_THREAD; i++) {
				executor.execute(new MeanShiftThread(i, N_THREAD, ALGORITHM_ITER, BANDWIDTH, luvPointsSoA, rgbShiftedPointsSoA, ph));
			}
			
			ph.arriveAndAwaitAdvance();
			ph.arriveAndDeregister();
			
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
    	
//    	ip.renderImageOneLoopSoA(rgbShiftedPointsSoA, "results/resultBenzinaBW12Iter5ParallelSoA.jpg");
    	
    	
    	
    	
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
