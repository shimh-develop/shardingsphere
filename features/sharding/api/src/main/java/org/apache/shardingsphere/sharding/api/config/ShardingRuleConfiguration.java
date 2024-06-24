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

package org.apache.shardingsphere.sharding.api.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.function.DistributedRuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Sharding rule configuration.
 * 数据分片
 * rules:
 * - !SHARDING
 *   tables: # 数据分片规则配置
 *     <logic_table_name> (+): # 逻辑表名称
 *       actualDataNodes (?): # 由数据源名 + 表名组成（参考 Inline 语法规则）
 *       databaseStrategy (?): # 分库策略，缺省表示使用默认分库策略，以下的分片策略只能选其一
 *         standard: # 用于单分片键的标准分片场景
 *           shardingColumn: # 分片列名称
 *           shardingAlgorithmName: # 分片算法名称
 *         complex: # 用于多分片键的复合分片场景
 *           shardingColumns: # 分片列名称，多个列以逗号分隔
 *           shardingAlgorithmName: # 分片算法名称
 *         hint: # Hint 分片策略
 *           shardingAlgorithmName: # 分片算法名称
 *         none: # 不分片
 *       tableStrategy: # 分表策略，同分库策略
 *       keyGenerateStrategy: # 分布式序列策略
 *         column: # 自增列名称，缺省表示不使用自增主键生成器
 *         keyGeneratorName: # 分布式序列算法名称
 *       auditStrategy: # 分片审计策略
 *         auditorNames: # 分片审计算法名称
 *           - <auditor_name>
 *           - <auditor_name>
 *         allowHintDisable: true # 是否禁用分片审计hint
 *   autoTables: # 自动分片表规则配置
 *     t_order_auto: # 逻辑表名称
 *       actualDataSources (?): # 数据源名称
 *       shardingStrategy: # 切分策略
 *         standard: # 用于单分片键的标准分片场景
 *           shardingColumn: # 分片列名称
 *           shardingAlgorithmName: # 自动分片算法名称
 *   bindingTables (+): # 绑定表规则列表
 *     - <logic_table_name_1, logic_table_name_2, ...>
 *     - <logic_table_name_1, logic_table_name_2, ...>
 *   defaultDatabaseStrategy: # 默认数据库分片策略
 *   defaultTableStrategy: # 默认表分片策略
 *   defaultKeyGenerateStrategy: # 默认的分布式序列策略
 *   defaultShardingColumn: # 默认分片列名称
 *
 *   # 分片算法配置
 *   shardingAlgorithms:
 *     <sharding_algorithm_name> (+): # 分片算法名称
 *       type: # 分片算法类型
 *       props: # 分片算法属性配置
 *       # ...
 *
 *   # 分布式序列算法配置
 *   keyGenerators:
 *     <key_generate_algorithm_name> (+): # 分布式序列算法名称
 *       type: # 分布式序列算法类型
 *       props: # 分布式序列算法属性配置
 *       # ...
 *   # 分片审计算法配置
 *   auditors:
 *     <sharding_audit_algorithm_name> (+): # 分片审计算法名称
 *       type: # 分片审计算法类型
 *       props: # 分片审计算法属性配置
 *       # ...
 *
 * https://shardingsphere.apache.org/document/5.5.0/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/sharding/
 */
@Getter
@Setter
public final class ShardingRuleConfiguration implements DatabaseRuleConfiguration, DistributedRuleConfiguration {
    
    private Collection<ShardingTableRuleConfiguration> tables = new LinkedList<>();
    
    private Collection<ShardingAutoTableRuleConfiguration> autoTables = new LinkedList<>();
    
    private Collection<ShardingTableReferenceRuleConfiguration> bindingTableGroups = new LinkedList<>();
    
    private ShardingStrategyConfiguration defaultDatabaseShardingStrategy;
    
    private ShardingStrategyConfiguration defaultTableShardingStrategy;
    
    private KeyGenerateStrategyConfiguration defaultKeyGenerateStrategy;
    
    private ShardingAuditStrategyConfiguration defaultAuditStrategy;
    
    private String defaultShardingColumn;
    
    private Map<String, AlgorithmConfiguration> shardingAlgorithms = new LinkedHashMap<>();
    
    private Map<String, AlgorithmConfiguration> keyGenerators = new LinkedHashMap<>();
    
    private Map<String, AlgorithmConfiguration> auditors = new LinkedHashMap<>();
    
    private ShardingCacheConfiguration shardingCache;
    
    @Override
    public boolean isEmpty() {
        return tables.isEmpty() && autoTables.isEmpty() && null == defaultDatabaseShardingStrategy && null == defaultTableShardingStrategy;
    }
}
