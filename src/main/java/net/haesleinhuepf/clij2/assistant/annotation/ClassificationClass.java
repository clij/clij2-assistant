package net.haesleinhuepf.clij2.assistant.annotation;

import java.awt.*;

public class ClassificationClass {
    private int identifier;
    private String name;
    private Color color;

    public ClassificationClass(int identifier) {
        name = "" + identifier;
        this.identifier = identifier;
        this.color = getColor(identifier);
    }

    public static Color getColor(int c) {
        switch((c - 1) % 10) {
            case 0:
                return Color.green;
            case 1:
                return Color.magenta;
            case 2:
                return Color.cyan;
            case 3:
                return Color.yellow;
            case 4:
                return Color.red;
            case 5:
                return Color.blue;
            case 6:
                return Color.gray;
            case 7:
                return Color.orange;
            case 8:
                return Color.pink;
            case 9:
                return Color.lightGray;
            default:
                return Color.white;
        }
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Color getColor() {
        return color;
    }
    public void setColor(Color color) {
        this.color = color;
    }

    public int getIdentifier() {
        return identifier;
    }
}
