package huffman.example;

import java.io.File;
import java.io.IOException;

import huffman.BinaryTreePrinter;
import huffman.Huffman;
import huffman.HuffmanFile;

public class App {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        HuffmanFile.compress(new File(System.getProperty("user.dir") +
                "/src/main/resources/test.txt"), new File(
                        System.getProperty("user.dir") +
                                "/src/main/resources/compressed.hm"));

        HuffmanFile.decompress(new File(
                System.getProperty("user.dir") +
                        "/src/main/resources/compressed.hm"),
                new File(System.getProperty("user.dir") +
                        "/src/main/resources/decompressed.txt"));

        // Huffman.Encoded encoded = Huffman.encode(
        // """
        // /* Malloc implementation for multiple threads without lock contention.
        // Copyright (C) 1996-2024 Free Software Foundation, Inc.
        // Copyright The GNU Toolchain Authors.
        // This file is part of the GNU C Library.

        // The GNU C Library is free software; you can redistribute it and/or
        // modify it under the terms of the GNU Lesser General Public License as
        // published by the Free Software Foundation; either version 2.1 of the
        // License, or (at your option) any later version.

        // The GNU C Library is distributed in the hope that it will be useful,
        // but WITHOUT ANY WARRANTY; without even the implied warranty of
        // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
        // Lesser General Public License for more details.

        // You should have received a copy of the GNU Lesser General Public
        // License along with the GNU C Library; see the file COPYING.LIB. If
        // not, see <https://www.gnu.org/licenses/>. */
        // """);
        // BinaryTreePrinter.print(encoded.root);
        // System.out.println("Depth: " + encoded.root.depth());
        // System.out.println(encoded);

        // System.out.println(Huffman.decode(encoded.root, encoded.encoded));
    }
}
