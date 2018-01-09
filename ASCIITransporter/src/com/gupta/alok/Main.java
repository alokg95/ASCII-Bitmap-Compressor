package com.gupta.alok;

import java.io.File;

public class Main {

    public static ASCIITransporter createASCIITransporter(String dir){
        File file = new File(dir);
        ASCIITransporter asciiTransporter = new ASCIITransporter(file);
        return asciiTransporter;
    }

    public static void main(String[] args) {
        String fileName = "/Users/Sunny/Desktop/data.txt";
        ASCIITransporter asciiTransporter = createASCIITransporter(fileName);

        System.out.println(asciiTransporter.encode());
        System.out.println(asciiTransporter.decode());
    }
}
