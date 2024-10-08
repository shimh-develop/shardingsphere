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

package org.apache.shardingsphere.infra.database.core.connector;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Properties;

/**
 * Standard connection properties.
 */
@RequiredArgsConstructor
@Getter
public final class StandardConnectionProperties implements ConnectionProperties {
    /**
     * 从jdbc_url解析的IP port 数据库名 连接参数
     * url -> jdbc:mysql://localhost:3306/demo_ds_0
     */
    private final String hostname;
    
    private final int port;
    
    private final String catalog;
    
    private final String schema;
    
    private final Properties queryProperties;
    
    private final Properties defaultQueryProperties;
    
    public StandardConnectionProperties(final String hostname, final int port, final String catalog, final String schema) {
        this(hostname, port, catalog, schema, new Properties(), new Properties());
    }
    
    @Override
    public boolean isInSameDatabaseInstance(final ConnectionProperties connectionProps) {
        return hostname.equals(connectionProps.getHostname()) && port == connectionProps.getPort();
    }
}
