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
package com.ghgande.j2mod.modbus.msg;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.util.BitVector;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Class implementing a <tt>ReadCoilsResponse</tt>.
 * The implementation directly correlates with the class 1
 * function <i>read coils (FC 1)</i>. It encapsulates
 * the corresponding response message.
 * <p>
 * Coils are understood as bits that can be manipulated
 * (i.e. set or unset).
 *
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 */

/**
 * Completed re-implementation 1/10/2011
 *
 * Created getMessage() method to abstractly create the message
 * data.
 * Cleaned up the constructors.
 *
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class ReadCoilsResponse extends ModbusResponse {
    private BitVector coils;

    /**
     * ReadCoilsResponse -- create an empty response message to be
     * filled in later.
     */
    public ReadCoilsResponse() {
        setFunctionCode(Modbus.READ_COILS);
        setDataLength(1);
        coils = null;
    }

    /**
     * ReadCoilsResponse -- create a response for a given number of
     * coils.
     *
     * @param count the number of bits to be read.
     */
    public ReadCoilsResponse(int count) {
        setFunctionCode(Modbus.READ_COILS);
        coils = new BitVector(count);
        setDataLength(coils.byteSize() + 1);
    }

    /**
     * getBitCount -- return the number of coils
     *
     * @return number of defined coils
     */
    public int getBitCount() {
        if (coils == null) {
            return 0;
        }
        else {
            return coils.size();
        }
    }

    /**
     * getCoils -- get the coils bit vector.
     *
     * The coils vector may be read (when operating as a master) or
     * written (when operating as a slave).
     *
     * @return BitVector containing the coils.
     */
    public BitVector getCoils() {
        return coils;
    }

    /**
     * Convenience method that returns the state
     * of the bit at the given index.
     * <p>
     *
     * @param index the index of the coil for which
     *              the status should be returned.
     *
     * @return true if set, false otherwise.
     *
     * @throws IndexOutOfBoundsException if the
     *                                   index is out of bounds
     */
    public boolean getCoilStatus(int index) throws IndexOutOfBoundsException {

        if (index < 0) {
            throw new IllegalArgumentException(index + " < 0");
        }

        if (index > coils.size()) {
            throw new IndexOutOfBoundsException(index + " > " + coils.size());
        }

        return coils.getBit(index);
    }

    /**
     * Sets the status of the given coil.
     *
     * @param index the index of the coil to be set.
     * @param b     true if to be set, false for reset.
     */
    public void setCoilStatus(int index, boolean b) {
        if (index < 0) {
            throw new IllegalArgumentException(index + " < 0");
        }

        if (index > coils.size()) {
            throw new IndexOutOfBoundsException(index + " > " + coils.size());
        }

        coils.setBit(index, b);
    }

    public void writeData(DataOutput output) throws IOException {
        byte result[] = getMessage();

        output.write(result);
    }

    public void readData(DataInput input) throws IOException {
        int count = input.readUnsignedByte();
        byte[] data = new byte[count];

        input.readFully(data, 0, count);
        coils = BitVector.createBitVector(data);
        setDataLength(count + 1);
    }

    public byte[] getMessage() {
        int len = 1 + coils.byteSize();
        byte result[] = new byte[len];

        result[0] = (byte)coils.byteSize();
        System.arraycopy(coils.getBytes(), 0, result, 1, coils.byteSize());

        return result;
    }
}
