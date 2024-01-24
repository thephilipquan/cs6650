package org.philipquan.model;

public class SynchronizedInteger {

    private int value;

    public synchronized void setValue(int value) {
        this.value = value;
    }

    public synchronized int getValue() {
        return this.value;
    }
}