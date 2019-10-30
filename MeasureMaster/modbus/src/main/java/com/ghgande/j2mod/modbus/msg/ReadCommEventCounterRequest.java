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
import com.ghgande.j2mod.modbus.net.AbstractModbusListener;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Class implementing a <tt>Read MEI Data</tt> request.
 *
 * @author Julie Haugh (jfh@ghgande.com)
 * @author jfhaugh (jfh@ghgande.com)
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class ReadCommEventCounterRequest extends ModbusRequest {

    /**
     * Constructs a new <tt>Report Slave ID request</tt> instance.
     */
    public ReadCommEventCounterRequest() {
        super();

        setFunctionCode(Modbus.READ_COMM_EVENT_COUNTER);

        // There is no additional data in this request.
        setDataLength(0);
    }

    @Override
    public ModbusResponse getResponse() {
        return updateResponseWithHeader(new ReadCommEventCounterResponse());
    }

    @Override
    public ModbusResponse createResponse(AbstractModbusListener listener) {
        return createExceptionResponse(Modbus.ILLEGAL_FUNCTION_EXCEPTION);
    }

    /**
     * writeData -- output this Modbus message to dout.
     * @throws IOException If the data cannot be written
     */
    public void writeData(DataOutput dout) throws IOException {
        dout.write(getMessage());
    }

    /**
     * readData -- dummy function.  There is no additional data
     * to read.
     * @throws IOException If the data cannot be read
     */
    public void readData(DataInput din) throws IOException {
    }

    /**
     * getMessage
     * @return an empty array as there is no data for this request
     */
    public byte[] getMessage() {

        return new byte[0];
    }
}
