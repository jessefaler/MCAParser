package com.protoxon.mca;

import com.protoxon.mca.chunk.Block;
import com.protoxon.mca.chunk.Chunk;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.stream.Stream;

import static com.protoxon.mca.utils.ByteUtils.bytesToInt;

public class Region {

    RandomAccessFile MCAFile;

    //A HashMap to store all chunks that have been previously fetched
    //The key is a long that holds two 32-bit integers representing the x and z positions of the chunk
    HashMap<Long, Chunk> bufferedChunks = new HashMap<>();

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
     * @return a {@link Chunk} object for parsing a single chunks data
     * */
    public Chunk getChunk(int x, int z) throws IOException {
        long key = ((long) x << 32) | z;//compute the key by storing x and z in a long
        if(bufferedChunks.containsKey(key)) {//check if the chunk is in the buffer
            return bufferedChunks.get(key);
        }
        //if the chunk wasn't in the buffer fetch it from the MCA file
        int offset = getChunksOffset(x, z);
        if(offset == 0) {//chunk has not yet been generated yet handle accordingly
            //todo add the ability to generate basic NBT for a chunk so information such as blocks can be inserted
            return null;
        }
        MCAFile.seek(offset);
        int length = MCAFile.readInt();//get the length if the rest of the chunks data
        int compressionType = MCAFile.readByte();//get the chunks compression type
        byte[] compressedData = new byte[length - 1];
        MCAFile.readFully(compressedData);//read the compressed chunk data from the file
        Chunk chunk = new Chunk(compressionType, compressedData);
        bufferedChunks.put(key, chunk);//add the chunk to the buffer
        return chunk;
    }

    /**
     * Retrieves all the chunks within the region file.<p>
     * Chunks are stored in an array in the order of x increasing then z increasing<p>
     * To calculate a chunks index in the array, use the formula:
     * <pre>
     *     index = <b>z * 32 + x</b>
     * </pre>
     * @return an {@code array} containing all the chunks in the region.
     * @throws IOException if an I/O error occurs while reading from the region file.
     */
    public Chunk[] getChunks() throws IOException {
        Chunk[] chunks = new Chunk[1024];
        for (int z = 0; z < 32; z++) {
            for (int x = 0; x < 32; x++) {
                Chunk chunk = getChunk(x, z);
                if(chunk != null) {
                    chunks[z * 32 + x] = chunk;
                }
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

    /**
     * Gets the block at the specified location.<p>
     * The block coordinates can be absolute coordinates, or they can be relative to the region.
     * @param x The x-coordinate of the chunk
     * @param y The y-coordinate of the chunk
     * @param z The z-coordinate of the chunk
     * @return The block at the coordinate location
     */
    public Block getBlock(int x, int y, int z) throws IOException {
        int chunkX = x >> 4; // equivalent to x / 16
        int chunkZ = z >> 4; // equivalent to z / 16
        return getChunk(chunkX, chunkZ).getBlock(x, y, z);
    }

    /**
     * Retrieves all blocks that match the specified block.
     * @param block the block to search for.
     * @return an {@link Iterable} of {@link Block} objects that match the specified block.
     */
    public ArrayList<Block> findBlock(Block block) throws IOException {
        ArrayList<Block> blocks = new ArrayList<>();
        for(Chunk chunk : getChunks()) {
            blocks.addAll(chunk.findBlock(block));
        }
        return blocks;
    }

    /**
     * Retrieves all blocks that match the specified block.
     * @param name the block to search for.
     * @return an {@link Iterable} of {@link Block} objects that match the specified block name.
     */
    public ArrayList<Block> findBlock(String name) throws IOException {
        ArrayList<Block> blocks = new ArrayList<>();
        for(Chunk chunk : getChunks()) {
            if(chunk == null) {
                continue;
            }
            blocks.addAll(chunk.findBlock(name));
        }
        return blocks;
    }

    /**
     * Retrieves all blocks within a region. <p>
     *
     * @return an array of blocks
     */
    public Iterable<Block> getBlocks() throws IOException {
        Chunk[] chunks = getChunks();
        return () -> new Iterator<Block>() {
            private int chunkIndex = 0;
            private int blockIndex = 0;
            private Block[] currentBlocks = chunks[0].getBlocks();
            @Override
            public boolean hasNext() {
                return chunkIndex < chunks.length && blockIndex < 98304;
            }
            @Override
            public Block next() {
                Block block = currentBlocks[blockIndex++];
                if (blockIndex >= 98304 && chunkIndex < chunks.length - 1) {
                    blockIndex = 0;
                    currentBlocks = chunks[++chunkIndex].getBlocks();
                }
                return block;
            }
        };
    }

    /**
     * Retrieves all block entities present in the region.
     * <br><br>
     * Block entities are stored as CompoundTags. To access specific information
     * about a block entity such as its name or location, you will need to retrieve
     * that field from the CompoundTag. For example, to access its name:
     * <pre>{@code
     * for(CompoundTag blockEntity : getBlockEntities()) {
     *    String name = blockEntity.get("id").toString();
     * }
     * }</pre>
     * <p>
     * More details about the tag fields associated with each block entity can be found
     * at <a href="https://minecraft.wiki/w/Chunk_format#Block_entity_format">Block entity format</a>.
     *
     * @return an {@link Iterable} of {@link CompoundTag} objects representing block entities
     */
    public ArrayList<CompoundTag> getBlockEntities() throws IOException {
        ArrayList<CompoundTag> blockEntities = new ArrayList<>();
        for(Chunk chunk : getChunks()) {
            blockEntities.addAll(chunk.getBlockEntities().getValue());
        }
        return blockEntities;
    }
}
