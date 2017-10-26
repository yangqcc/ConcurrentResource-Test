package com.yqc.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class Pair { // 非线程安全
    private int x, y;

    public Pair(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Pair() {
        this(0, 0);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void incrementX() {
        x++;
    }

    public void incrementY() {
        y++;
    }

    public String toString() {
        return "x: " + x + ",y: " + y;
    }

    public void checkState() {
        if (x != y) {
            throw new PairValuesNotEqualException();
        }
    }

    public class PairValuesNotEqualException extends RuntimeException {
        private static final long serialVersionUID = -981102967037233361L;

        public PairValuesNotEqualException() {
            super("Pair values not equal: " + Pair.this);
        }
    }
}

abstract class PairManager {
    protected Pair p = new Pair();
    AtomicInteger checkCounter = new AtomicInteger(0);
    private List<Pair> storage = Collections.synchronizedList(new ArrayList<Pair>());

    public synchronized Pair getPair() {
        return new Pair(p.getX(), p.getY());
    }

    protected void store(Pair p) {
        storage.add(p);
        try {
            TimeUnit.MILLISECONDS.sleep(50);
        } catch (Exception e) {
        }
    }

    public abstract void increment();
}

class PairManager1 extends PairManager {
    public synchronized void increment() {
        p.incrementX();
        p.incrementY();
        store(getPair());
    }
}

class PairManager2 extends PairManager {
    public void increment() {
        Pair temp;
        synchronized (this) {
            p.incrementX();
            p.incrementY();
            temp = getPair();
        }
        store(temp);
    }
}

class PairMainPulator implements Runnable {
    private PairManager pm;

    public PairMainPulator(PairManager pm) {
        this.pm = pm;
    }

    @Override
    public void run() {
        while (true) {
            pm.increment();
        }
    }

    public String toString() {
        return "Pair: " + pm.getPair() + " checkCounter= " + pm.checkCounter.get();
    }
}

class PairChecker implements Runnable {
    private PairManager pm;

    public PairChecker(PairManager pm) {
        this.pm = pm;
    }

    @Override
    public void run() {
        while (true) {
            pm.checkCounter.incrementAndGet();
            pm.getPair().checkState();
        }
    }
}

public class CriticalSection {
    static void testApproaches(PairManager pman1, PairManager pman2) {
        ExecutorService exec = Executors.newCachedThreadPool();
        PairMainPulator pm1 = new PairMainPulator(pman1), pm2 = new PairMainPulator(pman2);
        PairChecker pcheck1 = new PairChecker(pman1), peheck2 = new PairChecker(pman2);
        exec.execute(pm1);
        exec.execute(pm2);
        exec.execute(pcheck1);
        exec.execute(peheck2);
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (Exception e) {
            System.out.println("Sleep interrupted");
        }
        System.out.println("pm1: " + pm1 + "\npm2: " + pm2);
        System.exit(0);
    }

    public static void main(String[] args) {
        PairManager pman1 = new PairManager1(), pman2 = new PairManager2();
        testApproaches(pman1, pman2);
    }
}