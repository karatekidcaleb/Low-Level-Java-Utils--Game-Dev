/**************************************************
Created by Caleb Leeb 8/5/2021

These functions are intended to decode a 24-bit bitmap image in memory into a 
multidimensional array organized by coordinates and color channels. 
Mapping follows the convention raster[x-coord][y-coord][color], starting from the 
bottom left of the image and with color indexes corresponding to BGR (i.e. 
[0] indexes Blue rather than Red). 

As of the posting of this code only square resolution bitmaps are able to be
rasterized. Additionally any true 0x00 values (i.e. pure black, red, blue & green)
will be discarded as the current code interprets them as byte padding used in the
bitmap image encoding scheme.

This code was produced because I was not able to find the same functionality in 
existing libraries while producing a game for LowRez Jam 2022.

Enjoy and feel free to modify (or optimize <3)!
**************************************************/
package com;

import java.io.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

class RasterFunctions {
	public static byte[][][] toRaster (String filePath) throws IOException {
		
	BufferedImage image = ImageIO.read(new File(filePath));
	ByteArrayOutputStream outStreamObj = new ByteArrayOutputStream();
	ImageIO.write(image, "bmp", outStreamObj);
	// create array of bytes from bitmap
	byte [] byteArray = outStreamObj.toByteArray();
	
	int bufferLength = (int)(Math.sqrt((byteArray.length - 54)/3)) % 4; 
	//used for calculating bitmap conversion to bytearray and back 
	//(the encoding format for bitmap pads 00s haphazardly into the pixel data to save rows without half-words or shorts)
	
	byte [][][] modArray = RawToRaster(byteArray); 
	return modArray;
	}
	
	public static void writeRasterToImage (byte[][][] modArray, String inputPath, String outputPath) throws IOException
	{
		BufferedImage image = ImageIO.read(new File(inputPath));
		ByteArrayOutputStream outStreamObj = new ByteArrayOutputStream();
		ImageIO.write(image, "bmp", outStreamObj);
		// create array of bytes from bitmap
		byte [] byteArray = outStreamObj.toByteArray();
	
		int bufferLength = (int)(Math.sqrt((byteArray.length - 54)/3)) % 4;
		//used for calculating bitmap conversion to bytearray and back
		//(the encoding format for bitmap pads 00s haphazardly into the pixel data to save rows without half-words or shorts)
		byteArray = RasterToWriteableBuffer(modArray, byteArray, bufferLength);
		//returns altered array of easily modified pixels into their bitmap encoded home :)

		ByteArrayInputStream inStreambj = new ByteArrayInputStream(byteArray);
		// turn byte array
		BufferedImage newImage = ImageIO.read(inStreambj);
		ImageIO.write(newImage, "bmp", new File(outputPath));
		System.out.println("Image generated from the byte array.");
	}
	
	//converts square bitmap images into x,y coordinated byte arrays with RGB channels
	public static byte[][][] RawToRaster(byte[] raw)
	{
		byte[] metadata = new byte[54];
		byte[] data = new byte[raw.length-54];
		
		//seperate the raw bitmap bytes into header data and pixel data
		for(int i=0; i<54; i++)
			metadata[i] = raw[i];
		for(int i=0; i<raw.length-54; i++)
			data[i] = raw[54+i];
		
		int rowLength = (int)(Math.sqrt((data.length)/3)); //how many pixels are in each row
		int bufferLength = (rowLength) % 4;		//how many bytes are appended to the end of the row to make a full word
		
		data = removeBufferBits(rowLength, bufferLength, data);
		
		byte [][][] raster = new byte[rowLength][rowLength][3];
		for (int i=0; i<data.length; i++)
		{
			int currRow = i / (3*rowLength); //what row in the final raster
			int currIndex = (i / 3) % rowLength;  //what position in that row the pixel is
			int currRGB = i % 3;
			raster[currRow][currIndex][currRGB] = data[i];
		}
		System.out.println(rowLength + " " +bufferLength);
		return raster;
	}
	
	private static byte[] removeBufferBits(int rowLength, int bufferLength, byte[] data)
	{
		byte[] dataNoBuffer = new byte[data.length-(rowLength*bufferLength)];
		
		int q = 0;
		for(int i=0; i<data.length; i++) //remove buffer bits (and maybe some blue from the edges, just for fun [BUG]!)
		{	if(data[i]!=0){
				dataNoBuffer[q] = data[i];
				q++;
			}}
		return dataNoBuffer;
	}
	
	private static byte[] RasterToWriteableBuffer(byte[][][] modArray, byte[] byteArray, int bufferLength)
	{
		System.out.println(modArray.length + " " + modArray[0].length + " " + modArray[0][0].length + ":" + byteArray.length);
		int y = 54;
		for(int i=0; i<modArray.length; i++)
		{
			for(int j=0; j<modArray[0].length; j++)
			{
				for(int k=0; k<modArray[0][0].length; k++)
				{
					byteArray[y] = modArray[i][j][k];
					y++;
				}}
			y+=bufferLength;}
		return byteArray;
	}
	
	//Try it out!
	public static void main(String[] args)
	{
		String filePath = "resources/test_bmp.bmp";
		String outputPath = "resources/output.bmp";
		try{
			double elapse = System.currentTimeMillis();
			
			byte[][][] raster = toRaster(filePath);
			//Main functionality of file: turn any .bmp saved in memory to a raster byte array
			
			raster[1][1][2] = (byte)0xFF; //changes pixel colors of near bottom left pixel to Red!
			//Try looping and creating patterns via coordinates with mathematical functions of your choice
			
			writeRasterToImage(raster, filePath, outputPath); 
			//Function which writes modified raster graphics to .bmp file in memory. 
			//Ensure to specify the same source .bmp or a .bmp of similar dimensionality for proper write!
			
			System.out.println("Operation Completed in " + (System.currentTimeMillis()-elapse) + "ms");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	
}

