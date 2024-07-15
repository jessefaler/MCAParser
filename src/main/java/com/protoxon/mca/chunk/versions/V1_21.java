package com.protoxon.mca.chunk.versions;

import com.protoxon.mca.chunk.Block;
import com.protoxon.mca.chunk.ChunkBase;
import com.viaversion.nbt.tag.*;

import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings({"unchecked"})
public class V1_21 extends ChunkBase {
    public V1_21(int compressionID, byte[] compressedData) throws IOException {
        super(compressionID, compressedData);
    }
    @Override
    public IntTag getXPos() {
        return (IntTag) chunkNBT.get("xPos");
    }

    @Override
    public IntTag getYPos() {
        return (IntTag) chunkNBT.get("yPos");
    }

    @Override
    public IntTag getZPos() {
        return (IntTag) chunkNBT.get("zPos");
    }

    @Override
    public LongTag getLastUpdate() {
        return (LongTag) chunkNBT.get("LastUpdate");
    }

    @Override
    public ListTag<CompoundTag> getSections() {
        return (ListTag<CompoundTag>) chunkNBT.get("sections");
    }
    @Override
    public StringTag getStatus() {
        return (StringTag) chunkNBT.get("Status");
    }

    @Override
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

    @Override
    public Block getBlockAt(int x, int y, int z) {
        for(Block block : getBlocks()) {
            if(block.getPosX() == x && block.getPosY() == y && block.getPosZ() == z) {
                return block;
            }
        }
        return null;
    }

    @Override
    public ListTag<CompoundTag> getEntities() {
        return (ListTag<CompoundTag>) chunkNBT.get("Entities");
    }

    public ListTag<CompoundTag> getBlockEntities() {
        return (ListTag<CompoundTag>) chunkNBT.get("block_entities");
    }

    public LongTag getInhabitedTime() {
        return (LongTag) chunkNBT.get("InhabitedTime");
    }
}
