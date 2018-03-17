package com.example.bouda04.testlogindrive;

/**
 * Created by bouda04 on 14/3/2018.
 */

public class MyFile {
    String name;
    String path;

    public MyFile(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
