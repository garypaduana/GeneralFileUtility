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

import gp.gfu.util.Calculations

public abstract class AbstractRenameableCollection extends Observable implements RenameableCollection{
    private Object[][] data
    private java.util.List<java.util.List<Object>> interimData = Collections.synchronizedList(new ArrayList<java.util.List<Object>>())
    private String[] columnNames = ["Original", "Rename?", "Expected Output"]
    private java.util.List<String> files = Collections.synchronizedList(new ArrayList<String>())
    private String regex = ""
    private int percentComplete = 0
    private String status = "Ready."
    private String topDir = System.getProperty("user.dir")
    private boolean previewCompleted = false
    
    public AbstractRenameableCollection(String topDir, String regex){
        this.topDir = topDir
        this.regex = regex
    }
    
    public void setValueAt(Object value, int row, int col){
        interimData.get(row).set(col, value)
    }
    
    abstract String applyChange(String name, File file);
    
    public Object[][] getData(){
        data = new Object[interimData.size()][3]
        
        for(int i = 0; i < interimData.size(); i++){
            data[i][0] = interimData.get(i).get(0)
            data[i][1] = interimData.get(i).get(1)
            data[i][2] = interimData.get(i).get(2)
        }
        return data
    }
    
    public void setData(Object[][] data){
        this.data = data
    }
    
    public String[] getColumnNames(){
        return columnNames
    }
    
    @Override
    public void notifyObservers() {
        setChanged()
        super.notifyObservers()
    }

    public int getPercentComplete(){
        return percentComplete
    }
    
    public String getStatus(){
        return status
    }
    
    @Override
    public String toString(){
        return "Top Dir: $topDir, Regex: $regex, Status: $status"
    }
    
    public void setStatus(String status){
        this.status = status
    }
    
    public String getTopDir(){
        return topDir
    }
    
    public java.util.List<String> getFiles(){
        return files
    }
    
    public String getRegex(){
        return regex
    }
    
    public java.util.List<java.util.List<Object>> getInterimData(){
        return interimData
    }
    
    protected filterFiles(){
        for(Iterator it = getFiles().iterator(); it.hasNext();){
            File f = new File(it.next())
            if(!(f.name ==~ regex)){
                it.remove()
            }
        }   
    }
    
    public void generatePreview(){
        setStatus("Generating file listing...")
        notifyObservers()
        
        getFiles().addAll(Calculations.getFileList(getTopDir()))
        filterFiles()
        getInterimData().clear()
        
        for(String filePath : getFiles()){
            File file = new File(filePath)
            java.util.List<Object> entry = new ArrayList<Object>()
            entry.add(filePath)
            String updatedName = applyChange(file.name, file)
            if(file.name.equals(updatedName)){
                entry.add(Data.X_MARK)
            }
            else{
                entry.add(Data.CHECK_MARK)
            }
            entry.add(file.parent + "/" + updatedName)
            interimData.add(entry)
            setStatus("Processing $filePath")
            notifyObservers()
        }   
        setStatus("Preview Complete! ${getFiles().size()} files met the pattern.")
        notifyObservers()
        previewCompleted = true
    }
    
    public void executeOperation(){
        if(!previewCompleted){
            generatePreview()
        }
        
        int renamed = 0
        int failed = 0
        int skipped = 0
        
        for(ArrayList<Object> l : interimData){
            try{
                if(l.get(1).equals(Data.CHECK_MARK)){
                    File originalFile = new File(l.get(0).toString())
                    File renamedFile = new File(l.get(2).toString())
                    originalFile.renameTo(renamedFile) == true ? ++renamed : ++failed
                }
                else{
                    ++skipped
                }
            }
            catch(Exception ex){
                ++failed
            }
        }
        setStatus("Rename Results: $renamed renamed, $failed failed, $skipped skipped")
        notifyObservers()
    }
 }