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

public class TrimmedRenameableCollection extends AbstractRenameableCollection{
	
	public enum TrimEnd {LEFT, RIGHT;}
	
	private TrimEnd trimEnd = TrimEnd.LEFT
	private int trimLength = 0
		
	public TrimmedRenameableCollection(String topDir, TrimEnd trimEnd, int trimLength, String regex){
		super(topDir, regex)
		this.trimEnd = trimEnd
		this.trimLength = trimLength
	}
	
	String applyChange(String name, File file){
		StringBuilder modifiedName = new StringBuilder()
		
		int begin, end = 0
		int extPos = name.lastIndexOf(".")
		
		// File extension IS the name, nothing to rename
		if(extPos == 0){return name}
		
		// File does not contain extension
		if(extPos == -1){
			extPos = name.length()
		}
		
		if(trimEnd == TrimEnd.LEFT){
			if(extPos > trimLength){
				begin = trimLength
			}
			else{
				begin = extPos - 1
			}
			end = name.length()
			modifiedName.append(name.substring(begin, end))
		}
		else if(trimEnd == TrimEnd.RIGHT){
			begin = 0
			
			if(extPos > trimLength){
				end = extPos - trimLength
			}
			else{
				end = begin + 1
			}
			modifiedName.append(name.substring(begin, end))
			modifiedName.append(name.substring(extPos, name.length()))
		}
		return modifiedName.toString()
	}		
 }