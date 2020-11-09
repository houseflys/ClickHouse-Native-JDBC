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

package com.github.housepower.jdbc.wrapper;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Struct;
import java.util.Map;

public interface SQLStruct extends Struct {
    @Override
    default String getSQLTypeName() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    default Object[] getAttributes() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    default Object[] getAttributes(Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}
