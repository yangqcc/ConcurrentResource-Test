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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import sun.misc.Unsafe;

/**
 * Provides a framework for implementing blocking locks and related
 * synchronizers (semaphores, events, etc) that rely on first-in-first-out
 * (FIFO) wait queues. This class is designed to be a useful basis for most
 * kinds of synchronizers that rely on a single atomic {@code int} value to
 * represent state. Subclasses must define the protected methods that change
 * this state, and which define what that state means in terms of this object
 * being acquired or released. Given these, the other methods in this class
 * carry out all queuing and blocking mechanics. Subclasses can maintain other
 * state fields, but only the atomically updated {@code int} value manipulated
 * using methods {@link #getState}, {@link #setState} and
 * {@link #compareAndSetState} is tracked with respect to synchronization.
 * 子类更新int值只能使用getState，setState以及compareAnsSetState方法来进行同步有关的操作。
 * <p>
 * <p>
 * Subclasses should be defined as non-public internal helper classes that are
 * used to implement the synchronization properties of their enclosing class.
 * 子类应该定义一个内部类，作为类的封闭类来进行操作。
 * Class {@code AbstractQueuedSynchronizer} does not implement any
 * synchronization interface.
 * 当前类没有继承任何同步接口。
 * Instead it defines methods such as
 * {@link #acquireInterruptibly} that can be invoked as appropriate by concrete
 * locks and related synchronizers to implement their public methods.
 * 取而代之的是像acquireInterruptibly之类的方法，在进行锁和同步操作时的共有方法中来引用这个方法
 * <p>
 * <p>
 * This class supports either or both a default <em>exclusive</em> mode and a
 * <em>shared</em> mode.
 * 独占模式和共享模式
 * When acquired in exclusive mode, attempted acquires by
 * other threads cannot succeed.
 * 独占模式请求成功，其他线程请求时就会失败。
 * Shared mode acquires by multiple threads may
 * (but need not) succeed.
 * 共享模式，多个线程同时请求，可能成功，也可能不成功
 * This class does not &quot;understand&quot; these
 * differences except in the mechanical sense that when a shared mode acquire
 * succeeds, the next waiting thread (if one exists) must also determine whether
 * it can acquire as well. Threads waiting in the different modes share the same
 * FIFO queue. Usually, implementation subclasses support only one of these
 * modes, but both can come into play for example in a {@link ReadWriteLock}.
 * 子类只能实现一种模式，但是两种模式可以混合到一起进行操作。
 * Subclasses that support only exclusive or only shared modes need not define
 * the methods supporting the unused mode.
 * 子类要么只支持独占模式，或者共享模式，但是选择所支持的一种模式后，并不需要定义支持另一只模式的
 * 方法。
 * <p>
 * <p>
 * This class defines a nested {@link ConditionObject} class that can be used as
 * a {@link Condition} implementation by subclasses supporting exclusive mode
 * for which method {@link #isHeldExclusively} reports whether synchronization
 * is exclusively held with respect to the current thread, method
 * {@link #release} invoked with the current {@link #getState} value fully
 * releases this object, and {@link #acquire}, given this saved state value,
 * eventually restores this object to its previous acquired state. No
 * {@code AbstractQueuedSynchronizer} method otherwise creates such a condition,
 * so if this constraint cannot be met, do not use it. The behavior of
 * {@link ConditionObject} depends of course on the semantics of its
 * synchronizer implementation.
 * <p>
 * <p>
 * This class provides inspection, instrumentation, and monitoring methods for
 * the internal queue, as well as similar methods for condition objects. These
 * can be exported as desired into classes using an
 * {@code AbstractQueuedSynchronizer} for their synchronization mechanics.
 * <p>
 * <p>
 * Serialization of this class stores only the underlying atomic integer
 * maintaining state, so deserialized objects have empty thread queues. Typical
 * subclasses requiring serializability will define a {@code readObject} method
 * that restores this to a known initial state upon deserialization.
 *
 * 当前类序列化只会保存内部integer类型变量的信息，所以反序列化得到的对象将包含一个空的队列。
 * 一个子类要定义readObject来保存这些信息,用于反序列话时初始化状态。
 * <p>
 * <h3>Usage</h3>
 * <p>
 * <p>
 * 要使用这个类，需要定义以下的方法，内部修改状态通过getState以及setState和compareAndSetState
 * 进行操作。
 * To use this class as the basis of a synchronizer, redefine the following
 * methods, as applicable, by inspecting and/or modifying the synchronization
 * state using {@link #getState}, {@link #setState} and/or
 * {@link #compareAndSetState}:
 * <p>
 * <ul>
 * <li>{@link #tryAcquire}
 * <li>{@link #tryRelease}
 * <li>{@link #tryAcquireShared}
 * <li>{@link #tryReleaseShared}
 * <li>{@link #isHeldExclusively}
 * </ul>
 * <p>
 * Each of these methods by default throws {@link UnsupportedOperationException}
 * . Implementations of these methods must be internally thread-safe, and should
 * in general be short and not block. Defining these methods is the
 * <em>only</em> supported means of using this class. All other methods are
 * declared {@code final} because they cannot be independently varied.
 * <p>
 * 当前类只支持定义这几个方法，但是必须是线程安全的，通常比较短并且不会阻塞，因为其他的方法都被设置成了final。
 * <p>
 * You may also find the inherited methods from
 * {@link AbstractOwnableSynchronizer} useful to keep track of the thread owning
 * an exclusive synchronizer. You are encouraged to use them -- this enables
 * monitoring and diagnostic tools to assist users in determining which threads
 * hold locks.
 * 你可能会发现当前类类继承的方法来保证独占的同步，非常有用。
 * 鼓励你使用它们，这些也包含了监控的工作，来来分析决定具体哪一个线程包含了锁。
 * <p>
 * <p>
 * Even though this class is based on an internal FIFO queue, it does not
 * automatically enforce FIFO acquisition policies. The core of exclusive
 * synchronization takes the form:
 * <p>
 * 尽管这个类基于内部的FIFO队列，但是他不会自动执行先进先出的策略，下面这种形式就是
 * 独占策略的主要方式。
 * <pre>
 * Acquire:
 *     while (!tryAcquire(arg)) {
 *        <em>enqueue thread if it is not already queued</em>;
 *        <em>possibly block current thread</em>;
 *     }
 *
 * Release:
 *     if (tryRelease(arg))
 *        <em>unblock the first queued thread</em>;
 * </pre>
 * <p>
 * (Shared mode is similar but may involve cascading signals.)
 * 共享模式可能是差不多的，但是可能会涉及到级联信号。
 * <p>
 * <p id="barging">
 * Because checks in acquire are invoked before enqueuing, a newly acquiring
 * thread may <em>barge</em> ahead of others that are blocked and queued.
 * 在进队列前，要判断请求调用，一个新请求的线程可能会在在其他线程前被阻塞和进队列。
 * However, you can, if desired, define {@code tryAcquire} and/or
 * {@code tryAcquireShared} to disable barging by internally invoking one or
 * more of the inspection methods, thereby providing a <em>fair</em> FIFO
 * acquisition order.
 * 但是，如果你需要，或者希望，定义一个tryAcquire或者tryAcquireShared来
 * In particular, most fair synchronizers can define
 * {@code tryAcquire} to return {@code false} if {@link #hasQueuedPredecessors}
 * (a method specifically designed to be used by fair synchronizers) returns
 * {@code true}. Other variations are possible.
 *
 * 特别的是，很多公平的同步在可以定义tryAcquire来返回false,如果hasQueuedPredecessors(
 * 一个针对公平同步这特别设计的方法)返回true.其他的变化也可能出现。
 * <p>
 * <p>
 * Throughput and scalability are generally highest for the default barging
 * (also known as <em>greedy</em>, <em>renouncement</em>, and
 * <em>convoy-avoidance</em>) strategy.
 *
 * While this is not guaranteed to be fair
 * or starvation-free, earlier queued threads are allowed to recontend before
 * later queued threads, and each recontention has an unbiased chance to succeed
 * against incoming threads.
 * 当没有公平或者无饥饿，早入队列的线程将被允许和后进队列的线程竞争，而每一次竞争都有一个公平
 * 的机会赢过刚进来的线程。
 *
 * Also, while acquires do not &quot;spin&quot; in the
 * usual sense, they may perform multiple invocations of {@code tryAcquire}
 * interspersed with other computations before blocking. This gives most of the
 * benefits of spins when exclusive synchronization is only briefly held,
 * without most of the liabilities when it isn't. If so desired, you can augment
 * this by preceding calls to acquire methods with "fast-path" checks, possibly
 * prechecking {@link #hasContended} and/or {@link #hasQueuedThreads} to only do
 * so if the synchronizer is likely not to be contended.
 * 同样的，当请求不需要自旋。按平常的来看，它们可能在阻塞前多次调用tryAcquire和其他的计算。
 *
 * <p>
 * <p>
 * This class provides an efficient and scalable basis for synchronization in
 * part by specializing its range of use to synchronizers that can rely on
 * {@code int} state, acquire, and release parameters, and an internal FIFO wait
 * queue. When this does not suffice, you can build synchronizers from a lower
 * level using {@link java.util.concurrent.atomic atomic} classes, your own
 * custom {@link java.util.Queue} classes, and {@link LockSupport} blocking
 * support.
 * 这个类给同步提供了一个有用的可以扩展的基础，依赖于int状态，请求和释放参数，以及内部的FIFO等待队列，
 * 来指定同步者的范围，如果这还不够的话，你可以通过更底层的atomic来，有自己定制的Queue类，以及LockSupport
 * 阻塞支持来构造你自己的同步者。
 * <p>
 * <h3>Usage Examples</h3>
 * <p>
 * <p>
 * Here is a non-reentrant mutual exclusion lock class that uses the value zero
 * to represent the unlocked state, and one to represent the locked state.
 * 这里一个非重入的独占锁的类，通过使用0来代表非锁状态，1来代表锁状态。
 * While
 * a non-reentrant lock does not strictly require recording of the current owner
 * thread, this class does so anyway to make usage easier to monitor.
 * 这个非重入锁并没有严格要求记录当前线程，但是这个类有很多方法让监控更贱容易。
 * It also
 * supports conditions and exposes one of the instrumentation methods:
 * 这还支持暴露其中的工具方法。
 * <p>
 * <pre>
 * {
 * 	&#64;code
 * 	class Mutex implements Lock, java.io.Serializable {
 *
 * 		// Our internal helper class
 * 		private static class Sync extends AbstractQueuedSynchronizer {
 * 			// Reports whether in locked state 判断是否在阻塞状态
 * 			protected boolean isHeldExclusively() {
 * 				return getState() == 1;
 *            }
 *
 * 			// Acquires the lock if state is zero 如果状态是0，请求锁
 * 			public boolean tryAcquire(int acquires) {
 * 				assert acquires == 1; // Otherwise unused
 * 				if (compareAndSetState(0, 1)) {
 * 					setExclusiveOwnerThread(Thread.currentThread());
 * 					return true;
 *                }
 * 				return false;
 *            }
 *
 * 			// Releases the lock by setting state to zero 通过设置state为0,来释放锁
 * 			protected boolean tryRelease(int releases) {
 * 				assert releases == 1; // Otherwise unused
 * 				if (getState() == 0)
 * 					throw new IllegalMonitorStateException();
 * 				setExclusiveOwnerThread(null);
 * 				setState(0);
 * 				return true;
 *            }
 *
 * 			// Provides a Condition
 * 			Condition newCondition() {
 * 				return new ConditionObject();
 *            }
 *
 * 			// Deserializes properly 反序列化参数
 * 			private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
 * 				s.defaultReadObject();
 * 				setState(0); // reset to unlocked state
 *            }
 *        }
 *
 * 		// The sync object does all the hard work. We just forward to it.
 * 		private final Sync sync = new Sync();
 *
 * 		public void lock() {
 * 			sync.acquire(1);
 *        }
 *
 * 		public boolean tryLock() {
 * 			return sync.tryAcquire(1);
 *        }
 *
 * 		public void unlock() {
 * 			sync.release(1);
 *        }
 *
 * 		public Condition newCondition() {
 * 			return sync.newCondition();
 *        }
 *
 * 		public boolean isLocked() {
 * 			return sync.isHeldExclusively();
 *        }
 *
 * 		public boolean hasQueuedThreads() {
 * 			return sync.hasQueuedThreads();
 *        }
 *
 * 		public void lockInterruptibly() throws InterruptedException {
 * 			sync.acquireInterruptibly(1);
 *        }
 *
 * 		public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
 * 			return sync.tryAcquireNanos(1, unit.toNanos(timeout));
 *        }
 *    }
 * }
 * </pre>
 * <p>
 * <p>
 * Here is a latch class that is like a
 * {@link java.util.concurrent.CountDownLatch CountDownLatch} except that it
 * only requires a single {@code signal} to fire.
 * 这里有一个类似于CountDownLatch的门闩类，除了它只需要一个信号来进行触发。
 * Because a latch is
 * non-exclusive, it uses the {@code shared} acquire and release methods.
 * 因为一个门闩是非独占的，它使用共享模式来请求和释放锁。
 * <p>
 * <pre>
 * {
 * 	&#64;code
 * 	class BooleanLatch {
 *
 * 		private static class Sync extends AbstractQueuedSynchronizer {
 *
 *      //有没有被通知
 * 	    boolean isSignalled() {
 * 				return getState() != 0;
 *            }
 *
 * 			protected int tryAcquireShared(int ignore) {
 * 				return isSignalled() ? 1 : -1;
 *            }
 *
 * 			protected boolean tryReleaseShared(int ignore) {
 * 				setState(1);
 * 				return true;
 *            }
 *        }
 *
 * 		private final Sync sync = new Sync();
 *
 * 		public boolean isSignalled() {
 * 			return sync.isSignalled();
 *        }
 *
 * 		public void signal() {
 * 			sync.releaseShared(1);
 *        }
 *
 * 		public void await() throws InterruptedException {
 * 			sync.acquireSharedInterruptibly(1);
 *        }
 *    }
 * }
 * </pre>
 *
 * @author Doug Lea
 * @since 1.5
 */
