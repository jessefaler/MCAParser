package com.protoxon.mca;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import static com.protoxon.mca.io.readRegionFile.readMCAFile;

public class MCA {

    //todo use https://github.com/astei/libdeflate-java for zlib decompression
    //todo implement creating a new region from scratch
    public Region createNewRegion() {
        return null;
    }

    /**
     * Reads an MCA file
     * @see com.protoxon.mca.io.readRegionFile
     * @return a {@link Region} object for parsing the regions' data.
     * @param file a {@link File} object representing the region file to be opened.
     */
    public Region read(File file) throws FileNotFoundException {
        return new Region(new RandomAccessFile(file, "r"));
    }

    /**
     * Reads an MCA file
     * @see com.protoxon.mca.io.readRegionFile
     * @return a {@link Region} object for parsing the regions' data.
     * @param filePath a {@link String} representing the path to the region file
     */
    public Region read(String filePath) throws FileNotFoundException {
        return new Region(new RandomAccessFile(filePath, "r"));
    }
}
