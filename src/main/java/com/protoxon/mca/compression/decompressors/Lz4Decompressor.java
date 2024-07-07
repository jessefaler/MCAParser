package com.protoxon.mca.compression.decompressors;

import java.io.IOException;

public class Lz4Decompressor implements Decompressor {
    @Override
    public byte[] decompress(byte[] data) throws IOException {
        throw new UnsupportedOperationException("Compression type not yet supported");
    }
}
