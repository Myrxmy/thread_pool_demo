package main.java.tech.insight;

public interface RejectHandle {

    void reject(Runnable rejectCommand, MyThreadPool threadPool);
}
