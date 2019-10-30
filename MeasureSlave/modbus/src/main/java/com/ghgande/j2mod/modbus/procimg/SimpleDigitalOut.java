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
 * Class implementing a simple <tt>DigitalOut</tt>.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class SimpleDigitalOut implements DigitalOut {

    /**
     * Field for the digital out state.
     */
    protected boolean set;

    /**
     * Constructs a new <tt>SimpleDigitalOut</tt> instance.
     * It's state will be invalid.
     */
    public SimpleDigitalOut() {
    }

    /**
     * Constructs a new <tt>SimpleDigitalOut</tt> instance
     * with the given state.
     *
     * @param b true if set, false otherwise.
     */
    public SimpleDigitalOut(boolean b) {
        set(b);
    }

    public boolean isSet() {
        return set;
    }

    public void set(boolean b) {
        set = b;
    }

}
