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

package gp.gfu.main

import gp.gfu.domain.TrimmedRenameableCollection.TrimEnd;
import gp.gfu.controller.FileInfoManager;
import gp.gfu.controller.MergeFileInfoManager;
import gp.gfu.domain.Data;
import gp.gfu.domain.DateifyRenameableCollection;
import gp.gfu.domain.FileInfo;
import gp.gfu.domain.InsertRenameableCollection;
import gp.gfu.domain.RenameableCollection;
import gp.gfu.domain.ReplaceableRenameableCollection;
import gp.gfu.domain.SeriesRenameableCollection;
import gp.gfu.domain.TrimmedRenameableCollection;
import gp.gfu.presentation.BasicTableModel
import gp.gfu.presentation.FileInfoObserver;
import gp.gfu.presentation.MyTableModel;
import gp.gfu.presentation.RenameObserver;
import gp.gfu.util.Calculations;
import groovy.swing.SwingBuilder

import java.awt.BorderLayout
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.Insets
import java.awt.Point
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.AdjustmentEvent
import java.awt.event.AdjustmentListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.Collection
import java.util.Map

import javax.swing.DefaultComboBoxModel
import javax.swing.event.CaretEvent
import javax.swing.event.CaretListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPopupMenu
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities
import javax.swing.UIManager

Main main = new Main()

class Main{

    private SwingBuilder swingBuilder = new SwingBuilder()
    private int fileTableLastSelectedIndex = -1
	private String duplicateFileLastSelectedHash
	public enum WORK_STATE {CONTINUE, CANCEL}
	private String parity
	private int lastParityTextAreaLength

	public Main(){
        initializeGUI()
    }
    
    static void close(def frame, def exit) {
        if (exit) {
            System.exit(0)
        }
        else {
            frame.visible = false
        }
    }
    
