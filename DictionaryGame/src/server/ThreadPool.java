package server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A simple custom thread pool implementation for managing and executing Runnable tasks.
 * It maintains a fixed number of worker threads that continuously poll tasks from a queue and execute them.
 * This implementation is used on the server side to handle multiple client connections concurrently.
 */
public class ThreadPool {
	
	private final BlockingQueue<Runnable> taskQueue;
    private final Thread[] workers;


    /**
     * Constructor to create a thread pool with a given number of worker threads.
     *
     * @param poolSize the number of threads to keep in the pool
     */
    public ThreadPool(int poolSize) {
        taskQueue = new LinkedBlockingQueue<>();
        workers = new Thread[poolSize];

        // Initialize and start each worker thread
        for (int i = 0; i < poolSize; i++) {
            workers[i] = new Worker("Worker" + i);
            workers[i].start();
        }
    }

    /**
     * Public method to submit a task to the thread pool.
     * The task will be queued and executed by an available worker thread.
     *
     * @param task the task to be executed (must implement Runnable)
     */
    public void submit(Runnable task) {
        try {
            taskQueue.put(task); // This blocks if the queue is full
        } catch (InterruptedException e) {
        	// Preserve interrupt status so higher-level handlers can respond appropriately
            Thread.currentThread().interrupt();
        }
    }


    /**
     * Inner class representing a worker thread.
     * Continuously takes tasks from the queue and executes them.
     */
    private class Worker extends Thread {
        public Worker(String name) {
            super(name);
        }

        public void run() {
            while (true) {
                try {
                	// Take a task from the queue; blocks if empty
                    Runnable task = taskQueue.take(); 
                    task.run();
                } catch (InterruptedException e) {
                	// If interrupted (optional), break the loop and allow thread to exit
                    // This line could be enhanced to support graceful shutdown
                    break;
                }
            }
        }
    }
}
