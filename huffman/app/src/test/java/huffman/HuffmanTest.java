package huffman;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

public class HuffmanTest {
    static List<String> simpleTestCases = Arrays.asList(
            "aaaaabbbbbbbbbccccccccccccdddddddddddddeeeeeeeeeeeeeeeefffffffffffffffffffffffffffffffffffffffffffff",
            "aaaaa bbbbbbbbb cccccccccccc ddddddddddddd eeeeeeeeeeeeeeee fffffffffffffffffffffffffffffffffffffffffffff",
            "oh mac donalds had a fun");

    static List<String> symbolTestCases = Arrays.asList("5U1F0b~c;E#*", " 5U1F 0b~ c;E#*");

    @Test
    void simpleString() {
        simpleTestCases
                .forEach(tc -> assertEquals(tc, Huffman.decode(Huffman.encode(tc).root,
                        Huffman.encode(tc).encoded)));
    }

    @Test
    void symbolString() {
        symbolTestCases
                .forEach(tc -> assertEquals(Huffman.decode(Huffman.encode(tc).root,
                        Huffman.encode(tc).encoded), tc));
    }
}
