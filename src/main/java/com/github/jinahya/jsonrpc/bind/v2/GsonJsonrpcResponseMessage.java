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
import com.google.gson.JsonObject;
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

public class GsonJsonrpcResponseMessage
        extends AbstractJsonrpcResponseMessage
        implements IGsonJsonrpcResponseMessage<GsonJsonrpcResponseMessage> {

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return super.toString() + "{"
               + PROPERTY_NAME_RESULT + "=" + result
               + "," + PROPERTY_NAME_ERROR + "=" + error
               + "," + PROPERTY_NAME_ID + "=" + id
               + "}";
    }

    // -----------------------------------------------------------------------------------------------------------------
    @Override
    @AssertTrue
    public boolean isResultAndErrorExclusive() {
        return super.isResultAndErrorExclusive();
    }

    // ---------------------------------------------------------------------------------------------------------- result
    @Override
    public boolean hasResult() {
        return result != null && !result.isJsonNull();
    }

    @Override
    @AssertTrue
    public boolean isResultContextuallyValid() {
        if (!hasResult()) {
            return true;
        }
        return super.isResultContextuallyValid();
    }

    @Override
    public <T> List<T> getResultAsArray(final Class<T> elementClass) {
        requireNonNull(elementClass, "elementClass is null");
        if (!hasResult()) {
            return null;
        }
        if (result.isJsonArray()) {
            return jsonArrayToList(result.getAsJsonArray(), elementClass);
        }
        return new ArrayList<>(singletonList(getResultAsObject(elementClass)));
    }

    @Override
    public void setResultAsArray(final List<?> result) {
        final Type type = new TypeToken<List<?>>() {
        }.getType();
        this.result = ofNullable(result).map(v -> getGson().toJsonTree(v, type)).orElse(null);
    }

    @Override
    public <T> T getResultAsObject(final Class<T> objectClass) {
        requireNonNull(objectClass, "objectClass is null");
        if (!hasResult()) {
            return null;
        }
        try {
            return getGson().fromJson(result, objectClass);
        } catch (final JsonSyntaxException jse) {
            throw new JsonrpcBindException(jse);
        }
    }

    @Override
    public void setResultAsObject(final Object result) {
        this.result = ofNullable(result).map(v -> getGson().toJsonTree(v)).orElse(null);
    }

    // ----------------------------------------------------------------------------------------------------------- error
    @Override
    public boolean hasError() {
        return error != null;
    }

    @Override
    public boolean isErrorContextuallyValid() {
        return super.isErrorContextuallyValid();
    }

    @Override
    public <T extends JsonrpcResponseMessageError> T getErrorAs(final Class<T> clazz) {
        requireNonNull(clazz, "clazz is null");
        try {
            return getGson().fromJson(error, clazz);
        } catch (final JsonSyntaxException jse) {
            throw new JsonrpcBindException(jse);
        }
    }

    @Override
    public void setErrorAs(final JsonrpcResponseMessageError error) {
        this.error = (JsonObject) ofNullable(error).map(v -> getGson().toJsonTree(v)).orElse(null);
    }

    @Override
    public JsonrpcResponseMessageError getErrorAsDefaultType() {
        return getErrorAs(GsonJsonrpcResponseMessageError.class);
    }

    // -----------------------------------------------------------------------------------------------------------------
    private JsonElement result;

    private JsonObject error;

    // -----------------------------------------------------------------------------------------------------------------
    private JsonPrimitive id;
}
