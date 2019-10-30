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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Class implementing a <tt>WriteCoilResponse</tt>. The implementation directly
 * correlates with the class 0 function <i>write coil (FC 5)</i>. It
 * encapsulates the corresponding response message.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class WriteCoilResponse extends ModbusResponse {
    private boolean coil = false;
    private int reference;

    /**
     * Constructs a new <tt>WriteCoilResponse</tt> instance.
     */
    public WriteCoilResponse() {
        super();

        setFunctionCode(Modbus.WRITE_COIL);
        setDataLength(4);
    }

    /**
     * Constructs a new <tt>WriteCoilResponse</tt> instance.
     *
     * @param reference the offset were writing was started from.
     * @param b         the state of the coil; true set, false reset.
     */
    public WriteCoilResponse(int reference, boolean b) {
        super();

        setFunctionCode(Modbus.WRITE_COIL);
        setDataLength(4);

        setReference(reference);
        setCoil(b);
    }

    /**
     * Gets the state that has been returned in this <tt>WriteCoilRequest</tt>.
     *
     * @return true if the coil is set, false if unset.
     */
    public boolean getCoil() {
        return coil;
    }

    /**
     * Sets the state that has been returned in the raw response.
     *
     * @param b true if the coil should be set of false if it should be unset.
     */
    public void setCoil(boolean b) {
        coil = b;
    }

    /**
     * Returns the reference of the register of the coil that has been written
     * to with the request.
     * <p>
     *
     * @return the reference of the coil's register.
     */
    public int getReference() {
        return reference;
    }

    /**
     * Sets the reference of the register of the coil that has been written to
     * with the request.
     * <p>
     *
     * @param ref the reference of the coil's register.
     */
    public void setReference(int ref) {
        reference = ref;
    }

    public void writeData(DataOutput dout) throws IOException {
        dout.write(getMessage());
    }

    public void readData(DataInput din) throws IOException {
        byte data[] = new byte[4];
        din.readFully(data);

        setReference(((data[0] << 8) | (data[1] & 0xff)));
        setCoil(data[2] == Modbus.COIL_ON);

        setDataLength(4);
    }

    public byte[] getMessage() {
        byte result[] = new byte[4];

        result[0] = (byte)((reference >> 8) & 0xff);
        result[1] = (byte)(reference & 0xff);
        if (coil) {
            result[2] = Modbus.COIL_ON_BYTES[0];
            result[3] = Modbus.COIL_ON_BYTES[1];
        }
        else {
            result[2] = Modbus.COIL_OFF_BYTES[0];
            result[3] = Modbus.COIL_OFF_BYTES[1];
        }
        return result;
    }
}