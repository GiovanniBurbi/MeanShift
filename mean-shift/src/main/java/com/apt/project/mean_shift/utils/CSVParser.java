package com.apt.project.mean_shift.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import com.apt.project.mean_shift.model.Point;
import com.apt.project.mean_shift.model.RGBPoint;

public class CSVParser {
	
	private BufferedReader br;
	private ArrayList<Point> points = new ArrayList<>();
	private ArrayList<RGBPoint> rgbPoints = new ArrayList<>();
	
	public CSVParser() {}
	
	public CSVParser(String path) {		
		try {
			InputStream inputStream = this.getClass().getResourceAsStream(path);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			this.br = new BufferedReader(inputStreamReader);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void write(ArrayList<Point> points, String outputFile) {
		java.io.File resultCSV = new java.io.File(outputFile);
		try {
			java.io.PrintWriter outfile = new java.io.PrintWriter(resultCSV);
			for (int i=0; i < points.size() ; i++) {
		        outfile.write(points.get(i).toCSVString());
		    }
			outfile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void writeRGB(ArrayList<RGBPoint> points, String outputFile) {
		java.io.File resultCSV = new java.io.File(outputFile);
		try {
			java.io.PrintWriter outfile = new java.io.PrintWriter(resultCSV);
			for (int i=0; i < points.size() ; i++) {
		        outfile.write(points.get(i).toCSVString());
		    }
			outfile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<Point> extractPoints() {
		Stream<String> lines = br.lines();
		lines.forEach(line -> {
			String[] stringPoint = line.split(",");
			Double[] point = Arrays.stream(stringPoint).map(Double::valueOf).toArray(Double[]::new);			
			this.points.add(new Point(point[0], point[1], point[2]));
		});
		return points;
	}
	
	public ArrayList<RGBPoint> extractRGBPoints() {
		Stream<String> lines = br.lines();
		lines.forEach(line -> {
			String[] stringPoint = line.split(",");
			Integer[] point = Arrays.stream(stringPoint).map(Integer::valueOf).toArray(Integer[]::new);			
			this.getRgbPoints().add(new RGBPoint(point[0], point[1], point[2]));
		});
		return getRgbPoints();
	}
	
	public ArrayList<RGBPoint> extractRGBPointsNewPath(String imgPath) {
		try {
			InputStream inputStream = this.getClass().getResourceAsStream(imgPath);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader reader = new BufferedReader(inputStreamReader);
			Stream<String> lines = reader.lines();
			lines.forEach(line -> {
				String[] stringPoint = line.split(",");
				Integer[] point = Arrays.stream(stringPoint).map(Integer::valueOf).toArray(Integer[]::new);			
				this.getRgbPoints().add(new RGBPoint(point[0], point[1], point[2]));
			});
			return getRgbPoints();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void printPoints() {
		this.points.forEach(p->System.out.println(p.toString()));
	}
	
	public void printRGBPoints() {
		this.getRgbPoints().forEach(p->System.out.println(p.toString()));
	}
	
	public void printPoints(ArrayList<Point> pointsList) {
		pointsList.forEach(p->System.out.println(p.toString()));
	}

	public ArrayList<RGBPoint> getRgbPoints() {
		return rgbPoints;
	}
}
