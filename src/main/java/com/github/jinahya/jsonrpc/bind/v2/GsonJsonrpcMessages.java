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

import com.google.gson.Gson;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.WeakHashMap;

import static com.github.jinahya.jsonrpc.bind.v2.GsonJsonrpcConfiguration.getGson;
import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;
import static java.util.Collections.synchronizedMap;
import static java.util.Objects.requireNonNull;

/**
 * A utility class for messages.
 */
final class GsonJsonrpcMessages {

    private static final Map<Class<?>, MethodHandle> FROM_JSON_HANDLES = synchronizedMap(new WeakHashMap<>());

    private static MethodHandle fromJsonHandle(final Class<?> clazz) {
        assert clazz != null;
        return FROM_JSON_HANDLES.computeIfAbsent(clazz, k -> {
            try {
                for (Class<?> c = k; c != null; c = c.getSuperclass()) {
                    try {
                        return publicLookup().findVirtual(
                                Gson.class, "fromJson", methodType(Object.class, c, Class.class));
                    } catch (final NoSuchMethodException nsme) {
                        // suppressed
                    }
                }
                throw new NoSuchMethodException("no readValue method for " + k);
            } catch (final ReflectiveOperationException roe) {
                throw new RuntimeException(roe);
            }
        });
    }

    static <T extends JsonrpcMessage> T fromJson(final Object source, final Class<T> clazz) {
        requireNonNull(source, "source is null");
        requireNonNull(clazz, "clazz is null");
        try {
            return clazz.cast(fromJsonHandle(source.getClass()).invoke(getGson(), source, clazz));
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static final Map<Class<?>, MethodHandle> TO_JSON_HANDLES = synchronizedMap(new WeakHashMap<>());

    private static MethodHandle toJsonHandle(final Class<?> clazz) {
        assert clazz != null;
        return TO_JSON_HANDLES.computeIfAbsent(clazz, k -> {
            try {
                for (Class<?> c = k; c != null; c = c.getSuperclass()) {
                    try {
                        return publicLookup().findVirtual(
                                Gson.class, "toJson", methodType(Void.class, c, Class.class));
                    } catch (final NoSuchMethodException nsme) {
                        // suppressed
                    }
                }
                throw new NoSuchMethodException("no writeValue method for " + k);
            } catch (final ReflectiveOperationException roe) {
                throw new RuntimeException(roe);
            }
        });
    }

    static <T extends JsonrpcMessage> void toJson(final Object target, final T value) {
        requireNonNull(target, "target is null");
        requireNonNull(value, "value is null");
        try {
            toJsonHandle(target.getClass()).invoke(getGson(), target, value);
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private GsonJsonrpcMessages() {
        throw new AssertionError("instantiation is not allowed");
    }
}
