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

package com.github.housepower.jdbc.statement;

import com.github.housepower.client.NativeContext;
import com.github.housepower.data.Block;
import com.github.housepower.data.DataTypeFactory;
import com.github.housepower.data.IColumn;
import com.github.housepower.data.IDataType;
import com.github.housepower.data.type.*;
import com.github.housepower.data.type.complex.*;
import com.github.housepower.exception.ClickHouseSQLException;
import com.github.housepower.jdbc.ClickHouseArray;
import com.github.housepower.jdbc.ClickHouseConnection;
import com.github.housepower.jdbc.ClickHouseStruct;
import com.github.housepower.log.Logger;
import com.github.housepower.log.LoggerFactory;
import com.github.housepower.misc.BytesCharSeq;
import com.github.housepower.misc.DateTimeUtil;
import com.github.housepower.misc.ExceptionUtil;
import com.github.housepower.misc.Validate;
import com.github.housepower.stream.ValuesWithParametersNativeInputFormat;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.github.housepower.misc.ExceptionUtil.unchecked;

public class ClickHousePreparedInsertStatement extends AbstractPreparedStatement {

    private static final Logger LOG = LoggerFactory.getLogger(ClickHousePreparedInsertStatement.class);

    private static int computeQuestionMarkSize(String query, int start) throws SQLException {
        int param = 0;
        boolean inQuotes = false, inBackQuotes = false;
        for (int i = 0; i < query.length(); i++) {
            char ch = query.charAt(i);
            if (ch == '`') {
                inBackQuotes = !inBackQuotes;
            } else if (ch == '\'') {
                inQuotes = !inQuotes;
            } else if (!inBackQuotes && !inQuotes) {
                if (ch == '?') {
                    Validate.isTrue(i > start, "");
                    param++;
                }
            }
        }
        return param;
    }

    private final int posOfData;
    private final String fullQuery;
    private final String insertQuery;
    private boolean blockInit;

    public ClickHousePreparedInsertStatement(int posOfData,
                                             String fullQuery,
                                             ClickHouseConnection conn,
                                             NativeContext nativeContext) throws SQLException {
        super(conn, nativeContext, null);
        this.blockInit = false;
        this.posOfData = posOfData;
        this.fullQuery = fullQuery;
        this.insertQuery = fullQuery.substring(0, posOfData);

        initBlockIfPossible();
    }

    // paramPosition start with 1
    @Override
    public void setObject(int paramPosition, Object x) throws SQLException {
        initBlockIfPossible();
        int columnIdx = block.paramIdx2ColumnIdx(paramPosition - 1);
        IColumn column = block.getColumn(columnIdx);
        block.setObject(columnIdx, convertToCkDataType(column.type(), x));
    }

    @Override
    public boolean execute() throws SQLException {
        return executeQuery() != null;
    }

    @Override
    public int executeUpdate() throws SQLException {
        addParameters();
        int result = connection.sendInsertRequest(block);
        this.blockInit = false;
        this.block.initWriteBuffer();
        return result;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        executeUpdate();
        return null;
    }

    @Override
    public void addBatch() throws SQLException {
        addParameters();
    }

    @Override
    public void clearBatch() throws SQLException {
    }

    @Override
    public int[] executeBatch() throws SQLException {
        int rows = connection.sendInsertRequest(block);
        int[] result = new int[rows];
        Arrays.fill(result, 1);
        clearBatch();
        this.blockInit = false;
        this.block.initWriteBuffer();
        return result;
    }

