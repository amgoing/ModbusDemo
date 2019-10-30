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

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.ModbusSlaveException;
import com.ghgande.j2mod.modbus.msg.ExceptionResponse;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.net.AbstractSerialConnection;
import com.ghgande.j2mod.modbus.util.ModbusUtil;


/**
 * Class implementing the <tt>ModbusTransaction</tt>
 * interface.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class ModbusSerialTransaction extends ModbusTransaction {

    
    //instance attributes and associations
    private int transDelayMS = Modbus.DEFAULT_TRANSMIT_DELAY;
    private final Object MUTEX = new Object();
    private long lastTransactionTimestamp = 0;

    /**
     * Constructs a new <tt>ModbusSerialTransaction</tt>
     * instance.
     */
    public ModbusSerialTransaction() {
    }

    /**
     * Constructs a new <tt>ModbusSerialTransaction</tt>
     * instance with a given <tt>ModbusRequest</tt> to
     * be send when the transaction is executed.
     * <p>
     *
     * @param request a <tt>ModbusRequest</tt> instance.
     */
    public ModbusSerialTransaction(ModbusRequest request) {
        setRequest(request);
    }

    /**
     * Constructs a new <tt>ModbusSerialTransaction</tt>
     * instance with a given <tt>ModbusRequest</tt> to
     * be send when the transaction is executed.
     *
     * @param con a <tt>TCPMasterConnection</tt> instance.
     */
    public ModbusSerialTransaction(AbstractSerialConnection con) {
        setSerialConnection(con);
    }

    /**
     * Sets the port on which this <tt>ModbusTransaction</tt>
     * should be executed.
     *
     * @param con a <tt>SerialConnection</tt>.
     */
    public void setSerialConnection(AbstractSerialConnection con) {
        synchronized (MUTEX) {
            transport = con.getModbusTransport();
        }
    }

    public void setTransport(ModbusSerialTransport transport) {
        synchronized (MUTEX) {
            this.transport = transport;
        }
    }

    /**
     * Get the TransDelayMS value.
     *
     * @return the TransDelayMS value.
     */
    public int getTransDelayMS() {
        return transDelayMS;
    }

    /**
     * Set the TransDelayMS value.
     *
     * @param newTransDelayMS The new TransDelayMS value.
     */
    public void setTransDelayMS(int newTransDelayMS) {
        this.transDelayMS = newTransDelayMS;
    }

    /**
     * Asserts if this <tt>ModbusTCPTransaction</tt> is
     * executable.
     *
     * @throws ModbusException if the transaction cannot be asserted.
     */
    private void assertExecutable() throws ModbusException {
        if (request == null || transport == null) {
            throw new ModbusException("Assertion failed, transaction not executable");
        }
    }

    @Override
    public void execute() throws ModbusException {
        //1. assert executeability
        assertExecutable();

        //3. write request, and read response,
        //   while holding the lock on the IO object
        int tries = 0;
        boolean finished = false;
        do {
            try {
                // Wait between adjacent requests
                ((ModbusSerialTransport) transport).waitBetweenFrames(transDelayMS, lastTransactionTimestamp);

                synchronized (MUTEX) {
                    //write request message
                    transport.writeRequest(request);
                    //read response message
                    response = transport.readResponse();
                    finished = true;
                }
            }
            catch (ModbusIOException e) {
                if (++tries >= retries) {
                    throw e;
                }
                ModbusUtil.sleep(getRandomSleepTime(tries));
                Log.d("Execute try {} error:{}", tries+""+e.getMessage());
            }
        } while (!finished);

        //4. deal with exceptions
        if (response instanceof ExceptionResponse) {
            throw new ModbusSlaveException(((ExceptionResponse) response).getExceptionCode());
        }

        // Check that the response is for this request
        if (isCheckingValidity()) {
            checkValidity();
        }

        // Set the last transaction timestamp
        lastTransactionTimestamp = System.nanoTime();
    }

}
