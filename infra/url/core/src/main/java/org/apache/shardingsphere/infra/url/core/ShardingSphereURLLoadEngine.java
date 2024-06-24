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

import org.apache.shardingsphere.infra.url.core.arg.URLArgumentLineRender;
import org.apache.shardingsphere.infra.url.core.arg.URLArgumentPlaceholderTypeFactory;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.url.spi.ShardingSphereURLLoader;

import java.util.Arrays;
import java.util.Collection;

/**
 * ShardingSphere URL load engine.
 */
public final class ShardingSphereURLLoadEngine {
    
    private final ShardingSphereURL url;
    
    private final ShardingSphereURLLoader urlLoader;
    
    public ShardingSphereURLLoadEngine(final ShardingSphereURL url) {
        this.url = url;
        /**
         * 根据配置文件加载方式获取对应的Loader
         * classpath: ->
         * @see org.apache.shardingsphere.infra.url.classpath.ClassPathURLLoader
         * absolutepath: ->
         * @see org.apache.shardingsphere.infra.url.absolutepath.AbsolutePathURLLoader
         */
        urlLoader = TypedSPILoader.getService(ShardingSphereURLLoader.class, url.getSourceType());
    }
    
    /**
     * Load configuration content.
     * 
     * @return loaded content
     */
    public byte[] loadContent() {
        /**
         * 加载的配置文件的行集合
         */
        Collection<String> lines = Arrays.asList(urlLoader.load(url.getConfigurationSubject(), url.getQueryProps()).split(System.lineSeparator()));
        /**
         *  https://shardingsphere.apache.org/document/5.5.0/cn/user-manual/shardingsphere-jdbc/yaml-config/jdbc-driver/known-implementation/#%E5%85%B6%E4%BB%96%E5%AE%9E%E7%8E%B0
         * 解析 placeholder-type 填充配置文件中的占位符
         * ds_1:
         *   dataSourceClassName: com.zaxxer.hikari.HikariDataSource
         *   driverClassName: $${fixture.config.driver.driver-class-name::org.h2.Driver}
         *   jdbcUrl: $${fixture.config.driver.jdbc-url::jdbc:h2:mem:foo_ds_do_not_use}
         *   username: $${fixture.config.driver.username::}
         *   password: $${fixture.config.driver.password::}
         * --->
         * ds_1:
         *   dataSourceClassName: com.zaxxer.hikari.HikariDataSource
         *   driverClassName: org.h2.Driver
         *   jdbcUrl: jdbc:h2:mem:foo_ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL
         *   username: sa
         *   password:
         *
         */
        return URLArgumentLineRender.render(lines, URLArgumentPlaceholderTypeFactory.valueOf(url.getQueryProps()));
    }
}
