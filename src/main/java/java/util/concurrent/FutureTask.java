/*
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

import java.util.concurrent.locks.LockSupport;

/**
 * 结果只能咋计算完成后才回返回，如果计算还没有完成，那么get()方法将会 阻塞，一旦计算完成，不能再次计算，或者取消任务 A cancellable
 * asynchronous computation. This class provides a base implementation of
 * {@link Future}, with methods to start and cancel a computation, query to see
 * if the computation is complete, and retrieve the result of the computation.
 * The result can only be retrieved when the computation has completed; the
 * {@code get} methods will block if the computation has not yet completed. Once
 * the computation has completed, the computation cannot be restarted or
 * cancelled (unless the computation is invoked using {@link #runAndReset}).
 * <p>
 * <p>
 * A {@code FutureTask} can be used to wrap a {@link Callable} or
 * {@link Runnable} object. Because {@code FutureTask} implements
 * {@code Runnable}, a {@code FutureTask} can be submitted to an
 * {@link Executor} for execution.
 * <p>
 * <p>
 * In addition to serving as a standalone class, this class provides
 * {@code protected} functionality that may be useful when creating customized
 * task classes.
 *
 * @param <V> The result type returned by this FutureTask's {@code get} methods
 * @author Doug Lea
 * @since 1.5
 */
public class FutureTask<V> implements RunnableFuture<V> {
    /*
	 * Revision notes: This differs from previous versions of this class that
	 * relied on AbstractQueuedSynchronizer, mainly to avoid surprising users
	 * about retaining interrupt status during cancellation races. Sync control
	 * in the current design relies on a "state" field updated via CAS to track
	 * completion, along with a simple Treiber stack to hold waiting threads.
	 *
	 * Style note: As usual, we bypass overhead of using AtomicXFieldUpdaters
	 * and instead directly use Unsafe intrinsics.
	 */

