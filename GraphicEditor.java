import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Edytor graficzny — aplikacja JavaFX do rysowania i edycji kształtów.
 */
public class GraphicEditor extends Application {

    private IShapeTool activeTool = null;

    /**
     * Punkt wejścia aplikacji JavaFX — buduje UI i ustawia eventy.
     *
     * @param stage główne okno
     */
    @Override
    public void start(Stage stage) {
        stage.setTitle("GraphicEditor");

        /* kontenery i kontent */
        VBox vbox = new VBox(30);
        HBox hbox = new HBox(8);
        BorderPane root = new BorderPane();
        Pane drawingPane = new Pane();

        Scene menuScene = new Scene(vbox, 1280, 720);
        Scene editorScene = new Scene(root, 1280, 720);

        MenuBar menuBar = new MenuBar();
        Menu menuDraw = new Menu("Rysuj");
        Region spacer = new Region();

        Button menuBtn = new Button("Rysuj");
        Button infoBtn = new Button("Info");
        Button backMenuBtn = new Button("Powrót");
        Button insBtn = new Button("Instrukcja");

        MenuItem drawItemCircle = new MenuItem("Okrąg");
        MenuItem drawItemRect = new MenuItem("Prostokąt");
        MenuItem drawItemPoly = new MenuItem("Wielokąt");

        /* scena 1 – menu */
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: lightyellow;");
        vbox.getChildren().addAll(menuBtn, insBtn, infoBtn);

        menuBtn.setOnAction(e -> stage.setScene(editorScene));
        menuBtn.setPrefSize(200, 75);

        infoBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Informacje");
            alert.setContentText("Nazwa: GraphicEditor\n" +
                    "Przeznaczenie: To edytor graficzny służący do rysowania okręgów, prostokątów i wielokątów oraz do ich edycji.\n" +
                    "Autor: gxcgx");
            alert.showAndWait();
        });
        infoBtn.setPrefSize(200, 75);

        insBtn.setPrefSize(200, 75);
        insBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Instrukcja");
            alert.setContentText(
                    "Aby narysować okrąg lub prostokąt wybierz 'Rysuj' z paska górnego, a następnie przeciągnij mysz w wybranym miejscu. " +
                    "Aby narysować wielokąt kliknij w wybrane punkty, które mają być jego wierzchołkami, aby finalizować wielokąt naciśnij klawisz R.\n\n" +
                    "Aby wczytać figurę należy zapisać ją wcześniej w formacie .json. Kliknij na figurę aby oznaczyć ją jako aktywną. Przesuń ją przeciągając, zmień kolor klikając PPM lub " +
                    "zmień rozmiar używając scrolla.\nMożesz także obrócić figurę używając opcji z paska górnego.");
            alert.showAndWait();
        });

        /* scena 2 – edytor */
        root.setTop(hbox);
        root.setCenter(drawingPane);

        HBox.setHgrow(spacer, Priority.ALWAYS);

        menuDraw.getItems().addAll(drawItemCircle, drawItemRect, drawItemPoly);
        menuBar.getMenus().add(menuDraw);

        backMenuBtn.setOnAction(e -> stage.setScene(menuScene));

        /* clip do rysowania */
        Rectangle clip = new Rectangle();
        drawingPane.setClip(clip);
        drawingPane.layoutBoundsProperty().addListener((obs, oldB, newB) -> {
            clip.setWidth(newB.getWidth());
            clip.setHeight(newB.getHeight());
        });

        /* narzędzia */
        CircleTool circleTool = new CircleTool(drawingPane);
        RectangleTool rectangleTool = new RectangleTool(drawingPane);
        PolygonTool polygonTool = new PolygonTool(drawingPane);

        drawItemCircle.setOnAction(e -> activeTool = circleTool);
        drawItemRect.setOnAction(e -> activeTool = rectangleTool);
        drawItemPoly.setOnAction(e -> activeTool = polygonTool);

        /* Selection manager */
        SelectionManager selection = new SelectionManager(drawingPane);

        Button rotateLeft = new Button("Obróć -15st");
        Button rotateRight = new Button("Obróć +15st");
        Button saveBtn = new Button("Zapisz");
        Button loadBtn = new Button("Wczytaj");

        hbox.getChildren().addAll(menuBar, saveBtn, loadBtn, rotateLeft, rotateRight, spacer, backMenuBtn);

        rotateLeft.setOnAction(e -> selection.rotateActive(-15));
        rotateRight.setOnAction(e -> selection.rotateActive(15));

        saveBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pliki JSON", "*.json"));
            File f = fc.showSaveDialog(stage);
            if (f != null) {
                try {
                    selection.saveToFile(f);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        loadBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pliki JSON", "*.json"));
            File f = fc.showOpenDialog(stage);
            if (f != null) {
                try {
                    selection.loadFromFile(f);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        drawItemPoly.setOnAction(e -> {
            activeTool = polygonTool;
            drawingPane.requestFocus();
        });

        editorScene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (activeTool instanceof PolygonTool) {
                polygonTool.onKeyPressed(e);
                activeTool = null;
            }
        });

        /* eventy myszki */
        drawingPane.setOnMousePressed(e -> {
            if (activeTool != null) activeTool.onMousePressed(e);
        });

        drawingPane.setOnMouseDragged(e -> {
            if (activeTool != null) activeTool.onMouseDragged(e);
        });

        drawingPane.setOnMouseReleased(e -> {
            if (activeTool != null) activeTool.onMouseReleased(e);
            if (!(activeTool instanceof PolygonTool)) {
                activeTool = null;
            }
        });

        stage.setScene(menuScene);
        stage.show();
    }

    /**
     * Uruchamia aplikację.
     *
     * @param args argumenty wiersza poleceń
     */
    public static void main(String[] args) {
        launch(args);
    }
}