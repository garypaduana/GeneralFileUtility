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

public class FileInfo{
	
	private String path = null
	private String name = null
	private String hash = null
	private String firstMbHash = null
	private double size
	
	public FileInfo(String path, String name, double size){
		this.path = path
		this.name = name
		this.size = size
		this.hash = hash
	}
	
	public String getPath(){
		return path
	}
	
	public void setPath(String path){
		this.path = path
	}
	
	public String getName(){
		return name
	}
	
	public void setName(String name){
		this.name = name
	}
	
	public String getHash(){
		return hash
	}
	
	public void setHash(String hash){
		this.hash = hash
	}
	
	public String getFirstMbHash(){
		return firstMbHash
	}
	
	public void setFirstMbHash(String firstMbHash){
		this.firstMbHash = firstMbHash
	}
	
	public double getSize(){
		return this.size
	}
	
	public void setSize(double size){
		this.size = size
	}
	
	@Override
	public boolean equals(Object o){
		if(!o instanceof FileInfo){return false}
		FileInfo oArg = ((FileInfo)o)
		
		if(oArg.getSize() == this.size){
			if(oArg.getFirstMbHash() == null){
				oArg.setFirstMbHash(DirectoryTools.generateShortMD5(new File(oArg.getPath())))
			}
			if(this.firstMbHash == null){
				setFirstMbHash(DirectoryTools.generateShortMD5(new File(this.path)))
			}

			if(!this.firstMbHash.equals(oArg.getFirstMbHash())){
				return false
			}				
			else{
				if(oArg.getHash() == null){
					oArg.setHash(DirectoryTools.generateMD5(new File(oArg.getPath())))
				}	
				if(this.hash == null){
					this.hash = DirectoryTools.generateMD5(new File(this.path))
				}
				if(oArg.getHash().equals(this.hash)){
					return true
				}
			}
		}
		return false		
	}
	
	@Override
	public int hashCode(){
		int hash = this.size * 31
		return hash
	}
	
	@Override
	public String toString(){
		return "Path: $path  Size: $size  Hash: $hash"
	}
}