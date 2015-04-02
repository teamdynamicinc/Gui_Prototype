import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class IgpGui extends JPanel implements ActionListener {
	
	JComboBox<String> organism;		//drop down box containing organism names
	JTextField strain;				//text field for user to enter a strain
	JTextField geneID;				//text field for user to enter a Gene ID
	JTextField thresholdValue;		//single line box for user to enter their preferred threshold value
	JRadioButton humanFilterY;		//human genome filter radio buttons (yes/no)
	JRadioButton humanFilterN;
	JRadioButton localBlastY;		//local blast radio buttons (yes/no)
	JRadioButton localBlastN;
	JButton submit;					//submission button
	JFileChooser chooseFile;		//file chooser for multiplex
	JButton chooseButton;
	JTextField chosenFile;			//displays path of chosen file
	
	private static final long serialVersionUID = -420254383943138649L;
	
	public IgpGui() throws ClassNotFoundException {			//default constructor for the GUI
		JPanel windowContents = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		setPreferredSize(new Dimension(800, 600));	
		
		///////////////////////////
		//ORGANISM DROP DOWN MENU//
		///////////////////////////
		//set up position of the organism drop down menu and its label
		c.gridx = 0;
		c.gridy = 0;
		c.anchor=GridBagConstraints.WEST;
		windowContents.add(new Label("Select an Organism:  "), c);	//organism drop down menu label (0,0)
		organism = new JComboBox<String>();
		c.gridx = 1;
		c.gridy = 0;
		
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
		windowContents.add(organism, c);
		
		/////////////////////
		//Strain Text Field//
		/////////////////////
		c.gridx = 2;
		c.gridy = 0;
		windowContents.add(new Label("and Strain:"), c); //strain text field label (2, 0)
		c.gridx = 3;
		c.gridy = 0;
		strain = new JTextField(" ", 8);
		windowContents.add(strain, c);	//strain text field (3,0)
		
		/////////////////////
		//GeneID Text Field//
		/////////////////////
		c.gridx = 2;
		c.gridy = 1;
		windowContents.add(new Label("or Gene ID:"), c); //gene id text field label (2,1)
		c.gridx = 3;
		c.gridy = 1;
		geneID = new JTextField(" ", 8);
		windowContents.add(geneID, c);	//geneID text field (2,2)
		
		////////////////////////
		// THRESHOLD TEXTFIELD//
		////////////////////////
		c.gridx = 0;
		c.gridy = 2;
		windowContents.add(new Label("Enter a Threshold Value (%)"), c);	//threshold value label (0,2)
		c.gridx = 1;
		c.gridy = 2;
		thresholdValue = new JTextField("90", 8);
		windowContents.add(thresholdValue, c);			//threshold value text field (1,2)
		
		//////////////////////////////
		//HUMAN FILTER RADIO BUTTONS//    //CHANGE TO CHECKBOX
		//////////////////////////////
		humanFilterY = new JRadioButton("YES");
		humanFilterN = new JRadioButton("NO");
		ButtonGroup humanFilter = new ButtonGroup();//create button group for human filter
		humanFilter.add(humanFilterY);
		humanFilter.add(humanFilterN);
		c.gridx = 0;
		c.gridy = 3;
		windowContents.add(new Label ("Filter with human genome?"), c); //Human filter radio buttons label (0,3)
		c.gridx = 1;
		windowContents.add(humanFilterY, c);	//human filter yes button (1,3)
		c.gridx = 2;
		windowContents.add(humanFilterN, c);	//human filter no button (2, 3)
		
		
		//RUN LOCAL BLAST  // CHECK FOR PRIMER DIMERS CHECKBOX
		localBlastY = new JRadioButton("YES");
		localBlastN = new JRadioButton("NO");
		ButtonGroup localBlast = new ButtonGroup();//create button group for human filter
		localBlast.add(localBlastY);
		localBlast.add(localBlastN);
		c.gridx = 0;
		c.gridy = 4;
		windowContents.add(new Label ("Run local BLAST against primers?"), c); //Human filter radio buttons label (0,3)
		c.gridx = 1;
		windowContents.add(localBlastY, c);	//human filter yes button (1,3)
		c.gridx = 2;
		windowContents.add(localBlastN, c);	//human filter no button (2, 3)
		
		
		//////////////////////////
		//MULTIPLEX FILE CHOOSER//
		//////////////////////////
		c.gridx = 0;
		c.gridy	= 5;
		windowContents.add(new Label("Upload a file for multiplex"), c);		//file chooser label (0, 5)
		c.gridy = 6;
		c.fill = GridBagConstraints.BOTH;
		chooseFile = new JFileChooser();
		chooseFile.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);	//set file chooser to look at files and directories
		chooseButton = new JButton("Browse");
		chooseButton.addActionListener(this);
		chooseButton.setActionCommand("browse");
		windowContents.add(chooseButton, c);									//file chooser button (0,6)
		c.gridx = 1;
		chosenFile = new JTextField(30);
		windowContents.add(chosenFile, c);										//chosen file text field (1, 6)
		
		
		//////////////////
		// SUBMIT BUTTON//
		//////////////////
		c.gridx = 0;
		c.gridy = 7;
		c.fill = GridBagConstraints.BOTH;
		c.anchor=GridBagConstraints.CENTER;
		submit = new JButton("Submit");
		windowContents.add(submit, c);
		submit.addActionListener(this);
		submit.setActionCommand("submit");
		
		//add controls area layout to MYGUI JPanel
		this.add(windowContents, BorderLayout.SOUTH);
	}
	

		
	
	////////////////////////////////////////////////////////////////////////////////////////
	// ACTION LISTENER CARRYS OUT ACTIONS WHEN A BUTTON IS PRESSED						//
	////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand() == "submit"){					//when submit button is pressed, an array of strings is generated based on the items selected
			Process p = null;
			String[] command = new String[9];						
			command[0] = "perl";									//always set to perl as perl is run
			command[1] = "test.pl";									//set to the perl script name and/or path
			command[2] = (String) organism.getSelectedItem();		//set to the organism name chosen from the drop down menu
			if (strain.getText() != null){
				command[3] = strain.getText();							//set to strain in text field, set to "null" if empty
			}else {
				command[3] = "null";
			}
			if (geneID.getText() != null){
				command[4] = geneID.getText();							//set to geneID in text field, set to "null" if empty
			}else {
				command[4] = "null";
			}
			if (thresholdValue.getText() != null)	{
				command[5] = thresholdValue.getText();					//set to threshold value in text field, set to "0" if empty
			}else {
				command[5] = "0";
			}
			if (humanFilterY.isSelected()) {						//set to humanY/N depending on choices 
				command[6] = "humanY";
			} else {
				command[6] = "humanN";
			}
			if (localBlastY.isSelected()){							//set to y/n depending on choices
				command[7] = "localY";
			} else {
				command[7] = "localN";
			}
			command[8] = "'" + chosenFile.getText() + "'";						//absolute file path if user is "uploading" a file for multiplex analysis
			
																	//the arguments for the commands will always appear in the same order
																	//and will be set to null if the an option is not filled in
																	//thus, in the perl @ARGV, the arguments will always appear in the same order
			
			
			ProcessBuilder runPerlScript = new ProcessBuilder(command);		//commands to be carried out stored in ProcessBuilder class
			try {
				p = runPerlScript.start();						//execute the command
				System.out.println("Perl script executed correctly");	//message to say that the command worked
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		if (event.getActionCommand() == "browse"){				//if "browse" button is clicked, open up file choosing box
			int returnVal = chooseFile.showOpenDialog(IgpGui.this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION){				//displays file path in chosenFile text field
				File file = chooseFile.getSelectedFile();
				chosenFile.setText(file.getAbsolutePath());
			}
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

}
