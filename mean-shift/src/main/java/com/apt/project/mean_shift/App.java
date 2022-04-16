package com.apt.project.mean_shift;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

import com.apt.project.mean_shift.algorithm.MeanShift;
import com.apt.project.mean_shift.model.Point;
//import com.apt.project.mean_shift.algorithm.MeanShift;
//import com.apt.project.mean_shift.model.Point;
import com.apt.project.mean_shift.model.RGBPoint;
import com.apt.project.mean_shift.utils.CSVParser;
import com.apt.project.mean_shift.utils.ImageParser;
//import com.apt.project.mean_shift.utils.ImageParser;

public class App 
{
    public static void main( String[] args )
    {
    	
//    	CSVParser p = new CSVParser("/points/100.csv");
//    	ArrayList<Point> points = p.extractPoints();
//    	MeanShift meanShift = new MeanShift(2, 10, points);
//    	ArrayList<Point> shiftedPoints = meanShift.meanShiftAlgorithm();
//    	p.write(shiftedPoints);
//    	p.printPoints();
    	
    	CSVParser p = new CSVParser("/points/imageResult.csv");
    	p.extractRGBPoints();
    	ArrayList<RGBPoint> points = p.getRgbPoints();
    	ImageParser imgParser = new ImageParser(null);
    	imgParser.renderImage(points);
    	
    	
//    	Integer[] rgbPoints = p.getRgbPoints().toArray();
//    	BufferedImage img = new BufferedImage(100, 134, BufferedImage.TYPE_INT_RGB);
//    	img.getRaster().setPixels(0, 0, 100, 134, );
    	
    	
    	
//    	CSVParser p = new CSVParser();
//    	ImageParser ip = new ImageParser("/images/imageSmall.jpg");
//    	ArrayList<RGBPoint> rgbPoints = ip.extractRGBPoints();
//    	ArrayList<Point> luvPoints = ip.convertToLUVPoints(rgbPoints);
//    	ip.printLUVPoints(luvPoints);
   	
//    	MeanShift meanShift = new MeanShift(1f, 5, luvPoints);
//    	ArrayList<Point> shiftedPoints = meanShift.meanShiftAlgorithm();
//    	rgbPoints = ip.convertToRGBPoints(shiftedPoints);
//    	p.writeRGB(rgbPoints, "imageResult");
    	
    }
}
