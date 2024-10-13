import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Main {
    // ANSI escape codes для кольорів
    public static final String RESET = "\u001B[0m";  // скидання кольору
    public static final String RED = "\u001B[31m";   // червоний колір
    public static final String GREEN = "\u001B[32m"; // зелений колір
    public static final String YELLOW = "\u001B[33m"; // жовтий колір

    public static void main(String[] args) {
        Warehouse.openWarehouse();  // Відкриваємо склад

        Semaphore semaphore = Warehouse.getSemaphore();
        ExecutorService executor = Executors.newCachedThreadPool();

        // Запускаємо постачальника
        Supplier supplier = new Supplier(semaphore);
        Thread supplierThread = new Thread(supplier);
        supplierThread.start();

        // Створюємо окремий потік для закриття складу через 5 секунд
        new Thread(() -> {
            try {
                Thread.sleep(5000);  // Відлік 5 секунд
                Warehouse.closeWarehouse();  // Закриття складу
                supplier.closeWarehouse();  // Припинення роботи постачальника
            } catch (InterruptedException e) {
                System.err.println("Помилка при закритті складу.");
            }
        }).start();

        // Створюємо покупців
        for (int i = 0; i < 7; i++) {
            executor.execute(new Customer(semaphore, i));  // Додаємо покупців у чергу
            try {
                Thread.sleep((int) (Math.random() * 2000));  // Затримка приходу покупців
            } catch (InterruptedException e) {
                System.err.println("Помилка в затримці покупця.");
            }
        }

        // Закриваємо пул потоків після завершення роботи
        executor.shutdownNow();

        try {
            supplierThread.join();  // Чекаємо завершення роботи постачальника
        } catch (InterruptedException e) {
            System.err.println("Помилка при завершенні постачальника.");
        }

        System.out.println(RED + "===== Програма завершена =====" + RESET);
    }
}
