import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;

import au.com.bytecode.opencsv.CSVReader;

import com.mysql.jdbc.ResultSetMetaData;
/*TODO: 
 * 		IMPLEMENT CSV CONVERSION TO TABLE IN TAB3
 * 			-have to resize the jscrolltable window, and figure out why text field disappears when it is added
 * 		IMPLEMENT VIEWER TO TAB 4
 *
 */


public class IgpGui extends JTabbedPane implements ActionListener {
	
	//primer generator tab
	JComboBox<String> organism;		//drop down box containing organism names
	JTextField strain;				//text field for user to enter a strain
	JTextField geneID;				//text field for user to enter a Gene ID
	JTextField thresholdValue;		//single line box for user to enter their preferred threshold value
	JRadioButton humanFilterY;		//human genome filter radio buttons (yes/no)
	JRadioButton humanFilterN;
	JRadioButton hmrgdBlastY;		//HMRGD filter radio buttons (yes/no)
	JRadioButton hmrgdBlastN;
	JRadioButton localBlastY;		//local blast radio buttons (yes/no)
	JRadioButton localBlastN;
	JButton submit;					//submission button
	JFileChooser chooseFile;		//file chooser for multiplex
	JButton chooseButton;
	JTextField chosenFile;			//displays path of chosen file
	
	//database viewer tab
	JComboBox<String> dbTable;		//drop down box containing table names
	JButton tableSelect;			//select button for dropdown box after table selection is made
	JTable dbDisplay;			//text area for display db table contents
	
	//results table tab
	JButton resultSubmit;
	JButton mostRecentSubmit;
	JButton chooseResultsButton;
	JFileChooser chooseResultFile;
	JTextField chosenResultFile;
	JTable resultDisplay;
	CSVReader reader;
	String[] line;			//csv reader table items
	Object[] row;
	String[] columnNames;
	
	private static final long serialVersionUID = -420254383943138649L;
	
