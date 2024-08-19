package com.protoxon.mca;

import com.protoxon.mca.chunk.Chunk;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/*
 * see "https://minecraft.fandom.com/wiki/Region_file_format" for more info
 */

public class Region {

    byte[] region;
    int[] offsets = new int[1024];
    private static HashMap<Integer, String> dataVersions;

    //HashMap to store all chunks loaded into memory
    //The key is a long that holds two 32-bit integers representing the x and z positions of the chunk
    Map<Long, Chunk> CHUNKS = new LinkedHashMap<>(1024, 1);
    Region(File region) throws IOException {
        this.region = read(region);
        offsets = offsets();
        loadChunks();
    }

    /**
     * Reads the region file and loads the chunks
     *
     * @param region The path to the region file.
     * @throws IOException if an I/O error occurs during file reading.
     */
    Region(String region) throws IOException {
        this.region = read(region);
        offsets = offsets();
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


    public byte[] exportRegion() throws IOException {
        //Header initialization
        byte[] LOCATIONS = new byte[0]; //locations (1024 entries; 4 bytes each) indexes 0-4095
        byte[] TIMESTAMPS = new byte[0]; //timestamps (1024 entries; 4 bytes each) indexes 4096-8192
        byte[] CHUNKS_OUT = new byte[0]; //(chunks and unused space) indexes 8193...
        byte[] MCA_DATA = new byte[0];

        //get all the chunks
        for(Chunk chunk : CHUNKS.values()) {
            //chunk.saveChunk();

            byte[] compressedChunk = chunk.saveChunk();
            if(chunk.getXPos().asInt() == 0 && chunk.getZPos().asInt() == 0) {
                System.out.println(chunk.chunkNBT);
            }
            //gets the offset and sector count
            int offset = getNumberOf4KiBSectors(8192 + CHUNKS_OUT.length);
            int sectorCount = getNumberOf4KiBSectors(compressedChunk.length + 4); //chunks compressed data + payload
            byte[] location = appendBytes(mapIntToBytes(offset, 3), mapIntToBytes(sectorCount, 1));
            LOCATIONS = appendBytes(LOCATIONS, location);
            //constructs the chunks Payload information
            int length = compressedChunk.length + 1;//the number of bytes the chunks compressed data takes up plus the compression type field
            int compressionType = 2;//2 for Zlib (need to add full functionality)
            byte[] payload = appendBytes(mapIntToBytes(length, 4), appendBytes(mapIntToBytes(compressionType, 1), compressedChunk));
            CHUNKS_OUT = padTo4096(appendBytes(CHUNKS_OUT, payload));
        }
        //get timestamps
        TIMESTAMPS = sliceByteArray(region, 4096, 8192);
        //construct the MCA Data
        MCA_DATA = appendBytes(MCA_DATA, LOCATIONS);
        MCA_DATA = appendBytes(MCA_DATA, TIMESTAMPS);
        MCA_DATA = appendBytes(MCA_DATA, CHUNKS_OUT);
        MCA_DATA = padTo4096(MCA_DATA);
        return MCA_DATA;
    }

    public byte[] sliceByteArray(byte[] data, int startIndex, int endIndex) {
        if (startIndex < 0 || endIndex > data.length || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid start or end index");
        }

        // Calculate the length of the new byte array
        int length = endIndex - startIndex;

        // Create the new byte array to hold the slice
        byte[] result = new byte[length];

        // Copy the specified range from the original array into the new array
        System.arraycopy(data, startIndex, result, 0, length);

        return result;
    }

    public int getNumberOf4KiBSectors(int byteCount) {
        int sectorSize = 4096;
        return (byteCount + sectorSize - 1) / sectorSize;
    }

    public static byte[] padTo4096(byte[] data) {
        int sectorSize = 4096;
        int originalLength = data.length;

        // Calculate how much padding is needed
        int padding = sectorSize - (originalLength % sectorSize);

        // If the data is already a multiple of 4096, no padding is needed
        if (padding == sectorSize) {
            return data;
        }

        // Create a new array with the new length (original length + padding)
        byte[] paddedData = Arrays.copyOf(data, originalLength + padding);

        // Optionally fill the padding with zeros (this is done by Arrays.copyOf automatically)
        // If specific padding is required, you can fill the rest manually

        return paddedData;
    }


    public byte[] appendBytes(byte[] original, byte[] toAppend) {
        byte[] result = new byte[original.length + toAppend.length];

        // Copy the original byte array into the result
        System.arraycopy(original, 0, result, 0, original.length);

        // Copy the byte array to append into the result
        System.arraycopy(toAppend, 0, result, original.length, toAppend.length);

        return result;
    }


    public byte[] mapIntToBytes(int number, int numBytes) {
        // Check if the number of bytes is valid
        if (numBytes < 1 || numBytes > 4) {
            throw new IllegalArgumentException("Number of bytes must be between 1 and 4.");
        }

        byte[] unsignedBytes = new byte[numBytes];

        // Convert the integer to the corresponding unsigned byte array
        for (int i = 0; i < numBytes; i++) {
            unsignedBytes[numBytes - 1 - i] = (byte) (number >>> (i * 8));
        }

        return unsignedBytes;
    }


    /*




    public byte[] exportRegion() throws IOException {
        byte[] newOffsets = new byte[4096];
        byte[] out = Arrays.copyOfRange(region, 4096,8191);
        byte sectorCount;
        int offsetIndex = 0;
        int currentByte = 8192;//chunk data starts at byte 8192
        int lastChunk = 0;
        for(Chunk chunk : CHUNKS.values()) {
            byte[] temp = chunk.compress();
            byte[] result = new byte[out.length + temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
            System.arraycopy(out, 0, result, temp.length, out.length);



            sectorCount = (byte) (((temp.length + 5) / 4096) & 0xFF); //divide by 4 to get the sector count
            if(lastChunk == CHUNKS.size() - 1) {//add padding to the last chunk to be a multiple-of-4096B in length
                result = addPadding(result);
            }
            out = result;
            currentByte += result.length;
            System.out.println("unsigned: " + ((temp.length / 4096) & 0xFF));
            newOffsets[offsetIndex] = (byte) ((currentByte >> 16) & 0xFF);
            newOffsets[offsetIndex + 1] = (byte) ((currentByte >> 8) & 0xFF);
            newOffsets[offsetIndex + 2] = (byte) ((currentByte) & 0xFF);
            newOffsets[offsetIndex + 3] = sectorCount;
            System.out.println(newOffsets[offsetIndex + 3]);
            offsetIndex += 4;
            lastChunk++;
        }
        byte[] result = new byte[out.length + newOffsets.length];
        System.arraycopy(newOffsets, 0, result, 0, newOffsets.length);
        System.arraycopy(out, 0, result, newOffsets.length, out.length);
        System.out.println("result: " + result[3]);
        out = result;
        return out;
    }


    //adds padding to a byte array
    private byte[] addPadding(byte[] originalArray) {
        int originalLength = originalArray.length;
        int paddedLength = ((originalLength + 4096 - 1) / 4096) * 4096;

        byte[] paddedArray = new byte[paddedLength];
        System.arraycopy(originalArray, 0, paddedArray, 0, originalLength);

        return paddedArray;
    }

     */

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
        int count = 0;
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
                Chunk chunk = new Chunk(compressionType, compressedData);
                CHUNKS.put(computeKey(chunk.getXPos().asInt(), chunk.getZPos().asInt()), chunk);
            }
            count++;
        }
    }

    public Chunk getChunk(int x, int z) {
        return CHUNKS.get(computeKey(x, z));
    }

    /**
     * Computes a unique key identifier for an (x, z) position.
     * Combines the x integer in the upper 32 bits of a long
     * and the z integer in the lower 32 bits, leveraging the
     * 64-bit size of a long.
     *
     * @param x The x position integer.
     * @param z The z position integer.
     * @return A unique long key identifier combining x and z.
     */
    private long computeKey(int x, int z) {
        return ((long) x << 32) | z;
    }

    /**
     * Retrieves the x and z position integers from a given key.
     * Assumes the key was generated using computeKey(x, z).
     *
     * @param key The key containing combined x and z positions.
     * @return An array where index 0 is x and index 1 is z.
     */
    private int[] getPosFromKey(long key) {
        int x = (int) (key >> 32);  //retrieves x from upper 32 bits
        int z = (int) key;          //retrieves z from lower 32 bits
        return new int[]{x, z};
    }

    /**
     * Retrieves the Minecraft version corresponding to the given data version.
     * This method initializes the dataVersion HashMap on the first call,
     * ensuring that it is only loaded when necessary.
     *
     * @param dataVersion The data version number.
     * @return A String specifying the Minecraft version.
     */
    public static String getMCVersionFromDataVersion(int dataVersion) {
        if(dataVersions == null) {
            readInDataVersions();
        }
        return dataVersions.get(dataVersion);
    }

    /**
     * Reads the data versions file and populates the Data_Versions HashMap.
     * The file is expected to be in the format "minecraftVersion, dataVersion" on each line.
     */
    private static void readInDataVersions() {
        dataVersions = new HashMap<>(557);
        try (InputStream inputStream = Region.class.getResourceAsStream("/Data_Versions");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(", ");
                if (parts.length == 2) {
                    String minecraftVersion = parts[0];
                    int dataVersion = Integer.parseInt(parts[1]);
                    dataVersions.put(dataVersion, minecraftVersion);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
