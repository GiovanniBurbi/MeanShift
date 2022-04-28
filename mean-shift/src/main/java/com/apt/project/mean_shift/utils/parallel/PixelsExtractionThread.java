package com.apt.project.mean_shift.utils.parallel;

import java.awt.image.Raster;
import java.util.List;
import java.util.concurrent.Phaser;

import com.apt.project.mean_shift.model.Point;
import com.apt.project.mean_shift.model.PointsSoA;
import com.apt.project.mean_shift.utils.ColorConverter;

public class PixelsExtractionThread implements Runnable {
	
	private int tid;
	private int nThreads;
	private Raster raster;
	private List<Point<Double>> extractedPoints;
	private PointsSoA<Double> extractedPointsSoA;
	private Phaser ph;
	private boolean isAoS;
	
	public PixelsExtractionThread(int tid, int nThreads, Raster raster, List<Point<Double>> extractedPoints, Phaser ph) {
		this.tid = tid;
		this.nThreads = nThreads;
		this.raster = raster;
		this.extractedPoints = extractedPoints;
		this.ph = ph;
		this.isAoS = true;
		ph.register();
	}
	
	public PixelsExtractionThread(int tid, int nThreads, Raster raster, PointsSoA<Double> extractedPoints, Phaser ph) {
		this.tid = tid;
		this.nThreads = nThreads;
		this.raster = raster;
		this.extractedPointsSoA = extractedPoints;
		this.ph = ph;
		this.isAoS = false;
		ph.register();
	}
	
	private void extractAoS(int height, int width, int startChunk, int endChunk) {
		int[] pixel;
		for (int i = 0; i < height; i++) {
	    	for (int j = startChunk; j < endChunk; j++) {
	          pixel = raster.getPixel(j, i, new int[3]);
	          extractedPoints.set(i*width + j, new Point<>(ColorConverter.convertToLUVPoint(pixel)));
	        }
	    }
	}
	
	private void extractSoA(int height, int width, int startChunk, int endChunk) {
		int[] pixel;
		for (int i = 0; i < height; i++) {
	    	for (int j = startChunk; j < endChunk; j++) {
	          pixel = raster.getPixel(j, i, new int[3]);
	          Double[] luvPoint = ColorConverter.convertToLUVPoint(pixel);
	          extractedPointsSoA.getD1().set(i*width + j, luvPoint[0]);
	          extractedPointsSoA.getD2().set(i*width + j, luvPoint[1]);
	          extractedPointsSoA.getD3().set(i*width + j, luvPoint[2]);
	        }
	    }
	}

	@Override
	public void run() {
		int height = raster.getHeight();
		int width = raster.getWidth();
		
//		Calculate the chunk of the matrix of the image for the thread. The matrix is split by columns and the chunk is evenly distributed between threads. max variance is 1
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
		
//		Based on a flag defined in the constructor apply one of the methods
		if(isAoS) extractAoS(height, width, startChunk, endChunk);
		else extractSoA(height, width, startChunk, endChunk);
						
		ph.arriveAndDeregister();
		
	}

}
