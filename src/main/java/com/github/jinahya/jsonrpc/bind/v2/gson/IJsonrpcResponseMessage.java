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
import com.github.jinahya.jsonrpc.bind.v2.JsonrpcResponseMessage;
import com.github.jinahya.jsonrpc.bind.v2.JsonrpcResponseMessageError;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import javax.validation.constraints.AssertTrue;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.github.jinahya.jsonrpc.bind.v2.gson.GsonJsonrpcConfiguration.getGson;
import static com.github.jinahya.jsonrpc.bind.v2.gson.IJsonrpcMessageHelper.setResponseError;
import static com.github.jinahya.jsonrpc.bind.v2.gson.IJsonrpcMessageHelper.setResponseResult;
import static com.github.jinahya.jsonrpc.bind.v2.gson.IJsonrpcObjectHelper.evaluatingTrue;
import static com.github.jinahya.jsonrpc.bind.v2.gson.IJsonrpcObjectHelper.fromJsonArrayToListOf;
import static com.github.jinahya.jsonrpc.bind.v2.gson.IJsonrpcObjectHelper.hasOneThenEvaluateOrFalse;
import static com.github.jinahya.jsonrpc.bind.v2.gson.IJsonrpcObjectHelper.hasOneThenMapOrNull;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

interface IJsonrpcResponseMessage<S extends IJsonrpcResponseMessage<S>>
        extends IJsonrpcMessage<S>,
                JsonrpcResponseMessage {

    @Override
    @AssertTrue
    default boolean isResultAndErrorExclusive() {
        return JsonrpcResponseMessage.super.isResultAndErrorExclusive();
    }

    // ---------------------------------------------------------------------------------------------------------- result
    @Override
    default boolean hasResult() {
        return hasOneThenEvaluateOrFalse(
                getClass(),
                this,
                IJsonrpcMessageHelper::getResponseResult,
                evaluatingTrue()
        );
    }

    @Override
    @AssertTrue
    default boolean isResultContextuallyValid() {
        return JsonrpcResponseMessage.super.isResultContextuallyValid();
    }

    @Override
    default <T> List<T> getResultAsArray(final Class<T> elementClass) {
        requireNonNull(elementClass, "elementClass is null");
        return hasOneThenMapOrNull(
                getClass(),
                this,
                IJsonrpcMessageHelper::getResponseResult,
                result -> {
                    if (result.isJsonArray()) {
                        return fromJsonArrayToListOf(result.getAsJsonArray(), elementClass);
                    }
                    return new ArrayList<>(singletonList(getResultAsObject(elementClass)));
                }
        );
    }

    @Override
    default void setResultAsArray(final List<?> result) {
        final Gson gson = getGson();
        final Type type = new TypeToken<List<?>>() {
        }.getType();
        setResponseResult(getClass(), this, ofNullable(result).map(v -> gson.toJsonTree(v, type)).orElse(null));
    }

    @Override
    default <T> T getResultAsObject(final Class<T> objectClass) {
        requireNonNull(objectClass, "objectClass is null");
        return hasOneThenMapOrNull(
                getClass(),
                this,
                IJsonrpcMessageHelper::getResponseResult,
                result -> {
                    final Gson gson = getGson();
                    try {
                        return gson.fromJson(result, objectClass);
                    } catch (final JsonSyntaxException jse) {
                        throw new JsonrpcBindException(jse);
                    }
                }
        );
    }

    @Override
    default void setResultAsObject(final Object result) {
        final Gson gson = getGson();
        setResponseResult(getClass(), this, ofNullable(result).map(v -> gson.toJsonTree(result)).orElse(null));
    }

    // ----------------------------------------------------------------------------------------------------------- error
    @Override
    default boolean hasError() {
        return hasOneThenEvaluateOrFalse(
                getClass(),
                this,
                IJsonrpcMessageHelper::getResponseError,
                evaluatingTrue()
        );
    }

    @Override
    default boolean isErrorContextuallyValid() {
        return JsonrpcResponseMessage.super.isErrorContextuallyValid();
    }

    @Override
    default <T extends JsonrpcResponseMessageError> T getErrorAs(final Class<T> clazz) {
        requireNonNull(clazz, "clazz is null");
        return hasOneThenMapOrNull(
                getClass(),
                this,
                IJsonrpcMessageHelper::getResponseError,
                error -> {
                    final Gson gson = getGson();
                    try {
                        return gson.fromJson(error, clazz);
                    } catch (final JsonSyntaxException jse) {
                        throw new JsonrpcBindException(jse);
                    }
                }
        );
    }

    @Override
    default void setErrorAs(final JsonrpcResponseMessageError error) {
        final Gson gson = getGson();
        setResponseError(getClass(), this, (JsonObject) ofNullable(error).map(gson::toJsonTree).orElse(null));
    }

    @Override
    default JsonrpcResponseMessageError getErrorAsDefaultType() {
        return getErrorAs(GsonJsonrpcResponseMessageError.class);
    }
}
