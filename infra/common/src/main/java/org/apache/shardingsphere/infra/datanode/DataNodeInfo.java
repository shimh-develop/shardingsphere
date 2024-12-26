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

package org.apache.shardingsphere.infra.datanode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Data node info.
 */
@RequiredArgsConstructor
@Getter
public final class DataNodeInfo {
    /**
     * 数据源或表名前缀
     * ds_0 -> ds_
     * t_order_0 -> t_order_
     */
    private final String prefix;
    /**
     * 数据源后缀里最小的长度
     * ds_0、ds_1 -> 1
     */
    private final int suffixMinLength;
    /**
     * '0'
     */
    private final char paddingChar;
}
