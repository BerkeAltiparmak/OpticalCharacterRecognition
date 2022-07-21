package org.example;

import com.google.cloud.vision.v1.BoundingPoly;

public class WordText {
    private String text;
    private int x1; // top left vertex
    private int y1;
    private int x2; // top right vertex
    private int y2;
    private int x3; // bottom right vertex
    private int y3;
    private int x4; // bottom left vertex
    private int y4;
    public WordText(String text, BoundingPoly boundingPoly) {
        this.text = text;
        x1 = boundingPoly.getVertices(0).getX();
        y1 = boundingPoly.getVertices(0).getY();
        x2 = boundingPoly.getVertices(1).getX();
        y2 = boundingPoly.getVertices(1).getY();
        x3 = boundingPoly.getVertices(2).getX();
        y3 = boundingPoly.getVertices(2).getY();
        x4 = boundingPoly.getVertices(3).getX();
        y4 = boundingPoly.getVertices(3).getY();
    }

    public String getText() {
        return text;
    }

    public int getRangeY() {
        return ((y4 - y1) + (y3 - y2)) / 2;
    }

    public int getRangeX() {
        return ((x2 - x1) + (x3 - x4)) / 2;
    }

    public int getCenterY() {
        return getRangeY() / 2 + y1;
    }

    public int getCenterX() {
        return getRangeX() / 2 + x1;
    }

    public int getNeighbourLimitY() {
        return getRangeY() / 2;
    }

    public int getX1() {
        return x1;
    }

    public int getX2() {
        return x2;
    }

    public int getX3() {
        return x3;
    }

    public int getX4() {
        return x4;
    }

    public int getY1() {
        return y1;
    }

    public int getY2() {
        return y2;
    }

    public int getY3() {
        return y3;
    }

    public int getY4() {
        return y4;
    }
}
