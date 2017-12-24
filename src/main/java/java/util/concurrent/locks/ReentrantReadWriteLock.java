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

package java.util.concurrent.locks;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of {@link ReadWriteLock} supporting similar semantics to
 * {@link ReentrantLock}.
 * <p>
 * This class has the following properties:
 * <p>
 * <ul>
 * <li><b>Acquisition order</b>
 * <p>
 * <p>
 * This class does not impose a reader or writer preference ordering for lock
 * access. However, it does support an optional <em>fairness</em> policy.
 * <p>
 * <dl>
 * <dt><b><i>Non-fair mode (default)</i></b>
 * <dd>When constructed as non-fair (the default), the order of entry to the
 * read and write lock is unspecified, subject to reentrancy constraints. A
 * nonfair lock that is continuously contended may indefinitely postpone one or
 * more reader or writer threads, but will normally have higher throughput than
 * a fair lock.
 * <p>
 * <dt><b><i>Fair mode</i></b>
 * <dd>When constructed as fair, threads contend for entry using an
 * approximately arrival-order policy. When the currently held lock is released,
 * either the longest-waiting single writer thread will be assigned the write
 * lock, or if there is a group of reader threads waiting longer than all
 * waiting writer threads, that group will be assigned the read lock.
 * 公平模式
 * 当使用公平模式构造，线程使用近似到达的策略进入。当获取的锁被释放，要么是等待时间最长
 * 的单独的写线程获取锁，或者是超过所有写线程等待时间的是所有的读锁获取读锁，这个所有读锁
 * 的群组将会获得到读锁。
 * <p>
 * <p>
 * A thread that tries to acquire a fair read lock (non-reentrantly) will block
 * if either the write lock is held, or there is a waiting writer thread. The
 * thread will not acquire the read lock until after the oldest currently
 * waiting writer thread has acquired and released the write lock. Of course, if
 * a waiting writer abandons its wait, leaving one or more reader threads as the
 * longest waiters in the queue with the write lock free, then those readers
 * will be assigned the read lock.
 * 写锁被释放，所有的读锁等待上的线程将会获得读锁。这是在公平模式下。
 * <p>
 * <p>
 * A thread that tries to acquire a fair write lock (non-reentrantly) will block
 * unless both the read lock and write lock are free (which implies there are no
 * waiting threads). (Note that the non-blocking {@link ReadLock#tryLock()} and
 * {@link WriteLock#tryLock()} methods do not honor this fair setting and will
 * immediately acquire the lock if it is possible, regardless of waiting
 * threads.)
 *
 * <p>
 * </dl>
 * <p>
 * <li><b>Reentrancy</b>
 * <p>
 * <p>
 * This lock allows both readers and writers to reacquire read or write locks in
 * the style of a {@link ReentrantLock}. Non-reentrant readers are not allowed
 * until all write locks held by the writing thread have been released.
 * <p>
 * <p>
 * Additionally, a writer can acquire the read lock, but not vice-versa.
 * 一个写锁看可以请求一个读锁，但是返过来则不行。
 * Among
 * other applications, reentrancy can be useful when write locks are held during
 * calls or callbacks to methods that perform reads under read locks. If a
 * reader tries to acquire the write lock it will never succeed.
 * 一些其他的程序，重入是很有用的，当在一个方法引用或者回调方法中持有一个写锁，它任然能够获取
 * 到一个读锁。如果一个读锁在请求一个写锁，那么就会失败。
 * <p>
 * <li><b>Lock downgrading</b>
 * 锁降级
 * <p>
 * Reentrancy also allows downgrading from the write lock to a read lock, by
 * acquiring the write lock, then the read lock and then releasing the write
 * lock. However, upgrading from a read lock to the write lock is <b>not</b>
 * possible.
 *
 * 重入任然允许一个写锁降级为一个读锁，当持有一个写锁，然后再获取读锁此会释放写锁。然后
 * 从一个读锁降级到一个写锁则不可能。
 * <p>
 * <li><b>Interruption of lock acquisition</b>
 * 闭锁获取的中断
 * <p>
 * The read lock and write lock both support interruption during lock
 * acquisition.
 * 读锁和写锁再请求锁的同时，都支持中断。
 * <p>
 * <li><b>{@link Condition} support</b>
 * Condition支持
 * <p>
 * The write lock provides a {@link Condition} implementation that behaves in
 * the same way, with respect to the write lock, as the {@link Condition}
 * implementation provided by {@link ReentrantLock#newCondition} does for
 * {@link ReentrantLock}. This {@link Condition} can, of course, only be used
 * with the write lock.
 * 写锁支持Condition操作，动作从Condition继承过来的。为了支持写锁，从 {@link ReentrantLock#newCondition}
 * 提供的的操作来自ReentrantLock。当然Condition只支持写锁。
 * <p>
 * <p>
 * The read lock does not support a {@link Condition} and
 * {@code readLock().newCondition()} throws
 * {@code UnsupportedOperationException}.
 * 写锁不自持Condition，{@code readLock().newCondition()} 抛出
 * {@code UnsupportedOperationException}异常。
 * <p>
 * <li><b>Instrumentation</b>
 * 仪器，仪表
 * <p>
 * This class supports methods to determine whether locks are held or contended.
 * These methods are designed for monitoring system state, not for
 * synchronization control.
 * 这个类任然支持获取具体哪个线程持有锁或者是竞争锁。这些方法将被设计为监控系统状态，
 * 而不是同步控制。
 * </ul>
 * <p>
 * <p>
 * Serialization of this class behaves in the same way as built-in locks: a
 * deserialized lock is in the unlocked state, regardless of its state when
 * serialized.
 * 序列化操作任然支持锁，反序列化的锁都将是释放锁的状态，序列化时将不管她的状态。
 * <p>
 * <p>
 * <b>Sample usages</b>. Here is a code sketch showing how to perform lock
 * downgrading after updating a cache (exception handling is particularly tricky
 * when handling multiple locks in a non-nested fashion):
 * <p>
 * <pre>
 *  {@code
 * class CachedData {
 *   Object data;
 *   volatile boolean cacheValid;
 *   final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
 *
 *   void processCachedData() {
 *     rwl.readLock().lock();
 *     if (!cacheValid) {
 *       // Must release read lock before acquiring write lock   请求写锁前，必须释放读锁
 *       //因为读锁不能降级为写锁。读锁是共享锁，写锁是独占锁。
 *       rwl.readLock().unlock();
 *       rwl.writeLock().lock();
 *       try {
 *         // Recheck state because another thread might have
 *         // acquired write lock and changed state before we did.
 *         if (!cacheValid) {
 *           data = ...
 *           cacheValid = true;
 *         }
 *         // Downgrade by acquiring read lock before releasing write lock
 *         //在释放写锁前可以降级为读锁
 *         rwl.readLock().lock();
 *       } finally {
 *         rwl.writeLock().unlock(); // Unlock write, still hold read
 *       }
 *     }
 *
 *     try {
 *       use(data);
 *     } finally {
 *       rwl.readLock().unlock();
 *     }
 *   }
 * }}
 * </pre>
 * <p>
 * ReentrantReadWriteLocks can be used to improve concurrency in some uses of
 * some kinds of Collections. This is typically worthwhile only when the
 * collections are expected to be large, accessed by more reader threads than
 * writer threads, and entail operations with overhead that outweighs
 * synchronization overhead. For example, here is a class using a TreeMap that
 * is expected to be large and concurrently accessed.
 * <p>
 * <pre>
 * {
 * 	&#64;code
 * 	class RWDictionary {
 * 		private final Map<String, Data> m = new TreeMap<String, Data>();
 * 		private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
 * 		private final Lock r = rwl.readLock();
 * 		private final Lock w = rwl.writeLock();
 *
 * 		public Data get(String key) {
 * 			r.lock();
 * 			try {
 * 				return m.get(key);
 *            } finally {
 * 				r.unlock();
 *            }
 *        }
 *
 * 		public String[] allKeys() {
 * 			r.lock();
 * 			try {
 * 				return m.keySet().toArray();
 *            } finally {
 * 				r.unlock();
 *            }
 *        }
 *
 * 		public Data put(String key, Data value) {
 * 			w.lock();
 * 			try {
 * 				return m.put(key, value);
 *            } finally {
 * 				w.unlock();
 *            }
 *        }
 *
 * 		public void clear() {
 * 			w.lock();
 * 			try {
 * 				m.clear();
 *            } finally {
 * 				w.unlock();
 *            }
 *        }
 *    }
 * }
 * </pre>
 * <p>
 * <h3>Implementation Notes</h3>
 * <p>
 * <p>
 * This lock supports a maximum of 65535 recursive write locks and 65535 read
 * locks. Attempts to exceed these limits result in {@link Error} throws from
 * locking methods.
 *
 * @author Doug Lea
 * @since 1.5
 */
public class ReentrantReadWriteLock implements ReadWriteLock, java.io.Serializable {
    private static final long serialVersionUID = -6992448646407690164L;
    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long TID_OFFSET;

    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> tk = Thread.class;
            TID_OFFSET = UNSAFE.objectFieldOffset
                    (tk.getDeclaredField("tid"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * Performs all synchronization mechanics  执行所有的同步机制
     */
    final Sync sync;
    /**
     * Inner class providing readlock
     */
    private final ReentrantReadWriteLock.ReadLock readerLock;
    /**
     * Inner class providing writelock
     */
    private final ReentrantReadWriteLock.WriteLock writerLock;

    /**
     * 默认非公平锁
     * Creates a new {@code ReentrantReadWriteLock} with
     * default (nonfair) ordering properties.
     */
    public ReentrantReadWriteLock() {
        this(false);
    }

    /**
     * Creates a new {@code ReentrantReadWriteLock} with
     * the given fairness policy.
     *
     * @param fair {@code true} if this lock should use a fair ordering policy
     */
    public ReentrantReadWriteLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
        readerLock = new ReadLock(this);
        writerLock = new WriteLock(this);
    }

    /**
     * Returns the thread id for the given thread.  We must access
     * this directly rather than via method Thread.getId() because
     * getId() is not final, and has been known to be overridden in
     * ways that do not preserve unique mappings.
     */
    static final long getThreadId(Thread thread) {
        return UNSAFE.getLongVolatile(thread, TID_OFFSET);
    }

    public ReentrantReadWriteLock.WriteLock writeLock() {
        return writerLock;
    }

    public ReentrantReadWriteLock.ReadLock readLock() {
        return readerLock;
    }

    /**
     * Returns {@code true} if this lock has fairness set true.
     *
     * @return {@code true} if this lock has fairness set true
     */
    public final boolean isFair() {
        return sync instanceof FairSync;
    }

    // Instrumentation and status

    /**
     * Returns the thread that currently owns the write lock, or
     * {@code null} if not owned. When this method is called by a
     * thread that is not the owner, the return value reflects a
     * best-effort approximation of current lock status. For example,
     * the owner may be momentarily {@code null} even if there are
     * threads trying to acquire the lock but have not yet done so.
     * This method is designed to facilitate construction of
     * subclasses that provide more extensive lock monitoring
     * facilities.
     *
     * @return the owner, or {@code null} if not owned
     */
    protected Thread getOwner() {
        return sync.getOwner();
    }

    /**
     * Queries the number of read locks held for this lock. This
     * method is designed for use in monitoring system state, not for
     * synchronization control.
     *
     * @return the number of read locks held
     */
    public int getReadLockCount() {
        return sync.getReadLockCount();
    }

    /**
     * Queries if the write lock is held by any thread. This method is
     * designed for use in monitoring system state, not for
     * synchronization control.
     *
     * @return {@code true} if any thread holds the write lock and
     * {@code false} otherwise
     */
    public boolean isWriteLocked() {
        return sync.isWriteLocked();
    }

    /**
     * Queries if the write lock is held by the current thread.
     *
     * @return {@code true} if the current thread holds the write lock and
     * {@code false} otherwise
     */
    public boolean isWriteLockedByCurrentThread() {
        return sync.isHeldExclusively();
    }

    /**
     * Queries the number of reentrant write holds on this lock by the
     * current thread.  A writer thread has a hold on a lock for
     * each lock action that is not matched by an unlock action.
     *
     * @return the number of holds on the write lock by the current thread,
     * or zero if the write lock is not held by the current thread
     */
    public int getWriteHoldCount() {
        return sync.getWriteHoldCount();
    }

    /**
     * Queries the number of reentrant read holds on this lock by the
     * current thread.  A reader thread has a hold on a lock for
     * each lock action that is not matched by an unlock action.
     *
     * @return the number of holds on the read lock by the current thread,
     * or zero if the read lock is not held by the current thread
     * @since 1.6
     */
    public int getReadHoldCount() {
        return sync.getReadHoldCount();
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire the write lock.  Because the actual set of threads may
     * change dynamically while constructing this result, the returned
     * collection is only a best-effort estimate.  The elements of the
     * returned collection are in no particular order.  This method is
     * designed to facilitate construction of subclasses that provide
     * more extensive lock monitoring facilities.
     *
     * @return the collection of threads
     */
    protected Collection<Thread> getQueuedWriterThreads() {
        return sync.getExclusiveQueuedThreads();
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire the read lock.  Because the actual set of threads may
     * change dynamically while constructing this result, the returned
     * collection is only a best-effort estimate.  The elements of the
     * returned collection are in no particular order.  This method is
     * designed to facilitate construction of subclasses that provide
     * more extensive lock monitoring facilities.
     *
     * @return the collection of threads
     */
    protected Collection<Thread> getQueuedReaderThreads() {
        return sync.getSharedQueuedThreads();
    }

    /**
     * Queries whether any threads are waiting to acquire the read or
     * write lock. Note that because cancellations may occur at any
     * time, a {@code true} return does not guarantee that any other
     * thread will ever acquire a lock.  This method is designed
     * primarily for use in monitoring of the system state.
     *
     * @return {@code true} if there may be other threads waiting to
     * acquire the lock
     */
    public final boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    /**
     * Queries whether the given thread is waiting to acquire either
     * the read or write lock. Note that because cancellations may
     * occur at any time, a {@code true} return does not guarantee
     * that this thread will ever acquire a lock.  This method is
     * designed primarily for use in monitoring of the system state.
     *
     * @param thread the thread
     * @return {@code true} if the given thread is queued waiting for this lock
     * @throws NullPointerException if the thread is null
     */
    public final boolean hasQueuedThread(Thread thread) {
        return sync.isQueued(thread);
    }

    /**
     * Returns an estimate of the number of threads waiting to acquire
     * either the read or write lock.  The value is only an estimate
     * because the number of threads may change dynamically while this
     * method traverses internal data structures.  This method is
     * designed for use in monitoring of the system state, not for
     * synchronization control.
     *
     * @return the estimated number of threads waiting for this lock
     */
    public final int getQueueLength() {
        return sync.getQueueLength();
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire either the read or write lock.  Because the actual set
     * of threads may change dynamically while constructing this
     * result, the returned collection is only a best-effort estimate.
     * The elements of the returned collection are in no particular
     * order.  This method is designed to facilitate construction of
     * subclasses that provide more extensive monitoring facilities.
     *
     * @return the collection of threads
     */
    protected Collection<Thread> getQueuedThreads() {
        return sync.getQueuedThreads();
    }

    /**
     * Queries whether any threads are waiting on the given condition
     * associated with the write lock. Note that because timeouts and
     * interrupts may occur at any time, a {@code true} return does
     * not guarantee that a future {@code signal} will awaken any
     * threads.  This method is designed primarily for use in
     * monitoring of the system state.
     *
     * @param condition the condition
     * @return {@code true} if there are any waiting threads
     * @throws IllegalMonitorStateException if this lock is not held
     * @throws IllegalArgumentException     if the given condition is
     *                                      not associated with this lock
     * @throws NullPointerException         if the condition is null
     */
    public boolean hasWaiters(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.hasWaiters((AbstractQueuedSynchronizer.ConditionObject) condition);
    }

    /**
     * Returns an estimate of the number of threads waiting on the
     * given condition associated with the write lock. Note that because
     * timeouts and interrupts may occur at any time, the estimate
     * serves only as an upper bound on the actual number of waiters.
     * This method is designed for use in monitoring of the system
     * state, not for synchronization control.
     *
     * @param condition the condition
     * @return the estimated number of waiting threads
     * @throws IllegalMonitorStateException if this lock is not held
     * @throws IllegalArgumentException     if the given condition is
     *                                      not associated with this lock
     * @throws NullPointerException         if the condition is null
     */
    public int getWaitQueueLength(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.getWaitQueueLength((AbstractQueuedSynchronizer.ConditionObject) condition);
    }

    /**
     * Returns a collection containing those threads that may be
     * waiting on the given condition associated with the write lock.
     * Because the actual set of threads may change dynamically while
     * constructing this result, the returned collection is only a
     * best-effort estimate. The elements of the returned collection
     * are in no particular order.  This method is designed to
     * facilitate construction of subclasses that provide more
     * extensive condition monitoring facilities.
     *
     * @param condition the condition
     * @return the collection of threads
     * @throws IllegalMonitorStateException if this lock is not held
     * @throws IllegalArgumentException     if the given condition is
     *                                      not associated with this lock
     * @throws NullPointerException         if the condition is null
     */
    protected Collection<Thread> getWaitingThreads(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.getWaitingThreads((AbstractQueuedSynchronizer.ConditionObject) condition);
    }

    /**
     * Returns a string identifying this lock, as well as its lock state.
     * The state, in brackets, includes the String {@code "Write locks ="}
     * followed by the number of reentrantly held write locks, and the
     * String {@code "Read locks ="} followed by the number of held
     * read locks.
     *
     * @return a string identifying this lock, as well as its lock state
     */
    public String toString() {
        int c = sync.getCount();
        int w = Sync.exclusiveCount(c);
        int r = Sync.sharedCount(c);

        return super.toString() +
                "[Write locks = " + w + ", Read locks = " + r + "]";
    }

    /**
     * Synchronization implementation for ReentrantReadWriteLock.
     * Subclassed into fair and nonfair versions.
     * ReentrantReadWriteLock的同步操作，子类用于公平和非公平版本。
     */
    abstract static class Sync extends AbstractQueuedSynchronizer {
        static final int SHARED_SHIFT = 16;

        /*
         * Read vs write count extraction constants and functions.
         * Lock state is logically divided into two unsigned shorts:
         * The lower one representing the exclusive (writer) lock hold count,  低位代表独占锁（也就是写锁）的数量
         * and the upper the shared (reader) hold count.   高位代表共享锁（也就是读锁）代表的数量
         */
        static final int SHARED_UNIT = (1 << SHARED_SHIFT);
        static final int MAX_COUNT = (1 << SHARED_SHIFT) - 1;   //最大持有数量
        static final int EXCLUSIVE_MASK = (1 << SHARED_SHIFT) - 1;
        private static final long serialVersionUID = 6317671515068378041L;
        /**
         * The number of reentrant read locks held by current thread.
         * Initialized only in constructor and readObject.
         * Removed whenever a thread's read hold count drops to 0.
         * 当前线程持有的可重入读锁的数量，只能通过构造函数和readObject初始化。
         * 一个线程持有读锁的数量减到0时便会移除该对象。
         */
        private transient ThreadLocalHoldCounter readHolds;
        /**
         * The hold count of the last thread to successfully acquire
         * readLock.
         * 保存最后一个请求读锁成功的线程的请求数量
         * This saves ThreadLocal lookup in the common case
         * where the next thread to release is the last one to
         * acquire.
         * 这节省了左后一个请求的线程作为下一个线程释放
         * This is non-volatile since it is just used
         * as a heuristic, and would be great for threads to cache.
         * <p>
         * <p>Can outlive the Thread for which it is caching the read
         * hold count, but avoids garbage retention by not retaining a
         * reference to the Thread.
         * <p>
         * <p>Accessed via a benign data race; relies on the memory
         * model's final field and out-of-thin-air guarantees.
         */
        private transient HoldCounter cachedHoldCounter;
        /**
         * firstReader is the first thread to have acquired the read lock.
         * firstReaderHoldCount is firstReader's hold count.
         *
         * firstReader是第一个请求读锁的线程对象。而firstReaderHoldCount是firstReader
         * 持有读锁的数量。
         * <p>
         * <p>More precisely, firstReader is the unique thread that last
         * changed the shared count from 0 to 1, and has not released the
         * read lock since then; null if there is no such thread.
         *
         * <p>
         * <p>Cannot cause garbage retention unless the thread terminated
         * without relinquishing its read locks, since tryReleaseShared
         * sets it to null.
         * <p>
         * <p>Accessed via a benign data race; relies on the memory
         * model's out-of-thin-air guarantees for references.
         * <p>
         * <p>This allows tracking of read holds for uncontended read
         * locks to be very cheap.
         */
        private transient Thread firstReader = null;
        private transient int firstReaderHoldCount;

        Sync() {
            readHolds = new ThreadLocalHoldCounter();
            setState(getState()); // ensures visibility of readHolds
        }

        /**
         * 返回共享锁持有的数量
         * Returns the number of shared holds represented in count
         */
        static int sharedCount(int c) {
            return c >>> SHARED_SHIFT;
        }

        /**
         * 返回独占锁持有的数量
         * Returns the number of exclusive holds represented in count
         */
        static int exclusiveCount(int c) {
            return c & EXCLUSIVE_MASK;
        }

        /**
         * Returns true if the current thread, when trying to acquire
         * the read lock, and otherwise eligible to do so, should block
         * because of policy for overtaking other waiting threads.
         */
        abstract boolean readerShouldBlock();

        /**
         * Returns true if the current thread, when trying to acquire
         * the write lock, and otherwise eligible to do so, should block
         * because of policy for overtaking other waiting threads.
         */
        abstract boolean writerShouldBlock();

        /*
         * Acquires and releases use the same code for fair and
         * nonfair locks, but differ in whether/how they allow barging
         * when queues are non-empty.
         */

        protected final boolean tryRelease(int releases) {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            int nextc = getState() - releases;
            boolean free = exclusiveCount(nextc) == 0;
            if (free)
                setExclusiveOwnerThread(null);
            setState(nextc);
            return free;
        }

        protected final boolean tryAcquire(int acquires) {
            /*
             * Walkthrough:
             * 1. If read count nonzero or write count nonzero             1.如果读锁数量不为0或者写锁数量不为0且当前运行线程不为请求线程，那么获取失败
             *    and owner is a different thread, fail.
             * 2. If count would saturate, fail. (This can only            2.如果持有数量可能溢出，失败(只会在count非零的时候发生)
             *    happen if count is already nonzero.)
             * 3. Otherwise, this thread is eligible for lock if           3.除了上面两种情况以外，要么是已经获取锁再次重入或者队列策略允许便能获取锁，
             *    it is either a reentrant acquire or                        如果满足以上情况，那么更新状态并且设置锁的所有者。
             *    queue policy allows it. If so, update state
             *    and set owner.
             */
            Thread current = Thread.currentThread();
            int c = getState();         //返回已请求数量
            int w = exclusiveCount(c);   //返回独占持有数量
            if (c != 0) {
                // (Note: if c != 0 and w == 0 then shared count != 0)
                //注意，这里如果c!=0且w==0,表示shared的数量不为0
                //但是如果此时w!=0,代表shared为0,但是独占锁不为0,所以此时需要判断持有锁的线程是否为当前线程
                if (w == 0 || current != getExclusiveOwnerThread())    //独占持有为0.且当前持有锁的线程不为请求锁的线程，失败
                    return false;
                if (w + exclusiveCount(acquires) > MAX_COUNT)    //溢出
                    throw new Error("Maximum lock count exceeded");
                // Reentrant acquire
                //程序到这里表示shared为0,但是写锁被持有,且持有写锁的线程就是当前线程
                setState(c + acquires);   //设置请求数量,因为持有锁的线程就是当前线程,所以更新状态就可以了
                return true;           //请求成功
            }
            //请求数量为0
            if (writerShouldBlock() || !compareAndSetState(c, c + acquires))
                return false;
            //请求成功,设置当前读锁的持有者为当前请求线程
            setExclusiveOwnerThread(current);
            return true;
        }

        /*
         * Note that tryRelease and tryAcquire can be called by
         * Conditions. So it is possible that their arguments contain
         * both read and write holds that are all released during a
         * condition wait and re-established in tryAcquire.
         * 注意,tryRelease和tryAcquire可能被Conditions调用。
         */

        protected final boolean tryReleaseShared(int unused) {
            Thread current = Thread.currentThread();
            if (firstReader == current) {
                // assert firstReaderHoldCount > 0;支持有一次
                if (firstReaderHoldCount == 1)
                    firstReader = null;
                else
                    firstReaderHoldCount--;
            } else {
                HoldCounter rh = cachedHoldCounter;
                if (rh == null || rh.tid != getThreadId(current))
                    rh = readHolds.get();
                int count = rh.count;
                if (count <= 1) {
                    readHolds.remove();
                    if (count <= 0)
                        throw unmatchedUnlockException();
                }
                --rh.count;
            }
            for (; ; ) {
                int c = getState();
                int nextc = c - SHARED_UNIT;
                if (compareAndSetState(c, nextc))
                    // Releasing the read lock has no effect on readers,
                    // but it may allow waiting writers to proceed if
                    // both read and write locks are now free.
                    return nextc == 0;
            }
        }

        private IllegalMonitorStateException unmatchedUnlockException() {
            return new IllegalMonitorStateException("attempt to unlock read lock, not locked by current thread");
        }

        /**
         * 用于共享锁获取
         * 如果被一个独占锁获取，那么获取共享失败
         */
        protected final int tryAcquireShared(int unused) {
            /*
             * Walkthrough:
             * 1. If write lock held by another thread, fail.  1.如果写锁被另一个线程持有，失败
             * 2. Otherwise, this thread is eligible for       2.除此之外，这个线程有资格获取读锁，但是需要判断一下是否因为队列策略需要阻塞
             *    lock wrt state, so ask if it should block      如果不需要阻塞，那么CAS修改状态，并且更新count字段。
             *    because of queue policy. If not, try           注意，这些步骤不需要判断是否是重入请求，
             *    to grant by CASing state and updating count.
             *    Note that step does not check for reentrant
             *    acquires, which is postponed to full version
             *    to avoid having to check hold count in
             *    the more typical non-reentrant case.
             * 3. If step 2 fails either because thread              3.步骤2失败，要么线程没有资格获取，要么CAS操作失败，要么锁的数量已经满了
             *    apparently not eligible or CAS fails or count        然后到重试版本
             *    saturated, chain to version with full retry loop.
             */
            Thread current = Thread.currentThread();
            int c = getState();
            //独占锁不为0且获取独占锁的线程不是当前线程，返回-1
            if (exclusiveCount(c) != 0 && getExclusiveOwnerThread() != current)  //如有独占锁持有量不为0，且持有锁的线程不是当前线程，返回-1
                return -1;
            //程序走到这里，表示持有独占锁的数量为0，或者当前线程已经持有独占锁
            int r = sharedCount(c);   //共享锁持有的数量
            //如果不需要阻塞，且不会溢出，且设置请求数量成功
            if (!readerShouldBlock() && r < MAX_COUNT && compareAndSetState(c, c + SHARED_UNIT)) {
                if (r == 0) {  //没有线程持有共享锁,设置firstReader
                    firstReader = current;
                    firstReaderHoldCount = 1;
                } else if (firstReader == current) {  //判断firstReader是否是当前线程
                    firstReaderHoldCount++;
                } else {
                    HoldCounter rh = cachedHoldCounter;
                    if (rh == null || rh.tid != getThreadId(current))
                        cachedHoldCounter = rh = readHolds.get();
                    else if (rh.count == 0)
                        readHolds.set(rh);
                    rh.count++;
                }
                return 1;
            }
            return fullTryAcquireShared(current);
        }

        /**
         * Full version of acquire for reads, that handles CAS misses
         * and reentrant reads not dealt with in tryAcquireShared.
         * 读锁请求的Full版本
         */
        final int fullTryAcquireShared(Thread current) {
            /*
			 * This code is in part redundant with that in tryAcquireShared but
			 * is simpler overall by not complicating tryAcquireShared with
			 * interactions between retries and lazily reading hold counts.
			 */
            HoldCounter rh = null;
            for (; ; ) {
                int c = getState();
                if (exclusiveCount(c) != 0) {
                    if (getExclusiveOwnerThread() != current)
                        return -1;
                    // else we hold the exclusive lock; blocking here
                    // would cause deadlock.
                } else if (readerShouldBlock()) {
                    // Make sure we're not acquiring read lock reentrantly
                    if (firstReader == current) {
                        // assert firstReaderHoldCount > 0;
                    } else {
                        if (rh == null) {
                            rh = cachedHoldCounter;
                            if (rh == null || rh.tid != getThreadId(current)) {
                                rh = readHolds.get();
                                if (rh.count == 0)
                                    readHolds.remove();
                            }
                        }
                        if (rh.count == 0)
                            return -1;
                    }
                }
                if (sharedCount(c) == MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
                if (compareAndSetState(c, c + SHARED_UNIT)) {
                    if (sharedCount(c) == 0) {
                        firstReader = current;
                        firstReaderHoldCount = 1;
                    } else if (firstReader == current) {
                        firstReaderHoldCount++;
                    } else {
                        if (rh == null)
                            rh = cachedHoldCounter;
                        if (rh == null || rh.tid != getThreadId(current))
                            rh = readHolds.get();
                        else if (rh.count == 0)
                            readHolds.set(rh);
                        rh.count++;
                        cachedHoldCounter = rh; // cache for release
                    }
                    return 1;
                }
            }
        }

        /**
         * Performs tryLock for write, enabling barging in both modes.
         * This is identical in effect to tryAcquire except for lack
         * of calls to writerShouldBlock.
         */
        final boolean tryWriteLock() {
            Thread current = Thread.currentThread();
            int c = getState();
            if (c != 0) {
                int w = exclusiveCount(c);   //读锁不能升级为写锁
                if (w == 0 || current != getExclusiveOwnerThread())  //说明有线程获取读锁，且持有锁的线程并不是当前线程
                    return false;
                if (w == MAX_COUNT)    //溢出
                    throw new Error("Maximum lock count exceeded");
            }
            if (!compareAndSetState(c, c + 1))   //获取锁失败（修改状态失败）
                return false;
            setExclusiveOwnerThread(current);   //获取所成功，当前持有锁的线程设置为当前请求锁的线程，且返回true
            return true;
        }

        /**
         * Performs tryLock for read, enabling barging in both modes.
         * This is identical in effect to tryAcquireShared except for
         * lack of calls to readerShouldBlock.
         */
        final boolean tryReadLock() {
            Thread current = Thread.currentThread();
            for (; ; ) {
                int c = getState();
                if (exclusiveCount(c) != 0 &&
                        getExclusiveOwnerThread() != current)
                    return false;
                int r = sharedCount(c);
                if (r == MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
                if (compareAndSetState(c, c + SHARED_UNIT)) {
                    if (r == 0) {
                        firstReader = current;
                        firstReaderHoldCount = 1;
                    } else if (firstReader == current) {
                        firstReaderHoldCount++;
                    } else {
                        HoldCounter rh = cachedHoldCounter;
                        //如果th为空，或者获取锁的线程不是当前线程，那么则再次获取一个HoldCounter
                        if (rh == null || rh.tid != getThreadId(current))
                            cachedHoldCounter = rh = readHolds.get();
                        else if (rh.count == 0)
                            readHolds.set(rh);
                        rh.count++;
                    }
                    return true;
                }
            }
        }

        protected final boolean isHeldExclusively() {
            // While we must in general read state before owner,
            // we don't need to do so to check if current thread is owner
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        final ConditionObject newCondition() {
            return new ConditionObject();
        }

        final Thread getOwner() {
            // Must read state before owner to ensure memory consistency
            return ((exclusiveCount(getState()) == 0) ?
                    null :
                    getExclusiveOwnerThread());
        }

        // Methods relayed to outer class

        final int getReadLockCount() {
            return sharedCount(getState());
        }

        final boolean isWriteLocked() {
            return exclusiveCount(getState()) != 0;
        }

        final int getWriteHoldCount() {
            return isHeldExclusively() ? exclusiveCount(getState()) : 0;
        }

        final int getReadHoldCount() {
            if (getReadLockCount() == 0)
                return 0;

            Thread current = Thread.currentThread();
            if (firstReader == current)
                return firstReaderHoldCount;

            HoldCounter rh = cachedHoldCounter;
            if (rh != null && rh.tid == getThreadId(current))
                return rh.count;

            int count = readHolds.get().count;
            if (count == 0) readHolds.remove();
            return count;
        }

        /**
         * Reconstitutes the instance from a stream (that is, deserializes it).
         */
        private void readObject(java.io.ObjectInputStream s)
                throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            readHolds = new ThreadLocalHoldCounter();
            setState(0); // reset to unlocked state
        }

        final int getCount() {
            return getState();
        }

        /**
         * 用于每个线程保存计数的计数器,主要用于ThreadLocal,缓存到cachedHoldCounter
         * A counter for per-thread read hold counts.
         * Maintained as a ThreadLocal; cached in cachedHoldCounter
         */
        static final class HoldCounter {
            // Use id, not reference, to avoid garbage retention
            // 使用id,而不是引用,来避免保持垃圾回收,当期线程的id
            final long tid = getThreadId(Thread.currentThread());
            int count = 0;
        }

        /**
         * ThreadLocal subclass. Easiest to explicitly define for sake
         * of deserialization mechanics.
         * ThreadLocal的子类，为了反序列化机制非常方便的显示定义
         */
        static final class ThreadLocalHoldCounter extends ThreadLocal<HoldCounter> {
            public HoldCounter initialValue() {  //初始化
                return new HoldCounter();
            }
        }
    }

    /**
     * Nonfair version of Sync
     */
    static final class NonfairSync extends Sync {
        private static final long serialVersionUID = -8159625535654395037L;

        final boolean writerShouldBlock() {  //对于非公平锁，任何时候都可以抢占
            return false; // writers can always barge
        }

        final boolean readerShouldBlock() {
            /* As a heuristic to avoid indefinite writer starvation,
             * block if the thread that momentarily appears to be head
             * of queue, if one exists, is a waiting writer.  This is
             * only a probabilistic effect since a new reader will not
             * block if there is a waiting writer behind other enabled
             * readers that have not yet drained from the queue.
             */
            return apparentlyFirstQueuedIsExclusive();
        }
    }

    /**
     * Fair version of Sync
     */
    static final class FairSync extends Sync {
        private static final long serialVersionUID = -2274990926593161451L;

        /**
         * 判断是否应该阻塞，如果有节点等待时间超多当前线程，那么返回true，
         * 否则返回false
         */
        final boolean writerShouldBlock() {
            return hasQueuedPredecessors();
        }

        /**
         * 判断是否应该阻塞，如果有节点等待时间超多当前线程，那么返回true，
         * 否则返回false
         */
        final boolean readerShouldBlock() {
            return hasQueuedPredecessors();
        }
    }

    /**
     * The lock returned by method {@link ReentrantReadWriteLock#readLock}.
     */
    public static class ReadLock implements Lock, java.io.Serializable {
        private static final long serialVersionUID = -5992448646407690164L;
        private final Sync sync;

        /**
         * Constructor for use by subclasses
         *
         * @param lock the outer lock object
         * @throws NullPointerException if the lock is null
         */
        protected ReadLock(ReentrantReadWriteLock lock) {
            sync = lock.sync;
        }

        /**
         * Acquires the read lock.
         * <p>
         * <p>Acquires the read lock if the write lock is not held by
         * another thread and returns immediately.
         * <p>
         * <p>If the write lock is held by another thread then
         * the current thread becomes disabled for thread scheduling
         * purposes and lies dormant until the read lock has been acquired.
         */
        public void lock() {
            sync.acquireShared(1);
        }

        /**
         * Acquires the read lock unless the current thread is
         * {@linkplain Thread#interrupt interrupted}.
         * <p>
         * <p>Acquires the read lock if the write lock is not held
         * by another thread and returns immediately.
         * <p>
         * <p>If the write lock is held by another thread then the
         * current thread becomes disabled for thread scheduling
         * purposes and lies dormant until one of two things happens:
         * <p>
         * <ul>
         * <p>
         * <li>The read lock is acquired by the current thread; or
         * <p>
         * <li>Some other thread {@linkplain Thread#interrupt interrupts}
         * the current thread.
         * <p>
         * </ul>
         * <p>
         * <p>If the current thread:
         * <p>
         * <ul>
         * <p>
         * <li>has its interrupted status set on entry to this method; or
         * <p>
         * <li>is {@linkplain Thread#interrupt interrupted} while
         * acquiring the read lock,
         * <p>
         * </ul>
         * <p>
         * then {@link InterruptedException} is thrown and the current
         * thread's interrupted status is cleared.
         * <p>
         * <p>In this implementation, as this method is an explicit
         * interruption point, preference is given to responding to
         * the interrupt over normal or reentrant acquisition of the
         * lock.
         *
         * @throws InterruptedException if the current thread is interrupted
         */
        public void lockInterruptibly() throws InterruptedException {
            sync.acquireSharedInterruptibly(1);
        }

        /**
         * Acquires the read lock only if the write lock is not held by
         * another thread at the time of invocation.
         * <p>
         * <p>Acquires the read lock if the write lock is not held by
         * another thread and returns immediately with the value
         * {@code true}. Even when this lock has been set to use a
         * fair ordering policy, a call to {@code tryLock()}
         * <em>will</em> immediately acquire the read lock if it is
         * available, whether or not other threads are currently
         * waiting for the read lock.  This &quot;barging&quot; behavior
         * can be useful in certain circumstances, even though it
         * breaks fairness. If you want to honor the fairness setting
         * for this lock, then use {@link #tryLock(long, TimeUnit)
         * tryLock(0, TimeUnit.SECONDS) } which is almost equivalent
         * (it also detects interruption).
         * <p>
         * <p>If the write lock is held by another thread then
         * this method will return immediately with the value
         * {@code false}.
         *
         * @return {@code true} if the read lock was acquired
         */
        public boolean tryLock() {
            return sync.tryReadLock();
        }

        /**
         * Acquires the read lock if the write lock is not held by
         * another thread within the given waiting time and the
         * current thread has not been {@linkplain Thread#interrupt
         * interrupted}.
         * <p>
         * <p>Acquires the read lock if the write lock is not held by
         * another thread and returns immediately with the value
         * {@code true}. If this lock has been set to use a fair
         * ordering policy then an available lock <em>will not</em> be
         * acquired if any other threads are waiting for the
         * lock. This is in contrast to the {@link #tryLock()}
         * method. If you want a timed {@code tryLock} that does
         * permit barging on a fair lock then combine the timed and
         * un-timed forms together:
         * <p>
         * <pre> {@code
         * if (lock.tryLock() ||
         *     lock.tryLock(timeout, unit)) {
         *   ...
         * }}</pre>
         * <p>
         * <p>If the write lock is held by another thread then the
         * current thread becomes disabled for thread scheduling
         * purposes and lies dormant until one of three things happens:
         * <p>
         * <ul>
         * <p>
         * <li>The read lock is acquired by the current thread; or
         * <p>
         * <li>Some other thread {@linkplain Thread#interrupt interrupts}
         * the current thread; or
         * <p>
         * <li>The specified waiting time elapses.
         * <p>
         * </ul>
         * <p>
         * <p>If the read lock is acquired then the value {@code true} is
         * returned.
         * <p>
         * <p>If the current thread:
         * <p>
         * <ul>
         * <p>
         * <li>has its interrupted status set on entry to this method; or
         * <p>
         * <li>is {@linkplain Thread#interrupt interrupted} while
         * acquiring the read lock,
         * <p>
         * </ul> then {@link InterruptedException} is thrown and the
         * current thread's interrupted status is cleared.
         * <p>
         * <p>If the specified waiting time elapses then the value
         * {@code false} is returned.  If the time is less than or
         * equal to zero, the method will not wait at all.
         * <p>
         * <p>In this implementation, as this method is an explicit
         * interruption point, preference is given to responding to
         * the interrupt over normal or reentrant acquisition of the
         * lock, and over reporting the elapse of the waiting time.
         *
         * @param timeout the time to wait for the read lock
         * @param unit    the time unit of the timeout argument
         * @return {@code true} if the read lock was acquired
         * @throws InterruptedException if the current thread is interrupted
         * @throws NullPointerException if the time unit is null
         */
        public boolean tryLock(long timeout, TimeUnit unit)
                throws InterruptedException {
            return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
        }

        /**
         * Attempts to release this lock.
         * <p>
         * <p>If the number of readers is now zero then the lock
         * is made available for write lock attempts.
         */
        public void unlock() {
            sync.releaseShared(1);
        }

        /**
         * Throws {@code UnsupportedOperationException} because
         * {@code ReadLocks} do not support conditions.
         *
         * @throws UnsupportedOperationException always
         */
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }

        /**
         * Returns a string identifying this lock, as well as its lock state.
         * The state, in brackets, includes the String {@code "Read locks ="}
         * followed by the number of held read locks.
         *
         * @return a string identifying this lock, as well as its lock state
         */
        public String toString() {
            int r = sync.getReadLockCount();
            return super.toString() +
                    "[Read locks = " + r + "]";
        }
    }

    /**
     * The lock returned by method {@link ReentrantReadWriteLock#writeLock}.
     */
    public static class WriteLock implements Lock, java.io.Serializable {
        private static final long serialVersionUID = -4992448646407690164L;
        private final Sync sync;

        /**
         * Constructor for use by subclasses
         *
         * @param lock the outer lock object
         * @throws NullPointerException if the lock is null
         */
        protected WriteLock(ReentrantReadWriteLock lock) {
            sync = lock.sync;
        }

        /**
         * Acquires the write lock.
         * <p>
         * <p>Acquires the write lock if neither the read nor write lock
         * are held by another thread
         * and returns immediately, setting the write lock hold count to
         * one.
         * <p>
         * <p>If the current thread already holds the write lock then the
         * hold count is incremented by one and the method returns
         * immediately.
         * <p>
         * <p>If the lock is held by another thread then the current
         * thread becomes disabled for thread scheduling purposes and
         * lies dormant until the write lock has been acquired, at which
         * time the write lock hold count is set to one.
         */
        public void lock() {
            sync.acquire(1);
        }

        /**
         * Acquires the write lock unless the current thread is
         * {@linkplain Thread#interrupt interrupted}.
         * <p>
         * <p>Acquires the write lock if neither the read nor write lock
         * are held by another thread
         * and returns immediately, setting the write lock hold count to
         * one.
         * <p>
         * <p>If the current thread already holds this lock then the
         * hold count is incremented by one and the method returns
         * immediately.
         * <p>
         * <p>If the lock is held by another thread then the current
         * thread becomes disabled for thread scheduling purposes and
         * lies dormant until one of two things happens:
         * <p>
         * <ul>
         * <p>
         * <li>The write lock is acquired by the current thread; or
         * <p>
         * <li>Some other thread {@linkplain Thread#interrupt interrupts}
         * the current thread.
         * <p>
         * </ul>
         * <p>
         * <p>If the write lock is acquired by the current thread then the
         * lock hold count is set to one.
         * <p>
         * <p>If the current thread:
         * <p>
         * <ul>
         * <p>
         * <li>has its interrupted status set on entry to this method;
         * or
         * <p>
         * <li>is {@linkplain Thread#interrupt interrupted} while
         * acquiring the write lock,
         * <p>
         * </ul>
         * <p>
         * then {@link InterruptedException} is thrown and the current
         * thread's interrupted status is cleared.
         * <p>
         * <p>In this implementation, as this method is an explicit
         * interruption point, preference is given to responding to
         * the interrupt over normal or reentrant acquisition of the
         * lock.
         *
         * @throws InterruptedException if the current thread is interrupted
         */
        public void lockInterruptibly() throws InterruptedException {
            sync.acquireInterruptibly(1);
        }

        /**
         * Acquires the write lock only if it is not held by another thread
         * at the time of invocation.
         * <p>
         * <p>Acquires the write lock if neither the read nor write lock
         * are held by another thread
         * and returns immediately with the value {@code true},
         * setting the write lock hold count to one. Even when this lock has
         * been set to use a fair ordering policy, a call to
         * {@code tryLock()} <em>will</em> immediately acquire the
         * lock if it is available, whether or not other threads are
         * currently waiting for the write lock.  This &quot;barging&quot;
         * behavior can be useful in certain circumstances, even
         * though it breaks fairness. If you want to honor the
         * fairness setting for this lock, then use {@link
         * #tryLock(long, TimeUnit) tryLock(0, TimeUnit.SECONDS) }
         * which is almost equivalent (it also detects interruption).
         * <p>
         * <p>If the current thread already holds this lock then the
         * hold count is incremented by one and the method returns
         * {@code true}.
         * <p>
         * <p>If the lock is held by another thread then this method
         * will return immediately with the value {@code false}.
         *
         * @return {@code true} if the lock was free and was acquired
         * by the current thread, or the write lock was already held
         * by the current thread; and {@code false} otherwise.
         */
        public boolean tryLock() {
            return sync.tryWriteLock();
        }

        /**
         * Acquires the write lock if it is not held by another thread
         * within the given waiting time and the current thread has
         * not been {@linkplain Thread#interrupt interrupted}.
         * <p>
         * <p>Acquires the write lock if neither the read nor write lock
         * are held by another thread
         * and returns immediately with the value {@code true},
         * setting the write lock hold count to one. If this lock has been
         * set to use a fair ordering policy then an available lock
         * <em>will not</em> be acquired if any other threads are
         * waiting for the write lock. This is in contrast to the {@link
         * #tryLock()} method. If you want a timed {@code tryLock}
         * that does permit barging on a fair lock then combine the
         * timed and un-timed forms together:
         * <p>
         * <pre> {@code
         * if (lock.tryLock() ||
         *     lock.tryLock(timeout, unit)) {
         *   ...
         * }}</pre>
         * <p>
         * <p>If the current thread already holds this lock then the
         * hold count is incremented by one and the method returns
         * {@code true}.
         * <p>
         * <p>If the lock is held by another thread then the current
         * thread becomes disabled for thread scheduling purposes and
         * lies dormant until one of three things happens:
         * <p>
         * <ul>
         * <p>
         * <li>The write lock is acquired by the current thread; or
         * <p>
         * <li>Some other thread {@linkplain Thread#interrupt interrupts}
         * the current thread; or
         * <p>
         * <li>The specified waiting time elapses
         * <p>
         * </ul>
         * <p>
         * <p>If the write lock is acquired then the value {@code true} is
         * returned and the write lock hold count is set to one.
         * <p>
         * <p>If the current thread:
         * <p>
         * <ul>
         * <p>
         * <li>has its interrupted status set on entry to this method;
         * or
         * <p>
         * <li>is {@linkplain Thread#interrupt interrupted} while
         * acquiring the write lock,
         * <p>
         * </ul>
         * <p>
         * then {@link InterruptedException} is thrown and the current
         * thread's interrupted status is cleared.
         * <p>
         * <p>If the specified waiting time elapses then the value
         * {@code false} is returned.  If the time is less than or
         * equal to zero, the method will not wait at all.
         * <p>
         * <p>In this implementation, as this method is an explicit
         * interruption point, preference is given to responding to
         * the interrupt over normal or reentrant acquisition of the
         * lock, and over reporting the elapse of the waiting time.
         *
         * @param timeout the time to wait for the write lock
         * @param unit    the time unit of the timeout argument
         * @return {@code true} if the lock was free and was acquired
         * by the current thread, or the write lock was already held by the
         * current thread; and {@code false} if the waiting time
         * elapsed before the lock could be acquired.
         * @throws InterruptedException if the current thread is interrupted
         * @throws NullPointerException if the time unit is null
         */
        public boolean tryLock(long timeout, TimeUnit unit)
                throws InterruptedException {
            return sync.tryAcquireNanos(1, unit.toNanos(timeout));
        }

        /**
         * Attempts to release this lock.
         * <p>
         * <p>If the current thread is the holder of this lock then
         * the hold count is decremented. If the hold count is now
         * zero then the lock is released.  If the current thread is
         * not the holder of this lock then {@link
         * IllegalMonitorStateException} is thrown.
         *
         * @throws IllegalMonitorStateException if the current thread does not
         *                                      hold this lock
         */
        public void unlock() {
            sync.release(1);
        }

        /**
         * Returns a {@link Condition} instance for use with this
         * {@link Lock} instance.
         * <p>The returned {@link Condition} instance supports the same
         * usages as do the {@link Object} monitor methods ({@link
         * Object#wait() wait}, {@link Object#notify notify}, and {@link
         * Object#notifyAll notifyAll}) when used with the built-in
         * monitor lock.
         * <p>
         * <ul>
         * <p>
         * <li>If this write lock is not held when any {@link
         * Condition} method is called then an {@link
         * IllegalMonitorStateException} is thrown.  (Read locks are
         * held independently of write locks, so are not checked or
         * affected. However it is essentially always an error to
         * invoke a condition waiting method when the current thread
         * has also acquired read locks, since other threads that
         * could unblock it will not be able to acquire the write
         * lock.)
         * <p>
         * <li>When the condition {@linkplain Condition#await() waiting}
         * methods are called the write lock is released and, before
         * they return, the write lock is reacquired and the lock hold
         * count restored to what it was when the method was called.
         * <p>
         * <li>If a thread is {@linkplain Thread#interrupt interrupted} while
         * waiting then the wait will terminate, an {@link
         * InterruptedException} will be thrown, and the thread's
         * interrupted status will be cleared.
         * <p>
         * <li> Waiting threads are signalled in FIFO order.
         * <p>
         * <li>The ordering of lock reacquisition for threads returning
         * from waiting methods is the same as for threads initially
         * acquiring the lock, which is in the default case not specified,
         * but for <em>fair</em> locks favors those threads that have been
         * waiting the longest.
         * <p>
         * </ul>
         *
         * @return the Condition object
         */
        public Condition newCondition() {
            return sync.newCondition();
        }

        /**
         * Returns a string identifying this lock, as well as its lock
         * state.  The state, in brackets includes either the String
         * {@code "Unlocked"} or the String {@code "Locked by"}
         * followed by the {@linkplain Thread#getName name} of the owning thread.
         *
         * @return a string identifying this lock, as well as its lock state
         */
        public String toString() {
            Thread o = sync.getOwner();
            return super.toString() + ((o == null) ?
                    "[Unlocked]" :
                    "[Locked by thread " + o.getName() + "]");
        }

        /**
         * Queries if this write lock is held by the current thread.
         * Identical in effect to {@link
         * ReentrantReadWriteLock#isWriteLockedByCurrentThread}.
         *
         * @return {@code true} if the current thread holds this lock and
         * {@code false} otherwise
         * @since 1.6
         */
        public boolean isHeldByCurrentThread() {
            return sync.isHeldExclusively();
        }

        /**
         * Queries the number of holds on this write lock by the current
         * thread.  A thread has a hold on a lock for each lock action
         * that is not matched by an unlock action.  Identical in effect
         * to {@link ReentrantReadWriteLock#getWriteHoldCount}.
         *
         * @return the number of holds on this lock by the current thread,
         * or zero if this lock is not held by the current thread
         * @since 1.6
         */
        public int getHoldCount() {
            return sync.getWriteHoldCount();
        }
    }

}
