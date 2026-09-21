package com.zx.mall;

public interface Observer {
    void update(Subject subject);
    void remove(Subject subject);
}
