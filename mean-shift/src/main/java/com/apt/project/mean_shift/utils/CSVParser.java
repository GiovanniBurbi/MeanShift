package com.apt.project.mean_shift.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import com.apt.project.mean_shift.model.Point;

public class CSVParser {
	
	private BufferedReader br;
	private ArrayList<Point> points = new ArrayList<>();
	
	public CSVParser(String path) {		
		try {
			InputStream inputStream = this.getClass().getResourceAsStream(path);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			this.br = new BufferedReader(inputStreamReader);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void extractPoints() {
		Stream<String> lines = br.lines();
		lines.forEach(line -> {
			String[] stringPoint = line.split(",");
			Double[] point = Arrays.stream(stringPoint).map(Double::valueOf).toArray(Double[]::new);			
			this.points.add(new Point(point[0], point[1], point[2]));
		});
	}
	
	public void printPoints() {
		this.points.forEach(p->System.out.println(p.toString()));
	}
}
