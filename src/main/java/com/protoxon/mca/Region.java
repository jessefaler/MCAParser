package com.protoxon.mca;

import com.protoxon.mca.chunk.Chunk_old;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

import static com.protoxon.mca.utils.ByteUtils.bytesToInt;

public class Region {

    RandomAccessFile MCAFile;

    //A HashMap to store all chunks that have been previously fetched
    //The key is a long that holds two 32-bit integers representing the x and z positions of the chunk
    HashMap<Long, Chunk_old> bufferedChunks = new HashMap<>();

    /**
     * initializes the region for reading from a file
     * @param file a RandomAccessFile representing the region
     */
    Region(RandomAccessFile file) {
        MCAFile = file;
    }

    /**
     * Used for creating a new blank region
     * @param region 1
     */
    //TODO implement the ability to create blank regions
    Region(byte[] region) {}


    /**
     * gets a chunk from the region
     * @param x The x-coordinate of the chunk.
     * @param z The z-coordinate of the chunk.
     * @return a {@link Chunk_old} object for parsing a single chunks data
     * */
    public Chunk_old getChunk(int x, int z) throws IOException {
        long key = ((long) x << 32) | z;//compute the key by storing x and z in a long
        if(bufferedChunks.containsKey(key)) {//check if the chunk is in the buffer
            return bufferedChunks.get(key);
        }
        //if the chunk wasn't in the buffer fetch it from the MCA file
        int offset = getChunksOffset(x, z);
        if(offset == 0) {//chunk has not yet been generated yet handle accordingly
            //todo add the ability to generate basic NBT for a chunk so information such as blocks can be inserted
            System.out.println("Chunk " + x + ", " + z + " has not been generated yet");
            return null;
        }
        MCAFile.seek(offset);
        int length = MCAFile.readInt();//get the length if the rest of the chunks data
        int compressionType = MCAFile.readByte();//get the chunks compression type
        byte[] compressedData = new byte[length - 1];
        MCAFile.readFully(compressedData);//read the compressed chunk data from the file
        Chunk_old chunk = new Chunk_old(compressionType, compressedData);
        bufferedChunks.put(key, chunk);//add the chunk to the buffer
        return chunk;
    }

    /**
     * Retrieves all the chunks within the region file.<p>
     * @return an {@code ArrayList} containing all the chunks in the region.
     * @throws IOException if an I/O error occurs while reading from the region file.
     */
    public Chunk_old[] getChunks() throws IOException {
        Chunk_old[] chunks = new Chunk_old[1024];
        for (int x = 0; x < 32; x++) {
            for (int z = 0; z < 32; z++) {
                chunks[x * 32 + z] = getChunk(x, z);
            }
        }
        return chunks;
    }

    /**
     * gets all the offset values in bytes
     * @return An ArrayList of offsets
     */
    public ArrayList<Integer> getOffsets() throws IOException {
        ArrayList<Integer> offsets = new ArrayList<>(1024);
        MCAFile.seek(0);
        for (int i = 0; i < 1024; i++) {
            byte[] offsetInBytes = new byte[3];
            MCAFile.read(offsetInBytes);//read the next 3 bytes from the file
            offsets.add(bytesToInt(offsetInBytes) * 4096);
            MCAFile.skipBytes(1);
        }
        return offsets;
    }

    /**
     * Calculates the byte offset for the chunk at (x, z) <p>
     * The offset for each chunk is stored in 3 bytes as an unsigned integer<p>
     * The offset represents the number of 4KiB sectors from the start of the file to where the chunks data is located
     * @param x The x-coordinate of the chunk.
     * @param z The z-coordinate of the chunk.
     * @return the chunk data offset in bytes from the start of the file
     */
    private int getChunksOffset(int x, int z) throws IOException {
        int offsetLocation = 4 * ((x & 31) + (z & 31) * 32);//calculate the location of the chunks offset information in the file
        MCAFile.seek(offsetLocation);//go to the offsets location in the file
        byte[] offsetInBytes = new byte[3];
        MCAFile.read(offsetInBytes);//read the next 3 bytes from the file
        return bytesToInt(offsetInBytes) * 4096;//multiply by 4096 to get the offset in bytes
    }
}
