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

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * URL argument line.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class URLArgumentLine {
    
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\$\\{(.+::.*)}$");
    
    private static final String KV_SEPARATOR = "::";
    
    private final String argName;
    
    private final String argDefaultValue;
    
    private final Matcher placeholderMatcher;
    
    /**
     * Parse URL argument line.
     *
     * @param line line
     * @return parsed URL argument line
     */
    public static Optional<URLArgumentLine> parse(final String line) {
        /**
         * 正则表达式匹配 $${::}
         * ds_1:
         *   dataSourceClassName: com.zaxxer.hikari.HikariDataSource
         *   driverClassName: $${fixture.config.driver.driver-class-name::org.h2.Driver}
         *   jdbcUrl: $${fixture.config.driver.jdbc-url::jdbc:h2:mem:foo_ds_do_not_use}
         *   username: $${fixture.config.driver.username::}
         *   password: $${fixture.config.driver.password::}
         */
        Matcher matcher = URLArgumentLine.PLACEHOLDER_PATTERN.matcher(line);
        if (!matcher.find()) {
            return Optional.empty();
        }
        String[] parsedArg = matcher.group(1).split(KV_SEPARATOR, 2);
        return Optional.of(new URLArgumentLine(parsedArg[0], parsedArg[1], matcher));
    }
    
    /**
     * Replace argument.
     *
     * @param type placeholder type
     * @return replaced argument
     */
    public String replaceArgument(final URLArgumentPlaceholderType type) {
        // 获取占位符对应的值
        String argumentValue = getArgumentValue(type);
        /**
         * 当对应的动态占位符的值不存在时，此 YAML 属性的值将被设置为::右侧的默认值。
         * 当对应的动态占位符的值和::右侧的默认值均不存在时，此属性将被设置为空。
         */
        if (!Strings.isNullOrEmpty(argumentValue)) {
            return placeholderMatcher.replaceAll(argumentValue);
        }
        if (!argDefaultValue.isEmpty()) {
            return placeholderMatcher.replaceAll(argDefaultValue);
        }
        String modifiedLineWithSpace = placeholderMatcher.replaceAll("");
        return modifiedLineWithSpace.substring(0, modifiedLineWithSpace.length() - 1);
    }
    
    private String getArgumentValue(final URLArgumentPlaceholderType type) {
        if (URLArgumentPlaceholderType.ENVIRONMENT == type) {
            return System.getenv(argName);
        }
        if (URLArgumentPlaceholderType.SYSTEM_PROPS == type) {
            return System.getProperty(argName);
        }
        return null;
    }
}
