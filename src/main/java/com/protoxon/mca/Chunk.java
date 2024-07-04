package com.protoxon.mca;

import com.viaversion.nbt.conversion.converter.CompoundTagConverter;
import com.viaversion.nbt.io.NBTIO;
import com.viaversion.nbt.io.TagReader;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.limiter.TagLimiter;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.Tag;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.InflaterInputStream;

public class Chunk {

    CompoundTag chunkNBT;

    /*
     * @param length, the number of bytes the chunk takes up in the file
     * @param compressionType, the compression method used (usually Zlib)
     * @param compressedData, the compressed chunk data
     */
    Chunk(int length, int compressionType, byte[] compressedData) throws IOException {
        decompress(compressionType, compressedData);
    }

    private void decompress(int compressionType, byte[] compressedData) throws IOException {
        if(compressionType != 2) {
            if(compressionType == 0) {//empty chunk
                return;
            }
            //unsupported compression type
            System.err.println("Unsupported compression type: " + compressionType);
            return;
        }

        try (InflaterInputStream iis = new InflaterInputStream(new ByteArrayInputStream(compressedData))) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = iis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            chunkNBT = convertToCompoundTag(baos.toByteArray());
        }
    }

    private CompoundTag convertToCompoundTag(byte[] chunkData) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(chunkData);
        DataInputStream dis = new DataInputStream(bais);
        return NBTIO.readTag(dis, TagLimiter.create(999999,999999), true, CompoundTag.class);
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


}
