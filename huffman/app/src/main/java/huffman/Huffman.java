package huffman;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Huffman {
    public static class Encoded {
        public String encoded;
        public HashMap<Character, String> codes;
        public Node root;

        public Encoded(String encoded, HashMap<Character, String> codes, Node root) {
            this.encoded = encoded;
            this.codes = codes;
            this.root = root;
        }

        @Override
        public String toString() {
            return "{encoded=" + encoded + ", codes=" + codes + "}";
        }
    }

    public static class Node implements Comparable<Node> {
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

        @Override
        public int compareTo(Node o) {
            return freq - o.freq;
        }

        @Override
        public String toString() {
            return "{" + c + "=" + freq + "}";
        }
    }

    public static Encoded encode(String payload) {
        // Construct a frequency map of individual character
        HashMap<Character, Integer> freqMap = new HashMap<>();
        for (int i = 0; i < payload.length(); i++)
            freqMap.put(payload.charAt(i), freqMap.getOrDefault(payload.charAt(i), 0) + 1);

        // Build a huffman tree from the frequencies
        Node root = buildTree(freqMap);

        // Traverse the tree to get the codes
        HashMap<Character, String> codes = getCodesFromTree(root);

        // Encode the payload with codes
        String encoded = "";
        for (int i = 0; i < payload.length(); i++)
            encoded += codes.get(payload.charAt(i));

        return new Encoded(encoded, codes, root);
    }

    public static String decode(Huffman.Node root, String encoded) {
        String decoded = "";
        Node curr = root;

        // Go through the encoded string and follow the path in the tree
        for (int i = 0; i < encoded.length(); i++) {
            if (encoded.charAt(i) == '0') // if it is '0' then go left
                curr = curr.left;
            else // else it is a '1', go right
                curr = curr.right;

            // Once arriving at leaf node, we found the character so we reset
            if (curr.left == null && curr.right == null) {
                decoded += curr.c;
                curr = root;
            }
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
        MinHeap<Node> queue = new MinHeap<>();
        freqMap.forEach((c, freq) -> queue.add(new Node(c, freq)));

        // Consume the nodes in queue to construct a tree
        while (queue.size() > 1) {
            Node min = queue.poll();
            Node nextMin = queue.poll();

            Node aggregated = new Node(min.freq + nextMin.freq);
            aggregated.left = min;
            aggregated.right = nextMin;

            queue.add(aggregated);
        }

        return queue.poll();
    }

    private static HashMap<Character, String> getCodesFromTree(Node root) {
        HashMap<Character, String> codes = new HashMap<>();
        getCodesFromTree(root, codes, "");
        return codes;
    }

    private static void getCodesFromTree(Node root, HashMap<Character, String> codes, String str) {
        // Base case: arrive at leaf node (is a character node)
        if (root.left == null && root.right == null) {
            codes.put(root.c, str);
            return;
        }

        // Traverse left subtree
        getCodesFromTree(root.left, codes, str + "0");

        // Traverse right subtree
        getCodesFromTree(root.right, codes, str + "1");
    }
}
