import groovy.swing.SwingBuilder

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

public class FileInfoObserver implements Observer{
	private FileInfoManager fileInfoManager = null
	private SwingBuilder swingBuilder = null
	
	public FileInfoObserver(FileInfoManager fileInfoManager, SwingBuilder swingBuilder){
		this.fileInfoManager = fileInfoManager
		this.swingBuilder = swingBuilder
	}
	
	public void update(Observable obs, Object obj){
		if(obs == fileInfoManager){
			swingBuilder.edt{
				swingBuilder.statusLabel.text = obs.getStatus()
				swingBuilder.processProgressBar.value = obs.getPercentComplete()
			}
		}
	}
}