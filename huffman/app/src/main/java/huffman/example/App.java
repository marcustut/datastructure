package huffman.example;

import java.io.File;
import java.io.IOException;

import huffman.BinaryTreePrinter;
import huffman.Huffman;
import huffman.HuffmanFile;

public class App {
        public static void main(String[] args) throws IOException, ClassNotFoundException {
                // HuffmanFile.compress(new File(System.getProperty("user.dir") +
                // "/src/main/resources/test.txt"), new File(
                // System.getProperty("user.dir") +
                // "/src/main/resources/compressed.hm"));

                // HuffmanFile.decompress(new File(
                // System.getProperty("user.dir") +
                // "/src/main/resources/compressed.hm"),
                // new File(System.getProperty("user.dir") +
                // "/src/main/resources/decompressed.txt"));

                Huffman.Encoded encoded = Huffman.encode(
                                "yippy ya ya");
                BinaryTreePrinter.print(encoded.root);
                System.out.println("Depth: " + encoded.root.depth());
                System.out.println(encoded);

                System.out.println(Huffman.decode(encoded.root, encoded.encoded));
        }
}
