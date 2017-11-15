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
 * Written by Doug Lea, Bill Scherer, and Michael Scott with
 * assistance from members of JCP JSR-166 Expert Group and released to
 * the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

import java.util.*;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 公平策略使用的是Queue，非公平策略使用的是Stack
 * A {@linkplain BlockingQueue blocking queue} in which each insert
 * operation must wait for a corresponding remove operation by another
 * thread, and vice versa.  A synchronous queue does not have any
 * internal capacity, not even a capacity of one.  You cannot
 * {@code peek} at a synchronous queue because an element is only
 * present when you try to remove it; you cannot insert an element
 * (using any method) unless another thread is trying to remove it;
 * you cannot iterate as there is nothing to iterate.  The
 * <em>head</em> of the queue is the element that the first queued
 * inserting thread is trying to add to the queue; if there is no such
 * queued thread then no element is available for removal and
 * {@code poll()} will return {@code null}.  For purposes of other
 * {@code Collection} methods (for example {@code contains}), a
 * {@code SynchronousQueue} acts as an empty collection.  This queue
 * does not permit {@code null} elements.
 * <p>
 * <p>Synchronous queues are similar to rendezvous channels used in
 * CSP and Ada. They are well suited for handoff designs, in which an
 * object running in one thread must sync up with an object running
 * in another thread in order to hand it some information, event, or
 * task.
 * <p>
 * <p>This class supports an optional fairness policy for ordering
 * waiting producer and consumer threads.  By default, this ordering
 * is not guaranteed. However, a queue constructed with fairness set
 * to {@code true} grants threads access in FIFO order.
 * <p>
 * <p>This class and its iterator implement all of the
 * <em>optional</em> methods of the {@link Collection} and {@link
 * Iterator} interfaces.
 * <p>
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @param <E> the type of elements held in this collection
 * @author Doug Lea and Bill Scherer and Michael Scott
 * @since 1.5
 */
public class SynchronousQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>, java.io.Serializable {
    /**
     * The number of CPUs, for spin control
     */
    static final int NCPUS = Runtime.getRuntime().availableProcessors();

    /*
     * This class implements extensions of the dual stack and dual
     * queue algorithms described in "Nonblocking Concurrent Objects
     * with Condition Synchronization", by W. N. Scherer III and
     * M. L. Scott.  18th Annual Conf. on Distributed Computing,
     * Oct. 2004 (see also
     * http://www.cs.rochester.edu/u/scott/synchronization/pseudocode/duals.html).
     * The (Lifo) stack is used for non-fair mode, and the (Fifo)
     * queue for fair mode. The performance of the two is generally
     * similar. Fifo usually supports higher throughput under
     * contention but Lifo maintains higher thread locality in common
     * applications.
     * LIFO(后进先出)堆栈用于非公平模式，而FIFO(先进先出)队列用于公平模式。
     * 两者的性能差不多。FIFO通常用于支持在竞争中有更高的吞吐量，而一个LIFO
     * 主要用于普通应用中高度的线程位置。
     *
     * A dual queue (and similarly stack) is one that at any given
     * time either holds "data" -- items provided by put operations,
     * or "requests" -- slots representing take operations, or is
     * empty. A call to "fulfill" (i.e., a call requesting an item
     * from a queue holding data or vice versa) dequeues a
     * complementary node.  The most interesting feature of these
     * queues is that any operation can figure out which mode the
     * queue is in, and act accordingly without needing locks.
     *
     * Both the queue and stack extend abstract class Transferer
     * defining the single method transfer that does a put or a
     * take. These are unified into a single method because in dual
     * data structures, the put and take operations are symmetrical,
     * so nearly all code can be combined. The resulting transfer
     * methods are on the long side, but are easier to follow than
     * they would be if broken up into nearly-duplicated parts.
     *
     * queue和stack都继承了抽象类Transferer类，而抽象类Transferer只定义了
     * transfer一个方法，该方法用于put或者take方法。两种数据结构统一到一个方法，
     * 因为put操作和take操作是对称的，所以可以将代码整合到一起。结果就是方法
     * 可能有些长，但是比拆开分成多个相似的方法要简单很多。
     *
     * The queue and stack data structures share many conceptual
     * similarities but very few concrete details. For simplicity,
     * they are kept distinct so that they can later evolve
     * separately.
     *
     * The algorithms here differ from the versions in the above paper
     * in extending them for use in synchronous queues, as well as
     * dealing with cancellation. The main differences include:
     *
     *  1. The original algorithms used bit-marked pointers, but
     *     the ones here use mode bits in nodes, leading to a number
     *     of further adaptations.
     *  2. SynchronousQueues must block threads waiting to become
     *     fulfilled.
     *  3. Support for cancellation via timeout and interrupts,
     *     including cleaning out cancelled nodes/threads
     *     from lists to avoid garbage retention and memory depletion.
     *
     * Blocking is mainly accomplished using LockSupport park/unpark,
     * except that nodes that appear to be the next ones to become
     * fulfilled first spin a bit (on multiprocessors only). On very
     * busy synchronous queues, spinning can dramatically improve
     * throughput. And on less busy ones, the amount of spinning is
     * small enough not to be noticeable.
     *
     * Cleaning is done in different ways in queues vs stacks.  For
     * queues, we can almost always remove a node immediately in O(1)
     * time (modulo retries for consistency checks) when it is
     * cancelled. But if it may be pinned as the current tail, it must
     * wait until some subsequent cancellation. For stacks, we need a
     * potentially O(n) traversal to be sure that we can remove the
     * node, but this can run concurrently with other threads
     * accessing the stack.
     *
     * While garbage collection takes care of most node reclamation
     * issues that otherwise complicate nonblocking algorithms, care
     * is taken to "forget" references to data, other nodes, and
     * threads that might be held on to long-term by blocked
     * threads. In cases where setting to null would otherwise
     * conflict with main algorithms, this is done by changing a
     * node's link to now point to the node itself. This doesn't arise
     * much for Stack nodes (because blocked threads do not hang on to
     * old head pointers), but references in Queue nodes must be
     * aggressively forgotten to avoid reachability of everything any
     * node has ever referred to since arrival.
     */
    /**
     * The number of times to spin before blocking in timed waits.
     * The value is empirically derived -- it works well across a
     * variety of processors and OSes. Empirically, the best value
     * seems not to vary with number of CPUs (beyond 2) so is just
     * a constant.
     * 在阻塞定时等待之前，自旋的次数。
     */
    static final int maxTimedSpins = (NCPUS < 2) ? 0 : 32;
    /**
     * The number of times to spin before blocking in untimed waits.
     * This is greater than timed value because untimed waits spin
     * faster since they don't need to check times on each spin.
     * 没有定时等待，在阻塞前自选的次数。这比有定时的次数多，因为在每次旋转
     * 时不需要检查times，所以这也使得untimed自旋比较快速。
     */
    static final int maxUntimedSpins = maxTimedSpins * 16;
    /**
     * The number of nanoseconds for which it is faster to spin
     * rather than to use timed park. A rough estimate suffices.
     *
     */
    static final long spinForTimeoutThreshold = 1000L;
    private static final long serialVersionUID = -3223113410248163686L;
    /**
     * The transferer. Set only in constructor, but cannot be declared
     * as final without further complicating serialization.  Since
     * this is accessed only at most once per public method, there
     * isn't a noticeable performance penalty for using volatile
     * instead of final here.
     */
    private transient volatile Transferer<E> transferer;
    private ReentrantLock qlock;
    private WaitQueue waitingProducers;
    private WaitQueue waitingConsumers;

