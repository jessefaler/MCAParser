package com.protoxon.mca.compression;

import java.io.IOException;

public class Compression {

    /**
     * Decompresses the given compressed data based on the specified compression method ID.
     * see <a href="https://minecraft.wiki/w/Region_file_format#Payload">...</a> for compression id's
     *
     * @param compressionID The ID representing the compression method used
     * @param compressedData The data to be decompressed.
     * @return byte[] The decompressed data.
     */
    public byte[] decompress(int compressionID, byte[] compressedData) throws IOException {
        CompressionType compressionType = CompressionType.getFromID(compressionID);
        if(compressionType == null) {
            throw new IOException("invalid compression type " + compressionID);
        }
        return compressionType.decompress(compressedData);
    }
}
