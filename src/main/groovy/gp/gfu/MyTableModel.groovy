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

import javax.swing.table.AbstractTableModel

public class MyTableModel extends AbstractTableModel {
    private String[] columnNames = (String[])[]
    private Object[][] data = (Object[][])[][]
	private RenameableCollection renameableCollection = null
		
    public MyTableModel(){
        super()
    }
    
    public MyTableModel(Object[][] data, String[] columnNames, RenameableCollection renameableCollection){
        this.columnNames = columnNames
        this.data = data
		this.renameableCollection = renameableCollection
    }
	    
    public int getColumnCount() {
        return columnNames.length
    }

    public int getRowCount() {
        if(data == null)
            return 0
        else
            return data.length
    }

    public String getColumnName(int col) {
        return columnNames[col]
    }

    public Object getValueAt(int row, int col) {
		if(data.size() > 0)
            return data[row][col]
        else
            return null
    }

    public Class getColumnClass(int c){
		if(getValueAt(0,c) != null)
			return getValueAt(0, c).getClass()
        else
            return Object
    }
    
    public boolean isCellEditable(int row, int col) {
		// col 1 is the check mark or x mark ImageIcon, do not edit!
		if(col == 1){
			return false
		}
		return true
    }

    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value
        fireTableCellUpdated(row, col)
		
		if(renameableCollection != null){
			renameableCollection.setValueAt(value, row, col)
		}
    }
}