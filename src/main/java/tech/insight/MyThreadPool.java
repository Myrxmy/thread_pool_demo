package main.java.tech.insight;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MyThreadPool {

    BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(1024);

    Thread thread = new Thread(() ->{
        while (true) {
            try {
                Runnable command = blockingQueue.take();
                command.run();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    });


    void execute(Runnable command) {
        boolean offer = blockingQueue.offer(command);
    }

}
