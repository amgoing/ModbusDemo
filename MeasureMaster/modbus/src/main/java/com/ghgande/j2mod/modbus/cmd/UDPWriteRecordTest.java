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
package com.ghgande.j2mod.modbus.cmd;

import android.util.Log;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.ModbusSlaveException;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.io.ModbusUDPTransaction;
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.msg.ReadFileRecordRequest.RecordRequest;
import com.ghgande.j2mod.modbus.msg.ReadFileRecordResponse.RecordResponse;
import com.ghgande.j2mod.modbus.net.UDPMasterConnection;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * WriteRecordText -- Exercise the "WRITE FILE RECORD" Modbus
 * message.
 *
 * @author Julie
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class UDPWriteRecordTest {
    
    /**
     * usage -- Print command line arguments and exit.
     */
    private static void usage() {
        System.out.printf("Usage: UDPWriteRecordTest address[:port[:unit]] file record registers [count]");

        System.exit(1);
    }

    public static void main(String[] args) {
        InetAddress ipAddress = null;
        int port = Modbus.DEFAULT_PORT;
        int unit = 0;
        UDPMasterConnection connection;
        ReadFileRecordRequest rdRequest;
        ReadFileRecordResponse rdResponse;
        WriteFileRecordRequest wrRequest;
        WriteFileRecordResponse wrResponse;
        ModbusTransaction trans;
        int file = 0;
        int record = 0;
        int registers = 0;
        int requestCount = 1;

        // Get the command line parameters.
        if (args.length < 4 || args.length > 5) {
            usage();
        }

        String serverAddress = args[0];
        String parts[] = serverAddress.split(" *: *");
        String hostName = parts[0];

        try {
            /*
             * Address is of the form
             *
             * hostName:port:unitNumber
             *
             * where
             *
             * hostName -- Standard text host name
             * port        -- Modbus port, 502 is the default
             * unit        -- Modbus unit number, 0 is the default
             */
            if (parts.length > 1) {
                port = Integer.parseInt(parts[1]);

                if (parts.length > 2) {
                    unit = Integer.parseInt(parts[2]);
                }
            }
            ipAddress = InetAddress.getByName(hostName);

            file = Integer.parseInt(args[1]);
            record = Integer.parseInt(args[2]);
            registers = Integer.parseInt(args[3]);

            if (args.length > 4) {
                requestCount = Integer.parseInt(args[4]);
            }
        }
        catch (NumberFormatException x) {
            System.out.printf("Invalid parameter");
            usage();
        }
        catch (UnknownHostException x) {
            System.out.printf("Unknown host: %s", hostName);
            System.exit(1);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            usage();
            System.exit(1);
        }

        try {

            // Setup the UDP connection to the Modbus/UDP Master
            connection = new UDPMasterConnection(ipAddress);
            connection.setPort(port);
            connection.connect();
            connection.setTimeout(500);

            System.out.printf("Connected to %s:%d", ipAddress.toString(), connection.getPort());

            for (int i = 0; i < requestCount; i++) {
                // Setup the READ FILE RECORD request.  The record number
                // will be incremented for each loop.
                rdRequest = new ReadFileRecordRequest();
                rdRequest.setUnitID(unit);

                RecordRequest recordRequest = new RecordRequest(file, record + i, registers);
                rdRequest.addRequest(recordRequest);

                System.out.printf("Request: %s", rdRequest.getHexMessage());

                // Setup the transaction.
                trans = new ModbusUDPTransaction(connection);
                trans.setRequest(rdRequest);

                // Execute the transaction.
                try {
                    trans.execute();
                }
                catch (ModbusSlaveException x) {
                    Log.e("Slave Exception: {}", x.getLocalizedMessage());
                    continue;
                }
                catch (ModbusIOException x) {
                    Log.e("I/O Exception: {}", x.getLocalizedMessage());
                    continue;
                }
                catch (ModbusException x) {
                    Log.e("Modbus Exception: {}", x.getLocalizedMessage());
                    continue;
                }

                short values[];

                wrRequest = new WriteFileRecordRequest();
                wrRequest.setUnitID(unit);

                ModbusResponse dummy = trans.getResponse();
                if (dummy == null) {
                    System.out.printf("No response for transaction %d", i);
                    continue;
                }
                if (dummy instanceof ExceptionResponse) {
                    ExceptionResponse exception = (ExceptionResponse)dummy;

                    System.out.printf(exception.toString());

                    continue;
                }
                else if (dummy instanceof ReadFileRecordResponse) {
                    rdResponse = (ReadFileRecordResponse)dummy;

                    System.out.printf("Response: %s", rdResponse.getHexMessage());

                    int count = rdResponse.getRecordCount();
                    for (int j = 0; j < count; j++) {
                        RecordResponse data = rdResponse.getRecord(j);
                        values = new short[data.getWordCount()];
                        for (int k = 0; k < data.getWordCount(); k++) {
                            values[k] = data.getRegister(k).toShort();
                        }

                        System.out.printf("read data[%d] = ", j, Arrays.toString(values));

                        WriteFileRecordRequest.RecordRequest wrData = new WriteFileRecordRequest.RecordRequest(file, record + i, values);
                        wrRequest.addRequest(wrData);
                    }
                }
                else {
                    // Unknown message.
                    System.out.printf("Unknown Response: %s", dummy.getHexMessage());
                }

                // Setup the transaction.
                trans = new ModbusUDPTransaction(connection);
                trans.setRequest(wrRequest);

                // Execute the transaction.
                try {
                    trans.execute();
                }
                catch (ModbusSlaveException x) {
                    Log.e("Slave Exception: {}", x.getLocalizedMessage());
                    continue;
                }
                catch (ModbusIOException x) {
                    Log.e("I/O Exception: {}", x.getLocalizedMessage());
                    continue;
                }
                catch (ModbusException x) {
                    Log.e("Modbus Exception: {}", x.getLocalizedMessage());
                    continue;
                }

                dummy = trans.getResponse();
                if (dummy == null) {
                    System.out.printf("No response for transaction %d", i);
                    continue;
                }
                if (dummy instanceof ExceptionResponse) {
                    ExceptionResponse exception = (ExceptionResponse)dummy;

                    System.out.printf(exception.toString());

                }
                else if (dummy instanceof WriteFileRecordResponse) {
                    wrResponse = (WriteFileRecordResponse)dummy;

                    System.out.printf("Response: %s", wrResponse.getHexMessage());

                    int count = wrResponse.getRequestCount();
                    for (int j = 0; j < count; j++) {
                        WriteFileRecordResponse.RecordResponse data = wrResponse.getRecord(j);
                        values = new short[data.getWordCount()];
                        for (int k = 0; k < data.getWordCount(); k++) {
                            values[k] = data.getRegister(k).toShort();
                        }

                        System.out.printf("write response data[%d] = %s", j, Arrays.toString(values));
                    }
                }
                else {
                    // Unknown message.
                    System.out.printf("Unknown Response: %s", dummy.getHexMessage());
                }
            }

            // Teardown the connection.
            connection.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
