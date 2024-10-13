import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class Warehouse {
    public static final int MAX_ITEMS = 3;  // Максимальна кількість товарів на складі
    private static final Semaphore semaphore = new Semaphore(0);  // Початково товару немає
    public static final Object lock = new Object();
    private static final Queue<Customer> customerQueue = new LinkedList<>();  // Черга покупців
    private static boolean isClosed = false;

    public static Semaphore getSemaphore() {
        return semaphore;
    }

    public static void openWarehouse() {
        System.out.println("===== Склад відкрито =====");
    }

    public static void closeWarehouse() {
        synchronized (lock) {
            if (!isClosed) {
                isClosed = true;
                System.out.println("===== Склад закрито =====");
                processRemainingCustomers();  // Обробка покупців після закриття
            }
        }
    }

    public static boolean isClosed() {
        return isClosed;
    }

    public static void addToQueue(Customer customer) {
        synchronized (lock) {
            customerQueue.add(customer);
        }
    }

    public static void processQueue() throws InterruptedException {
        while (true) {
            Customer currentCustomer;
            synchronized (lock) {
                if (customerQueue.isEmpty()) return;  // Якщо черга порожня, виходимо
                currentCustomer = customerQueue.peek();  // Беремо першого покупця з черги
            }

            if (semaphore.tryAcquire()) {
                currentCustomer.receiveProduct();  // Покупець отримує товар
                synchronized (lock) {
                    customerQueue.poll();  // Видаляємо покупця з черги
                }
            } else if (isClosed) {
                currentCustomer.leaveWithoutProduct();  // Якщо склад закрито, покупець іде додому
                synchronized (lock) {
                    customerQueue.poll();  // Видаляємо покупця з черги
                }
            } else {
                if (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(100);  // Чекаємо перед наступною спробою
                }
            }
        }
    }

    private static void processRemainingCustomers() {
        while (!customerQueue.isEmpty()) {
            Customer customer = customerQueue.poll();
            customer.leaveWithoutProduct();  // Покупці йдуть додому після закриття складу
        }
    }
}
