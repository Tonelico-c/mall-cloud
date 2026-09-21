package com.zx.mall;

public class Demo {
    public static void main(String[] args) {
        //创建一个报纸，作为目标
        NewsPaper subject = new NewsPaper();
        // 创建一个读者
        Reader reader1 = new Reader("张三");
        Reader reader2= new Reader("李四");
        Reader reader3 = new Reader("王五");
        // 注册订阅者
        subject.attach(reader1);
        subject.attach(reader2);
        subject.attach(reader3);
        // 报纸内容更新
        subject.setContent("本期内容是观察者模式");
    }
}
