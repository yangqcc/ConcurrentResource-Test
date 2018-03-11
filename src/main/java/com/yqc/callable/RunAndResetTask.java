package com.yqc.callable;

import util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import util.concurrent.FutureTask;

/**
 * @author yangqc
 * @date 2017/10/30
 */
public class RunAndResetTask extends FutureTask {

    public RunAndResetTask(Callable callable) {
        super(callable);
    }

    /**
     * 调用了父类的{@link FutureTask#runAndReset}方法
     */
    public boolean runReset() {
        return super.runAndReset();
    }
}

class TestMain2 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CalculateTask oneTask = new CalculateTask(10);
        RunAndResetTask rt = new RunAndResetTask(oneTask);
        new Thread(rt).start();
        System.out.println(rt.get());
        System.out.println("reset!");
        if (rt.runReset()) {
            System.out.println(rt.get());
        } else {
            System.out.println("重启失败!");
        }
    }
}
