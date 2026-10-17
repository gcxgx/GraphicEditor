import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Zarządza zaznaczaniem, przesuwaniem, skalowaniem i zapisem/wczytywaniem kształtów
 * znajdujących się w zadanym Pane.
 */
public class SelectionManager {

    private final Pane pane;
    private Node active;
    private double dragStartX, dragStartY;
    private Translate translate = new Translate();
    private Rotate rotate = new Rotate(0, 0, 0);
    private Scale scale = new Scale(1, 1, 0, 0);
    private double ZOOM_FACTOR = 1.1;

    /**
     * Tworzy menedżera zaznaczeń dla danego Pane i rejestruje eventy myszy/scroll.
     *
     * @param pane Pane zawierające kształty
     */
    public SelectionManager(Pane pane) {
        this.pane = pane;
        pane.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        pane.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
        pane.addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);

        pane.setOnScroll(e -> {
            if (active == null) return;

            double factor = e.getDeltaY() > 0 ? ZOOM_FACTOR : 1 / ZOOM_FACTOR;

            active.setScaleX(active.getScaleX() * factor);
            active.setScaleY(active.getScaleY() * factor);

            e.consume();
        });
    }

    /* usuwanie i dodawanie nowych transformacji */
    private void applyTransforms() {
        if (active == null) return;
        active.getTransforms().removeAll(translate, rotate, scale);
        active.getTransforms().addAll(translate, rotate, scale);
    }

    /* obsługa naciśnięcia myszy (zaznaczanie, kontekstowe menu, inicjalizacja drag) */
    private void onMousePressed(MouseEvent e) {
        if (e.getButton() == MouseButton.SECONDARY) {
            if (active != null) {
                ContextMenu cm = new ContextMenu();
                MenuItem colorItem = new MenuItem("Zmień kolor wypełnienia...");
                ColorPicker cp = new ColorPicker();
                cp.setOnAction(a -> {
                    if (active instanceof Shape) {
                        ((Shape) active).setFill(cp.getValue());
                    }
                });
                // wybór koloru z Dialog
                colorItem.setOnAction(a -> {
                    ColorPicker dialogCp = new ColorPicker();
                    Dialog<ButtonType> d = new Dialog<>();
                    d.getDialogPane().setContent(dialogCp);
                    d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                    if (d.showAndWait().filter(bt -> bt == ButtonType.OK).isPresent()) {
                        if (active instanceof Shape) ((Shape) active).setFill(dialogCp.getValue());
                    }
                });
                cm.getItems().add(colorItem);
                cm.show(pane, e.getScreenX(), e.getScreenY());
            }
            return;
        }

        /* zaznaczanie aktywnej figury */
        Node target = e.getPickResult().getIntersectedNode();
        if (target == pane || target == null) {
            setActive(null);
            return;
        }
        while (target != null && !(target instanceof Shape)) {
            target = target.getParent();
        }
        setActive(target);

        /* przesuwanie obiektu - inicjalizacja transformacji */
        if (active != null) {
            dragStartX = e.getSceneX();
            dragStartY = e.getSceneY();

            Bounds b = active.getBoundsInLocal();
            double pivotX = b.getMinX() + b.getWidth() / 2.0;
            double pivotY = b.getMinY() + b.getHeight() / 2.0;

            translate = new Translate(0, 0);
            rotate = new Rotate(0, pivotX, pivotY);
            scale = new Scale(1, 1, pivotX, pivotY);

            applyTransforms();
        }
    }

    private void onMouseDragged(MouseEvent e) {
        if (active == null) return;
        double dx = e.getSceneX() - dragStartX;
        double dy = e.getSceneY() - dragStartY;
        translate.setX(dx);
        translate.setY(dy);
        applyTransforms();
    }

    private void onMouseReleased(MouseEvent e) {
        if (active == null) return;
        active.setLayoutX(active.getLayoutX() + translate.getX());
        active.setLayoutY(active.getLayoutY() + translate.getY());
        active.getTransforms().clear();
        translate = new Translate();
        rotate = new Rotate(rotate.getAngle(), rotate.getPivotX(), rotate.getPivotY());
        scale = new Scale(scale.getX(), scale.getY(), scale.getPivotX(), scale.getPivotY());
        applyTransforms();
    }

    /**
     * Obraca aktualnie zaznaczony węzeł o zadany kąt (w stopniach).
     *
     * @param degrees kąt w stopniach (dodatni = zgodnie z ruchem wskazówek zegara)
     */
    public void rotateActive(double degrees) {
        if (active == null) return;
        rotate = new Rotate(degrees + rotate.getAngle(),
                active.getBoundsInLocal().getWidth() / 2,
                active.getBoundsInLocal().getHeight() / 2);
        applyTransforms();
    }

    /**
     * Ustawia aktywny węzeł (zaznaczenie). Zmienia obrys dla wizualnego efektu.
     *
     * @param node nowy aktywny węzeł lub null
     */
    public void setActive(Node node) {
        if (active != null) {
            if (active instanceof Shape) {
                ((Shape) active).setStrokeWidth(1);
                ((Shape) active).setStroke(Color.BLACK);
            }
        }
        active = node;
        if (active != null) {
            if (active instanceof Shape) {
                ((Shape) active).setStrokeWidth(3);
                ((Shape) active).setStroke(Color.RED);
            }
        }
    }

    /**
     * Zapisuje wszystkie kształty z pane do pliku JSON.
     *
     * @param file plik docelowy
     * @throws IOException przy błędzie zapisu
     */
    public void saveToFile(File file) throws IOException {
        JSONArray arr = new JSONArray();
        for (Node n : pane.getChildren()) {
            if (!(n instanceof Shape)) continue;
            JSONObject obj = new JSONObject();
            Shape s = (Shape) n;
            if (s instanceof javafx.scene.shape.Circle) {
                javafx.scene.shape.Circle c = (javafx.scene.shape.Circle) s;
                obj.put("type", "circle");
                obj.put("cx", c.getCenterX());
                obj.put("cy", c.getCenterY());
                obj.put("r", c.getRadius());
            } else if (s instanceof javafx.scene.shape.Rectangle) {
                javafx.scene.shape.Rectangle r = (javafx.scene.shape.Rectangle) s;
                obj.put("type", "rect");
                obj.put("x", r.getX());
                obj.put("y", r.getY());
                obj.put("w", r.getWidth());
                obj.put("h", r.getHeight());
            } else if (s instanceof javafx.scene.shape.Polygon) {
                javafx.scene.shape.Polygon p = (javafx.scene.shape.Polygon) s;
                obj.put("type", "poly");
                JSONArray ps = new JSONArray();
                for (Double d : p.getPoints()) ps.put(d);
                obj.put("points", ps);
            } else continue;
            if (s.getFill() instanceof Color) {
                Color c = (Color) s.getFill();
                obj.put("fill", c.toString());
            }
            obj.put("layoutX", n.getLayoutX());
            obj.put("layoutY", n.getLayoutY());
            arr.put(obj);
        }
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(arr.toString(2));
        }
    }

    /**
     * Wczytuje kształty z pliku JSON i dodaje je do pane.
     *
     * @param file plik źródłowy
     * @throws IOException przy błędzie odczytu/parsing
     */
    public void loadFromFile(File file) throws IOException {
        String txt;
        try (FileReader fr = new FileReader(file); Scanner sc = new Scanner(fr)) {
            StringBuilder sb = new StringBuilder();
            while (sc.hasNextLine()) sb.append(sc.nextLine());
            txt = sb.toString();
        }
        JSONArray arr = new JSONArray(txt);
        pane.getChildren().clear();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            String type = obj.getString("type");
            Shape s = null;
            if ("circle".equals(type)) {
                javafx.scene.shape.Circle c = new javafx.scene.shape.Circle(obj.getDouble("cx"), obj.getDouble("cy"), obj.getDouble("r"));
                s = c;
            } else if ("rect".equals(type)) {
                javafx.scene.shape.Rectangle r = new javafx.scene.shape.Rectangle(obj.getDouble("x"), obj.getDouble("y"), obj.getDouble("w"), obj.getDouble("h"));
                s = r;
            } else if ("poly".equals(type)) {
                javafx.scene.shape.Polygon p = new javafx.scene.shape.Polygon();
                JSONArray ps = obj.getJSONArray("points");
                for (int j = 0; j < ps.length(); j++) p.getPoints().add(ps.getDouble(j));
                s = p;
            }
            if (s != null) {
                if (obj.has("fill")) {
                    s.setFill(Color.valueOf(obj.getString("fill")));
                } else {
                    s.setFill(Color.TRANSPARENT);
                }
                s.setStroke(Color.BLACK);
                s.setStrokeWidth(1);
                s.setLayoutX(obj.optDouble("layoutX", 0));
                s.setLayoutY(obj.optDouble("layoutY", 0));
                pane.getChildren().add(s);
            }
        }
    }
}