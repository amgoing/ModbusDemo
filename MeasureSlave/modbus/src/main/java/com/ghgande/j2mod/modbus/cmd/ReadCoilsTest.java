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

import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransport;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.msg.ReadCoilsRequest;
import com.ghgande.j2mod.modbus.msg.ReadCoilsResponse;
import com.ghgande.j2mod.modbus.net.ModbusMasterFactory;

/**
 * Class that implements a simple command line tool for reading a digital input.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class ReadCoilsTest {


    private static void printUsage() {
        System.out.printf("\nUsage:\n    java com.ghgande.j2mod.modbus.cmd.ReadDiscretesTest <connection [String]> <unit [int8]> <register [int16]> <bitcount [int16]> {<repeat [int]>}");
    }

    public static void main(String[] args) {
        ReadCoilsRequest req;
        ReadCoilsResponse res;
        AbstractModbusTransport transport = null;
        ModbusTransaction trans;
        int ref = 0;
        int count = 0;
        int repeat = 1;
        int unit = 0;

        try {

            // 1. Setup the parameters
            if (args.length < 4 || args.length > 5) {
                printUsage();
                System.exit(1);
            }
            else {
                try {
                    transport = ModbusMasterFactory.createModbusMaster(args[0]);

                    if (transport instanceof ModbusSerialTransport) {
                        transport.setTimeout(500);
                        if (System.getProperty("com.ghgande.j2mod.modbus.baud") != null) {
                            ((ModbusSerialTransport)transport).setBaudRate(Integer.parseInt(System.getProperty("com.ghgande.j2mod.modbus.baud")));
                        }
                        else {
                            ((ModbusSerialTransport)transport).setBaudRate(19200);
                        }
                    }

                    // There are a number of devices which won't initialize immediately
                    // after being opened.  Take a moment to let them come up.
                    Thread.sleep(2000);

                    unit = Integer.parseInt(args[1]);
                    ref = Integer.parseInt(args[2]);
                    count = Integer.parseInt(args[3]);
                    if (args.length == 5) {
                        repeat = Integer.parseInt(args[4]);
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    printUsage();
                    System.exit(1);
                }
            }

            req = new ReadCoilsRequest(ref, count);
            req.setUnitID(unit);
            System.out.printf("Request: %s", req.getHexMessage());

            // 4. Prepare the transaction
            trans = transport.createTransaction();
            trans.setRequest(req);

            if (trans instanceof ModbusTCPTransaction) {
                ((ModbusTCPTransaction)trans).setReconnecting(true);
            }

            // 5. Execute the transaction repeat times
            int k = 0;
            do {
                trans.execute();

                res = (ReadCoilsResponse)trans.getResponse();

                System.out.printf("Response: %s", res.getHexMessage());

                System.out.printf("Digital Inputs Status=%s", res.getCoils().toString());

                k++;
            } while (k < repeat);

            // 6. Close the connection
            transport.close();

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        System.exit(0);
    }
}