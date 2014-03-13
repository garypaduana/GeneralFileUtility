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

import java.text.DateFormat
import java.text.SimpleDateFormat

public class DateifyRenameableCollection extends AbstractRenameableCollection{

	private String dateFormat
	private Map<Long, Integer> msMap = new TreeMap<Long, Integer>()
	
	public DateifyRenameableCollection(String topDir, String regex, String dateFormat){
		super(topDir, regex)
		this.dateFormat = dateFormat
	}
	
	String applyChange(String name, File file){
		long time = file.lastModified()
		if(msMap.containsKey(time)){
			msMap.put(time, msMap.get(time) + 50)
		}
		else{
			msMap.put(time, 0)
		}
		
		time = time + msMap.get(time)
		
		DateFormat df = new SimpleDateFormat(dateFormat);
		return df.format(new Date(time)) + "." + name.substring(name.lastIndexOf(".") + 1, name.length())
	}
}