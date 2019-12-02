package com;

import androidx.annotation.Keep;

@Keep
public class ChildModel {
    public ChildModel(String id, String image, String desc, String name, String grade) {
        this.image = image;
        this.desc = desc;
        this.name = name;
        this.grade = grade;
        this.id = id;
    }

    public ChildModel() {
    }

    public String id = "",image, desc, name, grade;
}
