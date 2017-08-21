package com.example.patil.tabtest;

/**
 * Created by patil on 5/20/2017.
 */

public class ClosetItem {

    private int id;
    private String type;
    private String color;
    private byte[] imageSource;
    private int fav;

    public ClosetItem()
    {
    }
    public ClosetItem(String type,String color,byte[] imageSource,int fav)
    {
        //this.id = id;
        this.type = type;
        this.color = color;
        this.imageSource = imageSource;
        this.fav = fav;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public byte[] getImageSource() {
        return imageSource;
    }

    public void setImageSource(byte[] imageSource) {
        this.imageSource = imageSource;
    }

    public int getFav() {
        return fav;
    }

    public void setFav(int fav) {
        this.fav = fav;
    }
}
