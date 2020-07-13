package com.github.jinahya.jsonrpc.bind.v2;

/*-
 * #%L
 * jsonrpc-bind-jackson
 * %%
 * Copyright (C) 2019 - 2020 Jinahya, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.jinahya.jsonrpc.bind.JsonrpcBindException;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import jakarta.validation.constraints.AssertTrue;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.github.jinahya.jsonrpc.bind.v2.GsonJsonrpcConfiguration.getGson;
import static com.github.jinahya.jsonrpc.bind.v2.GsonJsonrpcObjectHelper.jsonArrayToList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class GsonJsonrpcResponseMessageError
        extends AbstractJsonrpcResponseMessageError
        implements IGsonJsonrpcResponseMessageError<GsonJsonrpcResponseMessageError> {

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return super.toString() + "{"
               + PROPERTY_NAME_DATA + "=" + data
               + "}";
    }

    // ------------------------------------------------------------------------------------------------------------ data
    @Override
    public boolean hasData() {
        return data != null && !data.isJsonNull();
    }

    @Override
    @AssertTrue
    public boolean isDataContextuallyValid() {
        if (!hasData()) {
            return true;
        }
        return true;
    }

    @Override
    public <T> List<T> getDataAsArray(final Class<T> elementClass) {
        requireNonNull(elementClass, "elementClass is null");
        if (!hasData()) {
            return null;
        }
        if (data.isJsonArray()) {
            return jsonArrayToList(data.getAsJsonArray(), elementClass);
        }
        return new ArrayList<>(singletonList(getDataAsObject(elementClass)));
    }

    @Override
    public void setDataAsArray(final List<?> data) {
        final Type type = new TypeToken<List<?>>() {
        }.getType();
        this.data = ofNullable(data).map(v -> getGson().toJsonTree(v, type)).orElse(null);
    }

    @Override
    public <T> T getDataAsObject(final Class<T> objectClass) {
        requireNonNull(objectClass, "objectClass is null");
        if (!hasData()) {
            return null;
        }
        try {
            return getGson().fromJson(data, objectClass);
        } catch (final JsonSyntaxException jse) {
            throw new JsonrpcBindException(jse);
        }
    }

    @Override
    public void setDataAsObject(final Object data) {
        this.data = ofNullable(data).map(v -> getGson().toJsonTree(v)).orElse(null);
    }

    // -----------------------------------------------------------------------------------------------------------------
    private JsonElement data;
}
