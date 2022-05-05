package com.apt.project.mean_shift.utils.parallel;

import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.apt.project.mean_shift.model.Point;
import com.apt.project.mean_shift.utils.ColorConverter;

public class PixelsExtractionThreadAoS implements Callable<List<Point<Double>>> {
	
	private int tid;
	private int nThreads;
	private Raster raster;
	
	public PixelsExtractionThreadAoS(int tid, int nThreads, Raster raster) {
		this.tid = tid;
		this.nThreads = nThreads;
		this.raster = raster;
	}	

	@Override
	public List<Point<Double>> call() throws Exception {
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
		
		List<Point<Double>> extractedPoints = new ArrayList<>(chunkSize);
		
		int[] pixel;
		for (int i = 0; i < height; i++) {
	    	for (int j = startChunk; j < endChunk; j++) {
	          pixel = raster.getPixel(j, i, new int[3]);	          
	          extractedPoints.add(new Point<>(ColorConverter.convertToLUVPoint(pixel)));
	        }
	    }
		
		return extractedPoints;
	}

}
