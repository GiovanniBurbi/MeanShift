package com.apt.project.mean_shift.utils;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.apt.project.mean_shift.model.Point;
import com.apt.project.mean_shift.model.PointsSoA;


public class ImageParser {
	private static final Logger LOGGER = Logger.getLogger(ImageParser.class.getName());

	private BufferedImage image;
	private int width;
	private int height;
	
	public ImageParser(String path) {
		try {
//			read image from path
			this.image = ImageIO.read(this.getClass().getResource(path));
			this.width = image.getWidth();
			this.height = image.getHeight();
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
	}
	
	public Raster getRaster() {
		return image.getRaster();
	}
	
//	Method to extract RGB points from an image and store it in an List
	public List<Point<Integer>> extractRGBPoints() {
		ArrayList<Point<Integer>> rgbPoints = new ArrayList<>();
		int[] pixel;
		Raster raster = image.getRaster();
		for (int i = 0; i < height; i++) {
	    	for (int j = 0; j < width; j++) {
	          pixel = raster.getPixel(j, i, new int[3]);
	          rgbPoints.add(new Point<>(pixel[0], pixel[1], pixel[2]));
	        }
	    }
		
		return rgbPoints;
	}
	
//	Method to extract LUV points from an image and store it in a List
	public List<Point<Double>> extractLUVPoints() {
		ArrayList<Point<Double>> luvPoints = new ArrayList<>();
		int[] pixel;
		Raster raster = image.getRaster();
		for (int i = 0; i < height; i++) {
	    	for (int j = 0; j < width; j++) {
	          pixel = raster.getPixel(j, i, new int[3]);
	          luvPoints.add(new Point<>(ColorConverter.convertToLUVPoint(pixel)));
	        }
	    }
		
		return luvPoints;
	}
	
//	Method to extract LUV points from an image and store it in an structure of arrays (SoA)
	public PointsSoA<Double> extractLUVPointsSoA() {
		ArrayList<Double> d1 = new ArrayList<>();
		ArrayList<Double> d2 = new ArrayList<>();
		ArrayList<Double> d3 = new ArrayList<>();

		int[] pixel;
		Raster raster = image.getRaster();
		for (int i = 0; i < height; i++) {
	    	for (int j = 0; j < width; j++) {
	          pixel = raster.getPixel(j, i, new int[3]);
	          Double[] point = ColorConverter.convertToLUVPoint(pixel);
	          d1.add(point[0]);
	          d2.add(point[1]);
	          d3.add(point[2]);
	        }
	    }
		return new PointsSoA<>(d1, d2, d3);
	}
	
//	Method to render an image from a list of RGB points and write it as a JPG image in the specified path.
	public void renderImage(List<Point<Integer>> points, String imageName) {
		BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); 
		for (int i = 0; i < width*height; i++) {
			Point<Integer> point = points.get(i);
			int rgb = point.getD1();
	        rgb = (rgb << 8) + point.getD2(); 
	        rgb = (rgb << 8) + point.getD3();
	        outputImage.setRGB( i % width, i / width, rgb);
		}
		
    	write(outputImage, imageName);
	}
	
//	Method to render an image from a list of RGB points and write it as a JPG image in the specified path.
	public void renderImageFromLUV(List<Point<Double>> points, String imageName) {
		BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); 
		for (int i = 0; i < width*height; i++) {
			Point<Integer> point = ColorConverter.convertToRGBPoint(points.get(i));
			int rgb = point.getD1();
	        rgb = (rgb << 8) + point.getD2(); 
	        rgb = (rgb << 8) + point.getD3();
	        outputImage.setRGB( i % width, i / width, rgb);
		}
		
    	write(outputImage, imageName);
	}
	
//	Method that creates an image as a bufferedImage from a list of RGB points
	public void renderImage(List<Point<Integer>> points) {
		BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); 
		for (int i = 0; i < width*height; i++) {
			Point<Integer> point = points.get(i);
			int rgb = point.getD1();
	        rgb = (rgb << 8) + point.getD2();
	        rgb = (rgb << 8) + point.getD3();
	        outputImage.setRGB( i % width, i / width, rgb);
		}
	}
	
