package com.github.xiaogegechen.fineviews;

public class Item {

    private int iconId;
    private int normalColor;
    private int clickedColor;

    public Item(int iconId, int normalColor, int clickedColor) {
        this.iconId = iconId;
        this.normalColor = normalColor;
        this.clickedColor = clickedColor;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public int getNormalColor() {
        return normalColor;
    }

    public void setNormalColor(int normalColor) {
        this.normalColor = normalColor;
    }

    public int getClickedColor() {
        return clickedColor;
    }

    public void setClickedColor(int clickedColor) {
        this.clickedColor = clickedColor;
    }
}
