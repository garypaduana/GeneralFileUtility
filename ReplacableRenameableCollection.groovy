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

public class ReplacableRenameableCollection extends AbstractRenameableCollection{
	
	private String replace = ""
	private String with = ""
	
	public ReplacableRenameableCollection(String topDir, String regex, String replace, String with){
		super(topDir, regex)
		this.replace = replace
		this.with = with
	}
	
	String applyChange(String name, File file){
		return name.replace(replace, with)
	}
 }