package tests;

class Main {
    public static void main(String[] args) {
        Context ctx = new Context();

        QueueTest.queueAdd(ctx);

        ctx.report();
    }
}
