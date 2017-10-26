package com.yqc.forkJoin;

import java.util.ArrayList;
import java.util.List;

public class ProductListGenerator {
	public List<Production> generate(int size) {
		List<Production> ret = new ArrayList<Production>();
		for (int i = 0; i < size; i++) {
			Production production = new Production();
			production.setName("Product" + i);
			production.setPrice(10);
			ret.add(production);
		}
		return ret;
	}
}
