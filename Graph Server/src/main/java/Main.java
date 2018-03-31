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
	Socket clientSocket;
	
	public ConnectionHandler(Socket connect){
		clientSocket = connect;
	}
	public void run(){		
        int i = 0;
		 
		try{
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			String line = Integer.toString(Main.connectedClients.size());
			out.println(line);
			System.out.println("Line Sent");
			out.flush(); 			
			
          InputStream cin = clientSocket.getInputStream();
          InputStreamReader creader = new InputStreamReader(cin);
          BufferedReader cbin = new BufferedReader(creader);
		  while(true){
          String cline = null;
			System.out.println("Listening...");
          //Wait for client's command
		  
				if ((cline = cbin.readLine()) != null) {
					System.out.println(cline);
					cline = null;
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
	
    public static void main(String [] args) {
        try {
            serverSocket = new ServerSocket(8888);

            System.out.println("Ready");

            //Constantly accept client connections
            while(true){

                clientSocket = serverSocket.accept();
                if(clientSocket != null){
					ConnectionHandler ch = new ConnectionHandler(clientSocket);			
					ch.start();
					
					connectedClients.add(clientSocket);
					System.out.println(connectedClients.size());
					sendMessage(clientSocket);
                }
				clientSocket = null;
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
	
	public static void sendMessage(Socket sender){
		for(Socket c: connectedClients){
			if(c != sender){
					System.out.println("HI");

			}
		}
	}
}