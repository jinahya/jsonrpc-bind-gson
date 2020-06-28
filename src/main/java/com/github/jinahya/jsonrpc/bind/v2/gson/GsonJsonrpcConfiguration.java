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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static java.util.Objects.requireNonNull;

/**
 * A configuration class for JSON-RPC 2.0.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
public final class GsonJsonrpcConfiguration {

    @SuppressWarnings({"java:S3077"}) // Gson is thread-safe!!!
    static volatile Gson gson;

    /**
     * Returns current gson instance.
     *
     * @return current gson instance.
     */
    public static synchronized Gson getGson() {
        return gson;
    }

    /**
     * Replaces current gson instance with specified value.
     *
     * @param gson new gson instance.
     */
    static synchronized void setGson(final Gson gson) {
        GsonJsonrpcConfiguration.gson = requireNonNull(gson, "gson is null");
    }

    /**
     * Replaces current gson instance with specified builder.
     *
     * @param builder a gson builder instance.
     */
    public static synchronized void setGson(final GsonBuilder builder) {
        requireNonNull(builder, "builder is null");
        setGson(builder.create());
    }

    static {
        setGson(new GsonBuilder());
    }

    private GsonJsonrpcConfiguration() {
        throw new AssertionError("instantiation is not allowed");
    }
}
