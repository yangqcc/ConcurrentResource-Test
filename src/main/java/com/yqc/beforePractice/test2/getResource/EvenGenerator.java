package com.yqc.beforePractice.test2.getResource;

public class EvenGenerator extends IntGenerator {

    private int currentEvenValue = 0;

    public static void main(String[] args) {
        EventChecker.test(new EvenGenerator());
    }

    @Override
    public int next() {
        ++currentEvenValue;
        ++currentEvenValue;
        return currentEvenValue;
    }

}
