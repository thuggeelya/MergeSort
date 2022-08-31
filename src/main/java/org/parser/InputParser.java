package org.parser;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class InputParser {

    private static final String SORT_MODE_FLAGS = "ad";
    private static final String DATA_TYPE_FLAGS = "si";
    private final String[] input;
    private final List<String> inputFilesNameList = new ArrayList<>();
    private String sortMode = null;
    private String dataType = null;
    private String outputFileName = null;

    public InputParser(String[] input) throws IllegalArgumentException, URISyntaxException {
        if (input == null || input.length == 0) {
            throw new IllegalArgumentException("Invalid input: empty");
        }

        this.input = input;
        parseInput();
    }

    public String getSortMode() {
        return sortMode;
    }

    public String getDataType() {
        return dataType;
    }

    public List<String> getInputFilesNameList() {
        return inputFilesNameList;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    private void parseInput() throws URISyntaxException {
        boolean isFirstFileName = true;

        for (String el : input) {
            if (el.matches("-.")) {
                String flag = el.charAt(1) + "";

                if (SORT_MODE_FLAGS.contains(flag) && sortMode == null) {
                    sortMode = flag;
                } else if (DATA_TYPE_FLAGS.contains(flag) && dataType == null) {
                    dataType = flag;
                }
            } else if (el.matches("\\S+\\.\\S+")) {
                if (isFirstFileName) {
                    outputFileName = el;
                    isFirstFileName = false;
                } else {
                    inputFilesNameList.add(getFilePath(el));
                }
            }
        }

        if (sortMode == null) {
            System.out.println("Sorting by -a");
            sortMode = "a";
        }

        if (dataType == null) {
            System.err.println("Invalid input: data type. Sorting by -s ..");
            dataType = "s";
        }
    }

    private String getFilePath(String fileName) throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(getClass()
                        .getClassLoader()
                        .getResource(fileName)).toURI())
                .toFile()
                .getAbsolutePath();
    }
}
