package com.protoxon.mca;

import com.protoxon.mca.compression.Compression;
import com.viaversion.nbt.io.NBTIO;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.limiter.TagLimiter;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.Tag;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;

public class Chunk {

    CompoundTag chunkNBT;

    /**
     * @param length, the number of bytes the chunk takes up in the file
     * @param compressionType, the compression method used (usually Zlib)
     * @param compressedData, the compressed chunk data
     */
    Chunk(int length, int compressionType, byte[] compressedData) throws IOException {
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
        return NBTIO.readTag(dataInputStream, TagLimiter.create(999999999,999999999), true, CompoundTag.class);
    }

    public int DataVersion() {
        return (int) chunkNBT.get("DataVersion").getValue();
    }

    public Pos pos() {
        return new Pos((int) chunkNBT.get("xPos").getValue(), (int) chunkNBT.get("zPos").getValue());
    }
    public String status() {
        return (String) chunkNBT.get("Status").getValue();
    }

    public long lastUpdate() {
        return (long) chunkNBT.get("LastUpdate").getValue();
    }

    public ArrayList<?> sections() {
        return (ArrayList<?>) chunkNBT.get("sections").getValue();
    }

    public @Nullable Tag getBlocks() {
        for (Object section : sections()) {
            CompoundTag section1 = (CompoundTag) section;
            CompoundTag palette = (CompoundTag) section1.get("block_states");

            ListTag<CompoundTag> block = (ListTag<CompoundTag>) palette.get("palette");
            System.out.println(block);

           // System.out.println(palette.get("data"));

            //for (Object block1 : block) {
            //    System.out.println(block1);
            //}
        }
        return null;
    }




}
