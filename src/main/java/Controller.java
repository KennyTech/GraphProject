//package sample;          ----you might want to uncomment this if you're using Intellij

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.control.Alert.AlertType;

import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.Expression;


public class Controller {
    @FXML LineChart<Float, Float> lineChart;
    @FXML TextField equationInput;
    @FXML TextField xInput;
    @FXML TextField yInput;

    @FXML ListView graphList;
    @FXML TextField nameInput;


    @FXML Button btn_Add;

    //Series = one line graph
    private ObservableList<Series<Float, Float>> seriesList;
    private int seriesIndex = -1;

    public void initialize() {

        seriesList = FXCollections.observableArrayList();
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

    //fills x and y coordinates into a map
    public Map<Float, Float> fillCoordinates(String rawInput) {

        int graphRange =50; //this changes how far the graph would plot/grow
        Map<Float, Float> coordMap = new LinkedHashMap<Float, Float>();

        //some methods from an external library which parse equation string and evaluates
        Expression e = new ExpressionBuilder(rawInput).variables("x")
                .build();
        //fill x and y
        for (int i=0;i<graphRange;i++){
            e.setVariable("x", i);
            coordMap.put((float)i, (float)e.evaluate());
        }

        return coordMap;
    }

    public void addEQPoints() {

        Series series = new Series<Float, Float>();
        series.setName(Integer.toString(seriesIndex + 1) + ": " + nameInput.getText());

        if (isEmpty(equationInput)) {
            infoBox("Enter an equation to plot the appropriate graph",
                    "Error Found!", null);

            return ;
        }
        else {

            Map<Float, Float> getCoordinates = fillCoordinates(equationInput.getText());
            System.out.println(getCoordinates);

            //iterate and draw graph with all points
            getCoordinates.forEach((k,v) ->
                    series.getData().add(new Data<Float, Float>(k, v)));

            seriesList.add(series);
            lineChart.getData().add(series);
            System.out.println("Added graph");
            graphList.getItems().add(series.getName());
            selectGraphInList();
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



        series.setName(Integer.toString(seriesIndex + 1) + ": " + nameInput.getText());

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
            }
            else {  //Otherwise change the old one

                seriesList.set(seriesIndex, series);
                System.out.println("Modified graph");

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
        if (nameInput.getText().isEmpty()) {

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
    }

    public void renameGraph()
    {
        int graphIndex = graphList.getSelectionModel().getSelectedIndex();

        if (graphIndex >= graphList.getItems().size() || graphIndex < 0) return;


        //Read name field
        String  newName = nameInput.getText();

        //Rename list item, series and graph
        graphList.getItems().set(graphIndex, Integer.toString(graphIndex + 1) + ": " + newName);

        seriesList.get(graphIndex).setName(Integer.toString(graphIndex + 1) + ": " + newName);

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


}