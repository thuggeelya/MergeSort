package org.example;

import org.producerconsumer.TConsumer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

final class TConsumerUtility {

    static TConsumer<String> newStringConsumer(boolean desc, boolean isString) {
        BlockingQueue<String> sharedQueue = new LinkedBlockingQueue<>();
        return new TConsumer<>(sharedQueue, desc, isString);
    }

    static TConsumer<Integer> newIntConsumer(boolean desc, boolean isString) {
        BlockingQueue<Integer> sharedQueue = new LinkedBlockingQueue<>();
        return new TConsumer<>(sharedQueue, desc, isString);
    }
}
