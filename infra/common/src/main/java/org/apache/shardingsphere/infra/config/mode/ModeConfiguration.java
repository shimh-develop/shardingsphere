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

package org.apache.shardingsphere.infra.config.mode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Mode configuration.
 * 模式配置
 * https://shardingsphere.apache.org/document/5.5.0/cn/user-manual/shardingsphere-jdbc/yaml-config/mode/
 * mode:
 *   type: Standalone
 *   repository:
 *     type: JDBC
 */
@RequiredArgsConstructor
@Getter
public final class ModeConfiguration {

    private final String type;
    
    private final PersistRepositoryConfiguration repository;
}