    private static final int NEW = 0;
    /**
     * 计算已完成，但是还未设置值
     */
    private static final int COMPLETING = 1;
    /**
     * 计算执行完毕，会在set方法中，将状态从COMPLETING改为NORMAL
     */
    private static final int NORMAL = 2;
    private static final int EXCEPTIONAL = 3;
    /**
     * 被取消，调用cancle(boolean)将状态改为CANCELLED，会根据此状态判断能否从
     * get()获取值
     */
    private static final int CANCELLED = 4;
    /**
     * 被中断，调用cancle(boolean)，如果当前线程还未执行，将状态改为INTERRPUTING
     */
    private static final int INTERRUPTING = 5;
    /**
     * 被中断，如果当前线程已经执行，则中断此线程，将状态位设置为INTERRUPTED
     */
    private static final int INTERRUPTED = 6;
    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long stateOffset;
    private static final long runnerOffset;
    private static final long waitersOffset;

    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = FutureTask.class;
            stateOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("state"));
            runnerOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("runner"));
            waitersOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("waiters"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * The run state of this task, initially NEW. The run state transitions to a
     * terminal state only in methods set, setException, and cancel. During
     * completion, state may take on transient values of COMPLETING (while
     * outcome is being set) or INTERRUPTING (only while interrupting the runner
     * to satisfy a cancel(true)). Transitions from these intermediate to final
     * states use cheaper ordered/lazy writes because values are unique and
     * cannot be further modified.
     * <p>
     * 可能出现的状态过程
     * Possible state transitions: NEW -> COMPLETING -> NORMAL
     * NEW -> COMPLETING-> EXCEPTIONAL
     * NEW -> CANCELLED
     * NEW -> INTERRUPTING -> INTERRUPTED
     */
    private volatile int state;
    /**
     * The underlying callable; nulled out after running
     */
    private Callable<V> callable;
    /**
     * The result to return or exception to throw from get()
     */
    private Object outcome; // non-volatile, protected by state reads/writes
    /**
     * The thread running the callable; CASed during run()
     */
    private volatile Thread runner;
    /**
     * Treiber stack of waiting threads
     */
    private volatile WaitNode waiters;

    /**
     * Creates a {@code FutureTask} that will, upon running, execute the given
     * {@code Callable}.
     *
     * @param callable the callable task
     * @throws NullPointerException if the callable is null
     */
    public FutureTask(Callable<V> callable) {
        if (callable == null)
            throw new NullPointerException();
        this.callable = callable;
        this.state = NEW; // ensure visibility of callable
    }

    /**
     * Creates a {@code FutureTask} that will, upon running, execute the given
     * {@code Runnable}, and arrange that {@code get} will return the given
     * result on successful completion.
     *
     * @param runnable the runnable task
     * @param result   the result to return on successful completion. If you don't
     *                 need a particular result, consider using constructions of the
     *                 form:
     *                 {@code Future<?> f = new FutureTask<Void>(runnable, null)}
     * @throws NullPointerException if the runnable is null
     */
    public FutureTask(Runnable runnable, V result) {
        this.callable = Executors.callable(runnable, result);
        this.state = NEW; // ensure visibility of callable
    }

    /**
     * Returns result or throws exception for completed task.
     *
     * @param s completed state value
     */
    @SuppressWarnings("unchecked")
    private V report(int s) throws ExecutionException {
        Object x = outcome;
        if (s == NORMAL)        //任务正常执行完
            return (V) x;
        if (s >= CANCELLED)    //如果任务状态为CANCELLED,INTERRUPTING,INTERRUPTED，则不允许获取
            throw new CancellationException();
        throw new ExecutionException((Throwable) x);
    }

    public boolean isCancelled() {
        return state >= CANCELLED;
    }

    /**
     * 如果任务已完成，则返回 true。 可能由于正常终止、异常或取消而完成，在所有这些情况中，此方法都将返回 true。
     */
    public boolean isDone() {
        return state != NEW;
    }

    /**
     * cancel如果任务已完成，即状态不为NEW,调用cancel会返回false
     * mayInterruptIfRunning用来决定任务的状态。
     * true : 任务状态= INTERRUPTING =5。如果任务已经运行，则强行中断。如果任务未运行，那么则不会再运行
     * false：CANCELLED = 4。如果任务已经运行，则允许运行完成（但不能通过get获取结果）。如果任务未运行，那么则不会再运行
     */
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!(state == NEW
                && UNSAFE.compareAndSwapInt(this, stateOffset, NEW, mayInterruptIfRunning ? INTERRUPTING : CANCELLED)))
            return false;
        try { // in case call to interrupt throws exception
            if (mayInterruptIfRunning) {
                try {
                    Thread t = runner;
                    if (t != null)
                        t.interrupt(); // 直接中断正在执行的线程
                } finally { // final state
                    UNSAFE.putOrderedInt(this, stateOffset, INTERRUPTED);   //将状态从INTERRIPTING转为INTERRUPTED
                }
            }
        } finally {
            finishCompletion();
        }
        return true;
    }

    /**
     * @throws CancellationException {@inheritDoc}
     */
    public V get() throws InterruptedException, ExecutionException {
        int s = state;
        if (s <= COMPLETING)   //如果任务还没有完成，则将调用get()方法的线程加入到等待队列
            s = awaitDone(false, 0L);
        return report(s);
    }

    /**
     * @throws CancellationException {@inheritDoc}
     */
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (unit == null)
            throw new NullPointerException();
        int s = state;
        if (s <= COMPLETING && (s = awaitDone(true, unit.toNanos(timeout))) <= COMPLETING)
            throw new TimeoutException();
        return report(s);
    }

    /**
     * Protected method invoked when this task transitions to state
     * {@code isDone} (whether normally or via cancellation). The default
     * implementation does nothing. Subclasses may override this method to
     * invoke completion callbacks or perform bookkeeping. Note that you can
     * query status inside the implementation of this method to determine
     * whether this task has been cancelled.
     */
    protected void done() {
    }

    /**
     * 该方法在FutureTask里只有run方法在任务完成后调用。 主要保存任务执行结果到成员变量outcome 中，
     * 和切换任务执行状态。由该方法可以得知：
     * COMPLETING ： 任务已执行完成（也可能是异常完成），但还未设置结果到成员变量outcome中，也意味着还不能get
     * 然后继续将状态设为NORMAL
     * NORMAL ： 任务彻底执行完成
     * <p>
     * <p>
     * Sets the result of this future to the given value unless this future has
     * already been set or has been cancelled.
     * <p>
     * <p>
     * This method is invoked internally by the {@link #run} method upon
     * successful completion of the computation.
     *
     * @param v the value
     */
    protected void set(V v) {
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
            outcome = v;
            UNSAFE.putOrderedInt(this, stateOffset, NORMAL); // final state
            finishCompletion();
        }
    }

    /**
     * Causes this future to report an {@link ExecutionException} with the given
     * throwable as its cause, unless this future has already been set or has
     * been cancelled.
     * <p>
     * <p>
     * This method is invoked internally by the {@link #run} method upon failure
     * of the computation.
     *
     * @param t the cause of failure
     */
    protected void setException(Throwable t) {
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
            outcome = t;
            UNSAFE.putOrderedInt(this, stateOffset, EXCEPTIONAL); // final state
            finishCompletion();
        }
    }

    public void run() {
        if (state != NEW || !UNSAFE.compareAndSwapObject(this, runnerOffset, null, Thread.currentThread()))
            return;
        try {
            Callable<V> c = callable;
            if (c != null && state == NEW) {
                V result;
                boolean ran;
                try {
                    result = c.call();
                    ran = true;
                } catch (Throwable ex) {
                    result = null;
                    ran = false;
                    setException(ex);
                }
                if (ran)  //线程正常执行完
                    set(result); // 唤醒被阻塞的调用的线程
            }
        } finally {
            // runner must be non-null until state is settled to
            // prevent concurrent calls to run()
            //执行完成后，将runnber置空
            runner = null;
            // state must be re-read after nulling runner to prevent
            // leaked interrupts
            int s = state;
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
    }

    /**
     * Executes the computation without setting its result, and then resets this
     * future to initial state, failing to do so if the computation encounters
     * an exception or is cancelled. This is designed for use with tasks that
     * intrinsically execute more than once.
     *
     * @return {@code true} if successfully run and reset
     */
    protected boolean runAndReset() {
        if (state != NEW || !UNSAFE.compareAndSwapObject(this, runnerOffset, null, Thread.currentThread()))
            return false;
        boolean ran = false;
        int s = state;
        try {
            Callable<V> c = callable;
            if (c != null && s == NEW) {
                try {
                    c.call(); // don't set result
                    ran = true;
                } catch (Throwable ex) {
                    setException(ex);
                }
            }
        } finally {
            // runner must be non-null until state is settled to
            // prevent concurrent calls to run()
            runner = null;
            // state must be re-read after nulling runner to prevent
            // leaked interrupts
            s = state;
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
        return ran && s == NEW;
    }

    /**
     * Ensures that any interrupt from a possible cancel(true) is only delivered
     * to a task while in run or runAndReset.
     */
    private void handlePossibleCancellationInterrupt(int s) {
        // It is possible for our interrupter to stall before getting a
        // chance to interrupt us. Let's spin-wait patiently.
        if (s == INTERRUPTING)
            while (state == INTERRUPTING)
                Thread.yield(); // wait out pending interrupt

        // assert state == INTERRUPTED;

        // We want to clear any interrupt we may have received from
        // cancel(true). However, it is permissible to use interrupts
        // as an independent mechanism for a task to communicate with
        // its caller, and there is no way to clear only the
        // cancellation interrupt.
        //
        // Thread.interrupted();
    }

    /**
     * Removes and signals all waiting threads, invokes done(), and nulls out
     * callable.
     */
    private void finishCompletion() {
        // assert state > COMPLETING;
        for (WaitNode q; (q = waiters) != null; ) {
            if (UNSAFE.compareAndSwapObject(this, waitersOffset, q, null)) {
                //循环唤醒等待的对象
                for (; ; ) {
                    Thread t = q.thread;
                    if (t != null) {
                        q.thread = null;
                        LockSupport.unpark(t); // 唤醒线程
                    }
                    WaitNode next = q.next;
                    if (next == null)
                        break;
                    q.next = null; // unlink to help gc
                    q = next;
                }
                break;
            }
        }

        done();

        callable = null; // to reduce footprint
    }

    /**
     * 等待计算或被interrupte中断或时间到了
     * Awaits completion or aborts on interrupt or timeout.
     *
     * @param timed true if use timed waits
     * @param nanos time to wait, if timed
     * @return state upon completion
     */
    private int awaitDone(boolean timed, long nanos) throws InterruptedException {
        final long deadline = timed ? System.nanoTime() + nanos : 0L;
        WaitNode q = null;
        boolean queued = false;  //是否已加入到队列
        for (; ; ) {
            /**
             * 这里的if else的顺序也是有讲究的。
             *  1.先判断线程是否中断，中断则从队列中移除(也可能该线程不存在于队列中)
             *  2.判断当前任务是否执行完成，执行完成则不再阻塞，直接返回。
             *  3.如果任务状态=COMPLETING，证明该任务处于已执行完成，正在切换任务执行状态，CPU让出片刻即可
             *  4.q==null，则证明还未创建节点，则创建节点
             *  5.q节点入队
             *  6和7.阻塞
             **/
            if (Thread.interrupted()) {   //如果已被中断，从中断队列移除，并且抛出异常
                removeWaiter(q);
                throw new InterruptedException();
            }

            int s = state;
            if (s > COMPLETING) {  //已经完成或中断，返回状态
                if (q != null)
                    q.thread = null;
                return s;
            } else if (s == COMPLETING) // 如果任务已完成，但是还没设置值，让出cpu
                Thread.yield();
            else if (q == null)
                q = new WaitNode();  //第一次创建等待节点
            else if (!queued)
                queued = UNSAFE.compareAndSwapObject(this, waitersOffset, q.next = waiters, q);
            else if (timed) {  //阻塞指定时长
                nanos = deadline - System.nanoTime();
                if (nanos <= 0L) {
                    removeWaiter(q);
                    return state;
                }
                LockSupport.parkNanos(this, nanos);
            } else
                LockSupport.park(this); // 阻塞调用线程
        }
    }

    /**
     * Tries to unlink a timed-out or interrupted wait node to avoid
     * accumulating garbage. Internal nodes are simply unspliced without CAS
     * since it is harmless if they are traversed anyway by releasers. To avoid
     * effects of unsplicing from already removed nodes, the list is retraversed
     * in case of an apparent race. This is slow when there are a lot of nodes,
     * but we don't expect lists to be long enough to outweigh higher-overhead
     * schemes.
     */
    private void removeWaiter(WaitNode node) {
        if (node != null) {
            node.thread = null;
            retry:
            for (; ; ) { // restart on removeWaiter race
                for (WaitNode pred = null, q = waiters, s; q != null; q = s) {
                    s = q.next;
                    if (q.thread != null)
                        pred = q;
                    else if (pred != null) {
                        pred.next = s;
                        if (pred.thread == null) // check for race
                            continue retry;
                    } else if (!UNSAFE.compareAndSwapObject(this, waitersOffset, q, s))
                        continue retry;
                }
                break;
            }
        }
    }

    /**
     * 链表，用于存放等待的线程
     * Simple linked list nodes to record waiting threads in a Treiber stack.
     * See other classes such as Phaser and SynchronousQueue for more detailed
     * explanation.
     */
    static final class WaitNode {
        volatile Thread thread;
        volatile WaitNode next;

        WaitNode() {
            thread = Thread.currentThread();
        }
    }

}
