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

package org.apache.shardingsphere.infra.util.yaml;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.util.yaml.constructor.ShardingSphereYamlConstructor;
import org.apache.shardingsphere.infra.util.yaml.representer.ShardingSphereYamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * YAML engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlEngine {
    
    /**
     * Unmarshal YAML.
     *
     * @param yamlFile YAML file
     * @param classType class type
     * @param <T> type of class
     * @return object from YAML
     * @throws IOException IO Exception
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static <T extends YamlConfiguration> T unmarshal(final File yamlFile, final Class<T> classType) throws IOException {
        try (BufferedReader inputStreamReader = Files.newBufferedReader(Paths.get(yamlFile.toURI()))) {
            T result = new Yaml(new ShardingSphereYamlConstructor(classType)).loadAs(inputStreamReader, classType);
            return null == result ? classType.getConstructor().newInstance() : result;
        }
    }
    
    /**
     * Unmarshal YAML.
     *
     * @param yamlBytes YAML bytes
     * @param classType class type
     * @param <T> type of class
     * @return object from YAML
     * @throws IOException IO Exception
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static <T extends YamlConfiguration> T unmarshal(final byte[] yamlBytes, final Class<T> classType) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(yamlBytes)) {
            T result = new Yaml(new ShardingSphereYamlConstructor(classType)).loadAs(inputStream, classType);
            return null == result ? classType.getConstructor().newInstance() : result;
        }
    }
    
    /**
     * Unmarshal YAML.
     *
     * @param yamlContent YAML content
     * @param classType class type
     * @param <T> type of class
     * @return object from YAML
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static <T> T unmarshal(final String yamlContent, final Class<T> classType) {
        T result = new Yaml(new ShardingSphereYamlConstructor(classType)).loadAs(yamlContent, classType);
        return null == result ? classType.getConstructor().newInstance() : result;
    }
    
    /**
     * Unmarshal YAML.
     *
     * @param yamlContent YAML content
     * @param classType class type
     * @param skipMissingProps true if missing properties should be skipped, false otherwise
     * @param <T> type of class
     * @return object from YAML
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static <T> T unmarshal(final String yamlContent, final Class<T> classType, final boolean skipMissingProps) {
        Representer representer = new Representer(new DumperOptions());
        representer.getPropertyUtils().setSkipMissingProperties(skipMissingProps);
        T result = new Yaml(new ShardingSphereYamlConstructor(classType), representer).loadAs(yamlContent, classType);
        return null == result ? classType.getConstructor().newInstance() : result;
    }
    
    /**
     * Marshal YAML.
     *
     * @param value object to be marshaled
     * @return YAML content
     */
    public static String marshal(final Object value) {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setLineBreak(DumperOptions.LineBreak.getPlatformLineBreak());
        if (value instanceof Collection) {
            return new Yaml(new ShardingSphereYamlRepresenter(dumperOptions), dumperOptions).dumpAs(value, null, DumperOptions.FlowStyle.BLOCK);
        }
        return new Yaml(new ShardingSphereYamlRepresenter(dumperOptions), dumperOptions).dumpAsMap(value);
    }
}
