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
package com.ghgande.j2mod.modbus.facade;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransaction;
import com.ghgande.j2mod.modbus.net.AbstractSerialConnection;
import com.ghgande.j2mod.modbus.net.SerialConnection;
import com.ghgande.j2mod.modbus.util.SerialParameters;

/**
 * Modbus/Serial Master facade.
 *
 * @author Dieter Wimberger
 * @author John Charlton
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class ModbusSerialMaster extends AbstractModbusMaster {

    private AbstractSerialConnection connection;
    private int transDelay = Modbus.DEFAULT_TRANSMIT_DELAY;

    /**
     * Constructs a new master facade instance for communication
     * with a given slave.
     *
     * @param param SerialParameters specifies the serial port parameters to use
     *              to communicate with the slave device network.
     */
    public ModbusSerialMaster(SerialParameters param) {
        this(param, Modbus.DEFAULT_TIMEOUT, Modbus.DEFAULT_TRANSMIT_DELAY);
    }

    /**
     * Constructs a new master facade instance for communication
     * with a given slave.
     *
     * @param param   SerialParameters specifies the serial port parameters to use
     *                to communicate with the slave device network.
     * @param timeout Receive timeout in milliseconds
     */
    public ModbusSerialMaster(SerialParameters param, int timeout) {
        this(param, timeout, Modbus.DEFAULT_TRANSMIT_DELAY);
    }

    /**
     * Constructs a new master facade instance for communication
     * with a given slave.
     *
     * @param param      SerialParameters specifies the serial port parameters to use
     *                   to communicate with the slave device network.
     * @param timeout    Receive timeout in milliseconds
     * @param transDelay The transmission delay to use between frames (milliseconds)
     */
    public ModbusSerialMaster(SerialParameters param, int timeout, int transDelay) {
        try {
            this.transDelay = transDelay > -1 ? transDelay : Modbus.DEFAULT_TRANSMIT_DELAY;
            connection = new SerialConnection(param);
            connection.setTimeout(timeout);
            this.timeout = timeout;
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public AbstractSerialConnection getConnection() {
        return connection;
    }

    /**
     * Connects this <tt>ModbusSerialMaster</tt> with the slave.
     *
     * @throws Exception if the connection cannot be established.
     */
    public void connect() throws Exception {
        if (connection != null && !connection.isOpen()) {
            connection.open();
            transaction = connection.getModbusTransport().createTransaction();
            ((ModbusSerialTransaction) transaction).setTransDelayMS(transDelay);
            setTransaction(transaction);
        }
    }

    /**
     * Disconnects this <tt>ModbusSerialMaster</tt> from the slave.
     */
    public void disconnect() {
        if (connection != null && connection.isOpen()) {
            connection.close();
            transaction = null;
            setTransaction(null);
        }
    }

    @Override
    public void setTimeout(int timeout) {
        super.setTimeout(timeout);
        if (connection != null) {
            connection.setTimeout(timeout);
        }
    }

    @Override
    public AbstractModbusTransport getTransport() {
        return connection == null ? null : connection.getModbusTransport();
    }
}