package main;

public class App {

	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		ChatClientApp app=new ChatClientApp("ec2-3-92-8-144.compute-1.amazonaws.com",80);
		//ChatClientApp app=new ChatClientApp("localhost",80);
		app.setVisible(true);
	}

}
