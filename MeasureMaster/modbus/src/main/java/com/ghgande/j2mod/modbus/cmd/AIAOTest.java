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
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersResponse;
import com.ghgande.j2mod.modbus.msg.WriteSingleRegisterRequest;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;

import java.net.InetAddress;

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

/**
 * <p>
 * Class that implements a simple commandline tool which demonstrates how a
 * analog input can be bound with a analog output.
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
 * If the time period is exceeded, then the device might react by turning out
 * all signals of the I/O modules. After this timeout, the device might require
 * a reset message.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class AIAOTest {


    private static void printUsage() {
        System.out.printf("\nUsage:\n    java com.ghgande.j2mod.modbus.cmd.AIAOTest <address{:<port>} [String]> <register a_in [int16]> <register a_out [int16]>");
    }

    public static void main(String[] args) {

        InetAddress addr = null;
        TCPMasterConnection con = null;
        ModbusRequest ai_req;
        WriteSingleRegisterRequest ao_req;

        ModbusTransaction ai_trans;
        ModbusTransaction ao_trans;

        int ai_ref = 0;
        int ao_ref = 0;
        int port = Modbus.DEFAULT_PORT;
        int unit_in = 0;
        int unit_out = 0;

        // 1. Setup the parameters
        if (args.length < 3) {
            printUsage();
            System.exit(1);
        }
        try {

            try {
                String serverAddress = args[0];
                String parts[] = serverAddress.split(" *: *");

                String address = parts[0];
                if (parts.length > 1) {
                    port = Integer.parseInt(parts[1]);
                    if (parts.length > 2) {
                        unit_in = unit_out = Integer.parseInt(parts[2]);
                        if (parts.length > 3) {
                            unit_out = Integer.parseInt(parts[3]);
                        }
                    }
                }
                addr = InetAddress.getByName(address);
                ai_ref = Integer.parseInt(args[1]);
                ao_ref = Integer.parseInt(args[2]);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                printUsage();
                System.exit(1);
            }

            // 2. Open the connection
            con = new TCPMasterConnection(addr);
            con.setPort(port);
            con.connect();
            System.out.printf("Connected to %s:%d", addr.toString(), con.getPort());

            // 3. Prepare the requests
            ai_req = new ReadInputRegistersRequest(ai_ref, 1);
            ao_req = new WriteSingleRegisterRequest();
            ao_req.setReference(ao_ref);

            ai_req.setUnitID(unit_in);
            ao_req.setUnitID(unit_out);

            // 4. Prepare the transactions
            ai_trans = new ModbusTCPTransaction(con);
            ai_trans.setRequest(ai_req);
            ao_trans = new ModbusTCPTransaction(con);
            ao_trans.setRequest(ao_req);

            // 5. Prepare holders to update only on change
            SimpleRegister new_out = new SimpleRegister(0);
            ao_req.setRegister(new_out);
            int last_out = Integer.MIN_VALUE;

            // 5. Execute the transaction repeatedly
            do {
                ai_trans.execute();
                int new_in = ((ReadInputRegistersResponse)ai_trans.getResponse()).getRegister(0).getValue();

                // write only if differ
                if (new_in != last_out) {
                    new_out.setValue(new_in); // update register
                    ao_trans.execute();
                    last_out = new_in;
                    System.out.printf("Updated Output Register with value from Input Register");
                }
            } while (true);

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            // 6. Close the connection
            if (con != null) {
                con.close();
            }
        }
    }
}
