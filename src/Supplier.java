import java.util.concurrent.Semaphore;

public class Supplier implements Runnable {
    private Semaphore semaphore;
    private boolean warehouseClosed;

    public Supplier(Semaphore semaphore) {
        this.semaphore = semaphore;
        this.warehouseClosed = false;
    }

    public void closeWarehouse() {
        this.warehouseClosed = true;
    }

    @Override
    public void run() {
        try {
            while (!warehouseClosed || semaphore.hasQueuedThreads()) {
                Thread.sleep((int) (Math.random() * 3000)); // Імітація доставки товару

                synchronized (Warehouse.lock) {
                    if (Warehouse.isClosed()) {
                        System.out.println("Склад закрито. Постачальник припиняє доставку товару.");
                        break;
                    }

                    if (semaphore.availablePermits() < Warehouse.MAX_ITEMS) {
                        semaphore.release();
                        System.out.println("Постачальник доставив товар на склад. Зараз на складі: "
                                + semaphore.availablePermits() + " товарів.");
                    } else {
                        System.out.println("Склад заповнений. Постачальник чекає.");
                    }
                }
            }
        } catch (InterruptedException e) {
            System.err.println("Постачальника було перервано.");
        } finally {
            System.out.println("===== Постачальник пішов додому =====");
        }
    }
}
