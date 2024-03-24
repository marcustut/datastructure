package huffman;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import huffman.Huffman;

public class Huffman {
    public static final int MAX_DEPTH = 32; // because we use Integer to represent the code (Integer is 32-bit)

    private static class Code {
        static final int DIGITS = 6;
        int code;
        int numBits;

        Code(int code) {
            this.code = code;
        }

        Code(int code, int numBits) {
            this.code = code;
            this.numBits = numBits;
        }

        public static Code fromString(String str) {
            if (str.length() != DIGITS)
                System.err.println("wrong digits for Code");

            return new Code(
                    Integer.parseInt(str.substring(0, 4), 16),
                    Integer.parseInt(str.substring(DIGITS - 2, DIGITS), 16));
        }

        public String path() {
            return Integer.toBinaryString((1 << numBits) | code).substring(1);
        }

        @Override
        public String toString() {
            return String.format("%1$04X", code) + String.format("%1$02X", numBits);
        }
    }

    public static class Encoded {
        public String encoded;
        public HashMap<Character, Code> codes;
        public Node root;

        public Encoded(String encoded, HashMap<Character, Code> codes, Node root) {
            this.encoded = encoded;
            this.codes = codes;
            this.root = root;
        }

        @Override
        public String toString() {
            return "{encoded=" + encoded + ", codes=" + codes + "}";
        }
    }

    public static class Node implements Comparable<Node>, BinaryTreePrinter.PrintableNode {
        int freq;
        char c;
        Node left, right;

        public Node(int freq) {
            this.freq = freq;
        }

        public Node(char c) {
            this.c = c;
        }

        public Node(char c, int freq) {
            this(c);
            this.freq = freq;
        }

        public int depth() {
            // Leaf node has a depth of 1
            if (left == null && right == null)
                return 1;

            return depth(this);
        }

        private int depth(Node root) {
            if (root == null) // reached the end
                return 0;

            // Take the maximum depth of left or right subtree
            return Math.max(depth(root.left), depth(root.right)) + 1;
        }

        @Override
        public int compareTo(Node o) {
            return freq - o.freq;
        }

        @Override
        public String toString() {
            return "{" + c + "=" + freq + "}";
        }

        @Override
        public BinaryTreePrinter.PrintableNode getLeft() {
            return left;
        }

        @Override
        public BinaryTreePrinter.PrintableNode getRight() {
            return right;
        }

        @Override
        public String getText() {
            return toString();
        }

    }

    public static Encoded encode(String payload) {
        // Construct a frequency map of individual character
        HashMap<Character, Integer> freqMap = new HashMap<>();
        for (int i = 0; i < payload.length(); i++)
            freqMap.put(payload.charAt(i), freqMap.getOrDefault(payload.charAt(i), 0) + 1);

        // Build a huffman tree from the frequencies
        Node root = buildTree(freqMap);

        // Print to STDERR if exceeds maximum depth
        if (root.depth() > MAX_DEPTH)
            System.err.printf(
                    "Huffman Tree has a depth of %d which exceeds the maximum of %d, expect the encoded data to be wrong\n",
                    root.depth(), MAX_DEPTH);

        // Traverse the tree to get the codes
        HashMap<Character, Code> codes = getCodesFromTree(root);

        // Encode the payload with codes
        String encoded = "";
        for (int i = 0; i < payload.length(); i++)
            encoded += codes.get(payload.charAt(i));

        return new Encoded(encoded, codes, root);
    }

    public static String decode(Huffman.Node root, String encoded) {
        if (encoded.length() % Code.DIGITS != 0)
            System.err.printf("The size of the encoded string is not valid (must be a multiple of %d)\n", Code.DIGITS);

        String decoded = "";
        Node curr = root;
        Code[] codes = new Code[encoded.length() / Code.DIGITS];

        // Parse the decoded string into codes
        for (int i = 0; i < encoded.length(); i += Code.DIGITS)
            codes[i / Code.DIGITS] = Code.fromString(encoded.substring(i, i + Code.DIGITS));

        // Go through the codes and follow the path in the tree
        for (Code code : codes) {
            // Traverse the path
            for (char c : code.path().toCharArray())
                if (c == '0') // if it is '0' then go left
                    curr = curr.left;
                else // else it is a '1', go right
                    curr = curr.right;

            // Get character from the current path and reset
            decoded += curr.c;
            curr = root;
        }

        return decoded;
    }

    public static String serializeTree(Huffman.Node root) {
        if (root == null)
            throw new NullPointerException();

        return serializeTree(root, "");
    }

    private static String serializeTree(Huffman.Node root, String serialized) {
        if (root.left == null && root.right == null) // leaf node, write 1 and character
            serialized += "1" + (root.c == '\n' ? (char) 0 : root.c);
        else // aggregated node, write 0 then continue traversing
            serialized += "0" + serializeTree(root.left, serialized) + serializeTree(root.right, serialized);
        return serialized;
    }

    public static Huffman.Node deserializeTree(String serialized) {
        if (serialized == null)
            throw new NullPointerException();

        ArrayList<Character> bits = new ArrayList<>(
                serialized.chars().mapToObj(e -> (char) e).collect(Collectors.toList()));
        return deserializeTreeHelper(bits);
    }

    private static Huffman.Node deserializeTreeHelper(ArrayList<Character> bits) {
        if (bits.size() == 0) // finish processing the bits
            return null;

        if (bits.getFirst() == '1') { // bit 1 meaning the next one is a character
            Huffman.Node node = new Huffman.Node((int) bits.get(1) == 0 ? '\n' : bits.get(1));
            bits.removeFirst();
            bits.removeFirst();
            return node;
        }

        Huffman.Node root = new Huffman.Node(0); // bit 0 then skip
        bits.removeFirst();

        // Continue for the left and right subtrees
        root.left = deserializeTreeHelper(bits);
        root.right = deserializeTreeHelper(bits);

        return root;
    }

    private static Node buildTree(HashMap<Character, Integer> freqMap) {
        // Convert these frequencies as a Node and put them in a queue
        MinHeap<Node> heap = new MinHeap<>();
        freqMap.forEach((c, freq) -> heap.add(new Node(c, freq)));

        // Consume the nodes in heap to construct a tree
        while (heap.size() > 1) {
            Node min = heap.poll();
            Node nextMin = heap.poll();

            Node aggregated = new Node(min.freq + nextMin.freq);
            aggregated.left = min;
            aggregated.right = nextMin;

            heap.add(aggregated);
        }

        // Return the root (the only node remaining in heap)
        return heap.poll();
    }

    private static HashMap<Character, Code> getCodesFromTree(Node root) {
        HashMap<Character, Code> codes = new HashMap<>();
        getCodesFromTree(root, codes, new Code(0));
        return codes;
    }

    private static void getCodesFromTree(Node root, HashMap<Character, Code> codes, Code code) {
        // Base case: arrive at leaf node (is a character node)
        if (root.left == null && root.right == null) {
            codes.put(root.c, code);
            return;
        }

        // Traverse left subtree
        getCodesFromTree(root.left, codes, new Code((code.code << 1) | 0, code.numBits + 1));

        // Traverse right subtree
        getCodesFromTree(root.right, codes, new Code((code.code << 1) | 1, code.numBits + 1));
    }
}
