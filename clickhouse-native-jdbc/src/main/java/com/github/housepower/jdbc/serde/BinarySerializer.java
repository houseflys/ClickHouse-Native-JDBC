/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.housepower.jdbc.serde;

import com.github.housepower.jdbc.buffer.BuffedWriter;
import com.github.housepower.jdbc.buffer.CompressedBuffedWriter;
import com.github.housepower.jdbc.misc.Either;
import com.github.housepower.jdbc.settings.ClickHouseDefines;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class BinarySerializer {

    private final Either<BuffedWriter> either;
    private final boolean enableCompress;

    public BinarySerializer(BuffedWriter writer, boolean enableCompress) {
        this.enableCompress = enableCompress;
        BuffedWriter compressBuffer = null;
        if (enableCompress) {
            compressBuffer = new CompressedBuffedWriter(ClickHouseDefines.SOCKET_SEND_BUFFER_BYTES, writer);
        }
        either = new Either<>(writer, compressBuffer);
    }

    public void writeVarInt(long x) throws IOException {
        for (int i = 0; i < 9; i++) {
            byte byt = (byte) (x & 0x7F);

            if (x > 0x7F) {
                byt |= 0x80;
            }

            x >>= 7;
            either.get().writeBinary(byt);

            if (x == 0) {
                return;
            }
        }
    }

    public void writeByte(byte x) throws IOException {
        either.get().writeBinary(x);
    }

    public void writeBoolean(boolean x) throws IOException {
        writeVarInt((byte) (x ? 1 : 0));
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    public void writeShort(short i) throws IOException {
        // @formatter:off
        either.get().writeBinary((byte) ((i >> 0) & 0xFF));
        either.get().writeBinary((byte) ((i >> 8) & 0xFF));
        // @formatter:on
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    public void writeInt(int i) throws IOException {
        // @formatter:off
        either.get().writeBinary((byte) ((i >> 0)  & 0xFF));
        either.get().writeBinary((byte) ((i >> 8)  & 0xFF));
        either.get().writeBinary((byte) ((i >> 16) & 0xFF));
        either.get().writeBinary((byte) ((i >> 24) & 0xFF));
        // @formatter:on
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    public void writeLong(long i) throws IOException {
        // @formatter:off
        either.get().writeBinary((byte) ((i >> 0)  & 0xFF));
        either.get().writeBinary((byte) ((i >> 8)  & 0xFF));
        either.get().writeBinary((byte) ((i >> 16) & 0xFF));
        either.get().writeBinary((byte) ((i >> 24) & 0xFF));
        either.get().writeBinary((byte) ((i >> 32) & 0xFF));
        either.get().writeBinary((byte) ((i >> 40) & 0xFF));
        either.get().writeBinary((byte) ((i >> 48) & 0xFF));
        either.get().writeBinary((byte) ((i >> 56) & 0xFF));
        // @formatter:on
    }

    public void writeUTF8StringBinary(String utf8) throws IOException {
        writeStringBinary(utf8, StandardCharsets.UTF_8);
    }

    public void writeStringBinary(String data, Charset charset) throws IOException {
        byte[] bs = data.getBytes(charset);
        writeBytesBinary(bs);
    }

    public void writeBytesBinary(byte[] bs) throws IOException {
        writeVarInt(bs.length);
        either.get().writeBinary(bs);
    }

    public void flushToTarget(boolean force) throws IOException {
        either.get().flushToTarget(force);
    }

    public void maybeEnableCompressed() {
        if (enableCompress)
            either.select(true);
    }

    public void maybeDisableCompressed() throws IOException {
        if (enableCompress) {
            either.get().flushToTarget(true);
            either.select(false);
        }
    }

    public void writeFloat(float datum) throws IOException {
        int x = Float.floatToIntBits(datum);
        writeInt(x);
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    public void writeDouble(double datum) throws IOException {
        long x = Double.doubleToLongBits(datum);
        // @formatter:off
        either.get().writeBinary((byte) ((x >>> 0)  & 0xFF));
        either.get().writeBinary((byte) ((x >>> 8)  & 0xFF));
        either.get().writeBinary((byte) ((x >>> 16) & 0xFF));
        either.get().writeBinary((byte) ((x >>> 24) & 0xFF));
        either.get().writeBinary((byte) ((x >>> 32) & 0xFF));
        either.get().writeBinary((byte) ((x >>> 40) & 0xFF));
        either.get().writeBinary((byte) ((x >>> 48) & 0xFF));
        either.get().writeBinary((byte) ((x >>> 56) & 0xFF));
        // @formatter:on
    }

    public void writeBytes(byte[] bytes) throws IOException {
        either.get().writeBinary(bytes);
    }
}
