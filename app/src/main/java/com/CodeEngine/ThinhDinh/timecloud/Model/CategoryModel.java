package com.CodeEngine.ThinhDinh.timecloud.Model;

import java.io.Serializable;

/**
 * Created by Administrator on 2/26/2018.
 */

public class CategoryModel implements Serializable {
    private int id;
    private String name;
    private ProjectModel project;

    public CategoryModel(int id, String name, ProjectModel project) {
        this.id = id;
        this.name = name;
        this.project = project;
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

    public ProjectModel getProject() {
        return project;
    }

    public void setProject(ProjectModel project) {
        this.project = project;
    }
}
