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

import java.awt.Font
import java.awt.FontMetrics
import java.text.DecimalFormat
import javax.swing.JTable

public class ComponentModifier{

    private ComponentModifier(){
    }
    
    public static JTable resizeJTable(JTable jTable, Font font){
        for(int i = 0; i < jTable.getColumnModel().getColumnCount(); i++){
            jTable.getColumnModel().getColumn(i).setPreferredWidth(getMaxContentWidth(jTable, i, font))
        }
        return jTable
    }

    private static int getMaxContentWidth(JTable jTable, int column, Font font){
        FontMetrics metrics = jTable.getFontMetrics(font)        
        int maxWidth = metrics.stringWidth(jTable.getModel().getColumnName(column))
        
        for(int i = 0; i < jTable.getRowCount(); i++){
            Object cell = jTable.getValueAt(i, column)
            if(cell != null && metrics.stringWidth(cell.toString()) > maxWidth){
                maxWidth = metrics.stringWidth(cell.toString());
            }
        }
        return Math.round((maxWidth * 0.95f)) + 50;
    }
    
    public static String customFormat(String pattern, int value){
        DecimalFormat decimalFormat = new DecimalFormat(pattern);
        return decimalFormat.format(value); 
    }
    
    public static String customFormat(String pattern, double value){
        DecimalFormat decimalFormat = new DecimalFormat(pattern);
        return decimalFormat.format(value); 
    }
}