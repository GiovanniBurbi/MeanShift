package com.apt.project.mean_shift.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.apt.project.mean_shift.model.Point;



public class CSVParser {
	private static final Logger LOGGER = Logger.getLogger(CSVParser.class.getName());
	
	private BufferedReader br;
	
	public void fetchCSVFile(String path) {
		try {
			InputStream inputStream = this.getClass().getResourceAsStream(path);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			this.br = new BufferedReader(inputStreamReader);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
	}
	
	public void write(List<? extends Point<?>> points, String outputFile) {
		java.io.File resultCSV = new java.io.File(outputFile);
		try {
			java.io.PrintWriter outfile = new java.io.PrintWriter(resultCSV);
			for (Point<?> point: points) {
				outfile.write(point.toCSVString());
			}
			outfile.close();
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
	}
	
	public List<Point<Double>> extractPoints() {
		ArrayList<Point<Double>> points = new ArrayList<>();
		Stream<String> lines = br.lines();
		lines.forEach(line -> {
			String[] stringPoint = line.split(",");
			Double[] point = Arrays.stream(stringPoint).map(Double::valueOf).toArray(Double[]::new);			
			points.add(new Point<>(point[0], point[1], point[2]));
		});
		return points;
	}
	
	public void printPoints(List<? extends Point<?>> pointsList) {
		pointsList.forEach(p->LOGGER.info(p.toString()));
	}
}
