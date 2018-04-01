package com.CodeEngine.ThinhDinh.timecloud.Model;


import java.io.Serializable;

/**
 * Created by Administrator on 2/26/2018.
 */

public class TaskModel  implements Serializable {
    private int id;
    private String name;
    private TimeModel time;
    private CategoryModel category;

    public TaskModel(int id, String name, TimeModel time, CategoryModel category) {

        this.id = id;
        this.name = name;
        this.time = time;
        this.category = category;
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

    public TimeModel getTime() {
        return time;
    }

    public void setTime(TimeModel time) {
        this.time = time;
    }

    public CategoryModel getCategory() {
        return category;
    }

    public void setCategory(CategoryModel category) {
        this.category = category;
    }

    public String getProjectCategory(){
        return (category==null) ? "No project - No category" : category.getProject().getName() + " - " + category.getName();
    }
}
