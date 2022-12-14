/*
 * Copyright 2002-2012 the original author or authors.
 *
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
 */

package com.github.nikyotensai.elf.instrument.client.spring.el;

import java.util.UUID;

/**
 * Converts from a String to a java.util.UUID by calling {@link UUID#fromString(String)}.
 *
 * @author Phillip Webb
 * @since 3.2
 */
final class StringToUUIDConverter implements Converter<String, UUID> {

    public UUID convert(String source) {
        if (StringUtils.hasLength(source)) {
            return UUID.fromString(source.trim());
        }
        return null;
    }

}
