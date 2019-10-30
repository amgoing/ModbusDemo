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
 * Abstract class for a register.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public abstract class AbstractRegister implements Register {

    /**
     * The word (<tt>byte[2]</tt>) holding the register content.
     */
    protected byte[] register = new byte[2];

    public int getValue() {
        return ((register[0] & 0xff) << 8 | (register[1] & 0xff));
    }

    public int toUnsignedShort() {
        return ((register[0] & 0xff) << 8 | (register[1] & 0xff));
    }

    public short toShort() {
        return (short)((register[0] << 8) | (register[1] & 0xff));
    }

    public synchronized byte[] toBytes() {
        byte[] dest = new byte[register.length];
        System.arraycopy(register, 0, dest, 0, dest.length);
        return dest;
    }

    public void setValue(short s) {
        register[0] = (byte)(0xff & (s >> 8));
        register[1] = (byte)(0xff & s);
    }

    public void setValue(byte[] bytes) {
        if (bytes.length < 2) {
            throw new IllegalArgumentException();
        }
        else {
            register[0] = bytes[0];
            register[1] = bytes[1];
        }
    }

    public void setValue(int v) {
        register[0] = (byte)(0xff & (v >> 8));
        register[1] = (byte)(0xff & v);
    }

}