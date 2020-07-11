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
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import static com.github.jinahya.jsonrpc.bind.v2.GsonJsonrpcConfiguration.getGson;
import static java.util.Objects.requireNonNull;

/**
 * A helper class for message services.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
final class GsonJsonrpcMessageServiceHelper {

    static <T extends JsonrpcMessage> T fromJson(final Object source, final Class<T> clazz) {
        requireNonNull(source, "source is null");
        requireNonNull(clazz, "clazz is null");
        final Gson gson = getGson();
        if (source instanceof JsonElement) {
            try {
                return gson.fromJson((JsonElement) source, clazz);
            } catch (final JsonSyntaxException jse) {
                throw new JsonrpcBindException(jse);
            }
        }
        if (source instanceof ReadableByteChannel) {
            return fromJson(Channels.newInputStream((ReadableByteChannel) source), clazz);
        }
        if (source instanceof InputStream) {
            return fromJson(new InputStreamReader((InputStream) source), clazz);
        }
        if (source instanceof Reader) {
            try {
                return gson.fromJson((Reader) source, clazz);
            } catch (JsonIOException | JsonSyntaxException e) {
                throw new JsonrpcBindException(e);
            }
        }
        if (!(source instanceof JsonReader)) {
            throw new IllegalArgumentException("source(" + source + ") is not an instance of " + JsonReader.class);
        }
        try {
            return gson.fromJson((JsonReader) source, clazz);
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new JsonrpcBindException(e);
        }
    }

    static <T extends JsonrpcMessage> void toJson(final Object target, final T value) {
        requireNonNull(target, "target is null");
        requireNonNull(value, "value is null");
        if (target instanceof WritableByteChannel) {
            toJson(Channels.newOutputStream((WritableByteChannel) target), value);
            return;
        }
        if (target instanceof java.io.OutputStream) {
            toJson(new OutputStreamWriter((OutputStream) target), value);
            return;
        }
        if (target instanceof java.io.Writer) {
            toJson(new JsonWriter((Writer) target), value);
            return;
        }
        if (!(target instanceof JsonWriter)) {
            throw new IllegalArgumentException("target(" + target + ") is not an instance of " + JsonWriter.class);
        }
        final Gson gson = getGson();
        try {
            gson.toJson(gson.toJsonTree(value), (JsonWriter) target);
        } catch (final JsonIOException jioe) {
            throw new JsonrpcBindException(jioe);
        }
    }

    private GsonJsonrpcMessageServiceHelper() {
        throw new AssertionError("instantiation is not allowed");
    }
}
