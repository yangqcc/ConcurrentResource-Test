package com.yqc.forkJoin.cancel;

import java.util.Random;

/**
 * Created by yangqc on 2017/10/29
 */
public class ArrayGenerator {

    public int[] generator(int size) {
        int[] array = new int[size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(10);
        }
        return array;
    }
}
