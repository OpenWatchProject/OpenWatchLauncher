package com.openwatchproject.launcher.model;

import android.graphics.drawable.Drawable;

import java.util.List;

public class ClockInfo {
    private String angle;
    private String arraytype;
    private String centerX;
    private String centerY;
    private String color;
    private String colorArray;
    private String direction;
    private String mulrotate;
    private String name;
    private Drawable namepng;
    private List<Num> nums;
    private String radius;
    private String rotate;
    private String rotatemode;
    private String startAngle;
    private String textcolor;
    private String textsize;
    private String width;

    public class Num {
        private Drawable numDrawable;

        public Num() {
        }

        public Drawable getNumDrawable() {
            return this.numDrawable;
        }

        public void setNumDrawable(Drawable numDrawable2) {
            this.numDrawable = numDrawable2;
        }
    }

    public String getAngle() {
        return angle;
    }

    public void setAngle(String angle) {
        this.angle = angle;
    }

    public String getArraytype() {
        return arraytype;
    }

    public void setArraytype(String arraytype) {
        this.arraytype = arraytype;
    }

    public String getCenterX() {
        return centerX;
    }

    public void setCenterX(String centerX) {
        this.centerX = centerX;
    }

    public String getCenterY() {
        return centerY;
    }

    public void setCenterY(String centerY) {
        this.centerY = centerY;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getColorArray() {
        return colorArray;
    }

    public void setColorArray(String colorArray) {
        this.colorArray = colorArray;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getMulrotate() {
        return mulrotate;
    }

    public void setMulrotate(String mulrotate) {
        this.mulrotate = mulrotate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getNamepng() {
        return namepng;
    }

    public void setNamepng(Drawable namepng) {
        this.namepng = namepng;
    }

    public List<Num> getNums() {
        return nums;
    }

    public void setNums(List<Num> nums) {
        this.nums = nums;
    }

    public String getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }

    public String getRotate() {
        return rotate;
    }

    public void setRotate(String rotate) {
        this.rotate = rotate;
    }

    public String getRotatemode() {
        return rotatemode;
    }

    public void setRotatemode(String rotatemode) {
        this.rotatemode = rotatemode;
    }

    public String getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(String startAngle) {
        this.startAngle = startAngle;
    }

    public String getTextcolor() {
        return textcolor;
    }

    public void setTextcolor(String textcolor) {
        this.textcolor = textcolor;
    }

    public String getTextsize() {
        return textsize;
    }

    public void setTextsize(String textsize) {
        this.textsize = textsize;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }
}
