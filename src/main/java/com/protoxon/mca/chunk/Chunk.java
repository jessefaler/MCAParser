package com.protoxon.mca.chunk;

import com.viaversion.nbt.tag.*;

import java.io.IOException;
import java.util.ArrayList;


/**
 * The {@code Chunk} class serves as a facade for accessing chunk data. It delegates the calls to the appropriate chunk version handler
 * ({@code ChunkHandler}), which encapsulates the complexity of handling different versions of chunks. This allows for a uniform way to
 * interact with chunks, regardless of their version.
 *
 * <p>Each method in this class corresponds to a specific piece of data that can be retrieved from a chunk. The actual retrieval and
 * processing of the data is handled by the {@code ChunkHandler} class, which is determined based on the version of the chunk.
 *
 * <p>This class extends {@code ChunkBase}, which provides the basic structure and common functionality for all chunk versions.
 *
 * @see com.protoxon.mca.chunk.ChunkHandler
 * @see com.protoxon.mca.chunk.ChunkBase
 * @see com.protoxon.mca.chunk.versions
 */
public class Chunk extends ChunkBase {
    /*
     * @param compressionID, the compression method used (usually Zlib)
     * @param compressedData, the compressed chunk data
     */
    public Chunk(int compressionID, byte[] compressedData) throws IOException {
        super(compressionID, compressedData);
        chunk = new ChunkHandler(compressionID, compressedData).getHandler(getDataVersion().asInt());
    }

    @Override
    public IntTag getXPos() {
        return chunk.getXPos();
    }

    @Override
    public IntTag getYPos() {
        return chunk.getYPos();
    }

    @Override
    public IntTag getZPos() {
        return chunk.getZPos();
    }

    @Override
    public LongTag getLastUpdate() {
        return chunk.getLastUpdate();
    }

    @Override
    public ListTag<CompoundTag> getSections() {
        return chunk.getSections();
    }

    @Override
    public StringTag getStatus() {
        return chunk.getStatus();
    }

    @Override
    public ArrayList<Block> getBlocks() {
        return chunk.getBlocks();
    }

    @Override
    public ListTag<CompoundTag> getBlockEntities() {
        return chunk.getBlockEntities();
    }

    @Override
    public LongTag getInhabitedTime() {
        return chunk.getInhabitedTime();
    }

    @Override
    public ListTag<CompoundTag> getEntities() {
        return chunk.getEntities();
    }
}
