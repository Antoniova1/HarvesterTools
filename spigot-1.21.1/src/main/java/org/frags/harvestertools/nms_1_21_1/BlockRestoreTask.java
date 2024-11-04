package org.frags.harvestertools.nms_1_21_1;

import java.util.PriorityQueue;
import java.util.Queue;

public class BlockRestoreTask implements Runnable {


    private final Queue<ReplenishCrop> restoreQueue = new PriorityQueue<>();

    @Override
    public void run() {
        long currentTime = System.currentTimeMillis();
        while (!restoreQueue.isEmpty() && restoreQueue.peek().time() <= currentTime) {
            restoreQueue.poll().restoreCrop();
        }
    }

    public void addToQueue(ReplenishCrop crop) {
        restoreQueue.add(crop);
    }

}
