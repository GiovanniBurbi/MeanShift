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


public class ImageParser {
	private static final Logger LOGGER = Logger.getLogger(ImageParser.class.getName());

	private BufferedImage image;
	private int width;
	private int height;
	
	public ImageParser(String path) {
		try {
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
	
	public List<Point<Double>> extractLUVPoints() {
		ArrayList<Point<Double>> luvPoints = new ArrayList<>();
		int[] pixel;
		Raster raster = image.getRaster();
		for (int i = 0; i < height; i++) {
	    	for (int j = 0; j < width; j++) {
	          pixel = raster.getPixel(j, i, new int[3]);
	          luvPoints.add(ColorConverter.convertToLUVPoint(new Point<>(pixel[0], pixel[1], pixel[2])));
	        }
	    }
		
		return luvPoints;
	}
	
	
	public void renderImage(List<Point<Integer>> points, String path) {
		BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); 
		for (int i = 0; i < height; i++) {
    		for (int j = 0; j < width; j++) {
    			Point<Integer> point = points.get(i*width + j);
    			int rgb = point.getD1();
		        rgb = (rgb << 8) + point.getD2(); 
		        rgb = (rgb << 8) + point.getD3();
		        outputImage.setRGB(j, i, rgb);
		     }
		}
		
    	File outputFile = new File(path);
    	try {
			ImageIO.write(outputImage, "jpg", outputFile);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
	}
	
	public void renderImageOneLoop(List<Point<Integer>> points, String path) {
		BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); 
		for (int i = 0; i < width*height; i++) {
			Point<Integer> point = points.get(i);
			int rgb = point.getD1();
	        rgb = (rgb << 8) + point.getD2(); 
	        rgb = (rgb << 8) + point.getD3();
	        outputImage.setRGB( i % width, i / width, rgb);
		}
		
    	File outputFile = new File(path);
    	try {
			ImageIO.write(outputImage, "jpg", outputFile);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
	}
	
	public void renderImageWithoutWrite(List<Point<Integer>> points) {
		BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); 
		for (int i = 0; i < height; i++) {
    		for (int j = 0; j < width; j++) {
    			Point<Integer> point = points.get(i*width + j);
    			int rgb = point.getD1();
		        rgb = (rgb << 8) + point.getD2(); 
		        rgb = (rgb << 8) + point.getD3();
		        outputImage.setRGB(j, i, rgb);
		     }
		}
	}
	
	public void renderImageWithoutWriteOneLoop(List<Point<Integer>> points) {
		BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); 
		for (int i = 0; i < width*height; i++) {
			Point<Integer> point = points.get(i);
			int rgb = point.getD1();
	        rgb = (rgb << 8) + point.getD2(); 
	        rgb = (rgb << 8) + point.getD3();
	        outputImage.setRGB( i % width, i / width, rgb);
		}
	}
	
	public void write(BufferedImage image, String path) {
		File outputFile = new File(path);
    	try {
			ImageIO.write(image, "jpg", outputFile);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
	}
	
	public void printPoints(List<Point<?>> points) {
		points.forEach(p->LOGGER.info(p.toString()));
	}
	
	public void printImageSize() {
		LOGGER.info(this.image.getWidth() + " x " + this.image.getHeight());
	}
}
