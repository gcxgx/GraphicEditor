import javafx.scene.input.MouseEvent;

public interface IShapeTool {

    void onMousePressed(MouseEvent event);

    void onMouseDragged(MouseEvent event);

    void onMouseReleased(MouseEvent event);

}