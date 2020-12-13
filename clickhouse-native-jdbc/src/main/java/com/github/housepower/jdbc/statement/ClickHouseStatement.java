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

import com.github.housepower.jdbc.ClickHouseConnection;
import com.github.housepower.jdbc.ClickHouseResultSet;
import com.github.housepower.jdbc.connect.NativeContext;
import com.github.housepower.jdbc.data.Block;
import com.github.housepower.jdbc.log.Logger;
import com.github.housepower.jdbc.log.LoggerFactory;
import com.github.housepower.jdbc.misc.Validate;
import com.github.housepower.jdbc.stream.QueryResult;
import com.github.housepower.jdbc.settings.ClickHouseConfig;
import com.github.housepower.jdbc.settings.SettingKey;
import com.github.housepower.jdbc.stream.ValuesInputFormat;
import com.github.housepower.jdbc.wrapper.SQLStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClickHouseStatement implements SQLStatement {

    private static final Logger LOG = LoggerFactory.getLogger(ClickHouseStatement.class);

    private static final Pattern VALUES_REGEX = Pattern.compile("[V|v][A|a][L|l][U|u][E|e][S|s]\\s*\\(");
    private static final Pattern SELECT_DB_TABLE = Pattern.compile("(?i)FROM\\s+(\\S+\\.)?(\\S+)");

    private ResultSet lastResultSet;
    protected Block block;
    protected final ClickHouseConnection connection;
    protected final NativeContext nativeContext;

    private ClickHouseConfig cfg;
    private long maxRows;
    private String db;
    private String table = "unknown";

    private int updateCount = -1;
    private boolean isClosed = false;

    public ClickHouseStatement(ClickHouseConnection connection, NativeContext nativeContext) {
        this.connection = connection;
        this.nativeContext = nativeContext;
        this.cfg = connection.cfg();
        this.db = cfg.database();
    }

    @Override
    public boolean execute(String query) throws SQLException {
        LOG.debug("execute: {}", query);
        return executeQuery(query) != null;
    }

    @Override
    public ResultSet executeQuery(String query) throws SQLException {
        LOG.debug("executeQuery: {}", query);
        executeUpdate(query);
        return getResultSet();
    }

    @Override
    public int executeUpdate(String query) throws SQLException {
        LOG.debug("executeUpdate: {}", query);
        cfg.settings().put(SettingKey.max_result_rows, maxRows);

        extractDBAndTableName(query);
        Matcher matcher = VALUES_REGEX.matcher(query);

        if (matcher.find() && query.trim().toUpperCase(Locale.ROOT).startsWith("INSERT")) {
            lastResultSet = null;
            String insertQuery = query.substring(0, matcher.end() - 1);
            block = getSampleBlock(insertQuery);
            block.initWriteBuffer();
            new ValuesInputFormat(matcher.end() - 1, query).fillBlock(block);
            updateCount = connection.sendInsertRequest(block);
            return updateCount;
        }

        updateCount = -1;
        QueryResult result = connection.sendQueryRequest(query, cfg);
        lastResultSet = new ClickHouseResultSet(this, cfg, db, table, result.header(), result.data());
        return 0;
    }

    @Override
    public int getUpdateCount() throws SQLException {
        LOG.debug("getUpdateCount: {}", updateCount);
        return updateCount;
    }

    @Override
    public ResultSet getResultSet() {
        LOG.debug("getResultSet: {}", lastResultSet);
        return lastResultSet;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        LOG.debug("getMoreResults");
        updateCount = -1;
        if (lastResultSet != null) {
            lastResultSet.close();
            lastResultSet = null;
        }
        return false;
    }

    @Override
    public int getMaxRows() throws SQLException {
        return (int) maxRows;
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        Validate.isTrue(max >= 0, "Illegal maxRows value: " + max);
        maxRows = max;
    }

    @Override
    public void close() throws SQLException {
        LOG.debug("close Statement");
        this.isClosed = true;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return this.isClosed;
    }

    @Override
    public void cancel() throws SQLException {
        LOG.debug("cancel Statement");
        // TODO send cancel request and clear responses
        this.close();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return ResultSet.CONCUR_READ_ONLY;
    }

    @Override
    public int getResultSetType() throws SQLException {
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return ResultSet.FETCH_FORWARD;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
    }

    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return null;
    }

    // JDBC returns timeout in seconds
    @Override
    public int getQueryTimeout() {
        return (int) cfg.queryTimeout().getSeconds();
    }

    @Override
    public void setQueryTimeout(int seconds) {
        this.cfg = cfg.withQueryTimeout(Duration.ofSeconds(seconds));
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {
    }

    @Override
    public void setCursorName(String name) throws SQLException {
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return 0;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isAssignableFrom(getClass())) {
            return iface.cast(this);
        }
        throw new SQLException("Cannot unwrap to " + iface.getName());
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(getClass());
    }

    protected Block getSampleBlock(final String insertQuery) throws SQLException {
        return connection.getSampleBlock(insertQuery);
    }

    private void extractDBAndTableName(String sql) {
        String upperSQL = sql.trim().toUpperCase(Locale.ROOT);
        if (upperSQL.startsWith("SELECT")) {
            Matcher m = SELECT_DB_TABLE.matcher(sql);
            if (m.find()) {
                if (m.groupCount() == 2) {
                    if (m.group(1) != null) {
                        db = m.group(1);
                    }
                    table = m.group(2);
                }
            }
        } else if (upperSQL.startsWith("DESC")) {
            db = "system";
            table = "columns";
        } else if (upperSQL.startsWith("SHOW")) {
            db = "system";
            table = upperSQL.contains("TABLES") ? "tables" : "databases";
        }
    }

    @Override
    public Logger logger() {
        return ClickHouseStatement.LOG;
    }
}