public abstract class AbstractQueuedSynchronizer extends AbstractOwnableSynchronizer implements
    java.io.Serializable {

  /**
   * The number of nanoseconds for which it is faster to spin rather than to
   * use timed park. A rough estimate suffices to improve responsiveness with
   * very short timeouts.
   * 纳秒的值，使用更快的自旋来代替超时park，在短的超时范围内用于提高响应速度的。
   */
  static final long spinForTimeoutThreshold = 1000L;
  private static final long serialVersionUID = 7373984972572414691L;
  /**
   * Setup to support compareAndSet. We need to natively implement this here:
   * For the sake of permitting future enhancements, we cannot explicitly
   * subclass AtomicInteger, which would be efficient and useful otherwise.
   * So, as the lesser of evils, we natively implement using hotspot
   * intrinsics API. And while we are at it, we do the same for other CASable
   * fields (which could otherwise be done with atomic field updaters).
   */
  private static final Unsafe unsafe = Unsafe.getUnsafe();
  private static final long stateOffset;
  private static final long headOffset;
  private static final long tailOffset;
  private static final long waitStatusOffset;
  private static final long nextOffset;

  static {
    try {
      stateOffset = unsafe
          .objectFieldOffset(AbstractQueuedSynchronizer.class.getDeclaredField("state"));
      headOffset = unsafe
          .objectFieldOffset(AbstractQueuedSynchronizer.class.getDeclaredField("head"));
      tailOffset = unsafe
          .objectFieldOffset(AbstractQueuedSynchronizer.class.getDeclaredField("tail"));
      waitStatusOffset = unsafe.objectFieldOffset(Node.class.getDeclaredField("waitStatus"));
      nextOffset = unsafe.objectFieldOffset(Node.class.getDeclaredField("next"));

    } catch (Exception ex) {
      throw new Error(ex);
    }
  }

  // Queuing utilities

  /**
   * Head of the wait queue, lazily initialized. Except for initialization, it
   * is modified only via method setHead. Note: If head exists, its waitStatus
   * is guaranteed not to be CANCELLED.
   * 等待队列头结点，懒初始化，除了初始化，它只能通过setHead方法进行修改。注意:如果一个头结点存在，
   * 它的等待状态不能被设置为CANCELLED、
   */
  private transient volatile Node head;
  /**
   * Tail of the wait queue, lazily initialized. Modified only via method enq
   * to add new wait node.
   * 尾节点，懒初始化，只能通过enq方法来添加一个新的尾节点。
   */
  private transient volatile Node tail;
  /**
   * The synchronization state.
   * 同步状态
   */
  private volatile int state;

  /**
   * Creates a new {@code AbstractQueuedSynchronizer} instance with initial
   * synchronization state of zero.
   * 创建一个新的AbstractQueuedSynchronizer实例，此时内部同步变量的状态为0.
   */
  protected AbstractQueuedSynchronizer() {
  }

  /**
   * 这个方法的目的：若上家非头节点，通过shouldParkAfterFailedAcquire()确保上家的节点状态被置为SIGNAL
   * （其中还包含去掉已取消的节点的过程）
   * 此函数return true表示允许当前节点（线程）休眠。return false表示需要重入无限for循环检查
   * （可能在修改状态的过程中当前节点已经来到了队首，那么在休眠之前再tryAcquire()一次，可提高性能）。
   * 该方法返回false就是为了再进行以此判断，要不要再来次tryAcquire
   *
   * 节点acquire失败后，判断是否需要阻塞。如果线程需要被阻塞返回true。
   * 这里是所有的acquire循环主要信号控制的地方，需要的条件是pred==node.prev，返回true表示应该被park
   * Checks and updates status for a node that failed to acquire. Returns true
   * if thread should block. This is the main signal control in all acquire
   * loops. Requires that pred == node.prev.
   *
   * @param pred node's predecessor holding status 当前节点的上一个节点
   * @param node the node
   * @return {@code true} if thread should block
   */
  private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) { // 请求失败应该被阻塞
    int ws = pred.waitStatus;
    if (ws == Node.SIGNAL)
      /*
       * This node has already set status asking a release to signal it,
       * so it can safely park.
       */ {
      return true;
    }
    if (ws > 0) { // 前继节点的状态大于0（大于0就CANCELLED这一个状态）
      /*
       * Predecessor was cancelled. Skip over predecessors and indicate
       * retry.
       */
      do {
        //循环去掉被取消的节点
        node.prev = pred = pred.prev;
      } while (pred.waitStatus > 0);
      pred.next = node;
    } else {
      /*
       * waitStatus must be 0 or PROPAGATE. Indicate that we need a
       * signal, but don't park yet. Caller will need to retry to make
       * sure it cannot acquire before parking.
       */
      //如果前驱正常，那就把前驱的状态设置成SIGNAL，告诉它拿完号后通知自己一下。有可能失败，人家说不定刚刚释放完呢！
      compareAndSetWaitStatus(pred, ws, Node.SIGNAL); // 将前继节点的waitStatus设为SIGNAL，为了当前节点以后能够唤醒
    }
    return false;
  }

  /**
   * Convenience method to interrupt current thread.
   */
  static void selfInterrupt() { // 中断当前线程
    Thread.currentThread().interrupt();
  }

  /**
   * CAS waitStatus field of a node.
   */
  private static final boolean compareAndSetWaitStatus(Node node, int expect, int update) {
    return unsafe.compareAndSwapInt(node, waitStatusOffset, expect, update);
  }

  // Utilities for various versions of acquire

  /**
   * CAS next field of a node.
   */
  private static final boolean compareAndSetNext(Node node, Node expect, Node update) {
    return unsafe.compareAndSwapObject(node, nextOffset, expect, update);
  }

  /**
   * Returns the current value of synchronization state. This operation has
   * memory semantics of a {@code volatile} read.
   *
   * @return current state value
   */
  protected final int getState() {
    return state;
  }

  /**
   * Sets the value of synchronization state. This operation has memory
   * semantics of a {@code volatile} write.
   *
   * @param newState the new state value
   */
  protected final void setState(int newState) {
    state = newState;
  }

  /**
   * Atomically sets synchronization state to the given updated value if the
   * current state value equals the expected value. This operation has memory
   * semantics of a {@code volatile} read and write.
   *
   * @param expect the expected value
   * @param update the new value
   * @return {@code true} if successful. False return indicates that the
   * actual value was not equal to the expected value.
   */
  protected final boolean compareAndSetState(int expect, int update) {
    // See below for intrinsics setup to support this
    return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
  }

  /*
   * Various flavors of acquire, varying in exclusive/shared and control
   * modes. Each is mostly the same, but annoyingly different. Only a little
   * bit of factoring is possible due to interactions of exception mechanics
   * (including ensuring that we cancel if tryAcquire throws exception) and
   * other control, at least not without hurting performance too much.
   */

  /**
   * 节点进入队列操作，返回node节点的前一个节点
   * Inserts node into queue, initializing if necessary. See picture above.
   *
   * @param node the node to insert
   * @return node's predecessor
   */
  private Node enq(final Node node) { // 如果尾节点为null，那么说明该队列为空，那就设置头结点
    for (; ; ) {
      Node t = tail;
      if (t == null) { // Must initialize
        if (compareAndSetHead(new Node())) {
          tail = head;
        }
      } else {
        node.prev = t; // 加入队列后，那么返回
        if (compareAndSetTail(t,
            node)) {    // private final boolean compareAndSetTail(Node expect, Node update) {
          t.next = node;                   //          return unsafe.compareAndSwapObject(this,tailOffset, expect, update);
          return t;                        // }
        }
      }
    }
  }

  /**
   * 将节点以mode模式加入到等待队列
   * Creates and enqueues node for current thread and given mode.
   *
   * @param mode Node.EXCLUSIVE for exclusive, Node.SHARED for shared
   * @return the new node
   */
  private Node addWaiter(Node mode) { // 将节点加入waiter（condition条件队列）
    Node node = new Node(Thread.currentThread(), mode);
    // Try the fast path of enq; backup to full enq on failure
    Node pred = tail;
    if (pred != null) {
      node.prev = pred;
      if (compareAndSetTail(pred, node)) {
        pred.next = node;
        return node;
      }
    }
    enq(node);
    return node;
  }

  /**
   * Sets head of queue to be node, thus dequeuing. Called only by
   * 设置节点为头结点，因此对就出队列了，只会被acquire使用 acquire methods. Also nulls out unused
   * fields for sake of GC and to suppress unnecessary signals and traversals.
   *
   * @param node the node
   */
  private void setHead(Node node) { // head其实是一个伪节点，自己没有关联线程，也没有前继节点
    head = node;
    node.thread = null;
    node.prev = null;
  }

  /**
   * Wakes up node's successor, if one exists. 如果存在后继节点，那么便唤醒后继节点
   *
   * @param node the node
   */
  private void unparkSuccessor(Node node) { // 该方法是用于唤醒后继节点

    /*
     * If status is negative (i.e., possibly needing signal) try to clear in
     * anticipation of signalling. It is OK if this fails or if status is
     * changed by waiting thread.
     */
    int ws = node.waitStatus; // 独占锁来说，这里的waitStauts是-1（SIGNAL） node是头结点
    if (ws < 0) {
      compareAndSetWaitStatus(node, ws, 0);
    }

    /*
     * Thread to unpark is held in successor, which is normally just the
     * next node. But if cancelled or apparently null, traverse backwards
     * from tail to find the actual non-cancelled successor.
     */
    Node s = node.next; // s等于头结点的下一个节点
    if (s == null || s.waitStatus > 0) { // CANCALED
      s = null;
      for (Node t = tail; t != null && t != node; t = t.prev) // ？为什么要从末尾判断呢？
      {
        if (t.waitStatus <= 0) {
          s = t;
        }
      }
    }
    if (s != null) {
      LockSupport.unpark(s.thread);
    }
  }

  /**
   * shared模式下的Release操作，唤醒后继节点并且保证信号传递。
   * Release action for shared mode -- signals successor and ensures
   * propagation. (Note: For exclusive mode,release just amounts to
   * calling unparkSuccessor of head if it needs signal.
   * 对于独占模式，释放只需要head节点调用unparkSuccessor如果需要被唤醒
   * )
   */
  private void doReleaseShared() {
    /*
     * Ensure that a release propagates, even if there are other in-progress
     * acquires/releases. This proceeds in the usual way of trying to
     * unparkSuccessor of head if it needs signal. But if it does not,
     * status is set to PROPAGATE to ensure that upon release, propagation
     * continues. Additionally, we must loop in case a new node is added
     * while we are doing this. Also, unlike other uses of unparkSuccessor,
     * we need to know if CAS to reset status fails, if so rechecking.
     */
    for (; ; ) {
      Node h = head;
      if (h != null && h != tail) {
        int ws = h.waitStatus;
        if (ws == Node.SIGNAL) { // 如果waitStatus值为SIGNAL
          if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0)) // 如果将waitStatus信号设置为0失败，那么继续自旋
          {
            continue; // loop to recheck cases
          }
          //设置成功，唤醒下一个节点
          unparkSuccessor(h);
        } else if (ws == 0 && !compareAndSetWaitStatus(h, 0,
            Node.PROPAGATE)) // 如果waitStatus为0切将信号设置为PROPAGATE失败，还是自旋
        {
          continue; // loop on failed CAS
        }
      }
      if (h == head) // loop if head changed 如果h变成头结点，那么停止
      {
        break;
      }
    }
  }

  /**
   * 设置queue的头结点，在共享模式中判断后继节点是否需要等待，要么propagate>0
   * 要么节点的状态被设置为PROPAGATE，表示是传播状态.
   * Sets head of queue, and checks if successor may be waiting in shared
   * mode, if so propagating if either propagate > 0 or PROPAGATE status was
   * set.
   *
   * @param node the node
   * @param propagate the return value from a tryAcquireShared
   */
  private void setHeadAndPropagate(Node node, int propagate) {
    //记录以前的头结点
    Node h = head; // Record old head for check below
    //设置头结点为当前节点
    setHead(node);
    /*
     * Try to signal next queued node if: Propagation was indicated by
     * caller, or was recorded (as h.waitStatus either before or after
     * setHead) by a previous operation (note: this uses sign-check of
     * waitStatus because PROPAGATE status may transition to SIGNAL.) and
     * The next node is waiting in shared mode, or we don't know, because it
     * appears null
     *
     * The conservatism in both of these checks may cause unnecessary
     * wake-ups, but only when there are multiple racing acquires/releases,
     * so most need signals now or soon anyway.
     */
    if (propagate > 0 || h == null || h.waitStatus < 0 || (h = head) == null || h.waitStatus < 0) {
      Node s = node.next;
      if (s == null || s.isShared()) {
        doReleaseShared();
      }
    }
  }

  // Main exported methods

  /**
   * 取消一个正在尝试的acquire（会将节点状态设为CANCELLED）
   * Cancels an ongoing attempt to acquire.
   *
   * @param node the node
   */
  private void cancelAcquire(Node node) {
    // Ignore if node doesn't exist
    if (node == null) {
      return;
    }

    node.thread = null;

    // Skip cancelled predecessors
    Node pred = node.prev;
    while (pred.waitStatus > 0) {
      node.prev = pred = pred.prev;
    }

    // predNext is the apparent node to unsplice. CASes below will
    // fail if not, in which case, we lost race vs another cancel
    // or signal, so no further action is necessary.
    Node predNext = pred.next;

    // Can use unconditional write instead of CAS here.
    // After this atomic step, other Nodes can skip past us.
    // Before, we are free of interference from other threads.
    node.waitStatus = Node.CANCELLED;

    // If we are the tail, remove ourselves.
    if (node == tail && compareAndSetTail(node, pred)) {
      compareAndSetNext(pred, predNext, null);
    } else {
      // If successor needs signal, try to set pred's next-link
      // so it will get one. Otherwise wake it up to propagate.
      int ws;
      if (pred != head && ((ws = pred.waitStatus) == Node.SIGNAL
          || (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) && pred.thread != null) {
        Node next = node.next;
        if (next != null && next.waitStatus <= 0) {
          compareAndSetNext(pred, predNext, next);
        }
      } else {
        unparkSuccessor(node);
      }

      node.next = node; // help GC
    }
  }

  /**
   * 阻塞当前线程 如果此过程中，线程已被中断，那么返回true，清除标志位 如果线程唤醒，没有被中断，返回false
   * Convenience method to park and then check if interrupted
   *
   * @return {@code true} if interrupted
   */
  private final boolean parkAndCheckInterrupt() {
    LockSupport.park(this); // 阻塞当前线程，线程在这里被阻塞
    /**
     * public static boolean interrupted()
     * 测试当前线程是否已经中断。线程的中断状态
     * 由该方法清除。换句话说，如果连续两次调用该方法，则第二次调用将返回
     * false（在第一次调用已清除了其中断状态之后，且第二次调用检验完中断状态前，当前线程再次中断的情况除外）。
     * 线程中断被忽略，因为在中断时不处于活动状态的线程将由此返回 false 的方法反映出来。
     *
     */
    return Thread.interrupted(); // 如果中断则返回true，并且清除中断状态
  }

  /**
   * 等待队列上获取锁，失败（阻塞），成功（继续执行） 获取锁过程中被中断，返回true，没被中断，返回false
   * Acquires in exclusive uninterruptible mode for thread already in queue. Used by
   * condition wait methods as well as acquire.
   *
   * @param node the node
   * @param arg the acquire argument
   * @return {@code true} if interrupted while waiting
   */
  final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;
    try {
      boolean interrupted = false;
      for (; ; ) {
        // 拿到当前节点的前继节点
        final Node p = node.predecessor();
        //如果前驱是head，即该结点已成老二，那么便有资格去尝试获取资源（可能是老大释放完资源唤醒自己的，当然也可能被interrupt了）。
        if (p == head && tryAcquire(arg)) {
          //设置头结点为当前节点
          setHead(node);
          //当前节点的next字段设置为null，帮助GC，当前节点请求成功，说明前继节点已经被释放
          p.next = null; // help GC
          failed = false;
          return interrupted;
        }
        //如果自己可以休息了，就进入waiting状态，直到被unpark()
        //(*)节点就是在这里被阻塞的，也是在这里去掉被Canceled的节点
        //先判断是否需要park，然后再park，unpark后，如果线程被中断，则进入if语句，设置interrupted为true
        //唤醒过后，继续for循环，再次使用tryAcquire获取，直到获取成功或者抛出异常，才退出循环。
        if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt()) {
          interrupted = true; // 如果当前线程被中断，中断标志会被清除，interrupted设为true，而interrupted是局部变量
        }
      }
    } finally {
      //fialed到这里，抛出异常，请求失败，那么取消请求
      if (failed) {
        cancelAcquire(node);
      }
    }
  }

  /**
   * 独占且能够响应中断的模式，并将该节点状态设为
   * CANCELLED Acquires in exclusive interruptible mode.
   *
   * @param arg the acquire argument
   */
  private void doAcquireInterruptibly(int arg) throws InterruptedException {
    final Node node = addWaiter(Node.EXCLUSIVE); // 添加到队列
    boolean failed = true;
    try {
      for (; ; ) {
        final Node p = node.predecessor();
        if (p == head && tryAcquire(arg)) {
          setHead(node);
          p.next = null; // help GC
          failed = false;
          return;
        }
        if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt()) {
          throw new InterruptedException(); // 抛出异常
        }
      }
    } finally {
      if (failed) {
        cancelAcquire(node);
      }
    }
  }

  /**
   * 会响应中断，并将节点设为CANCELLED Acquires in exclusive timed mode.
   *
   * @param arg the acquire argument
   * @param nanosTimeout max wait time
   * @return {@code true} if acquired
   */
  private boolean doAcquireNanos(int arg, long nanosTimeout) throws InterruptedException {
    if (nanosTimeout <= 0L) {
      return false;
    }
    final long deadline = System.nanoTime() + nanosTimeout;
    final Node node = addWaiter(Node.EXCLUSIVE);
    boolean failed = true;
    try {
      for (; ; ) {
        final Node p = node.predecessor();
        if (p == head && tryAcquire(arg)) {
          setHead(node);
          p.next = null; // help GC
          failed = false;
          return true;
        }
        nanosTimeout = deadline - System.nanoTime();
        if (nanosTimeout <= 0L) // 超时，失败
        {
          return false;
        }
        if (shouldParkAfterFailedAcquire(p, node) && nanosTimeout > spinForTimeoutThreshold) {
          LockSupport.parkNanos(this, nanosTimeout);
        }
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }
      }
    } finally {
      if (failed) {
        cancelAcquire(node);
      }
    }
  }

  /**
   * 将节点加入到等待队列，尝试获取锁，如果获取是失败，则阻塞 但是不会响应中断，程序会继续执行
   * Acquires in shared uninterruptible mode.
   *
   * @param arg the acquire argument
   */
  private void doAcquireShared(int arg) {
    // 将节点以SHARED模式加入到等待队列里
    final Node node = addWaiter(Node.SHARED);
    boolean failed = true;
    try {
      boolean interrupted = false;
      //自旋
      for (; ; ) {
        //前继节点
        final Node p = node.predecessor();
        //前继节点是头结点
        if (p == head) {
          //tryAcquireShared由子类实现
          //返回负数，表示请求资源失败
          //返回0或者正式表示请求成功
          int r = tryAcquireShared(arg); // 再次尝试获取锁
          if (r >= 0) {
            setHeadAndPropagate(node, r);
            p.next = null; // help GC
            if (interrupted) {
              selfInterrupt();
            }
            failed = false;
            return;
          }
        }
        //获取失败判断是否需要阻塞
        if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt()) // 获取失败则阻塞
        {
          interrupted = true;
        }
      }
    } finally {
      if (failed) {
        cancelAcquire(node);
      }
    }
  }

  /**
   * 再次尝试获取锁，如果失败，那么阻塞当前线程 会响应中断，并将节点状态设为CANCELLED
   * Acquires in shared interruptible mode.
   *
   * @param arg the acquire argument
   */
  private void doAcquireSharedInterruptibly(int arg) throws InterruptedException {
    final Node node = addWaiter(Node.SHARED); // 共享模式
    boolean failed = true;
    try {
      for (; ; ) {
        final Node p = node.predecessor();
        if (p == head) {
          int r = tryAcquireShared(arg);
          if (r >= 0) {
            setHeadAndPropagate(node, r);
            p.next = null; // help GC
            failed = false;
            return;
          }
        }
        if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt()) {
          throw new InterruptedException();
        }
      }
    } finally {
      if (failed) {
        cancelAcquire(node);
      }
    }
  }

  /**
   * Acquires in shared timed mode.
   *
   * @param arg the acquire argument
   * @param nanosTimeout max wait time
   * @return {@code true} if acquired
   */
  private boolean doAcquireSharedNanos(int arg, long nanosTimeout) throws InterruptedException {
    if (nanosTimeout <= 0L) {
      return false;
    }
    final long deadline = System.nanoTime() + nanosTimeout;
    final Node node = addWaiter(Node.SHARED);
    boolean failed = true;
    try {
      for (; ; ) {
        final Node p = node.predecessor();
        if (p == head) {
          int r = tryAcquireShared(arg);
          if (r >= 0) { // 获取成功
            setHeadAndPropagate(node, r);
            p.next = null; // help GC
            failed = false;
            return true;
          }
        }
        nanosTimeout = deadline - System.nanoTime();
        if (nanosTimeout <= 0L) {
          return false;
        }
        if (shouldParkAfterFailedAcquire(p, node) && nanosTimeout > spinForTimeoutThreshold) {
          LockSupport.parkNanos(this, nanosTimeout);
        }
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }
      }
    } finally {
      if (failed) {
        cancelAcquire(node);
      }
    }
  }

  /**
   * Attempts to acquire in exclusive mode. This method should query if the
   * state of the object permits it to be acquired in the exclusive mode, and
   * if so to acquire it.
   * <p>
   * <p>
   * This method is always invoked by the thread performing acquire. If this
   * method reports failure, the acquire method may queue the thread, if it is
   * not already queued, until it is signalled by a release from some other
   * thread. This can be used to implement method {@link Lock#tryLock()}.
   * <p>
   * <p>
   * The default implementation throws {@link UnsupportedOperationException}.
   *
   * @param arg the acquire argument. This value is always the one passed to
   * an acquire method, or is the value saved on entry to a
   * condition wait. The value is otherwise uninterpreted and can
   * represent anything you like.
   * @return {@code true} if successful. Upon success, this object has been
   * acquired.
   * @throws IllegalMonitorStateException if acquiring would place this synchronizer in an illegal
   * state. This exception must be thrown in a consistent fashion
   * for synchronization to work correctly.
   * @throws UnsupportedOperationException if exclusive mode is not supported
   */
  protected boolean tryAcquire(int arg) {
    throw new UnsupportedOperationException();
  }

  /**
   * Attempts to set the state to reflect a release in exclusive mode.
   * <p>
   * <p>
   * This method is always invoked by the thread performing release.
   * <p>
   * <p>
   * The default implementation throws {@link UnsupportedOperationException}.
   *
   * @param arg the release argument. This value is always the one passed to a
   * release method, or the current state value upon entry to a
   * condition wait. The value is otherwise uninterpreted and can
   * represent anything you like.
   * @return {@code true} if this object is now in a fully released state, so
   * that any waiting threads may attempt to acquire; and
   * {@code false} otherwise.
   * @throws IllegalMonitorStateException if releasing would place this synchronizer in an illegal
   * state. This exception must be thrown in a consistent fashion
   * for synchronization to work correctly.
   * @throws UnsupportedOperationException if exclusive mode is not supported
   */
  protected boolean tryRelease(int arg) {
    throw new UnsupportedOperationException();
  }

  /**
   * Attempts to acquire in shared mode. This method should query if the state
   * of the object permits it to be acquired in the shared mode, and if so to
   * acquire it.
   * <p>
   * <p>
   * This method is always invoked by the thread performing acquire. If this
   * method reports failure, the acquire method may queue the thread, if it is
   * not already queued, until it is signalled by a release from some other
   * thread.
   * <p>
   * <p>
   * The default implementation throws {@link UnsupportedOperationException}.
   *
   * @param arg the acquire argument. This value is always the one passed to
   * an acquire method, or is the value saved on entry to a
   * condition wait. The value is otherwise uninterpreted and can
   * represent anything you like.
   * @return a negative value on failure; zero if acquisition in shared mode
   * succeeded but no subsequent shared-mode acquire can succeed; and
   * a positive value if acquisition in shared mode succeeded and
   * subsequent shared-mode acquires might also succeed, in which case
   * a subsequent waiting thread must check availability. (Support for
   * three different return values enables this method to be used in
   * contexts where acquires only sometimes act exclusively.) Upon
   * success, this object has been acquired.
   * @throws IllegalMonitorStateException if acquiring would place this synchronizer in an illegal
   * state. This exception must be thrown in a consistent fashion
   * for synchronization to work correctly.
   * @throws UnsupportedOperationException if shared mode is not supported
   */
  protected int tryAcquireShared(int arg) {
    throw new UnsupportedOperationException();
  }

  /**
   * Attempts to set the state to reflect a release in shared mode.
   * <p>
   * <p>
   * This method is always invoked by the thread performing release.
   * <p>
   * <p>
   * The default implementation throws {@link UnsupportedOperationException}.
   *
   * @param arg the release argument. This value is always the one passed to a
   * release method, or the current state value upon entry to a
   * condition wait. The value is otherwise uninterpreted and can
   * represent anything you like.
   * @return {@code true} if this release of shared mode may permit a waiting
   * acquire (shared or exclusive) to succeed; and {@code false}
   * otherwise
   * @throws IllegalMonitorStateException if releasing would place this synchronizer in an illegal
   * state. This exception must be thrown in a consistent fashion
   * for synchronization to work correctly.
   * @throws UnsupportedOperationException if shared mode is not supported
   */
  protected boolean tryReleaseShared(int arg) {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns {@code true} if synchronization is held exclusively with respect
   * to the current (calling) thread. This method is invoked upon each call to
   * a non-waiting {@link ConditionObject} method. (Waiting methods instead
   * invoke {@link #release}.)
   * <p>
   * <p>
   * The default implementation throws {@link UnsupportedOperationException}.
   * This method is invoked internally only within {@link ConditionObject}
   * methods, so need not be defined if conditions are not used.
   *
   * @return {@code true} if synchronization is held exclusively;
   * {@code false} otherwise
   * @throws UnsupportedOperationException if conditions are not supported
   */
  protected boolean isHeldExclusively() {
    throw new UnsupportedOperationException();
  }

  // Queue inspection methods

  /**
   * Acquires in exclusive mode, ignoring interrupts. Implemented by invoking
   * at least once {@link #tryAcquire}, returning on success. Otherwise the
   * thread is queued, possibly repeatedly blocking and unblocking, invoking
   * {@link #tryAcquire} until success. This method can be used to implement
   * method {@link Lock#lock}.
   *
   * @param arg the acquire argument. This value is conveyed to
   * {@link #tryAcquire} but is otherwise uninterpreted and can
   * represent anything you like.
   */
  public final void acquire(int arg) {
    if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg)) {
      selfInterrupt();
    }
  }

  /**
   * Acquires in exclusive mode, aborting if interrupted. 独占方式请求，如果被中断，会被终止
   * Implemented by first checking interrupt status, then invoking at least
   * once {@link #tryAcquire}, returning on success. Otherwise the thread is
   * queued, possibly repeatedly blocking and unblocking, invoking
   * {@link #tryAcquire} until success or the thread is interrupted. This
   * method can be used to implement method {@link Lock#lockInterruptibly}.
   *
   * @param arg the acquire argument. This value is conveyed to
   * {@link #tryAcquire} but is otherwise uninterpreted and can
   * represent anything you like.
   * @throws InterruptedException if the current thread is interrupted
   */
  public final void acquireInterruptibly(int arg) throws InterruptedException {
    if (Thread.interrupted()) {
      throw new InterruptedException();
    }
    if (!tryAcquire(arg)) // 没有获取成功
    {
      doAcquireInterruptibly(arg);
    }
  }

  /**
   * Acquires in shared mode, ignoring interrupts. Implemented by 共享模式请求锁，忽略中断
   * first invoking at least once {@link #tryAcquireShared}, returning on
   * success. Otherwise the thread is queued, possibly repeatedly blocking and
   * unblocking, invoking {@link #tryAcquireShared} until success.
   *
   * @param arg the acquire argument. This value is conveyed to
   * {@link #tryAcquireShared} but is otherwise uninterpreted and
   * can represent anything you like.
   */
  public final void acquireShared(int arg) {
    if (tryAcquireShared(arg) < 0)
      //还会再尝试获取资源一次
    {
      doAcquireShared(arg);
    }
  }

  /**
   * Acquires in shared mode, aborting if interrupted. Implemented by first
   * checking interrupt status, then invoking at least once
   * {@link #tryAcquireShared}, returning on success. Otherwise the thread is
   * queued, possibly repeatedly blocking and unblocking, invoking
   * {@link #tryAcquireShared} until success or the thread is interrupted.
   *
   * @param arg the acquire argument. This value is conveyed to
   * {@link #tryAcquireShared} but is otherwise uninterpreted and
   * can represent anything you like.
   * @throws InterruptedException if the current thread is interrupted
   */
  public final void acquireSharedInterruptibly(int arg) throws InterruptedException {
    if (Thread.interrupted()) {
      throw new InterruptedException();
    }
    if (tryAcquireShared(arg) < 0) // 表示等待或者剩下的信号不满足所获取的信号，所以需要挂起
    {
      doAcquireSharedInterruptibly(arg);
    }
  }

  /**
   * Attempts to acquire in exclusive mode, aborting if interrupted, and
   * failing if the given timeout elapses. Implemented by first checking
   * interrupt status, then invoking at least once {@link #tryAcquire},
   * returning on success. Otherwise, the thread is queued, possibly
   * repeatedly blocking and unblocking, invoking {@link #tryAcquire} until
   * success or the thread is interrupted or the timeout elapses. This method
   * can be used to implement method {@link Lock#tryLock(long, TimeUnit)}.
   *
   * @param arg the acquire argument. This value is conveyed to
   * {@link #tryAcquire} but is otherwise uninterpreted and can
   * represent anything you like.
   * @param nanosTimeout the maximum number of nanoseconds to wait
   * @return {@code true} if acquired; {@code false} if timed out
   * @throws InterruptedException if the current thread is interrupted
   */
  public final boolean tryAcquireNanos(int arg, long nanosTimeout) throws InterruptedException {
    if (Thread.interrupted()) {
      throw new InterruptedException();
    }
    return tryAcquire(arg) || doAcquireNanos(arg, nanosTimeout);
  }

  /**
   * Attempts to acquire in shared mode, aborting if interrupted, and failing
   * if the given timeout elapses. Implemented by first checking interrupt
   * status, then invoking at least once {@link #tryAcquireShared}, returning
   * on success. Otherwise, the thread is queued, possibly repeatedly blocking
   * and unblocking, invoking {@link #tryAcquireShared} until success or the
   * thread is interrupted or the timeout elapses.
   *
   * @param arg the acquire argument. This value is conveyed to
   * {@link #tryAcquireShared} but is otherwise uninterpreted and
   * can represent anything you like.
   * @param nanosTimeout the maximum number of nanoseconds to wait
   * @return {@code true} if acquired; {@code false} if timed out
   * @throws InterruptedException if the current thread is interrupted
   */
  public final boolean tryAcquireSharedNanos(int arg, long nanosTimeout)
      throws InterruptedException {
    if (Thread.interrupted()) {
      throw new InterruptedException();
    }
    return tryAcquireShared(arg) >= 0 || doAcquireSharedNanos(arg, nanosTimeout);
  }

  /**
   * 独占模式下释放锁。如果{@link #tryRelease(int)}返回true，便可取消一个或者多个
   * 线程额阻塞。
   * Releases in exclusive mode. Implemented by unblocking one or more
   * threads if {@link #tryRelease} returns true. This method can be used to
   * implement method {@link Lock#unlock}.
   *
   * @param arg the release argument. This value is conveyed to
   * {@link #tryRelease} but is otherwise uninterpreted and can
   * represent anything you like.
   * @return the value returned from {@link #tryRelease}
   */
  public final boolean release(int arg) {
    if (tryRelease(arg)) { // 如果tryRelease返回false，表示getState()不为0,所以不唤醒后继节点
      Node h = head;
      if (h != null && h.waitStatus != 0) {
        unparkSuccessor(h); // 唤醒后继节点
      }
      return true;
    }
    return false;
  }

  // Instrumentation and monitoring methods

  /**
   * Releases in shared mode. Implemented by unblocking one or more threads if
   * {@link #tryReleaseShared} returns true.
   *
   * @param arg the release argument. This value is conveyed to
   * {@link #tryReleaseShared} but is otherwise uninterpreted and
   * can represent anything you like.
   * @return the value returned from {@link #tryReleaseShared}
   */
  public final boolean releaseShared(int arg) {
    if (tryReleaseShared(arg)) {
      doReleaseShared();
      return true;
    }
    return false;
  }

  /**
   * Queries whether any threads are waiting to acquire. Note that because
   * cancellations due to interrupts and timeouts may occur at any time, a
   * {@code true} return does not guarantee that any other thread will ever
   * acquire.
   * <p>
   * <p>
   * In this implementation, this operation returns in constant time.
   *
   * @return {@code true} if there may be other threads waiting to acquire
   */
  public final boolean hasQueuedThreads() {
    return head != tail;
  }

  /**
   * Queries whether any threads have ever contended to acquire this
   * synchronizer; that is if an acquire method has ever blocked.
   * <p>
   * <p>
   * In this implementation, this operation returns in constant time.
   *
   * @return {@code true} if there has ever been contention
   */
  public final boolean hasContended() {
    return head != null;
  }

  /**
   * Returns the first (longest-waiting) thread in the queue, or {@code null}
   * if no threads are currently queued.
   * <p>
   * <p>
   * In this implementation, this operation normally returns in constant time,
   * but may iterate upon contention if other threads are concurrently
   * modifying the queue.
   *
   * @return the first (longest-waiting) thread in the queue, or {@code null}
   * if no threads are currently queued
   */
  public final Thread getFirstQueuedThread() {
    // handle only fast path, else relay
    return (head == tail) ? null : fullGetFirstQueuedThread();
  }

  /**
   * Version of getFirstQueuedThread called when fastpath fails
   */
  private Thread fullGetFirstQueuedThread() {
    /*
     * The first node is normally head.next. Try to get its thread field,
     * ensuring consistent reads: If thread field is nulled out or s.prev is
     * no longer head, then some other thread(s) concurrently performed
     * setHead in between some of our reads. We try this twice before
     * resorting to traversal.
     */
    Node h, s;
    Thread st;
    if (((h = head) != null && (s = h.next) != null && s.prev == head && (st = s.thread) != null)
        || ((h = head) != null && (s = h.next) != null && s.prev == head
        && (st = s.thread) != null)) {
      return st;
    }

    /*
     * Head's next field might not have been set yet, or may have been unset
     * after setHead. So we must check to see if tail is actually first
     * node. If not, we continue on, safely traversing from tail back to
     * head to find first, guaranteeing termination.
     */

    Node t = tail;
    Thread firstThread = null;
    while (t != null && t != head) {
      Thread tt = t.thread;
      if (tt != null) {
        firstThread = tt;
      }
      t = t.prev;
    }
    return firstThread;
  }

  // Internal support methods for Conditions

  /**
   * Returns true if the given thread is currently queued.
   * <p>
   * <p>
   * This implementation traverses the queue to determine presence of the
   * given thread.
   *
   * @param thread the thread
   * @return {@code true} if the given thread is on the queue
   * @throws NullPointerException if the thread is null
   */
  public final boolean isQueued(Thread thread) {
    if (thread == null) {
      throw new NullPointerException();
    }
    for (Node p = tail; p != null; p = p.prev) {
      if (p.thread == thread) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns {@code true} if the apparent first queued thread, if one exists,
   * is waiting in exclusive mode. If this method returns {@code true}, and
   * the current thread is attempting to acquire in shared mode (that is, this
   * method is invoked from {@link #tryAcquireShared}) then it is guaranteed
   * that the current thread is not the first queued thread. Used only as a
   * heuristic in ReentrantReadWriteLock.
   */
  final boolean apparentlyFirstQueuedIsExclusive() {
    Node h, s;
    return (h = head) != null && (s = h.next) != null && !s.isShared() && s.thread != null;
  }

  /**
   * 查询是否有任何线程等待时间比当前线程等待时间更长。 Queries whether any threads have been waiting
   * to acquire longer than the current thread.
   * <p>
   * <p>
   * An invocation of this method is equivalent to (but may be more efficient
   * than): 这种方法相当于如下调用（但也可能效果更好）
   * <p>
   * <pre>
   *  {@code
   * getFirstQueuedThread() != Thread.currentThread() &&
   * hasQueuedThreads()}
   * </pre>
   * <p>
   * <p>
   * Note that because cancellations due to interrupts and timeouts may occur
   * at any time, a {@code true} return does not guarantee that some other
   * thread will acquire before the current thread. Likewise, it is possible
   * for another thread to win a race to enqueue after this method has
   * returned {@code false}, due to the queue being empty.
   * <p>
   * <p>
   * This method is designed to be used by a fair synchronizer to avoid
   * <a href="AbstractQueuedSynchronizer#barging">barging</a>. Such a
   * synchronizer's {@link #tryAcquire} method should return {@code false},
   * and its {@link #tryAcquireShared} method should return a negative value,
   * if this method returns {@code true} (unless this is a reentrant acquire).
   * For example, the {@code
   * tryAcquire} method for a fair, reentrant, exclusive mode synchronizer
   * might look like this:
   * <p>
   * <pre>
   *  {@code
   * protected boolean tryAcquire(int arg) {
   *   if (isHeldExclusively()) {
   *     // A reentrant acquire; increment hold count
   *     return true;
   *   } else if (hasQueuedPredecessors()) {
   *     return false;
   *   } else {
   *     // try to acquire normally
   *   }
   * }}
   * </pre>
   *
   * @return {@code true} if there is a queued thread preceding the current
   * thread, and {@code false} if the current thread is at the head of
   * the queue or the queue is empty
   * @since 1.7
   */
  public final boolean hasQueuedPredecessors() {
    // The correctness of this depends on head being initialized
    // before tail and on head.next being accurate if the current
    // thread is first in queue.
    Node t = tail; // Read fields in reverse initialization order
    Node h = head;
    Node s;
    return h != t && ((s = h.next) == null || s.thread != Thread.currentThread());
  }

  /**
   * Returns an estimate of the number of threads waiting to acquire. The
   * value is only an estimate because the number of threads may change
   * dynamically while this method traverses internal data structures. This
   * method is designed for use in monitoring system state, not for
   * synchronization control.
   *
   * @return the estimated number of threads waiting to acquire
   */
  public final int getQueueLength() {
    int n = 0;
    for (Node p = tail; p != null; p = p.prev) {
      if (p.thread != null) {
        ++n;
      }
    }
    return n;
  }

  /**
   * Returns a collection containing threads that may be waiting to acquire.
   * Because the actual set of threads may change dynamically while
   * constructing this result, the returned collection is only a best-effort
   * estimate. The elements of the returned collection are in no particular
   * order. This method is designed to facilitate construction of subclasses
   * that provide more extensive monitoring facilities.
   *
   * @return the collection of threads
   */
  public final Collection<Thread> getQueuedThreads() {
    ArrayList<Thread> list = new ArrayList<Thread>();
    for (Node p = tail; p != null; p = p.prev) {
      Thread t = p.thread;
      if (t != null) {
        list.add(t);
      }
    }
    return list;
  }

  // Instrumentation methods for conditions

  /**
   * Returns a collection containing threads that may be waiting to acquire in
   * exclusive mode. This has the same properties as {@link #getQueuedThreads}
   * except that it only returns those threads waiting due to an exclusive
   * acquire.
   *
   * @return the collection of threads
   */
  public final Collection<Thread> getExclusiveQueuedThreads() {
    ArrayList<Thread> list = new ArrayList<Thread>();
    for (Node p = tail; p != null; p = p.prev) {
      if (!p.isShared()) {
        Thread t = p.thread;
        if (t != null) {
          list.add(t);
        }
      }
    }
    return list;
  }

  /**
   * Returns a collection containing threads that may be waiting to acquire in
   * shared mode. This has the same properties as {@link #getQueuedThreads}
   * except that it only returns those threads waiting due to a shared
   * acquire.
   *
   * @return the collection of threads
   */
  public final Collection<Thread> getSharedQueuedThreads() {
    ArrayList<Thread> list = new ArrayList<Thread>();
    for (Node p = tail; p != null; p = p.prev) {
      if (p.isShared()) {
        Thread t = p.thread;
        if (t != null) {
          list.add(t);
        }
      }
    }
    return list;
  }

  /**
   * Returns a string identifying this synchronizer, as well as its state. The
   * state, in brackets, includes the String {@code "State ="} followed by the
   * current value of {@link #getState}, and either {@code "nonempty"} or
   * {@code "empty"} depending on whether the queue is empty.
   *
   * @return a string identifying this synchronizer, as well as its state
   */
  public String toString() {
    int s = getState();
    String q = hasQueuedThreads() ? "non" : "";
    return super.toString() + "[State = " + s + ", " + q + "empty queue]";
  }

  /**
   * 判断节点是否在同步队列上面，false为没有，true表示有。
   * （如果节点状态为CONDITION或节点头为null，返回false，如果next不为null，那么节点肯定在等待队列里面）
   * <p>
   * Returns true if a node, always one that was initially placed on a
   * condition queue, is now waiting to reacquire on sync queue.
   *
   * @param node the node
   * @return true if is reacquiring
   */
  final boolean isOnSyncQueue(Node node) {
    if (node.waitStatus == Node.CONDITION || node.prev == null) {
      return false;
    }
    if (node.next != null) // If has successor, it must be on queue
    {
      return true;
    }
    /*
     * node.prev can be non-null, but not yet on queue because the CAS to
     * place it on queue can fail. So we have to traverse from tail to make
     * sure it actually made it. It will always be near the tail in calls to
     * this method, and unless the CAS failed (which is unlikely), it will
     * be there, so we hardly ever traverse much.
     */
    return findNodeFromTail(node);
  }

  /**
   * 同步队列(syc)由末尾向前搜索，如果节点在同步队列上，那么返回true，否则返回false，
   * 这个方法只会在{@link #isOnSyncQueue}需要的时候被引用
   * Returns true if node is on sync queue by searching backwards from tail. Called only when needed by
   * isOnSyncQueue.
   *
   * @return true if present
   */
  private boolean findNodeFromTail(Node node) { // 从等待节点向前搜索，如果节点在等待队列上，那么返回true，否则返回false
    Node t = tail;
    for (; ; ) {
      if (t == node) // 尾节点等于node，返回true
      {
        return true;
      }
      if (t == null) // 尾节点等于null，返回false
      {
        return false;
      }
      t = t.prev;
    }
  }

  /**
   * 将节点从condition队列中sync队列中
   * Transfers a node from a condition queue onto sync queue.
   * Returns true if successful.
   *
   * @param node the node
   * @return true if successfully transferred (else the node was cancelled
   * before signal)
   */
  final boolean transferForSignal(Node node) { // 将条件队列中的节点放入等待队列中
    /*
     * If cannot change waitStatus, the node has been cancelled.
     */
    if (!compareAndSetWaitStatus(node, Node.CONDITION, 0)) // 如果失败，返回false,将状态由CONDITION设置为0
    {
      return false;
    }

    /*
     * Splice onto queue and try to set waitStatus of predecessor to
     * indicate that thread is (probably) waiting. If cancelled or attempt
     * to set waitStatus fails, wake up to resync (in which case the
     * waitStatus can be transiently and harmlessly wrong).
     */
    Node p = enq(node); // 加入到等待队列中，仅仅是加入到等待队列的末尾，且此时的状态为0，enq(node)会返回node在等待队列里的前一个结点
    int ws = p.waitStatus; // 前个节点的状态
    if (ws > 0 || !compareAndSetWaitStatus(p, ws,
        Node.SIGNAL)) // 如果前一个节点状态为CANCELLED或设设置状态为SIGNAL失败，那么唤醒当前节点
    {
      LockSupport.unpark(node.thread); // 唤醒当前节点
    }
    return true;
  }

  /**
   * 1.如果节点状态为CONDITION,且将状态设为0成功，将节点加入等待队列，返回true，否则，转2
   * 2.让出cpu，直到节点加入到等待队列上，最后返回false Transfers node, if necessary, to sync
   * queue after a cancelled wait. Returns true if thread was cancelled before
   * being signalled.
   *
   * @param node the node
   * @return true if cancelled before the node was signalled
   */
  final boolean transferAfterCancelledWait(Node node) {
    if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) { // 将节点状态有CONDITION设为0
      enq(node); // 如果成功，则将该节点加入到等待队列
      return true;
    }
    /*
     * If we lost out to a signal(), then we can't proceed until it finishes
     * its enq(). Cancelling during an incomplete transfer is both rare and
     * transient, so just spin.
     */
    while (!isOnSyncQueue(node)) // 节点不在等待队列上，暂时让出cpu
    {
      Thread.yield();
    }
    return false;
  }

  /**
   * （特殊情况会设置状态为CANCELLED，记住，只是特殊情况）
   * Invokes release with current state value;
   * returns saved state. Cancels node and throws exception on failure.
   *
   * @param node the condition node for this wait
   * @return previous sync state
   */
  final int fullyRelease(Node node) {
    boolean failed = true;
    try {
      int savedState = getState(); // 保存当前持有锁数量
      if (release(savedState)) { // 锁释放成功
        failed = false;
        return savedState; // 返回持有锁数量
      } else {
        throw new IllegalMonitorStateException();
      }
    } finally {
      if (failed) // 失败，则将状态修改为CANCLELED
      {
        node.waitStatus = Node.CANCELLED;
      }
    }
  }

  /**
   * Queries whether the given ConditionObject uses this synchronizer as its
   * lock.
   *
   * @param condition the condition
   * @return {@code true} if owned
   * @throws NullPointerException if the condition is null
   */
  public final boolean owns(ConditionObject condition) {
    return condition.isOwnedBy(this);
  }

  /**
   * Queries whether any threads are waiting on the given condition associated
   * with this synchronizer. Note that because timeouts and interrupts may
   * occur at any time, a {@code true} return does not guarantee that a future
   * {@code signal} will awaken any threads. This method is designed primarily
   * for use in monitoring of the system state.
   *
   * @param condition the condition
   * @return {@code true} if there are any waiting threads
   * @throws IllegalMonitorStateException if exclusive synchronization is not held
   * @throws IllegalArgumentException if the given condition is not associated with this
   * synchronizer
   * @throws NullPointerException if the condition is null
   */
  public final boolean hasWaiters(ConditionObject condition) {
    if (!owns(condition)) {
      throw new IllegalArgumentException("Not owner");
    }
    return condition.hasWaiters();
  }

  /**
   * Returns an estimate of the number of threads waiting on the given
   * condition associated with this synchronizer. Note that because timeouts
   * and interrupts may occur at any time, the estimate serves only as an
   * upper bound on the actual number of waiters. This method is designed for
   * use in monitoring of the system state, not for synchronization control.
   *
   * @param condition the condition
   * @return the estimated number of waiting threads
   * @throws IllegalMonitorStateException if exclusive synchronization is not held
   * @throws IllegalArgumentException if the given condition is not associated with this
   * synchronizer
   * @throws NullPointerException if the condition is null
   */
  public final int getWaitQueueLength(ConditionObject condition) {
    if (!owns(condition)) {
      throw new IllegalArgumentException("Not owner");
    }
    return condition.getWaitQueueLength();
  }

  /**
   * Returns a collection containing those threads that may be waiting on the
   * given condition associated with this synchronizer. Because the actual set
   * of threads may change dynamically while constructing this result, the
   * returned collection is only a best-effort estimate. The elements of the
   * returned collection are in no particular order.
   *
   * @param condition the condition
   * @return the collection of threads
   * @throws IllegalMonitorStateException if exclusive synchronization is not held
   * @throws IllegalArgumentException if the given condition is not associated with this
   * synchronizer
   * @throws NullPointerException if the condition is null
   */
  public final Collection<Thread> getWaitingThreads(ConditionObject condition) {
    if (!owns(condition)) {
      throw new IllegalArgumentException("Not owner");
    }
    return condition.getWaitingThreads();
  }

  /**
   * CAS head field. Used only by enq.
   */
  private final boolean compareAndSetHead(Node update) {
    return unsafe.compareAndSwapObject(this, headOffset, null, update);
  }

  /**
   * CAS tail field. Used only by enq.
   */
  private final boolean compareAndSetTail(Node expect, Node update) {
    return unsafe.compareAndSwapObject(this, tailOffset, expect, update);
  }

  /**
   * Wait queue node class.
   * <p>
   * <p>
   * The wait queue is a variant of a "CLH" (Craig, Landin, and Hagersten)
   * lock queue. CLH locks are normally used for CLH数据结构用于组织锁 spinlocks. We
   * instead use them for blocking synchronizers, but use the same basic
   * tactic of holding some of the control information about a thread in the
   * predecessor of its node. A "status" field in each node keeps track of
   * whether a thread should block.
   * 每个节点的status字段用来保存该节点的状态信息，以判断该节点持有的该线程是否应该阻塞
   * A node is signalled when its predecessor releases.
   * 当一个节点的前继节点被释放后，该节点就应该被唤醒
   * Each node of the queue otherwise serves as a
   * specific-notification-style monitor holding a single waiting thread. The
   * status field does NOT control whether threads are granted locks etc
   * though.
   *
   * A thread may try to acquire if it is first in the queue.
   * 如果线程是队列的头结点（head），那么就应该请求锁
   * But being first does not guarantee success;
   * 但是并不保证头结点的请求一定会成功
   * it only gives the right to contend. So the currently
   * released contender thread may need to rewait.
   * 它只是给与竞争的权利，所以当前释放的竞争者可能需要再次等待。
   * <p>
   * <p>
   * To enqueue into a CLH lock, you atomically splice it in as new tail.
   * To dequeue, you just set the head field.
   * 入队列时，你只能原子性的添加到队列末尾。出队列时，只能设置头结点字段。
   * <p>
   * <pre>
   *      +------+  prev +-----+       +-----+
   * head |      | <---- |     | <---- |     |  tail
   *      +------+       +-----+       +-----+
   * </pre>
   * <p>
   * <p>
   * Insertion into a CLH queue requires only a single atomic operation on
   * "tail", so there is a simple atomic point of demarcation from unqueued to
   * queued. Similarly, dequeuing involves only updating the "head". However,
   * it takes a bit more work for nodes to determine who their successors are,
   * in part to deal with possible cancellation due to timeouts and
   * interrupts.
   * <p>
   * <p>
   * The "prev" links (not used in original CLH locks), are mainly needed to
   * handle cancellation. If a node is cancelled, its successor is (normally)
   * relinked to a non-cancelled predecessor. For explanation of similar
   * mechanics in the case of spin locks, see the papers by Scott and Scherer
   * at http://www.cs.rochester.edu/u/scott/synchronization/
   * <p>
   * <p>
   * We also use "next" links to implement blocking mechanics. The thread id
   * for each node is kept in its own node, so a predecessor signals the next
   * node to wake up by traversing next link to determine which thread it is.
   * Determination of successor must avoid races with newly queued nodes to
   * set the "next" fields of their predecessors. This is solved when
   * necessary by checking backwards from the atomically updated "tail" when a
   * node's successor appears to be null. (Or, said differently, the
   * next-links are an optimization so that we don't usually need a backward
   * scan.)
   * <p>
   * <p>
   * Cancellation introduces some conservatism to the basic algorithms. Since
   * we must poll for cancellation of other nodes, we can miss noticing
   * whether a cancelled node is ahead or behind us. This is dealt with by
   * always unparking successors upon cancellation, allowing them to stabilize
   * on a new predecessor, unless we can identify an uncancelled predecessor
   * who will carry this responsibility.
   * <p>
   * <p>
   * CLH queues need a dummy header node to get started. But we don't create
   * them on construction, because it would be wasted effort if there is never
   * contention. Instead, the node is constructed and head and tail pointers
   * are set upon first contention.
   * <p>
   * <p>
   * Threads waiting on Conditions use the same nodes, but use an additional
   * link. Conditions only need to link nodes in simple (non-concurrent)
   * linked queues because they are only accessed when exclusively held. Upon
   * await, a node is inserted into a condition queue. Upon signal, the node
   * is transferred to the main queue. A special value of status field is used
   * to mark which queue a node is on. 节点的状态表示节点在哪个队列上
   * <p>
   * <p>
   * Thanks go to Dave Dice, Mark Moir, Victor Luchangco, Bill Scherer and
   * Michael Scott, along with members of JSR-166 expert group, for helpful
   * ideas, discussions, and critiques on the design of this class.
   */
  static final class Node {

    /**
     * Marker to indicate a node is waiting in shared mode
     * 作为表示节点处于共享模式的等待状态的标记
     */
    static final Node SHARED = new Node();
    /**
     * Marker to indicate a node is waiting in exclusive mode
     * 节点处于独占状态的标记
     */
    static final Node EXCLUSIVE = null;

    /**
     * waitStatus value to indicate thread has cancelled
     * 等待状态的值用于表示节点已经取消了。
     */
    static final int CANCELLED = 1;
    /**
     * 表示该节点的后继节点的应该被unpark
     * waitStatus value to indicate successor's thread needs unparking
     */
    static final int SIGNAL = -1;
    /**
     * 表示该节点在condition上阻塞
     * waitStatus value to indicate thread is waiting on condition
     */
    static final int CONDITION = -2;
    /**
     * 该状态表示下一个共享请求 应该向下传播
     * waitStatus value to indicate the next acquireShared should unconditionally propagate
     */
    static final int PROPAGATE = -3;

    /**
     * Status field, taking on only the values:
     * SIGNAL:     The successor of this node is (or will soon be)
     * blocked (via park), so the current node must
     * unpark its successor when it releases or
     * cancels. To avoid races, acquire methods must
     * first indicate they need a signal,
     * then retry the atomic acquire, and then,
     * on failure, block.
     * CANCELLED:  This node is cancelled due to timeout or interrupt.
     * Nodes never leave this state. In particular,
     * a thread with cancelled node never again blocks.
     * 节点timeout或者interrupt便会设置为CANCELLED状态.
     * 节点一旦设置为CANCELLED状态后将不会改变，另外，一个
     * 线程如果是CANCELLED状态将不会阻塞，会从队列里面清除。
     * CONDITION:  This node is currently on a condition queue.
     * It will not be used as a sync queue node
     * until transferred, at which time the status
     * will be set to 0. (Use of this value here has
     * nothing to do with the other uses of the
     * field, but simplifies mechanics.)
     * 这个节点当前在一个条件队列里面。直到被传输前，它将不会作为
     * 一个同步队列。
     * PROPAGATE:  A releaseShared should be propagated to other
     * nodes. This is set (for head node only) in
     * doReleaseShared to ensure propagation
     * continues, even if other operations have
     * since intervened.
     * 0:          None of the above
     *
     * The values are arranged numerically to simplify use.
     * Non-negative values mean that a node doesn't need to
     * signal. So, most code doesn't need to check for particular
     * values, just for sign.
     *
     * The field is initialized to 0 for normal sync nodes, and
     * CONDITION for condition nodes.  It is modified using CAS
     * (or when possible, unconditional volatile writes).
     */
    volatile int waitStatus;

    /**
     * Link to predecessor node that current node/thread relies on for
     * checking waitStatus. Assigned during enqueuing, and nulled out (for
     * sake of GC) only upon dequeuing. Also, upon cancellation of a
     * predecessor, we short-circuit while finding a non-cancelled one,
     * which will always exist because the head node is never cancelled: A
     * node becomes head only as a result of successful acquire. A cancelled
     * thread never succeeds in acquiring, and a thread only cancels itself,
     * not any other node.
     */
    volatile Node prev;

    /**
     * Link to the successor node that the current node/thread unparks upon
     * release. Assigned during enqueuing, adjusted when bypassing cancelled
     * predecessors, and nulled out (for sake of GC) when dequeued. The enq
     * operation does not assign next field of a predecessor until after
     * attachment, so seeing a null next field does not necessarily mean
     * that node is at end of queue. However, if a next field appears to be
     * null, we can scan prev's from the tail to double-check. The next
     * field of cancelled nodes is set to point to the node itself instead
     * of null, to make life easier for isOnSyncQueue.
     */
    volatile Node next;

    /**
     * The thread that enqueued this node. Initialized on construction and
     * nulled out after use.
     */
    volatile Thread thread;

    /**
     * Link to next node waiting on condition, or the special   链接等待条件上的下一个节点，或者SHAERD共享模式
     * value SHARED. Because condition queues are               由于条件队列只访问当在独占模式下，我们只需要一个简单的链接队列来保持节点
     * accessed only when holding in                            然后，他们被转移到队列尝试获取因为条件只能对独占，我们用一个特殊的值来表示共享模式
     * exclusive mode, we just need a simple linked queue to hold nodes
     * while they are waiting on conditions. They are then transferred to
     * the queue to re-acquire. And because  conditions can
     * only be exclusive, we save a field by using special
     * value to indicate shared mode.
     */
    Node nextWaiter;

    Node() { // Used to establish initial head or SHARED marker
    }

    /**
     * Used by addWaiter
     */
    Node(Thread thread, Node mode) { // Used by addWaiter
      this.nextWaiter = mode; //
      this.thread = thread;
    }

    /**
     * Used by Condition
     */
    Node(Thread thread, int waitStatus) { // Used by Condition
      this.waitStatus = waitStatus;
      this.thread = thread;
    }

    /**
     * Returns true if node is waiting in shared mode.
     * 如果节点是在共享模式下等待，那么返回true
     */
    final boolean isShared() {
      return nextWaiter == SHARED;
    }

    /**
     * 返回当前节点的前一个节点，如果前一个节点为nll那么抛出NullPointerException异常。
     * 使用时前一个节点不能为空，检查null可以省略，但是目前为了帮助虚拟机。
     * Returns previous node, or throws NullPointerException if null. Use
     * when predecessor cannot be null. The null check could be elided, but
     * is present to help the VM.
     *
     * @return the predecessor of this node
     */
    final Node predecessor() throws NullPointerException {
      Node p = prev;
      if (p == null) {
        throw new NullPointerException();
      } else {
        return p;
      }
    }
  }

  /**
   * Condition implementation for a {@link AbstractQueuedSynchronizer} serving
   * as the basis of a {@link Lock} implementation.
   *
   * Condition实现作在AbstractQueuedSynchronizer中作为Lock一个基础服务。
   * <p>
   * <p>
   * Method documentation for this class describes mechanics, not behavioral
   * specifications from the point of view of Lock and Condition users.
   * Exported versions of this class will in general need to be accompanied by
   * documentation describing condition semantics that rely on those of the
   * associated {@code AbstractQueuedSynchronizer}.
   * <p>
   * <p>
   * This class is Serializable, but all fields are transient, so deserialized
   * conditions have no waiters.
   */
  public class ConditionObject implements Condition, java.io.Serializable {

    private static final long serialVersionUID = 1173984872572414699L;
    /**
     * 改模式表示从wait退出后，再次进行中断
     * Mode meaning to reinterrupt on exit from wait
     */
    private static final int REINTERRUPT = 1;
    /**
     * 该模式表示从等待退出后直接抛出异常
     * Mode meaning to throw InterruptedException on exit from wait
     */
    private static final int THROW_IE = -1;
    /**
     * First node of condition queue.
     */
    private transient Node firstWaiter;

    // Internal methods
    /**
     * Last node of condition queue.
     */
    private transient Node lastWaiter;

    /**
     * Creates a new {@code ConditionObject} instance.
     */
    public ConditionObject() {
    }

    /**
     * 该方法在Condition的await()方法中使用，由于Condition的await()方法
     * 必须在持有锁的情况下调用，所以这里已经是单线程执行情况，所有没有实现线程安全机制 使用了线程封闭策略
     * Adds a new waiter to wait queue.
     *
     * @return its new wait node
     */
    private Node addConditionWaiter() { // 添加一个waiter到等待队列（如果当前队列头结点为空，则将当前节点作为头结点）
      Node t = lastWaiter;
      // If lastWaiter is cancelled, clean out. 如果尾节点被取消掉，则踢出尾节点
      if (t != null && t.waitStatus != Node.CONDITION) {
        unlinkCancelledWaiters();
        t = lastWaiter;
      }
      Node node = new Node(Thread.currentThread(), Node.CONDITION); // 新建一个状态位CONDITION的节点
      if (t == null) // 如果队列为空,当前节点为头结点
      {
        firstWaiter = node;
      } else {
        t.nextWaiter = node; // 将该节点加入到条件队列中（t为尾节点）
      }
      lastWaiter = node; // 尾节点更新为当前节点
      return node; // 返回当前节点
    }

    /**
     * Removes and transfers nodes until hit non-cancelled one or null.
     * Split out from signal in part to encourage compilers to inline the
     * case of no waiters.
     *
     * @param first (non-null) the first node on condition queue
     */
    private void doSignal(Node first) {
      do {
        if ((firstWaiter = first.nextWaiter) == null) // 头结点的下个节点为空，那么lastWaiter设为空
        {
          lastWaiter = null;
        }
        first.nextWaiter = null; // 头结点的下个节点设为空,帮助gc
      } while (!transferForSignal(first) && (first = firstWaiter) != null); // 如果头节点不为空，且加入等待队列失败
    }

    // public methods

    /**
     * Removes and transfers all nodes.
     *
     * @param first (non-null) the first node on condition queue
     */
    private void doSignalAll(Node first) {
      lastWaiter = firstWaiter = null;
      do {
        Node next = first.nextWaiter;
        first.nextWaiter = null;
        transferForSignal(first);
        first = next;
      } while (first != null); // 将所有节点都加入到等待队列中
    }

    /**
     * 用于Condition Unlinks cancelled waiter nodes from condition queue.
     * Called only while holding lock. This is called when cancellation
     * occurred during condition wait, and upon insertion of a new waiter
     * when lastWaiter is seen to have been cancelled. This method is needed
     * to avoid garbage retention in the absence of signals. So even though
     * it may require a full traversal, it comes into play only when
     * timeouts or cancellations occur in the absence of signals. It
     * traverses all nodes rather than stopping at a particular target to
     * unlink all pointers to garbage nodes without requiring many
     * re-traversals during cancellation storms.
     */
    private void unlinkCancelledWaiters() {
      Node t = firstWaiter;
      Node trail = null;
      while (t != null) {
        Node next = t.nextWaiter;
        if (t.waitStatus != Node.CONDITION) { // 下一个结点状态不为CONDITION，说明已被加入到等待队列
          t.nextWaiter = null; // 头结点的下个节点置为null
          if (trail == null) //
          {
            firstWaiter = next; // 头结点设置为当前节点的下个节点，此时当前节点完全从条件队列中移除
          } else {
            trail.nextWaiter = next;
          }
          if (next == null) // 如果当前节点的下个
          {
            lastWaiter = trail;
          }
        } else // 如果当前节点的下个节点的状态为CONDITION ，trail设为当前节点
        {
          trail = t;
        }
        t = next; // t为下一个节点
      }
    }

    /**
     * Moves the longest-waiting thread, if one exists, from the wait queue
     * for this condition to the wait queue for the owning lock.
     *
     * @throws IllegalMonitorStateException if {@link #isHeldExclusively} returns {@code false}
     */
    public final void signal() {
      if (!isHeldExclusively()) // 判断当前唤醒的线程是都是当前线程
      {
        throw new IllegalMonitorStateException();
      }
      Node first = firstWaiter; // 获取头结点
      if (first != null) {
        doSignal(first);
      }
    }

    /*
     * For interruptible waits, we need to track whether to throw
     * InterruptedException, if interrupted while blocked on condition,
     * versus reinterrupt current thread, if interrupted while blocked
     * waiting to re-acquire.
     */

    /**
     * Moves all threads from the wait queue for this condition to the wait
     * queue for the owning lock.
     *
     * @throws IllegalMonitorStateException if {@link #isHeldExclusively} returns {@code false}
     */
    public final void signalAll() {
      if (!isHeldExclusively()) {
        throw new IllegalMonitorStateException();
      }
      Node first = firstWaiter;
      if (first != null) {
        doSignalAll(first);
      }
    }

    /**
     * Implements uninterruptible condition wait.
     * <ol>
     * <li>Save lock state returned by {@link #getState}.
     * <li>Invoke {@link #release} with saved state as argument, throwing
     * IllegalMonitorStateException if it fails.
     * <li>Block until signalled.
     * <li>Reacquire by invoking specialized version of {@link #acquire}
     * with saved state as argument.
     * </ol>
     */
    public final void awaitUninterruptibly() {
      Node node = addConditionWaiter();
      int savedState = fullyRelease(node);
      boolean interrupted = false;
      while (!isOnSyncQueue(node)) {
        LockSupport.park(this);
        if (Thread.interrupted()) {
          interrupted = true;
        }
      }
      if (acquireQueued(node, savedState) || interrupted) {
        selfInterrupt();
      }
    }

    /**
     * （1）如果节点被中断{
     * 1.如果节点状态为CONDITION，返回THROW_IE
     * 2.如果节点状态不为CONDITION，返回REINTERRUPT }
     * （2）没被中断，返回0
     * Checks for interrupt,returning THROW_IE if interrupted before signalled, REINTERRUPT if
     * after signalled, or 0 if not interrupted.
     */
    private int checkInterruptWhileWaiting(
        Node node) { // 如果被中断，返回THROW_IE,否则返回0（如果加入到等待队列成功，返回THROW_IE）
      return Thread.interrupted() ? (transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT) : 0;
    }

    /**
     * 如果是THROW_IE则抛出异常 如果是REINTERRUPT,当前线程设置中断标志位 Throws
     * InterruptedException, reinterrupts current thread, or does nothing,
     * depending on mode.
     */
    private void reportInterruptAfterWait(int interruptMode) throws InterruptedException {
      if (interruptMode == THROW_IE) {
        throw new InterruptedException();
      } else if (interruptMode == REINTERRUPT) // 设置中断标志位
      {
        selfInterrupt();
      }
    }

    /**
     * Implements interruptible condition wait.
     * <ol>
     * <li>If current thread is interrupted, throw InterruptedException.
     * 如果当前节点被中断，抛出异常
     * <li>Save lock state returned by {@link #getState}.
     * <li>Invoke {@link #release} with saved state as argument, throwing
     * IllegalMonitorStateException if it fails.
     * <li>Block until signalled or interrupted.
     * <li>Reacquire by invoking specialized version of {@link #acquire}
     * with saved state as argument.
     * <li>If interrupted while blocked in step 4, throw
     * InterruptedException.
     * </ol>
     */
    public final void await() throws InterruptedException {
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
      Node node = addConditionWaiter(); // 将线程添加到waiter队列
      int savedState = fullyRelease(node); // 释放锁，不然其他线程无法获取锁（注意这里是独占模型），将该节点从等待队列移出
      int interruptMode = 0;
      while (!isOnSyncQueue(node)) { // 如果当前节点在同步队列上，跳出循环
        LockSupport.park(this); // 阻塞当前线程
        if ((interruptMode = checkInterruptWhileWaiting(node))
            != 0) // 如果被唤醒后，发现当前线程被中断，则跳出循环，没有中断则继续
        {
          break;
        }
      }
      /**
       * THROW_IE 表示抛出异常 REINTERRUPT表示只是设置中断标志位
       */
      // 程序走到这里，node有三种状态，0（正常状态，加入到等待队列成功，继续获取锁），THROW_IE（中断状态，该节点状态为CONDITION），REINTERRUPT(中断状态，节点状态不为CONDITION)
      // acquireQueued(node,
      // savedState)返回true表示当前节点再次获取锁的时候被中断了，那么只是设置中断标志位
      if (acquireQueued(node, savedState)
          && interruptMode != THROW_IE) // 如果节点不是中断状态且请求到了锁，将中断模式设为REINTERRUPTA
      {
        interruptMode = REINTERRUPT;
      }
      if (node.nextWaiter != null) // clean up if cancelled 如果下一个节点不为空
      {
        unlinkCancelledWaiters();
      }
      if (interruptMode != 0) // 如果中断模式不为0
      {
        reportInterruptAfterWait(interruptMode); // 根据中断模式判断只是设置中断标志位还是抛出异常
      }
    }

    /**
     * Implements timed condition wait.
     * <ol>
     * <li>If current thread is interrupted, throw InterruptedException.
     * <li>Save lock state returned by {@link #getState}.
     * <li>Invoke {@link #release} with saved state as argument, throwing
     * IllegalMonitorStateException if it fails.
     * <li>Block until signalled, interrupted, or timed out.
     * <li>Reacquire by invoking specialized version of {@link #acquire}
     * with saved state as argument.
     * <li>If interrupted while blocked in step 4, throw
     * InterruptedException.
     * </ol>
     */
    public final long awaitNanos(long nanosTimeout) throws InterruptedException {
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
      Node node = addConditionWaiter();
      int savedState = fullyRelease(node);
      final long deadline = System.nanoTime() + nanosTimeout;
      int interruptMode = 0;
      while (!isOnSyncQueue(node)) {
        if (nanosTimeout <= 0L) {
          transferAfterCancelledWait(node);
          break;
        }
        if (nanosTimeout >= spinForTimeoutThreshold) {
          LockSupport.parkNanos(this, nanosTimeout);
        }
        if ((interruptMode = checkInterruptWhileWaiting(node)) != 0) {
          break;
        }
        nanosTimeout = deadline - System.nanoTime();
      }
      if (acquireQueued(node, savedState) && interruptMode != THROW_IE) {
        interruptMode = REINTERRUPT;
      }
      if (node.nextWaiter != null) {
        unlinkCancelledWaiters();
      }
      if (interruptMode != 0) {
        reportInterruptAfterWait(interruptMode);
      }
      return deadline - System.nanoTime();
    }

    /**
     * Implements absolute timed condition wait.
     * <ol>
     * <li>If current thread is interrupted, throw InterruptedException.
     * <li>Save lock state returned by {@link #getState}.
     * <li>Invoke {@link #release} with saved state as argument, throwing
     * IllegalMonitorStateException if it fails.
     * <li>Block until signalled, interrupted, or timed out.
     * <li>Reacquire by invoking specialized version of {@link #acquire}
     * with saved state as argument.
     * <li>If interrupted while blocked in step 4, throw
     * InterruptedException.
     * <li>If timed out while blocked in step 4, return false, else true.
     * </ol>
     */
    public final boolean awaitUntil(Date deadline) throws InterruptedException {
      long abstime = deadline.getTime();
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
      Node node = addConditionWaiter();
      int savedState = fullyRelease(node);
      boolean timedout = false;
      int interruptMode = 0;
      while (!isOnSyncQueue(node)) {
        if (System.currentTimeMillis() > abstime) {
          timedout = transferAfterCancelledWait(node);
          break;
        }
        LockSupport.parkUntil(this, abstime);
        if ((interruptMode = checkInterruptWhileWaiting(node)) != 0) {
          break;
        }
      }
      if (acquireQueued(node, savedState) && interruptMode != THROW_IE) {
        interruptMode = REINTERRUPT;
      }
      if (node.nextWaiter != null) {
        unlinkCancelledWaiters();
      }
      if (interruptMode != 0) {
        reportInterruptAfterWait(interruptMode);
      }
      return !timedout;
    }

    /**
     * Implements timed condition wait.
     * <ol>
     * <li>If current thread is interrupted, throw InterruptedException.
     * <li>Save lock state returned by {@link #getState}.
     * <li>Invoke {@link #release} with saved state as argument, throwing
     * IllegalMonitorStateException if it fails.
     * <li>Block until signalled, interrupted, or timed out.
     * <li>Reacquire by invoking specialized version of {@link #acquire}
     * with saved state as argument.
     * <li>If interrupted while blocked in step 4, throw
     * InterruptedException.
     * <li>If timed out while blocked in step 4, return false, else true.
     * </ol>
     */
    public final boolean await(long time, TimeUnit unit) throws InterruptedException {
      long nanosTimeout = unit.toNanos(time);
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
      Node node = addConditionWaiter();
      int savedState = fullyRelease(node);
      final long deadline = System.nanoTime() + nanosTimeout;
      boolean timedout = false;
      int interruptMode = 0;
      while (!isOnSyncQueue(node)) {
        if (nanosTimeout <= 0L) {
          timedout = transferAfterCancelledWait(node);
          break;
        }
        if (nanosTimeout >= spinForTimeoutThreshold) {
          LockSupport.parkNanos(this, nanosTimeout);
        }
        if ((interruptMode = checkInterruptWhileWaiting(node)) != 0) {
          break;
        }
        nanosTimeout = deadline - System.nanoTime();
      }
      if (acquireQueued(node, savedState) && interruptMode != THROW_IE) {
        interruptMode = REINTERRUPT;
      }
      if (node.nextWaiter != null) {
        unlinkCancelledWaiters();
      }
      if (interruptMode != 0) {
        reportInterruptAfterWait(interruptMode);
      }
      return !timedout;
    }

    // support for instrumentation

    /**
     * Returns true if this condition was created by the given
     * synchronization object.
     *
     * @return {@code true} if owned
     */
    final boolean isOwnedBy(AbstractQueuedSynchronizer sync) {
      return sync == AbstractQueuedSynchronizer.this;
    }

    /**
     * Queries whether any threads are waiting on this condition. Implements
     * {@link AbstractQueuedSynchronizer#hasWaiters(ConditionObject)}.
     *
     * @return {@code true} if there are any waiting threads
     * @throws IllegalMonitorStateException if {@link #isHeldExclusively} returns {@code false}
     */
    protected final boolean hasWaiters() {
      if (!isHeldExclusively()) {
        throw new IllegalMonitorStateException();
      }
      for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
        if (w.waitStatus == Node.CONDITION) {
          return true;
        }
      }
      return false;
    }

    /**
     * Returns an estimate of the number of threads waiting on this
     * condition. Implements
     * {@link AbstractQueuedSynchronizer#getWaitQueueLength(ConditionObject)}
     * .
     *
     * @return the estimated number of waiting threads
     * @throws IllegalMonitorStateException if {@link #isHeldExclusively} returns {@code false}
     */
    protected final int getWaitQueueLength() {
      if (!isHeldExclusively()) {
        throw new IllegalMonitorStateException();
      }
      int n = 0;
      for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
        if (w.waitStatus == Node.CONDITION) {
          ++n;
        }
      }
      return n;
    }

    /**
     * Returns a collection containing those threads that may be waiting on
     * this Condition. Implements
     * {@link AbstractQueuedSynchronizer#getWaitingThreads(ConditionObject)}
     * .
     *
     * @return the collection of threads
     * @throws IllegalMonitorStateException if {@link #isHeldExclusively} returns {@code false}
     */
    protected final Collection<Thread> getWaitingThreads() {
      if (!isHeldExclusively()) {
        throw new IllegalMonitorStateException();
      }
      ArrayList<Thread> list = new ArrayList<Thread>();
      for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
        if (w.waitStatus == Node.CONDITION) {
          Thread t = w.thread;
          if (t != null)
            list.add(t);
        }
      }
      return list;
    }
  }
}
