package com.openwatchproject.launcher;

import android.graphics.drawable.Drawable;

import java.util.List;

public class ClockSkinItem {
    private int arrayType;
    private int range;
    private int centerX;
    private int centerY;
    private int color;
    private String colorArray;
    private int direction;
    private Drawable drawable;
    private int mulRotate;
    private String name;
    private double offsetAngle;
    private int rotate;
    private int startAngle;
    private int textSize;
    private int duration;
    private int count;
    private String valueType;
    private float progressDiliverArc;
    private int progressdiliverCount;
    private int progressRadius;
    private String progressStroken;
    private Drawable pictureShadow;
    private int framerate;
    private String className;
    private String packageName;
    private String childFolder;
    private List<Drawable> drawables;
    private float angle;
    private int width;
    private int radius;
    private int rotateMode;


    public ClockSkinItem() {
        this.name = null;
        this.centerX = 0;
        this.centerY = 0;
        this.rotate = 0;
        this.angle = 0;
        this.arrayType = 0;
        this.mulRotate = 1;
        this.color = 0;
        this.width = 5;
        this.radius = 50;
        this.startAngle = 0;
        this.direction = 1;
        this.textSize = 18;
        this.colorArray = null;
        this.rotateMode = 3;
    }

    public float getAngle() {
        return angle;
    }

    public int getArrayType() {
        return arrayType;
    }

    public void setArrayType(int arrayType) {
        this.arrayType = arrayType;
    }

    public List<Drawable> getDrawables() {
        return drawables;
    }

    public void setDrawables(List<Drawable> drawables) {
        this.drawables = drawables;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public int getCenterX() {
        return centerX;
    }

    public void setCenterX(int centerX) {
        this.centerX = centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public void setCenterY(int centerY) {
        this.centerY = centerY;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getColorArray() {
        return colorArray;
    }

    public void setColorArray(String colorArray) {
        this.colorArray = colorArray;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public int getMulRotate() {
        return mulRotate;
    }

    public void setMulRotate(int mulRotate) {
        this.mulRotate = mulRotate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getOffsetAngle() {
        return offsetAngle;
    }

    public void setOffsetAngle(double offsetAngle) {
        this.offsetAngle = offsetAngle;
    }

    public int getRotate() {
        return rotate;
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
    }

    public int getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(int startAngle) {
        this.startAngle = startAngle;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public float getProgressDiliverArc() {
        return progressDiliverArc;
    }

    public void setProgressDiliverArc(float progressDiliverArc) {
        this.progressDiliverArc = progressDiliverArc;
    }

    public int getProgressdiliverCount() {
        return progressdiliverCount;
    }

    public void setProgressdiliverCount(int progressdiliverCount) {
        this.progressdiliverCount = progressdiliverCount;
    }

    public int getProgressRadius() {
        return progressRadius;
    }

    public void setProgressRadius(int progressRadius) {
        this.progressRadius = progressRadius;
    }

    public String getProgressStroken() {
        return progressStroken;
    }

    public void setProgressStroken(String progressStroken) {
        this.progressStroken = progressStroken;
    }

    public Drawable getPictureShadow() {
        return pictureShadow;
    }

    public void setPictureShadow(Drawable pictureShadow) {
        this.pictureShadow = pictureShadow;
    }

    public int getFramerate() {
        return framerate;
    }

    public void setFramerate(int framerate) {
        this.framerate = framerate;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getChildFolder() {
        return childFolder;
    }

    public void setChildFolder(String childFolder) {
        this.childFolder = childFolder;
    }
}
