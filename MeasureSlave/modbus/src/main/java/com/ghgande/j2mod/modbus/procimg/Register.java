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
 * Interface defining a register.
 *
 * <p>
 * A register is read-write from slave and master or device side. Therefore
 * implementations have to be carefully designed for concurrency.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public interface Register extends InputRegister {

    /**
     * Sets the content of this <tt>Register</tt> from the given unsigned 16-bit
     * value (unsigned short).
     *
     * @param v the value as unsigned short (<tt>int</tt>).
     */
    void setValue(int v);

    /**
     * Sets the content of this register from the given signed 16-bit value
     * (short).
     *
     * @param s the value as <tt>short</tt>.
     */
    void setValue(short s);

    /**
     * Sets the content of this register from the given raw bytes.
     *
     * @param bytes the raw data as <tt>byte[]</tt>.
     */
    void setValue(byte[] bytes);
}
