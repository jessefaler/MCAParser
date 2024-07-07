package com.protoxon.mca.compression;

import com.protoxon.mca.compression.decompressors.*;
import com.protoxon.mca.compression.decompressors.Decompressor;
import java.io.IOException;

public enum CompressionType {

    //ID's associated with each compression type
    //https://minecraft.wiki/Region_file_format#Payload
    GZIP(1, new GzipDecompressor()),
    ZLIB(2, new ZlibDecompressor()),
    NONE(3, data -> data),
    LZ4(4, new Lz4Decompressor()),
    CUSTOM(127, new CustomDecompressor());

    private final int id;
    private final Decompressor decompressor;

    CompressionType(int id, Decompressor decompressor) {
        this.id = id;
        this.decompressor = decompressor;
    }

    //decompresses the compressed bytes using the enums defined decompressor
    public byte[] decompress(byte[] compressedData) throws IOException {
        return decompressor.decompress(compressedData);
    }

    //returns the compression type associated with the id
    public static CompressionType getFromID(int id) {
        for (CompressionType compressionType : CompressionType.values()) {
            if (compressionType.id == id) {
                return compressionType;
            }
        }
        return null;
    }
}
