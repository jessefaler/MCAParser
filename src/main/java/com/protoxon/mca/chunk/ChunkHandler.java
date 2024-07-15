package com.protoxon.mca.chunk;

import com.protoxon.mca.chunk.versions.V1_21;

import java.io.IOException;

public class ChunkHandler {

    int compressionType;
    byte[] compressedData;

    public ChunkHandler(int compressionType, byte[] compressedData) {
        this.compressionType = compressionType;
        this.compressedData = compressedData;
    }

    public ChunkBase getHandler(int dataVersion) throws IOException {
        if(dataVersion == 3953) {
            return new V1_21(compressionType, compressedData);
        } else if (dataVersion == 100) {

        }
        //return newest chunk version if no data version matched
        return new V1_21(compressionType, compressedData);
    }
}
