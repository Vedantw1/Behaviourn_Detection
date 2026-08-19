package com.behavior.collector;

/**
 * Main launcher class delegating to JavaFX App.
 * Prevents JavaFX module runtime errors when launching without explicit module arguments.
 */
public class Main {
    public static void main(String[] args) {
        App.main(args);
    }
}
