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

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransaction;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransport;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.net.ModbusMasterFactory;

import java.io.IOException;

/**
 * Class that implements a simple command line tool for reading the coomunications
 * event counter.
 *
 * <p>
 * Note that if you read from a remote I/O with a Modbus protocol stack, it will
 * most likely expect that the communication is <i>kept alive</i> after the
 * first read message.
 *
 * <p>
 * This can be achieved either by sending any kind of message, or by repeating
 * the read message within a given period of time.
 *
 * <p>
 * If the time period is exceeded, then the device might react by turning off
 * all signals of the I/O modules. After this timeout, the device might require
 * a reset message.
 *
 * @author Julie Haugh
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class ReadCommEventCounterTest {


    private static void printUsage() {
        System.out.printf("\nUsage:\n    java com.ghgande.j2mod.modbus.cmd.ReadCommEventCounterTest <address{:port} [String]> <unit [int]> {<repeat [int]>}");
    }

    public static void main(String[] args) {
        AbstractModbusTransport transport = null;
        ModbusRequest req;
        ModbusTransaction trans = null;
        int repeat = 1;
        int unit = 0;

        // 1. Setup parameters
        if (args.length < 2) {
            printUsage();
            System.exit(1);
        }

        try {
            try {
                // 2. Open the connection.
                transport = ModbusMasterFactory.createModbusMaster(args[0]);
                if (transport == null) {
                    System.out.printf("Cannot open %s", args[0]);
                    System.exit(1);
                }

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

                if (args.length > 1) {
                    unit = Integer.parseInt(args[1]);
                }

                if (args.length > 2) {
                    repeat = Integer.parseInt(args[2]);
                }

            }
            catch (Exception ex) {
                ex.printStackTrace();
                printUsage();
                System.exit(1);
            }

            // 5. Execute the transaction repeat times

            for (int k = 0; k < repeat; k++) {
                // 3. Create the command.
                req = new ReadCommEventCounterRequest();
                req.setUnitID(unit);
                req.setHeadless(trans instanceof ModbusSerialTransaction);
                System.out.printf("Request: %s", req.getHexMessage());

                // 4. Prepare the transaction
                trans = transport.createTransaction();
                trans.setRequest(req);
                trans.setRetries(1);

                if (trans instanceof ModbusSerialTransaction) {
                    // 10ms interpacket delay.
                    ((ModbusSerialTransaction)trans).setTransDelayMS(10);
                }

                try {
                    trans.execute();
                }
                catch (ModbusException x) {
                    System.out.printf(x.getMessage());
                    continue;
                }
                ModbusResponse res = trans.getResponse();
                if (res != null) {
                    System.out.printf("Response: %s", res.getHexMessage());
                }
                else {
                    System.out.printf("No response to READ INPUT request");
                }
                if (res instanceof ExceptionResponse) {
                    ExceptionResponse exception = (ExceptionResponse)res;
                    System.out.printf(exception.toString());
                    continue;
                }

                if (!(res instanceof ReadCommEventCounterResponse)) {
                    continue;
                }

                ReadCommEventCounterResponse data = (ReadCommEventCounterResponse)res;
                System.out.printf("Status: %d, Events: %d", data.getStatus(), data.getEventCount());
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            // 6. Close the connection
            if (transport != null) {
                transport.close();
            }
        }
        catch (IOException e) {
            // Do nothing.
        }
        System.exit(0);
    }
}
