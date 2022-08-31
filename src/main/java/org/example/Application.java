package org.example;

import org.parser.InputParser;
import org.producerconsumer.TConsumer;
import org.producerconsumer.TProducer;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Application {

    public static void main(String[] args) {
        InputParser parser = null;

        try {
            parser = new InputParser(args);
        } catch (IllegalArgumentException | URISyntaxException e) {
            System.err.println(e.getMessage());
        }

        if (parser != null) {
            List<String> inputFilesNameList = parser.getInputFilesNameList();
            int inputFilesCount = inputFilesNameList.size();

            if (inputFilesCount == 0) {
                System.err.println("Invalid input: no files to read");
            } else {
                String sortMode = parser.getSortMode();
                String dataType = parser.getDataType();
                boolean desc = "d".equals(sortMode);
                boolean isString = "s".equals(dataType);
                TConsumer<?> consumer;

                if (!isString) {
                    consumer = TConsumerUtility.newIntConsumer(desc, isString);
                } else {
                    consumer = TConsumerUtility.newStringConsumer(desc, isString);
                }

                ExecutorService executor = Executors.newFixedThreadPool(inputFilesCount + 1);
                executor.execute(consumer);
                Semaphore semaphore = new Semaphore(0);
                inputFilesNameList.forEach(file -> executor.execute(new TProducer<>(file, consumer.getQueue(), semaphore)));

                try {
                    semaphore.acquire(inputFilesCount);
                    consumer.terminate();
                    executor.shutdown();
                } catch (InterruptedException e) {
                    System.err.println(Thread.currentThread() + " was interrupted");
                }

                String outputFileName = parser.getOutputFileName();

                try (PrintStream printStream = new PrintStream(Objects.requireNonNullElse(outputFileName,
                        "src/main/resources/out.txt"))) {
                    consumer.sort().forEach(printStream::println);
                } catch (IOException e) {
                    System.err.println("Unable write to " + outputFileName);
                }
            }
        }
    }
}
