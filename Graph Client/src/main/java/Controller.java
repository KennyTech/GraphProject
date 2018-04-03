//package sample;          ----you might want to uncomment this if you're using Intellij

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;

import javafx.stage.Stage;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import java.io.*;
import java.net.*;
import javafx.application.Platform;

import javafx.scene.control.Alert.AlertType;

import javafx.util.Duration;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.Expression;

import org.gillius.jfxutils.chart.JFXChartUtil;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.xml.soap.Text;





public class Controller extends Thread{


    @FXML LineChart<Float, Float> lineChart;
    @FXML ListView graphList;
    @FXML TextField equationInput,xInput, yInput, nameInput;
    @FXML private NumberAxis xAxis, yAxis ;
    @FXML Button btn_Add, btn_Remove, btn_Rename, btn_UP, btn_DOWN, btn_LEFT, btn_RIGHT;

	@FXML TextField ipInput;
   //Series = one line graph
    private ObservableList<Series<Float, Float>> seriesList;

    private int seriesIndex = -1, xAxisUpperBound = 25, xAxisLowerBound = -25,
            yAxisUpperBound = 25, yAxisLowerBound = -25;
    private final int traverseRate = 5;


	
	//Networking variables
	@FXML Button btn_Connect;
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

    public void onKeyPress(Button button, KeyCode keyCode) {
        button.getScene().getAccelerators().put(
                //you can use CTRL+ arrows or just arrows
                new KeyCodeCombination(keyCode, KeyCombination.SHORTCUT_ANY),
                new Runnable() {
                    @Override public void run() {
                        if (keyCode.equals(KeyCode.UP))
                            traverseUP();
                        else if (keyCode.equals(KeyCode.DOWN))
                            traverseDOWN();
                        else if (keyCode.equals(KeyCode.LEFT))
                            traverseLEFT();
                        else if (keyCode.equals(KeyCode.RIGHT))
                            traverseRIGHT();
                        else if (keyCode.equals(KeyCode.ESCAPE))
                            setLineChartConditions();
                    }
                }
        );
    }

    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.1), (ActionEvent event) -> {
        // these will be called every 0.1 second
        //apparently having a timer is the best way to do on button press
        if (!lineChart.getData().isEmpty()) {
            //for key presses
            onKeyPress(btn_UP, KeyCode.UP);
            onKeyPress(btn_DOWN, KeyCode.DOWN);
            onKeyPress(btn_LEFT, KeyCode.LEFT);
            onKeyPress(btn_RIGHT, KeyCode.RIGHT);
            //onKeyPress(btn_ESCAPE, KeyCode.ESCAPE); for resetting zoom, maybe can add this on edit menu

            //for button presses
            if (btn_UP.isPressed()) traverseUP();
            if (btn_DOWN.isPressed()) traverseDOWN();
            if (btn_LEFT.isPressed()) traverseLEFT();
            if (btn_RIGHT.isPressed()) traverseRIGHT();
        }
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
        xAxis.setLowerBound(yAxisLowerBound);
        xAxis.setUpperBound(xAxisUpperBound);
        xAxis.setTickUnit(1);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(yAxisLowerBound);
        yAxis.setUpperBound(yAxisUpperBound);
        yAxis.setTickUnit(1);

        //this removes dots
        lineChart.setCreateSymbols(false);
        //enables zooming
        JFXChartUtil.setupZooming(lineChart);

    }
    //alert message for errors
    public void infoBox(String infoMessage, String titleBar, String headerMessage)
    {
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

        //float graphRange = xAxisUpperBound; //this changes how far the graph would plot/grow
        Map<Float, Float> coordMap = new LinkedHashMap<Float, Float>();

        //some methods from an external library which parse equation string and evaluates
        Expression e = new ExpressionBuilder(rawInput).variables("x")
                .build();

        //fill x and y
        for (float f=xAxisLowerBound-75;f<xAxisUpperBound+75;f+=0.075){

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
            return;
        }
        //else if (equationInput.getText().)
        else {
            try {
                btn_Remove.setDisable(false);
                setGraphName(series, equationInput.getText());
                Map<Float, Float> getCoordinates = fillCoordinates(equationInput.getText());
                System.out.println(getCoordinates);

                //iterate and draw graph with all points
                getCoordinates.forEach((k, v) ->
                        series.getData().add(new Data<Float, Float>(k, v)));

                graphEquations.add(equationInput.getText());    // add to ArrayList for save/open
                graphNames.add(nameInputStrip());                // add to ArrayList for save/open
                seriesList.add(series);
                lineChart.getData().add(series);


                //System.out.println(lineChart.);
                //lineChart.z
                System.out.println("Added graph");
                graphList.getItems().add(series.getName());

                selectGraphInList();

                // Update title to signify file changed by appending "*" if graphChange is true (ie. this function was called from button, not from open file)
                if (graphChange)
                    updateTitle(filename, true);

                if (connectedToServer) {

                    out.println("ADQ " + equationInput.getText());
                    out.flush();
                }
            } catch (RuntimeException e) {
                infoBox("Equation must be in the form, Ex: f(x) = x",
                        "Invalid Equation Input!", null);
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
 	
        // Update title to signify file changed by appending "*" if graphChange is true (ie. this function was called from button, not from open file)
        if (graphChange)
        	updateTitle(filename, true);

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

        btn_Remove.setDisable(false);
        btn_Add.setDisable(false);
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


            }


				
				if(connectedToServer){
					out.println("ADP " + seriesIndex + " " + newX + " " + newY); 
					out.flush();
				}
            }

	        // === For save/open ===  
	        // Fix name first
	        String splitFix = (String) series.getName();
			// Split with ":" delimiter
			String secondPart[] = splitFix.split(":");
			// Get second part of split
			String nameFix = secondPart[secondPart.length - 1];
			// Append ALL points from all graphs for save/open
			//toAppend = toAppend + seriesIndex + "," + nameFix + "," + xInput.getText() + "," + yInput.getText() + "\n";
			toAppend = seriesIndex + "," + nameFix + "," + xInput.getText() + "," + yInput.getText() + "\n";
	        
	        // ArrayList method (to sort)
			if (appendList.isEmpty()) {
				appendList.add("-POINT DATA-\n");
			}
			// sort data by index, allowing out-of-order editing of graphs and points to be saved and opened correctly
	        appendList.add(toAppend);
	        Collections.sort(appendList);
	        for(int i=0; i<appendList.size(); i++)
	            System.out.println(appendList.get(i));
	        // === end save/open ===
        
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


    //Add a new graph using the input fields
    public void addGraph()
    {
        //Check if graph name has been created
        if (!nameInputStrip().isEmpty()) {

            //Find where the new graph's index will be
            while (seriesIndex < seriesList.size())
                seriesIndex++;

            addPoint();
        } else {
            infoBox("A graph name needs to be entered in the bottom bar.", "Error Found!", null);
            return;
        }


        btn_Add.setDisable(false);

    }



    //ListView onClick that simply selects a graph.
    public void pickGraph()
    {
        int graphIndex = graphList.getSelectionModel().getSelectedIndex();
        btn_Rename.setDisable(false);

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

        if (graphList.getItems().isEmpty()) {
            btn_Rename.setDisable(true);
            btn_Remove.setDisable(true);
            btn_Add.setDisable(true);
        }


        seriesIndex = 0;
        selectGraphInList();

        // Update title to signify file changed by appending "*" if graphChange is true (ie. this function was called from button, not from open file)
        if (graphChange)
        	updateTitle(filename, true);

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

        if (graphIndex >= graphList.getItems().size() || graphIndex < 0) return;

        //this separates equation name by ":" into a list so you can know if it's an equation graph
        //Fixed the bug where if someone enters ":" into nameInput then it wouldn't split right- nameInputStrip()
        String[] equationName = graphList.getSelectionModel().getSelectedItem().toString().split(":");
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

    
    
    // =========================
    // Graph save/open variables
    // =========================
    private List<String> graphEquations 		= new ArrayList<String>();	// save all graph expressions for save/open in a dynamic ArrayList
    private List<String> graphNames 			= new ArrayList<String>();	// save all graph names for save/open in a dynamic ArrayList
    private String filename 					= "";						// file name of file being edited
    private String filepath						= "";						// check path to see if file exists to overwrite
    private boolean graphChange 				= true;						// graph changed, for appending title with "*" to signify file change
    private String toAppend						= "";						// appends add point data from all graphs for save/open
    private static ArrayList<String> isNewList 	= new ArrayList<String>();	// list to check if next index is new graph or not
    private List<String> appendList				= new ArrayList<String>();	// append data in a string for add point method and sort to allow out-of-order editing
    
    // ========================================
    // update title function (include filename)
    // ========================================
    public void updateTitle(String str, boolean changed) {
    	Stage primaryStage = (Stage) lineChart.getScene().getWindow();  
    	if (!(changed))
    		primaryStage.setTitle("Line Grapher (" + str + ")"); 
    	else
    		primaryStage.setTitle("Line Grapher (" + str + ")*");	// when file is changed, update title by adding "*"
    }
    
    // ==================
    // Save graph to file
    // ==================
    public void saveGraph() throws FileNotFoundException {
    	System.out.println("Saving...");
    	
    	// if new file, treat as save as, otherwise save to current file being edited
    	if (filename.equals("")) {
    		saveAsGraph();
    	}
    	else {
    		// create a new string array with the same size as the dynamic ArrayList
    		String[] expressionList = new String[graphEquations.size()];
    		// convert and fill the array
    		graphEquations.toArray(expressionList);
    		
    		// create a new string array with the same size as the dynamic ArrayList
    		String[] nameList = new String[graphNames.size()];
    		// convert and fill the array
    		graphNames.toArray(nameList);
    		
    		// if file already exists, delete and recreate it
        	File f = new File(filepath);
        	if (f.exists()) {
        		if (f.delete()) {
        			System.out.println(f.getName() + " was overwritten.");
        		} else {
        			System.out.println("Overwrite failed. Make sure file is not open somewhere else.");
        		}
        	}
    		
    		// iterate through graph expressions and write to file
        	try (PrintWriter out = new PrintWriter(filename)) {
        		
        		StringBuilder sb = new StringBuilder();     	              
        	    // data fields (name, equation)
    	    	for (int i = 0; i < expressionList.length; i++) {   	    
    	    		//setGraphName(series, equationInput.getText());
        	        sb.append(nameList[i]);
        	        sb.append(',');
        	        sb.append(expressionList[i]);
        	        sb.append('\n');
    	    	}
    	    	
    	    	// save points (data field: x, y)
    	    	for (int i = 0; i < appendList.size(); i++) {   	   
        	        sb.append(appendList.get(i));
    	    	}
    	    	
    	    	// Save to file
    	    	out.print(sb.toString());
    	    	out.flush();
    	    	out.close();
    	    	
    	    	// change title to reflect filename
    	        updateTitle(filename, false);    	    	
        	}
    	}  	
    }
    
    // =====================
    // Save as graph to file
    // =====================
    public void saveAsGraph() throws FileNotFoundException {
    		
    	System.out.println("Saving As...");
    	
    	// make new save sub-directory if nonexistent
    	new File("./saves").mkdirs();
    	
		// create a new string array with the same size as the dynamic ArrayList
		String[] expressionList = new String[graphEquations.size()];
		// convert and fill the array
		graphEquations.toArray(expressionList);
		
		// create a new string array with the same size as the dynamic ArrayList
		String[] nameList = new String[graphNames.size()];
		// convert and fill the array
		graphNames.toArray(nameList);
 	
		// prompt user to save file name/directory
        String extension = saveHelper("csv");
    		// iterate through graph expressions and write to file
        	try (PrintWriter out = new PrintWriter(extension)) {
        		
        		StringBuilder sb = new StringBuilder();     	              
        	    // data fields (name, equation)
    	    	for (int i = 0; i < expressionList.length; i++) {   	    
    	    		//setGraphName(series, equationInput.getText());
        	        sb.append(nameList[i]);
        	        sb.append(',');
        	        sb.append(expressionList[i]);
        	        sb.append('\n');
    	    	}
    	    	
    	    	// save points (data field: x, y)
    	    	for (int i = 0; i < appendList.size(); i++) {   	    
        	        sb.append(appendList.get(i));
    	    	}
    	    	
    	    	// Save to file
    	    	out.print(sb.toString());
    	    	out.flush();
    	    	out.close();
    	    	
    	    	// change title to reflect filename
    	        filename = extension;
    	        updateTitle(filename, false);    	    	
        	}
    }
    public void saveImage() throws FileNotFoundException {
        System.out.println("Saving Images As...");
        String extension = saveHelper("jpg");
        //get the image of line item and save it in a file
        WritableImage image = lineChart.snapshot(new SnapshotParameters(), null);
        File file = new File(extension);

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "jpg", file);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String saveHelper(String fileFormat) {
        String extension = "";

        // make new save sub-directory if nonexistent
        new File("./saves").mkdirs();

        // prompt user to save file name/directory
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("./saves"));
        int retrival = chooser.showSaveDialog(null);
        if (retrival == JFileChooser.APPROVE_OPTION) {

            String savePath = chooser.getSelectedFile().getAbsolutePath();
            Boolean alreadyTextExtension = false;
            
            // if file already exists, delete and recreate it
        	File f = chooser.getSelectedFile();
        	if (f.exists()) {
        		if (f.delete()) {
        			System.out.println(f.getName() + " was overwritten.");
        		} else {
        			System.out.println("Overwrite failed. Make sure file is not open somewhere else.");
        		}
        	}

            // get extension to make sure file does not save as duplicate extension ("file.csv.csv")
            if (savePath.length() > 3)
                extension = savePath.substring(savePath.length() - 3); 	// get last 3 chars of filename
            if (extension.equals(fileFormat)) {
                alreadyTextExtension = true;
            }

            // if file already has text extension, set extension to null, otherwise set to .txt
            if (alreadyTextExtension) {
                extension = "";
            }
            else {
                extension = "."+fileFormat;
            }

        }
        return chooser.getSelectedFile()+extension;
    }
    
    // =================
    // Open a graph file
    // =================
    public void openGraph() throws IOException {
    	System.out.println("Opening..."); 	
    	
		// prompt user to open a save file
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("./saves"));
        int retrival = chooser.showOpenDialog(null);
        if (retrival == JFileChooser.APPROVE_OPTION) {
        	// successfully loaded, start a new graph before loading
        	newGraph();
        	
            // get path of file to display on program title later
        	System.out.println(chooser.getSelectedFile().getAbsolutePath());
        
        	// scan file
        	Scanner scan = new Scanner(new File(chooser.getSelectedFile().getAbsolutePath()));
        	
        	ArrayList<String> eqList = new ArrayList<String>();		// equation list
        	ArrayList<String> nameList = new ArrayList<String>();	// name list
        	
        	boolean pointData = false;
        	String graphIndex = "";
        	// iterate and separate column values
	    	while (scan.hasNextLine()){
	    		String line = scan.nextLine();
	    		
	    		if (line.equals("-POINT DATA-")) {
	    			System.out.println("Found 'add point' method data");
	    			if (scan.hasNextLine()) {
	    				line = scan.nextLine();
	    				pointData = true;
	    			}
	    		}
	    		
	    		if (pointData) {
	    			String[] split = line.split(",");
	    			// plot and graph point data now--more efficient now than later due to format of setup	    			
	    			graphIndex = (split[0]);
    				nameInput.setText(split[1]);
	    			xInput.setText(split[2]);
	    			yInput.setText(split[3]);
	    			// if the index is new, we create a new graph
	    			if (isNew(graphIndex)) {
	    				addGraph();
	    			}
    				addPoint();
	    			// end plot
	    		}
	    		else {
	    			// Prepare expression and graph name data to loop through and plot after
		    		String[] split = line.split(",");	    		
		    		eqList.add(split[1]);
		    		nameList.add(split[0]);
	    		}
	    	}
	    	scan.close();
    	
	    	
	    	// loop through all graph names and equations
	    	for (int i = 0; i < eqList.size(); i++) {
	    		
	    		// create a new array with the same size as the dynamic list
	    		String[] eqString = new String[eqList.size()];
	    		String[] nameString = new String[nameList.size()];
	    		
	    		// fill the array
	    		eqList.toArray(eqString);
	    		nameList.toArray(nameString);
	    	    
	    		// convert
	    	    equationInput.setText(eqString[i]);   	    	 
	    	    nameInput.setText(nameString[i]);
	    	    
	    	    // loop index count
	    		while (seriesIndex < seriesList.size())
	                seriesIndex++;
	
	    		// plot graph based on name and equation
	    		graphChange = false;
	            addEQPoints();	 
	            graphChange = true;
	            
	    	} 
	    	
        }
        
		// change title to reflect filename
        filename = chooser.getSelectedFile() + "";
        updateTitle(filename, false); 
	    
    }
    
    // ==========
    // New graph
    // ==========
    public void newGraph() {
    	System.out.println("New graph...");
    	System.out.println("size..." + seriesList.size());
    	int graphIndex = seriesList.size();
    	
    	// iterate through all current graphs and remove
    	for (int i = 0; i < graphIndex; i++) {
	
    		seriesList.remove(0);
	        lineChart.getData().remove(0);
	        graphList.getItems().remove(0);
	
	        if (graphList.getItems().isEmpty()) {
	            btn_Rename.setDisable(true);
	            btn_Remove.setDisable(true);
	        }
	
	        seriesIndex = 0;
    	}   
    	
    	// Update filename in title
    	updateTitle("New", false); 
    	
    	// Clear any previous lists and reset string
    	toAppend = "POINT DATA\n";
    	graphEquations.clear();	
    	graphNames.clear();
    	appendList.clear();
    	isNewList.clear();
    }
    
    
    // ======================================================================================================================
    // for open file, checks if index is new or not to determine if program should create a new graph before appending points
    // ======================================================================================================================
    public boolean isNew(String index) {
    	if (isNewList.contains(index)) {
    		return false;
    	}
    	isNewList.add(index);
    	return true;
    }
    
    // ==========
    // Exit graph
    // ==========
    public void exitProgram() {
    	System.out.println("Exiting Grapher...");
    	Stage primaryStage = (Stage) lineChart.getScene().getWindow();
		connectedToServer = false;
    	primaryStage.close();
		System.exit(0);
    }
    



	
	
	//Network starts here//
	public void serverConnect() throws IOException {
			 String ip = ipInput.getText();
			 System.out.println(ip);
			 if(ip == null)
				 ip = "127.0.0.1";
                    //Connect to server
                try {
                    //if user enters random IP address then catch connection timeout
                    socket = new Socket(ip, 8888);
                    connectedToServer = true;
                    this.start();

                    //Don't let client connect multiple times
                    btn_Connect.setDisable(true);

                } catch (ConnectException e) {
                    infoBox("Try leaving the IP address empty or\n" +
                            "enter '127.0.0.1' as the IP address!",
                            "Connection Timed Out!", "[ Server not found ]");
                } catch (SocketException e) {
                    infoBox("Try leaving the IP address empty or\n" +
                                    "enter '127.0.0.1' as the IP address!",
                            "Invalid IP address!",
                            "[ The address you entered is not an IP adress ]");
                }
	    }
	
	public void run(){		
		try{
		out = new PrintWriter(socket.getOutputStream());
		InputStream in = socket.getInputStream();
		BufferedReader bin = new BufferedReader(new InputStreamReader(in));
		String line = null;
		
		while(connectedToServer){
			//Wait for command
			if ((line = bin.readLine()) != null){
				System.out.println(line);
				
				String[] command = line.split(" ");
				
				//First line in command is the code. Everything else is parameters
				if(command[0].contains("ADD")){
					 btn_Add.setDisable(false);
					N_addPoint(Integer.parseInt(command[1]), Float.parseFloat(command[2]), Float.parseFloat(command[3]),command[4]);
				}
				else if(command[0].contains("ADP")){
					N_addPoint(Integer.parseInt(command[1]), Float.parseFloat(command[2]), Float.parseFloat(command[3]),null);
				}
				else if(command[0].contains("ADQ")){
					N_addEQPoints(command[1]);
				}
				else if(command[0].contains("REM")){
					N_removeGraph(Integer.parseInt(command[1]));
				}
				else if(command[0].contains("REN")){
					N_renameGraph(Integer.parseInt(command[1]), command[2]);
				}
				else if(command[0].contains("DUP")){
					N_duplicateGraph(Integer.parseInt(command[1]));
				}
			}		
		}
		
		out.close();
		in.close();
		socket.close();
		bin.close();
		
		
		}
		catch(IOException e){
			System.out.println("IOException");
			connectedToServer = false;
		}
	}
	
	//These functions take inputs as arguments instead of from text boxes, but otherwise are copied from above 
	public void N_addPoint(int index, float x, float y, String name) {
		
        //Check if adding a new series or changing an old one
        boolean newSeries;
        Series series;

        if (seriesList.size() -1 < index) {
            series = new Series<Float, Float>();
            newSeries = true;
        }
        else {
            series = seriesList.get(index);
            newSeries = false;
        }

        //gg
        setGraphName(series, null);

		
        if (series.getData().add(new Data<Float, Float>(x, y))) {


            for (int i = 0; i < series.getData().size(); i++) {
                System.out.println(series.getData().get(i));
            }

            //Add onto series list if it's a new one
            if (newSeries) {
                seriesList.add(series);
				
				Platform.runLater(new Runnable(){
					@Override public void run(){
						lineChart.getData().add(series);
						 System.out.println("Added graph");
						 graphList.getItems().add(name);	
               
					}
				});
               		
            }
            else {  //Otherwise change the old one

                seriesList.set(index, series);
                System.out.println("Modified graph");
            }
        }
    }
	
	public void N_addEQPoints(String eq) {
		 
		 

        Series series = new Series<Float, Float>();


            setGraphName(series, eq);
            Map<Float, Float> getCoordinates = fillCoordinates(eq);
            System.out.println(getCoordinates);

            //iterate and draw graph with all points
            getCoordinates.forEach((k,v) ->
                    series.getData().add(new Data<Float, Float>(k, v)));

            seriesList.add(series);
           


            //System.out.println(lineChart.);
            //lineChart.z
            System.out.println("Added graph");
			
			Platform.runLater(new Runnable(){
					@Override public void run(){
						 lineChart.getData().add(series);
						 System.out.println("Added graph");
						  graphList.getItems().add(series.getName());	
               
					}
				});
           
        


    }
	
	public void N_removeGraph(int index){
		  

        if (index >= graphList.getItems().size() || index < 0) return;

        System.out.print("Removing: " +  index);


			Platform.runLater(new Runnable(){
					@Override public void run(){
						  seriesList.remove( index);
						   lineChart.getData().remove( index);

        graphList.getItems().remove( index);
					}
			});
      
       
	  }
	  
	public void N_renameGraph(int index, String name){

        //this separates equation name by ":" into a list so you can know if it's an equation graph
        //Fixed the bug where if someone enters ":" into nameInput then it wouldn't split right- nameInputStrip()
		graphList.getSelectionModel().select(index);
        String[] equationName = graphList.getSelectionModel().getSelectedItem().toString().split(":");

        if (index >= graphList.getItems().size() || index < 0) return;

        //Rename list item, series and graph
      

					Platform.runLater(new Runnable(){
					@Override public void run(){
						  graphList.getItems().set(index, Integer.toString(index+ 1) + ": " +
                name + printEquationName(equationName));
        seriesList.get(index).setName(Integer.toString(index + 1) + ": " +
                name + printEquationName(equationName));
						}
			});
    }

	public void N_duplicateGraph(int index)
    {
        int graphIndex = index;

        System.out.print("Duping: " + graphIndex);

        if (graphIndex >= graphList.getItems().size()) return;



        Series series = new Series();


        series.getData().add(new Data<Float, Float>(0.0f,0.0f));

        series.getData().addAll(seriesList.get(graphIndex).getData());


        series.setName(graphList.getItems().get(graphIndex) + " (Copy)");

        seriesIndex=graphIndex + 1;


		Platform.runLater(new Runnable(){
					@Override public void run(){
						   seriesList.add(series);
					}
			});
       
        lineChart.getData().add(series);
        System.out.println("Added graph");

        graphList.getItems().add(series.getName());

    }
}
