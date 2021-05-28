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

package com.github.housepower.jdbc.convert;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.ParameterizedType;

/**
 * A converter converts a source object of type {@code S} to a target of type {@code T}.
 *
 * @param <S> the source type
 * @param <T> the target type
 */
@ThreadSafe
@FunctionalInterface
public interface Converter<S, T> {

    /**
     * Convert the source object of type {@code S} to target type {@code T}.
     *
     * @param source the source object to convert, which must be an instance of {@code S}
     * @return the converted object, which must be an instance of {@code T}
     * @throws IllegalArgumentException if the source cannot be converted to the desired target type
     */
    T convert(S source);

    @SuppressWarnings("unchecked")
    default Class<S> sourceType() {
        return (Class<S>) ((ParameterizedType) this.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
    }

    @SuppressWarnings("unchecked")
    default Class<T> targetType() {
        return (Class<T>) ((ParameterizedType) this.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[1];
    }
}
