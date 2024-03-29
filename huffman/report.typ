#import "template.typ": *

#show: project.with(
  title: "Assignment 3: Huffman Code",
  authors: (
    (name: "Lee Kai Yang\n(23205838 - 100%)", email: "kai.y.lee@ucdconnect.ie"),
  ),
  date: "March 23, 2024",
  font: "CMU Serif",
  monofont: "CMU Typewriter Text",
)

#align(center)[
  #text(fill: blue, size: 9pt)[#underline(link("https://youtu.be/Tw7ODIysiX4")[🔗 Video Link])]
  #h(0.2em)
  #text(fill: blue, size: 9pt)[#underline(link("https://github.com/marcustut/datastructure/tree/main/huffman")[🔗 GitHub Link])]
]

#pad(x: 12pt, [
  *Abstract* - This document explores the implementation and implications of Huffman coding, a widely used data compression algorithm. Beginning with an overview of early computing limitations and the need for efficient storage techniques, it delves into the fundamentals of Huffman encoding, highlighting its role in reducing data size by exploiting character frequency patterns. The process of constructing Huffman trees and encoding messages is detailed, showcasing the algorithm's ability to achieve significant compression compared to ASCII encoding. Key design decisions, including tree serialization, byte array encoding, and handling end-of-file scenarios, are elucidated for effective implementation. Test cases and examples demonstrate the functionality and efficacy of the Huffman coding implementation. Through this exploration, valuable insights into information theory and bitwise operations are gained, culminating in a comprehensive understanding of Huffman coding's principles and applications.
])

= Background

Back then in the early ages of computing resources are limited, for example the 305 RAMAC (Random Access Method of Accounting and Control) launched by IBM in September 1956 is capable of only storing 5MB (megabytes) of data albeit weighting over 2000 pounds and cost 35,000 USD a year to operate. In contrast, a commerical memory card that weights no more than 5 gram can easily stores 1TB (terabytes) of data, in fact storage is so affordable that most people do not think twice on what they are storing e.g. images, music, and the all-important funny cat videos. However, the techniques to optimise storage space back then had since lived on until now, one of the most well known technique is data compression where given a larger file, it will be compressed by taking advantage of repetitive patterns in the contents, allowing it to use lesser space while having the ability to reconstruct the original data later. Such technique is also useful for speeding up communication across networks since it reduces the payload size. Huffman encoding is one of the most commonly known algorithm in this regard where it was being used in ZIP, GZIP as well as image compression such as JPEG and PNG, created by David Huffman in 1952 at MIT.

= Introduction

== Message Encoding

Assuming that we are only dealing with ASCII characters, each character maps to 1 byte so for the message *"yippy ya ya"*, it would need $11 times 8" bits" = 88 "bits"$ in total. The table below shows the mapping of each character to their corresponding ASCII values.

#figure(
  table(
    columns: (auto, auto, auto),
    inset: 6pt,
    align: horizon,
    fill: (col, row) => if row == 0 { silver } else { white },
    [*Character*], [*ASCII (Decimal)*], [*ASCII (Binary)*],
    [y], [121], [01111001],
    [i], [105], [01101001],
    [p], [112], [01110000],
    [a], [97], [01100001],
    [space], [32], [00100000],
  ),
  caption: [ASCII Table for the message "yippy ya ya"]
) <ascii_yippy_ya_ya>

Using @ascii_yippy_ya_ya above, the message encoded using ASCII would be 

#rect(fill: silver, stroke: 1pt, width: 100%, height: 3.4em, inset: 6.5pt)[
  #text(font: "CMU Typewriter Text", size: 10pt, weight: "bold")[01111001 01111001 01110000 01110000 01111001 01111001 01111001 01111001 00100000 01111001 01100001]
]

However do we really need that many bits? With huffman encoding, it leverages the frequency information of individual character in the message to devise a more compact encoding scheme. For example, the same message would use the following scheme:

#figure(
  table(
    columns: (auto, auto),
    inset: 6pt,
    align: horizon,
    fill: (col, row) => if row == 0 { silver } else { white },
    [*Character*], [*ASCII (Binary)*],
    [y], [0],
    [i], [100],
    [p], [110],
    [a], [111],
    [space], [101],
  ),
  caption: [Huffman Encoding Table for the message "yippy ya ya"]
) <huffman_yippy_ya_ya>

