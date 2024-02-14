//Grupp 169, Alice Wallin alwa1412, Vainius Mikelinskas vami4627, Andreas Retzius anre8319

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;



import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

import java.util.ArrayList;
import java.util.Optional;



public class PathFinder extends Application {
    private final ArrayList<NewPlace> selectedPlaces = new ArrayList<>();
    private ListGraph<NewPlace> listGraph = new ListGraph<>();
    private final ArrayList<String> places = new ArrayList<>();
    private final ArrayList<String> connections = new ArrayList<>();

    private Stage stage;
    private BorderPane root;
    private Pane center;
    private boolean changed;

    private String path = "file:europa.gif";

    private final Button newPlaceButton = new Button("New Place");

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {

        this.stage = stage;
        root = new BorderPane();
        VBox vbox = new VBox();
        root.setTop(vbox);

        //TOP

        Menu m = new Menu("File");
        m.setId("menuFile");
        MenuItem newMap = new MenuItem("New Map");
        newMap.setId("menuNewMap");
        newMap.setOnAction(new NewMapHandler());
        MenuItem openItem = new MenuItem("Open");
        openItem.setId("menuOpenFile");
        openItem.setOnAction(new OpenItemHandler());
        MenuItem saveItem = new MenuItem("Save");
        saveItem.setId("menuSaveFile");
        saveItem.setOnAction(new SaveItemHandler());
        MenuItem saveImage = new MenuItem("Save Image");
        saveImage.setId("menuSaveImage");
        saveImage.setOnAction(new SaveImageHandler());
        MenuItem exitProgram = new MenuItem("Exit");
        exitProgram.setId("menuExit");
        exitProgram.setOnAction(new ExitProgramHandler());
        stage.setOnCloseRequest(new ExitHandler());

        // add menu items to menu
        m.getItems().addAll(newMap, openItem, saveItem, saveImage, exitProgram);

        // create a menubar
        MenuBar menuBar = new MenuBar();
        menuBar.setId("menu");

        // add menu to menubar
        menuBar.getMenus().add(m);

        vbox.getChildren().add(menuBar);


        FlowPane controls = new FlowPane();

        controls.setPadding(new Insets(15));
        controls.setHgap(10);
        controls.setVgap(10);
        controls.setAlignment(Pos.CENTER);

        Button findPathButton = new Button("Find Path");
        findPathButton.setId("btnFindPath");
        findPathButton.setOnAction(new FindPathHandler());
        Button showConnectionButton = new Button("Show Connection");
        showConnectionButton.setOnAction(new ShowConnectionHandler());
        showConnectionButton.setId("btnShowConnection");
        newPlaceButton.setId("btnNewPlace");
        newPlaceButton.setOnAction(new NewButtonHandler());
        Button newConnectionButton = new Button("New Connection");
        newConnectionButton.setId("btnNewConnection");
        newConnectionButton.setOnAction(new NewConnectionHandler());
        Button changeConnectionButton = new Button("Change Connection");
        changeConnectionButton.setId("btnChangeConnection");
        changeConnectionButton.setOnAction(new ChangeConnectionHandler());

        controls.getChildren().addAll(findPathButton, showConnectionButton, newPlaceButton, newConnectionButton, changeConnectionButton);

        vbox.getChildren().add(controls);

        //CENTER

        center = new Pane();
        center.setId("outputArea");
        root.setCenter(center);

        Scene scene = new Scene(root, Color.GRAY);
        stage.setWidth(630);
        stage.setScene(scene);
        stage.setTitle("PathFinder");
        stage.show();
        stage.setResizable(false);

    }

    class ConnectionDialog extends Alert {
        private final TextField nameField = new TextField();
        private final TextField timeField = new TextField();

