package com.apt.project.mean_shift.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.apt.project.mean_shift.model.Point;


public class ImageParser {
	private BufferedImage image;
	
	
	final double ref_x = 95.047;
	final double ref_y = 100.000;
	final double ref_z = 108.883;
	final double ref_u = (4 * ref_x) / (ref_x + (15 * ref_y) + (3 * ref_z));
	final double ref_v = (9 * ref_y) / (ref_x + (15 * ref_y) + (3 * ref_z));
	
	public ImageParser(String path) {
		try {
			this.image = ImageIO.read(this.getClass().getResource(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<Point<Integer>> extractRGBPoints() {
		ArrayList<Point<Integer>> rgbPoints = new ArrayList<>();
		int height = image.getHeight();
		int width = image.getWidth();
		int[] pixel;
		for (int i = 0; i < height; i++) {
	    	for (int j = 0; j < width; j++) {
	          pixel = image.getRaster().getPixel(j, i, new int[3]);
	          rgbPoints.add(new Point<>(pixel[0], pixel[1], pixel[2]));
	        }
	    }
		
		return rgbPoints;
	}
	
	public void renderImage(List<Point<Integer>> points, String path) {
		int width = image.getWidth();
		int height = image.getHeight();
		BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); 
		for (int j = 0; j < height; j++) {
    		for (int i = 0; i < width; i++) {
    			int rgb = points.get(j*width + i).getD1();
		        rgb = (rgb << 8) + points.get(j*width + i).getD2(); 
		        rgb = (rgb << 8) + points.get(j*width + i).getD3();
		        outputImage.setRGB(i, j, rgb);
		     }
		}
		
    	File outputFile = new File(path);
    	try {
			ImageIO.write(outputImage, "jpg", outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void printRGBPoints(List<Point<Integer>> rgbPoints) {
		rgbPoints.forEach(p-> {
			if ((p.getD1() < 0 || p.getD1() > 255) || (p.getD2() < 0 || p.getD2() > 255) || (p.getD3() < 0 || p.getD3() > 255))
				System.out.println(p.toString());
		});
	}
	
	public void printLUVPoints(List<Point<Double>> luvPoints) {
		luvPoints.forEach(p-> {
			if ((p.getD1() < 0 || p.getD1() > 100) || (p.getD2() < -134 || p.getD2() > 224) || (p.getD3() < -140 || p.getD3() > 122))
				System.out.println(p.toString());
		});
//		luvPoints.forEach(p->System.out.println(p.toString()));
	}
	
	public void printImageSize() {
		System.out.println(this.image.getWidth() + " x " + this.image.getHeight());
	}
	
	public Point<Double> rgb2luv(Point<Integer> point) {
		Point<Double> pointXYZ = this.rgb2xyz(point);
		return this.xyz2luv(pointXYZ);
	}
	
	public Point<Integer> luv2rgb(Point<Double> point) {
		Point<Double> pointXYZ = this.luv2xyz(point);
		return this.xyz2rgb(pointXYZ);
	}
	
	public List<Point<Double>> convertToLUVPoints(List<Point<Integer>> rgbPoints) {
		ArrayList<Point<Double>> luvPoints = new ArrayList<>();
		rgbPoints.forEach(p -> luvPoints.add(this.rgb2luv(p)));
		return luvPoints;
	}
	
	public List<Point<Integer>> convertToRGBPoints(List<Point<Double>> luvPoints) {
		ArrayList<Point<Integer>> rgbPoints = new ArrayList<>();
		luvPoints.forEach(p -> rgbPoints.add(this.luv2rgb(p)));
		return rgbPoints;
	}
	
	public Point<Double> rgb2xyz(Point<Integer> point) {
		double red = (double) point.getD1() / 255;
		double green = (double) point.getD2() / 255;
		double blue = (double) point.getD3() / 255;

		if (red > 0.04045) {
			red = Math.pow(((red + 0.055) / 1.055), 2.4);
		}
		else  
			red = red / 12.92;
		
		if (green > 0.04045) 
			green = Math.pow(((green + 0.055) / 1.055), 2.4);
		else
			green = green / 12.92;

		if (blue > 0.04045)
			blue = Math.pow(((blue + 0.055) / 1.055), 2.4);
		else
			blue = blue / 12.92;

		red = red * 100;
		green = green * 100;
		blue = blue * 100;

		double x = red * 0.4124 + green * 0.3576 + blue * 0.1805;
		double y = red * 0.2126 + green * 0.7152 + blue * 0.0722;
		double z = red * 0.0193 + green * 0.1192 + blue * 0.9505;
		
		return new Point<>(x, y, z);
	}
	
	public Point<Double> xyz2luv(Point<Double> point) {
		double x = point.getD1();
		double y = point.getD2();
		double z = point.getD3();
		
		if (x == 0 && y == 0 && z == 0) return new Point<>(0.0, 0.0, 0.0);
		
		double u = ( 4 * x ) / ( x + ( 15 * y ) + ( 3 * z ));
		double v = ( 9 * y ) / ( x + ( 15 * y ) + ( 3 * z ));

		y = y / 100;
		if ( y > 0.008856 )
			y = Math.pow(y, (double) 1/3);
		else
			y = ( 7.787 * y ) + ( (double) 16/116 );

		double l = ( 116 * y ) - 16;
		u = 13 * l * ( u - ref_u );
		v = 13 * l * ( v - ref_v );
		
		return new Point<>(l, u, v);
	}
	
	public Point<Double> luv2xyz(Point<Double> point) {
		double l = point.getD1();
		double u = point.getD2();
		double v = point.getD3();
		
		double y = (l + 16) / 116;
		double y_pow = Math.pow(y, 3);
		if (y_pow > 0.008856)
			y = y_pow;
		else
			y = (y - (double) 16/116) / 7.787;
		
		u = u / (13 * l) + ref_u;
		v = v / (13 * l) + ref_v;
		
		y = y * 100;
		double x = - (9 * y * u) / ((u - 4) * v - u * v);
		double z = (9 * y - (15 * v * y) - (v * x)) / (3 * v);
		
		return new Point<>(x, y, z);
	}
	
	public Point<Integer> xyz2rgb(Point<Double> point) {
		double x = point.getD1() / 100;
		double y = point.getD2() / 100;
		double z = point.getD3() / 100;
		
		double r = x * 3.2406 + y * -1.5372 + z * -0.4986;
		double g = x * -0.9689 + y * 1.8758 + z * 0.0415;
		double b = x * 0.0557 + y * -0.2040 + z * 1.0570;
		
		double pow = 1 / 2.4;
		
		if (r > 0.0031308)
			r = 1.055 * (Math.pow(r, pow)) - 0.055;
		else
			r = 12.92 * r;
		
		if (g > 0.0031308)
			g = 1.055 * (Math.pow(g, pow)) - 0.055;
		else
			g = 12.92 * g;
		
		if (b > 0.0031308)
			b = 1.055 * (Math.pow(b, pow)) - 0.055;
		else
			b = 12.92 * b;
		
		r = r * 255;
		g = g * 255;
		b =  b * 255;
		
		return new Point<>((int)Math.round(r), (int)Math.round(g), (int)Math.round(b));
	}
}
