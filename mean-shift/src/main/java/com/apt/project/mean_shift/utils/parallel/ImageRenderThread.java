package com.apt.project.mean_shift.utils.parallel;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.apt.project.mean_shift.model.Point;

public class ImageRenderThread implements Runnable{

	private int tid;
	private int nThreads;
	private BufferedImage image;
	private List<Point<Integer>> renderPoints;
	private CountDownLatch latch;

	public ImageRenderThread(int tid, int nThreads, BufferedImage image, List<Point<Integer>> renderPoints, CountDownLatch latch) {
		this.tid = tid;
		this.nThreads = nThreads;
		this.image = image;
		this.renderPoints = renderPoints;
		this.latch = latch;
	}

	@Override
	public void run() {		
		int height = image.getHeight();
		int width = image.getWidth();
		
//		int numberOfElements = width;
		int numberOfElements = renderPoints.size();
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
				
		for (int i = startChunk; i < endChunk; i++) {
			Point<Integer> point = renderPoints.get(i);
			int rgb = point.getD1();
	        rgb = (rgb << 8) + point.getD2(); 
	        rgb = (rgb << 8) + point.getD3();
	        image.setRGB(i % width, i / width, rgb);
		}
		
//		for (int i = 0; i < height; i++) {
//    		for (int j = startChunk; j < endChunk; j++) {
//    			Point<Integer> point = renderPoints.get(i*width + j);
//    			int rgb = point.getD1();
//		        rgb = (rgb << 8) + point.getD2(); 
//		        rgb = (rgb << 8) + point.getD3();
//		        image.setRGB(j, i, rgb);
//		     }
//		}
		
		latch.countDown();
	}
}
