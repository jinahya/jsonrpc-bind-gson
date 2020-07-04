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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import javax.validation.constraints.AssertTrue;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.github.jinahya.jsonrpc.bind.v2.GsonJsonrpcConfiguration.getGson;
import static com.github.jinahya.jsonrpc.bind.v2.GsonJsonrpcObjectHelper.jsonArrayToList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class GsonJsonrpcRequestMessage
        extends AbstractJsonrpcRequestMessage
        implements IGsonJsonrpcRequestMessage<GsonJsonrpcRequestMessage> {

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return super.toString() + "{"
               + PROPERTY_NAME_PARAMS + "=" + params
               + "," + PROPERTY_NAME_ID + "=" + id
               + "}";
    }

    // ---------------------------------------------------------------------------------------------------------- params
    @Override
    public boolean hasParams() {
        return params != null && !params.isJsonNull();
    }

    @Override
    public @AssertTrue
    boolean isParamsContextuallyValid() {
        if (!hasParams()) {
            return true;
        }
        return params.isJsonArray() || params.isJsonObject();
    }

    @Override
    public <T> List<T> getParamsAsArray(final Class<T> elementClass) {
        requireNonNull(elementClass, "elementClass is null");
        if (!hasParams()) {
            return null;
        }
        if (params.isJsonArray()) {
            final JsonArray jsonArray = params.getAsJsonArray();
            return jsonArrayToList(jsonArray, elementClass);
        }
        return new ArrayList<>(singletonList(getParamsAsObject(elementClass)));
    }

    @Override
    public void setParamsAsArray(final List<?> params) {
        final Type type = new TypeToken<List<?>>() {
        }.getType();
        this.params = ofNullable(params).map(v -> getGson().toJsonTree(params, type)).orElse(null);
    }

    @Override
    public <T> T getParamsAsObject(final Class<T> objectClass) {
        requireNonNull(objectClass, "objectClass is null");
        if (!hasParams()) {
            return null;
        }
        try {
            return getGson().fromJson(params, objectClass);
        } catch (final JsonSyntaxException jse) {
            throw new JsonrpcBindException(jse.getCause());
        }
    }

    @Override
    public void setParamsAsObject(final Object params) {
        this.params = ofNullable(params).map(v -> getGson().toJsonTree(v)).orElse(null);
    }

    // -----------------------------------------------------------------------------------------------------------------
    private JsonElement params;

    private JsonPrimitive id;
}
