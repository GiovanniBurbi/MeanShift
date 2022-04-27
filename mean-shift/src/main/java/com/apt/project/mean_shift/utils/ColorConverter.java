package com.apt.project.mean_shift.utils;

import java.util.ArrayList;
import java.util.List;

import com.apt.project.mean_shift.model.Point;
import com.apt.project.mean_shift.model.PointsSoA;

public class ColorConverter {
	static final double REF_X = 95.047;
	static final double REF_Y = 100.000;
	static final double REF_Z = 108.883;
	static final double REF_U = (4 * REF_X) / (REF_X + (15 * REF_Y) + (3 * REF_Z));
	static final double REF_V = (9 * REF_Y) / (REF_X + (15 * REF_Y) + (3 * REF_Z));
	
	private ColorConverter() {}
	
	public static List<Point<Double>> convertToLUVPoints(List<Point<Integer>> rgbPoints) {
		ArrayList<Point<Double>> luvPoints = new ArrayList<>();
		rgbPoints.forEach(p -> luvPoints.add(rgb2luv(p)));
		return luvPoints;
	}
	
	public static List<Point<Integer>> convertToRGBPoints(List<Point<Double>> luvPoints) {
		ArrayList<Point<Integer>> rgbPoints = new ArrayList<>();
		luvPoints.forEach(p -> rgbPoints.add(luv2rgb(p)));
		return rgbPoints;
	}
	
	public static PointsSoA<Integer> convertToRGBPointsSoA(PointsSoA<Double> points) {
		ArrayList<Integer> d1 = new ArrayList<>();
		ArrayList<Integer> d2 = new ArrayList<>();
		ArrayList<Integer> d3 = new ArrayList<>();
		
		for (int i = 0; i < points.size(); i++) {
			int[] rgbPoint = convertToRGBPoint(new Double[]{points.getD1().get(i), points.getD2().get(i), points.getD3().get(i)});
			d1.add(rgbPoint[0]);
			d2.add(rgbPoint[1]);
			d3.add(rgbPoint[2]);
		}
		
		return new PointsSoA<>(d1, d2, d3);
	}
	
	public static Point<Double> convertToLUVPoint(Point<Integer> point) {
		return rgb2luv(point);
	}
	
	public static Double[] convertToLUVPoint(int[] point) {
		return rgb2luv(point);
	}
	
	public static Point<Integer> convertToRGBPoint(Point<Double> point) {
		return luv2rgb(point);
	}
	
	public static int[] convertToRGBPoint(Double[] point) {
		return luv2rgb(point);
	}
	
	private static Point<Double> rgb2luv(Point<Integer> point) {
		Point<Double> pointXYZ = rgb2xyz(point);
		return xyz2luv(pointXYZ);
	}
	
	private static Double[] rgb2luv(int[] point) {
		Double[] pointXYZ = rgb2xyz(point);
		return xyz2luv(pointXYZ);
	}
	
	private static Point<Integer> luv2rgb(Point<Double> point) {
		Point<Double> pointXYZ = luv2xyz(point);
		return xyz2rgb(pointXYZ);
	}
	
	private static int[] luv2rgb(Double[] point) {
		Double[] pointXYZ = luv2xyz(point);
		return xyz2rgb(pointXYZ);
	}
	
	private static Point<Double> rgb2xyz(Point<Integer> point) {
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
	
	private static Double[] rgb2xyz(int[] point) {
		double red = (double) point[0] / 255;
		double green = (double) point[1] / 255;
		double blue = (double) point[2] / 255;

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
		
		return new Double[]{x, y, z};
	}
	
	private static Point<Double> xyz2luv(Point<Double> point) {
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
		u = 13 * l * ( u - REF_U );
		v = 13 * l * ( v - REF_V );
		
		return new Point<>(l, u, v);
	}
	
	private static Double[] xyz2luv(Double[] point) {
		double x = point[0];
		double y = point[1];
		double z = point[2];
		
		if (x == 0 && y == 0 && z == 0) return new Double[]{0.0, 0.0, 0.0};
		
		double u = ( 4 * x ) / ( x + ( 15 * y ) + ( 3 * z ));
		double v = ( 9 * y ) / ( x + ( 15 * y ) + ( 3 * z ));

		y = y / 100;
		if ( y > 0.008856 )
			y = Math.pow(y, (double) 1/3);
		else
			y = ( 7.787 * y ) + ( (double) 16/116 );

		double l = ( 116 * y ) - 16;
		u = 13 * l * ( u - REF_U );
		v = 13 * l * ( v - REF_V );
		
		return new Double[]{l, u, v};
	}
	
	private static Point<Double> luv2xyz(Point<Double> point) {
		double l = point.getD1();
		double u = point.getD2();
		double v = point.getD3();
		
		double y = (l + 16) / 116;
		double powY = Math.pow(y, 3);
		if (powY > 0.008856)
			y = powY;
		else
			y = (y - (double) 16/116) / 7.787;
		
		u = u / (13 * l) + REF_U;
		v = v / (13 * l) + REF_V;
		
		y = y * 100;
		double x = - (9 * y * u) / ((u - 4) * v - u * v);
		double z = (9 * y - (15 * v * y) - (v * x)) / (3 * v);
		
		return new Point<>(x, y, z);
	}
	
	private static Double[] luv2xyz(Double[] point) {
		double l = point[0];
		double u = point[1];
		double v = point[2];
		
		double y = (l + 16) / 116;
		double powY = Math.pow(y, 3);
		if (powY > 0.008856)
			y = powY;
		else
			y = (y - (double) 16/116) / 7.787;
		
		u = u / (13 * l) + REF_U;
		v = v / (13 * l) + REF_V;
		
		y = y * 100;
		double x = - (9 * y * u) / ((u - 4) * v - u * v);
		double z = (9 * y - (15 * v * y) - (v * x)) / (3 * v);
		
		return new Double[]{x, y, z};
	}
	
	private static Point<Integer> xyz2rgb(Point<Double> point) {
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
	
	private static int[] xyz2rgb(Double[] point) {
		double x = point[0] / 100;
		double y = point[1] / 100;
		double z = point[2] / 100;
		
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
		
		return new int[]{(int)Math.round(r), (int)Math.round(g), (int)Math.round(b)};
	}
}
