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
import com.github.jinahya.jsonrpc.bind.v2.AbstractJsonrpcResponseMessage;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import static com.github.jinahya.jsonrpc.bind.v2.gson.GsonJsonrpcConfiguration.getGson;
import static java.util.Objects.requireNonNull;

@Setter(AccessLevel.PROTECTED)
@Getter(AccessLevel.PROTECTED)
public class GsonJsonrpcResponseMessage
        extends AbstractJsonrpcResponseMessage
        implements IJsonrpcResponseMessage {

    public static <T extends GsonJsonrpcResponseMessage> T fromJson(final Reader reader, final Class<T> clazz) {
        requireNonNull(reader, "reader is null");
        requireNonNull(clazz, "clazz is null");
        try {
            return getGson().fromJson(reader, clazz);
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new JsonrpcBindException(e);
        }
    }

    public static GsonJsonrpcResponseMessage fromJson(final Reader reader) {
        requireNonNull(reader, "reader is null");
        return fromJson(reader, GsonJsonrpcResponseMessage.class);
    }

    public static <T extends GsonJsonrpcResponseMessage> T fromJson(final InputStream stream, final Class<T> clazz) {
        requireNonNull(stream, "reader is null");
        requireNonNull(clazz, "clazz is null");
        try {
            // https://github.com/google/gson/issues/187
            return getGson().fromJson(new InputStreamReader(stream), clazz);
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new JsonrpcBindException(e);
        }
    }

    public static GsonJsonrpcResponseMessage fromJson(final InputStream stream) {
        requireNonNull(stream, "reader is null");
        return fromJson(stream, GsonJsonrpcResponseMessage.class);
    }

    @Override
    public String toString() {
        return super.toString() + "{"
               + PROPERTY_NAME_ID + "=" + id
               + "," + PROPERTY_NAME_RESULT + "=" + result
               + "," + PROPERTY_NAME_ERROR + "=" + error
//               + "," + PROPERTY_NAME_UNRECOGNIZED_PROPERTIES + "=" + unrecognizedProperties
               + "}";
    }

    private JsonPrimitive id;

    private JsonElement result;

    private JsonObject error;

//    @Setter(AccessLevel.NONE)
//    @Getter(AccessLevel.NONE)
//    private Map<String, Object> unrecognizedProperties;
}
