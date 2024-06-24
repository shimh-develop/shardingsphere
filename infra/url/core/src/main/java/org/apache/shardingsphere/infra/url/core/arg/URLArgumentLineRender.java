/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.url.core.arg;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;

/**
 * URL argument line render.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class URLArgumentLineRender {
    
    /**
     * Render argument.
     *
     * @param lines lines to be rendered
     * @param placeholderType configuration content placeholder type
     * @return rendered content
     */
    public static byte[] render(final Collection<String> lines, final URLArgumentPlaceholderType placeholderType) {
        StringBuilder result = new StringBuilder();
        /**
         * 逐行解析占位符 替换
         * 在涉及的 YAML 文件中允许通过动态占位符设置特定 YAML 属性的值，并配置可选的默认值。
         * 动态占位符的名称和其可选的默认值通过::分割， 在最外层通过$${和}包裹。
         *
         */
        for (String each : lines) {
            // 是否包含占位符
            Optional<URLArgumentLine> argLine = URLArgumentPlaceholderType.NONE == placeholderType ? Optional.empty() : URLArgumentLine.parse(each);
            // 有占位符 replaceArgument 替换
            result.append(argLine.map(optional -> optional.replaceArgument(placeholderType)).orElse(each)).append(System.lineSeparator());
        }
        return result.toString().getBytes(StandardCharsets.UTF_8);
    }
}
