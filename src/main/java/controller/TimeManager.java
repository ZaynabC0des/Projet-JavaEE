package controller;

import java.util.Timer;
import java.util.TimerTask;

public class TimeManager {
    private Timer timer;
    private long remainingTime;
    private boolean isTimerRunning;

    public TimeManager() {
        this.remainingTime = 5 * 60 * 1000; // 5 minutes en millisecondes
        this.isTimerRunning = false;
    }

    public void startTimer() {
        if (isTimerRunning) return;

        isTimerRunning = true;
        timer = new Timer();
        long startTime = System.currentTimeMillis();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long elapsedTime = System.currentTimeMillis() - startTime;
                remainingTime -= elapsedTime;

                if (remainingTime <= 0) {
                    remainingTime = 0;
                    timer.cancel();
                    isTimerRunning = false;
                    System.out.println("Temps écoulé !");
                }
            }
        }, 0, 1000); // Exécution toutes les secondes
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
        isTimerRunning = false;
    }

    public long getRemainingTime() {
        return remainingTime;
    }
}
