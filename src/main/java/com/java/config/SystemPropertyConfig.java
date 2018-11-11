package com.java.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.core.util.Yaml;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

public class SystemPropertyConfig extends ConfigurationImpl implements Configuration {
    public SystemPropertyConfig() {
        Properties properties = System.getProperties();

        JsonNode parent = Yaml.mapper().createObjectNode();
        for(Object propertyKey: properties.keySet()) {
            Object keyObject = propertyKey.toString();
            Object valueObject = properties.get(keyObject);

            String key = keyObject.toString();
            String value = valueObject.toString();

            try {
                JsonNode part = Yaml.mapper().readTree(propertySetToYaml(key, value));
                merge(parent, part);
            } catch (IOException e) {
                e.printStackTrace();;
            }
        }
        String yaml = "";
        try {
            yaml = Yaml.mapper().writeValueAsString(parent);
            super.fromContents(yaml);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("unable to load from properties", e);
        }
    }

    public static String propertySetToYaml(String key, String value) {
        StringBuilder builder = new StringBuilder();
        int position = 0;

        if(key.indexOf("__") > 0) {
            for(String part : key.split("__")) {
                if(builder.toString().length() > 0) {
                    builder.append("\n");
                    builder.append(indent(position * 2)); // indent two
                }
                builder.append(part).append(":");
                position ++;
            }
        }
        else {
            for (String part : key.split("\\.")) {
                if (builder.toString().length() > 0) {
                    builder.append("\n");
                    builder.append(indent(position * 2)); // indent two
                }
                builder.append(part).append(":");
                position++;
            }
        }

        builder.append(" \"").append(value.replace("\"", "\\\"")).append("\"");

        return builder.toString();
    }

    public static JsonNode merge(JsonNode mainNode, JsonNode updateNode) {
        Iterator<String> fieldNames = updateNode.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode jsonNode = mainNode.get(fieldName);
            if (jsonNode != null && jsonNode.isObject()) {
                merge(jsonNode, updateNode.get(fieldName));
            }
            else {
                if (mainNode instanceof ObjectNode) {
                    JsonNode value = updateNode.get(fieldName);
                    ((ObjectNode) mainNode).put(fieldName, value);
                }
            }

        }

        return mainNode;
    }

    public static String indent(int depth) {
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < depth; i++) {
            b.append(" ");
        }
        return b.toString();
    }
}

