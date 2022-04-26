package com.apt.project.mean_shift.utils.parallel;

import java.awt.image.Raster;
import java.util.List;
import java.util.concurrent.Phaser;

import com.apt.project.mean_shift.model.Point;
import com.apt.project.mean_shift.utils.ColorConverter;

public class PixelsExtractionThread implements Runnable {
	
	private int tid;
	private int nThreads;
	private Raster raster;
	private List<Point<Double>> extractedPoints;
	private Phaser ph;
	
	public PixelsExtractionThread(int tid, int nThreads, Raster raster, List<Point<Double>> extractedPoints, Phaser ph) {
		this.tid = tid;
		this.nThreads = nThreads;
		this.raster = raster;
		this.extractedPoints = extractedPoints;
		this.ph = ph;
		ph.register();
	}

	@Override
	public void run() {
		int height = raster.getHeight();
		int width = raster.getWidth();
		
		int numberOfElements = width;
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
		
		int[] pixel;
		
		for (int i = 0; i < height; i++) {
	    	for (int j = startChunk; j < endChunk; j++) {
	          pixel = raster.getPixel(j, i, new int[3]);
	          extractedPoints.get(i*width + j).replace(ColorConverter.convertToLUVPoint(new Point<>(pixel[0], pixel[1], pixel[2])));
	        }
	    }				
		ph.arriveAndDeregister();
		
	}

}
