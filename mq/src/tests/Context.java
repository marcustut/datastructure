package tests;

import java.util.HashMap;
import java.util.Map;

public class Context {
    private HashMap<String, Status> tests = new HashMap<>();

    private enum Status {
        NEW,
        FAILED,
        PASSED
    }

    public Context() {
    }

    public void add(String name) {
        if (tests.put(name, Status.NEW) != null) {
            System.err.printf("A test of name '%s' already exists\n", name);
            System.exit(1);
        }
        System.out.printf("Testing %s ", name);
    }

    public void pass(String name) {
        if (!tests.containsKey(name)) {
            System.err.printf("Passing test of name '%s' but it does not exist\n", name);
            System.exit(1);
        }
        tests.put(name, Status.PASSED);
        System.out.printf("✅\n");
    }

    public void fail(String name) {
        if (!tests.containsKey(name)) {
            System.err.printf("Failing test of name '%s' but it does not exist\n", name);
            System.exit(1);
        }
        tests.put(name, Status.FAILED);
        System.out.printf("⛔️\n");
    }

    public void fail(String name, String reason) {
        if (!tests.containsKey(name)) {
            System.err.printf("Failing test of name '%s' but it does not exist\n", name);
            System.exit(1);
        }
        tests.put(name, Status.FAILED);
        System.out.printf("⛔️ %s\n", reason);
    }

    public void report() {
        int total = tests.size();
        int passed = 0;
        int failed = 0;

        for (Map.Entry<String, Status> test : tests.entrySet()) {
            Status status = test.getValue();
            if (status == Status.PASSED)
                passed++;
            else if (status == Status.FAILED)
                failed++;
        }

        System.out.println("----------------------------------");
        System.out.printf("Passed %d/%d test cases, %d failed\n", passed, total, failed);
    }

    /**
     * Get the method name for a depth in call stack. <br />
     * Utility function
     * 
     * @param depth depth in the call stack (0 means current method, 1 means call
     *              method, ...)
     * @return method name
     */
    public static String getMethodName(final int depth) {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        return ste[ste.length - 1 - depth].getMethodName();
    }
}
