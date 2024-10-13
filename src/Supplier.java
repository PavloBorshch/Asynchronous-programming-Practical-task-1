import java.util.concurrent.Semaphore;

public class Supplier implements Runnable {
    private final Semaphore semaphore;
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
                    if (semaphore.availablePermits() < Warehouse.MAX_ITEMS) {
                        semaphore.release();
                        System.out.println(Main.GREEN + "Постачальник доставив товар на склад. Зараз на складі: "
                                + semaphore.availablePermits() + " товарів." + Main.RESET);
                    } else {
                        System.out.println(Main.YELLOW + "Склад заповнений. Постачальник чекає." + Main.RESET);
                    }

                    if (Warehouse.isClosed()) {
                        System.out.println(Main.RED + "Склад закрито. Постачальник припиняє доставку товару." + Main.RESET);
                        break;
                    }
                }
            }
        } catch (InterruptedException e) {
            System.err.println("Постачальника було перервано.");
        } finally {
            System.out.println(Main.RED + "===== Постачальник пішов додому =====" + Main.RESET);
        }
    }
}
