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

package com.github.housepower.jdbc.data.type.complex;

import com.github.housepower.jdbc.connect.PhysicalInfo;
import com.github.housepower.jdbc.data.DataTypeFactory;
import com.github.housepower.jdbc.data.IDataType;
import com.github.housepower.jdbc.misc.SQLLexer;
import com.github.housepower.jdbc.misc.Validate;
import com.github.housepower.jdbc.serializer.BinaryDeserializer;
import com.github.housepower.jdbc.serializer.BinarySerializer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DataTypeNullable implements IDataType {

    public static IDataType createNullableType(SQLLexer lexer, PhysicalInfo.ServerInfo serverInfo) throws SQLException {
        Validate.isTrue(lexer.character() == '(');
        IDataType nestedType = DataTypeFactory.get(lexer, serverInfo);
        Validate.isTrue(lexer.character() == ')');
        return new DataTypeNullable(
                "Nullable(" + nestedType.name() + ")", nestedType, DataTypeFactory.get("UInt8", serverInfo));
    }

    private static final Short IS_NULL = 1;
    private static final Short NON_NULL = 0;

    private final String name;
    private final IDataType nestedDataType;
    private final IDataType nullMapDataType;

    public DataTypeNullable(String name, IDataType nestedDataType, IDataType nullMapIDataType) throws SQLException {
        this.name = name;
        this.nestedDataType = nestedDataType;
        this.nullMapDataType = nullMapIDataType;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int sqlTypeId() {
        return nestedDataType.sqlTypeId();
    }

    @Override
    public Object defaultValue() {
        return nestedDataType.defaultValue();
    }

    @Override
    public Class javaTypeClass() {
        return nestedDataType.javaTypeClass();
    }

    @Override
    public boolean nullable() {
        return true;
    }

    @Override
    public int getPrecision() {
        return 0;
    }

    @Override
    public int getScale() {
        return 0;
    }

    @Override
    public Object deserializeTextQuoted(SQLLexer lexer) throws SQLException {
        if (lexer.isCharacter('n') || lexer.isCharacter('N')) {
            Validate.isTrue(Character.toLowerCase(lexer.character()) == 'n');
            Validate.isTrue(Character.toLowerCase(lexer.character()) == 'u');
            Validate.isTrue(Character.toLowerCase(lexer.character()) == 'l');
            Validate.isTrue(Character.toLowerCase(lexer.character()) == 'l');
            return null;
        }
        return nestedDataType.deserializeTextQuoted(lexer);
    }

    @Override
    public void serializeBinary(Object data, BinarySerializer serializer) throws SQLException, IOException {
        if (data == null) {
            serializer.writeByte((byte) 1);
        } else {
            serializer.writeByte((byte) 0);
            this.nestedDataType.serializeBinary(data, serializer);
        }
    }

    public void serializeBinary(Object data, BinarySerializer serializer, List<Byte> offset) throws SQLException, IOException {
        offset.add(data == null ? (byte) 1 : 0);
        this.nestedDataType.serializeBinary(data == null ? nestedDataType.defaultValue() : data, serializer);
    }

    @Override
    public Object deserializeBinary(BinaryDeserializer deserializer) throws SQLException, IOException {
        boolean isNull = (deserializer.readByte() == (byte)1);
        if (isNull) {
            return null;
        }
        return this.nestedDataType.deserializeBinary(deserializer);
    }

    @Override
    public void serializeBinaryBulk(Object[] data, BinarySerializer serializer) throws SQLException, IOException {
        Short[] isNull = new Short[data.length];
        for (int i = 0; i < data.length; i++) {
            isNull[i] = (data[i] == null ? IS_NULL : NON_NULL);
            data[i] = data[i] == null ? nestedDataType.defaultValue() : data[i];
        }
        nullMapDataType.serializeBinaryBulk(isNull, serializer);
        nestedDataType.serializeBinaryBulk(data, serializer);
    }

    @Override
    public Object[] deserializeBinaryBulk(int rows, BinaryDeserializer deserializer) throws SQLException, IOException {
        Object[] nullMap = nullMapDataType.deserializeBinaryBulk(rows, deserializer);

        Object[] data = nestedDataType.deserializeBinaryBulk(rows, deserializer);
        for (int i = 0; i < nullMap.length; i++) {
            if (IS_NULL.equals(nullMap[i])) {
                data[i] = null;
            }
        }
        return data;
    }
}
