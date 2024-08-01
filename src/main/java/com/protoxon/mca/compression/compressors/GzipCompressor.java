package com.protoxon.mca.compression.compressors;

import java.io.IOException;

public class GzipCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] data) throws IOException {
        throw new UnsupportedOperationException("Compression type not yet supported");
    }
}
