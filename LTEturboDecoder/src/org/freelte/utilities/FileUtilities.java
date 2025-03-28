/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freelte.utilities;
import java.util.*;
import java.lang.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * File IO and a few CSV tools
 * @author chris.deng
 */
public class FileUtilities {
	final static Charset ENCODING = StandardCharsets.UTF_8;

	public String array_toString(String[] sa) {
		StringBuilder sb = new StringBuilder();
		for (String s : sa) {
			sb.append(s).append("\n");
		}
		return sb.toString();
	}
	/**
	 * write entire text to the output file name
	 * @param fn
	 * @param otxt
	 */
	public void writeTextToFile(String fn, String otxt) {
		try {
			FileWriter fw = new FileWriter(fn);
			fw.write(otxt);
			fw.close();
		}
		catch (IOException ex) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
		}
	}
	/**
	 * Read entire text file and return each line as String
	 * ordered as an List<String>
	 * @param ifn
	 * @return
	 */
	public List<String> readEntireTextFile(String ifn) {
		Path p = Paths.get(ifn);
		try {
			return Files.readAllLines(p, ENCODING);
		}
		catch (IOException ex) {
			System.out.println("FileUtilities.readEntireTextFile io exception");
			System.out.println(ifn);
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}
        /**
         * get a particular field in csv format
         * @param line string without CR
         * @param n field index, starting at 1
         * @return String of that field
         * @throws Exception
         */
	public String csv_field_get(String line, int n) throws Exception {
		//n>0, first field is labeled as 1
		for (int i=0; i<n-1; i++) {
			int j = line.indexOf(",");
			if (j < 0) {
				throw new Exception("FileUtilities csv field get index fail.");
			}
			line = line.substring(j+1); //skip the found ,
		}
		int k = line.indexOf(",");
		if (k < 0) {
			k = line.length();
		}
		String s = line.substring(0,k);
		if (k == 0)
		{
			s = "0";
		}
		return s;
	}
        /**
         * cut line of string to field index
         * @param line string without CR
         * @param n field index to cut to
         * @return
         */
	private String[] csv_line_cuto(String line, int n) {
		String line1 = "";
		String[] linea = new String[2];
		for (int i=0; i< n-1; i++) {
			int j = line.indexOf(",");
			line1 += line.substring(0, j+1);
			line = line.substring(j+1);
		}
		linea[0] = line1;
		linea[1] = line;
		return linea;
	}
        /**
         * replace a field at index n with new string
         * @param line string of csv format, no CR
         * @param field new string
         * @param n field index to replace
         * @return replace line of string in csv format
         */
	public String csv_field_replace(String line, String field, int n) {
		 //n > 0, first field is labeled 1
		String[] line2s = this.csv_line_cuto(line, n);
		String line2 = line2s[1];
		int k = line2.indexOf(",");
		if (k < 0)
		{
			//may be last field or empty before
			return line2s[0] + field;
		}
		else
		{
			return line2s[0] + field + line2.substring(k);
		}
	}
}
