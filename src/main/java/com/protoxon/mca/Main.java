package com.protoxon.mca;

import java.io.*;
import java.util.Arrays;
import java.util.zip.DataFormatException;

public class Main {

    //A class for testing
    public static void main(String[] args) throws IOException, DataFormatException {
        File file = new File("regionFiles/1.21/r.0.0.mca");

        Region region = new Region(file);

        region.chunks();
        System.out.println(region.Chunk(0,0).DataVersion());
    }
}