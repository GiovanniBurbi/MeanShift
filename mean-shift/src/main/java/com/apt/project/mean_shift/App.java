package com.apt.project.mean_shift;

import java.util.ArrayList;


import com.apt.project.mean_shift.algorithm.MeanShift;
import com.apt.project.mean_shift.model.Point;
import com.apt.project.mean_shift.model.RGBPoint;
import com.apt.project.mean_shift.utils.ImageParser;

public class App 
{
    public static void main( String[] args )
    {
    	
//    	Sequential version
    	
    	
//    	For images
    	
    	ImageParser ip = new ImageParser("/images/imageSmall.jpg");
    	ArrayList<RGBPoint> rgbPoints = ip.extractRGBPoints();
    	ArrayList<Point> luvPoints = ip.convertToLUVPoints(rgbPoints);   	
    	MeanShift meanShift = new MeanShift(1f, 5, luvPoints);
    	ArrayList<Point> shiftedPoints = meanShift.meanShiftAlgorithm();
    	rgbPoints = ip.convertToRGBPoints(shiftedPoints);
    	ip.renderImage(rgbPoints, "results/resultImage.jpg");
    	
    	
//    	For csv files
    	
//    	CSVParser p = new CSVParser("/points/100.csv");
//    	ArrayList<Point> points = p.extractPoints();
//    	MeanShift meanShift = new MeanShift(2, 10, points);
//    	ArrayList<Point> shiftedPoints = meanShift.meanShiftAlgorithm();
//    	p.write(shiftedPoints);
//    	p.printPoints();
    	
    	
//    	Parallel version
    	
    }
}
