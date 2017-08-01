package com.bielang.customserver.bean;


public class Knowledge {
    private String category;
    private String content;
    private String title;

    public Knowledge(String category, String content, String title) {
        this.category = category;
        this.content = content;
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
