package huffman;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class HuffmanFile {
    public static void compress(File source, File destination) throws IOException {
        // Read the source file into one single string
        String input = String.join("\n", Files.readAllLines(source.toPath()));

        // Encode the contents
        Huffman.Encoded encoded = Huffman.encode(input);

        // Write the header and encoded contents
        byte[] header = (Huffman.serializeTree(encoded.root) + "\n").getBytes();
        byte[] data = new byte[header.length + encoded.encoded.length];
        System.arraycopy(header, 0, data, 0, header.length);
        System.arraycopy(encoded.encoded, 0, data, header.length, encoded.encoded.length);
        Files.write(destination.toPath(), data);
    }

    public static void decompress(File source, File destination) throws IOException {
        // Read file as raw bytes from source
        byte[] data = Files.readAllBytes(source.toPath());

        // Find the first new line (separates header and encoded content)
        int spliti = 0;
        for (int i = 0; i < data.length; i++)
            if (data[i] == '\n') {
                spliti = i;
                break;
            }

        // Split the data into header and encoded, note that we offset the encoded by 1
        // to not include the separator
        byte[] header = new byte[spliti];
        byte[] encoded = new byte[data.length - spliti - 1];
        System.arraycopy(data, 0, header, 0, spliti);
        System.arraycopy(data, spliti + 1, encoded, 0, data.length - spliti - 1);

        // Deserialize the header as a huffman tree
        Huffman.Node root = Huffman.deserializeTree(new String(header));

        // Decode the contents using the tree
        String decoded = Huffman.decode(root, encoded);

        /**
         * Write the decoded contents into destination. Note that we had to write the
         * last line with APPEND mode to avoid adding an extra new line.
         * 
         * @see: <https://stackoverflow.com/questions/43961095/avoid-extra-new-line-writing-on-txt-file>
         */
        ArrayList<String> lines = new ArrayList<>(Arrays.asList(decoded.split("\n")));
        String lastLine = lines.removeLast();
        Files.write(destination.toPath(), lines);
        Files.write(destination.toPath(), lastLine.getBytes(), StandardOpenOption.APPEND);
    }
}