//	Method that creates an image as a bufferedImage from a list of LUV points
	public void renderImageFromLUV(List<Point<Double>> points) {
		BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); 
		for (int i = 0; i < width*height; i++) {
			Point<Integer> point = ColorConverter.convertToRGBPoint(points.get(i));
			int rgb = point.getD1();
	        rgb = (rgb << 8) + point.getD2();
	        rgb = (rgb << 8) + point.getD3();
	        outputImage.setRGB( i % width, i / width, rgb);
		}
	}
	
	public void renderImageFromLUV(List<Point<Double>> points, int nThreads) {
		BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); 
		int numberOfElements = width;
		int minElementsPerThread = numberOfElements / nThreads;
		int threadsWithMoreElements = numberOfElements - nThreads * minElementsPerThread;
		int maxIndex;
		int start;
		for (int i = 0; i < nThreads; i++) {
			if (i < threadsWithMoreElements) {
				maxIndex = minElementsPerThread + 1;
				start = i * maxIndex;
			}
			else {
				maxIndex = minElementsPerThread;
				start = threadsWithMoreElements * (maxIndex + 1) + (i - threadsWithMoreElements) * maxIndex;
			}
			for (int k = 0; k < maxIndex*height; k++) {
				Point<Integer> point = ColorConverter.convertToRGBPoint(points.get(start*height + k));
				int rgb = point.getD1();
		        rgb = (rgb << 8) + point.getD2();
		        rgb = (rgb << 8) + point.getD3();
				outputImage.setRGB(start + k % maxIndex, k / maxIndex , rgb);					
			}
		}
	}
	
	public void renderImageFromLUV(List<Point<Double>> points, String imageName, int nThreads) {
		BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); 
		int numberOfElements = width;
		int minElementsPerThread = numberOfElements / nThreads;
		int threadsWithMoreElements = numberOfElements - nThreads * minElementsPerThread;
		int maxIndex;
		int start;
		for (int i = 0; i < nThreads; i++) {
			if (i < threadsWithMoreElements) {
				maxIndex = minElementsPerThread + 1;
				start = i * maxIndex;
			}
			else {
				maxIndex = minElementsPerThread;
				start = threadsWithMoreElements * (maxIndex + 1) + (i - threadsWithMoreElements) * maxIndex;
			}
			for (int k = 0; k < maxIndex*height; k++) {
				Point<Integer> point = ColorConverter.convertToRGBPoint(points.get(start*height + k));
				int rgb = point.getD1();
		        rgb = (rgb << 8) + point.getD2();
		        rgb = (rgb << 8) + point.getD3();
				outputImage.setRGB(start + k % maxIndex, k / maxIndex , rgb);					
			}
		}
		
		write(outputImage, imageName);
	}
	
//	Method to render an image from RGB points stored as a structure of arrays and write it as a JPG image in the path specified.
	public void renderImageSoA(PointsSoA<Integer> points, String imageName) {
		BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); 
		for (int i = 0; i < width*height; i++) {
			int rgb = points.getD1().get(i);
	        rgb = (rgb << 8) + points.getD2().get(i); 
	        rgb = (rgb << 8) + points.getD3().get(i);
	        outputImage.setRGB( i % width, i / width, rgb);
		}
		
    	write(outputImage, imageName);
	}
	
//	Method that creates an image as a bufferedImage from RGB points stored as structure of arrays
	public void renderImageSoA(PointsSoA<Integer> points) {
		BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); 
		for (int i = 0; i < width*height; i++) {
			int rgb = points.getD1().get(i);
	        rgb = (rgb << 8) + points.getD2().get(i); 
	        rgb = (rgb << 8) + points.getD3().get(i);
	        outputImage.setRGB( i % width, i / width, rgb);
		}
	}
	
//	Method that creates an image as a bufferedImage from RGB points stored as structure of arrays
	public void renderImageSoAFromLUV(PointsSoA<Double> points) {
		BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); 
		for (int i = 0; i < width*height; i++) {
			int[] point = ColorConverter.convertToRGBPoint(new Double[]{points.getD1().get(i), points.getD2().get(i), points.getD3().get(i)});
			int rgb = point[0];
	        rgb = (rgb << 8) + point[1]; 
	        rgb = (rgb << 8) + point[2];
	        outputImage.setRGB( i % width, i / width, rgb);
		}
	}
	
//	Method that creates an image as a bufferedImage from RGB points stored as structure of arrays and write it as a JPG image in the path specified.
	public void renderImageSoAFromLUV(PointsSoA<Double> points, String imageName) {
		BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); 
		for (int i = 0; i < width*height; i++) {
			int[] point = ColorConverter.convertToRGBPoint(new Double[]{points.getD1().get(i), points.getD2().get(i), points.getD3().get(i)});
			int rgb = point[0];
	        rgb = (rgb << 8) + point[1]; 
	        rgb = (rgb << 8) + point[2];
	        outputImage.setRGB( i % width, i / width, rgb);
		}
		
		write(outputImage, imageName);
	}
	
//	Method to write an JPG image from a bufferedImage in the given path
	public void write(BufferedImage image, String imageName) {
		String directoryName = "results";
		File directory = new File(directoryName);
		
		if(!directory.exists()) {
			directory.mkdir();
		}
		
		File outputFile = new File(directoryName, imageName);
    	try {
			ImageIO.write(image, "jpg", outputFile);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
	}
	
//	Method to print a list of points of a generic type
	public void printPoints(List<Point<?>> points) {
		points.forEach(p->LOGGER.info(p.toString()));
	}
	
//	Method to print the size of the image
	public void printImageSize() {
		LOGGER.info(this.image.getWidth() + " x " + this.image.getHeight());
	}
}
