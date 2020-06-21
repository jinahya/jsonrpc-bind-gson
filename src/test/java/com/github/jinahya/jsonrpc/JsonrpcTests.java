package com.github.jinahya.jsonrpc;

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

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class JsonrpcTests {

    public static final ThreadLocal<Class<?>> THREAD_LOCAL_CALLER = new ThreadLocal<>();

    /**
     * Applies a stream of specified resource to specified function and returns the result.
     *
     * @param name     the resource name to open.
     * @param function the function to be applied with the resource stream.
     * @param <R>      result type parameter.
     * @return the result of the function
     * @throws IOException if an I/O error occurs.
     */
    public static <R> R applyResourceStream(final String name,
                                            final Function<? super InputStream, ? extends R> function)
            throws IOException {
        requireNonNull(name, "name is null");
        requireNonNull(function, "function is null");
        try {
            try (InputStream resource = ofNullable(THREAD_LOCAL_CALLER.get())
                    .map(c -> c.getResourceAsStream(name))
                    .orElseGet(() -> currentThread().getContextClassLoader().getResourceAsStream(name))) {
                log.debug(": {}, {}", THREAD_LOCAL_CALLER.get(), name);
                assertNotNull(resource, "null resource stream loaded from '" + name + "'");
                return function.apply(resource);
            }
        } finally {
            THREAD_LOCAL_CALLER.remove();
        }
    }

    public static void acceptResourceStream(final String name, final Consumer<? super InputStream> consumer)
            throws IOException {
        requireNonNull(name, "name is null");
        requireNonNull(consumer, "consumer is null");
        applyResourceStream(name, s -> {
            consumer.accept(s);
            return null;
        });
    }

    public static <R> R applyResourceReader(final String name,
                                            final Function<? super Reader, ? extends R> function)
            throws IOException {
        requireNonNull(name, "name is null");
        requireNonNull(function, "function is null");
        return applyResourceStream(name, s -> function.apply(new InputStreamReader(s)));
    }

    public static void acceptResourceReader(final String name, final Consumer<? super Reader> consumer)
            throws IOException {
        requireNonNull(name, "name is null");
        requireNonNull(consumer, "consumer is null");
        applyResourceReader(name, r -> {
            consumer.accept(r);
            return null;
        });
    }

    private JsonrpcTests() {
        throw new AssertionError("instantiation is not allowed");
    }
}
