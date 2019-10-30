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

import com.ghgande.j2mod.modbus.io.ModbusUDPTransport;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Class implementing a <tt>UDPMasterTerminal</tt>.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
class UDPMasterTerminal extends AbstractUDPTerminal {
    

    /**
     * Create a UDP master connection to the specified Internet address.
     *
     * @param addr Remote address to connect to
     */
    UDPMasterTerminal(InetAddress addr) {
        address = addr;
    }

    /**
     * Create an uninitialized UDP master connection.
     */
    public UDPMasterTerminal() {
    }

    @Override
    public synchronized void activate() throws Exception {
        if (!isActive()) {
            if (socket == null) {
                socket = new DatagramSocket();
            }
            Log.d("tag","UDPMasterTerminal::haveSocket():{}"+socket.toString());
            Log.d("tag","UDPMasterTerminal::raddr=:{}:rport:{}"+ address.toString()+port);

            socket.setReceiveBufferSize(1024);
            socket.setSendBufferSize(1024);
            socket.setSoTimeout(timeout);

            transport = new ModbusUDPTransport(this);
            active = true;
        }
        Log.d("tag","UDPMasterTerminal::activated");
    }

    @Override
    public synchronized void deactivate() {
        try {
            Log.d("tag","UDPMasterTerminal::deactivate()");
            if (socket != null) {
                socket.close();
            }
            transport = null;
            active = false;
        }
        catch (Exception ex) {
            Log.e("Error closing socket", ex.toString());
        }
    }

    @Override
    public void sendMessage(byte[] msg) throws Exception {
        DatagramPacket req = new DatagramPacket(msg, msg.length, address, port);
        socket.send(req);
    }

    @Override
    public byte[] receiveMessage() throws Exception {

        // The longest possible DatagramPacket is 256 bytes (Modbus message
        // limit) plus the 6 byte header.
        byte[] buffer = new byte[262];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.setSoTimeout(timeout);
        socket.receive(packet);
        return buffer;
    }

}
