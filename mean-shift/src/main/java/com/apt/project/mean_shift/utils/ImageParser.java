package com.apt.project.mean_shift.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.apt.project.mean_shift.model.RGBPoint;

public class ImageParser {
	private ArrayList<RGBPoint> points = new ArrayList<>();
	BufferedImage image;
	
	public ImageParser(String path) {
		try {
			this.image = ImageIO.read(this.getClass().getResource(path));
			int height = image.getHeight();
			int width = image.getWidth();
			int[] pixel;
			for (int i = 0; i < height; i++) {
		    	for (int j = 0; j < width; j++) {
		          pixel = image.getRaster().getPixel(j, i, new int[3]);
		          this.points.add(new RGBPoint(pixel[0], pixel[1], pixel[2]));
		        }
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void printPoints() {
		this.points.forEach(p->System.out.println(p.toString()));
//		System.out.println(this.image.getWidth() + " X " + this.image.getHeight());
	}
}
