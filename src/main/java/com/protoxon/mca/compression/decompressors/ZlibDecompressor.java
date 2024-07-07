package com.protoxon.mca.compression.decompressors;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

public class ZlibDecompressor implements Decompressor {

    /** decompresses data compressed with Zlib
     *
     * @param data the compressed data
     * @return byte[] of uncompressed data
     */
    @Override
    public byte[] decompress(byte[] data) throws IOException {
        try (InflaterInputStream inflaterInputStream = new InflaterInputStream(new ByteArrayInputStream(data))) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];//read 1024 bytes at a time
            int bytesRead;
            while ((bytesRead = inflaterInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        }
    }
}