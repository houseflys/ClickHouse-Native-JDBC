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

package com.github.housepower.data;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.github.housepower.client.NativeContext;
import com.github.housepower.data.type.*;
import com.github.housepower.data.type.complex.*;
import com.github.housepower.misc.LRUCache;
import com.github.housepower.misc.SQLLexer;
import com.github.housepower.misc.Validate;
import com.github.housepower.settings.ClickHouseDefines;

public class DataTypeFactory {
    private static final LRUCache<String, IDataType<?, ?>> DATA_TYPE_CACHE = new LRUCache<>(ClickHouseDefines.DATA_TYPE_CACHE_SIZE);

    public static IDataType<?, ?> get(String type, NativeContext.ServerContext serverContext) throws SQLException {
        IDataType<?, ?> dataType = DATA_TYPE_CACHE.get(type);
        if (dataType != null) {
            DATA_TYPE_CACHE.put(type, dataType);
            return dataType;
        }

        SQLLexer lexer = new SQLLexer(0, type);
        dataType = get(lexer, serverContext);
        Validate.isTrue(lexer.eof());

        DATA_TYPE_CACHE.put(type, dataType);
        return dataType;
    }

    private static final Map<String, IDataType<?, ?>> dataTypes = initialDataTypes();

    public static IDataType<?, ?> get(SQLLexer lexer, NativeContext.ServerContext serverContext) throws SQLException {
        String dataTypeName = String.valueOf(lexer.bareWord());

        if (dataTypeName.equalsIgnoreCase("Tuple")) {
            return DataTypeTuple.creator.createDataType(lexer, serverContext);
        } else if (dataTypeName.equalsIgnoreCase("Array")) {
            return DataTypeArray.creator.createDataType(lexer, serverContext);
        } else if (dataTypeName.equalsIgnoreCase("Enum8")) {
            return DataTypeEnum8.creator.createDataType(lexer, serverContext);
        } else if (dataTypeName.equalsIgnoreCase("Enum16")) {
            return DataTypeEnum16.creator.createDataType(lexer, serverContext);
        } else if (dataTypeName.equalsIgnoreCase("DateTime")) {
            return DataTypeDateTime.creator.createDataType(lexer, serverContext);
        } else if (dataTypeName.equalsIgnoreCase("DateTime64")) {
            return DataTypeDateTime64.creator.createDataType(lexer, serverContext);
        } else if (dataTypeName.equalsIgnoreCase("Nullable")) {
            return DataTypeNullable.creator.createDataType(lexer, serverContext);
        } else if (dataTypeName.equalsIgnoreCase("FixedString") || dataTypeName.equals("Binary")) {
            return DataTypeFixedString.creator.createDataType(lexer, serverContext);
        } else if (dataTypeName.equalsIgnoreCase("Decimal")) {
            return DataTypeDecimal.creator.createDataType(lexer, serverContext);
        } else if (dataTypeName.equalsIgnoreCase("String")) {
            return DataTypeString.CREATOR.createDataType(lexer, serverContext);
        } else if (dataTypeName.equalsIgnoreCase("Nothing")) {
            return DataTypeNothing.CREATOR.createDataType(lexer, serverContext);
        } else if (dataTypeName.equalsIgnoreCase("Map")) {
            return DataTypeMap.CREATOR.createDataType(lexer, serverContext);
        }
        else {
            IDataType<?, ?> dataType = dataTypes.get(dataTypeName.toLowerCase(Locale.ROOT));
            Validate.isTrue(dataType != null, "Unknown data type: " + dataTypeName);
            return dataType;
        }
    }


    /**
     * Some framework like Spark JDBC will convert all types name to lower case
     */
    private static Map<String, IDataType<?, ?>> initialDataTypes() {
        Map<String, IDataType<?, ?>> creators = new HashMap<>();

        registerType(creators, new DataTypeIPv4());
        registerType(creators, new DataTypeUUID());
        registerType(creators, new DataTypeFloat32());
        registerType(creators, new DataTypeFloat64());

        registerType(creators, new DataTypeInt8());
        registerType(creators, new DataTypeInt16());
        registerType(creators, new DataTypeInt32());
        registerType(creators, new DataTypeInt64());

        registerType(creators, new DataTypeUInt8());
        registerType(creators, new DataTypeUInt16());
        registerType(creators, new DataTypeUInt32());
        registerType(creators, new DataTypeUInt64());

        registerType(creators, new DataTypeDate());
        return creators;
    }

    private static void registerType(Map<String, IDataType<?, ?>> creators, IDataType<?, ?> type) {
        creators.put(type.name().toLowerCase(Locale.ROOT), type);
        for (String typeName : type.getAliases()) {
            creators.put(typeName.toLowerCase(Locale.ROOT), type);
        }
    }

    // TODO
    private static Map<String, DataTypeCreator<?, ?>> initComplexDataTypes() {
        return new HashMap<>();
    }

    private static void registerComplexType(
            Map<String, DataTypeCreator<?, ?>> creators, IDataType<?, ?> type, DataTypeCreator<?, ?> creator) {

        creators.put(type.name().toLowerCase(Locale.ROOT), creator);
        for (String typeName : type.getAliases()) {
            creators.put(typeName.toLowerCase(Locale.ROOT), creator);
        }
    }
}
