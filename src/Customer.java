import java.util.concurrent.Semaphore;

public class Customer implements Runnable {
    private final Semaphore semaphore;
    private final int customerId;

    public Customer(Semaphore semaphore, int customerId) {
        this.semaphore = semaphore;
        this.customerId = customerId + 1;
    }

    @Override
    public void run() {
        synchronized (Warehouse.lock) {
            if (Warehouse.isClosed()) {
                System.out.println("Покупець " + customerId + " прийшов і дізнався, що склад закрито. Іде додому.");
                return;
            }
            System.out.println("Покупець " + customerId + " прийшов на склад і чекає своєї черги.");
            Warehouse.addToQueue(this);  // Додаємо до черги покупців
        }

        try {
            Warehouse.processQueue();  // Очікуємо обробки нашої черги
        } catch (InterruptedException e) {
            System.err.println("Виникла помилка при отриманні товару.");
        }
    }

    public void receiveProduct() {
        System.out.println("Покупець " + customerId + " отримав товар.");
    }

    public void leaveWithoutProduct() {
        System.out.println("Покупець " + customerId + " не отримав товар і йде додому.");
    }
}
