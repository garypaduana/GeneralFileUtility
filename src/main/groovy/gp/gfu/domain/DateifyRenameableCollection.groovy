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

import java.text.DateFormat
import java.text.SimpleDateFormat

public class DateifyRenameableCollection extends AbstractRenameableCollection{

    private String dateFormat
    private boolean addSequence
    private Map<Long, Integer> msMap = new TreeMap<Long, Integer>()


    public DateifyRenameableCollection(String topDir, String regex, String dateFormat, boolean addSequence){
        super(topDir, regex)
        this.dateFormat = dateFormat
        this.addSequence = addSequence
    }

    String applyChange(String name, File file){
        long time = file.lastModified()
        String dif = ""

        if(addSequence){
            def m = file.getName() =~ ".+?(\\d+).+"
            if(m){
                dif = m[0][1]
            }
        }

        DateFormat df = new SimpleDateFormat(dateFormat);
        if(dif.length() > 0){
            dif = "." + dif
        }
        return df.format(new Date(time)) + dif + "." + name.substring(name.lastIndexOf(".") + 1, name.length())
    }
}
