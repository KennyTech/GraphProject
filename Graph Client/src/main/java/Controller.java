//package sample;          ----you might want to uncomment this if you're using Intellij

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.*;
import java.net.*;
import java.util.LinkedHashMap;
import java.util.Map;

import javafx.scene.control.Alert.AlertType;

import javafx.util.Duration;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.Expression;

import org.gillius.jfxutils.chart.JFXChartUtil;

import javax.xml.soap.Text;


public class Controller extends Thread{
    @FXML LineChart<Float, Float> lineChart;
    @FXML TextField equationInput;
    @FXML TextField xInput;
    @FXML TextField yInput;

    @FXML ListView graphList;
    @FXML TextField nameInput;
	@FXML TextField ipInput;

    @FXML
    private NumberAxis xAxis ;

    @FXML
    private NumberAxis yAxis ;


    @FXML Button btn_Add;
    @FXML Button btn_UP;
    @FXML Button btn_DOWN;
    @FXML Button btn_LEFT;
    @FXML Button btn_RIGHT;

    //Series = one line graph
    private ObservableList<Series<Float, Float>> seriesList;

    private int seriesIndex = -1;
    private int xAxisUpperBound = 25;
    private int xAxisLowerBound = -25;
    private int yAxisUpperBound = 25;
    private int yAxisLowerBound = -25;
    private final int traverseRate = 5;
	
	
	
	//Networking commands
	private boolean connectedToServer = false;
	Socket socket;
	PrintWriter out;	