    @Override
    public void close() throws SQLException {
        if (blockInit) {
            // Empty insert when close.
            this.connection.sendInsertRequest(new Block());
            this.blockInit = false;
            this.block.initWriteBuffer();
        }
        super.close();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(": ");
        try {
            sb.append(insertQuery).append(" (");
            for (int i = 0; i < block.columnCnt(); i++) {
                Object obj = block.getObject(i);
                if (obj == null) {
                    sb.append("?");
                } else if (obj instanceof Number) {
                    sb.append(obj);
                } else {
                    sb.append("'").append(obj).append("'");
                }
                if (i < block.columnCnt() - 1) {
                    sb.append(",");
                }
            }
            sb.append(")");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private void initBlockIfPossible() throws SQLException {
        if (this.blockInit) {
            return;
        }
        ExceptionUtil.rethrowSQLException(() -> {
            this.block = connection.getSampleBlock(insertQuery);
            this.block.initWriteBuffer();
            this.blockInit = true;
            new ValuesWithParametersNativeInputFormat(posOfData, fullQuery).fill(block);
        });
    }

    private void addParameters() throws SQLException {
        block.appendRow();
    }

    // TODO we actually need a type cast system rather than put all type cast stuffs here
    private Object convertToCkDataType(IDataType<?, ?> type, Object obj) throws ClickHouseSQLException {
        if (obj == null) {
            if (type.nullable() || type instanceof DataTypeNothing)
                return null;
            throw new ClickHouseSQLException(-1, "type[" + type.name() + "] doesn't support null value");
        }
        // put the most common cast at first to avoid `instanceof` test overhead
        if (type instanceof DataTypeString || type instanceof DataTypeFixedString) {
            if (obj instanceof CharSequence)
                return obj;
            if (obj instanceof byte[])
                return new BytesCharSeq((byte[]) obj);
            String objStr = obj.toString();
            LOG.debug("set value[{}]: {} on String Column", obj.getClass(), obj);
            return objStr;
        }
        if (type instanceof DataTypeDate) {
            if (obj instanceof java.util.Date)
                return ((Date) obj).toLocalDate();
            if (obj instanceof LocalDate)
                return obj;
        }
        // TODO support
        //   1. other Java8 time, i.e. OffsetDateTime, Instant
        //   2. unix timestamp, but in second or millisecond?
        if (type instanceof DataTypeDateTime || type instanceof DataTypeDateTime64) {
            if (obj instanceof Timestamp)
                return DateTimeUtil.toZonedDateTime((Timestamp) obj, tz);
            if (obj instanceof LocalDateTime)
                return ((LocalDateTime) obj).atZone(tz);
            if (obj instanceof ZonedDateTime)
                return obj;
        }
        if (type instanceof DataTypeInt8) {
            if (obj instanceof Number)
                return ((Number) obj).byteValue();
        }
        if (type instanceof DataTypeUInt8 || type instanceof DataTypeInt16) {
            if (obj instanceof Number)
                return ((Number) obj).shortValue();
        }
        if (type instanceof DataTypeUInt16 || type instanceof DataTypeInt32) {
            if (obj instanceof Number)
                return ((Number) obj).intValue();
        }
        if (type instanceof DataTypeUInt32 || type instanceof DataTypeInt64) {
            if (obj instanceof Number)
                return ((Number) obj).longValue();
        }
        if (type instanceof DataTypeUInt64) {
            if (obj instanceof BigInteger)
                return obj;
            if (obj instanceof BigDecimal)
                return ((BigDecimal) obj).toBigInteger();
            if (obj instanceof Number)
                return BigInteger.valueOf(((Number) obj).longValue());
        }
        if (type instanceof DataTypeFloat32) {
            if (obj instanceof Number)
                return ((Number) obj).floatValue();
        }
        if (type instanceof DataTypeFloat64) {
            if (obj instanceof Number)
                return ((Number) obj).doubleValue();
        }
        if (type instanceof DataTypeDecimal) {
            if (obj instanceof BigDecimal)
                return obj;
            if (obj instanceof BigInteger)
                return new BigDecimal((BigInteger) obj);
            if (obj instanceof Number)
                return ((Number) obj).doubleValue();
        }
        if (type instanceof DataTypeUUID) {
            if (obj instanceof UUID)
                return obj;
            if (obj instanceof String) {
                return UUID.fromString((String) obj);
            }
        }
        if (type instanceof DataTypeNothing) {
            return null;
        }
        if (type instanceof DataTypeNullable) {
            // handled null at first, so obj also not null here
            return convertToCkDataType(((DataTypeNullable) type).getNestedDataType(), obj);
        }
        if (type instanceof DataTypeArray) {
            if (obj instanceof Map) obj = mapAssembleToCkArray(obj);
            if (!(obj instanceof ClickHouseArray)) {
                throw new ClickHouseSQLException(-1, "require ClickHouseArray for column: " + type.name() + ", but found " + obj.getClass());
            }
            return ((ClickHouseArray) obj).mapElements(unchecked(this::convertToCkDataType));
        }
        if (type instanceof DataTypeTuple) {
            if (!(obj instanceof ClickHouseStruct)) {
                throw new ClickHouseSQLException(-1, "require ClickHouseStruct for column: " + type.name() + ", but found " + obj.getClass());
            }
            return ((ClickHouseStruct) obj).mapAttributes(((DataTypeTuple) type).getNestedTypes(), unchecked(this::convertToCkDataType));
        }
        LOG.debug("unhandled type: {}[{}]", type.name(), obj.getClass());
        return obj;
    }

    private Object mapAssembleToCkArray(Object obj) {
        if (obj instanceof ClickHouseArray) return obj;
        if (obj instanceof Map) {
            try {
                IDataType key = null;
                IDataType value = null;
                int count = 0;
                String tupleName = "";
                boolean first = true;
                Map<?, ?> map = (HashMap) (obj);
                ClickHouseStruct[] tupleArrays = new ClickHouseStruct[map.size()];
                for (Object o : map.keySet()) {
                    if (first) {
                        key = DataTypeFactory.get(o.getClass().getSimpleName(), connection.serverContext());
                        value = DataTypeFactory.get(map.get(o).getClass().getSimpleName(), connection.serverContext());
                        tupleName = "Tuple(" + key.name() + ", " + value.name() + ")";
                        first = false;
                    }
                    tupleArrays[count++] = new ClickHouseStruct(tupleName, new Object[]{o, map.get(o)});
                }
                return new ClickHouseArray(new DataTypeTuple(tupleName, new IDataType[]{key, value}),
                        tupleArrays);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        throw new IllegalArgumentException("can not transfer map into clickhouseArray");
    }
}