    private void initializeGUI(){
        
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0))

        swingBuilder.edt {
          
            actions() {
                action(id: 'exitMenuItem', name: 'Exit', accelerator: shortcut('Q'), closure: { 
                    exitAction()
                })
                 
                action(id: 'aboutMenuItem', name: 'About', closure: {
                    aboutAction()
                })
            }
            
            def frame = frame(id:'fileUtilityFrame', title:'General File Utility', preferredSize:[800,700], defaultCloseOperation:JFrame.EXIT_ON_CLOSE, locationRelativeTo:null) {
				borderLayout()
                menuBar(constraints:BorderLayout.NORTH){
                    menu(text: "File", mnemonic: 'F') {
                        menuItem(exitMenuItem)
                    }
                                        
                    menu(text: "Help", mnemonic: 'H') {
                        menuItem(aboutMenuItem)
                    }                    
                }
                tabbedPane(constraints:BorderLayout.CENTER){
                    splitPane(title:'Directory Scan', constraints: BorderLayout.CENTER, orientation:JSplitPane.VERTICAL_SPLIT, dividerLocation:70){
                        scrollPane(constraints: "top"){
							panel(constraints:BorderLayout.CENTER){
								gridBagLayout()
								
								label(text:"Root Directory:", constraints:gbc(gridx:0, gridy:0, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3,]))
								textField(id:'scanDirTextField', text:"${System.getProperty('user.dir')}", minimumSize:[300,20], preferredSize:[300,20], constraints:gbc(gridx:1, gridy:0, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]))
								button(id:'ellipsisButton', text:'Choose Directory...', preferredSize: [150,20], constraints:gbc(gridx:2, gridy:0, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]),
									actionPerformed:{launchFileChooserAction(scanDirTextField)}
								)
								button(id:'scanDirectoryButton', text:"Scan Directory", preferredSize: [150,20], constraints:gbc(gridx:3, gridy:0, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]),
									actionPerformed: {filesToTableAction()}
								)
								button(id:'clearScanButton', text:"Clear", preferredSize: [150,20], constraints:gbc(gridx:2, gridy:1, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]),
									actionPerformed: {clearScanAction()}
								)
								button(id:'cancelScanButton', text:"Cancel", enabled:false, preferredSize: [150,20], constraints:gbc(gridx:3, gridy:1, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]),
									actionPerformed: {cancelScanAction()}
								)
							}
                        }
						
						panel(constraints:"bottom"){
							borderLayout()
							panel(constraints:BorderLayout.NORTH){
								flowLayout()
								label(text:"All Files")
							}
							scrollPane(constraints: BorderLayout.CENTER){
								table(id:'fileTable', autoCreateRowSorter:true, autoResizeMode:0, visible:true, model:new MyTableModel(), autoscrolls:true, showHorizontalLines:true, showVerticalLines:true)
							}
						}
                    }
					splitPane(title:'Duplicate Files', constraints: BorderLayout.CENTER, orientation:JSplitPane.VERTICAL_SPLIT, dividerLocation:250, resizeWeight: 0.5){
						panel(constraints:"top"){
							borderLayout()
							panel(constraints:BorderLayout.NORTH){
								flowLayout()
								label(text:"Duplicate File Signatures (with one randomly selected instance)")
							}
							scrollPane(constraints: BorderLayout.CENTER){
								table(id:'duplicateFileTable', autoCreateRowSorter:true, autoResizeMode:0, visible:true, model:new MyTableModel(), autoscrolls:true, showHorizontalLines:true, showVerticalLines:true)
							}
						}
						
						panel(constraints:"bottom"){
							borderLayout()
							panel(constraints:BorderLayout.NORTH){
								flowLayout()
								label(text:"All Instances of Chosen File Signature")
							}
							scrollPane(constraints: BorderLayout.CENTER){
								table(id:'duplicateSourcesTable', autoCreateRowSorter:true, autoResizeMode:0, visible:true, model:new MyTableModel(), autoscrolls:true, showHorizontalLines:true, showVerticalLines:true)
							}
						}
					}
					splitPane(title:'Smart Merge', constraints: BorderLayout.CENTER, orientation:JSplitPane.VERTICAL_SPLIT, dividerLocation:115, resizeWeight: 0.0){
						panel(constraints:BorderLayout.CENTER){
							gridBagLayout()
							label(text:"Source Directory", constraints:gbc(gridx:0, gridy:0, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3,]))
							textField(id:'sourceDirTextField', text:"", minimumSize:[300,20], preferredSize:[300,20], constraints:gbc(gridx:1, gridy:0, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]))
							button(id:'ellipsisMergeSourceButton', text:'Choose Directory...', preferredSize: [150,20], constraints:gbc(gridx:2, gridy:0, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]),
									actionPerformed:{
										launchFileChooserAction(sourceDirTextField)
									}
							)
							
							label(text:"Destination Directory", constraints:gbc(gridx:0, gridy:1, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3,]))
							textField(id:'destDirTextField', text:"", minimumSize:[300,20], preferredSize:[300,20], constraints:gbc(gridx:1, gridy:1, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]))
							button(id:'ellipsisMergeDestButton', text:'Choose Directory...', preferredSize: [150,20], constraints:gbc(gridx:2, gridy:1, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]),
								actionPerformed:{
									launchFileChooserAction(destDirTextField)
								}
							)
							
							panel(constraints:gbc(gridx:0, gridy:2, gridwidth:3, fill:GridBagConstraints.NONE, insets:[3,3,3,3])){
								flowLayout()
								
								checkBox(id:'copyNotMoveCheckBox', selected:true, text:'Copy (Don\'t Move)')
								
								button(id:'mergeButton', text:'Merge', preferredSize: [110,20],
									actionPerformed:{
										mergeAction()
									}
								)
								
								button(id:'cancelMergeButton', text:'Cancel', preferredSize: [110,20],
									actionPerformed:{
										cancelMergeAction()
									}
								)
							}
						}
						splitPane(constraints: BorderLayout.CENTER, orientation:JSplitPane.HORIZONTAL_SPLIT, dividerLocation:swingBuilder.fileUtilityFrame.preferredSize.width / 2, resizeWeight: 0.5){					
							panel(constraints:"left"){
								borderLayout()
								panel(constraints:BorderLayout.NORTH){
									flowLayout()
									label(text:"Merged Files")
								}
								scrollPane(constraints: BorderLayout.CENTER){
									table(id:'copiedFileTable', autoCreateRowSorter:true, autoResizeMode:0, visible:true, model:new MyTableModel(), autoscrolls:true, showHorizontalLines:true, showVerticalLines:true)
								}
							}
							panel(constraints:"right"){
								borderLayout()
								panel(constraints:BorderLayout.NORTH){
									flowLayout()
									label(text:"Duplicate Files, Not Merged")
								}
								scrollPane(constraints: BorderLayout.CENTER){
									table(id:'notCopiedFileTable', autoCreateRowSorter:true, autoResizeMode:0, visible:true, model:new MyTableModel(), autoscrolls:true, showHorizontalLines:true, showVerticalLines:true)
								}
							}
						}						
					}
					splitPane(title:'Bulk Rename', constraints: BorderLayout.CENTER, orientation:JSplitPane.VERTICAL_SPLIT, dividerLocation:140){
						scrollPane(constraints: "top"){
							splitPane(constraints: BorderLayout.CENTER, orientation:JSplitPane.HORIZONTAL_SPLIT, dividerLocation:475){
							
								panel(constraints:"left"){
									gridBagLayout()
									label(text:"Root Directory:", constraints:gbc(gridx:0, gridy:0, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]))
									textField(id:'renameDirTextField', text:"${System.getProperty('user.dir')}", minimumSize:[200,20], preferredSize:[300,20], constraints:gbc(gridx:1, gridy:0, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]))
									button(id:'ellipsisButtonRename', text:'Choose Directory...', minimumSize:[150,20], preferredSize: [150,20], constraints:gbc(gridx:2, gridy:0, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]),
										actionPerformed:{launchFileChooserAction(renameDirTextField)}
									)
									label(text:"Regex Match:", constraints:gbc(gridx:0, gridy:1, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]))
									textField(id:'regexMatchTextField', text:".+", minimumSize:[200,20], preferredSize:[300,20], constraints:gbc(gridx:1, gridy:1, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]))
									label(text:"Editor:", constraints:gbc(gridx:0, gridy:2, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]))
									comboBox(id:'editorComboBox', minimumSize:[200,20], preferredSize:[300,20], constraints:gbc(gridx:1, gridy:2, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]), model:new DefaultComboBoxModel((Object[]) ["Trim", "Replace", "Date-ify", "Insert", "Series Editor"]),
										actionPerformed:{
											editorCards.layout.show(editorCards, editorComboBox.getSelectedItem().toString())
										}
									)
									button(id:'bulkRenameExecuteButton', text:"Execute", constraints:gbc(gridx:2, gridy:2, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]), minimumSize:[150,20], preferredSize:[150,20],
											actionPerformed:{
												RenameableCollection renameableCollection = Data.getInstance().getRenameableCollection()
												if(renameableCollection == null){
													JOptionPane.showMessageDialog(null, "Execute preview first!")
												}
												else{
													renameableCollection.executeOperation()
												}
											}
										)

									button(id:'bulkRenamePreviewButton', text:"Preview", constraints:gbc(gridx:2, gridy:1, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]), minimumSize:[150,20], preferredSize:[150,20],
										actionPerformed:{
											doOutside{
												RenameObserver renameObserver = new RenameObserver(swingBuilder)
												RenameableCollection renameableCollection = null
												
												if(editorComboBox.getSelectedItem().equals("Trim")){
													TrimmedRenameableCollection.TrimEnd trimEnd
													try{
														if(swingBuilder.leftRadioButton.isSelected()){trimEnd = TrimmedRenameableCollection.TrimEnd.LEFT}
														else if(swingBuilder.rightRadioButton.isSelected()){trimEnd = TrimmedRenameableCollection.TrimEnd.RIGHT}
														else {throw new IllegalArgumentException("Left or Right not selected!")}
														int length = Integer.valueOf(swingBuilder.charsToTrimTextField.getText())
														renameableCollection = new TrimmedRenameableCollection(renameDirTextField.getText(), trimEnd, length, regexMatchTextField.getText())
													}
													catch(Exception ex){
														JOptionPane.showMessageDialog(null, "Preview Failed: ${ex.getMessage()}")
													}
												}
												else if(editorComboBox.getSelectedItem().equals("Replace")){
													renameableCollection = new ReplaceableRenameableCollection(renameDirTextField.getText(), regexMatchTextField.getText(),
														replaceCharsTextField.getText(), substituteCharsTextField.getText()) 
												}
												else if(editorComboBox.getSelectedItem().equals("Series Editor")){													
													renameableCollection = new SeriesRenameableCollection(renameDirTextField.getText(), 
														regexMatchTextField.getText(), seriesNameTextField.getText(), outputSpecTextField.getText())
												}
												else if(editorComboBox.getSelectedItem().equals("Date-ify")){
													renameableCollection = new DateifyRenameableCollection(renameDirTextField.getText(), regexMatchTextField.getText(),
															dateFormatTextField.getText())												
												}
												else if(editorComboBox.getSelectedItem().equals("Insert")){
													renameableCollection = new InsertRenameableCollection(renameDirTextField.getText(), regexMatchTextField.getText())
												}
												
												
												if(renameableCollection != null){
													try{
														renameableCollection.addObserver(renameObserver)
														renameableCollection.generatePreview()
														renamePreviewTable.setModel(new MyTableModel(renameableCollection.getData(), renameableCollection.getColumnNames(), renameableCollection))
														renamePreviewTable = Calculations.resizeJTable(renamePreviewTable, renamePreviewTable.getFont())
														Data.getInstance().setRenameableCollection(renameableCollection)
													}
													catch(Exception ex){
														JOptionPane.showMessageDialog(null, "Preview Failed: ${ex.getMessage()}")
													}
												}
											}
										}
									)
								}
								
								panel(id:'editorCards', constraints:"right"){
									cardLayout()
									panel(constraints:"Trim"){
										gridBagLayout()
										label(text:"Trim characters from one side of the filename", constraints:gbc(gridx:0,gridy:0, gridwidth:2, fill:GridBagConstraints.NONE, insets:[3,3,3,3]))
										radioButton(id:'leftRadioButton', text:"Left", constraints:gbc(gridx:0, gridy:1, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]),
											actionPerformed:{
												if(leftRadioButton.isSelected()) {rightRadioButton.setSelected(false)}
												else{rightRadioButton.setSelected(true)}
											}
										)
										radioButton(id:'rightRadioButton', text:"Right", constraints:gbc(gridx:1, gridy:1, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]),
											actionPerformed:{
												if(rightRadioButton.isSelected()){leftRadioButton.setSelected(false)}
												else{leftRadioButton.setSelected(true)}
											}
										)
										label(text:"Number of characters to trim:", constraints:gbc(gridx:0, gridy:2, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]))
										textField(id:'charsToTrimTextField', constraints:gbc(gridx:1, gridy:2, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]), minimumSize:[200,20], preferredSize:[200,20])
									}
									panel(constraints:"Replace"){
										gridBagLayout()
										label(text:"Replace character(s):", constraints:gbc(gridx:0, gridy:0, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]))
										textField(id:'replaceCharsTextField', minimumSize:[200,20], preferredSize:[200,20], constraints:gbc(gridx:1, gridy:0, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]))
										label(text:"With:", constraints:gbc(gridx:0, gridy:1, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]))
										textField(id:'substituteCharsTextField', minimumSize:[200,20], preferredSize:[200,20], constraints:gbc(gridx:1, gridy:1, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]))
									}
									panel(constraints:"Series Editor"){
										gridBagLayout()
										label(text:"Series Name:", constraints:gbc(gridx:0, gridy:0, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]))
										textField(id:'seriesNameTextField', minimumSize:[200,20], preferredSize:[200,20], constraints:gbc(gridx:1, gridy:0, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]))
										label(text:"Output Format:", constraints:gbc(gridx:0, gridy:1, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]))
										textField(id:'outputSpecTextField', text:"%sn - %se - %en - %res.%ext", minimumSize:[200,20], preferredSize:[200,20], constraints:gbc(gridx:1, gridy:1, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]))
									}
									panel(constraints:"Date-ify"){
										gridBagLayout()
										label(text:"Date Format:", constraints:gbc(gridx:0, gridy:0, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]))
										textField(id:'dateFormatTextField', text:"yyyy-MM-dd__HH-mm-ss", minimumSize:[200,20], preferredSize:[200,20], constraints:gbc(gridx:1, gridy:0, gridwidth:1, fill:GridBagConstraints.NONE, insets:[3,3,3,3]))
									}
								}
							}
						}
						scrollPane(constraints: "bottom"){
							table(id:'renamePreviewTable', autoCreateRowSorter:true, autoResizeMode:0, visible:true, model:new MyTableModel(), autoscrolls:true, showHorizontalLines:false, showVerticalLines:false)
						}
					}
					splitPane(title:"Binary Operations", constraints: BorderLayout.CENTER, orientation:JSplitPane.VERTICAL_SPLIT, dividerLocation:140){
						scrollPane(constraints:"top"){
							table(id:'digestProvidersTable', font:new Font("Deja Vu Sans", Font.PLAIN, 14), autoCreateRowSorter:true, autoResizeMode:1, visible:true, model:new MyTableModel(), autoscrolls:true, showHorizontalLines:true, showVerticalLines:true)
						}
						panel(constraints:"bottom"){
							borderLayout()
							scrollPane(id:'parityRowCounterJScrollPane', constraints: BorderLayout.WEST, 
									   verticalScrollBarPolicy:JScrollPane.VERTICAL_SCROLLBAR_NEVER,
									   horizontalScrollBarPolicy:JScrollPane.HORIZONTAL_SCROLLBAR_NEVER){
								textArea(id:'parityRowCounterTextArea', font:new Font("Consolas", Font.PLAIN, 14), text:"00000000 ", editable:false)
							}
							scrollPane(id:'parityJScrollPane', constraints: BorderLayout.CENTER){
								textArea(id:'parityTextArea', font:new Font("Consolas", Font.PLAIN, 14),
									keyReleased:{
										if(parityTextArea.getText().length() != lastParityTextAreaLength){
											formatParityText(4, 8)
											lastParityTextAreaLength = parityTextArea.getText().length()
										}
									}	
								)
							}
							scrollPane(id:'asciiJScrollPane', constraints: BorderLayout.EAST,
								verticalScrollBarPolicy:JScrollPane.VERTICAL_SCROLLBAR_NEVER,
								horizontalScrollBarPolicy:JScrollPane.HORIZONTAL_SCROLLBAR_NEVER){
								textArea(id:'asciiTextArea', font:new Font("Consolas", Font.PLAIN, 14), editable:false)
							}
						}						
					}
                }
                panel(constraints:BorderLayout.SOUTH){
                    borderLayout()
                    label(id:'statusLabel', text:'Ready!', constraints:BorderLayout.WEST)
                    progressBar(id:'processProgressBar', constraints:BorderLayout.EAST)
                }
            }
			
			parityTextArea.addCaretListener(new CaretListener(){
				
				@Override
				public void caretUpdate(CaretEvent e){
					// Call the update method here to process a partial section of the data
					int start = 0
					int end = swingBuilder.parityTextArea.getText().length()
					if(e.getDot() < e.getMark()){
						start = e.getDot()
						end = e.getMark()
					}
					else if(e.getMark() < e.getDot()){
						start = e.getMark()
						end = e.getDot()
					}
					performBinaryDataOperation(cleanHex(swingBuilder.parityTextArea.getText().substring(start, end)))
				}
			})
			
			parityJScrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener(){ 
				@Override
				public void adjustmentValueChanged(AdjustmentEvent e) {
				   swingBuilder.parityRowCounterJScrollPane.getVerticalScrollBar().setValue(e.getValue());
				   swingBuilder.asciiJScrollPane.getVerticalScrollBar().setValue(e.getValue());
				}
			})
							
            fileTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
				public void valueChanged(ListSelectionEvent e) { 
            
					ListSelectionModel lsm = (ListSelectionModel) e.getSource()
					boolean isAdjusting = e.getValueIsAdjusting()
					
					if (!lsm.isSelectionEmpty()) {
						// Find out which indexes are selected.
						int minIndex = lsm.getMinSelectionIndex()
						int maxIndex = lsm.getMaxSelectionIndex()
						if(minIndex == maxIndex && !isAdjusting){
							fileTableLastSelectedIndex = minIndex
						}
					}
				}
			})
					
            fileTable.addMouseListener(new MouseAdapter(){
                public void mouseClicked(MouseEvent e){
                    if (SwingUtilities.isLeftMouseButton(e)){
                        // Do something
                    }
                    else if(SwingUtilities.isRightMouseButton(e)){
                        
						if(swingBuilder.fileTable.getSelectedRows().size() < 2){
							Point p = e.getPoint()
							int rowNumber = swingBuilder.fileTable.rowAtPoint(p)
							ListSelectionModel model = swingBuilder.fileTable.getSelectionModel()
							model.setSelectionInterval(rowNumber, rowNumber)
						}
						
						if (!swingBuilder.fileTable.getColumnSelectionAllowed() && swingBuilder.fileTable.getRowSelectionAllowed()){
							int[] selectedRows = swingBuilder.fileTable.getSelectedRows()
							showFileOptionsPopup((int[])selectedRows, e.getX(), e.getY(), swingBuilder.fileTable, false)
						}
                    }
                }
            })
			
			duplicateSourcesTable.addMouseListener(new MouseAdapter(){
				public void mouseClicked(MouseEvent e){					
					if(SwingUtilities.isRightMouseButton(e)){
						if(swingBuilder.duplicateSourcesTable.getSelectedRows().size() < 2){
							Point p = e.getPoint()
							int rowNumber = swingBuilder.fileTable.rowAtPoint(p)
							ListSelectionModel model = swingBuilder.duplicateSourcesTable.getSelectionModel()
							model.setSelectionInterval(rowNumber, rowNumber)
						}
						
						if (!swingBuilder.duplicateSourcesTable.getColumnSelectionAllowed() && swingBuilder.duplicateSourcesTable.getRowSelectionAllowed()){
							int[] selectedRows = swingBuilder.duplicateSourcesTable.getSelectedRows()
							showFileOptionsPopup((int[])selectedRows, e.getX(), e.getY(), swingBuilder.duplicateSourcesTable, true)
						}
					}
				}
			})
			
			/*
			duplicateFileTable.addMouseListener(new MouseAdapter(){
				public void mouseClicked(MouseEvent e){					
					if(SwingUtilities.isLeftMouseButton(e)){
						if(swingBuilder.duplicateFileTable.getSelectedRows().size() < 2){
							Point p = e.getPoint()
							int rowNumber = swingBuilder.fileTable.rowAtPoint(p)
							ListSelectionModel model = swingBuilder.duplicateFileTable.getSelectionModel()
							model.setSelectionInterval(rowNumber, rowNumber)
						}
						
						if (!swingBuilder.duplicateFileTable.getColumnSelectionAllowed() && swingBuilder.duplicateFileTable.getRowSelectionAllowed()){
							int[] selectedRows = swingBuilder.duplicateFileTable.getSelectedRows()
							String hash = swingBuilder.duplicateFileTable.getValueAt(selectedRows[0], 2)							
							swingBuilder.duplicateSourcesTable.setModel(new MyTableModel(fileInfoToArray(Data.getInstance().getUniqueFilesMap().get(hash)), Data.getInstance().getColumnNames(), null))
							swingBuilder.duplicateSourcesTable = Calculations.resizeJTable(swingBuilder.duplicateSourcesTable, swingBuilder.duplicateSourcesTable.getFont())
						}
					}
				}
			})*/
			
			duplicateFileTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
				public void valueChanged(ListSelectionEvent e) { 
            
					ListSelectionModel lsm = (ListSelectionModel) e.getSource()
					boolean isAdjusting = e.getValueIsAdjusting()
					
					if (!lsm.isSelectionEmpty()) {
						// Find out which indexes are selected.
						int minIndex = lsm.getMinSelectionIndex()
						int maxIndex = lsm.getMaxSelectionIndex()
						if(minIndex == maxIndex && !isAdjusting){
							int[] selectedRows = swingBuilder.duplicateFileTable.getSelectedRows()
							String hash = swingBuilder.duplicateFileTable.getValueAt(selectedRows[0], 2)
							duplicateFileLastSelectedHash = hash							
							swingBuilder.duplicateSourcesTable.setModel(new MyTableModel(fileInfoToArray(Data.getInstance().getUniqueFilesMap().get(hash)), Data.getInstance().getColumnNames(), null))
							swingBuilder.duplicateSourcesTable = Calculations.resizeJTable(swingBuilder.duplicateSourcesTable, swingBuilder.duplicateSourcesTable.getFont())
						}
					}
				}
			})
			
			renamePreviewTable.addMouseListener(new MouseAdapter(){
				public void mouseClicked(MouseEvent e){
					if(SwingUtilities.isRightMouseButton(e)){
						if (!swingBuilder.renamePreviewTable.getColumnSelectionAllowed() && swingBuilder.renamePreviewTable.getRowSelectionAllowed()){
							int[] selectedRows = swingBuilder.renamePreviewTable.getSelectedRows()
							showRenameOptionsPopup((int[])selectedRows, e.getX(), e.getY(), swingBuilder.renamePreviewTable)
						}
					}
				}
			})
						
            frame.pack()
            frame.show()
        }
    }
	
	def performBinaryDataOperation(String text){
		swingBuilder.edt{						
			swingBuilder.processProgressBar.indeterminate = true
			doOutside{
				try{
					List<List<Object>> results = Calculations.calculateAllMessageDigests(text)
					Data.getInstance().getMessageDigestDataList().clear()
					Data.getInstance().getMessageDigestDataList().addAll(results)
					
					updateBinaryCalculationsTable()
					doLater{
						swingBuilder.processProgressBar.indeterminate = false
					}
				}
				catch(Exception ex){
					doLater{
						swingBuilder.processProgressBar.indeterminate = false
					}
				}				
			}
		}
	}
	
	def cleanHex(String text){
		text = text.replaceAll("[^0-9ABCDEFabcdef]", '')
		
		return text
	}
	
	/**
	 * Takes free-form input and formats it with digit grouping.
	 * 
	 * @return
	 */
	def formatParityText(int charsPerWord, int wordsPerLine){
		swingBuilder.edt{
			
			String text = swingBuilder.parityTextArea.getText()
			StringBuilder sb = new StringBuilder()
			StringBuilder counter = new StringBuilder()
			counter.append("00000000 ")
			StringBuilder ascii = new StringBuilder()
			
			doOutside{
				text = cleanHex(text)				
					
				int bits = text.length() * 4
				int bytes = bits % 8 == 0 ? (bits / 8) : (bits / 8 + 1)
				String label = "Length: ${Calculations.customFormat('###,###,###', text.length())} hex chars; " +
							   "${Calculations.customFormat('###,###,###', bits)} bits; " +
							   "${Calculations.customFormat('###,###,###', bytes)} bytes"
				
				// We want the output to look like this:
				// 1234 5233 5923 4234 ab32 def9 acd3 92ff [...]
				for(int i = 0; i < text.length(); i += charsPerWord){
					if(i % (charsPerWord * wordsPerLine) == 0 && i > 0){
						// remove trailing space
						sb.setLength(sb.length() - 1)
						sb.append("\n")
						ascii.append("\n")
						counter.append("\n" + Integer.toString((i / 2).intValue(), 8).padLeft(8, '0') + " ")
					}
					
					List<String> asciiBytes = new ArrayList<String>()
					
					for(int j = 0; j < charsPerWord; j += 2){
						asciiBytes.add(Calculations.safeSubstring(text, i + j, i + j + 2))
					}
					
					for(String asciiByte : asciiBytes){
						if(asciiByte.length() > 0){
							String toAppend = String.valueOf((char) (Integer.decode("0x" + asciiByte) & 0xFF))
							toAppend = toAppend.replaceAll(/[^\p{Print}]/, String.valueOf((char) Integer.decode("0x00")))
							ascii.append(toAppend)
						}
					}

					int end = (i + charsPerWord > text.length()) ? (text.length()) : (i + charsPerWord)
					sb.append(text.substring(i, end))
					sb.append(" ")
				}	
				sb.setLength(sb.toString().trim().length())
				
				doLater{
					swingBuilder.parityTextArea.setText(sb.toString())
					swingBuilder.statusLabel.setText(label)
					swingBuilder.parityRowCounterTextArea.setText(counter.toString())
					swingBuilder.asciiTextArea.setText(ascii.toString())
				}
			}
			
		}
	}
	
	def showRenameOptionsPopup(java.util.List<Integer> selectedRows, int x, int y, JTable parent){
		JPopupMenu popup = new JPopupMenu()
		JMenuItem skip = new JMenuItem("Skip")
		JMenuItem enable = new JMenuItem("Enable")
		
		popup.add(skip)
		popup.add(enable)
		
		skip.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				for(int i = 0; i < selectedRows.size(); i++){
					parent.setValueAt(Data.X_MARK, selectedRows.get(i), parent.getColumnModel().getColumnIndex("Rename?"))
				}
			}
		})
		
		enable.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				for(int i = 0; i < selectedRows.size(); i++){
					parent.setValueAt(Data.CHECK_MARK, selectedRows.get(i), parent.getColumnModel().getColumnIndex("Rename?"))
				}
			}
		})
		
		popup.show(parent, x, y)
	}
	
	def showFileOptionsPopup(java.util.List<Integer> selectedRows, int x, int y, JTable parent, boolean deleteEnabled){
		JPopupMenu popup = new JPopupMenu()
		JMenuItem delete = new JMenuItem("Delete")
		JMenuItem open = new JMenuItem("Open")
		JMenuItem browse = new JMenuItem("Browse")
		delete.setEnabled(deleteEnabled)
		
		popup.add(open)
		popup.add(browse)
		popup.add(delete)
		
		open.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event){
				for(int i = 0; i < selectedRows.size(); i++){
					String path = parent.getValueAt(selectedRows.get(i), parent.getColumnModel().getColumnIndex("Name"))		
					Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + path);
				}
			}
		
		})
		
		browse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event){
				for(int i = 0; i < selectedRows.size(); i++){
					String path = parent.getValueAt(selectedRows.get(i), parent.getColumnModel().getColumnIndex("Name"))
					File file = new File(path)
					Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + file.getParent());
				}
			}
		})
		
		delete.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent event) {
				try{
					int selection = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete ${selectedRows.size()} file(s)?", "Delete selected files?", JOptionPane.YES_NO_OPTION)
					if(selection == JOptionPane.YES_OPTION){
						String hash = ""
						for(int i = 0; i < selectedRows.size(); i++){
							String path = parent.getValueAt(selectedRows.get(i), parent.getColumnModel().getColumnIndex("Name"))		
							File file = new File(path)
							if(file.delete()){
								hash = parent.getValueAt(selectedRows.get(i), parent.getColumnModel().getColumnIndex("MD5"))
								String name = parent.getValueAt(selectedRows.get(i), parent.getColumnModel().getColumnIndex("Name"))
								
								for(Iterator<FileInfo> it = Data.getInstance().getUniqueFilesMap().get(hash).iterator(); it.hasNext();){
									FileInfo next = it.next()
									if(next.getPath().equals(name)){
										it.remove()
									}			
								}
							}
						}

						if(Data.getInstance().getUniqueFilesMap().get(hash).size() == 0){
							Data.getInstance().getUniqueFilesMap().remove(hash)
						}
						
						updateTableData()
					}
				}
				catch(Exception ex){
					ex.printStackTrace()
				}
			}
		})
		
		popup.show(parent, x, y)
		
	}
	
	def cancelScanAction(){
		Data.getInstance().setScanCanceled(true)
		swingBuilder.scanDirectoryButton.setEnabled(true)
		swingBuilder.clearScanButton.setEnabled(true)
		swingBuilder.cancelScanButton.setEnabled(false)
		swingBuilder.mergeButton.setEnabled(true)
		swingBuilder.cancelMergeButton.setEnabled(false)
	}
	
	def clearScanAction(){
		clearTable(swingBuilder.fileTable, Data.getInstance().getFileInfoData(Data.getInstance().getFileInfoDataList()))
		clearTable(swingBuilder.duplicateFileTable, Data.getInstance().getEmptyData())
		clearTable(swingBuilder.duplicateSourcesTable, Data.getInstance().getEmptyData())
		clearTable(swingBuilder.copiedFileTable, Data.getInstance().getEmptyData())
		clearTable(swingBuilder.notCopiedFileTable, Data.getInstance().getEmptyData())
		Data.getInstance().getUniqueFilesMap().clear()
		Data.getInstance().getUniqueFilesSet().clear()
		Data.getInstance().getFileInfoDataList().clear()
		Data.getInstance().getMergedFileInfoDataList().clear()
		Data.getInstance().getNotMergedFileInfoDataList().clear()
	}
    
	def cancelMergeAction(){
		Data.getInstance().setMergeCanceled(true)
		swingBuilder.scanDirectoryButton.setEnabled(true)
		swingBuilder.clearScanButton.setEnabled(true)
		swingBuilder.cancelScanButton.setEnabled(false)
		swingBuilder.mergeButton.setEnabled(true)
		swingBuilder.cancelMergeButton.setEnabled(false)
	}
	
	def mergeAction(){
		MergeFileInfoManager fileInfoManager = null
		java.util.List<String> fileList = new ArrayList<String>()
		boolean copyOnly = swingBuilder.copyNotMoveCheckBox.selected
		Data.getInstance().getMergedFileInfoDataList().clear()
		Data.getInstance().getNotMergedFileInfoDataList().clear()
		
		swingBuilder.edt{
			workingStatus()
			swingBuilder.scanDirectoryButton.setEnabled(false)
			swingBuilder.clearScanButton.setEnabled(false)
			swingBuilder.cancelScanButton.setEnabled(false)
			swingBuilder.mergeButton.setEnabled(false)
			swingBuilder.cancelMergeButton.setEnabled(true)
        
			doOutside{
                fileList.addAll(Calculations.getFileList(swingBuilder.sourceDirTextField.getText()))
				swingBuilder.cancelScanButton.setEnabled(true)
				fileInfoManager = new MergeFileInfoManager(fileList, swingBuilder.destDirTextField.getText(), 
					swingBuilder.sourceDirTextField.getText(), copyOnly)
				Data.getInstance().setMergeCanceled(false)
								
				FileInfoObserver fileInfoObserver = new FileInfoObserver(fileInfoManager, swingBuilder)
				fileInfoManager.addObserver(fileInfoObserver)
				fileInfoManager.processFiles()
				
                Data.getInstance().getMergedFileInfoDataList().addAll(fileInfoManager.getInterimMergedData())
				Data.getInstance().getNotMergedFileInfoDataList().addAll(fileInfoManager.getInterimNotMergedData())
				
				doLater{
					finishedWorkStatus()
					swingBuilder.statusLabel.text = "Recent Merge Analzyed Files: ${Calculations.customFormat('###,###,###', fileInfoManager.getFilesProcessedCount())}  Size: ${Calculations.customFormat('###,###,###,###,###', fileInfoManager.getTotalSize())} bytes"
					updateMergeTableData()
					swingBuilder.scanDirectoryButton.setEnabled(true)
					swingBuilder.clearScanButton.setEnabled(true)
					swingBuilder.cancelScanButton.setEnabled(false)
					swingBuilder.mergeButton.setEnabled(true)
				}
			}
		}
	}
	
    def filesToTableAction(){
		FileInfoManager fileInfoManager = null
        java.util.List<String> fileList = new ArrayList<String>()
		
		swingBuilder.edt{
			workingStatus()
			swingBuilder.scanDirectoryButton.setEnabled(false)
			swingBuilder.clearScanButton.setEnabled(false)
			swingBuilder.cancelScanButton.setEnabled(false)
			swingBuilder.mergeButton.setEnabled(false)
        
			doOutside{
                fileList.addAll(Calculations.getFileList(swingBuilder.scanDirTextField.getText()))
				swingBuilder.cancelScanButton.setEnabled(true)
				fileInfoManager = new FileInfoManager(fileList)
				Data.getInstance().setScanCanceled(false)
				
				FileInfoObserver fileInfoObserver = new FileInfoObserver(fileInfoManager, swingBuilder)
				fileInfoManager.addObserver(fileInfoObserver)
				fileInfoManager.processFiles()
				
                Data.getInstance().getFileInfoDataList().addAll(fileInfoManager.getInterimData())
				
				doLater{
					finishedWorkStatus()
					swingBuilder.statusLabel.text = "Recent Scan: ${Calculations.customFormat('###,###,###', fileInfoManager.getFilesProcessedCount())} files; Size: ${Calculations.customFormat('###,###,###,###,###', fileInfoManager.getTotalSize())} bytes"
					updateTableData()
					swingBuilder.scanDirectoryButton.setEnabled(true)
					swingBuilder.clearScanButton.setEnabled(true)
					swingBuilder.cancelScanButton.setEnabled(false)
					swingBuilder.mergeButton.setEnabled(true)
				}
			}
		}
    }
	
	def updateBinaryCalculationsTable(){
		swingBuilder.edt{
			String[] columnNames = new String[2]
			columnNames[0] = "Message Digest Provider"
			columnNames[1] = "Value"
			//
			swingBuilder.digestProvidersTable.setModel(new BasicTableModel(
				Data.convertListToArray(Data.getInstance().getMessageDigestDataList()), columnNames))
			swingBuilder.digestProvidersTable = Calculations.resizeJTable(swingBuilder.digestProvidersTable, swingBuilder.digestProvidersTable.getFont())
		}
	}
	
	def updateTableData(){
		swingBuilder.edt{
			swingBuilder.fileTable.setModel(new MyTableModel(Data.getInstance().getFileInfoData(Data.getInstance().getFileInfoDataList()), Data.getInstance().getColumnNames(), null))
			swingBuilder.fileTable = Calculations.resizeJTable(swingBuilder.fileTable, swingBuilder.fileTable.getFont())
			swingBuilder.duplicateFileTable.setModel(new MyTableModel(fileMapInfoToArray(Data.getInstance().getUniqueFilesMap()), Data.getInstance().getColumnNames(), null))
			swingBuilder.duplicateFileTable = Calculations.resizeJTable(swingBuilder.duplicateFileTable, swingBuilder.duplicateFileTable.getFont())
			
			if(duplicateFileLastSelectedHash != null && duplicateFileLastSelectedHash.length() > 0 &&
			   Data.getInstance().getUniqueFilesMap().containsKey(duplicateFileLastSelectedHash)){
				swingBuilder.duplicateSourcesTable.setModel(new MyTableModel(fileInfoToArray(Data.getInstance().getUniqueFilesMap().get(duplicateFileLastSelectedHash)), Data.getInstance().getColumnNames(), null))
				swingBuilder.duplicateSourcesTable = Calculations.resizeJTable(swingBuilder.duplicateSourcesTable, swingBuilder.duplicateSourcesTable.getFont())
			}
			else{
				swingBuilder.duplicateSourcesTable.setModel(new MyTableModel())
			}
		}
	}
	
	def updateMergeTableData(){
		swingBuilder.edt{
			swingBuilder.copiedFileTable.setModel(new MyTableModel(Data.getInstance().getFileInfoData(Data.getInstance().getMergedFileInfoDataList()), Data.getInstance().getColumnNames(), null))
			swingBuilder.copiedFileTable = Calculations.resizeJTable(swingBuilder.copiedFileTable, swingBuilder.copiedFileTable.getFont())
			swingBuilder.notCopiedFileTable.setModel(new MyTableModel(Data.getInstance().getFileInfoData(Data.getInstance().getNotMergedFileInfoDataList()), Data.getInstance().getColumnNames(), null))
			swingBuilder.notCopiedFileTable = Calculations.resizeJTable(swingBuilder.notCopiedFileTable, swingBuilder.notCopiedFileTable.getFont())
		}
	}
	
	private Object[][] fileInfoToArray(Collection<FileInfo> fileInfoCollection){
		Object[][] data = new Object[fileInfoCollection.size()][3]
		FileInfo[] fileInfoArray = (FileInfo[]) fileInfoCollection.toArray()
		for(int i = 0; i < fileInfoArray.size(); i++){
			data[i][0] = fileInfoArray[i].getPath()
			data[i][1] = fileInfoArray[i].getSize()
			data[i][2] = fileInfoArray[i].getHash()
		}
		return data
	}
	
	private Object[][] fileMapInfoToArray(Map<String, FileInfo> fileInfoMap){
		Map<String, FileInfo> tempMap = new HashMap<String, FileInfo>()
		
		for(String key : fileInfoMap.keySet()){
			if(fileInfoMap.get(key).size() > 1){
				tempMap.put(key, fileInfoMap.get(key))
			}
		}
		
		Object[][] data = new Object[tempMap.size()][3]
		int index = 0
		for(String key : tempMap.keySet()){
			data[index][0] = tempMap.get(key).get(0).getName()
			data[index][1] = tempMap.get(key).get(0).getSize()
			data[index][2] = tempMap.get(key).get(0).getHash()
			index++
		}
		return data
	}
			
    def aboutAction(){
        swingBuilder.edt{
            dialog(title: 'About General File Utility', size: [350, 250], show: true, owner: fileUtilityFrame, modal: true, locationRelativeTo: fileUtilityFrame) {
                borderLayout()
                label(text: 'General File Utility', constraints: BorderLayout.NORTH, border: emptyBorder(10))
                panel(constraints: BorderLayout.CENTER, border: emptyBorder(10)) {
                    borderLayout()
                    scrollPane(horizontalScrollBarPolicy: JScrollPane.HORIZONTAL_SCROLLBAR_NEVER, border: null) {
                        table() {
                             def systemProps = []
                             for (propName in System.properties.keySet()) {
                                 systemProps.add([property: propName, value: System.properties.getProperty(propName)])
                             }
                            tableModel(list: systemProps) {
                                 propertyColumn(header:'Property', propertyName:'property')
                                 propertyColumn(header:'Value', propertyName:'value')
                            }
                        }
                    }
                }
            }
        }
    }
        
    def exitAction(){
        swingBuilder.edt{
            close(fileUtilityFrame, true)
        }
    }
    
    def clearTable(JTable table, Object[][] dataModel){
        swingBuilder.edt{
            String[] columnNames = (String[])([""])
            Object[][] data = new Object[1][1]
            
            data[0][0] = ""
            
            dataModel = data
            swingBuilder.statusLabel.text = ""
            table.setModel(new MyTableModel(data, columnNames, null))
        }
    }
     
    def workingStatus(){
        swingBuilder.edt{
			statusLabel.text = "Gathering file listing, please wait..."
			swingBuilder.processProgressBar.value = 0
		}
    }
    
    def finishedWorkStatus(){
        swingBuilder.edt{statusLabel.text = "Finished."}
    }
    
    def launchFileChooserAction(javax.swing.JTextField textField){
        JFileChooser jFileChooser = new JFileChooser()
		if(textField.getText().length() > 0){
			try{
				File f = new File(textField.getText())
				if(f.isDirectory()){
					jFileChooser.setCurrentDirectory(new File(textField.getText()))
				}
			}catch(Exception e){
			
			}
		}
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
        int returnVal = jFileChooser.showOpenDialog(null)
        if(returnVal == JFileChooser.APPROVE_OPTION){
            textField.setText(jFileChooser.getSelectedFile().getAbsolutePath())
        }
    }  

	public SwingBuilder getSwingBuilder(){
		return this.swingBuilder
	}
}