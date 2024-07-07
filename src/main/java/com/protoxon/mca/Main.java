package com.protoxon.mca;

import java.io.*;
import java.util.Arrays;
import java.util.zip.DataFormatException;

public class Main {

    //A class for testing
    public static void main(String[] args) throws IOException, DataFormatException {
        Region region = new Region("regionFiles/1.21/r.0.0.mca");
        System.out.println(region.getChunk(0 ,0).DataVersion());
        //System.out.println(region.getChunk(0, 0).getBlocks());
    }
}