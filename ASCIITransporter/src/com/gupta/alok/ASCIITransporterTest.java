package com.gupta.alok;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.Assert.*;

public class ASCIITransporterTest {

    public ASCIITransporter createTransporterObject(String dir){
        File file = new File(dir);
        ASCIITransporter asciiTransporter = new ASCIITransporter(file);
        return asciiTransporter;
    }

    // TODO: Change code refer to data.txt dynamically in current directory instead of hardcoding directory

    @Test(expected=IllegalArgumentException.class)
    public void  testNullFileEncode() throws Exception {
        // Throw exception when file is null
        ASCIITransporter asciiTransporter = new ASCIITransporter(null);
    }

    @Test
    public void testValidEncodeLine() throws Exception {

        // Setup file and class objects
        String fileName = "/Users/Sunny/Desktop/data.txt";
        ASCIITransporter asciiTransporter = createTransporterObject(fileName);

        // Encode simple string
        assertEquals("1a 1b 1c", asciiTransporter.encodeLine("abc"));

        // Encode string with empty spaces
        assertEquals("5a 2b 1c 2d 2 ", asciiTransporter.encodeLine("aaaaabbcdd  "));

        // Encode string with run length >= 10 to ensure proper parsing
        assertEquals("1i 18b 1n", asciiTransporter.encodeLine("ibbbbbbbbbbbbbbbbbbn"));

        // Encode with odd ascii characters
        assertEquals("2k 3e 4* 4`", asciiTransporter.encodeLine("kkeee****````"));
    }

    @Test
    public void testNoEncode() throws Exception {
        // Encoded string of ascii that is larger when used run length compression is equivalent to the original string
        // Encoding is aborted, returning the original string
        String fileName = "/Users/Sunny/Desktop/dragon.txt";
        ASCIITransporter asciiTransporter = createTransporterObject(fileName);
        asciiTransporter.encode();
        assertEquals(866, asciiTransporter.decode().length());
    }

    @Test
    public void testValidEncode() throws Exception {
        // Encoded string of ascii that has many repeating characters is smaller than the original filestring
        String fileName = "/Users/Sunny/Desktop/data.txt";
        ASCIITransporter asciiTransporter = createTransporterObject(fileName);
        assertTrue(asciiTransporter.encode().length() < 5495);

        // Encoded string of ascii that has many repeating characters is smaller than the original filestring
        fileName = "/Users/Sunny/Desktop/ferrari.txt";
        asciiTransporter = createTransporterObject(fileName);
        assertTrue(asciiTransporter.encode().length() < 42520);
    }

    @Test
    public void testNoDecode() throws Exception {

        // Ensure no decoding is done when the original ascii was not compressed (original length was shorter)
        String fileName = "/Users/Sunny/Desktop/dragon.txt";
        ASCIITransporter asciiTransporter = createTransporterObject(fileName);
        // encode is required before decoding
        asciiTransporter.encode();
        assertEquals(866, asciiTransporter.decode().length());
    }

    @Test(expected = IllegalStateException.class)
    public void testNoDecodeBeforeEncoding() throws Exception {
        // Ensure that no decoding is done if a file is not encoded
        String fileName = "/Users/Sunny/Desktop/dragon.txt";
        ASCIITransporter asciiTransporter = createTransporterObject(fileName);
        asciiTransporter.decode();
    }

    @Test
    public void testDecode() throws Exception {
        // Encoded string of ascii that has many repeating characters is smaller than the original filestring
        String fileName = "/Users/Sunny/Desktop/data.txt";
        ASCIITransporter asciiTransporter = createTransporterObject(fileName);
        asciiTransporter.encode();
        assertEquals(5495, asciiTransporter.decode().length());

        // Encoded string of ascii that has many repeating characters is smaller than the original filestring
        fileName = "/Users/Sunny/Desktop/ferrari.txt";
        asciiTransporter = createTransporterObject(fileName);
        asciiTransporter.encode();
        assertTrue(asciiTransporter.decode().length() == 42520);
    }

}