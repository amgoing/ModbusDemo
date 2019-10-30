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
package com.ghgande.j2mod.modbus.procimg;


import java.util.Vector;

/**
 * @author Julie
 *
 *         FIFO -- an abstraction of a Modbus FIFO, as supported by the
 *         READ FIFO command.
 *
 *         The FIFO class is only intended to be used for testing purposes and does
 *         not reflect the actual behavior of a FIFO in a real Modbus device.  In an
 *         actual Modbus device, the FIFO is mapped within a fixed address.
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class FIFO {

    private int address;
    private int registerCount;
    private Vector<Register> registers;

    public FIFO(int address) {
        this.address = address;
        registerCount = 0;
        registers = new Vector<Register>();
    }

    public synchronized int getRegisterCount() {
        return registerCount;
    }

    public synchronized Register[] getRegisters() {
        Register result[] = new Register[registerCount + 1];

        result[0] = new SimpleRegister(registerCount);
        for (int i = 0; i < registerCount; i++) {
            result[i + 1] = registers.get(i);
        }

        return result;
    }

    public synchronized void pushRegister(Register register) {
        if (registerCount == 31) {
            registers.remove(0);
        }
        else {
            registerCount++;
        }

        registers.add(new SimpleRegister(register.getValue()));
    }

    public synchronized void resetRegisters() {
        registers.removeAllElements();
        registerCount = 0;
    }

    public int getAddress() {
        return address;
    }
}
