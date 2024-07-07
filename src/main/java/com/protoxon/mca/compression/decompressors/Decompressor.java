package com.protoxon.mca.compression.decompressors;

import java.io.IOException;

public interface Decompressor {
    byte[] decompress(byte[] data) throws IOException;
}
