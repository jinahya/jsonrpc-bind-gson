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
import com.github.jinahya.jsonrpc.bind.v2.JsonrpcRequestMessage;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import javax.validation.constraints.AssertTrue;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.github.jinahya.jsonrpc.bind.v2.gson.GsonJsonrpcConfiguration.getGson;
import static com.github.jinahya.jsonrpc.bind.v2.gson.IJsonrpcMessageHelper.setRequestParams;
import static com.github.jinahya.jsonrpc.bind.v2.gson.IJsonrpcObjectHelper.evaluatingTrue;
import static com.github.jinahya.jsonrpc.bind.v2.gson.IJsonrpcObjectHelper.fromJsonArrayToListOf;
import static com.github.jinahya.jsonrpc.bind.v2.gson.IJsonrpcObjectHelper.hasOneThenEvaluateOrFalse;
import static com.github.jinahya.jsonrpc.bind.v2.gson.IJsonrpcObjectHelper.hasOneThenEvaluateOrTrue;
import static com.github.jinahya.jsonrpc.bind.v2.gson.IJsonrpcObjectHelper.hasOneThenMapOrNull;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

interface IJsonrpcRequestMessage extends JsonrpcRequestMessage, IJsonrpcMessage {

    @Override
    default boolean hasParams() {
        return hasOneThenEvaluateOrFalse(
                getClass(),
                this,
                IJsonrpcMessageHelper::getRequestParams,
                evaluatingTrue()
        );
    }

    @Override
    default @AssertTrue boolean isParamsContextuallyValid() {
        return hasOneThenEvaluateOrTrue(
                getClass(),
                this,
                IJsonrpcMessageHelper::getRequestParams,
                params -> params.isJsonArray() || params.isJsonObject()
        );
    }

    @Override
    default <T> List<T> getParamsAsArray(final Class<T> elementClass) {
        requireNonNull(elementClass, "elementClass is null");
        return hasOneThenMapOrNull(
                getClass(),
                this,
                IJsonrpcMessageHelper::getRequestParams,
                params -> {
                    if (params.isJsonArray()) {
                        final JsonArray jsonArray = params.getAsJsonArray();
                        return fromJsonArrayToListOf(jsonArray, elementClass);
                    }
                    final Gson gson = getGson();
                    try {
                        return new ArrayList<>(singletonList(gson.fromJson(params, elementClass)));
                    } catch (final JsonSyntaxException jse) {
                        throw new JsonrpcBindException(jse.getCause());
                    }
                }
        );
    }

    @Override
    default void setParamsAsArray(final List<?> params) {
        final Gson gson = getGson();
        final Type type = new TypeToken<List<?>>() {
        }.getType();
        setRequestParams(getClass(), this, ofNullable(params).map(v -> gson.toJsonTree(params, type)).orElse(null));
    }

    @Override
    default <T> T getParamsAsObject(final Class<T> objectClass) {
        requireNonNull(objectClass, "objectClass is null");
        return hasOneThenMapOrNull(
                getClass(),
                this,
                IJsonrpcMessageHelper::getRequestParams,
                params -> {
                    final Gson gson = getGson();
                    try {
                        return gson.fromJson(params, objectClass);
                    } catch (final JsonSyntaxException jse) {
                        throw new JsonrpcBindException(jse.getCause());
                    }
                }
        );
    }

    @Override
    default void setParamsAsObject(final Object params) {
        final Gson gson = getGson();
        setRequestParams(getClass(), this, ofNullable(params).map(gson::toJsonTree).orElse(null));
    }
}
