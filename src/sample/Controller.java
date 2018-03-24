package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.util.List;

public class Controller {
    @FXML LineChart<Float, Float> lineChart;
    @FXML TextField equationInput;
    @FXML TextField xInput;
    @FXML TextField yInput;

    @FXML
    Button btn_Add;

    //Series = one line graph
    private ObservableList<Series<Float, Float>> seriesList;
    private int seriesIndex = -1;

    public void initialize() {



        seriesList = FXCollections.observableArrayList();
    }


    //Add a point onto a graph (series). Button can't be clicked until Add Graph is pressed
    public void addPoint() {




        //Make input use numbers only
        xInput.setText(filterLetters(xInput.getText()));
        yInput.setText(filterLetters(yInput.getText()));

        System.out.println(xInput.getText());
        System.out.println(yInput.getText());

        //Make sure the x and y field are entered
        if (xInput.getText().trim().isEmpty() || yInput.getText().trim().isEmpty())
        {
            System.out.println("Need to enter data in both x and y field.");
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



        series.setName(Integer.toString(seriesIndex + 1) + ": GraphName");

        if (series.getData().add(new Data<Float, Float>(newX, newY))) {


            for (int i = 0; i < series.getData().size(); i++) {
                System.out.println(series.getData().get(i));
            }

            //Add onto series list if it's a new one
            if (newSeries) {
                seriesList.add(series);
                lineChart.getData().add(series);
                System.out.println("Added graph");
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
                }
            });
        }
    }

    //Add a new graph using the input fields
    public void addGraph()
    {
        btn_Add.setDisable(false);

        //Find where the new graph's index will be
        while (seriesIndex < seriesList.size())
            seriesIndex++;

        addPoint();
    }


    //Field formatter
    private String filterLetters(String text)
    {

        if (!text.matches("\\d{0,7}([\\.]\\d{0,4})?")) {
            return text.replaceAll("[^\\d]", "");
        }

        return text;
    }
}
