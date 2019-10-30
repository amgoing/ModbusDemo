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
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.msg.WriteCoilRequest;
import com.ghgande.j2mod.modbus.msg.WriteCoilResponse;
import com.ghgande.j2mod.modbus.net.ModbusMasterFactory;

/**
 * <p>
 * Class that implements a simple commandline tool for writing to a digital
 * output.
 *
 * <p>
 * Note that if you write to a remote I/O with a Modbus protocol stack, it will
 * most likely expect that the communication is <i>kept alive</i> after the
 * first write message.
 *
 * <p>
 * This can be achieved either by sending any kind of message, or by repeating
 * the write message within a given period of time.
 *
 * <p>
 * If the time period is exceeded, then the device might react by turning off
 * all signals of the I/O modules. After this timeout, the device might require
 * a reset message.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class WriteCoilTest {

    private static void printUsage() {
        System.out.printf("\nUsage:\n    java com.ghgande.j2mod.modbus.cmd.WriteCoilTest <connection [String]> <unit [int8]> <coil [int16]> <state [boolean]> {<repeat [int]>}");
    }

    public static void main(String[] args) {
        WriteCoilRequest req;
        AbstractModbusTransport transport = null;
        ModbusTransaction trans;
        int ref = 0;
        boolean value = false;
        int repeat = 1;
        int unit = 0;

        // 1. Setup the parameters
        if (args.length < 4) {
            printUsage();
            System.exit(1);
        }
        try {
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
                value = "true".equals(args[3]);

                if (args.length == 5) {
                    repeat = Integer.parseInt(args[4]);
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
                printUsage();
                System.exit(1);
            }

            // 3. Prepare the request
            req = new WriteCoilRequest(ref, value);
            req.setUnitID(unit);
            System.out.printf("Request: %s", req.getHexMessage());

            // 4. Prepare the transaction
            trans = transport.createTransaction();
            trans.setRequest(req);

            // 5. Execute the transaction repeat times
            for (int count = 0; count < repeat; count++) {
                trans.execute();

                System.out.printf("Response: %s", trans.getResponse().getHexMessage());

                WriteCoilResponse data = (WriteCoilResponse)trans.getResponse();
                if (data != null) {
                    System.out.printf("Coil = %b", data.getCoil());
                }
            }

            // 6. Close the connection
            transport.close();

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        System.exit(0);
    }
}
