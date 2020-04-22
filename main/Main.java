package main;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

import utils.*;

public class Main {

	public static void main(String[] args) {
		File file = new File("text1.bin");
		TaskManager tm = new TaskManager();
		int keys=20;									//
		int sucDiv=0;
		int failDiv=0;
		
		try {
			System.out.println("COMP211 Excercise 1: File Editing & External Sorting");
			System.out.println("");
			System.out.println("");
			System.out.println("");
			
			System.out.println("Creating a new binary file with 100000 integers...");
			tm.randomNumGenerator(file.getName());
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
			ToCsv.toCsv(file.getName());
			System.out.println("File created with success!");
			System.out.println("");
			System.out.println("Number Of Accesses To The Disk: "+TaskManager.numOfDiskAccess);
			System.out.println("");
			
			System.out.println("Starting the sorting of the file's elements..");
			System.out.println("First, we split the file into 10 smaller ones.");
			tm.fileSort(file.getName());
			System.out.println("File split with success!");
			System.out.println("");
			System.out.println("Number Of Accesses To The Disk: "+TaskManager.numOfDiskAccess);
			System.out.println("");
			
			System.out.println("Now, we merge the files into one which will differ from the first \n"
					+ "file, as its elements will be sorted");
			tm.mergeFiles();
			System.out.println("The files merged with success! Now there are only two bin files: \n"
					+ "the first one and the sorted one");
			System.out.println("Number Of Accesses To The Disk: "+TaskManager.numOfDiskAccess);
			TaskManager.numOfDiskAccess=0;
			System.out.println("");
			
			int[] values = testArray(keys, file);
			
			System.out.println("Now we test two algorithms concerning search in a sorted binary file.");
			System.out.println("We 'll try to seek 20 numbers that exist in the file and 20 more \n"
					+ "that don't.");
			System.out.println("");
			System.out.println("Linear Search for Existent/Non-Existent Numbers:");
			for (int i=0; i<2*keys; i++) {
				if(tm.linearSearch("sortedText.bin", values[i])) {
					sucDiv+=TaskManager.numOfDiskAccess;
				}
				else {
					failDiv+=TaskManager.numOfDiskAccess;
				}
				TaskManager.numOfDiskAccess=0;
			}
			System.out.println("");
			System.out.println("\'Number of Accesses to the Disk\' Ratio for Successful searches: "+sucDiv/keys);
			System.out.println("\'Number of Accesses to the Disk\' Ratio for Failed searches: "+failDiv/keys);
			
			sucDiv=0; failDiv=0;
			
			System.out.println("");
			System.out.println("Binary Search for Existent/Non-Existent Numbers:");
			for (int i=0; i<2*keys; i++) {
				if(tm.binarySearch("sortedText.bin", values[i], 0, tm.getNumOfPages()-1)) {
					sucDiv+=TaskManager.numOfDiskAccess;
				}
				else {
					failDiv+=TaskManager.numOfDiskAccess;
				}
				TaskManager.numOfDiskAccess=0;
			}
			System.out.println("");
			System.out.println("\'Number of Accesses to the Disk\' Ratio for Successful searches: "+sucDiv/keys);
			System.out.println("\'Number of Accesses to the Disk\' Ratio for Failed searches: "+failDiv/keys);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static int[] testArray(int keys, File file) throws IOException {
		int[] values = new int[2*keys];
		int[] temp = Utils.readIntArrayFromDisk(file.getName(), 0, keys);
		for(int i=0; i<keys; i++) {
			values[i] = temp[i];
		}
		for (int i=keys; i<2*keys; i++) {
			values[i]=(new Random()).nextInt(1000000)+1000000;
		}
		return values;
	}
}
