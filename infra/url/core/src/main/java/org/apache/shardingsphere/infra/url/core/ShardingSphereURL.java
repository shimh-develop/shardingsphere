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

package org.apache.shardingsphere.infra.url.core;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Properties;

/**
 * ShardingSphere URL.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class ShardingSphereURL {
    /**
     * classpath: 或 absolutepath:
     */
    private final String sourceType;
    /**
     * 配置文件路径 shardingsphere.yaml   /path/to/config.yaml
     */
    private final String configurationSubject;
    /**
     * jdbc:shardingsphere:classpath:config.yaml?placeholder-type=environment
     * 解析 JDBC URL 参数 placeholder-type=environment 等
     * https://shardingsphere.apache.org/document/5.5.0/cn/user-manual/shardingsphere-jdbc/yaml-config/jdbc-driver/known-implementation/
     */
    private final Properties queryProps;
    
    /**
     * Parse ShardingSphere URL.
     * 
     * @param url URL
     * @return ShardingSphere URL
     */
    public static ShardingSphereURL parse(final String url) {
        /**
         * 从类路径中加载 或 从绝对路径中加载
         * url -> classpath:shardingsphere.yaml 或 jdbc:shardingsphere:absolutepath:/path/to/config.yaml
         * sourceType -> classpath: 或 absolutepath:
         *
         */
        String sourceType = parseSourceType(url);
        return new ShardingSphereURL(sourceType, parseConfigurationSubject(url.substring(sourceType.length())), parseProperties(url));
    }
    
    private static String parseSourceType(final String url) {
        /**
         * classpath:shardingsphere.yaml -> classpath:
         */
        return url.substring(0, url.indexOf(':') + 1);
    }
    
    private static String parseConfigurationSubject(final String url) {
        String result = url.substring(0, url.contains("?") ? url.indexOf('?') : url.length());
        Preconditions.checkArgument(!result.isEmpty(), "Configuration subject is required in URL.");
        return result;
    }
    
    private static Properties parseProperties(final String url) {
        /**
         * url -> jdbc:shardingsphere:classpath:config.yaml?placeholder-type=environment
         * 解析参数 placeholder-type 等
         * https://shardingsphere.apache.org/document/5.5.0/cn/user-manual/shardingsphere-jdbc/yaml-config/jdbc-driver/known-implementation/
         */
        if (!url.contains("?")) {
            return new Properties();
        }
        String queryProps = url.substring(url.indexOf('?') + 1);
        if (Strings.isNullOrEmpty(queryProps)) {
            return new Properties();
        }
        String[] pairs = queryProps.split("&");
        Properties result = new Properties();
        for (String each : pairs) {
            int index = each.indexOf("=");
            if (index > 0) {
                result.put(each.substring(0, index), each.substring(index + 1));
            }
        }
        return result;
    }
}
