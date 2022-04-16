package com.apt.project.mean_shift.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.apt.project.mean_shift.model.Point;
import com.apt.project.mean_shift.model.RGBPoint;


public class ImageParser {
//	private ArrayList<RGBPoint> rgbPoints = new ArrayList<>();
//	private ArrayList<Point> luvPoints = new ArrayList<>();
	private BufferedImage image;
	
	
	double ref_x = 95.047;
	double ref_y = 100.000;
	double ref_z = 108.883;
	double ref_u = (4 * ref_x) / (ref_x + (15 * ref_y) + (3 * ref_z));
	double ref_v = (9 * ref_y) / (ref_x + (15 * ref_y) + (3 * ref_z));

//	public ArrayList<RGBPoint> getRgbPoints() {
//		return rgbPoints;
//	}
//	
//	public ArrayList<Point> getLuvPoints() {
//		return luvPoints;
//	}
	
	public ImageParser(String path) {
		if (path == null) return;
		try {
			this.image = ImageIO.read(this.getClass().getResource(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<RGBPoint> extractRGBPoints() {
		ArrayList<RGBPoint> rgbPoints = new ArrayList<>();
		int height = image.getHeight();
		int width = image.getWidth();
		int[] pixel;
		for (int i = 0; i < height; i++) {
	    	for (int j = 0; j < width; j++) {
	          pixel = image.getRaster().getPixel(j, i, new int[3]);
	          rgbPoints.add(new RGBPoint(pixel[0], pixel[1], pixel[2]));
	        }
	    }
		
		return rgbPoints;
	}
	
	public void renderImage(ArrayList<RGBPoint> points) {
		BufferedImage image = new BufferedImage(100, 134, BufferedImage.TYPE_INT_RGB); 
		for (int j = 0; j < 134; j++) {
    		System.out.println(j);
    		for (int i = 0; i < 100; i++) {
    			int rgb = points.get(j*100 + i).getR();
		        rgb = (rgb << 8) + points.get(j*100 + i).getG(); 
		        rgb = (rgb << 8) + points.get(j*100 + i).getB();
		        image.setRGB(i, j, rgb);
		     }
		}
    	
    	File outputFile = new File("/home/giovanni/git/mean-shift/output2.jpg");
    	try {
			ImageIO.write(image, "jpg", outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void printRGBPoints(ArrayList<RGBPoint> rgbPoints) {
		rgbPoints.forEach(p->System.out.println(p.toString()));
	}
	
	public void printLUVPoints(ArrayList<Point> luvPoints) {
		luvPoints.forEach(p->System.out.println(p.toString()));
	}
	
	public void printImageSize() {
		System.out.println(this.image.getWidth() + " x " + this.image.getHeight());
	}
	
	public Point rgb2luv(RGBPoint point) {
		Point pointXYZ = this.rgb2xyz(point);
		return this.xyz2luv(pointXYZ);
	}
	
	public RGBPoint luv2rgb(Point point) {
		Point pointXYZ = this.luv2xyz(point);
		return this.xyz2rgb(pointXYZ);
	}
	
	public ArrayList<Point> convertToLUVPoints(ArrayList<RGBPoint> rgbPoints) {
		ArrayList<Point> luvPoints = new ArrayList<>();
		rgbPoints.forEach(p -> luvPoints.add(this.rgb2luv(p)));
		return luvPoints;
	}
	
	public ArrayList<RGBPoint> convertToRGBPoints(ArrayList<Point> luvPoints) {
		ArrayList<RGBPoint> rgbPoints = new ArrayList<>();
		luvPoints.forEach(p -> rgbPoints.add(this.luv2rgb(p)));
		return rgbPoints;
	}
	
	private Point rgb2xyz(RGBPoint point) {
		double red = (double) point.getR() / 255;
		double green = (double) point.getG() / 255;
		double blue = (double) point.getB() / 255;

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
		
		return new Point(x, y, z);
	}
	
	private Point xyz2luv(Point point) {
		double x = point.getX();
		double y = point.getY();
		double z = point.getZ();
		
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
		
		return new Point(l, u, v);
	}
	
	private Point luv2xyz(Point point) {
		double l = point.getX();
		double u = point.getY();
		double v = point.getZ();
		
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
		
		return new Point(x, y, z);
	}
	
	private RGBPoint xyz2rgb(Point point) {
		double x = point.getX() / 100;
		double y = point.getY() / 100;
		double z = point.getZ() / 100;
		
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
		
		return new RGBPoint((int)Math.round(r), (int)Math.round(g), (int)Math.round(b));
	}
}
