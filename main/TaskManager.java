package main;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Random;

import utils.ToCsv;
import utils.Utils;

public class TaskManager {
	
	//member variables, constants and useful arrays
	public static int numOfDiskAccess = 0;
	private final int totalInts = 100000;				//
	private final int bufferSize = 1000;				//
	private final int fileSize = 10000;					//
	private final int numOfFiles = totalInts/fileSize;
	private final int numOfPages = totalInts/bufferSize;
	int[] buffer = new int[bufferSize];
	int[] index = new int[numOfFiles];
	
	//This method uses a buffer to initialize a file with "totalInts" random numbers 
	public void randomNumGenerator(String filename) throws IOException {
		long offset=0;
		for (int i=0; i<(totalInts/bufferSize); i++) {
			for (int j=0; j<bufferSize; j++) {
				buffer[j] = (new Random()).nextInt(1000000)+1;
			}
			Utils.writeIntArrayToDisk(filename, offset, buffer);
			numOfDiskAccess++;
			offset+=bufferSize;
		}
	}
	
	/*
	 * This method splits a file into (totalInts/arraySize) files, sorts the first
	 * file's elements and finally stores them at the new files
	 */
	public void fileSort(String filename) throws IOException {
		long offset=0;
		int count=1;
		while(offset<totalInts) {
			int[] array = arrayInitialization(filename, (int) offset);
			offset+=fileSize;
			heapSort(array);
			//System.out.println("Array is sorted");
			storeArray(array, count);
			count++;
		}
	}
	
	/*
	 * This method uses a kind of merge-sort algorithm that compares the elements of
	 * multiple files and finally stores them from the smallest value to the
	 * greatest into a new file using 11 buffers of bufferSize integers.
	 */
	public void mergeFiles() throws IOException {
		
		int offset=0;
		File file= new File("sortedText.bin");
		int[][] array = new int[numOfFiles+1][bufferSize];
		Arrays.fill(index, 0);
		int[] offsets= new int[numOfFiles];
		Arrays.fill(offsets, 0);
		
		for (int i = 0; i<(numOfFiles); i++) {
			array[i]=Utils.readIntArrayFromDisk("sortedFile"+(i+1)+".bin", offsets[i], bufferSize);
			offsets[i]+=bufferSize;
			numOfDiskAccess++;
		}
		
		for(int k=0; k<totalInts/bufferSize; k++) {
			for (int i=0; i<bufferSize; i++) { 
				array[array.length-1][i]=findMin(array); 
				moveIndex(array[array.length-1][i], array);
				checkIndex(index, array, offsets);
			}
			Utils.writeIntArrayToDisk(file.getName(), offset, array[array.length-1]);
			numOfDiskAccess++;
			offset+=bufferSize;
		}
		ToCsv.toCsv(file.getName());
		 
	}
	
	/*
	 * This method is used to search for a key linearly in a file with the help of a
	 * bufferSize buffer
	 */ 
	public boolean linearSearch(String filename, int key) throws IOException {
		for (int i=0; i<totalInts; i+=bufferSize) {
			buffer = Utils.readIntArrayFromDisk(filename, i, bufferSize);
			numOfDiskAccess++;
			for (int j=0; j<bufferSize; j++) {
				if (buffer[j]==key)
					return true;
			}
		}
		return false;
	}
	
	/*
	 * This method is used to search for a key in a file according to the following:
	 * 		First it checks in the middle page of the file if the key exists there. 
	 * 		If not, it checks if the key is smaller than the first int of the page. If it is
	 * so, it recursively calls itself with limits (min, mid-1). 
	 * 		If the key is greater than the last int of the page, it recursively calls itself 
	 * with limits (mid+1, max). 
	 * 		If key is not found, it returns false. Otherwise, true.
	 */
	public boolean binarySearch(String filename, int key, int min, int max) throws IOException {
		if(min<=max) {
			int mid = min + (max-min) / 2;
			RandomAccessFile rFile = new RandomAccessFile(filename, "r");
			
			if (linearSearchThroughPage(filename, key, mid*bufferSize)) {
				return true;
			}
			rFile.seek(mid*bufferSize*4);
			if (rFile.readInt()>key) {
				return binarySearch(filename, key, min, mid-1);
			}
			return binarySearch(filename, key, mid+1, max);
		}
		return false;
	}
	
	/* This method is used to search linearly through a page of a file for a key */ 
	public boolean linearSearchThroughPage(String filename, int key, int offset) throws IOException {
		buffer=Utils.readIntArrayFromDisk(filename, offset, bufferSize);
		numOfDiskAccess++;
		for (int j = 0; j<bufferSize; j++) {
			if (buffer[j]==key) 
				return true;
		}
		return false;
	}
	
