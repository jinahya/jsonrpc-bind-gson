package com.github.jinahya.jsonrpc.bind.v2.gson;

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
import com.github.jinahya.jsonrpc.bind.v2.JsonrpcResponseMessageError;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import javax.validation.constraints.AssertTrue;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.github.jinahya.jsonrpc.bind.v2.gson.GsonJsonrpcConfiguration.getGson;
import static com.github.jinahya.jsonrpc.bind.v2.gson.IJsonrpcMessageHelper.setResponseErrorData;
import static com.github.jinahya.jsonrpc.bind.v2.gson.IJsonrpcObjectHelper.evaluatingTrue;
import static com.github.jinahya.jsonrpc.bind.v2.gson.IJsonrpcObjectHelper.fromJsonArrayToListOf;
import static com.github.jinahya.jsonrpc.bind.v2.gson.IJsonrpcObjectHelper.hasOneThenEvaluateOrFalse;
import static com.github.jinahya.jsonrpc.bind.v2.gson.IJsonrpcObjectHelper.hasOneThenMapOrNull;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

interface IJsonrpcResponseMessageError<S extends IJsonrpcResponseMessageError<S>>
        extends IJsonrpcObject<S>,
                JsonrpcResponseMessageError {

    @Override
    default boolean hasData() {
        return hasOneThenEvaluateOrFalse(
                getClass(),
                this,
                IJsonrpcMessageHelper::getResponseErrorData,
                evaluatingTrue()
        );
    }

    @Override
    @AssertTrue
    default boolean isDataContextuallyValid() {
        return JsonrpcResponseMessageError.super.isDataContextuallyValid();
    }

    @Override
    default <T> List<T> getDataAsArray(final Class<T> elementClass) {
        requireNonNull(elementClass, "elementClass is null");
        return hasOneThenMapOrNull(
                getClass(),
                this,
                IJsonrpcMessageHelper::getResponseErrorData,
                data -> {
                    if (data.isJsonArray()) {
                        return fromJsonArrayToListOf(data.getAsJsonArray(), elementClass);
                    }
                    return new ArrayList<>(singletonList(getDataAsObject(elementClass)));
                }
        );
    }

    @Override
    default void setDataAsArray(final List<?> data) {
        final Gson gson = getGson();
        final Type type = new TypeToken<List<?>>() {
        }.getType();
        setResponseErrorData(getClass(), this, ofNullable(data).map(v -> gson.toJsonTree(data, type)).orElse(null));
    }

    @Override
    default <T> T getDataAsObject(final Class<T> objectClass) {
        requireNonNull(objectClass, "objectClass is null");
        return hasOneThenMapOrNull(
                getClass(),
                this,
                IJsonrpcMessageHelper::getResponseErrorData,
                data -> {
                    final Gson gson = getGson();
                    try {
                        return gson.fromJson(data, objectClass);
                    } catch (final JsonSyntaxException jse) {
                        throw new JsonrpcBindException(jse);
                    }
                }
        );
    }

    @Override
    default void setDataAsObject(final Object data) {
        final Gson gson = getGson();
        setResponseErrorData(getClass(), this, ofNullable(data).map(gson::toJsonTree).orElse(null));
    }
}
