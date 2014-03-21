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
import java.security.MessageDigest
import java.text.DecimalFormat

import javax.swing.ImageIcon;
import javax.swing.JTable;

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
		
		return "0x" + Long.toHexString(parity).toUpperCase()
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
