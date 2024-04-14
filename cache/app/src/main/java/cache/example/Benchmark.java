package cache.example;

//import java.util.Random;

import cache.Cache;

public class Benchmark {
//    private static Random rand = new Random();
    private static Cache<String, Boolean> cache = new Cache<>();

    public static void main(String[] args) throws InterruptedException {
        long duration = 0;
        int numRequests = 1000;

        for (int i = 0; i < numRequests; i++) {
            long took = serveWithCache();
            duration += took;
        }

        System.out.println("serveWithCache");
        System.out.println("-------------------------------------------------");
        System.out.printf("Took %.2fms for %d requests\n", duration * 1e-6, numRequests);
        System.out.printf("Average time for one request: %.2fms\n", (duration * 1e-6) / numRequests);
        System.out.printf("Throughput: %.2f requests/s\n", (numRequests / (duration * 1e-6) * 1e3));

        System.out.println();
        duration = 0;
        for (int i = 0; i < numRequests; i++) {
            long took = serveNoCache();
            duration += took;
        }

        System.out.println("serveNoCache");
        System.out.println("-------------------------------------------------");
        System.out.printf("Took %.2fms for %d requests\n", duration * 1e-6, numRequests);
        System.out.printf("Average time for one request: %.2fms\n", (duration * 1e-6) / numRequests);
        System.out.printf("Throughput: %.2f requests/s\n", (numRequests / (duration * 1e-6) * 1e3));
    }

    private static long serveNoCache() {
        long start = System.nanoTime();
        query();
        return System.nanoTime() - start;
    }

    private static long serveWithCache() {
        long start = System.nanoTime();
        Boolean exist = cache.get("test");
        if (exist == null) {
            query();
            cache.put("test", true, 10);
        }
        return System.nanoTime() - start;
    }

    private static void query() {
        try {
            // Thread.sleep(rand.nextLong(5, 30));
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