	/*
	 * This method is used to initialize the array that later will be sorted and
	 * finally stored in a file consisting a page
	 */
	public int[] arrayInitialization(String filename, int offset) throws IOException {
		int[] temp= new int[fileSize];
		int pointer=0;
		for (int i=0; i<(fileSize/bufferSize); i++) {
			buffer = Utils.readIntArrayFromDisk(filename, offset+pointer, bufferSize);
			numOfDiskAccess++;
			for (int j=0; j<bufferSize; j++) {
				temp[(int) (j+pointer)]=buffer[j];
			}
			pointer+=bufferSize;
		}
		return temp;
	}
	
	/* This method is used for storing the previously initialized pageSize array */ 
	public void storeArray(int[] array, int counter) throws IOException {
		String fName = "sortedFile";
		int pointer=0;
		File file = new File(fName+counter+".bin");
		for (int i=0; i<(fileSize/bufferSize); i++) {
			for (int j=0; j<bufferSize; j++) {
				buffer[j] = array[pointer+j];
			}
			Utils.writeIntArrayToDisk(file.getName(), pointer, buffer);
			numOfDiskAccess++;
			pointer+=bufferSize;
		}
		ToCsv.toCsv(file.getName());
	}
	
	/*
	 * This method sorts an array from the least value to the greatest using the
	 * heap sort algorithm
	 */
	public void heapSort(int arr[]) 
    { 
        int n = arr.length; 
  
        // Build heap (rearrange array) 
        for (int i = n / 2 - 1; i >= 0; i--) 
            heapify(arr, n, i); 
  
        // One by one extract an element from heap 
        for (int i=n-1; i>=0; i--) 
        { 
            // Move current root to end 
            int temp = arr[0]; 
            arr[0] = arr[i]; 
            arr[i] = temp; 
  
            // call max heapify on the reduced heap 
            heapify(arr, i, 0); 
        } 
    } 
   
    void heapify(int arr[], int n, int i) {			// To heapify a subtree rooted with node i which is an index in arr[]. n is size of heap
        int largest = i; // Initialize largest as root 
        int l = 2*i + 1; // left = 2*i + 1 
        int r = 2*i + 2; // right = 2*i + 2 
  
        // If left child is larger than root 
        if (l < n && arr[l] > arr[largest]) 
            largest = l; 
  
        // If right child is larger than largest so far 
        if (r < n && arr[r] > arr[largest]) 
            largest = r; 
  
        // If largest is not root 
        if (largest != i) 
        { 
            int swap = arr[i]; 
            arr[i] = arr[largest]; 
            arr[largest] = swap; 
  
            // Recursively heapify the affected sub-tree 
            heapify(arr, n, largest); 
        } 
    } 
    
	/*
	 * This method is used in order to find the least value among the page buffers
	 */
    public int findMin(int[][] array) {
    	int min = Integer.MAX_VALUE;
    	
    	for (int i=0; i<array.length-1; i++) {
    		if(min>array[i][index[i]])	{
    			min=array[i][index[i]];
    		}
    	}
    	return min;
    }
    
	/*
	 * This method augments the index of the page array that will be checked later
	 * if the previous min value was draught by that buffer
	 */
    public void moveIndex(int min, int[][] array) {
    	
    	for (int i=0; i<array.length-1; i++) {
    		if (min==array[i][index[i]]) {
    			index[i]++;
    			return;
    		}
    	}
    }
    
	/*
	 * This method checks if the respective index has reached its maximum
	 * (bufferSize). If so, it loads to the buffer the next bufferSize ints and sets
	 * the index at 0. If the offset that points to the next read-position of the
	 * file has reached the End Of File (EOF), it deletes the file, and sets the
	 * respective buffer's cells with the MAX_VALUE
	 */
    public void checkIndex(int[] index, int[][] array, int offsets[]) throws IOException {
    	for (int i=0; i<numOfFiles; i++) {
    		if (index[i]==bufferSize && offsets[i]<=fileSize) {
    			array[i]=Utils.readIntArrayFromDisk("sortedFile"+(i+1)+".bin", offsets[i], bufferSize);
    			if (offsets[i]!=fileSize)
    				numOfDiskAccess++;
    			offsets[i]+=bufferSize;
    			index[i]=0;
    		}
    		if (offsets[i]>fileSize) {
    			Arrays.fill(array[i], Integer.MAX_VALUE);
				File file = new File("sortedFile"+(i+1)+".bin"); 
				if(file.delete())
					//System.out.println("File "+file.getName()+" is deleted");
				offsets[i]=fileSize;
    		}
    	}
    }

	public int getNumOfPages() {
		return numOfPages;
	}
}
