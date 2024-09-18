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

package org.apache.shardingsphere.sharding.rule;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.infra.datanode.DataNodeUtils;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.expr.core.InlineExpressionParserFactory;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.exception.metadata.DataNodeGenerateException;
import org.apache.shardingsphere.sharding.exception.metadata.MissingRequiredDataNodesException;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Sharding table.
 */
@Getter
@ToString(exclude = {"dataNodeIndexMap", "actualTables", "actualDataSourceNames", "dataSourceDataNode", "tableDataNode"})
public final class ShardingTable {
    
    private static final Pattern DATA_NODE_SUFFIX_PATTERN = Pattern.compile("\\d+$");
    
    private static final char DEFAULT_PADDING_CHAR = '0';
    /**
     * <logic_table_name> (+): # 逻辑表名称
     */
    private final String logicTable;
    /**
     * 数据节点（数据源名 + 表名）
     * 1.解析actualDataNodes ds_${0..1}.t_order_${0..1} -> [ds_0.t_order_0, ds_0.t_order_1, ds_1.t_order_0, ds_1.t_order_1]
     * 2. 自动分片表根据自动分片算法生成: 数据源名称.逻辑表名称_i
     * 未配置actualDataNodes时，为数据源名 + 逻辑表名
     */
    private final List<DataNode> actualDataNodes;
    /**
     * 实际的各个数据源下的物理表去重
     */
    @Getter(AccessLevel.NONE)
    private final Set<String> actualTables;

    /**
     * 数据节点（数据源名 + 表名）对应的索引
     */
    @Getter(AccessLevel.NONE)
    private final Map<DataNode, Integer> dataNodeIndexMap;
    /**
     * databaseStrategy (?): # 分库策略
     * 自动分片表无需分库策略 使用
     * @see NoneShardingStrategyConfiguration
     */
    private final ShardingStrategyConfiguration databaseShardingStrategyConfig;
    /**
     * tableStrategy: # 分表策略
     * 自动分片表 shardingStrategy: # 切分策略
     */
    private final ShardingStrategyConfiguration tableShardingStrategyConfig;
    /**
     * auditStrategy: # 分片审计策略
     */
    private final ShardingAuditStrategyConfiguration auditStrategyConfig;

    /**
     * keyGenerateStrategy: # 分布式序列策略
     *   column: # 自增列名称，缺省表示不使用自增主键生成器
     * 如果为空使用 默认的 defaultKeyGenerateStrategy
     */
    @Getter(AccessLevel.NONE)
    private final String generateKeyColumn;
    /**
     *       keyGenerateStrategy: # 分布式序列策略
     *         keyGeneratorName: # 分布式序列算法名称
     */
    private final String keyGeneratorName;
    /**
     * 解析配置的 actualDataNodes 用到的数据源名或数据源列表
     */
    private final Collection<String> actualDataSourceNames = new LinkedHashSet<>();

    /**
     * 数据源下包含的的物理表名
     * key: 数据源名称
     * value: 解析actualDataNodes配置的表名或逻辑表名
     */
    private final Map<String, Collection<String>> dataSourceToTablesMap = new HashMap<>();
    
    private final DataNodeInfo dataSourceDataNode;
    
    private final DataNodeInfo tableDataNode;
    
    public ShardingTable(final Collection<String> dataSourceNames, final String logicTableName) {
        logicTable = logicTableName;
        dataNodeIndexMap = new HashMap<>(dataSourceNames.size(), 1F);
        actualDataNodes = generateDataNodes(logicTableName, dataSourceNames);
        actualTables = getActualTables();
        databaseShardingStrategyConfig = null;
        tableShardingStrategyConfig = null;
        auditStrategyConfig = null;
        generateKeyColumn = null;
        keyGeneratorName = null;
        dataSourceDataNode = actualDataNodes.isEmpty() ? null : createDataSourceDataNode(actualDataNodes);
        tableDataNode = actualDataNodes.isEmpty() ? null : createTableDataNode(actualDataNodes);
    }
    
