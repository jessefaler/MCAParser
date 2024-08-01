package com.protoxon.mca.compression.compressors;

import java.io.IOException;

/**
 * Interface for all compressors classes<p>
 * used for polymorphism of compressors in the compressionType class
 *
 * @see com.protoxon.mca.compression.CompressionType
 */
public interface Compressor {
    byte[] compress(byte[] data) throws IOException;
}
