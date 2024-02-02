import sylva.Vector;

class Main {
    public static void main(String[] args) {
        // Stack stack = new Stack();
        var vector = new Vector<Integer>();
        vector.add(1);
        vector.add(2);
        vector.add(3);
        vector.add(4);
        vector.remove(1);
        System.out.printf("vector: %s", vector.toString());
    }
}
