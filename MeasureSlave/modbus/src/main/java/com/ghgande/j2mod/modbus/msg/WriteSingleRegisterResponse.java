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
 * Class implementing a <tt>WriteSingleRegisterResponse</tt>.
 * The implementation directly correlates with the class 0
 * function <i>write single register (FC 6)</i>. It
 * encapsulates the corresponding response message.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class WriteSingleRegisterResponse
        extends ModbusResponse {

    //instance attributes
    private int reference;
    private int registerValue;

    /**
     * Constructs a new <tt>WriteSingleRegisterResponse</tt>
     * instance.
     */
    public WriteSingleRegisterResponse() {
        super();
        setDataLength(4);
        setFunctionCode(Modbus.WRITE_SINGLE_REGISTER);
    }

    /**
     * Constructs a new <tt>WriteSingleRegisterResponse</tt>
     * instance.
     *
     * @param reference the offset of the register written.
     * @param value     the value of the register.
     */
    public WriteSingleRegisterResponse(int reference, int value) {
        super();
        setReference(reference);
        setRegisterValue(value);
        setDataLength(4);
        setFunctionCode(Modbus.WRITE_SINGLE_REGISTER);
    }

    /**
     * Returns the value that has been returned in
     * this <tt>WriteSingleRegisterResponse</tt>.
     * <p>
     *
     * @return the value of the register.
     */
    public int getRegisterValue() {
        return registerValue;
    }

    /**
     * Sets the value that has been returned in the
     * response message.
     * <p>
     *
     * @param value the returned register value.
     */
    private void setRegisterValue(int value) {
        registerValue = value;
    }

    /**
     * Returns the reference of the register
     * that has been written to.
     * <p>
     *
     * @return the reference of the written register.
     */
    public int getReference() {
        return reference;
    }

    /**
     * Sets the reference of the register that has
     * been written to.
     * <p>
     *
     * @param ref the reference of the written register.
     */
    private void setReference(int ref) {
        reference = ref;
        //setChanged(true);
    }

    public void writeData(DataOutput dout) throws IOException {
        dout.write(getMessage());
    }

    public void readData(DataInput din) throws IOException {
        setReference(din.readUnsignedShort());
        setRegisterValue(din.readUnsignedShort());
        //update data length
        setDataLength(4);
    }

    public byte[] getMessage() {
        byte result[] = new byte[4];

        result[0] = (byte)((reference >> 8) & 0xff);
        result[1] = (byte)(reference & 0xff);
        result[2] = (byte)((registerValue >> 8) & 0xff);
        result[3] = (byte)(registerValue & 0xff);

        return result;
    }

}