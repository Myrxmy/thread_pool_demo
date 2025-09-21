# Hand-Written Thread Pool (手写线程池)

这是一个从零开始、逐步构建的 Java 线程池实现，旨在深入理解 `java.util.concurrent.ThreadPoolExecutor` 的核心工作原理和设计思想。

本项目没有依赖任何第三方库，通过原生 Java 实现，完整地复刻了线程池的核心功能，包括核心线程管理、任务队列、辅助线程扩展以及拒绝策略。

## ✨ 项目特性 (Features)

通过分析本项目的提交历史，你可以清晰地看到一个线程池是如何被逐步设计和完善的：

-   **v0.1: 基本实现**
    -   构建了基于阻塞队列 (`BlockingQueue`) 的生产者-消费者模型，实现了任务的异步处理。
    -   实现了最基础的 `execute` 方法，用于接收并执行任务。

-   **v0.2: 核心与辅助线程池**
    -   引入了 **核心线程 (`corePoolSize`)** 的概念，线程池启动后会按需创建核心线程来处理任务。
    -   引入了 **辅助线程 (`maxSize`)** 的概念，当任务队列已满且核心线程都在忙碌时，可以创建额外的辅助线程来“救火”。
    -   通过两个独立的 `List` 来分别管理核心线程和辅助线程。

-   **v0.3: 拒绝策略 (Rejection Policy)**
    -   实现了完整的任务提交流程：`核心线程` -> `任务队列` -> `辅助线程`。
    -   当所有线程都在忙碌且任务队列也已满时，线程池将触发**拒绝策略**，防止系统资源被耗尽。
    -   实现了自定义的拒绝策略：**DiscardOldestPolicy** (抛弃队列中最旧的任务)，当新任务被拒绝时，会从阻塞队列的头部取出一个任务并丢弃，然后重新尝试将新任务加入队列。

-   **v0.4: 线程安全**
    -   通过 `synchronized` 关键字确保了 `execute` 方法的原子性，解决了在多线程环境下并发添加任务时可能导致的线程创建数量超限问题。

## 🚀 快速开始 (Getting Started)

你可以直接将 `MyThreadPool.java` 复制到你的项目中，并像下面这样使用它：

```java
public class Main {
    public static void main(String[] args) {
        // 1. 创建一个自定义的线程池实例
        MyThreadPool threadPool = new MyThreadPool();

        // 2. 循环向线程池提交任务
        for (int i = 0; i < 20; i++) {
            final int taskNumber = i;
            Runnable task = () -> {
                System.out.println("任务 " + taskNumber + " 正在由线程 " + Thread.currentThread().getName() + " 执行");
                try {
                    // 模拟任务执行耗时
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };

            // 3. 通过 execute 方法提交任务
            threadPool.execute(task);
        }

        System.out.println("所有任务已提交，主线程继续执行其他事情...");
    }
}
```

## 🧠 设计思路 (Design)

本线程池的核心设计遵循了 J.U.C (java.util.concurrent) 中 `ThreadPoolExecutor` 的经典思想：

1.  **任务提交 (`execute`)**:
    -   当一个新任务到来时，首先检查核心线程数是否已满。如果未满，则创建一个新的**核心线程**来处理任务。
    -   如果核心线程已满，则尝试将任务放入**阻塞队列**。
    -   如果阻塞队列也满了，则检查总线程数（核心线程 + 辅助线程）是否达到上限。如果未满，则创建一个新的**辅助线程**来处理任务。
    -   如果总线程数也达到上限，则执行**拒绝策略**。

2.  **线程工作 (`Worker Thread`)**:
    -   所有线程（无论是核心还是辅助）都执行一个相同的循环任务：通过调用阻塞队列的 `take()` 方法不断地获取并执行任务。`take()` 方法的阻塞特性使得线程在没有任务时能够自动挂起，从而节省 CPU 资源。

## 📁 项目结构

```
.
└── src
    └── main
        └── java
            └── tech
                └── insight
                    ├── MyThreadPool.java  // 线程池的核心实现类
                    └── Main.java          // 使用示例
```

---

*该项目由 [Myrxmy] 创建，旨在用于学习和交流。*
