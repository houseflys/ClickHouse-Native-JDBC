package com.github.housepower.jdbc.tool;

import com.github.housepower.jdbc.AbstractITest;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class TestHarness extends AbstractITest {
    static final Logger LOG = LoggerFactory.getLogger(TestHarness.class);
    static final int RECORD_COUNT = (1 << 17);
    static Function<String, String> SUM_EXPR = s -> "sum(" + s + ")";
    static Function<String, String> MAX_EXPR = s -> "max(" + s + ")";

    private final String tableName;
    private final List<DataTypeApply> types = new ArrayList<>();

    public TestHarness() {
        tableName = "test_" + (new Random().nextLong() & 0xffffffffL);
        LOG.info("Create TestHarness with table: {}", tableName);
        initAllTypes();
    }

    private void initAllTypes() {
        types.add(new DataTypeApply(
                () -> "Int8",
                (i) -> i % 128,
                MAX_EXPR,
                (rows) -> 127.0)
        );

        types.add(new DataTypeApply(
                () -> "Int16",
                (i) -> i % 32768,
                MAX_EXPR,
                (rows) -> 32767.0)
        );

        types.add(new DataTypeApply(
                () -> "Int32",
                (i) -> 3,
                SUM_EXPR,
                (rows) -> rows * 3.0)
        );

        types.add(new DataTypeApply(
                () -> "Int64",
                (i) -> 4,
                SUM_EXPR,
                (rows) -> rows * 4.0)
        );

        types.add(new DataTypeApply(
                () -> "UInt8",
                (i) -> i % 256,
                MAX_EXPR,
                (rows) -> 255.0)
        );

        types.add(new DataTypeApply(
                () -> "UInt16",
                (i) -> i % 65536,
                MAX_EXPR,
                (rows) -> 65535.0)
        );

        types.add(new DataTypeApply(
                () -> "UInt32",
                (i) -> 3,
                SUM_EXPR,
                (rows) -> rows * 3.0)
        );

        types.add(new DataTypeApply(
                () -> "UInt64",
                (i) -> 4,
                SUM_EXPR,
                (rows) -> rows * 4.0)
        );

        types.add(new DataTypeApply(
                () -> "Float32",
                (i) -> 5.0f,
                SUM_EXPR,
                (rows) -> rows * 5.0)
        );

        types.add(new DataTypeApply(
                () -> "Float64",
                (i) -> 6.0,
                SUM_EXPR,
                (rows) -> rows * 6.0)
        );

        types.add(new DataTypeApply(
                () -> "Nullable(Float64)",
                (i) -> 6.0,
                SUM_EXPR,
                (rows) -> rows * 6.0)
        );

        types.add(new DataTypeApply(
                () -> "String",
                (i) -> "00" + i,
                (col) -> "sum(toInt64(" + col + ") % 4)",
                (rows) -> (rows / 4.0 * (1 + 2 + 3)))
        );

        types.add(new DataTypeApply(
                () -> "DateTime",
                (i) -> new Timestamp(i / 1000 * 1000),
                MAX_EXPR,
                (rows) -> new Timestamp(rows).getTime() / 1000 * 1.0)
        );

        types.add(new DataTypeApply(
                () -> "Array(String)",
                (i) -> new Object[]{"00" + i},
                (col) -> "sum(toInt64(" + col + "[1]) % 4)",
                (rows) -> (rows / 4.0 * (1 + 2 + 3)))
        );
    }

    public void create() throws Exception {
        StringBuilder sb = new StringBuilder("CREATE TABLE " + tableName + " (");
        for (int i = 0; i < types.size(); i++) {
            if (i != 0) {
                sb.append(",\n");
            }
            sb.append("col_").append(i).append(" ").append(types.get(i).name.get());
        }
        sb.append(" ) Engine=Memory");
        String sql = sb.toString();
        LOG.debug("CREATE TABLE DDL: \n{}", sql);

        withNewConnection(connection -> {
            Statement stmt = connection.createStatement();
            stmt.execute(sql);
        });
    }

    public void insert() throws Exception {
        String sql = insertSQL();
        withNewConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql);
            for (int row = 0; row < RECORD_COUNT; row++) {
                for (int i = 0; i < types.size(); i++) {
                    if (types.get(i).name.get().startsWith("Array(")) {
                        Array array =
                                connection.createArrayOf("text", (Object[]) types.get(i).data.apply(row));
                        stmt.setObject(i + 1, array);
                    } else {
                        stmt.setObject(i + 1, types.get(i).data.apply(row));
                    }
                }
                stmt.addBatch();
            }
            stmt.executeBatch();
        });
    }

    public void checkAggr() throws Exception {
        StringBuilder sqlBuilder = new StringBuilder("SELECT ");
        Double[] results = new Double[types.size()];

        for (int i = 0; i < types.size(); i++) {
            if (i != 0) {
                sqlBuilder.append(",\n");
            }
            sqlBuilder.append("toFloat64(");
            sqlBuilder.append(types.get(i).expr.apply("col_" + i));
            sqlBuilder.append(")");
            results[i] = types.get(i).aggr.apply(RECORD_COUNT);
        }
        sqlBuilder.append(" FROM ").append(tableName);
        String sql = sqlBuilder.toString();
        LOG.debug("AGGR SQL: \n{}", sql);

        withNewConnection(connection -> {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            assertTrue(rs.next());
            for (int i = 0; i < types.size(); i++) {
                Double result = rs.getDouble(i + 1);
                assertEquals("Check Aggr Error Type: " + types.get(i).name.get(),
                        results[i],
                        result);
            }
        });
    }

    public void checkItem() throws Exception {
        withNewConnection(connection -> {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);
            int r = 0;
            while (rs.next()) {
                for (int i = 0; i < types.size(); i++) {
                    if (types.get(i).data.apply(r) instanceof Number) {
                        Number expected = (Number) (types.get(i).data.apply(r));
                        Number actually = (Number) (rs.getObject(i + 1));

                        assertEquals("Check Item Error Type: " + types.get(i).name.get() + ", at row: " + r,
                                expected.intValue(),
                                actually.intValue());
                        continue;
                    } else if (types.get(i).data.apply(r) instanceof Object[]) {
                        Object[] expected = (Object[]) types.get(i).data.apply(r);
                        Object[] actually = (Object[]) rs.getArray(i + 1).getArray();

                        assertArrayEquals(
                                "Check Item Error Type: " + types.get(i).name.get() + ", at row: " + r,
                                expected,
                                actually);
                        continue;
                    }
                    assertEquals(
                            "Check Item Error Type: " + types.get(i).name.get() + ", at row: " + r,
                            types.get(i).data.apply(r),
                            rs.getObject(i + 1));
                }
                r++;
            }
        });
    }

    public void clean() throws Exception {
        String sql = "DROP TABLE IF EXISTS " + tableName;
        withNewConnection(connection -> {
            Statement stmt = connection.createStatement();
            stmt.execute(sql);
        });
    }

    public void removeType(String typeName) throws Exception {
        types.removeIf((item) -> item.name.get().equals(typeName));
    }

    public String getTableName() {
        return tableName;
    }

    public List<DataTypeApply> getTypes() {
        return types;
    }

    public String insertSQL() {
        List<String> cols = new ArrayList<>();
        List<String> quotas = new ArrayList<>();

        for (int i = 0; i < types.size(); i++) {
            cols.add("col_" + i);
            quotas.add("?");
        }

        return String.format(Locale.ROOT, "INSERT INTO %s (%s) VALUES (%s)",
                tableName,
                Joiner.on(",").join(cols),
                Joiner.on(",").join(quotas));
    }

    public static class DataTypeApply {

        Supplier<String> name;
        Function<Integer, Object> data;
        Function<String, String> expr;
        Function<Integer, Double> aggr;

        public DataTypeApply(Supplier<String> name, Function<Integer, Object> data,
                             Function<String, String> expr, Function<Integer, Double> aggr) {
            this.name = name;
            this.data = data;
            this.expr = expr;
            this.aggr = aggr;
        }
    }
}
