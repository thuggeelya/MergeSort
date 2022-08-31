package org.producerconsumer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;

import static java.lang.Integer.parseInt;

public class TConsumer<T> implements Runnable {

    private final List<T> consumedList = new ArrayList<>();
    private final BlockingQueue<T> sharedQueue;
    private final boolean desc;
    private final boolean isString;
    private boolean running = true;
    private T POISON_PILL = null;

    public TConsumer(BlockingQueue<T> sharedQueue, boolean desc, boolean isString) {
        this.sharedQueue = sharedQueue;
        this.desc = desc;
        this.isString = isString;
    }

    public BlockingQueue<T> getQueue() {
        return sharedQueue;
    }

    @SuppressWarnings("unchecked")
    public void terminate() throws InterruptedException {
        running = false;

        try {
            POISON_PILL = (T) "";
            sharedQueue.put(POISON_PILL);
        } catch (ClassCastException cce) {
            try {
                POISON_PILL = (T) (Integer) 0;
                sharedQueue.put(POISON_PILL);
            } catch (NumberFormatException nfe) {
                System.err.println("Consumer error: invalid input");
            }
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                T next = sharedQueue.take();

                if (next != POISON_PILL) {
                    consumedList.add(next);
                }
            } catch (InterruptedException e) {
                System.err.println(Thread.currentThread().getName() + " was interrupted");
            }
        }
    }

    public List<T> sort() {
        return mergeSort(consumedList);
    }

    private List<T> mergeSort(List<T> list) {
        int size = list.size();

        if (size > 1) {
            int middle = size / 2;
            List<T> halfOne = new ArrayList<>(middle);
            List<T> halfTwo = new ArrayList<>(size - middle);

            for (int i = 0; i < middle; i++) {
                halfOne.add(list.get(i));
            }

            for (int i = middle; i < size; i++) {
                halfTwo.add(list.get(i));
            }

            halfOne = mergeSort(halfOne);
            halfTwo = mergeSort(halfTwo);
            return getSortedMerge(halfOne, halfTwo);
        } else {
            return list;
        }
    }

    private List<T> getSortedMerge(List<T> halfOne, List<T> halfTwo) {
        int sizeOne = halfOne.size();
        int sizeTwo = halfTwo.size();
        int tempSize = sizeOne + sizeTwo;
        List<T> sortedList = new ArrayList<>(tempSize);
        int biasOne = 0;
        int biasTwo = 0;

        for (int i = 0; i < tempSize; i++) {
            int iOne = i - biasOne;
            int iTwo = i - biasTwo;

            if (iOne >= sizeOne) {
                if (iTwo >= sizeTwo) {
                    break;
                }

                while (iTwo < sizeTwo) {
                    sortedList.add(halfTwo.get(iTwo++));
                }

                break;
            } else if (iTwo >= sizeTwo) {
                while (iOne < sizeOne) {
                    sortedList.add(halfOne.get(iOne++));
                }

                break;
            } else {
                T tOne = halfOne.get(iOne);
                T tTwo = halfTwo.get(iTwo);

                if (oCompare(tOne, tTwo) < 0) {
                    sortedList.add(tOne);
                    biasTwo++;
                } else if (oCompare(tOne, tTwo) == 0) {
                    sortedList.add(tOne);
                    sortedList.add(tTwo);
                } else {
                    sortedList.add(tTwo);
                    biasOne++;
                }

            }
        }

        return sortedList;
    }

    private int oCompare(Object o1, Object o2) {
        if (isString) {
            return Objects.compare(String.valueOf(o1), String.valueOf(o2), desc ?
                    Comparator.reverseOrder() :
                    Comparator.naturalOrder());
        } else {
            return desc ?
                    Integer.compare(parseInt((String) o2), parseInt((String) o1)) :
                    Integer.compare(parseInt((String) o1), parseInt((String) o2));
        }
    }
}
