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

import java.security.MessageDigest

public class DirectoryTools{

    private DirectoryTools(){
    
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