	public IgpGui() throws ClassNotFoundException {			//default constructor for the GUI
		setPreferredSize(new Dimension(900, 500));
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////*******************************************
		//									DATA ENTRY TAB													//
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		JPanel tab1Contents = new JPanel(new GridBagLayout());			//create a panel for the tabbed pane of data entry items
		this.add("Primer Generation", tab1Contents);							//add panel to tabbed pane
		GridBagConstraints tab1Constraints = new GridBagConstraints();
		///////////////////////////
		//ORGANISM DROP DOWN MENU//
		///////////////////////////
		//set up position of the organism drop down menu and its label
		tab1Constraints.gridx = 0;
		tab1Constraints.gridy = 0;
		tab1Constraints.anchor=GridBagConstraints.WEST;
		tab1Contents.add(new Label("Select an organism:  "), tab1Constraints);	//organism drop down menu label (0,0)
		organism = new JComboBox<String>();
		tab1Constraints.gridx = 1;
		tab1Constraints.gridy = 0;
		
		//connect to db for list of organisms
		Class.forName("com.mysql.jdbc.Driver"); //use class loader for db connections
		String username = "bif712_143a03";		//username
		String pw = "qhBQ5335";					//pw
		String dbURL = "jdbc:mysql://zenit.senecac.on.ca/bif712_143a03"; //database url
		Connection conn = null;
		Statement stmt = null;
		String retrieveNames = "SELECT organism_name FROM ORGANISM_GENE";	//statement to retrieve names from the db
		System.out.println("attempting to connect to database: " + dbURL + "with usr: "+ username + " and pw: " + "pw");
		try {
			conn = DriverManager.getConnection(dbURL, username, pw);
			System.out.println("successful connection to database");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// run statement query
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if (stmt != null){
			try {
				ResultSet records = stmt.executeQuery(retrieveNames);
				while (records.next()){										//while there are still entries in the db, add organisms as drop down box entries
					organism.addItem(records.getString("organism_name"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		tab1Contents.add(organism, tab1Constraints);
		
		
		/////////////////////
		//Strain Text Field//
		/////////////////////
		tab1Constraints.gridx = 2;
		tab1Constraints.gridy = 0;
		tab1Contents.add(new Label("and strain:"), tab1Constraints); //strain text field label (2, 0)
		tab1Constraints.gridx = 3;
		tab1Constraints.gridy = 0;
		strain = new JTextField("", 8);
		tab1Contents.add(strain, tab1Constraints);	//strain text field (3,0)
		
		
		/////////////////////
		//GeneID Text Field//
		/////////////////////
		tab1Constraints.gridx = 2;
		tab1Constraints.gridy = 1;
		tab1Contents.add(new Label("or gene ID:"), tab1Constraints); //gene id text field label (2,1)
		tab1Constraints.gridx = 3;
		tab1Constraints.gridy = 1;
		geneID = new JTextField("", 8);
		tab1Contents.add(geneID, tab1Constraints);	//geneID text field (2,2)
		
		
		////////////////////////
		// THRESHOLD TEXTFIELD//
		////////////////////////
		tab1Constraints.gridx = 0;
		tab1Constraints.gridy = 2;
		tab1Contents.add(new Label("Enter a threshold value (%)"), tab1Constraints);	//threshold value label (0,2)
		tab1Constraints.gridx = 1;
		tab1Constraints.gridy = 2;
		thresholdValue = new JTextField("90", 8);
		tab1Contents.add(thresholdValue, tab1Constraints);			//threshold value text field (1,2)
		
		
		//////////////////////////////
		//HUMAN FILTER RADIO BUTTONS//    //CHANGE TO CHECKBOX
		//////////////////////////////
		humanFilterY = new JRadioButton("YES");
		humanFilterN = new JRadioButton("NO");
		ButtonGroup humanFilter = new ButtonGroup();//create button group for human filter
		humanFilter.add(humanFilterY);
		humanFilter.add(humanFilterN);
		humanFilterN.setSelected(true);
		tab1Constraints.gridx = 0;
		tab1Constraints.gridy = 3;
		tab1Contents.add(new Label ("Filter through human genome?"), tab1Constraints); //Human filter radio buttons label (0,3)
		tab1Constraints.gridx = 1;
		tab1Contents.add(humanFilterY, tab1Constraints);	//human filter yes button (1,3)
		tab1Constraints.gridx = 2;
		tab1Contents.add(humanFilterN, tab1Constraints);	//human filter no button (2, 3)
		
		//RUN HMRGD BLAST
		hmrgdBlastY = new JRadioButton("YES");
		hmrgdBlastN = new JRadioButton("NO");
		ButtonGroup hmrgdBlast = new ButtonGroup();//create button group for human filter
		hmrgdBlast.add(hmrgdBlastY);
		hmrgdBlast.add(hmrgdBlastN);
		hmrgdBlastN.setSelected(true);
		tab1Constraints.gridx = 0;
		tab1Constraints.gridy = 4;
		tab1Contents.add(new Label ("Run BLAST against HMRGD gut database?"), tab1Constraints); //Human filter radio buttons label (0,3)
		tab1Constraints.gridx = 1;
		tab1Contents.add(hmrgdBlastY, tab1Constraints);	//human filter yes button (1,3)
		tab1Constraints.gridx = 2;
		tab1Contents.add(hmrgdBlastN, tab1Constraints);	//human filter no button (2, 3)
		
		//RUN LOCAL BLAST  // CHECK FOR PRIMER DIMERS CHECKBOX
		localBlastY = new JRadioButton("YES");
		localBlastN = new JRadioButton("NO");
		ButtonGroup localBlast = new ButtonGroup();//create button group for human filter
		localBlast.add(localBlastY);
		localBlast.add(localBlastN);
		localBlastN.setSelected(true);
		tab1Constraints.gridx = 0;
		tab1Constraints.gridy = 5;
		tab1Contents.add(new Label ("Run local BLAST against primers?"), tab1Constraints); //Human filter radio buttons label (0,3)
		tab1Constraints.gridx = 1;
		tab1Contents.add(localBlastY, tab1Constraints);	//human filter yes button (1,3)
		tab1Constraints.gridx = 2;
		tab1Contents.add(localBlastN, tab1Constraints);	//human filter no button (2, 3)
		
		
		//////////////////////////
		//MULTIPLEX FILE CHOOSER//
		//////////////////////////
		tab1Constraints.gridx = 0;
		tab1Constraints.gridy	= 6;
		tab1Contents.add(new Label("Upload a file for multiplex:"), tab1Constraints);		//file chooser label (0, 5)
		tab1Constraints.gridy = 7;
		tab1Constraints.fill = GridBagConstraints.BOTH;
		chooseFile = new JFileChooser();
		chooseFile.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);	//set file chooser to look at files and directories
		chooseButton = new JButton("Browse");
		chooseButton.addActionListener(this);
		chooseButton.setActionCommand("browse");
		tab1Contents.add(chooseButton, tab1Constraints);									//file chooser button (0,6)
		tab1Constraints.gridx = 1;
		chosenFile = new JTextField(30);
		tab1Contents.add(chosenFile, tab1Constraints);										//chosen file text field (1, 6)
		
		
		//////////////////
		// SUBMIT BUTTON//
		//////////////////
		tab1Constraints.gridx = 0;
		tab1Constraints.gridy = 8;
		tab1Constraints.fill = GridBagConstraints.BOTH;
		tab1Constraints.anchor=GridBagConstraints.CENTER;
		submit = new JButton("Submit");
		tab1Contents.add(submit, tab1Constraints);
		submit.addActionListener(this);
		submit.setActionCommand("submit");
	
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////******************************************************************
		//												DATABASE VIEWING TAB											//
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		JPanel tab2Contents = new JPanel(new GridBagLayout());
		this.add("Database Viewer", tab2Contents);
		GridBagConstraints tab2Constraints = new GridBagConstraints();
		tab2Constraints.fill = GridBagConstraints.BOTH;

		////////////////////////////
		//TABLE SELECTION COMBOBOX//
		////////////////////////////
		tab2Constraints.gridx = 0;
		tab2Constraints.gridy = 0;
		tab2Constraints.weightx = 1;
		tab2Contents.add(new Label("Select a table for viewing:"), tab2Constraints);
		tab2Constraints.gridx = 1;
		dbTable = new JComboBox<String>();
		//use db connection to generate radio buttons for tables
		String getTables = "SELECT * FROM information_schema.tables WHERE TABLE_SCHEMA = 'bif712_143a03';";
		
		if (stmt != null){
			try {
				ResultSet records = stmt.executeQuery(getTables);
				while (records.next()){										//while there are still entries in the db, add organisms as drop down box entries
					dbTable.addItem(records.getString("TABLE_NAME"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		tab2Contents.add(dbTable, tab2Constraints);
		
		//TABLE SELECTION CONFIRM
		tab2Constraints.gridx = 2;
		tableSelect = new JButton("Select Table");
		tab2Contents.add(tableSelect, tab2Constraints);
		tableSelect.addActionListener(this);
		tableSelect.setActionCommand("tableSelect");
		
		
		//TABLE DISPLAY WINDOW
		tab2Constraints.gridx = 0;
		tab2Constraints.gridy = 1;
		tab2Constraints.fill = GridBagConstraints.BOTH;
		tab2Constraints.gridwidth = 3;
		tab2Constraints.weighty = 1;
		tab2Constraints.weightx = 2;
		dbDisplay = new JTable();
		dbDisplay.setRowSelectionAllowed(true);
		dbDisplay.setColumnSelectionAllowed(true);
		dbDisplay.setCellSelectionEnabled(true);
		JScrollPane tableScroller = new JScrollPane(dbDisplay);
		tab2Contents.add(tableScroller, tab2Constraints);
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//								RESULT SET TABLE TAB								//
		//////////////////////////////////////////////////////////////////////////////////////
		JPanel tab3Contents = new JPanel(new GridBagLayout());
		this.add("Results Table", tab3Contents);
		GridBagConstraints tab3Constraints = new GridBagConstraints();
		tab3Constraints.fill = GridBagConstraints.BOTH;
		
		//RESULTS CHOOSER AND ITS BUTTON
		tab3Constraints.gridx = 0;
		tab3Constraints.gridy = 0;
		chooseResultFile = new JFileChooser();
		chooseResultFile.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooseResultsButton = new JButton("Browse Existing Results");
		chooseResultsButton.addActionListener(this);
		chooseResultsButton.setActionCommand("browseResults");
		tab3Contents.add(chooseResultsButton, tab3Constraints);
		
		//CHOSEN RESULT FILE TEXT FIELD
		tab3Constraints.gridx = 1;
		chosenResultFile = new JTextField(30);
		tab3Contents.add(chosenResultFile, tab3Constraints);
		
		//RESULT FILE SUBMIT BUTTON
		tab3Constraints.gridx = 4;
		resultSubmit = new JButton("Submit");
		resultSubmit.addActionListener(this);
		resultSubmit.setActionCommand("submitResultFile");
		tab3Contents.add(resultSubmit, tab3Constraints);
		
		//LATEST GENERATED FILE BUTTON
		tab3Constraints.gridx = 5;
		mostRecentSubmit = new JButton("Use Last Modified File");
		mostRecentSubmit.addActionListener(this);
		mostRecentSubmit.setActionCommand("submitResultFileMostRecent");
		tab3Contents.add(mostRecentSubmit, tab3Constraints);
		
		//RESULTS TABLE AND SCROLL PANE
		tab3Constraints.gridx = 0;
		tab3Constraints.gridy = 1;
		tab3Constraints.gridwidth = 6;
		tab3Constraints.weighty = 1;
		tab3Constraints.weightx = 2;
		resultDisplay = new JTable();
		resultDisplay.setRowSelectionAllowed(true);
		resultDisplay.setColumnSelectionAllowed(true);
		resultDisplay.setCellSelectionEnabled(true);
		JScrollPane resultScroller = new JScrollPane(resultDisplay);
		tab3Contents.add(resultScroller, tab3Constraints);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ACTION LISTENER CARRYS OUT ACTIONS WHEN A BUTTON IS PRESSED						//
	//////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void actionPerformed(ActionEvent event) {

		//connect to database (PERFORMED EVERY TIME AN ACTION EVENT OCCURS)
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} 
		//use class loader for database connections
		String username = "bif712_143a03";		//username
		String pw = "qhBQ5335";					//pw
		String dbURL = "jdbc:mysql://zenit.senecac.on.ca/bif712_143a03"; //database url
		Connection conn = null;
		Statement stmt = null;
		System.out.println("attempting to connect to database: " + dbURL + "with usr: "+ username + " and pw: " + "pw");
		try {
			conn = DriverManager.getConnection(dbURL, username, pw);
			System.out.println("successful connection to database");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//run statement query
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if (event.getActionCommand() == "submit"){					//when submit button is pressed, an array of strings is generated based on the items selected
			File f_primer = new File("output/primer_done.txt");
			f_primer.delete();
			File f_human = new File("output/human_done.txt");
			f_human.delete();
			File f_hmrgd = new File("output/hmrgd_done.txt");
			f_hmrgd.delete();
			File f_dimer = new File("output/dimer_done.txt");
			f_dimer.delete();
			
			Process p = null;
			String fastaFileName = "";
			String[] command = new String[10];						
			command[0] = "perl";									//always set to perl as perl is run
			command[1] = "primer3plus_v1.9.2.pl";					//set to the perl script name and/or path
			command[2] = (String) organism.getSelectedItem();		//set to the organism name chosen from the drop down menu
			command[2] = command[2].replace(" ", "_");
			
			if (strain.getText().equals("")){
				command[3] = "null";							//set to strain in text field, set to "null" if empty
			}else {
				command[3] = strain.getText();	
			}
			
			if (geneID.getText().equals("")){
				command[4] = "null";							//set to geneID in text field, set to "null" if empty
			}else {
				command[4] = geneID.getText();
			}
			
			if (thresholdValue.getText().equals(""))	{
				command[5] = "0";					//set to threshold value in text field, set to "0" if empty
			}else {
				command[5] = thresholdValue.getText();
			}
			
			if (thresholdValue.getText().equals(""))	{
				JOptionPane.showMessageDialog(null, "Error! Threshold value not entered!", "Error", JOptionPane.ERROR_MESSAGE);
			} else {
				//check if threshold box only contains numbers and is between 0-100, add threshold value to command if all conditions pass
				if (thresholdValue.getText().matches("[^0-9]")){		
					JOptionPane.showMessageDialog(null, "Error! Threshold value must be numeric.", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (Float.valueOf(thresholdValue.getText()) < 0 || Float.valueOf(thresholdValue.getText()) > 100){
					JOptionPane.showMessageDialog(null, "Error! Threshold value must be between 0 and 100.", "Error", JOptionPane.ERROR_MESSAGE);
				}
				command[5] = thresholdValue.getText();
			}
			
			if (humanFilterY.isSelected()) {						//set to humanY/N depending on choices 
				command[6] = "humanY";
			} else {
				command[6] = "humanN";
			}
			
			if (hmrgdBlastY.isSelected()) {						//set to humanY/N depending on choices 
				command[7] = "hmrgdY";
			} else {
				command[7] = "hmrgdN";
			}
			
			if (localBlastY.isSelected()){							//set to y/n depending on choices
				command[8] = "localY";
			} else {
				command[8] = "localN";
			}
			command[9] = "'" + chosenFile.getText() + "'";			//absolute file path if user is "uploading" a file for multiplex analysis
			command[9] = command[9].replace(" ", "_");				//replace spaces with underscores
			command[9] = command[9].replace("\\", "/");
																	//the arguments for the commands will always appear in the same order
																	//and will be set to null if the an option is not filled in
																	//thus, in the perl @ARGV, the arguments will always appear in the same order
			
			
			//////////////////////////////////////////////////////////////////////////////////////
			// ERROR HANDLING FOR USER INPUT													//
			//////////////////////////////////////////////////////////////////////////////////////
			
			//FOLLOWING TESTS TO MAKE SURE FASTA FILE IS SELECTED FOR PROCESSING
			//if the user has selected a fasta file to upload, make that the fasta file
			if (!chosenFile.getText().equals("")) {
				fastaFileName = chosenFile.getText();
			//if the user has not selected a fasta file to upload, test if values entered are within database
			} else {
				//statements for the two user inputted options to retrieve fasta file from database
				String retrieveFiles1 = "SELECT ORGANISM_GENE.gene_sequence FROM ORGANISM_GENE WHERE ORGANISM_GENE.organism_name = '" + command[2] + "' AND ORGANISM_GENE.strain = '" + command[3] + "'";	
				String retrieveFiles2 = "SELECT ORGANISM_GENE.gene_sequence FROM ORGANISM_GENE WHERE ORGANISM_GENE.gene_id = '" + command[4] + "'";
				
				if (stmt != null){
					//if a strain is entered, use strain AND organism name to find fasta file
					if (!strain.getText().equals("")) {
						try {
							ResultSet records = stmt.executeQuery(retrieveFiles1);
							while (records.next()){						
								//if records found under that organism name/strain combination, use fasta file from that row
								//if more than one record found with that combination, use the last record
								fastaFileName = (records.getString("gene_sequence"));
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
						
					//if a gene ID is entered, use just that value to find fasta file	
					} else if (!geneID.getText().equals("")) {
						try {
							ResultSet records = stmt.executeQuery(retrieveFiles2);
							while (records.next()){							
								//if records found under that gene ID, use fasta file from that row
								fastaFileName = (records.getString("gene_sequence"));
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
			}
			
			//if no fasta file is found from any of those 3 options
			if (fastaFileName.equals("")) {
				//produce an error dialog
				JOptionPane.showMessageDialog(null, "ERROR! No fasta file selected/found.", "Error", JOptionPane.ERROR_MESSAGE);
				//System.out.println("ERROR! No fasta file selected");
			} else {
				System.out.println("SUCCESS! Fasta file selected: " + fastaFileName);
			
				ProcessBuilder runPerlScript = new ProcessBuilder(command);		//commands to be carried out stored in ProcessBuilder class
				try {
					p = runPerlScript.start();						//execute the command
					System.out.println("Perl script executed correctly");	//message to say that the command worked
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				//WAITING DIALOG BOXES TO ALLOW PERL PROGRAM TO RUN
				//waiting dialog box, waits on primer_done.txt to be made when Primer3 is done
				JOptionPane waitPanePrimer = new JOptionPane("Please wait while primers are being produced. \nThis may take up to 10 minutes.");
				JDialog waitDialogPrimer = waitPanePrimer.createDialog("Primer Design");
				while (!f_primer.exists()){
					waitDialogPrimer.setVisible(true);
					//sleep for one second and then check to see if file exists again
					try {
						Thread.sleep(1000);                 //1000 milliseconds is one second.
					} catch(InterruptedException ex) {
					    Thread.currentThread().interrupt();
					}
				}
				waitDialogPrimer.setVisible(false);
				waitPanePrimer.setVisible(false);
				
				//dialog box when primers are complete
				JOptionPane donePanePrimer = new JOptionPane("Your primers have been made!");
				JDialog doneDialogPrimer = donePanePrimer.createDialog("Primer Design");
				doneDialogPrimer.setVisible(true);
				
				
				if (humanFilterY.isSelected()) {
					//waiting dialog box, waits on human_done.txt to be made when NCBI BLAST program is done
					JOptionPane waitPaneHuman = new JOptionPane("Please wait while amplimers are being filtered through a reference human genome. \nThis may take up to 10 minutes per amplimer.");
					JDialog waitDialogHuman = waitPaneHuman.createDialog("Human Filter");
					while (!f_human.exists()){
						waitDialogHuman.setVisible(true);
						//sleep for one second and then check to see if file exists again
						try {
							Thread.sleep(1000);                 //1000 milliseconds is one second.
						} catch(InterruptedException ex) {
						    Thread.currentThread().interrupt();
						}
					}
					waitDialogHuman.setVisible(false);
					waitPaneHuman.setVisible(false);
					
					//when human filter processing is done
					JOptionPane donePaneHuman = new JOptionPane("Your amplimers have been filtered!");
					JDialog doneDialogHuman = donePaneHuman.createDialog("Human Filter");
					doneDialogHuman.setVisible(true);
				}
				
				if (hmrgdBlastY.isSelected()) {
					//waiting dialog box, waits on hmrgd_done.txt to be made when HMRGD BLAST program is done
					JOptionPane waitPaneHMRGD = new JOptionPane("Please wait while amplimers are being aligned against DNA from gut \nmicro-organisms found in the Human Microbiome Project, HMRGD. \nThis may take up to 10 minutes per amplimer.");
					JDialog waitDialogHMRGD = waitPaneHMRGD.createDialog("HMRGD BLAST");
					while (!f_hmrgd.exists()){
						waitDialogHMRGD.setVisible(true);
						//sleep for one second and then check to see if file exists again
						try {
							Thread.sleep(1000);                 //1000 milliseconds is one second.
						} catch(InterruptedException ex) {
						    Thread.currentThread().interrupt();
						}
					}
					waitDialogHMRGD.setVisible(false);
					waitPaneHMRGD.setVisible(false);
					
					//when human filter processing is done
					JOptionPane donePaneHMRGD = new JOptionPane("Your amplimers have been BLAST'd against the HMRGD databank!");
					JDialog doneDialogHMRGD = donePaneHMRGD.createDialog("HMRGD BLAST");
					doneDialogHMRGD.setVisible(true);
				}
				
				if (localBlastY.isSelected()) {
					//waiting dialog box, waits on dimer_done.txt to be made when local BLAST program is done
					JOptionPane waitPaneLocal = new JOptionPane("Please wait while primers are being tested to \ndetermine if primer-dimers will be formed. \nThis may take up to 5 minutes.");
					JDialog waitDialogLocal = waitPaneLocal.createDialog("Primer Dimer");
					while (!f_dimer.exists()){
						waitDialogLocal.setVisible(true);
						//sleep for one second and then check to see if file exists again
						try {
							Thread.sleep(1000);                 //1000 milliseconds is one second.
						} catch(InterruptedException ex) {
						    Thread.currentThread().interrupt();
						}
					}
					waitDialogLocal.setVisible(false);
					waitPaneLocal.setVisible(false);
					
					//when human filter processing is done
					JOptionPane donePaneLocal = new JOptionPane("Your primers have been tested for primer-dimers!");
					JDialog doneDialogLocal = donePaneLocal.createDialog("Primer Dimer");
					doneDialogLocal.setVisible(true);
				}
			}
		}
		
		/////////////////////////////////////
		//File browser for multiplex button//
		/////////////////////////////////////
		if (event.getActionCommand() == "browse"){				//if "browse" button is clicked, open up file choosing box
			int returnVal = chooseFile.showOpenDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION){				//displays file path in chosenFile text field
				File file = chooseFile.getSelectedFile();
				chosenFile.setText(file.getAbsolutePath());
			}
		}
		
		////////////////////////////////////////
		//Table selection for database viewing//
		////////////////////////////////////////
		if (event.getActionCommand() == "tableSelect"){
			String getTable = "SELECT * FROM " + dbTable.getSelectedItem() + ";";
			
			if (stmt != null){
				try {
					ResultSet records = stmt.executeQuery(getTable); //execute getTable query
					
					ResultSetMetaData metaData = (ResultSetMetaData) records.getMetaData(); //get meta data to find column names
					int numColumns = metaData.getColumnCount();	//get number of columns to use in for loop for retrieving column names
					
					// CREATE A TABLE MODEL WITH THE DATABASE DATA WITHIN IT (by: IAndreev93 source: http://www.codeproject.com/Tips/882355/Java-and-MySQL-via-JDBC-How-to-Connect-DB-Get-Data)
					//for changing column and row model
				    DefaultTableModel tm = (DefaultTableModel) dbDisplay.getModel();
				    
				    //clear existing columns 
				    tm.setColumnCount(0);
					tm.setRowCount(0);

				    //add columns to the table model
				    for (int i = 1; i <= numColumns; i++ ) {
				        tm.addColumn(metaData.getColumnName(i));
				    }   

				    // clear existing rows
				    tm.setRowCount(0);

				    // add rows to table
				    while (records.next()) {
				        String[] a = new String[numColumns];
				        for(int i = 0; i < numColumns; i++) {
				            a[i] = records.getString(i+1);
				        }
				    tm.addRow(a);
				    }
				    tm.fireTableDataChanged();

				    // Close ResultSet and Statement
				    records.close();
				    stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}			
			}
		}
		
		
		////////////////////////////////////////
		//File Browser for Results Display Tab//
		////////////////////////////////////////
		if (event.getActionCommand() == "browseResults"){				//if "browse" button is clicked, open up file choosing box
			int returnVal = chooseResultFile.showOpenDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION){				//displays file path in chosenFile text field
				File file = chooseResultFile.getSelectedFile();
				chosenResultFile.setText(file.getAbsolutePath());
			}
		}
		
		//////////////////////////////////////////////
		//Submission button for Results Display Tab //
		//////////////////////////////////////////////
		if (event.getActionCommand() == "submitResultFile"){
			String chosenFile = chosenResultFile.getText();
			
			//error handling for files without .csv extension
			if(!chosenFile.matches(".+(.csv)")){
				JOptionPane.showMessageDialog(null, "File must have .csv extension.", "Error", JOptionPane.ERROR_MESSAGE);
	    	}
			
			//get total number of rows in csv file
	    	BufferedReader bufferedReader = null;
			try {
				bufferedReader = new BufferedReader(new FileReader(chosenFile));
			} catch (FileNotFoundException e3) {
				e3.printStackTrace();
			}
			
			//for changing column and row model
		    DefaultTableModel tm = (DefaultTableModel) resultDisplay.getModel();
			
		    //clear existing columns
		    tm.setColumnCount(0);
		    tm.setRowCount(0);
	        
	        //reading csv file with CSVReader
	        try {
				reader = new CSVReader(new FileReader(chosenFile));
				columnNames = reader.readNext();	// first line = column names
				//add the columns to the table model
				for (int i = 0; i < columnNames.length; i++ ) {
			        tm.addColumn(columnNames[i]);
			    }
				
				String[] rowData;
		    	while ((rowData = reader.readNext()) != null){ //checks if line is empty and adds the row to the table model
		    		tm.addRow(rowData);
		    	}
				reader.close();
			} catch (java.io.IOException e) {
				System.out.print("Error while reading\n");
			}
	        tm.fireTableDataChanged();
		}
		
		//MOST RECENT FILE SUBMIT BUTTON EVENT
		if (event.getActionCommand() == "submitResultFileMostRecent"){ 
			File lastModifiedFile = getLatestFileFromDir("output");
			
			//get total number of rows in csv file
	    	BufferedReader bufferedReader = null;
			try {
				bufferedReader = new BufferedReader(new FileReader(lastModifiedFile));
			} catch (FileNotFoundException e3) {
				e3.printStackTrace();
			}
			
			//for changing column and row model
		    DefaultTableModel tm = (DefaultTableModel) resultDisplay.getModel();
			
		    //clear existing columns
		    tm.setColumnCount(0);
		    tm.setRowCount(0);
	        
	        //reading csv file with CSVReader
	        try {
				reader = new CSVReader(new FileReader(lastModifiedFile));
				columnNames = reader.readNext();	// first line = column names
				//add the columns to the table model
				for (int i = 0; i < columnNames.length; i++ ) {
			        tm.addColumn(columnNames[i]);
			    }
				
				String[] rowData;
		    	while ((rowData = reader.readNext()) != null){ //checks if line is empty and adds the row to the table model
		    		tm.addRow(rowData);
		    	}
				reader.close();
			} catch (java.io.IOException e) {
				System.out.print("Error while reading\n");
			}
	        tm.fireTableDataChanged();
		}
	}
		
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 										MAIN METHOD															//
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static void main (String [] args){
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		//indicate to java to call createAndShowGUI "asynchronously" => "later"
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run(){
				try {
					createAndShowGUI();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	
	
	private static void createAndShowGUI() throws ClassNotFoundException{		//create and set up GUI window
		//JFrame is the outer container for the GUI
		JFrame frame = new JFrame("DyNAmic Primer Generator");		//sets window name
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	//window closes when X button pressed on frame
		
		//create and set up the content pane as
		JComponent newContentPane = new IgpGui();
		//make content pane opaque
		newContentPane.setOpaque(true);
		//set panel inside the frame
		frame.setContentPane(newContentPane);
		
		//display GUI window
		frame.pack();
		frame.setVisible(true);
	}
	
	//METHOD FOR OBTAINING THE LATEST FILE FROM A DIRECTORY (by : Bozho, source: http://stackoverflow.com/questions/2064694/how-do-i-find-the-last-modified-file-in-a-directory-in-java)
	private File getLatestFileFromDir(String dirPath){
	    File dir = new File(dirPath);
	    File[] files = dir.listFiles();
	    if (files == null || files.length == 0) {
	        return null;
	    }

	    File lastModifiedFile = files[0];
	    for (int i = 1; i < files.length; i++) {
	       if (lastModifiedFile.lastModified() < files[i].lastModified()) {
	           lastModifiedFile = files[i];
	       }
	    }
	    
	    //error handling if last modified file in output folder is not a .csv file
	    if(!lastModifiedFile.toString().matches(".+(.csv)")){
			JOptionPane.showMessageDialog(null, "Last modified file is not a .csv file", "Error", JOptionPane.ERROR_MESSAGE);
    	}
	    
	    return lastModifiedFile;
	}

}
