package com.protoxon.mca.compression;

import com.protoxon.mca.compression.compressors.*;
import com.protoxon.mca.compression.decompressors.*;

import java.io.IOException;

/**
 * An enumeration representing each compression type used in Minecraft region files.
 * Each enum constant contains the compression type's ID, decompressor, and compressor to use.
 *
 * <p>For more details on the compression IDs, see the
 * <a href="https://minecraft.wiki/w/Region_file_format#Payload">Region File Payload</a> section of the Minecraft Wiki.</p>
 *
 * <p>This enum is used to determine the appropriate compression and decompression mechanisms
 * for data within Minecraft region files.</p>
 */
public enum CompressionType {

    //ID's associated with each compression type
    //https://minecraft.wiki/Region_file_format#Payload
    GZIP(1, new GzipDecompressor(), new GzipCompressor()),
    ZLIB(2, new ZlibDecompressor(), new ZlibCompressor()),
    NONE(3, data -> data, data -> data),
    LZ4(4, new Lz4Decompressor(), new Lz4Compressor()),
    CUSTOM(127, new CustomDecompressor(), new CustomCompressor());
    private final int id;
    private final Decompressor decompressor;
    private final Compressor compressor;
    CompressionType(int id, Decompressor decompressor, Compressor compressor) {
        this.id = id;
        this.decompressor = decompressor;
        this.compressor = compressor;
    }

    //decompresses the compressed bytes using the enums defined decompressor
    public byte[] decompress(byte[] compressedData) throws IOException {
        return decompressor.decompress(compressedData);
    }

    //compresses the bytes using the enums defined compressor
    public byte[] compress(byte[] compressedData) throws IOException {
        return compressor.compress(compressedData);
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
