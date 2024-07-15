package com.protoxon.mca.chunk;

import com.protoxon.mca.compression.Compression;
import com.viaversion.nbt.io.NBTIO;
import com.viaversion.nbt.limiter.TagLimiter;
import com.viaversion.nbt.tag.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import static com.protoxon.mca.Region.getMCVersionFromDataVersion;

/**
 * Abstraction for the base of all chunk version classes
 */
public abstract class ChunkBase {
    public CompoundTag chunkNBT;
    public ChunkBase chunk;

    /*
     * @param compressionID, the compression method used (usually Zlib)
     * @param compressedData, the compressed chunk data
     */
    public ChunkBase(int compressionID, byte[] compressedData) throws IOException {
        decompress(compressionID, compressedData);
    }

    /**
     * Decompresses the provided data using the specified compression type.
     *
     * @param compressionID the id of compression used (e.g., zlib, gzip)
     * @param compressedData the data to be decompressed
     * @throws IOException if an I/O error occurs during decompression
     */
    private void decompress(int compressionID, byte[] compressedData) throws IOException {
        Compression compression = new Compression();
        byte[] decompressedData = compression.decompress(compressionID, compressedData);
        chunkNBT = convertToCompoundTag(decompressedData);
    }

    /**
     * Converts the provided byte array representing chunk data into a CompoundTag.
     * Uses NBTIO provided by ViaNbt
     * @see com.viaversion.nbt.io.NBTIO
     *
     * @param chunkData the byte array containing the chunk data to convert
     * @return a CompoundTag representing the converted chunk data
     * @throws IOException if an I/O error occurs during the conversion process
     */
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
    public abstract IntTag getXPos();

    /**
     * Lowest Y section position in the chunk (e.g. -4 in 1.18)
     * @return IntTag
     */
    public abstract IntTag getYPos();

    /**
     * Z position of the chunk (in absolute chunks from world x, z origin, not relative to the region).
     * @return IntTag
     */
    public abstract IntTag getZPos();

    /**
     * Tick when the chunk was last saved.
     * @return LongTag
     */
    public abstract LongTag getLastUpdate();

    /**
     * Retrieves all sections within a chunk.
     * Chunks are divided into smaller 16x16x16 areas called sections.
     * @return List of Compounds
     */
    public abstract ListTag<CompoundTag> getSections();

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
     * <p>
     * See <a href="https://minecraft.wiki/w/Chunk_format#NBT_structure">Chunk format</a> for more info
     *
     * @return A UTF-8 NBT string representing the world generation status.
     */
    public abstract StringTag getStatus();

    /**
     * Retrieves all blocks present in the chunk.
     *
     * @return An ArrayList containing all blocks in the chunk.
     */
    public abstract ArrayList<Block> getBlocks();

    /**
     * Retrieves all block entities present in the chunk.
     * <br><br>
     * Block entities are stored as CompoundTags. To access specific information
     * about a block entity such as its name or location, you will need to retrieve
     * that field from the CompoundTag. For example, to access its name:
     * <pre>{@code
     * for(CompoundTag blockEntity : getBlockEntities()) {
     *    String name = blockEntity.get("id").toString();
     * }
     * }</pre>
     *
     * More details about the tag fields associated with each block entity can be found
     * at <a href="https://minecraft.wiki/w/Chunk_format#Block_entity_format">Block entity format</a>.
     *
     * @return A list of CompoundTags representing all block entities in the chunk.
     */
    public abstract ListTag<CompoundTag> getBlockEntities();

    /**
     * The cumulative number of ticks players have been in this chunk. <br>
     * Note that this value increases faster when more players are in the chunk. <br>
     * Used for <a href="https://minecraft.wiki/w/Difficulty#Regional_difficulty">Regional difficulty</a>
     * @return LongTag representing the inhabited time
     */
    public abstract LongTag getInhabitedTime();

    /**
     * Retrieves the block at the specified coordinates (x, y, z) within this chunk.
     *
     * @param x the x-coordinate of the block
     * @param y the y-coordinate of the block
     * @param z the z-coordinate of the block
     * @return the Block object at the specified coordinates, or null if no block is found
     */
    public Block getBlockAt(int x, int y, int z) {
        for(Block block : getBlocks()) {
            if(block.getPosX() == x && block.getPosY() == y && block.getPosZ() == z) {
                return block;
            }
        }
        return null;
    }

    /**
     * gets a list of entities present in the chunk
     * <br><br>
     *Entities are stored as CompoundTags. To access specific information
     *about a block entity such as its name or pos, you will need to retrieve
     *that field from the CompoundTag. For example, to access its name:
     *<pre>{@code
     *for(CompoundTag entity : getEntities()) {
     *  String name = entity.get("id").toString();
     *}
     *}</pre>
     *
     * As of 1.17, this list is not present for fully generated chunks and entities are moved to a separated region files once the chunk is generated
     * see <a href="https://minecraft.wiki/w/Entity_format#Entity_Format">Entity format</a> for more details
     * @return A list of CompoundTags representing all entities in the chunk.
     */
    public abstract ListTag<CompoundTag> getEntities();
}
