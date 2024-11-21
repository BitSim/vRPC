package com.bitsim.vrpc.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 *@author  BitSim
 *@version  v1.0.0

 **/
object Config{
    private val properties: Properties = Properties()
    private val yamlMapper: ObjectMapper = ObjectMapper(YAMLFactory())
    fun loadConfig() {
        var configLoaded = false

        // 读取 properties 文件
        val propertiesFile = File("src/main/resources/vrpc.properties")
        if (propertiesFile.exists()) {
            FileInputStream(propertiesFile).use { properties.load(it) }
            configLoaded = true
        }

        // 读取 yaml 文件
        val yamlFile = File("src/main/resources/vrpc.yml")
        if (yamlFile.exists()) {
            @Suppress("UNCHECKED_CAST")
            val yamlConfig: Map<String, Any> =
                yamlMapper.readValue(yamlFile, Map::class.java) as Map<String, Any>
            flattenYamlConfig(yamlConfig, "")
            configLoaded = true
        }

        // 如果没有加载任何配置，抛出异常
        if (!configLoaded) {
            throw IllegalStateException("Both 'vrpc.properties' and 'vrpc.yml' are missing!")
        }
    }

    private fun flattenYamlConfig(map: Map<String, Any>, parentKey: String) {
        for ((key, value) in map) {
            val fullKey = if (parentKey.isEmpty()) key else "$parentKey.$key"
            if (value is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                flattenYamlConfig(value as Map<String, Any>, fullKey)
            } else {
                properties[fullKey] = value.toString()
            }
        }
    }
    fun getProperty(key: String): String? {
        return properties.getProperty(key)
    }
}
