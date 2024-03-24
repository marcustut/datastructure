package huffman;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

class TestCase {
    String payload, encoded;

    public TestCase(String payload, String encoded) {
        this.payload = payload;
        this.encoded = encoded;
    }
}

public class HuffmanTest {
    static List<TestCase> simpleTestCases = Arrays.asList(new TestCase(
            "aaaaabbbbbbbbbccccccccccccdddddddddddddeeeeeeeeeeeeeeeefffffffffffffffffffffffffffffffffffffffffffff",
            "11001100110011001100110111011101110111011101110111011101100100100100100100100100100100100100101101101101101101101101101101101101101111111111111111111111111111111111111111111111111000000000000000000000000000000000000000000000"),
            new TestCase(
                    "aaaaa bbbbbbbbb cccccccccccc ddddddddddddd eeeeeeeeeeeeeeee fffffffffffffffffffffffffffffffffffffffffffff",
                    "11111111111111111111111111111011101110111011101110111011101110111011110100100100100100100100100100100100100111101011011011011011011011011011011011011011111011011011011011011011011011011011011011011011011011110000000000000000000000000000000000000000000000"),
            new TestCase("oh mac donalds had a fun",
                    "101111010110101111110000110010110011111010010011001011101111100011110100010000001"));

    static List<TestCase> symbolTestCases = Arrays
            .asList(new TestCase("5U1F0b~c;E#*", "01101011010011010111110111001110000011101000"),
                    new TestCase(" 5U1F 0b~ c;E#*", "001000010010011110000101110011010001111010011010111111"));

    @Test
    void encodeSimpleString() {
        simpleTestCases.forEach(tc -> assertEquals(Huffman.encode(tc.payload).encoded, tc.encoded));
    }

    @Test
    void encodeSymbolString() {
        symbolTestCases.forEach(tc -> assertEquals(Huffman.encode(tc.payload).encoded, tc.encoded));
    }

    @Test
    void decodeSimpleString() {
        simpleTestCases.forEach(tc -> assertEquals(
                Huffman.decode(Huffman.encode(tc.payload).root, Huffman.encode(tc.payload).encoded), tc.payload));
    }

    @Test
    void decodeSymbolString() {
        symbolTestCases.forEach(tc -> assertEquals(
                Huffman.decode(Huffman.encode(tc.payload).root, Huffman.encode(tc.payload).encoded), tc.payload));
    }
}
