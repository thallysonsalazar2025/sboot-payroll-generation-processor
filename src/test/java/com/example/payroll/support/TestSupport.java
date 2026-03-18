package com.example.payroll.support;

import java.util.Objects;

public final class TestSupport {
    private TestSupport() {
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " expected=" + expected + " actual=" + actual);
        }
    }

    public static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    public static void assertContains(String expectedFragment, String actual, String message) {
        if (actual == null || !actual.contains(expectedFragment)) {
            throw new AssertionError(message + " expectedFragment=" + expectedFragment + " actual=" + actual);
        }
    }

    public static void expectThrows(Class<? extends Throwable> type, Runnable action, String message) {
        try {
            action.run();
        } catch (Throwable throwable) {
            if (type.isInstance(throwable)) {
                return;
            }
            throw new AssertionError(message + " wrong exception type=" + throwable.getClass().getName(), throwable);
        }
        throw new AssertionError(message + " expected exception=" + type.getName());
    }
}
