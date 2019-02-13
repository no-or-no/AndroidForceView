package top.amot.forceview;

public class Stepper {

    private Runnable callback;
    private final Object lock = new Object();
    private volatile int paused = 1;
    private volatile int markAsPaused = 1;
    private volatile int markAsDestroyed = 0;

    public Stepper(Runnable callback) {
        this.callback = callback;
        new StepperThread();
    }

    public void restart() {
        if (paused == 1) {
            markAsPaused = 0;
            lock.notify();
        }
    }

    public void stop() {
        markAsPaused = 1;
    }

    public void destroy() {
        markAsPaused = 0;
        markAsDestroyed = 1;
        if (paused == 1) {
            lock.notify();
        }
    }

    public boolean isStopped() {
        return paused == 1;
    }

    public boolean isDestroyed() {
        return markAsDestroyed == 1;
    }

    class StepperThread extends Thread {

        StepperThread() {
            super("StepperThread");
            start();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (markAsPaused == 1) {
                        synchronized (lock) {
                            paused = 1;
                            lock.wait();
                            paused = 0;
                        }
                    }
                    if (markAsDestroyed == 1) return;

                    if (callback != null) {
                        callback.run();
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
