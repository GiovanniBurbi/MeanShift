package com.apt.project.mean_shift.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.apt.project.mean_shift.model.Point;

public class CSVParser {
	
	private BufferedReader br;
	private ArrayList<Point<Double>> luvPoints = new ArrayList<>();
	private ArrayList<Point<Integer>> rgbPoints = new ArrayList<>();
	
	public void fetchCSVFile(String path) {
		try {
			InputStream inputStream = this.getClass().getResourceAsStream(path);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			this.br = new BufferedReader(inputStreamReader);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void write(List<Point<Double>> points, String outputFile) {
		java.io.File resultCSV = new java.io.File(outputFile);
		try {
			java.io.PrintWriter outfile = new java.io.PrintWriter(resultCSV);
			for (Point<Double> point: points) {
				outfile.write(point.toCSVString());
			}
			outfile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void writeRGB(List<Point<Integer>> points, String outputFile) {
		java.io.File resultCSV = new java.io.File(outputFile);
		try {
			java.io.PrintWriter outfile = new java.io.PrintWriter(resultCSV);
			for (Point<Integer> point: points) {
				outfile.write(point.toCSVString());
			}
			outfile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public List<Point<Double>> extractPoints() {
		Stream<String> lines = br.lines();
		lines.forEach(line -> {
			String[] stringPoint = line.split(",");
			Double[] point = Arrays.stream(stringPoint).map(Double::valueOf).toArray(Double[]::new);			
			this.luvPoints.add(new Point<>(point[0], point[1], point[2]));
		});
		return this.luvPoints;
	}
	
	public List<Point<Integer>> extractRGBPoints() {
		Stream<String> lines = br.lines();
		lines.forEach(line -> {
			String[] stringPoint = line.split(",");
			Integer[] point = Arrays.stream(stringPoint).map(Integer::valueOf).toArray(Integer[]::new);			
			this.rgbPoints.add(new Point<>(point[0], point[1], point[2]));
		});
		return this.rgbPoints;
	}
	
	public List<Point<Integer>> extractRGBPointsNewPath(String imgPath) {
		try {
			InputStream inputStream = this.getClass().getResourceAsStream(imgPath);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader reader = new BufferedReader(inputStreamReader);
			Stream<String> lines = reader.lines();
			lines.forEach(line -> {
				String[] stringPoint = line.split(",");
				Integer[] point = Arrays.stream(stringPoint).map(Integer::valueOf).toArray(Integer[]::new);			
				this.rgbPoints.add(new Point<>(point[0], point[1], point[2]));
			});
			return this.rgbPoints;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}
	
	public void printPoints() {
		this.luvPoints.forEach(p->System.out.println(p.toString()));
	}
	
	public void printRGBPoints() {
		this.rgbPoints.forEach(p->System.out.println(p.toString()));
	}
	
	public void printPoints(List<Point<Double>> pointsList) {
		pointsList.forEach(p->System.out.println(p.toString()));
	}
}
