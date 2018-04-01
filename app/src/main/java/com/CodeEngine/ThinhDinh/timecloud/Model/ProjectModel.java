package com.CodeEngine.ThinhDinh.timecloud.Model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Administrator on 2/13/2018.
 */

public class ProjectModel implements Serializable{
    private int id;
    private String name;
    private String color;
    private ArrayList<CategoryModel> categories;

    public ProjectModel(int id, String name, String color, ArrayList<CategoryModel> categories) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.categories = categories;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public ArrayList<CategoryModel> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<CategoryModel> categories) {
        this.categories = categories;
    }
}
