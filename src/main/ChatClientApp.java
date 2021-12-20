package main;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

//import com.apple.eawt.Application;


/**
 * a threaded version of the gui chatclient to 
 * be able to receive input from the server
 *
 */

public class ChatClientApp extends JFrame implements ActionListener, MouseListener, KeyListener, WindowListener, Runnable
{
	
			private static final long serialVersionUID = 1L;
			private Socket socket              = null;
			private DataInputStream  streamIn   = null;
			private DataOutputStream streamOut = null;
			//private ChatClientThread client    = null;
			private JTextArea  display = new JTextArea();
			private JTextArea ids=new JTextArea();
			private JTextArea input   = new JTextArea();
			private JRadioButton    sendToAll    = new JRadioButton("send to all");
			private JRadioButton    sendPrivate    = new JRadioButton("send private");
			private JButton   connect = new JButton("connect");
			private JButton    quit    = new JButton("bye");
			private JButton send=new JButton("send");
			private Border border=BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black, 1, true), BorderFactory.createEmptyBorder(5,5,5,5));
			
			private int port;
			private String host;
	
			private boolean done = true;
			private String line = "";
			//private ChatServer server;
			private JLabel idLabel=new JLabel("IDs to send private (space seperated):");
			private JTextField id=new JTextField();
			private JScrollPane idScrollPane, outputScrollPane;
			private String sendOption="send to all";
	   
	   public ChatClientApp(String host, int port)
	   {
		   setTitle("Chat"); 
		   setImageIcon("icon.png");
		   PrintStream o=null;
			try
			{
				o = new PrintStream(new FileOutputStream("./log.txt",true));
				System.setOut(o); 
			} 
			catch (FileNotFoundException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		   Dimension size=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		   setSize((int) (size.width),(int) (size.height));
		   setName("Chat");
		   setTitle("Chat");
		   setLayout(new BorderLayout());
		   Color primaryColor=new Color(227, 206, 179);
		   Color secondaryColor=new Color(255, 253, 250);
		   setBackground(primaryColor);
		   int marginSize=1;
		   int fontSize=16;
		   int borderSize=1;
		   String fontFamily="sans-serif";
		   Font fontRegular=new Font(fontFamily, Font.PLAIN, fontSize);
		   Font fontLabel=new Font(fontFamily, Font.BOLD, fontSize);
		   Font fontTitle=new Font(fontFamily, Font.BOLD, 2*fontSize);
		   
		   this.port=port;
		   this.host=host;
		   
		   display.setEditable(false);
		   display.addKeyListener(this);
		   display.setFont(fontRegular);
		   display.setMargin(new Insets(marginSize,marginSize,marginSize,marginSize));
		   DefaultCaret caret = (DefaultCaret) display.getCaret(); // 
		   caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); 
		   display.setLineWrap(true);
		   display.setBackground(Color.white);
		   outputScrollPane = new JScrollPane(display); 
		   outputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		   outputScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		   outputScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.gray, 1, true), new EmptyBorder(marginSize,marginSize,marginSize,marginSize)));
		   
		   ids.setEditable(false);
		   ids.addKeyListener(this);
		   ids.setFont(fontRegular);
		   ids.setBackground(Color.white);
		   ids.setMargin(new Insets(marginSize,marginSize,marginSize,marginSize));
		   caret=(DefaultCaret)ids.getCaret();
		   caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		   idScrollPane = new JScrollPane(ids); 
		   idScrollPane.setSize(this.getWidth()/2,this.getHeight()/2);
		   idScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		   idScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		   idScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.gray, 1, true), new EmptyBorder(marginSize,marginSize,marginSize,marginSize)));
		   
		   JPanel panel1=new JPanel(new GridLayout(1,2));
		   panel1.setBackground(secondaryColor);
		   panel1.add(idScrollPane);
		   panel1.add(outputScrollPane);
		   
		   //processPanel.setLayout(new GridLayout(0,2));
		   connect.setBorder(border);
		   connect.setFont(fontLabel);
		   connect.setBackground(primaryColor);
		   connect.setOpaque(true);
		   connect.addActionListener(this);
		   connect.addKeyListener(this);
		   
		   quit.setBorder(border);
		   quit.setFont(fontLabel);
		   quit.setBackground(primaryColor);
		   quit.setOpaque(true);
		   quit.addActionListener(this);
		   quit.setEnabled(false);
		   quit.addKeyListener(this);
		   
		   JPanel panel2a=new JPanel(new FlowLayout(FlowLayout.LEADING));
		   panel2a.add(connect);
		   panel2a.add(quit);
		   
		   sendToAll.setEnabled(false);
		   sendToAll.addActionListener(this);
		   sendToAll.addKeyListener(this);
		   sendToAll.setFont(fontLabel);
		   sendToAll.setBackground(secondaryColor);
		   
		   sendPrivate.setEnabled(false);
		   sendPrivate.addActionListener(this);
		   sendPrivate.addKeyListener(this);
		   sendPrivate.setFont(fontLabel);
		   sendPrivate.setBackground(secondaryColor);
		   
		   ButtonGroup buttonGroup=new ButtonGroup();
		   buttonGroup.add(sendToAll);
		   buttonGroup.add(sendPrivate);
		   
		   JPanel panel2b=new JPanel(new FlowLayout(FlowLayout.LEADING));
		   panel2b.add(sendToAll);
		   panel2b.add(sendPrivate);
		   
		   idLabel.setBackground(secondaryColor);
		   idLabel.setOpaque(true);
		   idLabel.addKeyListener(this);
		   idLabel.setFont(fontLabel);
		   
		   id.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.gray,1,true),new EmptyBorder(5*marginSize,5*marginSize,5*marginSize,5*marginSize)));
		   id.setOpaque(true);
		   id.setEnabled(false);
		   id.setColumns(50);
		   id.addKeyListener(this);
		   
		   JPanel panel2c=new JPanel(new FlowLayout(FlowLayout.LEADING));
		   panel2c.add(idLabel);
		   panel2c.add(id);
		   
		   input.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.gray,1,true),new EmptyBorder(marginSize,marginSize,marginSize,marginSize)));
		   input.setLineWrap(true);
		   input.setEnabled(false);
		   input.addKeyListener(this);
		   input.setRows(3);
		   input.setColumns(50);
		   caret=(DefaultCaret)input.getCaret();
		   caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
