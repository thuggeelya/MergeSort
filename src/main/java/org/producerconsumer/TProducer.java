package org.producerconsumer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

public record TProducer<T>(String fullFileName, BlockingQueue<T> sharedQueue, Semaphore semaphore) implements Runnable {

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new FileReader(fullFileName))) {
            String line;

            while ((line = br.readLine()) != null) {
                transferLine(line.replaceAll("\\s+", ""));
            }
        } catch (IOException e) {
            System.err.println("Unable open " + fullFileName);
        } finally {
            semaphore.release();
        }
    }

    private void transferLine(String line) {
        try {
            @SuppressWarnings("unchecked")
            T tLine = (T) line;
            sharedQueue.put(tLine);
        } catch (ClassCastException cce) {
            try {
                @SuppressWarnings("unchecked")
                T tLine = (T) (Integer) Integer.parseInt(line);
                sharedQueue.put(tLine);
            } catch (NumberFormatException nfe) {
                System.err.println("Element '" + line + "' is not allowed. File: " + fullFileName);
            } catch (InterruptedException e) {
                System.err.println(Thread.currentThread().getName() + " was interrupted (" + fullFileName + ")");
            }
        } catch (InterruptedException e) {
            System.err.println(Thread.currentThread().getName() + " was interrupted (" + fullFileName + ")");
        }
    }
}
