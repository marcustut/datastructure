package huffman;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import huffman.Huffman;

public class Huffman {
    /**
     * Encoded is a wrapper class to represent the result of `Huffman.encode`.
     */
    public static class Encoded {
        public byte[] encoded;
        public HashMap<Character, String> codes;
        public Node root;

        public Encoded(byte[] encoded, HashMap<Character, String> codes, Node root) {
            this.encoded = encoded;
            this.codes = codes;
            this.root = root;
        }

        private String formatEncoded() {
            String s = "[";
            for (byte b : encoded)
                s += Integer.toBinaryString(b & 0xFF) + " ";
            return s.substring(0, s.length() - 1) + "]";
        }

        @Override
        public String toString() {
            return "{encoded=" + this.formatEncoded() + ", codes=" + codes + "}";
        }
    }

    /**
     * Node represents the node in a Huffman tree.
     * 
     * `Comparable` - allows to compare two different node and determine which is
     * larger / smaller.
     * `PrintableNode` - allows the tree to be printed using
     * `BinaryTreePrinter.print()`.
     */
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

        /**
         * Find the depth of the tree.
         * 
         * @return an integer denoting the depth.
         */
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

    /**
     * Encode the payload using Huffman Code.
     * 
     * @param payload - the payload string
     * @return the result of encoding in a wrapper class `Encoded`.
     */
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
        ArrayList<Byte> bytes = new ArrayList<Byte>();
        byte _byte = 0;
        int biti = 7;

        // Iterate through each character of the payload, get its corresponding code
        // and convert them into actual bytes
        for (char c : payload.toCharArray()) {
            String path = codes.get(c);

            for (int i = 0; i < path.length(); i++) {
                if (biti < 0) { // push and reset once we finished writing 8 bits
                    bytes.add(_byte);
                    _byte = 0;
                    biti = 7;
                }

                int turn = path.charAt(i) - '0'; // convert char from ascii to integer

                _byte |= turn << biti;
                biti--;
            }
        }

        /**
         * Use the last two bytes to indicate EOF.
         * For example, the last remaining byte is 10110110 and biti = 0
         * 
         * <pre>
         *   Before adding the size:
         *     lastByte1 = 10110000 
         *     lastByte2 = 01100000
         * 
         *   After adding the size:
         *     lastByte1 = 10110100 (size of 4 = min(4, 7 - 0)) 
         *     lastByte2 = 01100011 (size of 3 = max(0, 7 - 0 - 4))
         * </pre>
         */

        // Use two last byte to indicate EOF
        byte lastByte1 = (byte) (_byte & 0b11110000); // take the first 4 bits and make it the second last
        byte lastByte2 = (byte) ((_byte & 0b00001111) << 4); // take the last 4 bits and shift to front

        // Add the size to read at last 3 bits
        lastByte1 |= Math.min(7 - biti, 4);
        lastByte2 |= Math.max(7 - biti - 4, 0);

        bytes.add(lastByte1);
        bytes.add(lastByte2);

        // Convert to Java's primitive byte[] array
        byte[] primitiveBytes = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++)
            primitiveBytes[i] = bytes.get(i).byteValue();

        return new Encoded(primitiveBytes, codes, root);
    }

    /**
     * Decode a previously encoded payload given the Huffman tree used.
     * 
     * @param root    - The root node of the Huffman tree.
     * @param encoded - The encoded raw bytes.
     * @return a decoded string of the payload
     */
    public static String decode(Huffman.Node root, byte[] encoded) {
        String decoded = "";
        Node curr = root;

        /**
         * For each byte, we test it bit by bit and traverse the tree.
         * Note that we always compare the most significant bit (MSB),
         * hence why we do an "AND" mask for the first bit.
         * 
         * For example, to iterate byte -> 01001101
         * 
         * <pre>
         *   i = 7:
         *      b = 01001101
         *      b & 0b10000000 = 01001101 & 1000000 = 0 (go left)
         *   i = 6:
         *      b = 10011010
         *      b & 0b10000000 = 10011010 & 1000000 = 128 (go right)
         *   i = 5:
         *      b = 00110100
         *      b & 0b10000000 = 00110100 & 1000000 = 0 (go left)
         *   ...
         * </pre>
         */
        for (int i = 0; i < encoded.length - 2; i++) {
            byte b = encoded[i];
            int biti = 7;
            while (biti >= 0) {
                if ((b & 0b10000000) > 0) // if bit is set, go right
                    curr = curr.right;
                else // go left
                    curr = curr.left;

                if (curr.left == null & curr.right == null) { // arriving at leaf node
                    decoded += curr.c; // add the character found
                    curr = root; // reset and start again
                }

                b <<= 1;
                biti--;
            }
        }

        // Handle the last two bytes (EOF)
        for (int i = encoded.length - 2; i < encoded.length; i++) {
            byte b = encoded[i];
            int nbits = b & 0b00000111; // take the last 3 bits as size

            // Traverse the bits
            while (nbits > 0) {
                if ((b & 0b10000000) > 0)
                    curr = curr.right;
                else
                    curr = curr.left;

                if (curr.left == null && curr.right == null) {
                    decoded += curr.c;
                    curr = root;
                }

                b <<= 1;
                nbits--;
            }
        }

        return decoded;
    }

    /**
     * Serialize a given Huffman tree so that it can be stored elsewhere.
     * 
     * @param root - The root node of the Huffman tree.
     * @return a string which contains the serialized tree.
     */
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

    /**
     * Deserialize a serialized tree from the given string.
     * 
     * @param serialized - The serialized tree in string.
     * @return the root node of the tree.
     */
    public static Huffman.Node deserializeTree(String serialized) {
        if (serialized == null)
            throw new NullPointerException();

        // Collect the serialized string into aray of characters
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

    private static HashMap<Character, String> getCodesFromTree(Node root) {
        HashMap<Character, String> codes = new HashMap<>();
        getCodesFromTree(root, codes, new String(""));
        return codes;
    }

    private static void getCodesFromTree(Node root, HashMap<Character, String> codes, String code) {
        // Base case: arrive at leaf node (is a character node)
        if (root.left == null && root.right == null) {
            codes.put(root.c, code);
            return;
        }

        // Traverse left subtree
        getCodesFromTree(root.left, codes, code + "0");

        // Traverse right subtree
        getCodesFromTree(root.right, codes, code + "1");
    }
}
