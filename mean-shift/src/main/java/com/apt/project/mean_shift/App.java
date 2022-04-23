package com.apt.project.mean_shift;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.apt.project.mean_shift.algorithm.MeanShift;
import com.apt.project.mean_shift.model.Point;
import com.apt.project.mean_shift.utils.ColorConverter;
import com.apt.project.mean_shift.utils.ImageParser;

public class App 
{
	private static final Logger LOGGER = Logger.getLogger(App.class.getName());
	private static final int ITER = 1;
	private static final int N_THREAD = 2;

	public static void main( String[] args )
    {
    	
//    	Sequential version
    	
    	
//    	For images
    	
//    	CSVParser p = new CSVParser();
//    	ImageParser ip = new ImageParser("/images/benzina.jpg");
//    	List<Point<Integer>> rgbPoints = ip.extractRGBPoints();
//    	List<Point<Double>> luvPoints = ColorConverter.convertToLUVPoints(rgbPoints);
//    	p.write(luvPoints, "originPoints.csv");
//    	MeanShift meanShift = new MeanShift(12f, 5, luvPoints);
//    	long startTime;
//    	long endTime;
//    	long allTimes = 0;
//    	for (int i = 0; i < ITER - 1; i++) {
//    		startTime = System.currentTimeMillis();
//    		meanShift.meanShiftAlgorithm();
//    		endTime = System.currentTimeMillis();
//    		allTimes += endTime - startTime;
//    	}
//    	startTime = System.currentTimeMillis();
//    	List<Point<Double>> shiftedPoints = meanShift.meanShiftAlgorithm();
//		endTime = System.currentTimeMillis();
//		allTimes += endTime - startTime;
//    	LOGGER.info("Sequential version took " + (allTimes / ITER) + " milliseconds");
//    	p.write(shiftedPoints, "shiftedPoints.csv");
//    	List<Point<Integer>> rgbShiftedPoints = ColorConverter.convertToRGBPoints(shiftedPoints);
//    	ip.renderImage(rgbShiftedPoints, "results/resultBenzinaBW12Iter5.jpg");
    	
    	
//    	For csv files
    	
//    	CSVParser p = new CSVParser();
//    	p.fetchCSVFile("/points/100.csv");
//    	List<Point<Double>> points = p.extractPoints();
//    	MeanShift meanShift = new MeanShift(2, 10, points);
//    	List<Point<Double>> shiftedPoints = meanShift.meanShiftAlgorithm();
//    	p.write(shiftedPoints, "shiftedPoints.csv");
    	
    	
//    	Parallel version
    	
    	ImageParser ip = new ImageParser("/images/benzina.jpg");
    	List<Point<Integer>> rgbPoints = ip.extractRGBPoints();
    	List<Point<Double>> luvPoints = ColorConverter.convertToLUVPoints(rgbPoints);
    	MeanShift meanShift = new MeanShift(12f, 5, luvPoints);
    	List<Point<Double>> shiftedPoints = new ArrayList<>(luvPoints.size());
    	long startTime;
    	long endTime;
    	long allTimes = 0;

    	ExecutorService exec = Executors.newFixedThreadPool(N_THREAD);
    	int numberOfElements = luvPoints.size();
    	int minElementsPerThread = numberOfElements / N_THREAD;
    	int threadsWithMaxElements = numberOfElements - N_THREAD * minElementsPerThread;
    	int startIndex = 0;
    	int nElements;
    	int endIndex;
    	//List<Future<?>> futures = new ArrayList<Future<?>>(N_THREAD); // ? Is it needed? maybe i only need to implement a runnable and not a callable
    	
    	for (int k = 0; k < ITER; k++) {
    		startTime = System.currentTimeMillis();
			for (int i = 0; i < N_THREAD; i++) {
				// TODO do these operation (and line 78) inside thread
				nElements = (i < threadsWithMaxElements) ? minElementsPerThread + 1 : minElementsPerThread;
				endIndex = startIndex + nElements;
// start threads TODO
				startIndex = endIndex;
			}
// wait threads to finish TODO
			
			endTime = System.currentTimeMillis();
			allTimes += endTime - startTime;
    	}
		exec.shutdown();
    	LOGGER.info("Parallel version took " + (allTimes / ITER) + " milliseconds");
    	List<Point<Integer>> rgbShiftedPoints = ColorConverter.convertToRGBPoints(shiftedPoints);
    	ip.renderImage(rgbShiftedPoints, "results/resultBenzinaBW12Iter5Parallel.jpg");
    }
}
