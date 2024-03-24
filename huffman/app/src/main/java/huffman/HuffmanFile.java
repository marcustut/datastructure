package huffman;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class HuffmanFile {
    public static void compress(File source, File destination) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(source));

        String input = "";
        String line = reader.readLine();

        while (line != null) {
            input += line + "\n";
            line = reader.readLine();
        }

        Huffman.Encoded encoded = Huffman.encode(input);
        BufferedWriter writer = new BufferedWriter(new FileWriter(destination));

        writer.write(Huffman.serializeTree(encoded.root));
        writer.write("\n");
        writer.write(encoded.encoded);

        reader.close();
        writer.close();
    }

    public static void decompress(File source, File destination) throws IOException, ClassNotFoundException {
        BufferedReader reader = new BufferedReader(new FileReader(source));

        Huffman.Node root = Huffman.deserializeTree(reader.readLine());
        String decoded = Huffman.decode(root, reader.readLine());

        BufferedWriter writer = new BufferedWriter(new FileWriter(destination));
        writer.write(decoded);

        reader.close();
        writer.close();
    }
}
