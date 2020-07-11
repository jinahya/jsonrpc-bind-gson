package com.github.jinahya.jsonrpc.bind.v2;

/*-
 * #%L
 * jsonrpc-bind-gson
 * %%
 * Copyright (C) 2020 Jinahya, Inc.
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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import static java.util.Objects.requireNonNull;

public class GsonJsonrpcResponseMessageService
        implements JsonrpcResponseMessageService {

    @Override
    public JsonrpcResponseMessage fromJson(Object source) {
        requireNonNull(source, "source is null");
        if (source instanceof InputStream) {
            source = new InputStreamReader((InputStream) source);
        }
        return GsonJsonrpcMessageServiceHelper.fromJson(source, GsonJsonrpcResponseMessage.class);
    }

    @Override
    public void toJson(final JsonrpcResponseMessage message, Object target) {
        requireNonNull(target, "target is null");
        if (target instanceof OutputStream) {
            target = new OutputStreamWriter((OutputStream) target);
        }
        GsonJsonrpcMessageServiceHelper.toJson(target, message);
    }
}
