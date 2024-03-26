package huffman.example;

import java.io.File;
import java.io.IOException;
import huffman.HuffmanFile;

public class App {
        public static void main(String[] args) throws IOException, ClassNotFoundException {
                System.out.println("compressing...");
                HuffmanFile.compress(new File(System.getProperty("user.dir") +
                                "/src/main/resources/test.txt"), new File(
                                                System.getProperty("user.dir") +
                                                                "/src/main/resources/compressed.hm"));
                System.out.println("compressed");

                System.out.println("decompressing...");
                HuffmanFile.decompress(new File(
                                System.getProperty("user.dir") +
                                                "/src/main/resources/compressed.hm"),
                                new File(System.getProperty("user.dir") +
                                                "/src/main/resources/decompressed.txt"));
                System.out.println("decompressed");
        }
}