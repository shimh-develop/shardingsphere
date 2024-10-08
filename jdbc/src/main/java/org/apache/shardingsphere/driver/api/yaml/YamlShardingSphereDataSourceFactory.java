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

package org.apache.shardingsphere.driver.api.yaml;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.YamlModeConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ShardingSphere data source factory for YAML.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlShardingSphereDataSourceFactory {
    
    /**
     * Create ShardingSphere data source.
     * 
     * @param yamlFile YAML file for rule configurations
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final File yamlFile) throws SQLException, IOException {
        YamlJDBCConfiguration rootConfig = YamlEngine.unmarshal(yamlFile, YamlJDBCConfiguration.class);
        return createDataSource(new YamlDataSourceConfigurationSwapper().swapToDataSources(rootConfig.getDataSources()), rootConfig);
    }
    
    /**
     * Create ShardingSphere data source.
     * 
     * @param yamlBytes YAML bytes for rule configurations
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final byte[] yamlBytes) throws SQLException, IOException {
        /**
         * 将配置文件的内容映射到YamlJDBCConfiguration
         *
         * # JDBC 逻辑库名称。在集群模式中，使用该参数来联通 ShardingSphere-JDBC 与 ShardingSphere-Proxy。
         * # 默认值：logic_db
         * databaseName (?):
         *
         * mode:
         *
         * dataSources:
         *
         * rules:
         * - !FOO_XXX
         *     ...
         * - !BAR_XXX
         *     ...
         *
         * props:
         *   key_1: value_1
         *   key_2: value_2
         */
        YamlJDBCConfiguration rootConfig = YamlEngine.unmarshal(yamlBytes, YamlJDBCConfiguration.class);
        /**
         *
         * 以数据库连接池自身配置为准 实例化连接池 如 HikariCP、Druid
         *
         */
        return createDataSource(new YamlDataSourceConfigurationSwapper().swapToDataSources(rootConfig.getDataSources()), rootConfig);
    }
    
    /**
     * Create ShardingSphere data source.
     * 
     * @param dataSourceMap data source map
     * @param yamlFile YAML file for rule configurations
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final File yamlFile) throws SQLException, IOException {
        return createDataSource(dataSourceMap, YamlEngine.unmarshal(yamlFile, YamlJDBCConfiguration.class));
    }
    
    /**
     * Create ShardingSphere data source.
     * 
     * @param dataSource data source
     * @param yamlFile YAML file for rule configurations
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final DataSource dataSource, final File yamlFile) throws SQLException, IOException {
        return createDataSource(dataSource, YamlEngine.unmarshal(yamlFile, YamlJDBCConfiguration.class));
    }
    
    /**
     * Create ShardingSphere data source.
     * 
     * @param dataSourceMap data source map
     * @param yamlBytes YAML bytes for rule configurations
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final byte[] yamlBytes) throws SQLException, IOException {
        return createDataSource(dataSourceMap, YamlEngine.unmarshal(yamlBytes, YamlJDBCConfiguration.class));
    }
    
    /**
     * Create ShardingSphere data source.
     * 
     * @param dataSource data source
     * @param yamlBytes YAML bytes for rule configurations
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final DataSource dataSource, final byte[] yamlBytes) throws SQLException, IOException {
        return createDataSource(dataSource, YamlEngine.unmarshal(yamlBytes, YamlJDBCConfiguration.class));
    }
    
    private static DataSource createDataSource(final DataSource dataSource, final YamlJDBCConfiguration jdbcConfig) throws SQLException {
        Map<String, DataSource> dataSourceMap = new LinkedHashMap<>(
                Collections.singletonMap(Strings.isNullOrEmpty(jdbcConfig.getDatabaseName()) ? DefaultDatabase.LOGIC_NAME : jdbcConfig.getDatabaseName(), dataSource));
        return createDataSource(dataSourceMap, jdbcConfig);
    }
    
    private static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final YamlJDBCConfiguration jdbcConfig) throws SQLException {
        /**
         * 模式配置映射
         * mode:
         *   type: Standalone
         *   repository:
         *     type: JDBC
         *     props:
         *       provider: H2
         *       jdbc_url: jdbc:h2:mem:config;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
         *       username: test
         *       password: Test@123
         *
         * https://shardingsphere.apache.org/document/5.5.0/cn/user-manual/shardingsphere-jdbc/yaml-config/mode/
         */
        ModeConfiguration modeConfig = null == jdbcConfig.getMode() ? null : new YamlModeConfigurationSwapper().swapToObject(jdbcConfig.getMode());
        jdbcConfig.rebuild();
        /**
         * 规则配置映射
         * 数据分片、读写分离等待 转换为 RuleConfiguration对象
         * https://shardingsphere.apache.org/document/5.5.0/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/
         */
        Collection<RuleConfiguration> ruleConfigs = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(jdbcConfig.getRules());
        /**
         * 创建ShardingSphereDataSource
         */
        return ShardingSphereDataSourceFactory.createDataSource(jdbcConfig.getDatabaseName(), modeConfig, dataSourceMap, ruleConfigs, jdbcConfig.getProps());
    }
}
