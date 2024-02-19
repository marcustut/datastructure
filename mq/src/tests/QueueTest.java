package tests;

import mq.Queue;

public class QueueTest {
    public static void queueAdd(Context ctx) {
        String name = Context.getMethodName(1);
        ctx.add(name);

        Queue<String> queue = new Queue<>();
        queue.add("1");
        queue.add("2");
        queue.add("3");

        ctx.pass(name);
    }
}
