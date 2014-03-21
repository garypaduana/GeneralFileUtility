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

package gp.gfu.controller

import gp.gfu.domain.Data;
import gp.gfu.domain.FileInfo;

import javax.swing.ImageIcon

public class MergeFileInfoManager extends FileInfoManager{
	
	private java.util.List<java.util.List<Object>> interimMergedData = new ArrayList<java.util.List<Object>>()
	private java.util.List<java.util.List<Object>> interimNotMergedData = new ArrayList<java.util.List<Object>>()
	private String destinationPath
	private String sourcePath
	private boolean copyOnly
	
	public MergeFileInfoManager(java.util.List<String> fileList, String destinationPath, String sourcePath, boolean copyOnly){
		super(fileList)
		this.destinationPath = destinationPath
		this.sourcePath = sourcePath
		this.copyOnly = copyOnly
	}
	
	public java.util.List<java.util.List<Object>> getInterimMergedData(){
		return interimMergedData
	}
	
	public java.util.List<java.util.List<Object>> getInterimNotMergedData(){
		return interimNotMergedData
	}
	
	@Override
	public void processFiles(){
		notifyObservers()
		for(int i = 0; i < getFileList().size(); i++){
			println getFileList().get(i)
			if(Data.getInstance().isMergeCanceled()){
				break
			}
			
			java.util.List<String> entry = new ArrayList<String>()
			
			FileInfo fileInfo = buildFileInfo(entry, i)
			// true means the file already exists
			if(addFileInfo(fileInfo, entry)){
				//println "existed in reference"
				interimNotMergedData.add(entry)
			}
			else{
				//println "did not exist"
				// attempt to copy the file, it did not exist already
				File orig = new File(getFileList().get(i))
				File dest = new File(destinationPath + "/" + orig.getParent().replace(sourcePath, "") + "/"	+ orig.getName())
				new File(dest.getParent()).mkdirs()
				
				// FIXME: Need to update the "path" field in the data structures to point to the new value.  DOH!
				// Perhaps do this in the observer. send back a key, value pair with old/new path values.
				
				try{
					if(copyOnly){
						// Make a copy of the file, do not rename or move it
						dest.append(orig.newInputStream())
						interimMergedData.add(entry)
					}
					else{
						boolean moved = false;
						for(int attempt = 0; attempt < 20; attempt++){
							if(orig.renameTo(dest)){
								FileInfo temp = Data.getInstance().getPathToFileInfoMap().get(orig.getAbsolutePath())
								temp.setPath(dest.getAbsolutePath())
								Data.getInstance().getPathToFileInfoMap().put(dest.getAbsolutePath(), temp)
								
								interimMergedData.add(entry)
								moved = true;
								break;
							}
							println "attempting to garbage collect ${attempt}"
							System.gc();
							Thread.sleep(50);
						}
						if(!moved){
							interimNotMergedData.add(entry)
						}
					}
				}
				catch(Exception ex){
					JOptionPane.showMessageDialog(orig.getName() + ",  " + ex.getMessage())
				}
			}
			interimData.add(entry)
		}
	
	
	}
}