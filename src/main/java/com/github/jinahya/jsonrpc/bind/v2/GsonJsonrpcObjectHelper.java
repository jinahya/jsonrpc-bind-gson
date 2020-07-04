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
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.jinahya.jsonrpc.bind.v2.GsonJsonrpcConfiguration.getGson;
import static com.github.jinahya.jsonrpc.bind.v2.JsonrpcObjectHelper.SUPPLYING_FALSE;
import static com.github.jinahya.jsonrpc.bind.v2.JsonrpcObjectHelper.SUPPLYING_FALSE_;
import static com.github.jinahya.jsonrpc.bind.v2.JsonrpcObjectHelper.SUPPLYING_TRUE;
import static com.github.jinahya.jsonrpc.bind.v2.JsonrpcObjectHelper.SUPPLYING_TRUE_;
import static com.github.jinahya.jsonrpc.bind.v2.JsonrpcObjectHelper.supplyingNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

final class GsonJsonrpcObjectHelper {

    // -----------------------------------------------------------------------------------------------------------------
    static <N extends JsonElement, R> R hasOneThenMapOrGet(final Class<?> clazz, final Object object,
                                                           final BiFunction<Class<?>, Object, ? extends N> getter,
                                                           final Function<? super N, ? extends R> function,
                                                           final Supplier<? extends R> supplier) {
        assert getter != null;
        assert function != null;
        assert supplier != null;
        final N v = getter.apply(clazz, object);
        if (v != null && !v.isJsonNull()) {
            return function.apply(v);
        }
        return supplier.get();
    }

    static <N extends JsonElement, R> R hasOneThenMapOrNull(final Class<?> clazz, final Object object,
                                                            final BiFunction<Class<?>, Object, ? extends N> getter,
                                                            final Function<? super N, ? extends R> function) {
        return hasOneThenMapOrGet(clazz, object, getter, function, supplyingNull());
    }

    private static <N extends JsonElement> boolean hasOneThenEvaluateOrGet(
            final Class<?> clazz, final Object object, final BiFunction<Class<?>, Object, ? extends N> getter,
            final Predicate<? super N> predicate, final BooleanSupplier supplier) {
        assert predicate != null;
        assert supplier != null;
        assert supplier == SUPPLYING_TRUE || supplier == SUPPLYING_FALSE;
        return hasOneThenMapOrGet(clazz, object, getter, predicate::test,
                                  supplier == SUPPLYING_TRUE ? SUPPLYING_TRUE_ : SUPPLYING_FALSE_);
    }

    static <N extends JsonElement> boolean hasOneThenEvaluateOrTrue(
            final Class<?> clazz, final Object object, final BiFunction<Class<?>, Object, ? extends N> getter,
            final Predicate<? super N> predicate) {
        return hasOneThenEvaluateOrGet(clazz, object, getter, predicate, SUPPLYING_TRUE);
    }

    static <N extends JsonElement> boolean hasOneThenEvaluateOrFalse(
            final Class<?> clazz, final Object object, final BiFunction<Class<?>, Object, ? extends N> getter,
            final Predicate<? super N> predicate) {
        return hasOneThenEvaluateOrGet(clazz, object, getter, predicate, SUPPLYING_FALSE);
    }

    // -----------------------------------------------------------------------------------------------------------------
    static <T> List<T> jsonArrayToList(final JsonArray jsonArray, final Class<T> elementClass) {
        assert jsonArray != null;
        assert elementClass != null;
        final Gson gson = getGson();
        return stream(jsonArray.spliterator(), false)
                .map(e -> {
                    try {
                        return gson.fromJson(e, elementClass);
                    } catch (final JsonSyntaxException jse) {
                        throw new JsonrpcBindException(jse);
                    }
                })
                .collect(toList())
                ;
    }

    // -----------------------------------------------------------------------------------------------------------------
    private GsonJsonrpcObjectHelper() {
        throw new AssertionError("instantiation is not allowed");
    }
}
