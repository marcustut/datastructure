package tests;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import mq.Queue;

public class QueueTest {
    public static void queueAdd(Context ctx) {
        String name = Context.getMethodName(1);
        ctx.add(name);

        Queue<String> queue = new Queue<>();
        queue.add("1");
        queue.add("2");
        queue.add("3");

        if (queue.size() != 3) {
            ctx.fail(name);
            return;
        }

        if (!queue.toString().equals("[1, 2, 3]")) {
            ctx.fail(name);
            return;
        }

        ctx.pass(name);
    }

    public static void queueIteratorToStream(Context ctx) {
        String name = Context.getMethodName(1);
        ctx.add(name);

        Queue<String> queue = new Queue<>();
        queue.add("Alice");
        queue.add("Bob");
        queue.add("Candy");
        if (!queue.toString().equals("[Alice, Bob, Candy]")) {
            ctx.fail(name);
            return;
        }

        Stream<String> stream = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(queue.iterator(), Spliterator.ORDERED),
                false);

        List<Integer> list = stream.map(x -> x.length()).collect(Collectors.toList());
        if (list.size() != 3) {
            ctx.fail(name, "stream has different length");
            return;
        }
        if (!list.equals(Arrays.asList(new Integer[] { 5, 3, 5 }))) {
            ctx.fail(name);
            return;
        }

        ctx.pass(name);
    }
}
