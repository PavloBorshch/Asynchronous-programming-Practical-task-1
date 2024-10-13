import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Main {
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

        System.out.println("===== Програма завершена =====");
    }
}