Using @huffman_yippy_ya_ya above, the encoded message would be 

#rect(fill: silver, stroke: 1pt, width: 100%, height: 2em, inset: 6.5pt)[
  #text(font: "CMU Typewriter Text", size: 10pt, weight: "bold")[0 100 110 110 0 101 0 111 0 111]
]

which only uses 22 bits, comparing this to the ASCII version, it achieved a 75% compression. However, most operating systems's file system work with byte as the smallest unit hence we need to pad extra zeroes at the end for the message making it to use 24 bits. That said, it is still a big improvement from only using ASCII.

== Constructing the Huffman Table

The major difference between Huffman encoding and ASCII encoding is that ASCII has a fixed table mapping each character to a bit pattern but Huffman encoding does not, it requires some processing to generate this table. The approach that Huffman took is to construct a binary tree that we can traverse to find the character and the path it took is the code for that particular character. Such trees are called _Huffman Trees_.

For example the message *"yippy ya ya"* would result in a huffman tree as follows:

```
                     0                       {=11}             1                                     
                    ┌───────────────────┴───────────┐                   
                  {y=4}                             0         {=7}          1
                                                    ┌─────────┴───────┐         
                                             0    {=3}    1           0    {=4}   1      
                                             ┌────┴────┐         ┌────┴────┐    
                                           {i=1}         { =2}     {p=2}         {a=2}
```

Note that in the message, there are five distinct characters and these characters correspond to the five leaf nodes in the tree, *y*, *i*, *space*, *p* and *a*. When traversing the tree, every turn to the left adds a '0' to the path and adds a '1' to the path when turning to the right. Therefore, for the character *y*, its path is just *0* since its only takes one turn to the left and arrived at the leaf node, for the character *i*, the path is *100*, the same idea applies for the other characters.

First, we have to construct this tree from the original message using a greedy algorithm, the algorithm is as follows:

1. Run through the message character by character and record the frequency of each character in a map.
2. Convert all these character-frequency pairing into nodes and store them in a min-heap.
3. Poll the heap twice to get the minimum and next minimum node
4. Aggregate these two nodes into one by taking the sum of their frequencies, the character does not matter.
5. Add the aggregated node back into the heap.
6. Repeat step 3 - 5 until there is only one node left in the heap.
7. The root node of the tree is the remaining node in the heap.

The algorithm described above has been implemented in the `buildTree` method of the `Huffman` class. 

== Decoding using the Huffman Tree

During the decode process, the tree that was used to encode the original message will also be needed in order to decode the encoded message. The idea is very simple, if we have a tree such as the one depicted above, then the encoded message represents the path to take in the tree. The decoding algorithm starts at the root and if the current bit is `0` then traverse the left subtree and if `0`, traverse the right subtree, until we arrive a leaf node where we get the first decoded character, then we start from root node again and keep doing this until the end of the message.

= Design decisions

== Classes overview

The classes in the project are outlined as follows:

- *Huffman* - implements the core operations and algorithm described above.
  - `encode` 
  - `decode`
  - `serializeTree`
  - `deserializeTree`
- *HuffmanFile* - a wrapper for `Huffman` to deal with files.
  - `compress`
  - `decompress`
- *MinHeap* - the data structure used by `Huffman` to construct the Huffman Tree.
  - `add`
  - `poll`
  - `size`

== Tree Serialization and Deserialization

The `serializeTree` and `deserializeTree` method here are used to transform the Huffman Tree into string and back to a tree from string. This is essential since the tree is required for the decoding process hence in the `compress` method of `HuffmanFile` we store the serialized tree together with the encoded message. Similarly, the `decompress` method of `HuffmanFile` will read the compressed file and deserialize the file header as a Huffman Tree in order to use it for decoding.

To give an illustration of the serialization and deserialization process, let's serialize the following tree as a string:

```
                     0                       {=11}             1                                     
                    ┌───────────────────┴───────────┐                   
                  {y=4}                             0         {=7}          1
                                                    ┌─────────┴───────┐         
                                             0    {=3}    1           0    {=4}   1      
                                             ┌────┴────┐         ┌────┴────┐    
                                           {i=1}         { =2}     {p=2}         {a=2}
```