//		   inputScrollPane=new JScrollPane(input);
//		   inputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
//		   inputScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		   
		   JPanel panel2d=new JPanel(new FlowLayout(FlowLayout.LEADING));
		   panel2d.add(input);
		   
		   send.setBorder(border);
		   send.setFont(fontLabel);
		   send.setBackground(primaryColor);
		   send.setOpaque(true);
		   send.addActionListener(this);
		   send.setEnabled(false);
		   send.addKeyListener(this);	
		   
		   JPanel panel2e=new JPanel(new FlowLayout(FlowLayout.LEADING));
		   panel2e.add(send);
		   
		   JPanel panel2=new JPanel(new GridLayout(6,1));
		   panel2a.setBackground(secondaryColor);
		   panel2.add(panel2a);
		   panel2b.setBackground(secondaryColor);
		   panel2.add(panel2b);
		   panel2c.setBackground(secondaryColor);
		   panel2.add(panel2c);
		   panel2d.setBackground(secondaryColor);
		   panel2.add(panel2d);
		   panel2e.setBackground(secondaryColor);
		   panel2.add(panel2e);
		   panel2.setBackground(secondaryColor);
		   
		   JPanel panel3=new JPanel();
		   JLabel label=new JLabel();
		   label.setText("Let's Chat");
		   label.setFont(fontTitle);
		   panel3.add(label);
		   
		   add(panel3,BorderLayout.NORTH);
		   
		   panel1.setBorder(new EmptyBorder(borderSize,borderSize,borderSize,borderSize));
		   add(panel1,BorderLayout.CENTER);
		   
		   panel2.setBorder(new EmptyBorder(borderSize,borderSize,borderSize,borderSize));
		   panel2.setSize(this.getWidth()/2,this.getHeight()/2);
		   add(panel2,BorderLayout.SOUTH);
		   
		   addKeyListener(this);
		   addMouseListener(this);
		   addWindowListener(this);
		   addKeyListener(this);
		   
		   setResizable(false);
	   }
	   
	   private void setImageIcon(String url)
	   {		   
		   URL imgURL = getClass().getResource(url);
		   if (imgURL != null) 
		   {
			   //Functions.printMessage("Icon image found");
			   BufferedImage image=null;
			   try
			   {
			      image = ImageIO.read(imgURL);
			      this.setIconImage(image);
			      //Application.getApplication().setDockIconImage(image);
			    } 
			    catch (IOException e)
			    {
			      e.printStackTrace();
			    }
		   } 
		   else
		   {
			   Functions.printMessage("Could not find icon image");
		   }
	   }
	
	public void actionPerformed(ActionEvent e) 
	{
		// TODO Auto-generated method stub
		if(e.getSource()==quit){
			disconnect();
		}
		else if(e.getSource()==connect){
			connect(host, port);
		}
		else if(e.getSource()==send)
		{
			send();
		}
		else if(e.getSource()==sendToAll)
		{
			sendOption="send to all";
		}
		else if(e.getSource()==sendPrivate)
		{
			sendOption="send private";
			
		}

	}
	
	public void connect(String serverName, int serverPort){		
		done=false;
		displayOutput("call to connect was made, waiting to connect to "+serverName+":"+serverPort+"...");
		Functions.printMessage("call to connect was made, waiting to connect to "+serverName+":"+serverPort+"...");
		//create new socket, open stream, disable connect button, enable send and quit button
		try
		{
			socket=new Socket(serverName, serverPort);
			//displayOutput("Socket closed: "+socket.isClosed());
			displayOutput("Connected: "+ socket);
			Functions.printMessage("Connected: "+ socket);
			open();
			id.setEnabled(true);
			input.setEnabled(true);
			sendToAll.setEnabled(true);
			sendPrivate.setEnabled(true);	
			send.setEnabled(true);
			quit.setEnabled(true);
			connect.setEnabled(false);
			sendOption="send to all";
			sendToAll.setSelected(true);
			String msg="hello";
			OneTimePad encryptor=new OneTimePad(msg);
			String msgEncrypted=encryptor.getEncryptedMessage();
			Functions.printMessage(msg+" encrypted as "+msgEncrypted);
			streamOut.writeUTF(msgEncrypted+"~"+encryptor.getCurrentKey());
			streamOut.flush();
		}
		catch(UnknownHostException uhe)
		{
			displayOutput(uhe.getMessage());
			Functions.printMessage(uhe.getMessage());
			done=true;
		}
		catch(IOException ioe)
		{
			displayOutput(ioe.getMessage());
			Functions.printMessage(ioe.getMessage());
			done=true;
		}
		catch(Exception e)
		{
			displayOutput(e.getMessage());
			Functions.printMessage(e.getMessage());
			done=true;
		}
	}

	public void disconnect()
	{
		try
		{
			String msg="bye";
			OneTimePad encryptor=new OneTimePad(msg);
			String msgEncrypted=encryptor.getEncryptedMessage();
			Functions.printMessage(msg+" encrypted as "+msgEncrypted);
			streamOut.writeUTF(msgEncrypted+"~"+encryptor.getCurrentKey());
			streamOut.flush();
		}	
		catch(IOException ioe)
		{
			displayOutput("Sending error "+ioe.getMessage());
			Functions.printMessage(ioe.getMessage());
		}
		catch(NullPointerException e)
		{
			setVisible(false);
		}
		done=true;
		close();
		id.setText("");
		id.setEnabled(false);
		input.setText("");
		input.setEnabled(true);
		display.setText("");
		ids.setText("");
		setTitle("Chat");
		quit.setEnabled(false);
		connect.setEnabled(true);
		sendToAll.setEnabled(false);
		sendPrivate.setEnabled(false);
		send.setEnabled(false);
	}
	
	private void send()
	{
		switch(sendOption)
		{
			case "send private":
				sendPrivate();
				break;
			default:
				sendToAll();
		}
	}
	
	private void sendToAll()
	{
		if(line.equalsIgnoreCase(""))
		{
			displayOutput("Error sending message");
		}
		else
		{
			//send message
			//	displayOutput(input.getText());//testing buttons before testing connection and streams
			//	input.setText("");//testing buttons before testing connection and streams
			String msg = input.getText();
			if(msg.equalsIgnoreCase(""))
			{
				displayOutput("Please enter text");
			}
			else
			{
				OneTimePad encryptor=new OneTimePad(msg);
				String msgEncrypted=encryptor.getEncryptedMessage();
				Functions.printMessage(msg+" encrypted as "+msgEncrypted);
				try
				{
					//displayOutput("You said: "+msg);
					streamOut.writeUTF(msgEncrypted+"~"+encryptor.getCurrentKey());
					streamOut.flush();
					if(msg.equalsIgnoreCase("bye"))
					{
						disconnect();
					}
					input.setText("");
				}
				catch(IOException ioe)
				{
					Functions.printMessage("Sending error "+ioe.getMessage());
					ioe.printStackTrace(System.out);
					disconnect();
				}
			}
		}
	}
	
	private void sendPrivate()
	{
		if(line.equalsIgnoreCase(""))
		{
			displayOutput("Error sending message");
		}
		else
		{
			//send message
			//	displayOutput(input.getText());//testing buttons before testing connection and streams
			//	input.setText("");//testing buttons before testing connection and streams
			String msg = input.getText();
			if(msg.equalsIgnoreCase(""))
			{
				displayOutput("Please enter text");
			}
			else if(id.getText().equalsIgnoreCase(""))
			{
				displayOutput("Message failed to be sent.  Please enter ID of recipient");
			}
			else
			{
				String newMsg="private~"+id.getText()+"~"+msg;
				OneTimePad encryptor=new OneTimePad(newMsg);
				String msgEncrypted=encryptor.getEncryptedMessage();
				Functions.printMessage(newMsg+" encrypted as "+msgEncrypted);
				try
				{
					try
					{
						streamOut.writeUTF(msgEncrypted+"~"+encryptor.getCurrentKey());
						streamOut.flush();				
						if(msg.equalsIgnoreCase("bye"))
						{
							disconnect();
						}
						input.setText("");
					}
					catch(NumberFormatException e)
					{
						displayOutput("Message failed to be sent.  Please enter ID of recipient");
					}
				//	privateInput.setText("");
				}
				catch(IOException ioe)
				{
					//displayOutput("Sending error "+ioe.getMessage());
					disconnect();
				}
			}
		}
	}
	
	/*private void sendPrivateEncrypted()
	{
		//send message
		//	displayOutput(input.getText());//testing buttons before testing connection and streams
		//	input.setText("");//testing buttons before testing connection and streams
		if(line.equalsIgnoreCase(""))
		{
			displayOutput("Error sending message");
		}
		else
		{
			String msg = input.getText();
			if(msg.equalsIgnoreCase(""))
			{
				displayOutput("Please enter text");
			}
			else if(id.getText().equalsIgnoreCase(""))
			{
				displayOutput("Message failed to be sent.  Please enter ID of recipient");
			}
			else
			{				
				OneTimePad encryptor=new OneTimePad(msg);
				String msgEncrypted=encryptor.getEncryptedMessage();
				try
				{
					try
					{
						streamOut.writeUTF("privateEncrypted~"+id.getText()+"~"+msgEncrypted+"~"+encryptor.getCurrentKey());
						streamOut.flush();				
						if(msg.equalsIgnoreCase("bye"))
						{
							disconnect();
						}
						input.setText("");
					}
					catch(NumberFormatException e)
					{
						displayOutput("Message failed to be sent.  Please enter ID of recipient");
					}
					//privateInputEncrypted.setText("");
				}
				catch(IOException ioe)
				{
					//displayOutput("Sending error "+ioe.getMessage());
					disconnect();
				}
			}
		}
	}*/
	
	public void open(){
		try
		{
			streamOut = new DataOutputStream(socket.getOutputStream());
			streamIn =  new DataInputStream(socket.getInputStream());
		    new Thread(this).start();//background thread to handle the input from the server...need to uncomment
		}
		catch(IOException ioe)
		{
			displayOutput("Read/Write error: "+ioe.getMessage());
		}
	}
	
	public void close(){
		done=true;
		try{
			if(streamOut !=null){
			streamOut.close();
			}
			if(streamIn !=null){
				streamIn.close();
			}
			if(socket !=null){
				socket.close();
			}		
		}
		catch(IOException ioe){
			displayOutput("Error closing: "+ioe.getMessage());
			//client.close();
			//client = null;
		}
	}
	
	public void displayOutput(String msg)
	{
		display.append(msg +"\n");
	}
	
	public void handle(String msg){
		if(msg.equals("bye")){
			disconnect();
		}
		else{
			displayOutput(msg);
		}
	}
	
	
	public void run() 
	{
		// TODO Auto-generated method stub
		try
		{
			while(!done)
			{
				if(streamIn!=null)
				{
					if(!(socket.isClosed()))
					{
						line = streamIn.readUTF();
						Functions.printMessage("line:"+line);
						
						String key="";
						String prefix="";
						String oldLine="";
						
						StringTokenizer tokenizer=new StringTokenizer(line,"~");
						if(tokenizer.countTokens()>=3)
						{
							prefix=tokenizer.nextToken();
							line=prefix+"~"+tokenizer.nextToken();
							key=tokenizer.nextToken();
						}
						else
						{
							line=tokenizer.nextToken();
							key=tokenizer.nextToken();
						}
		
						oldLine=line;
						line=OneTimePad.decryptMessage(line, key);
						
						Functions.printMessage(oldLine+" decrypted as "+line);
						
						tokenizer=new StringTokenizer(line,"~");
						if(tokenizer.countTokens()>1)
						{
							if(tokenizer.nextToken().equalsIgnoreCase("ids"))
							{
								line=tokenizer.nextToken();
								displayIDs(line);
								displayOutput(line);
							}
							else
							{
								tokenizer=new StringTokenizer(line,"~");
								if(tokenizer.nextToken().equalsIgnoreCase("id"))
								{
									line=tokenizer.nextToken();
									setTitle(line);
								}
							}
						}
						else
						{
							displayOutput(line);
							continue;
						}
					}
					else
					{
						Functions.printMessage("Socket closed");
					}
				}
				else
				{
					Functions.printMessage("Stream is null");
				}
			}
		}
		catch(IOException ioe)
		{
			done=true;
			Functions.printMessage("Read error occurred: "+ioe.getMessage());
			ioe.printStackTrace(System.out);
			disconnect();
		}		
	}
	
	private void displayIDs(String line)
	{
		ids.setText(line);
	}


	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
//		if(e.getKeyCode()==KeyEvent.VK_Q);
//		{
//			disconnect();
//		}
//		if(e.getKeyCode()==KeyEvent.VK_C)
//		{
//			if(done==true)
//			{
//				connect(host, port);
//			}
//		}
	}


	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}


	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}


	public void windowActivated(WindowEvent arg0) 
	{
		// TODO Auto-generated method stub
	}

	
	public void windowClosed(WindowEvent event) 
	{
		// TODO Auto-generated method stub
		disconnect();
		System.exit(0);
	}


	public void windowClosing(WindowEvent event) {
		// TODO Auto-generated method stub
		disconnect();
		System.exit(0);
	}

	
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
	}

	
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	static void printTime()
	{
	   DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
	   LocalDateTime now = LocalDateTime.now();  
	   System.out.print("@ "+dtf.format(now)+": ");  
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
