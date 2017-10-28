package com.yqc.forkJoin.saleproduct;

import java.util.List;
import java.util.concurrent.RecursiveAction;

/**
 * Created by yangqc on 2017/10/28
 */
public class Task extends RecursiveAction {

    private static final long serialVersionUID = 1L;
    private List<Product> products;
    private int first;
    private int last;
    private double increasement;

    public Task(List<Product> products, int first, int last, double increasement) {
        this.products = products;
        this.first = first;
        this.last = last;
        this.increasement = increasement;
    }

    @Override
    protected void compute() {
        if (last - first < 10) {
            updatePrices();
        } else {
            int middle = (first + last) / 2;
            System.out.println("Thread Name:" + Thread.currentThread().getName());
            Task task1 = new Task(products, first, middle, increasement);
            Task task2 = new Task(products, middle + 1, last, increasement);
            this.invokeAll(task1, task2);
        }
    }

    private void updatePrices() {
        for (int i = first; i < last; i++) {
            Product product = products.get(i);
            product.setPrice(product.getPrice() * (1 + increasement));
        }
    }
}
