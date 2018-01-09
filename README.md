# ASCII-Bitmap-Compressor
A library to compress ASCII bitmaps in lossless format to enable faster transfer of these large files over the internet. Includes encode and decode functionalities.

## Usage
1. Clone Repo to desired folder
2. Open project in Java IDE of your choice (i.e. Intellij)
3. In main, set filename to absolute path to your file, i.e. "/Users/Alok/Desktop/snoopy.txt". (Protip to find absolute path: Open terminal and drag your ascii text file into it. It'll resolve to the absolute path of the file)
4. Run the encode or decode on the `asciiTransporter` object in this main class

NOTE: For every file you want to encode/decode, you will need to create a new object of ASCIITransporter class. Doing so is simple within the code:
```
ASCIITransporter MY_TRANSPORTER_OBJECT = createASCIITransporter("PATH_TO_FILE");
```
This was done to ensure that each object has only 1 file that is keeps encoded or will decode. There are a few advantages to doing this. First, it allows us to quickly return a previously encoded file without re-encoding it if the encode command is run several times. In addition, it helps isolate the encoding of various files. Each file to be encoded will be stored in its own ASCIITransporter object. This makes it easier to test and triage potential issues. Finally, it helps ensure that no decoding will be done if a file has not been encoded yet.

Therefore, if a user runs decode before encoding a file, the program will throw an exception stating that the loaded file has not been encoded yet.

## How Encoding Works
The compression strategy used is run-length encoding. Run-length encoding is advantageous in this situation given the requirements because it exploits the nature of the ASCII bitmap files, which often have many repeating characters due to their given nature. Run-length encoding essentially calculates the number of repeats of a given character as it reads a string. The following is the format of run-length encoding and how it stores a character and its repeats: ```[NUM_REPEATS][CHAR]```.

The following are two examples of strings before and after run-length encoding:

String 1 Before:
```
aaaaaabbbbbbbbbbbbbbbdddeeeeeeeeee444
```

String 1 After:
```
6a 15b 3d 10e 34
```

String 2 Before:
```
                ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,!!!!!!!!!  (more closely relevant to the characters in ascii artwork)
```

String 2 After:
```
"16  46, 9!"
```
Note that in this string 2 example, we have encoded 16 counts of the `' '` (whitspace) character.

It uses a space delimiter to allow for distinguishing between a character and its count. If we didn't have a space delimiter, it would be challenging to distinguish between a characer and the number of repeats of that character. For example, is "20050a" 20050 repeats of the character 'a'? Making it space delimited, '200 50a' it is much easier to see that it refers to the character '0' being repeated 20 times and character 'a' being repeated 50 times. However, this space delimiting strategy makes it tricky when decoding as we need to ensure that a space is truly a delimiter and not an actual repeating character, as seen in the string 2 example above.

## Performance and Preliminary Results

The encode feature runs in `O(N)` time and `O(N)` space, where `N` is the number of characters in the fileString. We use `O(N)` space in order to store the original fileString in an array that is split up line by line (using regex). The `O(N)` time is to parse the file line by line, encoding each as we go along.

With a more involved parsing strategy, we could likely encode it in `O(N)` time and `O(1)` space as long as we are careful with parsing and avoiding edge cases (i.e. accidentally encoding a newline).

The run-length encoding works quite well on ASCII artwork since they often have many repeating characters. Using the ASCII artwork given in the requirements, we are able to compress the original `5495` characters into `3208` characters, a **42% improvement**. 

However, with artwork that is not repeating, the encoded string may potentially result in a length that is greater than the original length. The ASCIITransporter recognizes this as potential issue. To fix it, it keeps a running count of the number of characters encoded. If this running length ever becomes greater than the original length, it aborts immediately and returns the original string. With this, the worst case, upper-bound encoding perforamnce remains `O(N)`, where `N` is exactly the number of characters in original string.

## Tradeoffs and Alternatives
As mentioned earlier, the downside to run-length encoding is that it is unable to compress bitmaps that have contiguous, non-repeating characters. That is, the final encoded strings are the same exact length as the original string. Run-length encoding makes sense for the use case described in the requirements, but if we need to compress larger files and need a more involved compression strategy that may work better for non-repeating characters, [Huffman Encoding](https://en.wikipedia.org/wiki/Huffman_coding) or [Arithmetic Encoding](https://en.wikipedia.org/wiki/Arithmetic_coding) are good potential alternative approaches. Each has its own series of advantages and disadvantages which need to be evaluated based on the given use case.

## Testing
The `ASCIITransporterTest` class provides some basic unit test cases to help confirm the proper functionality of the logic of the compression class. While writing the `ASCIITransporter` class, I tried to segregate the responsibilities as much as possible to make it easier to unit test. For this reason, `encode()` and `encodeLine()` are separate functions. `Encode()` has the responsbility of creating the file split up line by line, running each line through `encodeLine()`, and then appending this compressed result to our decoded string. `encodeLine()` has the sole responsibility of taking in an input string and compressing it using run-length encoding. It serves as a wonderful black-box for the `encode()` function. This way, we can confirm the functionality of encoding a string and encoding a file of strings separately.

All testing is done in isolation. That is, when running a test, we are only relying on the expected output of 1 given function and comparing to the expected, true output that should occur. This helps when attempting to triage bugs.

The test class tests for a suite of things: null files, encoding simple strings, strings where the char to be repeated is a whitespace, strings where the number of repeats of a char is greater than 10 (to confirm proper parsing during decode), and encoding odd ascii characters. It also confirms that a string is not compressed if the original length is less than its encoded version, that decoding does not occur if a file was not encoded, and of course, the proper functionality of encode/decode with appropriate files and usage.

## Work Remaining
Given more time, the following would be the additional improvements to this program (ah, the programmer's dilemma):
- Build the command line interface to be able to use the program through command line (currently only works in IDE)
- Remove hardcoding of absolute filepath directories in both main and test suite
- Explore more robust encoding strategies, i.e. Huffman or Arithmetic
