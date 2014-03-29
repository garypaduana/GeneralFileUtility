/*
 General File Utility
 Copyright (C) 2012-2014, Gary Paduana, gary.paduana@gmail.com
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package gp.gfu.util

import java.awt.Font;
import java.awt.FontMetrics
import java.io.File;
import java.nio.charset.Charset;
import java.security.MessageDigest
import java.security.Provider
import java.security.Security
import java.text.DecimalFormat

import javax.swing.ImageIcon
import javax.swing.JTable

class Calculations {

	private Calculations(){
		
	}
	
	public static JTable resizeJTable(JTable jTable, Font font){
		for(int i = 0; i < jTable.getColumnModel().getColumnCount(); i++){
			jTable.getColumnModel().getColumn(i).setPreferredWidth(getMaxContentWidth(jTable, i, font))
		}
		return jTable
	}

	public static int getMaxContentWidth(JTable jTable, int column, Font font){
		FontMetrics metrics = jTable.getFontMetrics(font)
		int maxWidth = metrics.stringWidth(jTable.getModel().getColumnName(column))
		
		for(int i = 0; i < jTable.getRowCount(); i++){
			Object cell = jTable.getValueAt(i, column)
			
			if(cell != null){
				if(jTable.getColumnClass(column).equals(ImageIcon.class)){
					maxWidth = metrics.stringWidth(jTable.getColumnModel().getColumn(column).getHeaderValue())
				}
				else if(metrics.stringWidth(cell.toString()) > maxWidth){
					maxWidth = metrics.stringWidth(cell.toString());
				}
			}
		}
		
		return Math.round(maxWidth * 0.95f) + 20;
	}
	
	public static String customFormat(String pattern, int value){
		DecimalFormat decimalFormat = new DecimalFormat(pattern);
		return decimalFormat.format(value);
	}
	
	public static String customFormat(String pattern, double value){
		DecimalFormat decimalFormat = new DecimalFormat(pattern);
		return decimalFormat.format(value);
	}
	
	public static byte[] decodeMessyHexToByteArray(String text){
		text = text.replaceAll("[^0-9ABCDEFabcdef]", '')
		
		int bitCount = text.length() * 4
		int byteCount = bitCount % 8 == 0 ? (bitCount / 8) : (bitCount / 8 + 1)
		
		byte[] bytes = new byte[byteCount]
		
		int j = 0
		for(int i = 0; i < text.length(); i += 2){
			int end = (i + 2) <= text.length() ? (i + 2) : text.length()
			bytes[j] = 0xFF & Integer.valueOf(text.substring(i, end), 16)
			j++
		}
		
		return bytes
	}
	
	public static String calculateParity(String hex, int parityBitLength){
		
		long parity = 0
		hex = hex.replaceAll("0x", "")
		hex = hex.replaceAll(" ", "")
		hex = hex.replaceAll("\r\n", "")
		hex = hex.replaceAll("\n", "")
		
		if((hex.length() * 4) % parityBitLength != 0) {
			throw new IllegalArgumentException("Invalid word length found!")
		}
		
		java.util.List<Long> pieces = new ArrayList<Long>()
		
		StringBuilder sb = new StringBuilder(hex)
		
		while(sb.length() >= (parityBitLength / 4)){
			pieces.add(Long.decode("0x" + sb.substring(0, (int) (parityBitLength / 4))))
			sb.delete(0, (int)(parityBitLength / 4))
		}
		
		for(long piece : pieces){
			parity = parity ^ piece
		}
		
		return Long.toHexString(parity).toLowerCase()
	}
	
	public static java.util.List<String> getFileList(String filePath){
		java.util.List<String> fileList = new ArrayList<String>()
		File file = new File(filePath)
	
		file.eachFileRecurse(){singleFile ->
			if(singleFile.isFile()){
				fileList.add(singleFile.absolutePath)
			}
		}
		return fileList
	}
	
	public static String safeSubstring(String str, int start, int end){
		if(start >= str.length()){
			return ""
		}
		
		if(end < start){
			return ""
		}
		
		if(start < str.length() && end > str.length()){
			return str.substring(start, str.length())
		}
		
		if(start < str.length() && end <= str.length()){
			return str.substring(start, end)
		}
	}
	
	public static List<List<String>> calculateAllMessageDigests(String text){
	    List<List<String>> results = new ArrayList<List<String>>()
	    
		// [provider, string length]
	    [['MD5', 32], ['SHA1', 40], ['SHA-256', 64], 
		 ['SHA-384', 96], ['SHA-512', 128]].each(){
	        try{
	            List<String> entry = new ArrayList<String>()
	            entry.add(it[0])            
	            MessageDigest digest = MessageDigest.getInstance(it[0])
	            digest.reset()
	            digest.update(decodeMessyHexToByteArray(text))
	        
	            byte[] sum = digest.digest()
	            BigInteger bigInt = new BigInteger(1, sum)
	            String result = bigInt.toString(16).padLeft(it[1], '0')
	            entry.add(result)
	            results.add(entry)
	        }
	        catch(Exception ex){        
	           println ex.getMessage()
	        }
		 }
		 
		 [4, 8, 16, 32].each {
			 
			 List<String> entry = new ArrayList<String>()
			 entry.add("${it}-bit parity")
			 try{
				 entry.add(Calculations.calculateParity(text, it))
			 }
			 catch (Exception e){
				 entry.add(e.getMessage())
			 }
			 results.add(entry)
			 
		 }
		
		 // [radix, description, leftPadding, container bit size]
		 [[2, "Binary", 8, 32], 
		  [8, "Octal", 3, 32],
		  [10, "Decimal (> 8-bit)", 0, 32],
		  [10, "Decimal (8-bit)", 0, 8], 
		  [16, "Hexadecimal", 2, 32]].each {
			 List<String> entry = new ArrayList<String>()
			 entry.add(it[1])
			 String decoded = decodeToRadix(text, it[0], it[2], it[3])
			 entry.add(shorten(decoded, 64))
			 results.add(entry)
		 }
		  
		  ["UTF-8", "UTF-16", "ISO-8859-1", "UTF-16LE",
			  "UTF-16BE", "US-ASCII"].each(){
			  List<String> entry = new ArrayList<String>()
			  entry.add(it) 
			  byte[] bytes = decodeMessyHexToByteArray(text)
			  entry.add(shorten(new String(bytes, Charset.forName(it)), 64))
			  results.add(entry)
		  }
		
	    return results
	}
	
	public static String shorten(String text, int length){
		if(text.length() > length){
			text = text.substring(0, (int)(length / 2)) + " . . . " +
		    	   text.substring(text.length() - (int)(length / 2), text.length())
		}
		return text
	}
	
	public static String decodeToRadix(String text, int radix, int pad, int containerSize){
		StringBuilder sb = new StringBuilder()

		for(int i = 0; i < text.length(); i += 2){
			int end = (text.length() > i + 2) ? (i + 2) : text.length()
			String val
			int decoded = Integer.decode("0x" + text.substring(i, end)) & 0xFF
			
			if(containerSize == 8){
				decoded = (byte) (decoded & 0xFF)
			}
			else{
				sb.append(Integer.toString(decoded, (radix)).padLeft(pad, '0'))
				sb.append(" ")
			}
		}
		return sb.toString()
	}
	
	/**
	 * Generates an MD5 signature using all bytes in the file.
	 * @param file
	 * @return
	 */
	public static String generateMD5(File file) {
		MessageDigest digest = MessageDigest.getInstance("MD5")
		digest.reset()
		if(file.canRead() ){
			file.withInputStream(){is->
				byte[] buffer = new byte[8192]
				int read = 0
				while((read = is.read(buffer)) > 0) {
					 digest.update(buffer, 0, read)
				}
			}
		}
	
		byte[] md5sum = digest.digest()
		BigInteger bigInt = new BigInteger(1, md5sum)
		return bigInt.toString(16).padLeft(32, '0')
	}
	
	/**
	 * Generates an MD5 signature using up to 1MB (2^20) bytes from the
	 * beginning of the file.  Useful to quickly determine if two large files
	 * are different without having to read every byte in each.  If there is
	 * equality after this method is evaluated for each file then a full
	 * processing should occur for each.
	 * @param file
	 * @return
	 */
	public static String generateShortMD5(File file) {
		MessageDigest digest = MessageDigest.getInstance("MD5")
		digest.reset()
		file.withInputStream(){is->
			byte[] buffer = new byte[8192]
			int read = 0
			int totalRead = 0
			while((read = is.read(buffer)) > 0 && totalRead <= 1048576) {
				totalRead += read
				digest.update(buffer, 0, read)
			}
		}
	
		byte[] md5sum = digest.digest()
		BigInteger bigInt = new BigInteger(1, md5sum)
		return bigInt.toString(16).padLeft(32, '0')
	}
}