    public ShardingTable(final ShardingTableRuleConfiguration tableRuleConfig, final Collection<String> dataSourceNames, final String defaultGenerateKeyColumn) {
        logicTable = tableRuleConfig.getLogicTable();
        /**
         * 解析actualDataNodes (?): # 由数据源名 + 表名组成
         * ds_${0..1}.t_order_${0..1} -> [ds_0.t_order_0, ds_0.t_order_1, ds_1.t_order_0, ds_1.t_order_1]
         */
        List<String> dataNodes = InlineExpressionParserFactory.newInstance(tableRuleConfig.getActualDataNodes()).splitAndEvaluate();
        dataNodeIndexMap = new HashMap<>(dataNodes.size(), 1F);
        /**
         * 没有配置actualDataNodes 则使用配置的数据源名称 + 逻辑表名
         */
        actualDataNodes = isEmptyDataNodes(dataNodes) ? generateDataNodes(tableRuleConfig.getLogicTable(), dataSourceNames) : generateDataNodes(dataNodes, dataSourceNames);
        /**
         * 物理表名去重
         */
        actualTables = getActualTables();
        /**
         * 分库策略、分表策略、分片审计策略
         */
        databaseShardingStrategyConfig = tableRuleConfig.getDatabaseShardingStrategy();
        tableShardingStrategyConfig = tableRuleConfig.getTableShardingStrategy();
        auditStrategyConfig = tableRuleConfig.getAuditStrategy();
        /**
         * keyGenerateStrategy: # 分布式序列策略
         */
        KeyGenerateStrategyConfiguration keyGeneratorConfig = tableRuleConfig.getKeyGenerateStrategy();
        generateKeyColumn = null == keyGeneratorConfig || Strings.isNullOrEmpty(keyGeneratorConfig.getColumn()) ? defaultGenerateKeyColumn : keyGeneratorConfig.getColumn();
        keyGeneratorName = null == keyGeneratorConfig ? null : keyGeneratorConfig.getKeyGeneratorName();

        dataSourceDataNode = actualDataNodes.isEmpty() ? null : createDataSourceDataNode(actualDataNodes);
        tableDataNode = actualDataNodes.isEmpty() ? null : createTableDataNode(actualDataNodes);
        checkRule(dataNodes);
    }
    
    public ShardingTable(final ShardingAutoTableRuleConfiguration tableRuleConfig, final Collection<String> dataSourceNames,
                         final ShardingAutoTableAlgorithm shardingAutoTableAlgorithm, final String defaultGenerateKeyColumn) {
        logicTable = tableRuleConfig.getLogicTable();
        databaseShardingStrategyConfig = new NoneShardingStrategyConfiguration();
        tableShardingStrategyConfig = tableRuleConfig.getShardingStrategy();
        auditStrategyConfig = tableRuleConfig.getAuditStrategy();
        /**
         * 根据自动分片算法 生成 实际物理表名列表: 数据源名称.逻辑表名称_i
         */
        List<String> dataNodes = getDataNodes(tableRuleConfig, shardingAutoTableAlgorithm, dataSourceNames);
        dataNodeIndexMap = new HashMap<>(dataNodes.size(), 1F);
        actualDataNodes = isEmptyDataNodes(dataNodes) ? generateDataNodes(tableRuleConfig.getLogicTable(), dataSourceNames) : generateDataNodes(dataNodes, dataSourceNames);
        actualTables = getActualTables();
        KeyGenerateStrategyConfiguration keyGeneratorConfig = tableRuleConfig.getKeyGenerateStrategy();
        generateKeyColumn = null == keyGeneratorConfig || Strings.isNullOrEmpty(keyGeneratorConfig.getColumn()) ? defaultGenerateKeyColumn : keyGeneratorConfig.getColumn();
        keyGeneratorName = null == keyGeneratorConfig ? null : keyGeneratorConfig.getKeyGeneratorName();
        dataSourceDataNode = actualDataNodes.isEmpty() ? null : createDataSourceDataNode(actualDataNodes);
        tableDataNode = actualDataNodes.isEmpty() ? null : createTableDataNode(actualDataNodes);
        checkRule(dataNodes);
    }
    
    private DataNodeInfo createDataSourceDataNode(final Collection<DataNode> actualDataNodes) {
        String prefix = DATA_NODE_SUFFIX_PATTERN.matcher(actualDataNodes.iterator().next().getDataSourceName()).replaceAll("");
        int suffixMinLength = actualDataNodes.stream().map(each -> each.getDataSourceName().length() - prefix.length()).min(Comparator.comparing(Integer::intValue)).orElse(1);
        return new DataNodeInfo(prefix, suffixMinLength, DEFAULT_PADDING_CHAR);
    }
    
    private DataNodeInfo createTableDataNode(final Collection<DataNode> actualDataNodes) {
        String tableName = actualDataNodes.iterator().next().getTableName();
        String prefix = tableName.startsWith(logicTable) ? logicTable + DATA_NODE_SUFFIX_PATTERN.matcher(tableName.substring(logicTable.length())).replaceAll("")
                : DATA_NODE_SUFFIX_PATTERN.matcher(tableName).replaceAll("");
        int suffixMinLength = actualDataNodes.stream().map(each -> each.getTableName().length() - prefix.length()).min(Comparator.comparing(Integer::intValue)).orElse(1);
        return new DataNodeInfo(prefix, suffixMinLength, DEFAULT_PADDING_CHAR);
    }
    
