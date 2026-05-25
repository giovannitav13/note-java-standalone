package app.salvanota.runtime;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Mantiene un'attività di basso profilo in background; controlli UI neutri.
 */
public final class KeepAwakeController {

    private static final int[] DELTAS = {-3, -2, -1, 1, 2, 3, 4};
    private static final Random RANDOM = new Random();

    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile Thread worker;

    public boolean isRunning() {
        return running.get();
    }

    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        Thread t = new Thread(this::runLoop, "salvanota-keepawake");
        t.setDaemon(true);
        worker = t;
        t.start();
    }

    public void stop() {
        running.set(false);
        Thread t = worker;
        if (t != null) {
            t.interrupt();
        }
    }

    private void runLoop() {
        try {
            Robot robot = new Robot();
            while (running.get()) {
                Point point = MouseInfo.getPointerInfo().getLocation();
                int moveX = randomDelta();
                int moveY = randomDelta();
                robot.mouseMove(point.x + moveX, point.y + moveY);
                Thread.sleep(randomBetween(120, 350));
                robot.mouseMove(point.x, point.y);
                Thread.sleep(randomBetween(30_000, 40_000));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception ignored) {
            running.set(false);
        }
    }

    private static int randomBetween(int min, int max) {
        return RANDOM.nextInt(max - min + 1) + min;
    }

    private static int randomDelta() {
        return DELTAS[RANDOM.nextInt(DELTAS.length)];
    }
}
