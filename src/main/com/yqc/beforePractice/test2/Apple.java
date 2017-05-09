package com.yqc.beforePractice.test2;

import java.util.HashSet;
import java.util.Set;

public class Apple {
	private Set<Apple> apples = new HashSet<Apple>();
	private int id;
	private Apple parent;

	public void setApples(Set<Apple> apples) {
		this.apples = apples;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Apple getParent() {
		return parent;
	}

	public void setParent(Apple parent) {
		this.parent = parent;
	}

	public Set<Apple> getApples() {
		return apples;
	}
	
}
