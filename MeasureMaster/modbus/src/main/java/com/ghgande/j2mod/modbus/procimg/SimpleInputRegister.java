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
 * Class implementing a simple <tt>InputRegister</tt>.
 * <p>
 * The <tt>setValue()</tt> method is synchronized, which ensures atomic access, * but no specific access order.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class SimpleInputRegister extends SynchronizedAbstractRegister implements InputRegister {

    /**
     * Constructs a new <tt>SimpleInputRegister</tt> instance. It's state will
     * be invalid.
     */
    public SimpleInputRegister() {
    }

    /**
     * Constructs a new <tt>SimpleInputRegister</tt> instance.
     *
     * @param b1 the first (hi) byte of the word.
     * @param b2 the second (low) byte of the word.
     */
    public SimpleInputRegister(byte b1, byte b2) {
        register[0] = b1;
        register[1] = b2;
    }

    /**
     * Constructs a new <tt>SimpleInputRegister</tt> instance with the given
     * value.
     *
     * @param value the value of this <tt>SimpleInputRegister</tt> as <tt>int</tt>
     *              .
     */
    public SimpleInputRegister(int value) {
        setValue(value);
    }// constructor(int)

    public String toString() {
        if (register == null) {
            return "invalid";
        }

        return getValue() + "";
    }

}
