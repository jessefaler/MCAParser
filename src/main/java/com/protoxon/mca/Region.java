package com.protoxon.mca;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/*
 * see "https://minecraft.fandom.com/wiki/Region_file_format" for more info
 */

public class Region {

    byte[] region;

    HashMap<Pos, Chunk> CHUNKS = new HashMap<>(1024, 1);

    Region(File region) throws IOException {
        this.region = read(region);
        loadChunks();
    }

    Region(String region) throws IOException {
        this.region = read(region);
        loadChunks();
    }

    /**
     * Reads the contents of a region file and converts it to a byte array.
     * @param file File object representing the region file
     * @return byte[] The contents of the file as a byte array
     * @throws IOException If an I/O error occurs reading from the file
     */
    private byte[] read(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }

    /**
     * Reads the contents of a region file and converts it to a byte array.
     * @param file Path to the region file as a string
     * @return byte[] The contents of the file as a byte array
     * @throws IOException If an I/O error occurs reading from the file
     */
    private byte[] read(String file) throws IOException {
        return Files.readAllBytes(Paths.get(file));
    }

    /**
     * <pre>
     * Gets the offsets of the chunks in the region file
     * offsets: (1024 entries; 4 bytes each) 3 bytes for the offset and one byte for the sector count
     * The Offsets indicate the sector that the chunks data is stored in each sector is 4096 bytes
     * There are 1024 entries because a region file has 32x32 chunks (32 * 32 = 1024)
     * <p>
     * Range: 0x00 - 0x0FFF (0 - 4095) which covers 4096 bytes
     * This range covers the first 4096 bytes of the region file (1024 entries * 4 bytes each = 4096 bytes)
     *
     * @return int[] offsets (in bytes), an array of integers representing the offsets of the chunks in the region file
     */
    public int[] offsets() {
        int[] offsets = new int[1024]; //create an array of 1024 integers because there are 1024 offset entries
        int index = 0;
        int offset;

        for (int i = 0; i < 4096; i += 4) {
            // Extract the next 4 bytes from the region array and convert them to an integer.

            // Since the offset is a 32-bit (4-byte) integer, we use bitwise operation OR (|) to combine the bytes into the integer.
            // Masking with (& 0xFF) ensures each byte is treated as unsigned, preventing sign extension.
            // Shift the first byte left by 24 bits to position it in the most significant byte of the integer.
            offset = 0;

            offset |= (region[i] & 0xFF) << 16;   //0000 0000 0000 0000 <- 0000 0000 0000 0000
            offset |= (region[i + 1] & 0xFF) << 8;//0000 0000 0000 0000 0000 0000 <- 0000 0000      (<-) points to the current byte
            offset |= (region[i + 2] & 0xFF);     //0000 0000 0000 0000 0000 0000 0000 0000 <-      each byte is 8 bits (0000 0000)

            offsets[index] = offset * 4096; //multiply by 4096 to get the offset in bytes (each chunk section is made up of 4096 bytes)
            index++;
        }
        return offsets;
    }

    /**
     * <pre>
     * Gets the timestamps of the chunks in the region file
     * timestamps: (1024 entries; 4 bytes each)
     * The timestamps are four-byte big-endian integers, representing the last modification time of a chunk in epoch seconds
     * There are 1024 entries because a region file has 32x32 chunks (32 * 32 = 1024)
     *
     * Range: 0x1000 - 0x1FFF (4096 - 8191) which covers 4096 bytes
     * This range covers the second 4096 bytes of the region file (1024 entries * 4 bytes each = 4096 bytes)
     *
     * @return int[] timestamps, an array of integers representing the timestamps of the chunks in the region file
     */
    public int[] timestamps() {
        int[] timestamps = new int[1024]; //create an array of 1024 integers because there are 1024 timestamps entries
        int index = 0;
        int timestamp;

        for (int i = 4096; i < 8192; i += 4) {//starts at byte 4096 and ends at byte 8191 (1024 iterations)
            // Extract the next 4 bytes from the region array and convert them to an integer.

            // Since the timestamp is a 32-bit (4-byte) integer, we use bitwise operation OR (|) to combine the bytes into the integer.
            // Masking with (& 0xFF) ensures each byte is treated as unsigned, preventing sign extension.
            // Shift the first byte left by 24 bits to position it in the most significant byte of the integer.
            timestamp = 0;
            timestamp |= (region[i] & 0xFF) << 24;    //0000 0000 <- 0000 0000 0000 0000 0000 0000
            timestamp |= (region[i + 1] & 0xFF) << 16;//0000 0000 0000 0000 <- 0000 0000 0000 0000      (<-) points to the current byte
            timestamp |= (region[i + 2] & 0xFF) << 8; //0000 0000 0000 0000 0000 0000 <- 0000 0000      each byte is 8 bits (0000 0000)
            timestamp |= (region[i + 3] & 0xFF);      //0000 0000 0000 0000 0000 0000 0000 0000 <-

            timestamps[index] = timestamp;
            index++;
        }
        return timestamps;
    }


    /* Gets all the chunks in the region using the offsets, creates chunk objects and populates the CHUNKS HashMap
     *
     * Range: 0x2000... (8192...) covers all remaining bytes after 8191
     * includes chunks and unused space
     */
    private void loadChunks() throws IOException {
        int length;
        int compressionType;
        byte[] compressedData;
        for(int offset : offsets()) {

            //get the number of bytes the chunks compressed data takes up
            length = 0;
            length |= (region[offset] & 0xFF) << 24;    //0000 0000 <- 0000 0000 0000 0000 0000 0000
            length |= (region[offset + 1] & 0xFF) << 16;//0000 0000 0000 0000 <- 0000 0000 0000 0000      (<-) points to the current byte
            length |= (region[offset + 2] & 0xFF) << 8; //0000 0000 0000 0000 0000 0000 <- 0000 0000      each byte is 8 bits (0000 0000)
            length |= (region[offset + 3] & 0xFF);      //0000 0000 0000 0000 0000 0000 0000 0000 <-

            //get the compression type
            compressionType = 0;
            compressionType |= (region[offset + 4] & 0xFF); //0000 0000 0000 0000 0000 0000 0000 0000 <-

            //extract the compressed data from the region byte array
            compressedData = Arrays.copyOfRange(region, offset + 5, (offset + 5) + (length - 1));

            if(compressionType != 0) {//if it is zero it is an empty chunk
                Chunk chunk = new Chunk(length, compressionType, compressedData);
                CHUNKS.put(chunk.pos(), chunk);
            }
        }
    }

    //------------------------------------------------------------//

    public Chunk getChunk(int x, int z) {
        return CHUNKS.get(new Pos(x, z));
    }
}
