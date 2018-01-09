package com.gupta.alok;


import java.io.*;
/***
 * ASCII Transporter is a library to enable lossless compression of ASCII bitmaps
 * Comes with encoding and decoding capabilities based on run length encoding
 */
public class ASCIITransporter {
    private File file;
    private String fileString;
    private String encodedString;

    // Flag used to determine if original string was shorted than encoded version
    boolean shortEncode;

    /***
     * Constructor for creating object of ASCII Transporter Class.
     * @param file: text file containing ascii bitmap
     */
    public ASCIITransporter(File file) {
        if(file == null){
            throw new IllegalArgumentException("Error: File is null");
        }

        this.file = file;
        this.fileString = parseFile(file);
        this.shortEncode = false;
    }

    /***
     * Reads in an input file and parses it line by line.
     * @param file: text file containing ascii bitmap
     * @return parsed file as a string
     */
    private String parseFile(File file){
        String line;
        StringBuilder fileString = new StringBuilder();

        // Create file reader (if possible)
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
            System.exit(1);
        }

        // Read data from input file, line by line. Append newline char after each line.
        try {
            while ((line = reader.readLine()) != null) {
                // Avoid edge cases with odd ascii art images with lines with no chars
                if(line.length() == 0){
                    continue;
                }
                fileString.append(line);
                fileString.append("\n");
            }
            reader.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        // Remove extra newline char at the end and return it
        fileString.setLength(fileString.length() - 1);
        return fileString.toString();
    }

    /***
     * Encodes (member variable) fileString line by line
     * @return encoded filestring
     */
    public String encode(){
        // If fileString object is null we cannot encode it
        if(this.fileString == null){
            throw new IllegalStateException("fileString is null");
        }

        // If we have previously already encoded a string, return it immediately
        if(this.encodedString != null){
            return this.encodedString;
        }

        // Initialize variable to keep track of length of current length of the encoded string
        int encodedLen = 0;
        int originalLen = this.fileString.length();

        // Split original file, line by line via regex
        String[] fileLines = fileString.split("\\n");

        // Use StringBuilder to hold each encoded line as it is mutable in comparison to strings
        StringBuilder encodedFileString = new StringBuilder();

        for(String line: fileLines){

            // For each line: encode the line, append a newline char at the end of the line, and append to StringBuilder
            String encodedLine = encodeLine(line);
            encodedFileString.append(encodedLine);
            encodedFileString.append("\n");

            // If encoded string length becomes greater than original length at any point, escape further computation
            // and return original string
            encodedLen = encodedLen + encodedLine.length();
            if(encodedLen > originalLen) {
                encodedFileString = new StringBuilder(this.fileString + '\n');
                this.shortEncode = true;
                break;
            }
        }

        // Remove extra newline character at the end of the encoded string and return it
        encodedFileString.setLength(encodedFileString.length() - 1);
        this.encodedString = encodedFileString.toString();
        return this.encodedString;
    }

    /***
     * Encodes a string using run-length encoding
     * @return encoded string
     */
    public String encodeLine(String line){
        // If a line string object is null, throw an exception
        if(line == null){
            throw new IllegalStateException("line cannot be null");
        }

        // Return an empty string if the line is empty
        if(line.length() == 0){
            return "";
        }

        // Use StringBuilder to store the encoded line since it is mutable, in contrast to strings
        StringBuffer encodedLine = new StringBuffer();

        for (int i = 0; i < line.length(); i++) {
            // Initialize run length for a character to 1 (we must have atleast 1 of each char)
            int runLength = 1;

            // Calculate run length for this character
            while (i+1 < line.length() && line.charAt(i) == line.charAt(i+1)) {
                runLength++;
                i++;
            }

            // Append the runlength and the character itself
            encodedLine.append(runLength);
            encodedLine.append(line.charAt(i));

            // Append a space delimiter to allow us to distingish between different runlengths and characters while parsing later
            encodedLine.append(" ");
        }

        // Remove extra space character and return this encoded line string
        encodedLine.setLength(encodedLine.length() - 1);
        return encodedLine.toString();
    }

    /***
     * Decodes the encoded filestring
     * @return decoded filestring
     */
    public String decode() {
        if(this.encodedString == null){
            throw new IllegalStateException("Encoded string cannot be null. Please encode a string before attempting to decode.");
        }

        // If the encoded string is the original string since it was shorter than the compressed version, return it
        if(shortEncode){
            return this.encodedString;
        }

        String encodedStr = this.encodedString;

        // Use StringBuilder to append decoded strings since it is mutable
        StringBuilder decodedString = new StringBuilder();

        // Initialize our index and nextIndex
        // Index will refer to the current pair of runLength and character
        // nextIndex will refer to the next pair of runLength and character
        // This will later help with parsing in order to
        // distinguish between the runLength and its character which can be tricky when runLength > 9
        // i.e. '200' means run length of 20 for the character '0'
        int index = 0;
        int nextIndex = index;

        // Ensure that our index is always within range of encoded string
        while(index < encodedStr.length()){

            // Find the next pair of run length and character, if possible, by looping until the char is not a blank space
            while(nextIndex < encodedStr.length() && encodedStr.charAt(nextIndex) != ' '){

                // If we ever find a newline character, break so we can append it to decoded string
                if(encodedStr.charAt(nextIndex) == '\n') {
                    break;
                }

                // Increment nextIndex as we attempt to find the next pair of run length and character
                nextIndex++;
            }

            // After breaking out of loop, nextIndex is an empty space
            // It is either our delimiter or the empty char that we need to include
            // If nextIndex is out of bounds. It cannot be a space. Else, check if the neighboring char is potentially
            // the pair delimiting space
            boolean isSpace = nextIndex >= encodedStr.length()-1 ? false : encodedStr.charAt(nextIndex + 1) == ' ';

            // Determine the index of the character that we need to append into our decoded string
            int charIndex = 0;
            if(nextIndex < encodedStr.length() && encodedStr.charAt(nextIndex) == '\n'){

                // If current char is a newline, then the character is surely at the prior index
                charIndex = nextIndex - 1;
            } else if(isSpace){

                // If isSpace is true, the character is the space itself
                charIndex = nextIndex;
            } else {

                // nextIndex refers to the whitespace that serves as a delimiter between a pair of run length and char
                // Our char is simply at the previous index
                charIndex = nextIndex - 1;
            }
//            System.out.println("next = " + nextIndex + "charIdx = " + charIndex);
            // This is the character we need to include in our decoded string based on our previously calculated charIndex
            char ch = encodedStr.charAt(charIndex);

            // Check for valid indices at the beginning to handle edge cases that happen with differently encoded ascii bitmaps
            int numChar = index - charIndex >= 0 ? 0 : Integer.valueOf(encodedStr.substring(index, charIndex)); // first part is edge cases

            // Based on the run length and char we parsed, append it to our decoded string
            for(int n = 1; n <= numChar; n++){
                decodedString.append(ch);
            }

            // If nextIndex is within bounds and it's a newline, append it
            if(nextIndex < encodedStr.length() && encodedStr.charAt(nextIndex) == '\n') decodedString.append('\n');

            // Update index to be the next pair of run length and character
            index = isSpace ? nextIndex + 2 : nextIndex + 1; // inc index to our next pair of (numChar)(char)

            // Update nextIndex so it trails index for the next iteration
            nextIndex = index;

        }

        return decodedString.toString();
    }


}
