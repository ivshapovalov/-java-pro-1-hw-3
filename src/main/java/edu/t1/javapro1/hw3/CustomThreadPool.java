package edu.t1.javapro1.hw3;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CustomThreadPool {
    private final int capacity;
    private final Set<Thread> workers;
    private final LinkedList<Runnable> taskQueue;
    private final AtomicBoolean isShutdown;
    private final ReentrantLock lock;
    private final Condition taskAvailableCondition;

    public CustomThreadPool(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;
        this.taskQueue = new LinkedList<>();
        this.workers = new HashSet<>();
        this.isShutdown = new AtomicBoolean(false);
        this.lock = new ReentrantLock();
        this.taskAvailableCondition = lock.newCondition();

        for (int i = 0; i < capacity; i++) {
            Thread worker = new WorkerThread();
            worker.start();
            workers.add(worker);
        }
    }

    public void execute(Runnable task) {
        if (isShutdown.get()) {
            throw new IllegalStateException("ThreadPool is shut down");
        }

        lock.lock();
        try {
            taskQueue.addLast(task);
            taskAvailableCondition.signal();
        } finally {
            lock.unlock();
        }
    }

    public void shutdown() {
        if (isShutdown.compareAndSet(false, true)) {
            lock.lock();
            try {
                taskAvailableCondition.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }

    public void awaitTermination() throws InterruptedException {
        for (Thread worker : workers) {
            worker.join();
        }
    }

    private class WorkerThread extends Thread {
        @Override
        public void run() {
            while (true) {
                Runnable task = null;

                lock.lock();
                try {
                    while (taskQueue.isEmpty() && !isShutdown.get()) {
                        try {
                            taskAvailableCondition.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }

                    if (taskQueue.isEmpty() && isShutdown.get()) {
                        return;
                    }

                    if (!taskQueue.isEmpty()) {
                        task = taskQueue.pollFirst();
                    }
                } finally {
                    lock.unlock();
                }

                if (task != null) {
                    try {
                        task.run();
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
