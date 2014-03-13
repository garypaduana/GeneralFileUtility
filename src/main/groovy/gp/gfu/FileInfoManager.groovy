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

package gp.gfu

public class FileInfoManager extends Observable{

	private java.util.List<String> fileList
	private java.util.List<FileInfo> fileInfoList = new ArrayList<FileInfo>()
	private java.util.List<java.util.List<Object>> interimData = new ArrayList<java.util.List<Object>>()
	private Object[][] data
	private int percentComplete = 0
	private String status = "Initializing..."
	private long totalSize = 0
	private long filesProcessedCount = 0
	
	public FileInfoManager(List<String> fileList){
		this.fileList = fileList
		data = Data.getInstance().getEmptyData()
	}
	
	public void processFiles(){
		notifyObservers()
		for(int i = 0; i < fileList.size(); i++){
			if(Data.getInstance().isScanCanceled()){
				break
			}
			java.util.List<String> entry = new ArrayList<String>()
			
			FileInfo fileInfo = buildFileInfo(entry, i)
			addFileInfo(fileInfo, entry)
			interimData.add(entry)
		}		
	}
	
	protected FileInfo buildFileInfo(java.util.List<String> entry, int i){
		entry.add(fileList.get(i))
		Double size = new File(fileList.get(i)).size() / 1D
		entry.add(size)
		totalSize += size
		percentComplete = (int)(((i + 1) / fileList.size()) * 100)
		status = "Processing: ${i+1} / ${fileList.size()} Total Progress: ${percentComplete}% Current File: ${new File(fileList.get(i)).name} (${ComponentModifier.customFormat('###,###,###,###,###', size)} bytes)"
		notifyObservers()
		FileInfo fileInfo = new FileInfo(fileList.get(i), new File(fileList.get(i)).getName(), size)
		++filesProcessedCount
		return fileInfo
	}
	
	protected boolean addFileInfo(FileInfo fileInfo, java.util.List<String> entry){
		Data.getInstance().getPathToFileInfoMap().put(fileInfo.getPath(), fileInfo)
		
		if(Data.getInstance().getUniqueFilesSet().contains(fileInfo)){
			entry.add(fileInfo.getHash())
			if(Data.getInstance().getUniqueFilesMap().containsKey(fileInfo.getHash())){
				Data.getInstance().getUniqueFilesMap().get(fileInfo.getHash()).add(fileInfo)
			}
			else{
				Data.getInstance().getUniqueFilesMap().put(fileInfo.getHash(), new ArrayList<FileInfo>())
				Data.getInstance().getUniqueFilesMap().get(fileInfo.getHash()).add(fileInfo)
				
				for(Iterator<FileInfo> it = Data.getInstance().getUniqueFilesSet().iterator(); it.hasNext();){
					FileInfo fi = it.next()
					if(fi.equals(fileInfo)){
						Data.getInstance().getUniqueFilesMap().get(fileInfo.getHash()).add(fi)
					}
				}
			}
			return true
		}
		else{
			Data.getInstance().getUniqueFilesSet().add(fileInfo)
			entry.add("Not necessary")
			return false
		}
	}
	
	public java.util.List<java.util.List<Object>> getInterimData(){
		return interimData
	}
	
	public Object[][] getData(){
		data = new Object[interimData.size()][3]
		
		for(int i = 0; i < interimData.size(); i++){
			data[i][0] = interimData.get(i).get(0)
			data[i][1] = interimData.get(i).get(1)
			data[i][2] = interimData.get(i).get(2)
		}
		return data
	}
	
	public Object[][] getData(java.util.List<java.util.List<Object>> interimData){
		data = new Object[interimData.size()][3]
		
		for(int i = 0; i < interimData.size(); i++){
			data[i][0] = interimData.get(i).get(0)
			data[i][1] = interimData.get(i).get(1)
			data[i][2] = interimData.get(i).get(2)
		}
		return data
	}
	
	public long getTotalSize(){
		return totalSize
	}
	
	@Override
	public void notifyObservers() {
		setChanged();
		super.notifyObservers();
	}

	public String getStatus(){
		return status
	}
	
	public int getPercentComplete(){
		return percentComplete
	}
	
	public int getFilesProcessedCount(){
		return filesProcessedCount
	}
	
	public java.util.List<String> getFileList(){
		return fileList
	}
}