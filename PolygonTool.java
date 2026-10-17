import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 * Narzędzie służace do tworzenia i rysowania wielokąta na Pane.
 */
public class PolygonTool implements IShapeTool {

    private final Pane drawingPane;
    private Polygon currentPolygon;
    private boolean polygonStarted = false;

    public PolygonTool(Pane drawingPane) {
        this.drawingPane = drawingPane;
        this.drawingPane.setFocusTraversable(true);
    }
    //  tworzenie wielokąta
    @Override
    public void onMousePressed(MouseEvent e) {
        if (!e.isPrimaryButtonDown()) {
            return;
        }

        double x = e.getX();
        double y = e.getY();

        if (!polygonStarted) {
            currentPolygon = new Polygon();
            currentPolygon.setStroke(Color.BLACK);
            currentPolygon.setFill(Color.TRANSPARENT);
            drawingPane.getChildren().add(currentPolygon);
            polygonStarted = true;
        }

        currentPolygon.getPoints().addAll(x, y);
    }
    // finalizowanie wielokąta za pomocą klawisza 'R'
    public void onKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.R) {
            finishPolygon();
        }
    }

    private void finishPolygon() {
        currentPolygon = null;
        polygonStarted = false;
    }

    @Override
    public void onMouseDragged(MouseEvent e) {}
    @Override
    public void onMouseReleased(MouseEvent e) {}
}