    /**
     * Creates a {@code SynchronousQueue} with nonfair access policy.
     * 默认使用非公平策略
     */
    public SynchronousQueue() {
        this(false);
    }

    /**
     * Creates a {@code SynchronousQueue} with the specified fairness policy.
     *
     * @param fair if true, waiting threads contend in FIFO order for
     *             access; otherwise the order is unspecified.
     */
    public SynchronousQueue(boolean fair) {
        transferer = fair ? new TransferQueue<E>() : new TransferStack<E>();
    }

    // Unsafe mechanics
    static long objectFieldOffset(sun.misc.Unsafe UNSAFE,
                                  String field, Class<?> klazz) {
        try {
            return UNSAFE.objectFieldOffset(klazz.getDeclaredField(field));
        } catch (NoSuchFieldException e) {
            // Convert Exception to corresponding Error
            NoSuchFieldError error = new NoSuchFieldError(field);
            error.initCause(e);
            throw error;
        }
    }

    /**
     * Adds the specified element to this queue, waiting if necessary for
     * another thread to receive it.
     *
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public void put(E e) throws InterruptedException {
        if (e == null)
            throw new NullPointerException();
        if (transferer.transfer(e, false, 0) == null) {
            Thread.interrupted();
            throw new InterruptedException();
        }
    }

    /**
     * Inserts the specified element into this queue, waiting if necessary
     * up to the specified wait time for another thread to receive it.
     *
     * @return {@code true} if successful, or {@code false} if the
     * specified waiting time elapses before a consumer appears
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public boolean offer(E e, long timeout, TimeUnit unit)
            throws InterruptedException {
        if (e == null) throw new NullPointerException();
        if (transferer.transfer(e, true, unit.toNanos(timeout)) != null)
            return true;
        if (!Thread.interrupted())
            return false;
        throw new InterruptedException();
    }

    /**
     * Inserts the specified element into this queue, if another thread is
     * waiting to receive it.
     *
     * @param e the element to add
     * @return {@code true} if the element was added to this queue, else
     * {@code false}
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(E e) {
        if (e == null) throw new NullPointerException();
        return transferer.transfer(e, true, 0) != null;
    }

    /**
     * Retrieves and removes the head of this queue, waiting if necessary
     * for another thread to insert it.
     *
     * @return the head of this queue
     * @throws InterruptedException {@inheritDoc}
     */
    public E take() throws InterruptedException {
        E e = transferer.transfer(null, false, 0);
        if (e != null)
            return e;
        Thread.interrupted();
        throw new InterruptedException();
    }

