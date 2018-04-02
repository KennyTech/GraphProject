//package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.util.*;
import javafx.scene.chart.XYChart.Series;
import javafx.collections.ObservableList;

class ConnectionHandler extends Thread{
	public Socket clientSocket;
	public PrintWriter out;
	
	public ConnectionHandler(Socket connect){
		clientSocket = connect;
		try{
			out = new PrintWriter(clientSocket.getOutputStream());
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void run(){		
        int i = 0;
		 
		try{
			
			String line = Integer.toString(Main.connectedClients.size());
			out.println(line);
			System.out.println("Line Sent");
			out.flush(); 			
			
          InputStream cin = clientSocket.getInputStream();
          InputStreamReader creader = new InputStreamReader(cin);
          BufferedReader cbin = new BufferedReader(creader);
		  while(true){
          String cline = null;
				//Client will only send commands. Send to other clients once received		  
				if ((cline = cbin.readLine()) != null) {
					System.out.println(cline);
					Main.sendMessage(clientSocket, cline);
				}
			
		  }
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
public class Main{
    private static ServerSocket serverSocket;
    private static Socket clientSocket;
	
	public static ArrayList<Socket> connectedClients = new ArrayList<>();
	public static ObservableList<Series<Float, Float>> seriesList;
	
	private static ArrayList<ConnectionHandler> clientHandlers = new ArrayList<>();
    public static void main(String [] args) {
        try {
            serverSocket = new ServerSocket(8888);

            System.out.println("Ready");

            //Constantly accept client connections
            while(true){

                clientSocket = serverSocket.accept();
                if(clientSocket != null){
					//Add client to the list once connected
					ConnectionHandler ch = new ConnectionHandler(clientSocket);	
					clientHandlers.add(ch);					
					ch.start();
					
					connectedClients.add(clientSocket);
					System.out.println(connectedClients.size());
                }
				clientSocket = null;
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
	
	public static void sendMessage(Socket sender, String msg){
		for(ConnectionHandler c: clientHandlers){
			//Don't send command back to sender
			if(c.clientSocket != sender){
					c.out.println(msg);
					c.out.flush();

			}
		}
	}
}