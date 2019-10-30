/*
 *
 * Copyright (c) 2018, 4ng and/or its affiliates. All rights reserved.
 * 4ENERGY PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */
package com.ghgande.j2mod.modbus.cmd;

import com.ghgande.j2mod.modbus.net.SerialConnection;

/**
 *
 */
public class GetCommsPorts {

    public static void main(String[] args) {

        for (String commPort : new SerialConnection().getCommPorts()) {
            System.out.println(commPort);
        }
    }
}
