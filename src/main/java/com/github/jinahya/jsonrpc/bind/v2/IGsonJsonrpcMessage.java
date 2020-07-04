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
import com.google.gson.JsonPrimitive;

import javax.validation.constraints.AssertTrue;
import java.math.BigInteger;

import static com.github.jinahya.jsonrpc.bind.v2.GsonJsonrpcConfiguration.getGson;
import static com.github.jinahya.jsonrpc.bind.v2.GsonJsonrpcObjectHelper.hasOneThenEvaluateOrFalse;
import static com.github.jinahya.jsonrpc.bind.v2.GsonJsonrpcObjectHelper.hasOneThenEvaluateOrTrue;
import static com.github.jinahya.jsonrpc.bind.v2.GsonJsonrpcObjectHelper.hasOneThenMapOrNull;
import static com.github.jinahya.jsonrpc.bind.v2.IGsonJsonrpcMessageHelper.setId;
import static com.github.jinahya.jsonrpc.bind.v2.JsonrpcObjectHelper.evaluatingTrue;
import static java.util.Optional.ofNullable;

interface IGsonJsonrpcMessage<S extends IGsonJsonrpcObject<S>>
        extends IGsonJsonrpcObject<S>,
                JsonrpcMessage {

    @Override
    default boolean hasId() {
        return hasOneThenEvaluateOrFalse(
                getClass(),
                this,
                IGsonJsonrpcMessageHelper::getId,
                evaluatingTrue()
        );
    }

    @Override
    default @AssertTrue
    boolean isIdContextuallyValid() {
        return hasOneThenEvaluateOrTrue(
                getClass(),
                this,
                IGsonJsonrpcMessageHelper::getId,
                id -> id.isString() || id.isNumber()
        );
    }

    @Override
    default String getIdAsString() {
        return hasOneThenMapOrNull(
                getClass(),
                this,
                IGsonJsonrpcMessageHelper::getId,
                id -> {
                    try {
                        return id.getAsString();
                    } catch (ClassCastException | IllegalArgumentException e) {
                        throw new JsonrpcBindException(e);
                    }
                }
        );
    }

    @Override
    default void setIdAsString(final String id) {
        setId(getClass(), this, ofNullable(id).map(v -> (JsonPrimitive) getGson().toJsonTree(v)).orElse(null));
    }

    @Override
    default BigInteger getIdAsNumber() {
        return hasOneThenMapOrNull(
                getClass(),
                this,
                IGsonJsonrpcMessageHelper::getId,
                id -> {
                    try {
                        return id.getAsBigInteger();
                    } catch (ClassCastException | IllegalArgumentException | IllegalStateException e) {
                        throw new JsonrpcBindException(e);
                    }
                }
        );
    }

    @Override
    default void setIdAsNumber(final BigInteger id) {
        setId(getClass(), this, ofNullable(id).map(v -> (JsonPrimitive) getGson().toJsonTree(id)).orElse(null));
    }

    @Override
    default Long getIdAsLong() {
        return ofNullable(hasOneThenMapOrNull(
                getClass(),
                this,
                IGsonJsonrpcMessageHelper::getId,
                id -> {
                    if (id.isNumber()) {
                        try {
                            return id.getAsLong();
                        } catch (ClassCastException | IllegalStateException e) {
                            throw new JsonrpcBindException(e);
                        }
                    }
                    return null;
                }))
                .orElseGet(JsonrpcMessage.super::getIdAsLong);
    }

    @Override
    default void setIdAsLong(final Long id) {
        setId(getClass(), this, ofNullable(id).map(v -> (JsonPrimitive) getGson().toJsonTree(v)).orElse(null));
    }

    @Override
    default Integer getIdAsInteger() {
        return ofNullable(hasOneThenMapOrNull(
                getClass(),
                this,
                IGsonJsonrpcMessageHelper::getId,
                id -> {
                    if (id.isNumber()) {
                        try {
                            return id.getAsInt();
                        } catch (ClassCastException | IllegalStateException e) {
                            throw new JsonrpcBindException(e);
                        }
                    }
                    return null;
                }))
                .orElseGet(JsonrpcMessage.super::getIdAsInteger);
    }

    @Override
    default void setIdAsInteger(final Integer id) {
        setId(getClass(), this, ofNullable(id).map(v -> (JsonPrimitive) getGson().toJsonTree(v)).orElse(null));
    }
}

