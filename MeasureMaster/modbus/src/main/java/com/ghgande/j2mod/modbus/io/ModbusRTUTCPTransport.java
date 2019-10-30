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

import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;

import java.net.Socket;

/**
 * Class that implements the ModbusRTU over tCP transport flavor.
 *
 * @author axuan
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class ModbusRTUTCPTransport extends ModbusTCPTransport {

    /**
     * Default constructor
     */
    public ModbusRTUTCPTransport() {
        // RTU over TCP is headless by default
        setHeadless();
    }

    /**
     * Constructs a new <tt>ModbusTransport</tt> instance, for a given
     * <tt>Socket</tt>.
     * <p>
     *
     * @param socket the <tt>Socket</tt> used for message transport.
     */
    public ModbusRTUTCPTransport(Socket socket) {
        super(socket);
        // RTU over TCP is headless by default
        setHeadless();
    }

    @Override
    public void writeResponse(ModbusResponse msg) throws ModbusIOException {
        writeMessage(msg, true);
    }

    @Override
    public void writeRequest(ModbusRequest msg) throws ModbusIOException {
        writeMessage(msg, true);
    }
}
