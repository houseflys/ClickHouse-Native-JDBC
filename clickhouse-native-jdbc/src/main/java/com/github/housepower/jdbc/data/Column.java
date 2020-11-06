/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.housepower.jdbc.data;

import com.github.housepower.jdbc.serializer.BinarySerializer;

import java.io.IOException;
import java.sql.SQLException;

public class Column extends AbstractColumn {
    protected Object[] values;

    public Column(String name, IDataType type, Object[] values) {
        super(name, type);
        this.values = values;
    }

    public String name() {
        return this.name;
    }

    public IDataType type() {
        return this.type;
    }

    @Override
    public Object values(int idx) {
        return values[idx];
    }

    @Override
    public void write(Object object) throws IOException, SQLException {
        type().serializeBinary(object, buffer.column);
    }

    @Override
    public void serializeBinaryBulk(BinarySerializer serializer) throws SQLException, IOException {
        serializer.writeStringBinary(name);
        serializer.writeStringBinary(type.name());
        buffer.writeTo(serializer);
    }

    @Override
    public void clear() {
        values = new Object[0];
    }

    public long size() {
        return values.length;
    }

    public ColumnWriterBuffer getBuffer() {
        return buffer;
    }
}
