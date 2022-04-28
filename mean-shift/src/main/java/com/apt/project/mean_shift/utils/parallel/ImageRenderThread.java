package com.apt.project.mean_shift.utils.parallel;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.Phaser;

import com.apt.project.mean_shift.model.Point;
import com.apt.project.mean_shift.model.PointsSoA;
import com.apt.project.mean_shift.utils.ColorConverter;

public class ImageRenderThread implements Runnable{

	private int tid;
	private int nThreads;
	private BufferedImage image;
	private List<Point<Double>> renderPoints;
	private PointsSoA<Double> renderPointsSoA;
	private Phaser ph;
	private boolean isAoS;

	public ImageRenderThread(int tid, int nThreads, BufferedImage image, List<Point<Double>> renderPoints, Phaser ph) {
		this.tid = tid;
		this.nThreads = nThreads;
		this.image = image;
		this.renderPoints = renderPoints;
		this.ph = ph;
		this.isAoS = true;
		ph.register();
	}
	
	public ImageRenderThread(int tid, int nThreads, BufferedImage image, PointsSoA<Double> renderPoints, Phaser ph) {
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
			Point<Integer> point = ColorConverter.convertToRGBPoint(renderPoints.get(i));
			int rgb = point.getD1();
	        rgb = (rgb << 8) + point.getD2(); 
	        rgb = (rgb << 8) + point.getD3();
	        image.setRGB(i % width, i / width, rgb);
		}
	}
	
	private void renderSoA(int width, int startChunk, int endChunk) {
		for (int i = startChunk; i < endChunk; i++) {
			int[] rgbPoint = ColorConverter.convertToRGBPoint(new Double[]{renderPointsSoA.getD1().get(i), renderPointsSoA.getD2().get(i), renderPointsSoA.getD3().get(i)});
			int rgb = rgbPoint[0];
	        rgb = (rgb << 8) + rgbPoint[1]; 
	        rgb = (rgb << 8) + rgbPoint[2];
	        image.setRGB(i % width, i / width, rgb);
		}
	}

	@Override
	public void run() {	
		int width = image.getWidth();		
//		Calculate the chunk of the matrix of the image for the thread. The matrix is split by columns and the chunk is evenly distributed between threads. max variance is 1
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
		
//		Based on a flag defined in the constructor apply one of the methods
		if (isAoS) renderAoS(width, startChunk, endChunk);
		else renderSoA(width, startChunk, endChunk);
		
		ph.arriveAndDeregister();
	}
}
