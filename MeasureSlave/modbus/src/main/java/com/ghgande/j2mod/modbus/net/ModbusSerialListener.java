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
package com.ghgande.j2mod.modbus.net;

import android.util.Log;

import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransport;
import com.ghgande.j2mod.modbus.util.SerialParameters;

/**
 * Class that implements a ModbusSerialListener.<br>
 * If listening, it accepts incoming requests passing them on to be handled.
 *
 * @author Dieter Wimberger
 * @author Julie Haugh Code cleanup in prep to refactor with ModbusListener
 *         interface
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class ModbusSerialListener extends AbstractModbusListener {

    private AbstractSerialConnection serialCon;

    /**
     * Constructs a new <tt>ModbusSerialListener</tt> instance.
     *
     * @param params a <tt>SerialParameters</tt> instance.
     */
    public ModbusSerialListener(SerialParameters params) {
        serialCon = new SerialConnection(params);
    }

    /**
     * Constructs a new <tt>ModbusSerialListener</tt> instance specifying the serial connection interface
     *
     * @param serialCon Serial connection to use
     */
    public ModbusSerialListener(AbstractSerialConnection serialCon) {
        this.serialCon = serialCon;
    }

    @Override
    public void setTimeout(int timeout) {
        super.setTimeout(timeout);
        if (serialCon != null && listening) {
            ModbusSerialTransport transport = (ModbusSerialTransport)serialCon.getModbusTransport();
            if (transport != null) {
                transport.setTimeout(timeout);
            }
        }
    }

    @Override
    public void run() {

        // Set a suitable thread name
        if (threadName == null || threadName.isEmpty()) {
            threadName = String.format("Modbus Serial Listener [port:%s]", serialCon.getDescriptivePortName());
        }
        Thread.currentThread().setName(threadName);

        try {
            serialCon.open();
        }
        // Catch any fatal errors and set the listening flag to false to indicate an error
        catch (Exception e) {
            error = String.format("Cannot start Serial listener - %s", e.getMessage());
            listening = false;
            return;
        }

        listening = true;
        try {
            AbstractModbusTransport transport = serialCon.getModbusTransport();
            while (listening) {
                try {
                    handleRequest(transport, this);
                }
                catch (ModbusIOException ex) {
                    Log.d("tag", ex.getMessage());
                }
            }
        }
        catch (Exception e) {
            Log.e("tag","Exception occurred while handling request."+e.toString());
        }
        finally {
            listening = false;
            serialCon.close();
        }
    }

    @Override
    public void stop() {
        if (serialCon != null) {
            serialCon.close();
        }
        listening = false;
    }

}
