/*
 * Copyright 2002-2016 jamod & j2mod development teams
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ghgande.j2mod.modbus.io;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class is a replacement for ByteArrayInputStream that does not
 * synchronize every byte read.
 *
 * @author Mark Hayes
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class FastByteArrayInputStream extends InputStream {

    
    /**
     * Number of bytes in the input buffer.
     */
    protected int count;

    /**
     * Actual position pointer into the input buffer.
     */
    int pos;

    /**
     * Marked position pointer into the input buffer.
     */
    int mark;
    /**
     * Input buffer <tt>byte[]</tt>.
     */
    byte[] buf;

    /**
     * Creates an input stream.
     *
     * @param buffer the data to read.
     */
    FastByteArrayInputStream(byte[] buffer) {
        buf = buffer;
        count = buffer.length;
        pos = 0;
        mark = 0;
    }

    // --- begin ByteArrayInputStream compatible methods ---

    public int read() throws IOException {
        Log.d("count={} pos={}", count+" "+ pos);
        return (pos < count) ? (buf[pos++] & 0xff) : (-1);
    }

    public int read(byte[] toBuf) throws IOException {
        Log.d("read(byte[])",toBuf.toString());
        return read(toBuf, 0, toBuf.length);
    }

    public int read(byte[] toBuf, int offset, int length) throws IOException {
        Log.d("read(byte[],int,int)",toBuf.toString());
        int avail = count - pos;
        if (avail <= 0) {
            return -1;
        }
        if (length > avail) {
            length = avail;
        }
        for (int i = 0; i < length; i++) {
            toBuf[offset++] = buf[pos++];
        }
        return length;
    }

    public long skip(long count) {
        int myCount = (int)count;
        if (myCount + pos > this.count) {
            myCount = this.count - pos;
        }
        pos += myCount;
        return myCount;
    }

    public int available() {
        return count - pos;
    }

    public int getCount() {
        return count;
    }

    public void mark(int readlimit) {
        mark = pos;
        Log.d("mark={} pos={}", mark+" "+ pos);
    }

    public void reset() {
        pos = mark;
        Log.d("mark={} pos={}", mark+" "+ pos);
    }

    public boolean markSupported() {
        return true;
    }

    // --- end ByteArrayInputStream compatible methods ---

    public byte[] toByteArray() {
        byte[] toBuf = new byte[count];
        System.arraycopy(buf, 0, toBuf, 0, count);
        return toBuf;
    }

    /**
     * Returns the underlying data being read.
     *
     * @return the underlying data.
     */
    public synchronized byte[] getBufferBytes() {
        byte[] dest = new byte[count];
        System.arraycopy(buf, 0, dest, 0, dest.length);
        return dest;
    }

    /**
     * Returns the offset at which data is being read from the buffer.
     *
     * @return the offset at which data is being read.
     */
    public int getBufferOffset() {
        return pos;
    }

    /**
     * Returns the end of the buffer being read.
     *
     * @return the end of the buffer.
     */
    public int getBufferLength() {
        return count;
    }

}
