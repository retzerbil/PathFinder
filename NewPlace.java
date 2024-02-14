import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class NewPlace extends Circle {

    private Circle circle;
    private String name;
    private boolean selected;

    public NewPlace(String name, double x, double y) {
        circle = new Circle(10, 10, 10);
        setFill(Color.BLUE);
        setStroke(Color.BLACK);
        setRadius(15);
        setCenterX(x);
        setCenterY(y);

        this.name = name;
        selected = false;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName(){
        return name;
    }


    public void selectPlace(){
        this.selected = true;
        this.setFill(Color.RED);
    }

    public void deselectPlace(){
        this.selected = false;
        this.setFill(Color.BLUE);
    }

    public boolean isSelected(){
        return selected;
    }

}

