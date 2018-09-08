

import java.awt.*; 
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Random;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class Whiteboard extends JFrame		//subclass of JFrame
{
	public static String sendingLine, receivedLine;
	
	final protected static int SERVER_MODE = 1;
	final protected static int CLIENT_MODE = 2;
	final protected static int SIMPLE_MODE = 0;
	
	protected static int mode;
	static JColorChooser tcc;
	
	static JTextField textField;
	static Canvas canvas;
	
	protected static String[] fonts;
	protected static HashMap<String, Integer> fontMap;
	
	private static WhiteboardTableModel tableModel;
	private static JTable table;
	
	private static JLabel addLable, modeLable;
	
	public Color defaultColor = Color.GRAY;
	
	private static Container shapeBox,colorBox,textBox,optionsBox, fileBox, networkBox, mainBox;
	
	private static JButton OpenButton = new JButton("Open");
	private static JButton SaveButton = new JButton("Save");
	private static JButton RectButton = new JButton("Rect");
	private static JButton OvalButton = new JButton("Oval");
	private static JButton LineButton = new JButton("Line");
	private static JButton TextButton = new JButton("Text");
	private static JButton SetColorButton = new JButton("Set Color");
	private static JButton MoveToFrontButton = new JButton("Move To Front");
	private static JButton MoveToBackButton = new JButton("Move To Back");
	private static JButton RemoveShapeButton = new JButton("Remove Shape");
	private static JButton StartClientButton = new JButton("Start Client");
	private static JButton StartServerButton = new JButton("Start Server");
	
	private static JComboBox fontsComboBox;
	
	public static Whiteboard whiteBoard;

	/**
	 * @param text
	 */
	public Whiteboard( String text)
	{
		GraphicsEnvironment graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
        fonts = graphics.getAvailableFontFamilyNames();			//instantiate fonts
        mode = SIMPLE_MODE;										//default mode
        
        TextButton.addActionListener(new TextButton());			//instantiate all button action listeners and change colors
		RectButton.addActionListener(new RectButton());
		OvalButton.addActionListener(new OvalButton());
		LineButton.addActionListener(new LineButton());
		SetColorButton.addActionListener(new SetColorButton());
		SetColorButton.setForeground(Color.GRAY);
		MoveToFrontButton.addActionListener(new MoveToFront());
		MoveToFrontButton.setForeground(Color.PINK);
		MoveToBackButton.addActionListener(new MoveToBack());
		MoveToBackButton.setForeground(Color.PINK);
		RemoveShapeButton.addActionListener(new RemoveShapeButton());
		RemoveShapeButton.setForeground(Color.RED);
		OpenButton.addActionListener(new OpenButton());
		OpenButton.setForeground(Color.BLUE);
	    SaveButton.addActionListener(new SaveButton());
	    SaveButton.setForeground(Color.BLUE);
		StartClientButton.addActionListener(new StartClientButton());
		StartClientButton.setForeground(Color.MAGENTA);
		StartServerButton.addActionListener(new StartServerButton());
		StartServerButton.setForeground(Color.MAGENTA);
	}

	public static void main(String[] args) throws Exception			// main runnable method
	{
		KeyListener delete = new KeyListener()
        {
        	
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (canvas.hasSelected())
	        	{
	        		switch(e.getKeyCode()){
	        		case KeyEvent.VK_DELETE:
	        			whiteBoard.RemoveShape();
	        		}
	        	}
				
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
        	
        };
		whiteBoard = new Whiteboard("WhiteBoard");
		whiteBoard.setResizable(true);
		whiteBoard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		whiteBoard.addKeyListener(delete);
		canvas = new Canvas( whiteBoard);  
		canvas.setLayout(new GridBagLayout());
		canvas.setMinimumSize(new Dimension(1000, 1000));
		canvas.setPreferredSize(new Dimension(400, 400));		// 400 requirement
		canvas.setBackground(Color.WHITE);						// white canvas
		canvas.setOpaque(true);
		
		shapeBox = new Container();
		colorBox = new Container();
		textBox = new Container();
		optionsBox = new Container();
		fileBox = new Container();
		networkBox = new Container();

		addLable = new JLabel("Add");
		modeLable = new JLabel("Simple mode");
		
		textField = new JTextField(20);
		textField.setMaximumSize(new Dimension(200, 20));
        textField.setPreferredSize(new Dimension(200, 20));
        textField.getDocument().addDocumentListener(new DocumentListener() 
        {
            public void changedUpdate(DocumentEvent e) {}
    
            public void insertUpdate(DocumentEvent e) 
            {
                handleTextChange(e);
            }
    
            public void removeUpdate(DocumentEvent e) 
            {
                handleTextChange(e);
            }
        });
        
        fontMap = new HashMap<String, Integer>();
        for(int i = 0; i < fonts.length; i++) {
            fontMap.put(fonts[i], i);
        }
		
        fontsComboBox = new JComboBox(fonts);
        fontsComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(canvas.hasSelected() && canvas.selected instanceof DText)
                    canvas.setFontForSelected((String) fontsComboBox.getSelectedItem());
            }
        });

        //Here we add all buttons in a specific order from top to bottom
        //Each container holds a different line of buttons
        
		mainBox = new Container();
		mainBox.setLayout(new BoxLayout(mainBox, BoxLayout.PAGE_AXIS));

		shapeBox.add(addLable);
        shapeBox.add(RectButton);
        shapeBox.add(OvalButton);
        shapeBox.add(LineButton);
		shapeBox.add(TextButton);
		shapeBox.setLayout(new BoxLayout(shapeBox, BoxLayout.X_AXIS));

		colorBox.add(SetColorButton);
		colorBox.setLayout(new BoxLayout(colorBox, BoxLayout.X_AXIS));

		textBox.add(textField);
        textBox.add(fontsComboBox);
		textBox.setLayout(new BoxLayout(textBox, BoxLayout.X_AXIS));
        
        optionsBox.add(MoveToFrontButton);
		optionsBox.add(MoveToBackButton);
		optionsBox.add(RemoveShapeButton);
		optionsBox.setLayout(new BoxLayout(optionsBox, BoxLayout.X_AXIS));
		
		fileBox.add(OpenButton);
	    fileBox.add(SaveButton);
		fileBox.setLayout(new BoxLayout(fileBox, BoxLayout.X_AXIS));
		
		networkBox.add(StartClientButton);
	    networkBox.add(StartServerButton);
	    networkBox.add(modeLable);
		networkBox.setLayout(new BoxLayout(networkBox, BoxLayout.X_AXIS));
		
        mainBox.add(shapeBox);
        mainBox.add(colorBox);
        mainBox.add(textBox);
        mainBox.add(optionsBox);
        mainBox.add(fileBox);
        mainBox.add(networkBox);
        setUpTable();

        whiteBoard.getContentPane().add(mainBox, BorderLayout.WEST);
        whiteBoard.getContentPane().add(canvas, BorderLayout.CENTER);

		whiteBoard.pack();

		whiteBoard.setVisible(true);
	}
	

	class RectButton implements ActionListener
	{
		public void actionPerformed(ActionEvent e) 
		{
			Rectangle rect = new Rectangle(20,20,30,30);	//default location and size of a square
			DRectModel shapeModel = new DRectModel();
			shapeModel.setColor(defaultColor);				
			setCoordinates(shapeModel, rect);
			canvas.addShape(shapeModel);
			canvas.paintComponent(canvas.getGraphics());	
			if(mode == SERVER_MODE)
			{
				String toSend = "A_" + shapeModel.ConvertToString();
				serverSends(toSend);
			}
			
		}
	}
	
	class OvalButton implements ActionListener
	{
		public void actionPerformed(ActionEvent e) 
		{
			Rectangle rect = new Rectangle(20,20,30,30);		//default size and location
			DOvalModel shapeModel = new DOvalModel();			//specify that it is an oval and not square
			shapeModel.setColor(defaultColor);
			setCoordinates(shapeModel, rect);
			canvas.addShape(shapeModel);
			canvas.paintComponent(canvas.getGraphics());
			if(mode == SERVER_MODE)
			{
				String toSend = "A_" + shapeModel.ConvertToString();
				serverSends(toSend);
			}
		}
	}
	
	class LineButton implements ActionListener
	{
		public void actionPerformed(ActionEvent e) 
		{
			Rectangle rect = new Rectangle(20,20,40,40);		// default size is different because line is different
			DLineModel shapeModel = new DLineModel(0,0,0,0);
			shapeModel.setColor(defaultColor);
			setCoordinates(shapeModel, rect);
			canvas.addShape(shapeModel);
			canvas.paintComponent(canvas.getGraphics());
			if(mode == SERVER_MODE)
			{
				String toSend = "A_" + shapeModel.ConvertToString();
				serverSends(toSend);
			}
		}
	}
	
	class TextButton implements ActionListener
	{
		public void actionPerformed(ActionEvent e) 
		{
			Rectangle rect = new Rectangle(20,20,20,20);
			DTextModel shapeModel = new DTextModel();
			shapeModel.setColor(defaultColor);
			
			//added functionality
			if(!textField.getText().equals(""))		// if text exists in the text field, update current text to be that
				shapeModel.setText(textField.getText());
			else									//else just write text by default
				shapeModel.setText("Hello");
			
				shapeModel.setFontName(fontsComboBox.getFont().getName());
				setCoordinates(shapeModel, rect);
				canvas.addShape(shapeModel);
				canvas.paintComponent(canvas.getGraphics());
			if(mode == SERVER_MODE)
			{
				String toSend = "A_" + shapeModel.ConvertToString();
				serverSends(toSend);
			}
		}
	}
	
	class SetColorButton implements ActionListener				//functionality was added for artistic purposes
	{															//color of setColor button changes based on what color is default
		public void actionPerformed(ActionEvent e) 				//you can change default color if no color is selected
		{
			if(canvas.hasSelected())		//if the object is selected, change their color
			{
				Color newColor = JColorChooser.showDialog(whiteBoard, "Select Current Shape Color", Color.GRAY); //Component, message, default color
				if (newColor != canvas.selected.model.getColor())
				{
					canvas.selected.model.setColor(newColor);	//set color of text
					SetColorButton.setForeground(newColor);		//set color of button
					defaultColor = newColor;					//change default color
				}
				else		// if no color was selected
				{
					canvas.selected.model.setColor(canvas.selected.model.getColor());
					SetColorButton.setForeground(canvas.selected.model.getColor());
					defaultColor = canvas.selected.model.getColor();
				}
			}
			else			//if no object is selected
			{
				defaultColor = JColorChooser.showDialog(whiteBoard, "Select Default Color", Color.GRAY);
				SetColorButton.setForeground(defaultColor);
			}
	    }
	
	}
	
	class MoveToFront implements ActionListener
	{
		public void actionPerformed(ActionEvent e) 
		{
			if (mode == SERVER_MODE)
			{
				String toSend = "F";
				serverSends(toSend);
			}
			if(canvas.hasSelected())
			{
				canvas.listShapes.remove(canvas.selected);		//remove current shape
				canvas.listShapes.add(canvas.selected);			//add same object in the same position, in front
				
				canvas.paintComponent(canvas.getGraphics());	//confirm and paint
				
				whiteBoard.didMoveToFront(canvas.selected);
			}
		}
	}
	
	class MoveToBack implements ActionListener
	{
		public void actionPerformed(ActionEvent e) 
		{
			if (mode == SERVER_MODE)
			{
				String toSend = "B";
				serverSends(toSend);
			}
			if(canvas.hasSelected())
			{
				canvas.listShapes.remove(canvas.selected);		//remove current object
				canvas.listShapes.add(0, canvas.selected);		//add object back, in the furthest back position
				
				canvas.paintComponent(canvas.getGraphics());
				whiteBoard.didMoveToBack(canvas.selected);
			}
		}
	}
	
	class RemoveShapeButton implements ActionListener
	{
		public void actionPerformed(ActionEvent e) 
		{
			if(canvas.hasSelected())
			{
				if (mode == SERVER_MODE)
				{
					String toSend = "R";
					serverSends(toSend);
				}
				RemoveShape();
			}
		}
	}
	
    class SaveButton implements ActionListener
    {
		public void actionPerformed(ActionEvent e) 
		{
			JFileChooser c = new JFileChooser();
			
			int rVal = c.showSaveDialog(Whiteboard.this);
			if (rVal == JFileChooser.APPROVE_OPTION) 
			{
				  canvas.WriteToFile(c.getSelectedFile().getName(), c.getCurrentDirectory().toString());          
			}
		}
    }
  
	class OpenButton implements ActionListener 
	{
		public void actionPerformed(ActionEvent e) 
		{
			JFileChooser c = new JFileChooser();
			// Demonstrate "Open" dialog:
			int rVal = c.showOpenDialog(Whiteboard.this);		//creates open file dialog
			if (rVal == JFileChooser.APPROVE_OPTION) 			//if the file approves open, it is good
				
			{
			canvas.listShapes.clear();
			canvas.ReadFromFile(c.getSelectedFile().getName(), c.getCurrentDirectory().toString());    //opens file        
			}
		}
	}
	
	public void RemoveShape()
	{
		whiteBoard.didRemove(canvas.selected);
		canvas.listShapes.remove(canvas.selected);
		canvas.selected = null;
		canvas.paintComponent(canvas.getGraphics());
	}
	
	
	public static void setCoordinates(DShapeModel model, Rectangle rect)
	{
		model.setX((int)rect.getX());
		model.setY((int)rect.getY());
		model.setWidth((int)rect.getWidth());
		model.setHeight((int)rect.getHeight());
	}

	public static void handleTextChange(DocumentEvent e)
	{
		if(!canvas.hasSelected())
			return;
		if(canvas.selected instanceof DText)
		{
			((DTextModel)canvas.selected.model).setText(textField.getText());
		}
	}

	public void updateFontGroup(String text, String font) 
	{
        int index = fontMap.get(font);
        fontsComboBox.setSelectedIndex(index);
        textField.setText(text);
    }
	
	
	private static void setUpTable( ) 
	{
        tableModel = new WhiteboardTableModel();
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollpane = new JScrollPane(table);
        scrollpane.setPreferredSize(new Dimension(300, 200));
        mainBox.add(scrollpane, BorderLayout.WEST);
    }
	
	public void addToTable(DShape shape) 
	{
        tableModel.addModel(shape.getModel());
        updateTableSelection(shape);
    }
    
    public void didMoveToBack(DShape shape) 
    {
        tableModel.moveModelToBack(shape.getModel());
        updateTableSelection(shape);
    }
    
    public void didMoveToFront(DShape shape) 
    {
        tableModel.moveModelToFront(shape.getModel());
        updateTableSelection(shape);
    }
    
    public void didRemove(DShape shape) 
    {
        tableModel.removeModel(shape.getModel());
        updateTableSelection(null);
    }
    
    public void clearTable() 
    {
        updateTableSelection(null);
        tableModel.clear();
    }
    
    public void updateTableSelection(DShape selected) 
    {
        table.clearSelection();
        if(selected != null) {
            int index = tableModel.getRowForModel(selected.getModel());
            table.setRowSelectionInterval(index, index);
        }
    }
    
      
    class StartServerButton implements ActionListener 
    {
    	public void actionPerformed(ActionEvent e) 
    	{
    		JFrame frame = new JFrame("Server");
    		String input = JOptionPane.showInputDialog(frame, "Choose a port between 1023 and 65535", 3000);
    		try{
    		if(input.equals("") || input.equals(null) || Integer.parseInt(input) <= 1023)
    			return; 
    		}
    		catch(Exception ex)
    		{
    			return;
    		}
    		int portNumber = Integer.parseInt(input);
    		modeLable.setText("Server mode");
    		modeLable.setForeground(Color.RED);
    		mode = SERVER_MODE;
    		serverSends("Hello world");

    		(new Thread() {
    			public void run() {
    				ServerSocket ss;
    				try {
    					ss = new ServerSocket(portNumber);
    					Socket s = ss.accept();
              
    					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
    					while (true) 
    					{
    						if(sendingLine!=null)
    						{
    							out.write(sendingLine);
    							out.newLine();
    							out.flush();
    							sendingLine = null;
    						}
    						Thread.sleep(50);
    					}
    					
    				} catch (UnknownHostException e) {
    					e.printStackTrace();
    				} catch (IOException e) {
    					e.printStackTrace();
    				} catch (InterruptedException e) {
    					e.printStackTrace();
    				}
    			}
    		}).start();
    	}
    }

    class StartClientButton implements ActionListener 
    {
    	public void actionPerformed(ActionEvent e) 
    	{
    		JFrame frame = new JFrame("Client");

    		String input = JOptionPane.showInputDialog(frame, "Type the Server Number", 3000);
    		try{
        		if(input.equals("") || input.equals(null) || Integer.parseInt(input) <= 1023)
        			return; 
        		}
        		catch(Exception ex)
        		{
        			return;
        		}
    		int portNumber = Integer.parseInt(input);
    		modeLable.setText("Client mode");
  
    		mode = CLIENT_MODE;
    		mainBox.hide();

    		whiteBoard.pack();
  
    		(new Thread() {
    			public void run() 
    			{
    				try {
    					Socket s = new Socket("localhost", portNumber);
                      
    					BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
    					String line = null;
    					while ((line = in.readLine()) != null) 
    					{
    						try
    						{
    						clientReacts(line);
    						}
    						catch(Exception e)
    						{
    							System.out.println("Error: Client could not react");
    						}
    					}
    					s.close();
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    			}
    		}).start();
    	}
    }
  
    public static void serverSends(String line)
    {
    	sendingLine = line + "\n";
    }
      
    public void clientReacts(String line)
    {
    	System.out.println("Client is reacting " + line);
    	if(line.length() == 0)
    		return;
    	if (line.charAt(0)=='A')
    	{
    		try
    		{
    		canvas.addShapeFromString(line.substring(2));
    		canvas.paintComponent(canvas.getGraphics());
    		}
    		catch(Exception e)
    		{
    			System.out.println("Error: Cannot insert object");
    		}
    	}
    	if (line.charAt(0)=='R')
    	{
    		if(canvas.hasSelected())
    		{
    			whiteBoard.didRemove(canvas.selected);
    			canvas.listShapes.remove(canvas.selected);
    			canvas.selected = null;
    			canvas.paintComponent(canvas.getGraphics());
    		}
    	}
    	if (line.charAt(0)=='S')
    	{
    		int toSelect = Integer.parseInt(line.substring(2));
    		try{
    		canvas.changeSelection(canvas.listShapes.get(toSelect));
    		}
    		catch(Exception e)
    		{
    			System.out.println("Error");
    		}
    	}
  
    	if (line.charAt(0)=='F')
    	{
    		if(canvas.hasSelected())
    		{
    			canvas.listShapes.remove(canvas.selected);
    			canvas.listShapes.add(canvas.selected);
    			canvas.paintComponent(canvas.getGraphics());
    			whiteBoard.didMoveToFront(canvas.selected);
    		}
    	}
    	if (line.charAt(0)=='B')
    	{
    		if(canvas.hasSelected())
    		{
    			canvas.listShapes.remove(canvas.selected);
    			canvas.listShapes.add(0, canvas.selected);
    			canvas.paintComponent(canvas.getGraphics());
    			whiteBoard.didMoveToBack(canvas.selected);
    		}
    	}
    	if (line.charAt(0)=='C')
    	{
    		canvas.inputChanges(line.substring(2));
    	}
    }
}