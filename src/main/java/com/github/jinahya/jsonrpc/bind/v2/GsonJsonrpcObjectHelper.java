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
import com.google.gson.JsonSyntaxException;

import java.util.List;

import static com.github.jinahya.jsonrpc.bind.v2.GsonJsonrpcConfiguration.getGson;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

final class GsonJsonrpcObjectHelper {

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
