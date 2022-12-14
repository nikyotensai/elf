/*
 * Copyright 2002-2014 the original author or authors.
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


/**
 * A {@link GenericConverter} that may conditionally execute based on attributes
 * of the {@code source} and {@code target} {@link TypeDescriptor}.
 * See {@link ConditionalConverter} for details.
 *
 * @author Keith Donald
 * @author Phillip Webb
 * @see GenericConverter
 * @see ConditionalConverter
 * @since 3.0
 */
interface ConditionalGenericConverter extends GenericConverter, ConditionalConverter {

}
