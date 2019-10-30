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
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.ModbusSlaveException;
import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransport;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.msg.ExceptionResponse;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.msg.WriteFileRecordRequest;
import com.ghgande.j2mod.modbus.msg.WriteFileRecordRequest.RecordRequest;
import com.ghgande.j2mod.modbus.msg.WriteFileRecordResponse;
import com.ghgande.j2mod.modbus.msg.WriteFileRecordResponse.RecordResponse;
import com.ghgande.j2mod.modbus.net.ModbusMasterFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * ReadFileRecordText -- Exercise the "READ FILE RECORD" Modbus
 * message.
 *
 * @author Julie
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class WriteFileRecordTest {

    /**
     * usage -- Print command line arguments and exit.
     */
    private static void usage() {
        System.out.printf("Usage: WriteFileRecordTest connection unit file record value [value ...]");

        System.exit(1);
    }

    public static void main(String[] args) {
        AbstractModbusTransport transport = null;
        WriteFileRecordRequest request;
        WriteFileRecordResponse response;
        ModbusTransaction trans;
        int unit = 0;
        int file = 0;
        int record = 0;
        int registers;
        short values[] = null;
        boolean isSerial = false;

        // Get the command line parameters.
        if (args.length < 6) {
            usage();
        }

        try {
            transport = ModbusMasterFactory.createModbusMaster(args[0]);
            if (transport instanceof ModbusSerialTransport) {
                transport.setTimeout(500);
                ((ModbusSerialTransport)transport).setBaudRate(19200);
                isSerial = true;

                Thread.sleep(2000);
            }
            unit = Integer.parseInt(args[1]);
            file = Integer.parseInt(args[2]);
            record = Integer.parseInt(args[3]);

            if (args.length > 4) {
                registers = args.length - 4;
                values = new short[registers];

                for (int i = 0; i < registers; i++) {
                    values[i] = Short.parseShort(args[i + 4]);
                }
            }
        }
        catch (NumberFormatException x) {
            System.out.printf("Invalid parameter");
            usage();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            usage();
            System.exit(1);
        }

        try {
            // Setup the WRITE FILE RECORD request.
            request = new WriteFileRecordRequest();
            request.setUnitID(unit);
            if (isSerial) {
                request.setHeadless(true);
            }

            RecordRequest recordRequest = new RecordRequest(file, record, values);
            request.addRequest(recordRequest);

            System.out.printf("Request: %s", request.getHexMessage());

            // Setup the transaction.
            trans = transport.createTransaction();
            trans.setRequest(request);

            // Execute the transaction.
            try {
                trans.execute();
            }
            catch (ModbusSlaveException x) {
                System.out.printf("Slave Exception: %s", x.getLocalizedMessage());
                System.exit(1);
            }
            catch (ModbusIOException x) {
                System.out.printf("I/O Exception: %s", x.getLocalizedMessage());
                System.exit(1);
            }
            catch (ModbusException x) {
                System.out.printf("Modbus Exception: %s", x.getLocalizedMessage());
                System.exit(1);
            }

            ModbusResponse dummy = trans.getResponse();
            if (dummy == null) {
                System.out.printf("No response for transaction ");
                System.exit(1);
            }
            if (dummy instanceof ExceptionResponse) {
                ExceptionResponse exception = (ExceptionResponse)dummy;

                System.out.printf(exception.toString());
            }
            else if (dummy instanceof WriteFileRecordResponse) {
                response = (WriteFileRecordResponse)dummy;

                System.out.printf("Response: %s", response.getHexMessage());

                int count = response.getRequestCount();
                for (int j = 0; j < count; j++) {
                    RecordResponse data = response.getRecord(j);
                    values = new short[data.getWordCount()];
                    for (int k = 0; k < data.getWordCount(); k++) {
                        values[k] = data.getRegister(k).toShort();
                    }

                    System.out.printf("data[%d] = %s", j, Arrays.toString(values));
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            if (transport != null) {
                try {
                    transport.close();
                }
                catch (IOException e) {
                    // Do nothing.
                }
            }
        }
        System.exit(0);
    }
}
