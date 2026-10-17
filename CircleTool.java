import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

/**
 * Narzędzie służace do tworzenia i rysowania prostokąta na Pane.
 */
public class CircleTool implements IShapeTool {

    private final Pane drawingPane;
    private Circle tempCircle;
    private double startX, startY;

    public CircleTool(Pane drawingPane) {
        this.drawingPane = drawingPane;
    }
    //  tworzenie okręgu 
    @Override
    public void onMousePressed(MouseEvent e) {
        startX = e.getX();
        startY = e.getY();

        tempCircle = new Circle(startX, startY, 0);
        tempCircle.setFill(Color.TRANSPARENT);
        tempCircle.setStroke(Color.BLACK);

        drawingPane.getChildren().add(tempCircle);
    }

    @Override
    public void onMouseDragged(MouseEvent e) {
        if (tempCircle == null) return;

        double radius = Math.hypot(e.getX() - startX, e.getY() - startY);
        tempCircle.setRadius(radius);
    }

    @Override
    public void onMouseReleased(MouseEvent e) {
        tempCircle = null;
    }
}