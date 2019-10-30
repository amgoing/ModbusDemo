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
import com.ghgande.j2mod.modbus.net.AbstractUDPTerminal;
import com.ghgande.j2mod.modbus.net.UDPMasterConnection;
import com.ghgande.j2mod.modbus.util.ModbusUtil;

/**
 * Class implementing the <tt>ModbusTransaction</tt>
 * interface for the UDP transport mechanism.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class ModbusUDPTransaction extends ModbusTransaction {
    
    //instance attributes and associations
    private AbstractUDPTerminal terminal;
    private final Object MUTEX = new Object();

    /**
     * Constructs a new <tt>ModbusUDPTransaction</tt>
     * instance.
     */
    public ModbusUDPTransaction() {
    }

    /**
     * Constructs a new <tt>ModbusUDPTransaction</tt>
     * instance with a given <tt>ModbusRequest</tt> to
     * be send when the transaction is executed.
     * <p>
     *
     * @param request a <tt>ModbusRequest</tt> instance.
     */
    public ModbusUDPTransaction(ModbusRequest request) {
        setRequest(request);
    }

    /**
     * Constructs a new <tt>ModbusUDPTransaction</tt>
     * instance with a given <tt>UDPTerminal</tt> to
     * be used for transactions.
     * <p>
     *
     * @param terminal a <tt>UDPTerminal</tt> instance.
     */
    public ModbusUDPTransaction(AbstractUDPTerminal terminal) {
        setTerminal(terminal);
    }

    /**
     * Constructs a new <tt>ModbusUDPTransaction</tt>
     * instance with a given <tt>ModbusUDPConnection</tt>
     * to be used for transactions.
     * <p>
     *
     * @param con a <tt>ModbusUDPConnection</tt> instance.
     */
    public ModbusUDPTransaction(UDPMasterConnection con) {
        setTerminal(con.getTerminal());
    }

    /**
     * Sets the terminal on which this <tt>ModbusTransaction</tt>
     * should be executed.<p>
     *
     * @param terminal a <tt>UDPSlaveTerminal</tt>.
     */
    public void setTerminal(AbstractUDPTerminal terminal) {
        this.terminal = terminal;
        if (terminal.isActive()) {
            transport = terminal.getTransport();
        }
    }

    @Override
    public void execute() throws ModbusIOException, ModbusSlaveException, ModbusException {

        //1. assert executeability
        assertExecutable();
        //2. open the connection if not connected
        if (!terminal.isActive()) {
            try {
                terminal.activate();
                transport = terminal.getTransport();
            }
            catch (Exception ex) {
                Log.d("Terminal failed.", ex.toString());
                throw new ModbusIOException("Activation failed");
            }
        }

        //3. Retry transaction retries times, in case of
        //I/O Exception problems.
        int retryCount = 0;
        while (retryCount <= retries) {
            try {
                //3. write request, and read response,
                //   while holding the lock on the IO object
                synchronized (MUTEX) {
                    //write request message
                    transport.writeRequest(request);
                    //read response message
                    response = transport.readResponse();
                    break;
                }
            }
            catch (ModbusIOException ex) {
                retryCount++;
                if (retryCount > retries) {
                    Log.e("Cannot send UDP message", ex.toString());
                }
                else {
                    ModbusUtil.sleep(getRandomSleepTime(retryCount));
                }
            }
        }

        //4. deal with "application level" exceptions
        if (response instanceof ExceptionResponse) {
            throw new ModbusSlaveException(((ExceptionResponse)response).getExceptionCode());
        }

        // Check that the response is for this request
        if (isCheckingValidity()) {
            checkValidity();
        }

        //toggle the id
        incrementTransactionID();
    }

    /**
     * Asserts if this <tt>ModbusTCPTransaction</tt> is
     * executable.
     *
     * @throws ModbusException if this transaction cannot be
     *                         asserted as executable.
     */
    private void assertExecutable() throws ModbusException {
        if (request == null || terminal == null) {
            throw new ModbusException("Assertion failed, transaction not executable");
        }
    }

    /**
     * Toggles the transaction identifier, to ensure
     * that each transaction has a distinctive
     * identifier.<br>
     * When the maximum value of 65535 has been reached,
     * the identifiers will start from zero again.
     */
    private void incrementTransactionID() {
        if (isCheckingValidity()) {
            if (transactionID >= Modbus.MAX_TRANSACTION_ID) {
                transactionID = Modbus.DEFAULT_TRANSACTION_ID;
            }
            else {
                transactionID++;
            }
        }
        request.setTransactionID(getTransactionID());
    }
}