package com.apt.project.mean_shift.utils.parallel;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.Phaser;

import com.apt.project.mean_shift.model.Point;

public class ImageRenderThread implements Runnable{

	private int tid;
	private int nThreads;
	private BufferedImage image;
	private List<Point<Integer>> renderPoints;
	private Phaser ph;

	public ImageRenderThread(int tid, int nThreads, BufferedImage image, List<Point<Integer>> renderPoints, Phaser ph) {
		this.tid = tid;
		this.nThreads = nThreads;
		this.image = image;
		this.renderPoints = renderPoints;
		this.ph = ph;
		ph.register();
	}

	@Override
	public void run() {		
//		int height = image.getHeight();
		int width = image.getWidth();
		
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
		ph.arriveAndDeregister();
	}
}
