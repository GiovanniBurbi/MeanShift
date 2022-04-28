package com.apt.project.mean_shift;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.apt.project.mean_shift.algorithm.MeanShift;
import com.apt.project.mean_shift.algorithm.parallel.MeanShiftThread;
import com.apt.project.mean_shift.model.Point;
import com.apt.project.mean_shift.model.PointsSoA;
import com.apt.project.mean_shift.utils.ImageParser;
import com.apt.project.mean_shift.utils.parallel.ImageRenderThread;
import com.apt.project.mean_shift.utils.parallel.PixelsExtractionThread;

public class App 
{
	private static final Logger LOGGER = Logger.getLogger(App.class.getName());
	private static final String SOURCE_IMAGE= "benzina200x150";
	private static final boolean SEQUENTIAL_AOS_VERSION = false;
	private static final boolean SEQUENTIAL_SOA_VERSION = false;
	private static final boolean PARALLEL_AOS_VERSION = true;
	private static final boolean PARALLEL_SOA_VERSION = true;
	private static final int ITER = 1;
	private static final int N_THREAD = 4;
	private static final float BANDWIDTH = 12f;
	private static final int ALGORITHM_ITER = 5;

	
	public static void main( String[] args ) {

		StringBuilder src = new StringBuilder();
    	src.append("/images/").append(SOURCE_IMAGE).append(".jpg");	
		ImageParser ip = new ImageParser(src.toString());
		
		StringBuilder str = new StringBuilder();
		str.append("results/").append(SOURCE_IMAGE).append("_BW").append(BANDWIDTH).append("_ITER").append(ALGORITHM_ITER);
		int defaultStrSize = str.length();

//    	Sequential AoS version
    	
    	if (SEQUENTIAL_AOS_VERSION) {
    		// List of resulted points
	    	List<Point<Double>> shiftedPoints = null;

	    	long allTimes = 0;
	    	
	    	for (int i = 0; i < ITER; i++) {
	    		long startTime = System.currentTimeMillis();

	    		List<Point<Double>> luvPoints = ip.extractLUVPoints();
	    		MeanShift meanShift = new MeanShift(BANDWIDTH, ALGORITHM_ITER, luvPoints);
	    		shiftedPoints = meanShift.meanShiftAlgorithm();
	    		ip.renderImageFromLUV(shiftedPoints);
	    	
	    		long endTime = System.currentTimeMillis();
	    		allTimes += endTime - startTime;
	    		
	    	}
	    	
	    	LOGGER.info("Sequential AoS version took " + (allTimes / ITER) + " milliseconds");
	    	
	    	str.append("_Sequential_AoS").append(".jpg");
	
//	    	ip.renderImageFromLUV(shiftedPoints, str.toString());
    	}
    	
//	    Sequential SoA version
    	
    	if (SEQUENTIAL_SOA_VERSION) {
    		// List of resulted points
    		PointsSoA<Double> shiftedPoints = null;
	    	long allTimes = 0;
	    	
	    	for (int i = 0; i < ITER; i++) {
	    		long startTime = System.currentTimeMillis();

	    		PointsSoA<Double> luvPointsSoA = ip.extractLUVPointsSoA();
	    		MeanShift meanShift = new MeanShift(BANDWIDTH, ALGORITHM_ITER, luvPointsSoA);
	    		shiftedPoints = meanShift.meanShiftAlgorithmSoA();
	    		ip.renderImageSoAFromLUV(shiftedPoints);
	    	
	    		long endTime = System.currentTimeMillis();
	    		allTimes += endTime - startTime;
	    	}
	    	LOGGER.info("Sequential SoA version took " + (allTimes / ITER) + " milliseconds");
	    	
	    	str.append("_Sequential_SoA").append(".jpg");
	
//	    	ip.renderImageSoAFromLUV(shiftedPoints, str.toString());
    	}
		
//		Parallel version AoS
    	

    	if (PARALLEL_AOS_VERSION) {
    		BufferedImage resultImage = null;
	    	long allTimes = 0;
    		
			for (int k = 0; k < ITER; k++) {
				long startTime = System.currentTimeMillis();
				    	
				Phaser ph = new Phaser(1);
				ExecutorService executor = Executors.newFixedThreadPool(N_THREAD);
				
	        	Raster raster = ip.getRaster();
	
//	        	List<Point<Integer>> rgbShiftedPoints = new ArrayList<>();
	        	List<Point<Double>> luvPoints = new ArrayList<>();
	        	List<Point<Double>> resultPoints = new ArrayList<>();
	        	for (int i = 0; i < raster.getHeight() * raster.getWidth(); i++) {
	        		luvPoints.add(new Point<>(0.0, 0.0, 0.0));
	        		resultPoints.add(new Point<Double>(0.0, 0.0, 0.0));
//	        		rgbShiftedPoints.add(new Point<>(0, 0, 0));
	        	}
	        	
	        	for (int i = 0; i < N_THREAD; i++) {
	        		executor.execute(new PixelsExtractionThread(i, N_THREAD, raster, luvPoints, ph));
				}

	        	ph.arriveAndAwaitAdvance();
		    		
				for (int i = 0; i < N_THREAD; i++) {
					executor.execute(new MeanShiftThread(i, N_THREAD, ALGORITHM_ITER, BANDWIDTH, luvPoints, resultPoints, ph));
				}
				
				ph.arriveAndAwaitAdvance();
							
				resultImage = new BufferedImage(raster.getWidth(), raster.getHeight(), BufferedImage.TYPE_INT_RGB);
				
				for (int i = 0; i < N_THREAD; i++) {
					executor.execute(new ImageRenderThread(i, N_THREAD, resultImage, resultPoints, ph));
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
			
			LOGGER.info("Parallel AoS version took " + (allTimes / ITER) + " milliseconds");
			
	    	str.append("_Parallel_AoS").append("_").append(N_THREAD).append("-Threads").append(".jpg");
		    	
	    	ip.write(resultImage, str.toString());
	    	
	    	str.delete(defaultStrSize, str.length());
    	}
    	
    	
//    	Parallel SoA
    	
    	if (PARALLEL_SOA_VERSION) {		    	
	    	BufferedImage resultImage = null;
	    	long allTimes = 0;
	    	
	    	for (int k = 0; k < ITER; k++) {
	    		long startTime = System.currentTimeMillis();
	    		
	    		Phaser ph = new Phaser(1);
	        	ExecutorService executor = Executors.newFixedThreadPool(N_THREAD);
	    		
	        	Raster raster = ip.getRaster();
		    	
	        	ArrayList<Double> d1 = new ArrayList<>();
	        	ArrayList<Double> d2 = new ArrayList<>();
	        	ArrayList<Double> d3 = new ArrayList<>();
	        	
		    	ArrayList<Double> resultD1 = new ArrayList<>();
		    	ArrayList<Double> resultD2 = new ArrayList<>();
		    	ArrayList<Double> resultD3 = new ArrayList<>();
		    	
		    	for (int i = 0; i < raster.getWidth() * raster.getHeight(); i++) {
		    		d1.add(0.0);
		    		d2.add(0.0);
		    		d3.add(0.0);
		    		resultD1.add(0.0);
		    		resultD2.add(0.0);
		    		resultD3.add(0.0);
		    	}
		    	
		    	PointsSoA<Double> luvPointsSoA = new PointsSoA<>(d1, d2, d3);
		    	PointsSoA<Double> resultPointsSoA = new PointsSoA<>(resultD1, resultD2, resultD3);
		    	
		    	for (int i = 0; i < N_THREAD; i++) {
	        		executor.execute(new PixelsExtractionThread(i, N_THREAD, raster, luvPointsSoA, ph));
				}

	        	ph.arriveAndAwaitAdvance();
	    		
				for (int i = 0; i < N_THREAD; i++) {
					executor.execute(new MeanShiftThread(i, N_THREAD, ALGORITHM_ITER, BANDWIDTH, luvPointsSoA, resultPointsSoA, ph));
				}
				
				ph.arriveAndAwaitAdvance();
				
				resultImage = new BufferedImage(raster.getWidth(), raster.getHeight(), BufferedImage.TYPE_INT_RGB);
				
				for (int i = 0; i < N_THREAD; i++) {
					executor.execute(new ImageRenderThread(i, N_THREAD, resultImage, resultPointsSoA, ph));
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
	    	
	    	LOGGER.info("Parallel SoA version took " + (allTimes / ITER) + " milliseconds");
	    	
	    	str.append("_Parallel_SoA").append("_").append(N_THREAD).append("-Threads").append(".jpg");
	    	
	    	ip.write(resultImage, str.toString());
    	}
	}
}
