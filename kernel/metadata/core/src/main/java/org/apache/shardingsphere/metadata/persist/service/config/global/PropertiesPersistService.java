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

package org.apache.shardingsphere.metadata.persist.service.config.global;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.metadata.persist.node.GlobalNode;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Properties persist service.
 */
@RequiredArgsConstructor
public final class PropertiesPersistService implements GlobalPersistService<Properties> {
    
    private static final String DEFAULT_VERSION = "0";
    
    private final PersistRepository repository;
    
    @Override
    public void persist(final Properties props) {
        /**
         * /props/versions/[version] = props
         */
        if (Strings.isNullOrEmpty(getActiveVersion())) {
            repository.persist(GlobalNode.getPropsActiveVersionNode(), DEFAULT_VERSION);
        }
        List<String> versions = repository.getChildrenKeys(GlobalNode.getPropsVersionsNode());
        repository.persist(GlobalNode.getPropsVersionNode(versions.isEmpty()
                ? DEFAULT_VERSION
                : String.valueOf(Integer.parseInt(versions.get(0)) + 1)), YamlEngine.marshal(props));
        
    }
    
    @Override
    public Collection<MetaDataVersion> persistConfig(final Properties props) {
        List<String> versions = repository.getChildrenKeys(GlobalNode.getPropsVersionsNode());
        String nextActiveVersion = versions.isEmpty() ? DEFAULT_VERSION : String.valueOf(Integer.parseInt(versions.get(0)) + 1);
        String persistKey = GlobalNode.getPropsVersionNode(nextActiveVersion);
        repository.persist(persistKey, YamlEngine.marshal(props));
        if (Strings.isNullOrEmpty(getActiveVersion())) {
            repository.persist(GlobalNode.getPropsActiveVersionNode(), DEFAULT_VERSION);
        }
        return Collections.singletonList(new MetaDataVersion(GlobalNode.getPropsRootNode(), getActiveVersion(), nextActiveVersion));
    }
    
    @Override
    public Properties load() {
        // /props/versions/[version]
        String yamlContent = repository.getDirectly(GlobalNode.getPropsVersionNode(getActiveVersion()));
        return Strings.isNullOrEmpty(yamlContent) ? new Properties() : YamlEngine.unmarshal(yamlContent, Properties.class);
    }
    
    private String getActiveVersion() {
        // /props/active_version=
        return repository.getDirectly(GlobalNode.getPropsActiveVersionNode());
    }
}
