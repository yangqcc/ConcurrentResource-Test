package com.yqc.forkJoin.saleproduct;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangqc on 2017/10/28
 */
public class ProductListGenerator {

    public List<Product> generate(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("参数不能小于0!");
        }
        List<Product> ret = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Product product = new Product();
            product.setName("Product " + i);
            product.setPrice(10);
            ret.add(product);
        }
        return ret;
    }
}
