package com.protoxon.mca.chunk;

import com.protoxon.mca.compression.Compression;
import com.viaversion.nbt.io.NBTIO;
import com.viaversion.nbt.tag.*;
import com.viaversion.nbt.limiter.TagLimiter;

import java.io.*;
import java.util.ArrayList;

import static com.protoxon.mca.Region.getMCVersionFromDataVersion;

public class Chunk {

    CompoundTag chunkNBT;

    /*
     * @param length, the number of bytes the chunk takes up in the file
     * @param compressionType, the compression method used (usually Zlib)
     * @param compressedData, the compressed chunk data
     */

    public Chunk(int length, int compressionType, byte[] compressedData) throws IOException {
        decompress(compressionType, compressedData);
    }


    private void decompress(int compressionType, byte[] compressedData) throws IOException {
        Compression compression = new Compression();
        byte[] decompressedData = compression.decompress(compressionType, compressedData);
        chunkNBT = convertToCompoundTag(decompressedData);
    }


    private CompoundTag convertToCompoundTag(byte[] chunkData) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(chunkData);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
        return NBTIO.readTag(dataInputStream, TagLimiter.create(999999999, 999999999), true, CompoundTag.class);
    }

    /**
     * Data Version of the chunk NBT structure.
     * @return IntTag
     */
    public IntTag getDataVersion() {
        return (IntTag) chunkNBT.get("DataVersion");
    }

    /**
     * Gets the Minecraft version of the chunk
     * @return IntTag
     */
    public String getMinecraftVersion() {
        return getMCVersionFromDataVersion((Integer) chunkNBT.get("DataVersion").getValue());
    }


    /**
     * X position of the chunk (in absolute chunks from world x, z origin, not relative to the region).
     * @return IntTag
     */
    public IntTag getXPos() {
        return (IntTag) chunkNBT.get("xPos");
    }

    /**
     * Lowest Y section position in the chunk (e.g. -4 in 1.18)
     * @return IntTag
     */
    public IntTag getYPos() {
        return (IntTag) chunkNBT.get("yPos");
    }

    /**
     * Z position of the chunk (in absolute chunks from world x, z origin, not relative to the region).
     * @return IntTag
     */
    public IntTag getZPos() {
        return (IntTag) chunkNBT.get("zPos");
    }

    /**
     * Tick when the chunk was last saved.
     * @return LongTag
     */
    public LongTag lastUpdate() {
        return (LongTag) chunkNBT.get("LastUpdate");
    }

    public ListTag<CompoundTag> getSections() {
        return (ListTag<CompoundTag>) chunkNBT.get("sections");
    }

    /**
     * Defines the world generation status of a chunk.
     * Possible values are:
     * <ul>
     * <li>minecraft:empty</li>
     * <li>minecraft:structure_starts</li>
     * <li>minecraft:structure_references</li>
     * <li>minecraft:biomes</li>
     * <li>minecraft:noise</li>
     * <li>minecraft:surface</li>
     * <li>minecraft:carvers</li>
     * <li>minecraft:features</li>
     * <li>minecraft:light</li>
     * <li>minecraft:spawn</li>
     * <li>minecraft:full</li>
     * </ul>
     *
     * All statuses except minecraft:full are used for proto-chunks, which are chunks
     * with incomplete generation.
     *
     * @return A UTF-8 NBT string representing the world generation status.
     */
    public StringTag getStatus() {
        return (StringTag) chunkNBT.get("Status");
    }


    /**
     * gets all the blocks within the chunk
     *
     * @return array of Blocks
     */
    /*
     * Region files store blocks by having each block point to an index in the palette array.
     * The palette stores all the block states present in a section.
     * The data field contains longs that represent indices pointing to the palette array.
     *
     * The number of bits required to represent each index in the palette is determined
     * by the size of the palette. For example, if the palette contains 16 block states,
     * the smallest number of bits needed to represent each index is calculated as:
     * log₂(16) = 4, because 2^4 = 16. Thus, 4 bits are required to represent indices 0 to 15.
     *
     * These indices are extracted by reading the specified number of bits from the longs
     */
    public ArrayList<Block> getBlocks() {
        ArrayList<Block> blocks = new ArrayList<>(382);
        int x = getXPos().asInt();
        int z = getZPos().asInt();
        for (CompoundTag section : getSections()) {
            byte y = (byte) section.get("Y").getValue();
            CompoundTag blockStates = (CompoundTag) section.get("block_states");
            if(blockStates == null) {
                continue;
            }

            ListTag<CompoundTag> palette = (ListTag<CompoundTag>) blockStates.get("palette");
            LongArrayTag data = (LongArrayTag) blockStates.get("data");

            if(data == null || palette == null) {
                continue;
            }

            int bitsPerIndex = Math.max(4, (int) Math.ceil(Math.log(palette.size()) / Math.log(2)));
            int index;
            int bitPosition;
            int blockPos = -1;
            for(long value : data.getValue()) {
                bitPosition = 0;
                while(bitPosition + bitsPerIndex <= 64) {
                    index = 0;
                    index |= (int) ((value >> bitPosition) & ((1L << bitsPerIndex) - 1));
                    bitPosition += bitsPerIndex;
                    CompoundTag block = palette.get(index);
                    blockPos += 1;
                    blocks.add(new Block(block, getPosFromIndex(blockPos, x, y, z)));
                }
            }
        }
        return blocks;
    }

    public Block getBlockAt(int x, int y, int z) {
        for(Block block : getBlocks()) {
            if(block.posX == x && block.posY == y && block.posZ == z) {
                return block;
            }
        }
        return null;
    }

    /**
     * This method is used by the getBlocks method to determine the location of a block within a chunk using its index in the data array.
     * Entries are stored in order of increasing x coordinate, within rows of increasing z coordinates, within layers of increasing y coordinates.
     * In other words, if the data array were a multidimensional array in C (considering packed encoding), it would be indexed as array[y][z][x].
     * see (<a href="https://wiki.vg/Chunk_Format#Data_Array_format">Data Array format</a>) for more info
     * The method calculates the block's position in the chunk (ranging from 0 to 15 for x, y, and z) and then converts it into world coordinates.
     *
     * @param index The index of the block in the data array.
     * @param x The chunk's x coordinate.
     * @param y The chunk section y coordinate.
     * @param z The chunk's z coordinate.
     * @return An array containing the world coordinates [worldX, worldY, worldZ].
     */
    private int[] getPosFromIndex(int index, int x, int y, int z) {
        int worldX = (x * 16) + (index % 16);
        int worldY = (y * 16) + (index / 256);
        int worldZ = (z * 16) + ((index % 256) / 16);
        return new int[]{worldX, worldY, worldZ};
    }

    public ListTag<CompoundTag> getBlockEntities() {
        return (ListTag<CompoundTag>) chunkNBT.get("block_entities");
    }

    public LongTag getInhabitedTime() {
        return (LongTag) chunkNBT.get("InhabitedTime");
    }
}
