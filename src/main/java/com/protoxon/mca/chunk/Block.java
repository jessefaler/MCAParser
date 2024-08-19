package com.protoxon.mca.chunk;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;

/*
 * Represents a minecraft block
 * includes its pos and block state properties
 */
public class Block {

    private final int[] location;
    private final String name;
    private final CompoundTag properties;
    private final CompoundTag blockState;

    /**
     * Constructs a Block with the specified block state and position.
     *
     * @param blockState the block state containing the block's name and properties
     * @param location the position of the block in the format [x, y, z]
     */
    public Block(CompoundTag blockState, int[] location) {
        this.location = location;
        name = blockState.get("Name").asRawString();
        properties = (CompoundTag) blockState.get("Properties");
        this.blockState = blockState;
    }

    /**
     * Constructs a Block with the specified block state and position.
     *
     * @param blockState the block state containing the block's name and properties
     * @param xPos the x-coordinate of the block
     * @param yPos the y-coordinate of the block
     * @param zPos the z-coordinate of the block
     */
    public Block(CompoundTag blockState, int xPos, int yPos, int zPos) {
        location = new int[]{xPos, yPos, zPos};
        name = blockState.get("Name").asRawString();
        properties = (CompoundTag) blockState.get("Properties");
        this.blockState = blockState;
    }

    /**
     * Constructs a Block with the specified block state and position.
     *
     * @param blockState the block state containing the block's name and properties
     */
    public Block(CompoundTag blockState) {
        location = null;
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
        return location;
    }

    /**
     * Gets the x-coordinate of the block.
     *
     * @return the x-coordinate of the block
     */
    public int getPosX() {
        return location[0];
    }

    /**
     * Gets the y-coordinate of the block.
     *
     * @return the y-coordinate of the block
     */
    public int getPosY() {
        return location[1];
    }

    /**
     * Gets the z-coordinate of the block.
     *
     * @return the z-coordinate of the block
     */
    public int getPosZ() {
        return location[2];
    }
}
