package com.protoxon.mca.chunk;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;

/*
 * Represents a minecraft block
 * includes its pos and block state properties
 */
public class Block {

    private final int posX;
    private final int posY;
    private final int posZ;
    private final String name;
    private final CompoundTag properties;
    private final CompoundTag blockState;

    /**
     * Constructs a Block with the specified block state and position.
     *
     * @param blockState the block state containing the block's name and properties
     * @param pos the position of the block in the format [x, y, z]
     */
    public Block(CompoundTag blockState, int[] pos) {
        posX = pos[0];
        posY = pos[1];
        posZ = pos[2];
        name = blockState.get("Name").asRawString();
        properties = (CompoundTag) blockState.get("Properties");
        this.blockState = blockState;
    }

    /**
     * Constructs a Block with the specified block state and position.
     *
     * @param blockState the block state containing the block's name and properties
     * @param x the x-coordinate of the block
     * @param y the y-coordinate of the block
     * @param z the z-coordinate of the block
     */
    public Block(CompoundTag blockState, int x, int y, int z) {
        posX = x;
        posY = y;
        posZ = z;
        name = blockState.get("Name").asRawString();
        properties = (CompoundTag) blockState.get("Properties");
        this.blockState = blockState;
    }

    /**
     * Gets the block state.
     *
     * @return the block state as a CompoundTag which includes its properties and name
     */
    public CompoundTag getBlockState() {
        return blockState;
    }

    /**
     * Gets the blocks name
     *
     * @return the block state as a CompoundTag which includes its properties and name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the blocks properties.
     *
     * @return the block properties as a CompoundTag
     */
    public CompoundTag getProperties() {
        return properties;
    }

    /**
     * Gets the position of the block.
     *
     * @return an array containing the x, y, and z coordinates of the block
     */
    public int[] getPos() {
        return new int[]{posX, posY, posZ};
    }

    /**
     * Gets the x-coordinate of the block.
     *
     * @return the x-coordinate of the block
     */
    public int getPosX() {
        return posX;
    }

    /**
     * Gets the y-coordinate of the block.
     *
     * @return the y-coordinate of the block
     */
    public int getPosY() {
        return posY;
    }

    /**
     * Gets the z-coordinate of the block.
     *
     * @return the z-coordinate of the block
     */
    public int getPosZ() {
        return posZ;
    }
}
