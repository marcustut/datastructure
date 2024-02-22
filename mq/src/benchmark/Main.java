package benchmark;

import mq.Queue;
import java.io.*;
import java.util.*;

class Main {
    static final int[] NUM_OPS = {100, 1000, 10000, 100000, 1000000, 10000000};
    static BufferedWriter writer;

    public static void main(String[] args) throws Exception {
        writer = new BufferedWriter(new FileWriter("data/benchmark.csv"));
        writer.append("name,ops,duration\n");

        java.util.Queue<Integer> queue = new Queue<Integer>();
        bench("[mq.Queue - offer]", queue, (q, i) -> { q.offer(i); });
        bench("[mq.Queue - poll]", queue, (q, i) -> { q.poll(); });

        java.util.Queue<Integer> queue2 = new LinkedList<Integer>();
        bench("[LinkedList - offer]", queue2, (q, i) -> { q.offer(i); });
        bench("[LinkedList - poll]", queue2, (q, i) -> { q.poll(); });

        java.util.Queue<Integer> queue3 = new PriorityQueue<Integer>();
        bench("[PriorityQueue - offer]", queue3, (q, i) -> { q.offer(i); });
        bench("[PriorityQueue - poll]", queue3, (q, i) -> { q.poll(); });

        java.util.Queue<Integer> queue4 = new ArrayDeque<Integer>();
        bench("[ArrayDeque - offer]", queue4, (q, i) -> { q.offer(i); });
        bench("[ArrayDeque - poll]", queue4, (q, i) -> { q.poll(); });

        writer.close();
    }

    interface QueueOp {
        void op(java.util.Queue<Integer> queue, int index);
    }

    private static void bench(String prefix, java.util.Queue<Integer> queue, QueueOp op) throws Exception {
        for (int i = 0; i < NUM_OPS.length; i++) {
            long start = System.nanoTime();
            for (int idx = 0; idx < NUM_OPS[i]; idx++) {
                op.op(queue, idx);
            }
            long end = System.nanoTime();
            long duration = end - start;
            writer.append(prefix + "," + NUM_OPS[i] + "," + duration + "\n");
            System.out.printf("%s Took %.2fms for %d operations\n", prefix, duration * 1e-6, NUM_OPS[i]);
        }
    }
}
