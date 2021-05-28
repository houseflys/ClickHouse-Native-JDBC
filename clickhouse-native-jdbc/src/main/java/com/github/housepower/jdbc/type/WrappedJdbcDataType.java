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

package com.github.housepower.jdbc.type;

import com.github.housepower.data.IDataType;

public abstract class WrappedJdbcDataType<JDBC> implements JdbcDataType<JDBC> {

    protected final IDataType<JDBC, JDBC> ckDataType;

    protected WrappedJdbcDataType(IDataType<JDBC, JDBC> ckDataType) {
        this.ckDataType = ckDataType;
    }

    @Override
    public boolean nullable() {
        return ckDataType.nullable();
    }

    @Override
    public int getPrecision() {
        return ckDataType.getPrecision();
    }

    @Override
    public int getScale() {
        return ckDataType.getScale();
    }

    @Override
    public boolean isSigned() {
        return ckDataType.isSigned();
    }

    @Override
    public String format(JDBC value) {
        return ckDataType.serializeText(value);
    }
}
