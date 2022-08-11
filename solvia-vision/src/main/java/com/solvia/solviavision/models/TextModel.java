package com.solvia.solviavision.models;

import com.google.cloud.vision.v1.BoundingPoly;
import lombok.Getter;

@Getter
public class TextModel {

    private String text;
    private int x1; // top left vertex
    private int y1;
    private int x2; // top right vertex
    private int y2;
    private int x3; // bottom right vertex
    private int y3;
    private int x4; // bottom left vertex
    private int y4;
    public TextModel (String text, BoundingPoly boundingPoly) {
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

    public int getTopY() {
        return (y2 + y1) / 2;
        // return Math.min(y1, y2);
    }

    public int getBottomY() {
        return (y4 + y3) / 2;
        // return Math.max(y3, y4);
    }

    public int getLeftX() {
        return (x1 + x4) / 2;
        // return Math.min(x1, x4);
    }

    public int getRightX() {
        return (x2 + x3) / 2;
        // return Math.max(x2, x3);
    }

    public int getNeighbourLimitY() {
        return getRangeY() / 2;
    }



    public boolean hasCorrectOrientation() {
        return getX3() > getX1() && getX2() > getX1() &&
                getY3() > getY1() && getY3() > getY2() &&
                (text.length() == 1 || getRangeY() < getRangeX() * 2);
    }
}
