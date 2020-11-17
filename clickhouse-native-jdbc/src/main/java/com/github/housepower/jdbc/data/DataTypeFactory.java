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

import com.github.housepower.jdbc.connect.PhysicalInfo;
import com.github.housepower.jdbc.data.type.*;
import com.github.housepower.jdbc.data.type.complex.*;
import com.github.housepower.jdbc.misc.SQLLexer;
import com.github.housepower.jdbc.misc.StringView;
import com.github.housepower.jdbc.misc.Validate;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class DataTypeFactory {

    public static IDataType get(String type, PhysicalInfo.ServerInfo serverInfo) throws SQLException {
        SQLLexer lexer = new SQLLexer(0, type);
        IDataType dataType = get(lexer, serverInfo);
        Validate.isTrue(lexer.eof());
        return dataType;
    }

    private static final Map<String, IDataType> dataTypes = initialDataTypes();

    public static IDataType get(SQLLexer lexer, PhysicalInfo.ServerInfo serverInfo) throws SQLException {
        StringView dataTypeName = lexer.bareWord();

        if (dataTypeName.checkEquals("Date")) {
            return DataTypeDate.createDateType(lexer, serverInfo);
        } else if (dataTypeName.checkEquals("Tuple")) {
            return DataTypeTuple.createTupleType(lexer, serverInfo);
        } else if (dataTypeName.checkEquals("Array")) {
            return DataTypeArray.createArrayType(lexer, serverInfo);
        } else if (dataTypeName.checkEquals("Enum8")) {
            return DataTypeEnum8.createEnum8Type(lexer, serverInfo);
        } else if (dataTypeName.checkEquals("Enum16")) {
            return DataTypeEnum16.createEnum16Type(lexer, serverInfo);
        } else if (dataTypeName.checkEquals("DateTime")) {
            return DataTypeDateTime.createDateTimeType(lexer, serverInfo);
        } else if (dataTypeName.checkEquals("DateTime64")) {
            return DataTypeDateTime64.createDateTime64Type(lexer, serverInfo);
        } else if (dataTypeName.checkEquals("Nullable")) {
            return DataTypeNullable.createNullableType(lexer, serverInfo);
        } else if (dataTypeName.checkEquals("FixedString")) {
            return DataTypeFixedString.createFixedStringType(lexer, serverInfo);
        } else if (dataTypeName.checkEquals("Decimal")) {
            return DataTypeDecimal.createDecimalType(lexer, serverInfo);
        } else if (dataTypeName.checkEquals("String")) {
            return new DataTypeString(serverInfo);
        } else {
            String name = String.valueOf(dataTypeName);
            IDataType dataType = dataTypes.get(name);
            Validate.isTrue(dataType != null, "Unknown data type family:" + name);
            return dataType;
        }
    }

    private static Map<String, IDataType> initialDataTypes() {
        Map<String, IDataType> creators = new HashMap<>();

        creators.put("IPv4", new DataTypeIPv4());
        creators.put("UUID", new DataTypeUUID());
        creators.put("Float32", new DataTypeFloat32());
        creators.put("Float64", new DataTypeFloat64());

        creators.put("Int8", new DataTypeInt8("Int8"));
        creators.put("Int16", new DataTypeInt16("Int16"));
        creators.put("Int32", new DataTypeInt32("Int32"));
        creators.put("Int64", new DataTypeInt64("Int64"));
        creators.put("UInt8", new DataTypeInt8("UInt8"));
        creators.put("UInt16", new DataTypeInt16("UInt16"));
        creators.put("UInt32", new DataTypeInt32("UInt32"));
        creators.put("UInt64", new DataTypeInt64("UInt64"));

        return creators;
    }
}
