package com.apt.project.mean_shift.utils.parallel;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.Phaser;

import com.apt.project.mean_shift.model.Point;
import com.apt.project.mean_shift.model.PointsSoA;

public class ImageRenderThread implements Runnable{

	private int tid;
	private int nThreads;
	private BufferedImage image;
	private List<Point<Integer>> renderPoints;
	private PointsSoA<Integer> renderPointsSoA;
	private Phaser ph;
	private boolean isAoS;

	public ImageRenderThread(int tid, int nThreads, BufferedImage image, List<Point<Integer>> renderPoints, Phaser ph) {
		this.tid = tid;
		this.nThreads = nThreads;
		this.image = image;
		this.renderPoints = renderPoints;
		this.ph = ph;
		this.isAoS = true;
		ph.register();
	}
	
	public ImageRenderThread(int tid, int nThreads, BufferedImage image, PointsSoA<Integer> renderPoints, Phaser ph) {
		this.tid = tid;
		this.nThreads = nThreads;
		this.image = image;
		this.renderPointsSoA = renderPoints;
		this.ph = ph;
		this.isAoS = false;
		ph.register();
	}
	
	private void renderAoS(int width, int startChunk, int endChunk) {		
		for (int i = startChunk; i < endChunk; i++) {
			Point<Integer> point = renderPoints.get(i);
			int rgb = point.getD1();
	        rgb = (rgb << 8) + point.getD2(); 
	        rgb = (rgb << 8) + point.getD3();
	        image.setRGB(i % width, i / width, rgb);
		}
	}
	
	private void renderSoA(int width, int startChunk, int endChunk) {
		for (int i = startChunk; i < endChunk; i++) {
			int rgb = renderPointsSoA.getD1().get(i);
	        rgb = (rgb << 8) + renderPointsSoA.getD2().get(i); 
	        rgb = (rgb << 8) + renderPointsSoA.getD3().get(i);
	        image.setRGB(i % width, i / width, rgb);
		}
	}

	@Override
	public void run() {		
		int width = image.getWidth();
		
		int numberOfElements = width * image.getHeight();
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
		
		if (isAoS) renderAoS(width, startChunk, endChunk);
		else renderSoA(width, startChunk, endChunk);
		
		ph.arriveAndDeregister();
	}
}
