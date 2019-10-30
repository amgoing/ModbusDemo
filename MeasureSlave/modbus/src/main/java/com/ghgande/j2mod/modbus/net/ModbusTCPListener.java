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

import com.ghgande.j2mod.modbus.util.ThreadPool;

import java.io.IOException;
import java.net.*;

/**
 * Class that implements a ModbusTCPListener.
 * <p>
 * If listening, it accepts incoming requests passing them on to be handled.
 * If not listening, silently drops the requests.
 *
 * @author Dieter Wimberger
 * @author Julie Haugh
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class ModbusTCPListener extends AbstractModbusListener {
    
    private ServerSocket serverSocket = null;
    private ThreadPool threadPool;
    private Thread listener;
    private boolean useRtuOverTcp;

    /**
     * Constructs a ModbusTCPListener instance.<br>
     *
     * @param poolsize the size of the <tt>ThreadPool</tt> used to handle incoming
     *                 requests.
     * @param addr     the interface to use for listening.
     */
    public ModbusTCPListener(int poolsize, InetAddress addr) {
        this(poolsize, addr, false);
    }

    /**
     * Constructs a ModbusTCPListener instance.<br>
     *
     * @param poolsize      the size of the <tt>ThreadPool</tt> used to handle incoming
     *                      requests.
     * @param addr          the interface to use for listening.
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     */
    public ModbusTCPListener(int poolsize, InetAddress addr, boolean useRtuOverTcp) {
        threadPool = new ThreadPool(poolsize);
        address = addr;
        this.useRtuOverTcp = useRtuOverTcp;
    }

    /**
     * /**
     * Constructs a ModbusTCPListener instance.  This interface is created
     * to listen on the wildcard address (0.0.0.0), which will accept TCP packets
     * on all available adapters/interfaces
     *
     * @param poolsize the size of the <tt>ThreadPool</tt> used to handle incoming
     *                 requests.
     */
    public ModbusTCPListener(int poolsize) {
        this(poolsize, false);
    }

    /**
     * /**
     * Constructs a ModbusTCPListener instance.  This interface is created
     * to listen on the wildcard address (0.0.0.0), which will accept TCP packets
     * on all available adapters/interfaces
     *
     * @param poolsize      the size of the <tt>ThreadPool</tt> used to handle incoming
     *                      requests.
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     */
    public ModbusTCPListener(int poolsize, boolean useRtuOverTcp) {
        threadPool = new ThreadPool(poolsize);
        try {
            address = InetAddress.getByAddress(new byte[]{0, 0, 0, 0});
        }
        catch (UnknownHostException ex) {
            // Can't happen -- size is fixed.
        }
        this.useRtuOverTcp = useRtuOverTcp;
    }

    @Override
    public void setTimeout(int timeout) {
        super.setTimeout(timeout);
        if (serverSocket != null && listening) {
            try {
                serverSocket.setSoTimeout(timeout);
            }
            catch (SocketException e) {
                Log.e("Cant set socket timeout", e.toString());
            }
        }
    }

    @Override
    public void run() {

        // Set a suitable thread name
        if (threadName == null || threadName.isEmpty()) {
            threadName = String.format("Modbus TCP Listener [port:%d]", port);
        }
        Thread.currentThread().setName(threadName);

        try {
            /*
             * A server socket is opened with a connectivity queue of a size
             * specified in int floodProtection. Concurrent login handling under
             * normal circumstances should be alright, denial of service
             * attacks via massive parallel program logins can probably be
             * prevented.
             */
            int floodProtection = 100;
            serverSocket = new ServerSocket(port, floodProtection, address);
            serverSocket.setSoTimeout(timeout);
            Log.e("Listening to{}(Port {})", serverSocket.toString()+port);
        }

        // Catch any fatal errors and set the listening flag to false to indicate an error
        catch (Exception e) {
            error = String.format("Cannot start TCP listener - %s", e.getMessage());
            listening = false;
            return;
        }

        listener = Thread.currentThread();
        listening = true;
        try {

            // Initialise the message handling pool
            threadPool.initPool(threadName);

            // Infinite loop, taking care of resources in case of a lot of
            // parallel logins
            while (listening) {
                Socket incoming;
                try {
                    incoming = serverSocket.accept();
                }
                catch (SocketTimeoutException e) {
                    continue;
                }
                Log.e("Mak new connection {}", incoming.toString());
                if (listening) {
                    TCPSlaveConnection slave = new TCPSlaveConnection(incoming, useRtuOverTcp);
                    slave.setTimeout(timeout);
                    threadPool.execute(new TCPConnectionHandler(this, slave));
                }
                else {
                    incoming.close();
                }
            }
        }
        catch (IOException e) {
            error = String.format("Problem starting listener - %s", e.getMessage());
        }
        finally {
            if (threadPool != null) {
                threadPool.close();
            }
        }
    }

    @Override
    public void stop() {
        listening = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (listener != null) {
                listener.join();
            }
            if (threadPool != null) {
                threadPool.close();
            }
        }
        catch (Exception ex) {
            Log.e("tag","Error while stopping ModbusTCPListener"+ex.toString());
        }
    }

}