For this given tree, the serialized tree would be a string of *"01y001i1 01p1a"*. The serialized tree is in fact just a pre-order traversal of the tree where it follows the sequence of root $->$ left $->$ right. If the current node is a leaf node, then it appends "1" + "\<character>" onto the string, otherwise it appends "0". To reconstruct the tree from this string, it is just the reverse process of the above where it traverse through the string and construct the tree following its pre-order traversal.

== Encoding as byte array

To optimise the storage usage for the encoding and decoding process, instead of storing these zero and one codes as string we store them as an array of `byte` instead.  By doing so we save a lot of space because when we store a "0" as a character it uses 1 byte (8 bits) but in fact we only need a bit to represent this so by using a byte array, we can store 8 codes in a byte instead of 1 code in a byte. So for the encoded message above: 

#rect(fill: silver, stroke: 1pt, width: 100%, height: 2em, inset: 6.5pt)[
  #text(font: "CMU Typewriter Text", size: 10pt, weight: "bold")[0 100 110 110 0 101 0 111 0 111]
]

if we remove the spacing then it would be *0100110110010101110111*, storing this as a string would need 22 bytes since there are 22 individual charatcters but if we store them as a byte array, it would take only 3 bytes where it looks like

#rect(fill: silver, stroke: 1pt, width: 100%, height: 2em, inset: 6.5pt)[
  #text(font: "CMU Typewriter Text", size: 10pt, weight: "bold")[01001101, 10010101, 11011100]
]

Note that there are two extra zeroes at the last byte for padding since every byte is 8 bits long. The example of byte array above is incomplete because when using byte array we need to implement our own "End of file" mechanism which will be discussed later. However by using byte arrays, it also forces us to use bitwise operations in code since we deal directly with bits now hence why in `src/main/java/huffman/Huffman.java` you will see a lot of bitwise operations being used.

== Arbitrary End-of-File

When we deal with strings, we do not have to think of annotating the end of the message since every character is a byte and the last code will always be the last character of the string and you can know that it is the end of the string by checking its length. However for a byte array we do not know how many zeroes was padded after the actual ending of the code. Take the example byte array given above where two extra zeroes are padded at the last byte, during decode how could one possibly know that the last two zeroes are padding or they are part of the encoding?

My solution is to treat the last two bytes as special bytes and we can store the padding information in the last two bytes. For example given the encoding of *0100110110010101110111*, we would produce a byte array of 

#rect(fill: silver, stroke: 1pt, width: 100%, height: 2em, inset: 6.5pt)[
  #text(font: "CMU Typewriter Text", size: 10pt, weight: "bold")[01001101, 10010101, 11010100, 11000010]
]

Note that for the last bytes we use a special format where the first four bits store the actual encoding and the last four bits are used to store how many bits are used. For example,

```
11010100 -> 1101 (the actual code)
         -> 0100 (represents 4 in binary indicating four bits are used)

11000010 -> 1100 (the actual code)
         -> 0010 (represents 2 in binary indicating two bits are used)
```

Hence in the decoding process, we would first look at the last four bits and determine the size and only read the number of bits in the first four bits up to size.

= Test cases

The test cases for the aforementioned classes can be found in `src/test/java/huffman` and can be ran by executing `./gradlew test` in the terminal.

= Examples

There is only one example in this project where it can be ran by running `./gradlew run` in the terminal. What the example program will do is that it will compress the file `src/main/resources/test.txt` into `src/main/resources/compressed.hm` then decompress it into `src/main/resources/decompressed.txt`. To test it with your own files, just change the contents in the `text.txt` file and re-run the program again.

In the example, I used the C source code of GNU's malloc implementation which is 6049 lines. After running the program, this is the result of `ls -la`:

```sh
.rw-r--r--  121k marcus 26 Mar 17:00 compressed.hm
.rw-r--r--@ 197k marcus 26 Mar 17:00 decompressed.txt
.rw-r--r--@ 197k marcus 26 Mar 16:35 test.txt
```

As can be seen, in this case the program compressed the original file by 38.5%.

= Conclusion

In conclusion, I had learnt a lot through out this assignment both in programming using bitwise operations and the fundamentals of information theory. One of the biggest challenge which turns out to be an achievement is that I was able to implement the encoding to byte arrays, I was stuck for days but eventually figured it out through multiplle trials and errors. 

As an overall, I am satisfied with what this project had achieved despite it only supports compression and decompression for text files (the example program does not work for media files since those files are binary files).
