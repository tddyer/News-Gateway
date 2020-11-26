package com.example.newsgateway;

public class NewsSource {

    private String id;
    private String name;
    private String category;

    // constructors

    public NewsSource() {}

    public NewsSource(String id, String name, String category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    // getters + setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
