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

package gp.gfu.domain

import javax.swing.ImageIcon

public class Data{
    
    private Object[][] fileInfoData
	private java.util.List<java.util.List<Object>> fileInfoDataList = new ArrayList<java.util.List<Object>>()
	private java.util.List<java.util.List<Object>> mergedFileInfoDataList = new ArrayList<java.util.List<Object>>()
	private java.util.List<java.util.List<Object>> notMergedFileInfoDataList = new ArrayList<java.util.List<Object>>()
	private Map<String, FileInfo> pathToFileInfoMap = new HashMap<String, FileInfo>()
	private Map<String, List<FileInfo>> uniqueFilesMap = new HashMap<String, List<FileInfo>>()
	private Set<FileInfo> uniqueFilesSet = new HashSet<FileInfo>()
	private String[] columnNames = (String[])(["Name", "Size", "MD5"])
	private RenameableCollection renameableCollection
	private boolean scanCanceled = false
	private boolean mergeCanceled = false
	public static final ImageIcon X_MARK = new ImageIcon(Data.class.getResource("/resources/images/xmark.png"))
	public static final ImageIcon CHECK_MARK = new ImageIcon(Data.class.getResource("/resources/images/checkmark.png"))
	
    private static Data instance = null
    
    private Data(){
        
    }
    
    public static synchronized getInstance(){
        if(instance == null){
            instance = new Data()
        }
        return instance
    }
	
	public void setScanCanceled(boolean scanCanceled){
		this.scanCanceled = scanCanceled
	}
	
	public boolean isScanCanceled(){
		return scanCanceled
	}
	
	public void setMergeCanceled(boolean mergeCanceled){
		this.mergeCanceled = mergeCanceled
	}
	
	public boolean isMergeCanceled(){
		return this.mergeCanceled
	}
	
	public void setRenameableCollection(RenameableCollection renameableCollection){
		this.renameableCollection = renameableCollection
	}
	
	public RenameableCollection getRenameableCollection(){
		return renameableCollection
	}
	
	public java.util.List<java.util.List<Object>> getFileInfoDataList(){
		return fileInfoDataList
	}
	
	public java.util.List<java.util.List<Object>> getMergedFileInfoDataList(){
		return mergedFileInfoDataList
	}
	
	public java.util.List<java.util.List<Object>> getNotMergedFileInfoDataList(){
		return notMergedFileInfoDataList
	}
	
	public Map<String, FileInfo> getPathToFileInfoMap(){
		return this.pathToFileInfoMap
	}
	
	public Object[][] getFileInfoData(java.util.List<java.util.List<Object>> fileInfoDataList){
		fileInfoData = new Object[fileInfoDataList.size()][3]
		for(int i = 0; i < fileInfoDataList.size(); i++){
			fileInfoData[i][0] = fileInfoDataList.get(i).get(0)
			fileInfoData[i][1] = fileInfoDataList.get(i).get(1)
			fileInfoData[i][2] = fileInfoDataList.get(i).get(2)
		}
		
		return fileInfoData
	}
	
	public static Object[][] convertListToArray(java.util.List<java.util.List<Object>> myList){
		Object[][] data = new Object[myList.size()][myList.get(0).size()]
		for(int i = 0; i < myList.size(); i++){
			for(int j = 0; j < myList.get(i).size(); j++){
				data[i][j] = myList.get(i).get(j)
			}
		}
		
		return data
	}
		
	public void setfileInfoData(Object[][] fileInfoData){
		this.fileInfoData = fileInfoData
	} 

	public Map<String, List<FileInfo>> getUniqueFilesMap(){
		return uniqueFilesMap
	}
	
	public Set<FileInfo> getUniqueFilesSet(){
		return this.uniqueFilesSet
	}
	
	public String[] getColumnNames(){
		return columnNames
	}
	
	public static Object[][] getEmptyData(){
		Object[][] data = new Object[1][3]
        data[0][0] = ""
        data[0][1] = ""
        data[0][2] = ""
		return data
	}
}