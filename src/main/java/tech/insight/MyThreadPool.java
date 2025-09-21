package main.java.tech.insight;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class MyThreadPool {

    private final int corePoolSize;
    private final int maxSize;
    private final int timeout;
    private final TimeUnit timeUnit;
    public final BlockingQueue<Runnable> blockingQueue;
    private final RejectHandle rejectHandle;

    public MyThreadPool(int corePoolSize, int maxSize, int timeout, TimeUnit timeUnit, BlockingQueue<Runnable> blockingQueue, RejectHandle rejectHandle) {
        this.corePoolSize = corePoolSize;
        this.maxSize = maxSize;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.blockingQueue = blockingQueue;
        this.rejectHandle = rejectHandle;
    }

    List<Thread> coreList = new ArrayList<>();
    // 辅助线程
    List<Thread> supportList = new ArrayList<>();

    /**
     * 执行任务的方法。
     * 使用 synchronized 关键字确保在同一时刻只有一个线程能执行此方法，
     * 从而保证对 coreList 和 supportList 的检查与修改是原子操作，避免了线程安全问题。
     * @param command 要执行的任务
     */
    public synchronized void execute(Runnable command) {
        // 1. 如果核心线程数未满，创建核心线程来执行任务
        if (coreList.size() < corePoolSize) {
            Thread thread = new CoreThread();
            coreList.add(thread);
            thread.start();
        }

        // 2. 将任务尝试放入阻塞队列
        // 注意：这里的逻辑有个小问题，无论是否创建了新线程，任务都会被放入队列。
        // 新创建的线程会去队列里取任务，所以这个任务最终会被执行。
        if (blockingQueue.offer(command)) {
            return;
        }

        // 3. 如果队列已满，且总线程数未达到最大值，创建辅助线程
        if (coreList.size() + supportList.size() < maxSize) {
            Thread thread = new SupportThread();
            supportList.add(thread);
            thread.start();
        }

        // 4. 再次尝试将任务放入队列（新创建的辅助线程可以消费它）
        // 如果第二次还失败，说明队列真的满了，并且无法再创建新线程，此时就应该拒绝任务。
        if (!blockingQueue.offer(command)) {
            rejectHandle.reject(command, this);
        }
    }
    class CoreThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Runnable command = blockingQueue.take();
                    command.run();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    class SupportThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Runnable command = blockingQueue.poll(timeout, timeUnit);
                    if (command == null){
                        break;
                    }
                    command.run();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println(Thread.currentThread().getName() + "线程结束了！");
        }
    }
}
