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

import com.ghgande.j2mod.modbus.util.Observable;

/**
 * Class implementing an observable register.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class ObservableRegister extends Observable implements Register {

    /**
     * The word holding the content of this register.
     */
    protected short register;

    synchronized public int getValue() {
        return register & 0xFFFF;
    }

    public int toUnsignedShort() {
        return register & 0xFFFF;
    }

    public short toShort() {
        return register;
    }

    public synchronized byte[] toBytes() {
        return new byte[]{(byte)(register >> 8), (byte)(register & 0xFF)};
    }

    public synchronized void setValue(short s) {
        register = s;
        notifyObservers("value");
    }

    public synchronized void setValue(byte[] bytes) {
        if (bytes.length < 2) {
            throw new IllegalArgumentException();
        }
        else {
            register = (short)(((short)((bytes[0] << 8))) | (((short)(bytes[1])) & 0xFF));
            notifyObservers("value");
        }
    }

    public synchronized void setValue(int v) {
        register = (short)v;
        notifyObservers("value");
    }
}