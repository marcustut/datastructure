package huffman;

import java.util.HashMap;

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

    static class Node implements Comparable<Node> {
        int freq;
        char c;
        Node left, right;

        public Node(int freq) {
            this.freq = freq;
        }

        public Node(char c, int freq) {
            this.c = c;
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

    public static String decode(Encoded encoded) {
        String decoded = "";
        Node curr = encoded.root;

        // Go through the encoded string and follow the path in the tree
        for (int i = 0; i < encoded.encoded.length(); i++) {
            if (encoded.encoded.charAt(i) == '0') // if it is '0' then go left
                curr = curr.left;
            else // else it is a '1', go right
                curr = curr.right;

            // Once arriving at leaf node, we found the character so we reset
            if (curr.left == null && curr.right == null) {
                decoded += curr.c;
                curr = encoded.root;
            }
        }

        return decoded;
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
