package com.apt.project.mean_shift;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.apt.project.mean_shift.algorithm.MeanShift;
import com.apt.project.mean_shift.algorithm.parallel.MeanShiftThreadAoS;
import com.apt.project.mean_shift.algorithm.parallel.MeanShiftThreadRunnable;
import com.apt.project.mean_shift.model.Point;
import com.apt.project.mean_shift.model.PointsSoA;
import com.apt.project.mean_shift.utils.ImageParser;
import com.apt.project.mean_shift.utils.parallel.ImageRenderThread;
import com.apt.project.mean_shift.utils.parallel.PixelsExtractionThreadAoS;
import com.apt.project.mean_shift.utils.parallel.PixelsExtractionThreadRunnable;

public class App 
{
	private static final Logger LOGGER = Logger.getLogger(App.class.getName());
	private static final String SOURCE_IMAGE= "benzina120x90";
	private static final boolean SEQUENTIAL_AOS_VERSION = false;
	private static final boolean SEQUENTIAL_SOA_VERSION = false;
	private static final boolean PARALLEL_AOS_VERSION_RUNNABLE = false;
	private static final boolean PARALLEL_SOA_VERSION_RUNNABLE = false;
	private static final boolean PARALLEL_AOS_VERSION_CALLABLE = true;
	private static final boolean PARALLEL_SOA_VERSION_CALLABLE = false;
	private static final int ITER = 3;
	private static final int N_THREAD = 8;
	private static final float BANDWIDTH = 12f;
	private static final int ALGORITHM_ITER = 6;

	
	public static void main( String[] args ) {

		StringBuilder src = new StringBuilder();
    	src.append("/images/").append(SOURCE_IMAGE).append(".jpg");	
		ImageParser ip = new ImageParser(src.toString());
		
		StringBuilder str = new StringBuilder();
		str.append(SOURCE_IMAGE).append("_BW").append(BANDWIDTH).append("_ITER").append(ALGORITHM_ITER);
		int defaultStrSize = str.length();

//    	Sequential AoS version
    	
    	if (SEQUENTIAL_AOS_VERSION) {
    		// List of resulted points
	    	List<Point<Double>> shiftedPoints = null;

	    	long allTimes = 0;
	    	
	    	
	    	for (int i = 0; i < ITER; i++) {
	    		Instant start = Instant.now();

//	    		Extract list LUV points from image 
	    		List<Point<Double>> luvPoints = ip.extractLUVPoints();

//	    		Start the algorithm with a list of LUV points and some parameters
	    		MeanShift meanShift = new MeanShift(BANDWIDTH, ALGORITHM_ITER, luvPoints);
	    		shiftedPoints = meanShift.meanShiftAlgorithm();

//	    		Create a buffered image with the shifted points
	    		ip.renderImageFromLUV(shiftedPoints);
	    	
	    		Instant end = Instant.now();
	    		allTimes += Duration.between(start, end).toMillis();
	    		LOGGER.info("End iteration " + (i + 1));
	    		
	    	}
	    	
	    	LOGGER.info("Sequential AoS version took " + (allTimes / ITER) + " milliseconds");
	    	
	    	str.append("_Sequential_AoS").append(".jpg");
	
//	    	Create a JPG image with the shifted points and save it on a given path
	    	ip.renderImageFromLUV(shiftedPoints, str.toString());
	    	
//	    	Remove from the string that is used to name the JPG image the part specific to this if statement
	    	str.delete(defaultStrSize, str.length());
    	}
    	
//	    Sequential SoA version
    	
    	if (SEQUENTIAL_SOA_VERSION) {
    		// List of resulted points
    		PointsSoA<Double> shiftedPoints = null;
	    	long allTimes = 0;
	    	
	    	for (int i = 0; i < ITER; i++) {
	    		Instant start = Instant.now();

//	    		Extract LUV points from an image as a structure of arrays
	    		PointsSoA<Double> luvPointsSoA = ip.extractLUVPointsSoA();

//	    		Start the algorithm with the LUV points and some parameters
	    		MeanShift meanShift = new MeanShift(BANDWIDTH, ALGORITHM_ITER, luvPointsSoA);
	    		shiftedPoints = meanShift.meanShiftAlgorithmSoA();
	    		
//	    		Create a buffered image with the shifted points
	    		ip.renderImageSoAFromLUV(shiftedPoints);
	    	
	    		Instant end = Instant.now();
	    		allTimes += Duration.between(start, end).toMillis();
	    		LOGGER.info("End iteration " + (i + 1));
	    	}
	    	LOGGER.info("Sequential SoA version took " + (allTimes / ITER) + " milliseconds");
	    	
	    	str.append("_Sequential_SoA").append(".jpg");
	
//	    	Create a JPG image with the shifted points and save it on a given path
	    	ip.renderImageSoAFromLUV(shiftedPoints, str.toString());
	    	
//	    	Remove from the string that is used to name the JPG image the part specific to this if statement
	    	str.delete(defaultStrSize, str.length());
    	}
		
//		Parallel version AoS
    	

    	if (PARALLEL_AOS_VERSION_RUNNABLE) {
    		BufferedImage resultImage = null;
	    	long allTimes = 0;
    		
			for (int k = 0; k < ITER; k++) {
				Instant start = Instant.now();
				
//				Initialize phaser and executor with a fixed pool of threads
				Phaser ph = new Phaser(1);
				ExecutorService executor = Executors.newFixedThreadPool(N_THREAD);
				
//				Get the raster of the image
	        	Raster raster = ip.getRaster();
	
//	        	Initialize the shared list of LUV points of the image and the shared list where it'll be stored the shifted points
	        	List<Point<Double>> luvPoints = new ArrayList<>();
	        	List<Point<Double>> resultPoints = new ArrayList<>();
	        	for (int i = 0; i < raster.getHeight() * raster.getWidth(); i++) {
	        		luvPoints.add(new Point<>(0.0, 0.0, 0.0));
	        		resultPoints.add(new Point<>(0.0, 0.0, 0.0));
	        	}
	        	
//	        	Start threads to extract the LUV pixels of the image that will be stored in luvPoints
	        	for (int i = 0; i < N_THREAD; i++) {
	        		executor.execute(new PixelsExtractionThreadRunnable(i, N_THREAD, raster, luvPoints, ph));
				}

//	        	Wait for threads to finish their work
	        	ph.arriveAndAwaitAdvance();
		    		
//	        	Start threads to apply the mean shift algorithm to the list of LUV points with some parameters
				for (int i = 0; i < N_THREAD; i++) {
					executor.execute(new MeanShiftThreadRunnable(i, N_THREAD, ALGORITHM_ITER, BANDWIDTH, luvPoints, resultPoints, ph));
				}
				
//	        	Wait for threads to finish their work
				ph.arriveAndAwaitAdvance();
						
//				Initialize a buffered image for the shifted points
				resultImage = new BufferedImage(raster.getWidth(), raster.getHeight(), BufferedImage.TYPE_INT_RGB);
				
//				Start threads to create a buffered image from the shifted points
				for (int i = 0; i < N_THREAD; i++) {
					executor.execute(new ImageRenderThread(i, N_THREAD, resultImage, resultPoints, ph));
				}
				
//				Wait for thread to finish their work and unregister from the phaser
				ph.arriveAndAwaitAdvance();
				ph.arriveAndDeregister();
		
//				shutdown the executor
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
		
				Instant end = Instant.now();
				allTimes += Duration.between(start, end).toMillis();
				LOGGER.info("End iteration " + (k + 1));
			}
			
			LOGGER.info("Parallel AoS version Runnable took " + (allTimes / ITER) + " milliseconds");
			
	    	str.append("_Parallel_AoS").append("_").append(N_THREAD).append("-Threads").append(".jpg");

//	    	Create a JPG image with the shifted points and save it on the given path
	    	ip.write(resultImage, str.toString());
	    	
//	    	Remove from the string that is used to name the JPG image the part specific to this if statement
	    	str.delete(defaultStrSize, str.length());
    	}
    	
    	
//    	Parallel SoA
    	
    	if (PARALLEL_SOA_VERSION_RUNNABLE) {		    	
	    	BufferedImage resultImage = null;
	    	long allTimes = 0;
	    	
	    	for (int k = 0; k < ITER; k++) {
	    		Instant start = Instant.now();
	    		
//				Initialize phaser and executor with a fixed pool of threads
	    		Phaser ph = new Phaser(1);
	        	ExecutorService executor = Executors.newFixedThreadPool(N_THREAD);
	    		
//				Get the raster of the image
	        	Raster raster = ip.getRaster();
		    	
//	        	Initialize the shared structure of arrays of LUV points of the image and the shared structure of arrays where it'll be stored the shifted points
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
		    	
//	        	Start threads to extract the LUV pixels of the image that will be stored in luvPointsSoA
		    	for (int i = 0; i < N_THREAD; i++) {
	        		executor.execute(new PixelsExtractionThreadRunnable(i, N_THREAD, raster, luvPointsSoA, ph));
				}

//	        	Wait for threads to finish their work
	        	ph.arriveAndAwaitAdvance();
	    		
//	        	Start threads to apply the mean shift algorithm to the structure of arrays of LUV points with some parameters
				for (int i = 0; i < N_THREAD; i++) {
					executor.execute(new MeanShiftThreadRunnable(i, N_THREAD, ALGORITHM_ITER, BANDWIDTH, luvPointsSoA, resultPointsSoA, ph));
				}
				
//	        	Wait for threads to finish their work
				ph.arriveAndAwaitAdvance();
				
//				Initialize a buffered image for the shifted points stored as structure of arrays
				resultImage = new BufferedImage(raster.getWidth(), raster.getHeight(), BufferedImage.TYPE_INT_RGB);
				
//				Start threads to create a buffered image from the shifted points
				for (int i = 0; i < N_THREAD; i++) {
					executor.execute(new ImageRenderThread(i, N_THREAD, resultImage, resultPointsSoA, ph));
				}
				
//	        	Wait for threads to finish their work and unregister from the phaser
				ph.arriveAndAwaitAdvance();					
				ph.arriveAndDeregister();
				
//				shutdown the executor
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
	
				Instant end = Instant.now();
				allTimes += Duration.between(start, end).toMillis();
				LOGGER.info("End iteration " + (k + 1));
	    	}
	    	
	    	LOGGER.info("Parallel SoA version Runnable took " + (allTimes / ITER) + " milliseconds");
	    	
//	    	Create a JPG image with the shifted points stored as a structure of arrays and save it on the given path
	    	str.append("_Parallel_SoA").append("_").append(N_THREAD).append("-Threads").append(".jpg");
	    	
//	    	Remove from the string that is used to name the JPG image the part specific to this if statement
	    	ip.write(resultImage, str.toString());
    	}
    	
    	if(PARALLEL_AOS_VERSION_CALLABLE) {
//    		List of resulted shifted points
    		List<Point<Double>> shiftedPoints = new ArrayList<>();
	    	long allTimes = 0;
    		
			for (int k = 0; k < ITER; k++) {
				Instant start = Instant.now();
				
//				Initialize executor with a fixed pool of threads
				ExecutorService executor = Executors.newFixedThreadPool(N_THREAD);
				
//				Get the raster of the image
	        	Raster raster = ip.getRaster();
	        	
//	        	Initialize list of threads that that will extract pixels from the image
	        	List<PixelsExtractionThreadAoS> extractionThreads = new ArrayList<>();
//				Instantiate the threads passing the parameters
	        	for (int i = 0; i < N_THREAD; i++) {
	        		PixelsExtractionThreadAoS extractThread = new PixelsExtractionThreadAoS(i, N_THREAD, raster);
	        		extractionThreads.add(extractThread);
	        	}
		    	
//	        	Initialize a list to hold the Futures associated with threads that implement Callable
	        	List<Future<List<Point<Double>>>> extractionFuturesList = null;
		    	try {
//		    		Execute the threads
		    		extractionFuturesList = executor.invokeAll(extractionThreads);
				} catch (InterruptedException e) {
					LOGGER.log(Level.WARNING, e.getMessage(), e);
					Thread.currentThread().interrupt();
				}
		    	
//		    	Create a list to hold the LUV points extracted from the image
		    	List<Point<Double>> originPoints = new ArrayList<>();
		    	
//		    	Wait for the completion of the threads to get the return value and add it to a list
		    	for (Future<List<Point<Double>>> future : extractionFuturesList) {
		    		try {
						List<Point<Double>> originPointChunk = future.get();
						originPoints.addAll(originPointChunk);
					} catch (InterruptedException|ExecutionException e) {
						LOGGER.log(Level.WARNING, e.getMessage(), e);
						Thread.currentThread().interrupt();
					}
		    	}
		    	
//	        	Initialize list of threads that that will apply the mean shift algorithm
		    	List<MeanShiftThreadAoS> meanShiftThreads = new ArrayList<>();
//				Instantiate the threads passing the parameters
				for (int i = 0; i < N_THREAD; i++) {
					MeanShiftThreadAoS meanShiftThread = new MeanShiftThreadAoS(i, N_THREAD, ALGORITHM_ITER, BANDWIDTH, originPoints);
					meanShiftThreads.add(meanShiftThread);
				}
				
//	        	Initialize a list to hold the Futures associated with threads that implement Callable
				List<Future<List<Point<Double>>>> algorithmFuturesList = null;
		    	try {
//		    		Execute the threads
		    		algorithmFuturesList = executor.invokeAll(meanShiftThreads);
				} catch (InterruptedException e) {
					LOGGER.log(Level.WARNING, e.getMessage(), e);
					Thread.currentThread().interrupt();
				}
		    	
		    	for (Future<List<Point<Double>>> future : algorithmFuturesList) {
					try {
						List<Point<Double>> shiftedPointChunk = future.get();
						shiftedPoints.addAll(shiftedPointChunk);
					} catch (InterruptedException|ExecutionException e) {
						LOGGER.log(Level.WARNING, e.getMessage(), e);
						Thread.currentThread().interrupt();
					}
				}
		    	
//		    	shutdown the executor
				executor.shutdown();
						
//	    		render image without create a JPG image
		    	ip.renderImageFromLUV(shiftedPoints, N_THREAD);
		    	
//		    	Verify termination of the executor
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
		
				Instant end = Instant.now();
				allTimes += Duration.between(start, end).toMillis();
				LOGGER.info("End iteration " + (k + 1));
			}
			
			LOGGER.info("Parallel AoS version Callable took " + (allTimes / ITER) + " milliseconds");
			
	    	str.append("_Parallel_AoS").append("_").append(N_THREAD).append("-Threads").append("_").append("Callable").append(".jpg");

//	    	Create a JPG image with the shifted points and save it on the given path
	    	ip.renderImageFromLUV(shiftedPoints, str.toString(), N_THREAD);
	    	
//	    	Remove from the string that is used to name the JPG image the part specific to this if statement
	    	str.delete(defaultStrSize, str.length());
    	}
    	
    	if(PARALLEL_SOA_VERSION_CALLABLE) {
    		
    	}
	}
}