    /**
     * Retrieves and removes the head of this queue, waiting
     * if necessary up to the specified wait time, for another thread
     * to insert it.
     *
     * @return the head of this queue, or {@code null} if the
     * specified waiting time elapses before an element is present
     * @throws InterruptedException {@inheritDoc}
     */
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E e = transferer.transfer(null, true, unit.toNanos(timeout));
        if (e != null || !Thread.interrupted())
            return e;
        throw new InterruptedException();
    }

    /**
     * Retrieves and removes the head of this queue, if another thread
     * is currently making an element available.
     *
     * @return the head of this queue, or {@code null} if no
     * element is available
     */
    public E poll() {
        return transferer.transfer(null, true, 0);
    }

    /**
     * Always returns {@code true}.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @return {@code true}
     */
    public boolean isEmpty() {
        return true;
    }

    /**
     * Always returns zero.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @return zero
     */
    public int size() {
        return 0;
    }

    /**
     * Always returns zero.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @return zero
     */
    public int remainingCapacity() {
        return 0;
    }

    /**
     * Does nothing.
     * A {@code SynchronousQueue} has no internal capacity.
     */
    public void clear() {
    }

    /**
     * Always returns {@code false}.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @param o the element
     * @return {@code false}
     */
    public boolean contains(Object o) {
        return false;
    }

    /**
     * Always returns {@code false}.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @param o the element to remove
     * @return {@code false}
     */
    public boolean remove(Object o) {
        return false;
    }

    /**
     * Returns {@code false} unless the given collection is empty.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @param c the collection
     * @return {@code false} unless given collection is empty
     */
    public boolean containsAll(Collection<?> c) {
        return c.isEmpty();
    }

    /**
     * Always returns {@code false}.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @param c the collection
     * @return {@code false}
     */
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    /**
     * Always returns {@code false}.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @param c the collection
     * @return {@code false}
     */
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    /**
     * Always returns {@code null}.
     * A {@code SynchronousQueue} does not return elements
     * unless actively waited on.
     *
     * @return {@code null}
     */
    public E peek() {
        return null;
    }

    /**
     * Returns an empty iterator in which {@code hasNext} always returns
     * {@code false}.
     *
     * @return an empty iterator
     */
    public Iterator<E> iterator() {
        return Collections.emptyIterator();
    }

    /**
     * Returns an empty spliterator in which calls to
     * {@link java.util.Spliterator#trySplit()} always return {@code null}.
     *
     * @return an empty spliterator
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return Spliterators.emptySpliterator();
    }

    /**
     * Returns a zero-length array.
     *
     * @return a zero-length array
     */
    public Object[] toArray() {
        return new Object[0];
    }

    /**
     * Sets the zeroeth element of the specified array to {@code null}
     * (if the array has non-zero length) and returns it.
     *
     * @param a the array
     * @return the specified array
     * @throws NullPointerException if the specified array is null
     */
    public <T> T[] toArray(T[] a) {
        if (a.length > 0)
            a[0] = null;
        return a;
    }

    /**
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     */
    public int drainTo(Collection<? super E> c) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        int n = 0;
        for (E e; (e = poll()) != null; ) {
            c.add(e);
            ++n;
        }
        return n;
    }

    /*
     * To cope with serialization strategy in the 1.5 version of
     * SynchronousQueue, we declare some unused classes and fields
     * that exist solely to enable serializability across versions.
     * These fields are never used, so are initialized only if this
     * object is ever serialized or deserialized.
     */

    /**
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     */
    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        int n = 0;
        for (E e; n < maxElements && (e = poll()) != null; ) {
            c.add(e);
            ++n;
        }
        return n;
    }

    /**
     * Saves this queue to a stream (that is, serializes it).
     *
     * @param s the stream
     * @throws java.io.IOException if an I/O error occurs
     */
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        boolean fair = transferer instanceof TransferQueue;
        if (fair) {
            qlock = new ReentrantLock(true);
            waitingProducers = new FifoWaitQueue();
            waitingConsumers = new FifoWaitQueue();
        } else {
            qlock = new ReentrantLock();
            waitingProducers = new LifoWaitQueue();
            waitingConsumers = new LifoWaitQueue();
        }
        s.defaultWriteObject();
    }

    /**
     * Reconstitutes this queue from a stream (that is, deserializes it).
     *
     * @param s the stream
     * @throws ClassNotFoundException if the class of a serialized object
     *                                could not be found
     * @throws java.io.IOException    if an I/O error occurs
     */
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (waitingProducers instanceof FifoWaitQueue)
            transferer = new TransferQueue<E>();
        else
            transferer = new TransferStack<E>();
    }

    /**
     * Shared internal API for dual stacks and queues.
     * 用于双栈和队列的共享内部API
     */
    abstract static class Transferer<E> {
        /**
         * Performs a put or take. 进行put和take操作
         *
         * @param e     if non-null, the item to be handed to a consumer;
         *              if null, requests that transfer return an item
         *              offered by producer.
         *              如果E非空，那么元素将被消费者返回给消费者
         *              如果为空，请求将转化为生产者返回一个元素
         * @param timed if this operation should timeout 判断该操作是否有时间限制
         * @param nanos the timeout, in nanoseconds 超时时间，单位为纳秒
         * @return if non-null, the item provided or received; if null,
         * the operation failed due to timeout or interrupt --
         * the caller can distinguish which of these occurred
         * by checking Thread.interrupted.
         *
         * 返回如果非空，元素将被接收或者提供，如果为空，改操作可能会因为超时或者被中断而失败
         * 调用者通过判断Thread.interrupted来区分以上两个操作
         */
        abstract E transfer(E e, boolean timed, long nanos);
    }

    /**
     * Dual stack
     * 双端栈，用于实现非公平策略
     */
    static final class TransferStack<E> extends Transferer<E> {
        /*
         * This extends Scherer-Scott dual stack algorithm, differing,
         * among other ways, by using "covering" nodes rather than
         * bit-marked pointers: Fulfilling operations push on marker
         * nodes (with FULFILLING bit set in mode) to reserve a spot
         * to match a waiting node.
         */

        /* Modes for SNodes, ORed together in node fields */
        /**
         * 任何线程的操作都处于以下三种状态其中的一种，也就是三种模式，
         * 消费者请求数据模式或者是生产者放入数据模式，或者是正处于匹配状态
         */
        /**
         * Node represents an unfulfilled consumer
         * 节点代表一个未完成的消费者(正在消费数据的消费者)
         */
        static final int REQUEST = 0;
        /**
         * Node represents an unfulfilled producer
         * 节点代表一个未完成的生产者(正在生产数据的生产者)
         */
        static final int DATA = 1;
        /**
         * Node is fulfilling another unfulfilled DATA or REQUEST
         * 节点正在完成一个未完成的数据或者请求(表示正在匹配一个生产者或者消费者)
         *
         */
        static final int FULFILLING = 2;
        // Unsafe mechanics
        private static final sun.misc.Unsafe UNSAFE;
        private static final long headOffset;

        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = TransferStack.class;
                headOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("head"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }

        /**
         * The head (top) of the stack
         */
        volatile SNode head;

        /**
         * Returns true if m has fulfilling bit set.
         * 返回true表示当前节点已经被设置FULFILLING状态
         */
        static boolean isFulfilling(int m) {
            return (m & FULFILLING) != 0;
        }

        /**
         * Creates or resets fields of a node. Called only from transfer
         * where the node to push on stack is lazily created and
         * reused when possible to help reduce intervals between reads
         * and CASes of head and to avoid surges of garbage when CASes
         * to push nodes fail due to contention.
         * 创建节点或者重置一个节点的字段。
         * 如果节点s为空，那么s就作为新的一个节点，新节点的mode为mode，
         * 新节点的下一个节点就为next字段信息。
         */
        static SNode snode(SNode s, Object e, SNode next, int mode) {
            if (s == null) s = new SNode(e);
            s.mode = mode;
            s.next = next;
            return s;
        }

        /**
         * h为头结点，并且头结点设置为nh，如果以上成功，返回true
         * @param h
         * @param nh
         * @return
         */
        boolean casHead(SNode h, SNode nh) {
            return h == head && UNSAFE.compareAndSwapObject(this, headOffset, h, nh);
        }

        /**
         * Puts or takes an item.
         * 放入或者取出元素
         */
        @SuppressWarnings("unchecked")
        E transfer(E e, boolean timed, long nanos) {
            /*
             * Basic algorithm is to loop trying one of three actions:
             * 基本的算法就是循环执行以下三种操作其中的一个操作
             *
             * 1. If apparently empty or already containing nodes of same
             *    mode, try to push node on stack and wait for a match,
             *    returning it, or null if cancelled.
             *    如果显而易见的为空或者已经包含了同种模式的节点，尝试将节点放入
             *    栈等待匹配，返回节点或者因为取消而返回空
             *
             * 2. If apparently containing node of complementary mode,
             *    try to push a fulfilling node on to stack, match
             *    with corresponding waiting node, pop both from
             *    stack, and return matched item. The matching or
             *    unlinking might not actually be necessary because of
             *    other threads performing action 3:
             *
             *    如果显而易见的包含了已经处于完成模式的节点，尝试放入一个执行的节点
             *    到栈，匹配传递等待的节点，同时弹出栈，并且返回已经匹配的元素。由于其他
             *    的线程处于动作三，所以匹配或取消可能不是必须的。
             *
             * 3. If top of stack already holds another fulfilling node,
             *    help it out by doing its match and/or pop
             *    operations, and then continue. The code for helping
             *    is essentially the same as for fulfilling, except
             *    that it doesn't return the item.
             *
             */

            SNode s = null; // constructed/reused as needed
            //根据e来判断是处于消费者消费数据模式还是生产者生产数据模式
            int mode = (e == null) ? REQUEST : DATA;
            //h是头结点，mode为当前操作的模式，timed是一个入参，判断是否要
            //等待指定时间，nanos是时间单位

            for (; ; ) {
                //h等于头结点
                SNode h = head;
                //如果头结点为空或者头结点不为空但是头结点处于相同请求模式，
                //此时要么生成头结点或者生成新的节点，作为栈的尾节点的下一个节点
                if (h == null || h.mode == mode) {  // empty or same-mode
                    //判断能否继续等待(timed是一个入参)，如果有截至时间并且等待时间到了
                    if (timed && nanos <= 0) {      // can't wait
                        //如果头结点不为空但是头结点处于取消状态
                        if (h != null && h.isCancelled())
                            //将头结点设置为当前头结点的下一个节点
                            casHead(h, h.next);     // pop cancelled node
                        else
                            //如果头结点为空或者头结点不为空但是头结点没有被取消，那么则返回空
                            return null;
                    }
                    // 生成一个SNode结点,则将当前节点作为头结点，传入当前的mode，并将头结点作为当前节点的下一个节点
                    else if (casHead(h, s = snode(s, e, h, mode))) {
                        //开始了匹配节点，自旋或者阻塞，可能会有时间限制,times参数
                        SNode m = awaitFulfill(s, timed, nanos);
                        //如果匹配节点为节点自己，表示规定时间内匹配失败，清理节点并且退出
                        if (m == s) {               // wait was cancelled
                            clean(s);
                            return null;
                        }
                        //如果匹配成功，且头结点不为空，且头节点的下一个节点是s
                        if ((h = head) != null && h.next == s)
                            //将s的下一个节点设置头结点
                            casHead(h, s.next);     // help s's fulfiller
                        //根据不同模式返回数据
                        return (E) ((mode == REQUEST) ? m.item : s.item);
                    }
                }
                //如果头结点不为空，而且模式不相同，没有FULFILLING标记，尝试匹配
                else if (!isFulfilling(h.mode)) { // try to fulfill
                    //此时h为头节点，判断h是否已经取消
                    if (h.isCancelled())            // already cancelled
                        // 如果头结点被取消，那么则将头结点的下一个节点作为头结点
                        casHead(h, h.next);         // pop and retry
                    //如果头结点没有被取消，生成一个SNode结点，头结点作为s节点的下一个节点
                    else if (casHead(h, s = snode(s, e, h, FULFILLING | mode))) {
                        //无限循环一直等到匹配或者等待者消失
                        for (; ; ) { // loop until matched or waiters disappear
                            //保存s的下一个节点
                            SNode m = s.next;       // m is s's match
                            //如果next节点为空，表示已经没有等待者了
                            if (m == null) {        // all waiters are gone
                                //比较替换s，将头结点设置null
                                casHead(s, null);   // pop fulfill node
                                //s赋值为空
                                s = null;           // use new node next time
                                break;              // restart main loop
                            }
                            //找出m的下一个节点
                            SNode mn = m.next;
                            // 尝试匹配，并且成功
                            if (m.tryMatch(s)) {
                                // 如果m和s匹配成功，将头结点设置为mn，此时s节点和mn节点都从stack取出
                                casHead(s, mn);     // pop both s and m
                                //根据从此请求模式返回元素的数据
                                return (E) ((mode == REQUEST) ? m.item : s.item);
                            }
                            //如果m和s匹配失败
                            else                  // lost match
                                //则将s的下一个节点设置mn
                                s.casNext(m, mn);   // help unlink
                        }
                    }
                }
                //如果头结点不为空，并且模式不相同，并且匹配失败
                else {                            // help a fulfiller
                    //寻找下一个节点
                    SNode m = h.next;               // m is h's match
                    if (m == null)                  // waiter is gone
                        //如果下一个节点为null，那么表示stack里面的等待者为空，继续循环
                        casHead(h, null);           // pop fulfilling node
                    else {
                        SNode mn = m.next;
                        //尝试将m匹配头结点
                        if (m.tryMatch(h))          // help match
                            //如果头结点和m匹配成功，那么同时弹出头结点和m，继续循环
                            casHead(h, mn);         // pop both h and m
                        else                        // lost match
                            //匹配失败，将h的下一个节点设置为mn(m的下一个节点)，继续循环
                            //此时h还是头结点，只是h的下一个节点称为了mn
                            h.casNext(m, mn);       // help unlink
                    }
                }
            }
        }

        /**
         * Spins/blocks until node s is matched by a fulfill operation.
         * 旋转或者阻塞直到fulfill操作匹配到节点，s为将要匹配的节点
         *
         * @param s     the waiting node
         * @param timed true if timed wait {true为超时等待}
         * @param nanos timeout value
         * @return matched node, or s if cancelled
         */
        SNode awaitFulfill(SNode s, boolean timed, long nanos) {
            /*
             * When a node/thread is about to block, it sets its waiter
             * field and then rechecks state at least one more time
             * before actually parking, thus covering race vs
             * fulfiller noticing that waiter is non-null so should be
             * woken.
             *
             * When invoked by nodes that appear at the point of call
             * to be at the head of the stack, calls to park are
             * preceded by spins to avoid blocking when producers and
             * consumers are arriving very close in time.  This can
             * happen enough to bother only on multiprocessors.
             *
             * The order of checks for returning out of main loop
             * reflects fact that interrupts have precedence over
             * normal returns, which have precedence over
             * timeouts. (So, on timeout, one last check for match is
             * done before giving up.) Except that calls from untimed
             * SynchronousQueue.{poll/offer} don't check interrupts
             * and don't wait at all, so are trapped in transfer
             * method rather than calling awaitFulfill.
             */
            //判断是否有截至时间，有的话则返回截至时间
            final long deadline = timed ? System.nanoTime() + nanos : 0L;
            Thread w = Thread.currentThread();
            //判断自旋时间
            int spins = (shouldSpin(s) ? (timed ? maxTimedSpins : maxUntimedSpins) : 0);
            for (; ; ) {
                if (w.isInterrupted())
                    s.tryCancel();
                //获取匹配节点
                SNode m = s.match;
                //匹配节点不为空,返回匹配到的节点
                if (m != null)
                    return m;
                //如果有延时
                if (timed) {
                    nanos = deadline - System.nanoTime();
                    //如果时间到了，则将匹配节点(match字段)设为自己
                    if (nanos <= 0L) {
                        s.tryCancel();
                        continue;
                    }
                }
                if (spins > 0)
                    spins = shouldSpin(s) ? (spins - 1) : 0;
                else if (s.waiter == null)
                    s.waiter = w; // establish waiter so can park next iter
                else if (!timed)
                    LockSupport.park(this);
                else if (nanos > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanos);
            }
        }

        /**
         * Returns true if node s is at head or there is an active
         * fulfiller.
         */
        boolean shouldSpin(SNode s) {
            SNode h = head;
            return (h == s || h == null || isFulfilling(h.mode));
        }

        /**
         * Unlinks s from the stack.
         * 从stack断开节点
         */
        void clean(SNode s) {
            s.item = null;   // forget item
            s.waiter = null; // forget thread

            /*
             * At worst we may need to traverse entire stack to unlink
             * s. If there are multiple concurrent calls to clean, we
             * might not see s if another thread has already removed
             * it. But we can stop when we see any node known to
             * follow s. We use s.next unless it too is cancelled, in
             * which case we try the node one past. We don't check any
             * further because we don't want to doubly traverse just to
             * find sentinel.
             * 断开节点s的连接，最坏的情况下可能要遍历整个stack。如果多个线程并发
             * 引用clean函数，我们可能看不到s节点，如果其他的线程已经移除掉了该节点。
             * 但是我们可以停止s节点的下一个节点。我们在尝试下一个节点是，我们会使用s.next。
             * 我们将不会更进一步的验证，因为我们不想为了找到哨兵而遍历两次。
             */

            //past赋值为s的下一个节点
            SNode past = s.next;
            //如果s的下一个节点不为空且s的下一个节点被取消了，则将past设置为s的下一个节点的下一个节点
            if (past != null && past.isCancelled())
                past = past.next;

            // Absorb cancelled nodes at head
            SNode p;
            while ((p = head) != null && p != past && p.isCancelled())
                casHead(p, p.next);

            // Unsplice embedded nodes
            while (p != null && p != past) {
                SNode n = p.next;
                if (n != null && n.isCancelled())
                    p.casNext(n, n.next);
                else
                    p = n;
            }
        }

        /**
         * Node class for TransferStacks.
         * TransFerStacks的节点类
         */
        static final class SNode {
            // Unsafe mechanics
            private static final sun.misc.Unsafe UNSAFE;
            // match域的内存偏移地址
            private static final long matchOffset;
            // next域偏移地址
            private static final long nextOffset;

            static {
                try {
                    UNSAFE = sun.misc.Unsafe.getUnsafe();
                    Class<?> k = SNode.class;
                    //获取match域的偏移地址
                    matchOffset = UNSAFE.objectFieldOffset
                            (k.getDeclaredField("match"));
                    //获取next域的偏移地址
                    nextOffset = UNSAFE.objectFieldOffset
                            (k.getDeclaredField("next"));
                } catch (Exception e) {
                    throw new Error(e);
                }
            }

            /**
             * 指向下一个节点
             */
            volatile SNode next;        // next node in stack
            // Note: item and mode fields don't need to be volatile
            // since they are always written before, and read after,
            // other volatile/atomic operations.
            // 相匹配的节点
            volatile SNode match;       // the node matched to this
            //当前等待的线程，通过park或者unpark来控制
            volatile Thread waiter;     // to control park/unpark
            //元素项
            Object item;                // data; or null for REQUESTs
            //模式
            int mode;

            /**
             * 传入的是该节点保存的数据
             * @param item
             */
            SNode(Object item) {
                this.item = item;
            }

            /**
             * 如果该节点的下一个节点是cmp，那么就将下一个节点设置为val
             * nextOffset为下一个节点的偏移量
             *
             * @param cmp
             * @param val
             * @return
             */
            boolean casNext(SNode cmp, SNode val) {
                return cmp == next &&
                        UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
            }

            /**
             * Tries to match node s to this node, if so, waking up thread.
             * Fulfillers call tryMatch to identify their waiters.
             * Waiters block until they have been matched.
             * 当前节点匹配节点S，如果成功，唤醒线程，
             * Fulfiller操作调用tryMatch来识别其他的等待者，waiter将会一直阻塞直到被匹配
             * 如果该方法返回true表示s匹配成功
             *
             * @param s the node to match 匹配的节点
             * @return true if successfully matched to s
             */
            boolean tryMatch(SNode s) {
                // 本结点的match域为null并且设置当前节点的match域为s成功
                if (match == null && UNSAFE.compareAndSwapObject(this, matchOffset, null, s)) {
                    //获取当前等待的线程
                    Thread w = waiter;
                    if (w != null) {    // waiters need at most one unpark
                        //将本节点等待的线程重新置为空
                        waiter = null;
                        //unpark等待的线程
                        LockSupport.unpark(w);
                    }
                    return true;
                }
                // 如果match不为null或者CAS设置失败，则比较match域是否等于将要匹配的s结点，若相等，则表示已经完成匹配，匹配成功
                return match == s;
            }

            /**
             * Tries to cancel a wait by matching node to itself.
             * 通过匹配自己来取消等待，如果没有匹配节点，那么则将匹配节点设置为自己
             */
            void tryCancel() {
                UNSAFE.compareAndSwapObject(this, matchOffset, null, this);
            }

            /**
             * 判断match域是否等于自己来判断是否取消
             * @return
             */
            boolean isCancelled() {
                return match == this;
            }
        }
    }

    /**
     * Dual Queue 双端队列
     */
    static final class TransferQueue<E> extends Transferer<E> {
        /*
         * This extends Scherer-Scott dual queue algorithm, differing,
         * among other ways, by using modes within nodes rather than
         * marked pointers. The algorithm is a little simpler than
         * that for stacks because fulfillers do not need explicit
         * nodes, and matching is done by CAS'ing QNode.item field
         * from non-null to null (for put) or vice versa (for take).
         */

        private static final sun.misc.Unsafe UNSAFE;
        private static final long headOffset;
        private static final long tailOffset;
        private static final long cleanMeOffset;

        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = TransferQueue.class;
                headOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("head"));
                tailOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("tail"));
                cleanMeOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("cleanMe"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }

        /**
         * Head of queue，头结点
         */
        transient volatile QNode head;
        /**
         * Tail of queue，尾节点
         */
        transient volatile QNode tail;
        /**
         * Reference to a cancelled node that might not yet have been
         * unlinked from queue because it was the last inserted node
         * when it was cancelled.
         *
         */
        transient volatile QNode cleanMe;

        TransferQueue() {
            //初始化一个虚拟节点
            QNode h = new QNode(null, false); // initialize to dummy node.
            head = h;
            tail = h;
        }

        /**
         * Tries to cas nh as new head; if successful, unlink
         * old head's next node to avoid garbage retention.
         * 用cas的将nh设置为新的头结点，如果设置成功，
         * 断开之前头结点的下一个节点来避免垃圾的存在。
         */
        void advanceHead(QNode h, QNode nh) {
            if (h == head &&
                    UNSAFE.compareAndSwapObject(this, headOffset, h, nh))
                h.next = h; // forget old next
        }

        /**
         * Tries to cas nt as new tail.
         * 通过cas的方式是nt成为尾节点
         */
        void advanceTail(QNode t, QNode nt) {
            if (tail == t)
                UNSAFE.compareAndSwapObject(this, tailOffset, t, nt);
        }

        /**
         * Tries to CAS cleanMe slot.
         */
        boolean casCleanMe(QNode cmp, QNode val) {
            return cleanMe == cmp &&
                    UNSAFE.compareAndSwapObject(this, cleanMeOffset, cmp, val);
        }

        /**
         * 放入或取出元素
         * Puts or takes an item.
         */
        @SuppressWarnings("unchecked")
        E transfer(E e, boolean timed, long nanos) {
            /* Basic algorithm is to loop trying to take either of
             * two actions:
             *
             * 1. If queue apparently empty or holding same-mode nodes,
             *    try to add node to queue of waiters, wait to be
             *    fulfilled (or cancelled) and return matching item.
             *
             * 2. If queue apparently contains waiting items, and this
             *    call is of complementary mode, try to fulfill by CAS'ing
             *    item field of waiting node and dequeuing it, and then
             *    returning matching item.
             *
             * In each case, along the way, check for and try to help
             * advance head and tail on behalf of other stalled/slow
             * threads.
             *
             * The loop starts off with a null check guarding against
             * seeing uninitialized head or tail values. This never
             * happens in current SynchronousQueue, but could if
             * callers held non-volatile/final ref to the
             * transferer. The check is here anyway because it places
             * null checks at top of loop, which is usually faster
             * than having them implicitly interspersed.
             */

            QNode s = null; // constructed/reused as needed
            boolean isData = (e != null);

            for (; ; ) {
                QNode t = tail;
                QNode h = head;
                //如果头结点或者尾节点为空，说明初始化未完成，自旋等待初始化完成
                if (t == null || h == null)         // saw uninitialized value
                    continue;                       // spin
                //队列为空或者是相同模式
                if (h == t || t.isData == isData) { // empty or same-mode
                    QNode tn = t.next;
                    if (t != tail)                  // inconsistent read
                        continue;
                    if (tn != null) {               // lagging tail
                        advanceTail(t, tn);
                        continue;
                    }
                    if (timed && nanos <= 0)        // can't wait
                        return null;
                    if (s == null)
                        s = new QNode(e, isData);
                    if (!t.casNext(null, s))        // failed to link in
                        continue;

                    advanceTail(t, s);              // swing tail and wait
                    Object x = awaitFulfill(s, e, timed, nanos);
                    if (x == s) {                   // wait was cancelled
                        clean(t, s);
                        return null;
                    }

                    if (!s.isOffList()) {           // not already unlinked
                        advanceHead(t, s);          // unlink if head
                        if (x != null)              // and forget fields
                            s.item = s;
                        s.waiter = null;
                    }
                    return (x != null) ? (E) x : e;

                } else {                            // complementary-mode
                    QNode m = h.next;               // node to fulfill
                    if (t != tail || m == null || h != head)
                        continue;                   // inconsistent read

                    Object x = m.item;
                    if (isData == (x != null) ||    // m already fulfilled
                            x == m ||                   // m cancelled
                            !m.casItem(x, e)) {         // lost CAS
                        advanceHead(h, m);          // dequeue and retry
                        continue;
                    }

                    advanceHead(h, m);              // successfully fulfilled
                    LockSupport.unpark(m.waiter);
                    return (x != null) ? (E) x : e;
                }
            }
        }

        /**
         * Spins/blocks until node s is fulfilled.
         *
         * @param s     the waiting node
         * @param e     the comparison value for checking match
         * @param timed true if timed wait
         * @param nanos timeout value
         * @return matched item, or s if cancelled
         */
        Object awaitFulfill(QNode s, E e, boolean timed, long nanos) {
            /* Same idea as TransferStack.awaitFulfill */
            final long deadline = timed ? System.nanoTime() + nanos : 0L;
            Thread w = Thread.currentThread();
            int spins = ((head.next == s) ?
                    (timed ? maxTimedSpins : maxUntimedSpins) : 0);
            for (; ; ) {
                if (w.isInterrupted())
                    s.tryCancel(e);
                Object x = s.item;
                if (x != e)
                    return x;
                if (timed) {
                    nanos = deadline - System.nanoTime();
                    if (nanos <= 0L) {
                        s.tryCancel(e);
                        continue;
                    }
                }
                if (spins > 0)
                    --spins;
                else if (s.waiter == null)
                    s.waiter = w;
                else if (!timed)
                    LockSupport.park(this);
                else if (nanos > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanos);
            }
        }

        /**
         * Gets rid of cancelled node s with original predecessor pred.
         */
        void clean(QNode pred, QNode s) {
            s.waiter = null; // forget thread
            /*
             * At any given time, exactly one node on list cannot be
             * deleted -- the last inserted node. To accommodate this,
             * if we cannot delete s, we save its predecessor as
             * "cleanMe", deleting the previously saved version
             * first. At least one of node s or the node previously
             * saved can always be deleted, so this always terminates.
             */
            while (pred.next == s) { // Return early if already unlinked
                QNode h = head;
                QNode hn = h.next;   // Absorb cancelled first node as head
                if (hn != null && hn.isCancelled()) {
                    advanceHead(h, hn);
                    continue;
                }
                QNode t = tail;      // Ensure consistent read for tail
                if (t == h)
                    return;
                QNode tn = t.next;
                if (t != tail)
                    continue;
                if (tn != null) {
                    advanceTail(t, tn);
                    continue;
                }
                if (s != t) {        // If not tail, try to unsplice
                    QNode sn = s.next;
                    if (sn == s || pred.casNext(s, sn))
                        return;
                }
                QNode dp = cleanMe;
                if (dp != null) {    // Try unlinking previous cancelled node
                    QNode d = dp.next;
                    QNode dn;
                    if (d == null ||               // d is gone or
                            d == dp ||                 // d is off list or
                            !d.isCancelled() ||        // d not cancelled or
                            (d != t &&                 // d not tail and
                                    (dn = d.next) != null &&  //   has successor
                                    dn != d &&                //   that is on list
                                    dp.casNext(d, dn)))       // d unspliced
                        casCleanMe(dp, null);
                    if (dp == pred)
                        return;      // s is already saved node
                } else if (casCleanMe(null, pred))
                    return;          // Postpone cleaning s
            }
        }

        /**
         * Node class for TransferQueue.
         */
        static final class QNode {
            // Unsafe mechanics
            private static final sun.misc.Unsafe UNSAFE;
            private static final long itemOffset;
            private static final long nextOffset;

            static {
                try {
                    UNSAFE = sun.misc.Unsafe.getUnsafe();
                    Class<?> k = QNode.class;
                    itemOffset = UNSAFE.objectFieldOffset
                            (k.getDeclaredField("item"));
                    nextOffset = UNSAFE.objectFieldOffset
                            (k.getDeclaredField("next"));
                } catch (Exception e) {
                    throw new Error(e);
                }
            }

            final boolean isData;
            volatile QNode next;          // next node in queue
            volatile Object item;         // CAS'ed to or from null
            volatile Thread waiter;       // to control park/unpark

            QNode(Object item, boolean isData) {
                this.item = item;
                this.isData = isData;
            }

            boolean casNext(QNode cmp, QNode val) {
                return next == cmp &&
                        UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
            }

            boolean casItem(Object cmp, Object val) {
                return item == cmp &&
                        UNSAFE.compareAndSwapObject(this, itemOffset, cmp, val);
            }

            /**
             * Tries to cancel by CAS'ing ref to this as item.
             */
            void tryCancel(Object cmp) {
                UNSAFE.compareAndSwapObject(this, itemOffset, cmp, this);
            }

            boolean isCancelled() {
                return item == this;
            }

            /**
             * Returns true if this node is known to be off the queue
             * because its next pointer has been forgotten due to
             * an advanceHead operation.
             */
            boolean isOffList() {
                return next == this;
            }
        }
    }

    @SuppressWarnings("serial")
    static class WaitQueue implements java.io.Serializable {
    }

    static class LifoWaitQueue extends WaitQueue {
        private static final long serialVersionUID = -3633113410248163686L;
    }

    static class FifoWaitQueue extends WaitQueue {
        private static final long serialVersionUID = -3623113410248163686L;
    }

}
