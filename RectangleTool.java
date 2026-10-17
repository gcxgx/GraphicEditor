import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

/**
 * Narzędzie służace do tworzenia i rysowania prostokąta na Pane.
 */

public class RectangleTool implements IShapeTool {

    private final Pane drawingPane;
    private Rectangle tempRectangle;
    private double startX, startY;

    public RectangleTool(Pane drawingPane) {
        this.drawingPane = drawingPane;
    }
    //  tworzenie prostokąta 
    @Override
    public void onMousePressed(MouseEvent e) {
        startX = e.getX();
        startY = e.getY();

        tempRectangle = new Rectangle(startX, startY, 0, 0);
        tempRectangle.setStroke(Color.BLACK);
        tempRectangle.setFill(Color.TRANSPARENT);

        drawingPane.getChildren().add(tempRectangle);
    }

    @Override
    public void onMouseDragged(MouseEvent e) {
        if (tempRectangle == null) return;

        double x = e.getX();
        double y = e.getY();

        double offsetX = x - startX;
        double offsetY = y - startY;

        if (offsetX >= 0) {
            tempRectangle.setX(startX);
            tempRectangle.setWidth(offsetX);
        } else {
            tempRectangle.setX(x);
            tempRectangle.setWidth(-offsetX);
        }

        if (offsetY >= 0) {
            tempRectangle.setY(startY);
            tempRectangle.setHeight(offsetY);
        } else {
            tempRectangle.setY(y);
            tempRectangle.setHeight(-offsetY);
        }
    }

    @Override
    public void onMouseReleased(MouseEvent e) {
        tempRectangle = null;
    }
}