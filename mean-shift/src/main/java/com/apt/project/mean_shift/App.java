package com.apt.project.mean_shift;

//import com.apt.project.mean_shift.utils.CSVParser;
import com.apt.project.mean_shift.utils.ImageParser;

public class App 
{
    public static void main( String[] args )
    {
    	
//    	CSVParser p = new CSVParser("/points/100.csv");
//    	p.extractPoints();
//    	p.printPoints();
    	
    	ImageParser ip = new ImageParser("/images/image.jpg");
    	ip.printPoints();
    }
}
