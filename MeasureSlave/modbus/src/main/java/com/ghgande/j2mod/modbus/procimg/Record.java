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


/**
 * @author Julie
 *
 *         File -- an abstraction of a Modbus Record, as supported by the
 *         READ FILE RECORD and WRITE FILE RECORD commands.
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class Record {


    private int recordNumber;
    private int registerCount;
    private Register registers[];

    public Record(int recordNumber, int registers) {
        this.recordNumber = recordNumber;
        registerCount = registers;
        this.registers = new Register[registers];

        for (int i = 0; i < registerCount; i++) {
            this.registers[i] = new SimpleRegister(0);
        }
    }

    public int getRecordNumber() {
        return recordNumber;
    }

    public int getRegisterCount() {
        return registerCount;
    }

    public Register getRegister(int register) {
        if (register < 0 || register >= registerCount) {
            throw new IllegalAddressException();
        }

        return registers[register];
    }

    public Record setRegister(int ref, Register register) {
        if (ref < 0 || ref >= registerCount) {
            throw new IllegalAddressException();
        }

        registers[ref] = register;

        return this;
    }
}
