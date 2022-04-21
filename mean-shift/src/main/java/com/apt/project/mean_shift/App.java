package com.apt.project.mean_shift;

import java.util.List;

import com.apt.project.mean_shift.algorithm.MeanShift;
import com.apt.project.mean_shift.model.Point;
//import com.apt.project.mean_shift.utils.CSVParser;
import com.apt.project.mean_shift.utils.ColorConverter;
import com.apt.project.mean_shift.utils.ImageParser;

public class App 
{
    public static void main( String[] args )
    {
    	
//    	Sequential version
    	
    	
//    	For images
    	
//    	CSVParser p = new CSVParser();
    	ImageParser ip = new ImageParser("/images/benzina.jpg");
    	List<Point<Integer>> rgbPoints = ip.extractRGBPoints();
    	List<Point<Double>> luvPoints = ColorConverter.convertToLUVPoints(rgbPoints);
//    	p.write(luvPoints, "originPoints.csv");
    	MeanShift meanShift = new MeanShift(12f, 5, luvPoints);
    	List<Point<Double>> shiftedPoints = meanShift.meanShiftAlgorithm();
//    	p.write(shiftedPoints, "shiftedPoints.csv");
    	List<Point<Integer>> rgbShiftedPoints = ColorConverter.convertToRGBPoints(shiftedPoints);
    	ip.renderImage(rgbShiftedPoints, "results/resultBenzinaBW12Iter5.jpg");
    	
    	
//    	For csv files
    	
//    	CSVParser p = new CSVParser();
//    	p.fetchCSVFile("/points/100.csv");
//    	List<Point<Double>> points = p.extractPoints();
//    	MeanShift meanShift = new MeanShift(2, 10, points);
//    	List<Point<Double>> shiftedPoints = meanShift.meanShiftAlgorithm();
//    	p.write(shiftedPoints, "shiftedPoints.csv");
    	
    	
//    	Parallel version
    	
    }
}
