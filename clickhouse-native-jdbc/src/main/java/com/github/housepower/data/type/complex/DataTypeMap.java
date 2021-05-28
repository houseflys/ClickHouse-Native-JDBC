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

package com.github.housepower.data.type.complex;

import com.github.housepower.data.DataTypeFactory;
import com.github.housepower.data.IDataType;
import com.github.housepower.data.type.DataTypeInt64;
import com.github.housepower.jdbc.ClickHouseArray;
import com.github.housepower.misc.SQLLexer;
import com.github.housepower.misc.Validate;
import com.github.housepower.serde.BinaryDeserializer;
import com.github.housepower.serde.BinarySerializer;

import java.io.IOException;
import java.sql.SQLException;

public class DataTypeMap implements IDataType {

    private final String name;
    private final DataTypeArray nestedType;

    public IDataType getNestedType() {
        return  nestedType;
    }

    public static DataTypeCreator<Object, Object> CREATOR =
            (lexer, serverContext) -> {
                Validate.isTrue(lexer.character() == '(');
                IDataType key = DataTypeFactory.get(lexer, serverContext);
                Validate.isTrue(lexer.character() ==',');
                IDataType value = DataTypeFactory.get(lexer, serverContext);
                IDataType[]nestedDataTypes = new IDataType[]{key, value};
                Validate.isTrue(lexer.character() == ')');
                String mapType = "Map(String, String)";
                return new DataTypeMap(mapType,
                        new DataTypeArray(mapType,
                                new DataTypeTuple("Tuple",
                                        nestedDataTypes),
                                (DataTypeInt64) DataTypeFactory.get("Int64", serverContext)));
            };

    public DataTypeMap(String mapType, DataTypeArray array) {
        this.name = mapType;
        this.nestedType = array;
    }

    /*public static Object typeDefine(DataTypeMap type, Object obj) {
        // convert hashmap into ClickhouseArray[Clickhousedict[]]
        if(obj instanceof HashMap) {
            HashMap obj1 = (HashMap) obj;

        }
        return  type.nestedType.defaultValue();
       *//* String keyType = type.getKeyType();
        String valueType = type.getValueType();
        if (keyType.equals("String")) {
            if (valueType.equals("String")){
                return (HashMap<String, String>) obj;
            }
            if (valueType.equals("Integer")){
                return (HashMap<String, Integer>) obj;
            }
            if (valueType.equals("Array")){
                return (HashMap<String, Array>) obj;
            }
        }
        if (keyType.equals("Integer")) {
            if (valueType.equals("String")){
                return (HashMap<Integer, String>) obj;
            }
            if (valueType.equals("Integer")){
                return (HashMap<Integer, Integer>) obj;
            }
            if (valueType.equals("Array")){
                return (HashMap<Integer, Array>) obj;
            }
        }
        throw new IllegalArgumentException("Clickhouse map type only accept key type to be Integer or String and value type " +
                "to be Integer, String, Array");*//*
    }*/

    public IDataType getElementType() {
        return this.nestedType;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Class javaType() {
        return null;
    }

    @Override
    public int sqlTypeId() {
        return 0;
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
    public String serializeText(Object value) {
       return null;
    }

    @Override
    public void serializeBinary(Object data, BinarySerializer serializer) throws SQLException, IOException { }

    @Override
    public void serializeBinaryBulk(Object[] data, BinarySerializer serializer) throws SQLException, IOException { }

    @Override
    public Object deserializeText(SQLLexer lexer) throws SQLException {
        return null;
       /* Validate.isTrue(lexer.character() == '{');
        List<Object> arrayData = new ArrayList<>();
        for (; ; ) {
            if (lexer.isCharacter('}')) {
                lexer.character();
                break;
            }
            if (lexer.isCharacter(',')) {
                lexer.character();
            }
            arrayData.add(subNestedType.deserializeText(lexer));
        }
        return new ClickHouseArray(subNestedType, arrayData.toArray());*/
    }

    @Override
    public Object deserializeBinary(BinaryDeserializer deserializer) throws SQLException, IOException {
       /* Long offset = offsetIDataType.deserializeBinary(deserializer);
        Object[] data = subNestedType.deserializeBinaryBulk(offset.intValue(), deserializer);
        return new ClickHouseArray(subNestedType, data);*/
        return null;
    }

    @Override
    public Object[] deserializeBinaryBulk(int rows, BinaryDeserializer deserializer) throws SQLException, IOException {
        ClickHouseArray[] arrays = new ClickHouseArray[rows];
        if (rows == 0) {
            return arrays;
        }
        return nestedType.deserializeBinaryBulk(rows, deserializer);
        /*int[] offsets = Arrays.stream(offsetIDataType.deserializeBinaryBulk(rows, deserializer)).mapToInt(value -> ((Long) value).intValue()).toArray();
        ClickHouseArray res = new ClickHouseArray(subNestedType,
                subNestedType.deserializeBinaryBulk(offsets[rows - 1], deserializer));

        for (int row = 0, lastOffset = 0; row < rows; row++) {
            int offset = offsets[row];
            arrays[row] = res.slice(lastOffset, offset - lastOffset);
            lastOffset = offset;
        }
        return arrays;*/
    }
}
