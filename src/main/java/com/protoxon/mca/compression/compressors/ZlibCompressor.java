package com.protoxon.mca.compression.compressors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

public class ZlibCompressor implements Compressor {

    /** compresses data with Zlib
     *
     * @param data the uncompressed data
     * @return byte[] of compressed data
     */
    @Override
    public byte[] compress(byte[] data) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();
        byte[] buffer = new byte[1024];
        int compressedDataLength;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            while (!deflater.finished()) {
                compressedDataLength = deflater.deflate(buffer);
                outputStream.write(buffer, 0, compressedDataLength);
            }
        } finally {
            deflater.end();
        }
        return outputStream.toByteArray();
    }
}
