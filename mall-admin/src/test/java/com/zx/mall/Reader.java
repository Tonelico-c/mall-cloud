package com.zx.mall;

public class Reader implements Observer {
    private String name;
    public Reader(String name) {
        this.name = name;
    }
    @Override
    public void update(Subject subject) {
        String content = ((NewsPaper)subject).getContent();
        System.out.println(name + " received news: " + content);
    }

    @Override
    public void remove(Subject subject) {

    }
}