    private List<String> getDataNodes(final ShardingAutoTableRuleConfiguration tableRuleConfig, final ShardingAutoTableAlgorithm shardingAlgorithm, final Collection<String> dataSourceNames) {
        if (null == tableShardingStrategyConfig) {
            return new LinkedList<>();
        }
        /**
         * 解析actualDataSources (?): # 数据源名称  没有使用数据源列表
         */
        List<String> dataSources = Strings.isNullOrEmpty(tableRuleConfig.getActualDataSources()) ? new LinkedList<>(dataSourceNames)
                : InlineExpressionParserFactory.newInstance(tableRuleConfig.getActualDataSources()).splitAndEvaluate();
        /**
         * 根据自动分片算法 生成 实际数据源+表 列表
         * 数据源名称.逻辑表名称_i
         */
        return DataNodeUtils.getFormatDataNodes(shardingAlgorithm.getAutoTablesAmount(), logicTable, dataSources);
    }
    
    private Set<String> getActualTables() {
        return actualDataNodes.stream().map(DataNode::getTableName).collect(Collectors.toCollection(() -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER)));
    }
    
    private void addActualTable(final String datasourceName, final String tableName) {
        dataSourceToTablesMap.computeIfAbsent(datasourceName, key -> new LinkedHashSet<>()).add(tableName);
    }
    
    private boolean isEmptyDataNodes(final List<String> dataNodes) {
        return null == dataNodes || dataNodes.isEmpty();
    }
    
    private List<DataNode> generateDataNodes(final String logicTable, final Collection<String> dataSourceNames) {
        List<DataNode> result = new LinkedList<>();
        int index = 0;
        for (String each : dataSourceNames) {
            DataNode dataNode = new DataNode(each, logicTable);
            result.add(dataNode);
            dataNodeIndexMap.put(dataNode, index);
            actualDataSourceNames.add(each);
            addActualTable(dataNode.getDataSourceName(), dataNode.getTableName());
            index++;
        }
        return result;
    }
    
    private List<DataNode> generateDataNodes(final List<String> actualDataNodes, final Collection<String> dataSourceNames) {
        List<DataNode> result = new LinkedList<>();
        int index = 0;
        for (String each : actualDataNodes) {
            DataNode dataNode = new DataNode(each);
            /**
             * actualDataNodes使用的数据源但在dataSources数据源里未配置，抛异常
             */
            if (!dataSourceNames.contains(dataNode.getDataSourceName())) {
                throw new DataNodeGenerateException(each);
            }
            result.add(dataNode);
            dataNodeIndexMap.put(dataNode, index);
            actualDataSourceNames.add(dataNode.getDataSourceName());
            addActualTable(dataNode.getDataSourceName(), dataNode.getTableName());
            index++;
        }
        return result;
    }
    
    /**
     * Get data node groups.
     *
     * @return data node groups, key is data source name, values are data nodes belong to this data source
     */
    public Map<String, List<DataNode>> getDataNodeGroups() {
        return DataNodeUtils.getDataNodeGroups(actualDataNodes);
    }
    
    /**
     * Get actual data source names.
     *
     * @return actual data source names
     */
    public Collection<String> getActualDataSourceNames() {
        return actualDataSourceNames;
    }
    
    /**
     * Get actual table names via target data source name.
     *
     * @param targetDataSource target data source name
     * @return names of actual tables
     */
    public Collection<String> getActualTableNames(final String targetDataSource) {
        return dataSourceToTablesMap.getOrDefault(targetDataSource, Collections.emptySet());
    }
    
    /**
     * Find actual table index.
     * 
     * @param dataSourceName data source name
     * @param actualTableName actual table name
     * @return actual table index
     */
    public int findActualTableIndex(final String dataSourceName, final String actualTableName) {
        return dataNodeIndexMap.getOrDefault(new DataNode(dataSourceName, actualTableName), -1);
    }
    
    /**
     * Is existed.
     * 
     * @param actualTableName actual table name
     * @return is existed or not
     */
    public boolean isExisted(final String actualTableName) {
        return actualTables.contains(actualTableName);
    }
    
    private void checkRule(final List<String> dataNodes) {
        ShardingSpherePreconditions.checkState(!isEmptyDataNodes(dataNodes) || null == tableShardingStrategyConfig || tableShardingStrategyConfig instanceof NoneShardingStrategyConfiguration,
                () -> new MissingRequiredDataNodesException(logicTable));
    }
    
    /**
     * Get generate key column.
     *
     * @return generate key column
     */
    public Optional<String> getGenerateKeyColumn() {
        return Optional.ofNullable(generateKeyColumn);
    }
}
