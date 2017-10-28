/**
 * Created by yangqc on 2017/10/27
 */
package com.yqc.forkJoin;
/**
 * Fork/Join使用两个类来完成以上两件事情：
 * <p>
 * ForkJoinTask：我们要使用ForkJoin框架，必须首先创建一个ForkJoin任务。它提供在任务中执行fork()和join()操作的机制，
 * 通常情况下我们不需要直接继承ForkJoinTask类，而只需要继承它的子类，Fork/Join框架提供了以下两个子类：
 * (1)RecursiveAction：用于没有返回结果的任务。
 * (2)RecursiveTask ：用于有返回结果的任务。
 * ForkJoinPool ：ForkJoinTask需要通过ForkJoinPool来执行，任务分割出的子任务会添加到当前工作线程所维护的双端队列中，
 * 进入队列的头部。当一个工作线程的队列里暂时没有任务时，它会随机从其他工作线程的队列的尾部获取一个任务。
 */