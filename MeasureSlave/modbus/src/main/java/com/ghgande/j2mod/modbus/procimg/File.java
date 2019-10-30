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
 *         File -- an abstraction of a Modbus File, as supported by the
 *         READ FILE RECORD and WRITE FILE RECORD commands.
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class File {

    private int fileNumber;
    private int record_Count;
    private Record records[];

    public File(int fileNumber, int records) {
        this.fileNumber = fileNumber;
        record_Count = records;
        this.records = new Record[records];
    }

    public int getFileNumber() {
        return fileNumber;
    }

    public int getRecordCount() {
        return record_Count;
    }

    public Record getRecord(int i) {
        if (i < 0 || i >= record_Count) {
            throw new IllegalAddressException();
        }

        return records[i];
    }

    public File setRecord(int i, Record record) {
        if (i < 0 || i >= record_Count) {
            throw new IllegalAddressException();
        }

        records[i] = record;

        return this;
    }
}
