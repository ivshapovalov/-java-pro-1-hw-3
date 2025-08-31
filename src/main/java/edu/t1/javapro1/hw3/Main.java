package edu.t1.javapro1.hw3;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int capacity = 4;
        int taskAmount = 15;
        CustomThreadPool pool = new CustomThreadPool(capacity);
        for (int currentTask = 0; currentTask < taskAmount; currentTask++) {
            final int taskId = currentTask;
            try {
                pool.execute(() -> {
                    System.out.println("Task " + taskId + " executed by " + Thread.currentThread().getName());
                    try {
                        //Имитируем работу потока
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            } catch (IllegalStateException e) {
                System.out.println("Task " + taskId + " rejected");
            }
            if (currentTask == 10) {
                pool.shutdown();
                pool.awaitTermination();
            }
        }

        System.out.println("All tasks completed.");
    }
}
