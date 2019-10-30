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

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ReadInputDiscretesRequest;
import com.ghgande.j2mod.modbus.msg.ReadInputDiscretesResponse;
import com.ghgande.j2mod.modbus.msg.WriteCoilRequest;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;

import java.net.InetAddress;

/**
 * Class that implements a simple command line tool which demonstrates how a
 * digital input can be bound with a digital output.
 *
 * <p>Note that if you write to a remote I/O with a Modbus protocol stack,
 * it will most likely expect that the communication is <i>kept alive</i>
 * after the first write message.
 *
 * <p>This can be achieved either by sending any kind of message, or by
 * repeating the write message within a given period of time.
 *
 * <p>If the time period is exceeded, then the device might react by turning
 * off all signals of the I/O modules. After this timeout, the device might
 * require a reset message.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class DIDOTest {

    public static void main(String[] args) {

        InetAddress addr = null;
        TCPMasterConnection con = null;
        ModbusRequest di_req;
        WriteCoilRequest do_req;

        ModbusTCPTransaction di_trans;
        ModbusTCPTransaction do_trans;

        int di_ref = 0;
        int do_ref = 0;
        int port = Modbus.DEFAULT_PORT;

        try {

            // 1. Setup the parameters
            if (args.length < 3) {
                printUsage();
                System.exit(1);
            }
            else {
                try {
                    String astr = args[0];
                    int idx = astr.indexOf(':');
                    if (idx > 0) {
                        port = Integer.parseInt(astr.substring(idx + 1));
                        astr = astr.substring(0, idx);
                    }
                    addr = InetAddress.getByName(astr);
                    di_ref = Integer.parseInt(args[1]);
                    do_ref = Integer.parseInt(args[2]);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    printUsage();
                    System.exit(1);
                }
            }

            // 2. Open the connection
            con = new TCPMasterConnection(addr);
            con.setPort(port);
            con.connect();
            System.out.printf("Connected to %s:%d", addr.toString(), con.getPort());

            // 3. Prepare the requests
            di_req = new ReadInputDiscretesRequest(di_ref, 1);

            do_req = new WriteCoilRequest();
            do_req.setReference(do_ref);

            di_req.setUnitID(0);
            do_req.setUnitID(0);

            // 4. Prepare the transactions
            di_trans = new ModbusTCPTransaction(con);
            di_trans.setRequest(di_req);
            di_trans.setReconnecting(false);
            do_trans = new ModbusTCPTransaction(con);
            do_trans.setRequest(do_req);
            do_trans.setReconnecting(false);

            // 5. Holders for last states
            boolean last_out = false;
            boolean new_in;

            // 6. Execute the transactions repeatedly
            do {
                di_trans.execute();
                new_in = ((ReadInputDiscretesResponse)
                        di_trans.getResponse()).getDiscreteStatus(0);

                // write only if differ
                if (new_in != last_out) {
                    do_req.setCoil(new_in);
                    do_trans.execute();
                    last_out = new_in;
                    System.out.printf("Updated coil with state from DI");
                }
            } while (true);

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {

            // 7. Close the connection
            if (con != null) {
                con.close();
            }

        }
    }

    private static void printUsage() {
        System.out.printf("\nUsage:\n    java com.ghgande.j2mod.modbus.cmd.DIDOTest <address{:<port>} [String]> <register d_in [int16]> <register d_out [int16]>");
    }
}