        ConnectionDialog(String from, String to) {
            super(AlertType.CONFIRMATION);
            GridPane grid = new GridPane();
            grid.setAlignment(Pos.CENTER);
            grid.setPadding(new Insets(10));
            grid.setHgap(5);
            grid.setVgap(10);
            grid.addRow(0, new Label("Name: "), nameField);
            grid.addRow(1, new Label("Time: "), timeField);
            setHeaderText("Connection from " + from + " to " + to);
            getDialogPane().setContent(grid);

        }

        public int getTime() {
            return Integer.parseInt(timeField.getText());
        }

        public String getName() {
            return nameField.getText();
        }
    }

    class NewButtonHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            center.setOnMouseClicked(new ClickHandler());
            center.setCursor(Cursor.CROSSHAIR);
            newPlaceButton.setDisable(true);
        }
    }

    private void createMap() {
        places.clear();
        connections.clear();
        selectedPlaces.clear();
        center.getChildren().clear();
        listGraph = new ListGraph<>();
        Image image = new Image(path);
        ImageView imageView = new ImageView(image);
        center.getChildren().add(imageView);
        stage.sizeToScene();
        changed = true;
    }

    private void createConnection(NewPlace one, NewPlace two, String transport, int time) {
        if (listGraph.getEdgeBetween(one, two) == null) {
            listGraph.connect(one, two, transport, time);
            Line newLine = new Line(one.getCenterX(), one.getCenterY(), two.getCenterX(), two.getCenterY());
            newLine.setStrokeWidth(4);
            newLine.setDisable(true);
            center.getChildren().add(newLine);
            connections.add(one.getName() + ";" + two.getName() + ";" + transport + ";" + time + "\n");
            connections.add(two.getName() + ";" + one.getName() + ";" + transport + ";" + time + "\n");
            changed = true;
        }
    }

    private NewPlace findCountry(String countryName) {
        for (NewPlace newPlace : listGraph.getNodes()) {
            if (newPlace.getName().equals(countryName)) {
                return newPlace;
            }
        }
        return null;
    }

    class ClickHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            double x = event.getX();
            double y = event.getY();
            center.setOnMouseClicked(null);
            center.setCursor(Cursor.DEFAULT);

            TextInputDialog newPlaceDialog = new TextInputDialog();
            newPlaceDialog.setTitle("Name Input");
            newPlaceDialog.setContentText("Name of place:");

            Optional<String> placeName = newPlaceDialog.showAndWait();
            placeName.ifPresent(s -> addPlace(s, x, y));

            /*
            NewPlace place = new NewPlace(placeName.get(), x, y);
            place.setId(placeName.get());
            places.add(placeName.get() + ";" + x + ";" + y + ";");
            place.setOnMouseClicked(new PlaceSelectClickHandler());
            center.getChildren().add(place);
            listGraph.add(place);

             */
            newPlaceButton.setDisable(false);
        }
    }

    public void addPlace(String name, double x, double y) {
        NewPlace place = new NewPlace(name, x, y);
        place.setId(name);
        places.add(name + ";" + x + ";" + y + ";");
        place.setOnMouseClicked(new PlaceSelectClickHandler());
        center.getChildren().add(place);
        Text text = new Text(x + 10.0, y + 20.0, name);
        text.setStyle("-fx-font-weight: bold");
        text.setDisable(true);
        center.getChildren().add(text);
        listGraph.add(place);
        changed = true;
    }

    class PlaceSelectClickHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            NewPlace currentPlace = (NewPlace) event.getSource();

            if (currentPlace.isSelected()) {
                currentPlace.deselectPlace();
                selectedPlaces.remove(currentPlace);
            } else if (selectedPlaces.size() < 2) {
                currentPlace.selectPlace();
                selectedPlaces.add(currentPlace);
            }

        }
    }

    class NewConnectionHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (selectedPlaces.size() < 2) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "You have to have two places selected");
                alert.showAndWait();
                return;
            }
            try {
                NewPlace from = selectedPlaces.get(0);
                NewPlace to = selectedPlaces.get(1);

                if (listGraph.pathExists(from, to)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, from.getName() + " and " + to.getName() + " are already connected");
                    alert.showAndWait();
                    return;
                }

                ConnectionDialog dialog = new ConnectionDialog(from.getName(), to.getName());
                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isPresent() && result.get() != ButtonType.OK)
                    return;

                String name = dialog.getName();
                int time = dialog.getTime();

                if (name.equals("")) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Name cannot be empty");
                    alert.showAndWait();
                }

                if (time < 0) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Time cannot be a negative integer");
                    alert.showAndWait();
                }

                createConnection(from, to, name, time);

                //automatiskt avmarkerar
                //selectedPlaces.clear();
                //from.deselectPlace();
                //to.deselectPlace();
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Time has to be a number");
                alert.showAndWait();
            }
        }
    }

    class ShowConnectionHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (selectedPlaces.size() < 2) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "You have to have two places selected");
                alert.showAndWait();
                return;
            }
            try {
                NewPlace from = selectedPlaces.get(0);
                NewPlace to = selectedPlaces.get(1);

                if (listGraph.getEdgeBetween(from, to) == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, from.getName() + " and " + to.getName() + " are not connected");
                    alert.showAndWait();
                    return;
                }

                ConnectionDialog dialog = new ConnectionDialog(from.getName(), to.getName());
                dialog.nameField.setText(listGraph.getEdgeBetween(from, to).getName());
                dialog.nameField.setEditable(false);
                dialog.timeField.setText(String.valueOf(listGraph.getEdgeBetween(from, to).getWeight()));
                dialog.timeField.setEditable(false);
                dialog.showAndWait();


            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Time has to be a number");
                alert.showAndWait();
            }
        }
    }

    class FindPathHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (selectedPlaces.size() < 2) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "You have to have two places selected");
                alert.showAndWait();
            } else {

                int tempWeight = 0;
                NewPlace from = selectedPlaces.get(0);
                NewPlace to = selectedPlaces.get(1);

                if (!listGraph.pathExists(from, to)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "There is no path between " + from.getName() + " and " + to.getName());
                    alert.showAndWait();
                    return;
                }

                StringBuilder builder = new StringBuilder();

                for (Edge<?> edge : listGraph.getPath(from, to)) {
                    builder.append("to ").append(edge.getDestination()).append(" by ").append(edge.getName()).append(" takes ").append(edge.getWeight()).append("\n");
                }

                for (Edge<?> edge : listGraph.getPath(from, to)) {
                    tempWeight = tempWeight + edge.getWeight();
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Message");
                alert.setHeaderText("The Path from " + from.getName() + " to " + to.getName() + ":");
                TextArea textArea = new TextArea();
                textArea.setText(builder + "\n" + "Total " + tempWeight);
                alert.getDialogPane().setContent(textArea);
                //alert.setContentText(listGraph.findPathString(from, to) + "\n" + "Total " + listGraph.getTotalWeight(from, to));
                alert.showAndWait();

            }
        }
    }

    class ChangeConnectionHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (selectedPlaces.size() < 2) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "You have to have two places selected");
                alert.showAndWait();
                return;
            }
            try {
                NewPlace from = selectedPlaces.get(0);
                NewPlace to = selectedPlaces.get(1);

                if (!listGraph.pathExists(from, to)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, from.getName() + " and " + to.getName() + " are not connected");
                    alert.showAndWait();
                    return;
                }

                ConnectionDialog dialog = new ConnectionDialog(from.getName(), to.getName());
                dialog.nameField.setText(listGraph.getEdgeBetween(from, to).getName());
                dialog.nameField.setEditable(false);
                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isPresent() && result.get() != ButtonType.OK)
                    return;

                int time = dialog.getTime();
                if (time < 0) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Time cannot be a negative integer");
                    alert.showAndWait();
                }

                listGraph.getEdgeBetween(from, to).setWeight(time);
                listGraph.getEdgeBetween(to, from).setWeight(time);
                String find1 = from.getName() + ";" + to.getName() + ";" + listGraph.getEdgeBetween(from,to).getName();
                String find2 = to.getName() + ";" + from.getName() + ";" + listGraph.getEdgeBetween(to,from).getName();
                String add1 = from.getName() + ";" + to.getName() + ";" + listGraph.getEdgeBetween(from,to).getName() + ";" + time + "\n";
                String add2 = to.getName() + ";" + from.getName() + ";" + listGraph.getEdgeBetween(from,to).getName() + ";" + time + "\n";

                for (String s: connections){
                    if(s.startsWith(find1)){
                        connections.set(connections.indexOf(s), add1);
                    }else if(s.startsWith(find2)){
                        connections.set(connections.indexOf(s),add2);
                    }
                }

                changed = true;
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Time has to be a number");
                alert.showAndWait();
            }
        }
    }


    class SaveImageHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            try {
                WritableImage writableImage = root.snapshot(null, null);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(bufferedImage, "png", new File("capture.png"));
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Fel!");
                alert.showAndWait();
            }
        }
    }

    class SaveItemHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            try {
                FileWriter fileWriter = new FileWriter("europa.graph");
                PrintWriter printWriter = new PrintWriter(fileWriter);
                printWriter.println(path);
                String temp = places.toString().replace("[", "").replace("]", "").replace(", ", "");
                int temp2 = temp.length();
                if(temp2>0){
                    printWriter.println(temp.substring(0, temp2 - 1));
                    printWriter.println(connections.toString().replace("[", "").replace("]", "").replace(", ", ""));
                }
                printWriter.close();
                changed = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class OpenItemHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            File file = new File("europa.graph");
            selectedPlaces.clear();
            if (changed) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Unsaved changes, continue anyway?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() != ButtonType.OK)
                    return;
            }
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line = bufferedReader.readLine();
                int rowNumber = 1;
                while (line != null && line.length() > 0) {
                    if (rowNumber == 1) {
                        handleGif(line);
                    } else if (rowNumber == 2) {
                        handleCities(line);
                    } else {
                        handleCords(line);
                    }
                    rowNumber++;
                    line = bufferedReader.readLine();
                }
            } catch (FileNotFoundException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "File not found!");
                alert.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleGif(String line) {
        path = line;
        changed = false;
    }

    private void handleCities(String line) {
        String[] citiesToCreate = line.split(";");

        if (citiesToCreate.length % 3 != 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "File error places!");
            alert.showAndWait();
        } else {
            createMap();
            int start = 0;
            int end = citiesToCreate.length;
            while (start < end) {
                String cityName = citiesToCreate[start++];
                double cordOne = Double.parseDouble(citiesToCreate[start++]);
                double cordTwo = Double.parseDouble(citiesToCreate[start++]);

                addPlace(cityName, cordOne, cordTwo);
                changed = false;
            }
        }
    }

    private void handleCords(String line) {
        String[] connectionsToCreate = line.split(";");
        if (connectionsToCreate.length % 4 != 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "File Error cords!");
            alert.showAndWait();
        } else {
            int start = 0;
            int end = connectionsToCreate.length;
            while (start < end) {
                String countryOne = connectionsToCreate[start++];
                String countryTwo = connectionsToCreate[start++];
                NewPlace one = findCountry(countryOne);
                NewPlace two = findCountry(countryTwo);
                String transport = connectionsToCreate[start++];
                int time = Integer.parseInt(connectionsToCreate[start++]);
                createConnection(one, two, transport, time);
                changed = false;
            }
        }
    }


    class NewMapHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (changed) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Unsaved changes, continue anyway?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() != ButtonType.OK)
                    return;

            }
            createMap();
        }
    }

    class ExitHandler implements EventHandler<WindowEvent> {
        @Override
        public void handle(WindowEvent event) {
            if (changed) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Unsaved changes, exit anyway?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() != ButtonType.OK) {
                    event.consume();
                }
            }
        }
    }

    class ExitProgramHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent event){
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        }
    }
}