    public void initialize() {

        seriesList = FXCollections.observableArrayList();
        //x-axis and y-axis early range
        setLineChartConditions();
        //this is for traversing through graph
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

    }

    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.1), (ActionEvent event) -> {
        // these will be called every 0.1 second
        //apparently having a timer is the best way to do on button press
        if (btn_UP.isPressed()) traverseUP();
        if (btn_DOWN.isPressed()) traverseDOWN();
        if (btn_LEFT.isPressed()) traverseLEFT();
        if (btn_RIGHT.isPressed()) traverseRIGHT();
    }));
    public void traverseUP() {
        yAxis.setUpperBound(yAxisUpperBound+=traverseRate);
        yAxis.setLowerBound(yAxisLowerBound+=traverseRate);
    }
    public void traverseDOWN() {
        yAxis.setUpperBound(yAxisUpperBound-=traverseRate);
        yAxis.setLowerBound(yAxisLowerBound-=traverseRate);
    }
    public void traverseLEFT() {
        xAxis.setUpperBound(xAxisUpperBound-=traverseRate);
        xAxis.setLowerBound(xAxisLowerBound-=traverseRate);
    }
    public void traverseRIGHT() {
        xAxis.setUpperBound(xAxisUpperBound+=traverseRate);
        xAxis.setLowerBound(xAxisLowerBound+=traverseRate);
    }


    public void setLineChartConditions() {
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(-25);
        xAxis.setUpperBound(25);
        xAxis.setTickUnit(1);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(-25);
        yAxis.setUpperBound(25);
        yAxis.setTickUnit(1);

        //this removes dots
        lineChart.setCreateSymbols(false);
        //enables zooming
        JFXChartUtil.setupZooming(lineChart);

    }
    //alert message for errors
    public void infoBox(String infoMessage, String titleBar, String headerMessage)
    {
        xAxis.setUpperBound(50);
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titleBar);
        alert.setHeaderText(headerMessage);
        alert.setContentText(infoMessage);
        alert.showAndWait();
    }

    public boolean isEmpty(TextField input) {return input.getText().trim().isEmpty();}

    public String checkIfEquation(String equation) {
        if (equation != null)
            return ": f(x)= "+equation;
        return "";
    }
    public void setGraphName(Series series, String equation) {
        String s = null;
        if (nameInputStrip().isEmpty())
            series.setName(Integer.toString(seriesIndex + 1) + ": " + "[untitled]" +checkIfEquation(equation));

        else
            series.setName(Integer.toString(seriesIndex + 1) + ": " + nameInputStrip() +checkIfEquation(equation));
    }



    //fills x and y coordinates into a map
    public Map<Float, Float> fillCoordinates(String rawInput) {

        float graphRange =25.0f; //this changes how far the graph would plot/grow
        Map<Float, Float> coordMap = new LinkedHashMap<Float, Float>();

        //some methods from an external library which parse equation string and evaluates
        Expression e = new ExpressionBuilder(rawInput).variables("x")
                .build();

        //fill x and y
        for (float f=(-25.0f);f<graphRange;f+=0.02){

            //this works fine except for any discontinuous graphs (ex: 1/x)
            e.setVariable("x", f);
            coordMap.put((float)f, (float)e.evaluate());

        }

        return coordMap;
    }

    public void addEQPoints() {

        Series series = new Series<Float, Float>();

        if (isEmpty(equationInput)) {
            infoBox("Enter an equation to plot the appropriate graph",
                    "Error Found!", null);

            return ;
        }
        else {
            setGraphName(series, equationInput.getText());
            Map<Float, Float> getCoordinates = fillCoordinates(equationInput.getText());
            System.out.println(getCoordinates);

            //iterate and draw graph with all points
            getCoordinates.forEach((k,v) ->
                    series.getData().add(new Data<Float, Float>(k, v)));

            seriesList.add(series);
            lineChart.getData().add(series);


            //System.out.println(lineChart.);
            //lineChart.z
            System.out.println("Added graph");
            graphList.getItems().add(series.getName());

            selectGraphInList();
			
			if(connectedToServer){
				
			out.println("ADQ " + equationInput.getText()); 
			out.flush();
			}
        }


    }
    public void plotEquation() {
        while (seriesIndex < seriesList.size())
            seriesIndex++;

        addEQPoints();
    }

    //Add a point onto a graph (series). Button can't be clicked until Add Graph is pressed
    public void addPoint() {

        //Make input use numbers only
        xInput.setText(filterLetters(xInput.getText()));
        yInput.setText(filterLetters(yInput.getText()));

        System.out.println(xInput.getText());
        System.out.println(yInput.getText());

        //Make sure the x and y field are entered
        if (isEmpty(xInput) || isEmpty(yInput))
        {
            infoBox("Need to enter data in both x and y", "Error Found!", null);
            return;
        }

        float newX = Float.parseFloat(xInput.getText());
        float newY = Float.parseFloat(yInput.getText());



        //Check if adding a new series or changing an old one
        boolean newSeries;
        Series series;

        if (seriesList.size() -1 < seriesIndex) {
            series = new Series<Float, Float>();
            newSeries = true;
        }
        else {
            series = seriesList.get(seriesIndex);
            newSeries = false;
        }

        //gg
        setGraphName(series, null);

		
        if (series.getData().add(new Data<Float, Float>(newX, newY))) {


            for (int i = 0; i < series.getData().size(); i++) {
                System.out.println(series.getData().get(i));
            }

            //Add onto series list if it's a new one
            if (newSeries) {
                seriesList.add(series);
                lineChart.getData().add(series);
                System.out.println("Added graph");

                graphList.getItems().add(series.getName());
                selectGraphInList();
				
				if(connectedToServer){
					out.println("ADD " + seriesIndex + " " + newX + " " + newY + " " + series.getName()); 
					out.flush();
				}
            }
            else {  //Otherwise change the old one

                seriesList.set(seriesIndex, series);
                System.out.println("Modified graph");
				
				if(connectedToServer){
					out.println("ADP " + seriesIndex + " " + newX + " " + newY); 
					out.flush();
				}
            }

			
            //On click for selecting a graph to be edited (Seems to only work when clicking on a line and not a single node)
            series.getNode().setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {


                    //Get index of graph
                    System.out.println(series.getName());
                    seriesIndex = (int)(series.getName().charAt(0));

                    //Convert from char ASCII to actual number
                    seriesIndex -= 48;

                    //Get proper array index (include 0)
                    seriesIndex -=1;

                    System.out.println("Selected graph " + seriesIndex);
                    lineChart.setTitle("Editing " + series.getName());

                    selectGraphInList();
                }
            });
        }
    }

    //Add a new graph using the input fields
    public void addGraph()
    {
        //Check if graph name has been created
        if (nameInputStrip().isEmpty()) {

            infoBox("A graph name needs to be entered in the bottom bar.", "Error Found!", null);
            return;

        }

        btn_Add.setDisable(false);

        //Find where the new graph's index will be
        while (seriesIndex < seriesList.size())
            seriesIndex++;

        addPoint();
		
		
    }



    //ListView onClick that simply selects a graph.
    public void pickGraph()
    {
        int graphIndex = graphList.getSelectionModel().getSelectedIndex();

        if (graphIndex < seriesList.size())
        {
            seriesIndex = graphIndex;
        }
    }

    private void selectGraphInList()
    {

        //Don't select it if it doesn't exist
        if (seriesIndex >= graphList.getItems().size()) return;

        //Highlight correct graph on list view
        graphList.getSelectionModel().select(seriesIndex);
        graphList.getFocusModel().focus(seriesIndex);
        graphList.scrollTo(seriesIndex);
    }

    //Remove selected graph in list
    public void removeGraph()
    {
        int graphIndex = graphList.getSelectionModel().getSelectedIndex();

        if (graphIndex >= graphList.getItems().size() || graphIndex < 0) return;

        System.out.print("Removing: " + graphIndex);


        seriesList.remove(graphIndex);
        lineChart.getData().remove(graphIndex);

        graphList.getItems().remove(graphIndex);


        seriesIndex = 0;
        selectGraphInList();
		
		if(connectedToServer){
		out.println("REM " + graphIndex); 
		out.flush();
		}
    }

    //YOU CAN PLAY AROUND WITH THESE TWO METHODS TO STORE EQUATION NAME IN A CSV FILE

    public void graphList() {
        for (int i=0; i<seriesList.size(); i++)
            //this splits the name into an array by delimiter
            //and if the name doesn't contain an equation it would have a size of 2
            System.out.println(isEquation(seriesList.get(i).getName().split(":")));
    }
    public Boolean isEquation(String[] eqName) {
        return eqName.length == 3;
    }

    // ******************************************************************************

    //this will eliminate crash if user enters ":" in name field
    public String nameInputStrip() {
        return nameInput.getText().replace(":", "");
    }
    public String printEquationName(String[] eqName) {
        //an equation would have a list of 3 items
        if (isEquation(eqName))
            return ": "+eqName[2]; //this is the equation
        else return ""; //return empty string if it's not an equation
    }
    public void renameGraph()
    {
        int graphIndex = graphList.getSelectionModel().getSelectedIndex();

        //this separates equation name by ":" into a list so you can know if it's an equation graph
        //Fixed the bug where if someone enters ":" into nameInput then it wouldn't split right- nameInputStrip()
        String[] equationName = graphList.getSelectionModel().getSelectedItem().toString().split(":");

        if (graphIndex >= graphList.getItems().size() || graphIndex < 0) return;


        //Read name field
        String  newName = nameInputStrip();

        //Rename list item, series and graph
        graphList.getItems().set(graphIndex, Integer.toString(graphIndex + 1) + ": " +
                newName + printEquationName(equationName));

        seriesList.get(graphIndex).setName(Integer.toString(graphIndex + 1) + ": " +
                newName + printEquationName(equationName));
		
		if(connectedToServer){
		out.println("REN " + graphIndex + " " + newName); 
		out.flush();
		}
    }

    //Duplicate selected graph in list. Doesn't work properly because javafx throws an error whenever the same series is created. Might just remove this...
    public void duplicateGraph()
    {
        int graphIndex = graphList.getSelectionModel().getSelectedIndex();

        System.out.print("Duping: " + graphIndex);

        if (graphIndex >= graphList.getItems().size()) return;



        Series series = new Series();


        series.getData().add(new Data<Float, Float>(0.0f,0.0f));

        series.getData().addAll(seriesList.get(graphIndex).getData());


        series.setName(graphList.getItems().get(graphIndex) + " (Copy)");

        seriesIndex=graphIndex + 1;


        seriesList.add(series);
        lineChart.getData().add(series);
        System.out.println("Added graph");

        graphList.getItems().add(series.getName());
        selectGraphInList();
		
		if(connectedToServer){
		out.println("DUP " + graphIndex); 
		out.flush();
		}
    }


    //Field formatter that makes sure x and y input field has no letters, etc.
    private String filterLetters(String text)
    {

        //Check for a format of 0-7 digit numbers along with decimals of 0-8 digits
        if (!text.matches("\\d{0,7}([\\.]\\d{0,8})?")) {

            //If the above format doesn't match then replace it into the correct form.

            //Easy way to keep negatives
            if (text.charAt(0) == '-') return "-" + text.replaceAll("[^\\d]", "");


            return text.replaceAll("[^\\d]", "");
        }

        return text;
    }
	
	public void serverConnect(){
		 try {
			 String ip = ipInput.getText();
			 System.out.println(ip);
			 if(ip == null)
				 ip = "127.0.0.1";
                    //Connect to server
                    socket = new Socket(ip, 8888);   
					connectedToServer = true;
					this.start();
					  
                }catch (IOException e){
                    System.out.println("IOException");
                    e.printStackTrace();
					
                }
	}
	
	public void run(){
		
		
		try{
		out = new PrintWriter(socket.getOutputStream());
		InputStream in = socket.getInputStream();
		BufferedReader bin = new BufferedReader(new InputStreamReader(in));
		String line = null;

		while((line = bin.readLine()) == null){
			System.out.println("Waiting...");
		}
		
		System.out.println(line);
		
		
		
		}
		catch(IOException e){
			System.out.println("IOException");
			connectedToServer = false;
		}
	}
}