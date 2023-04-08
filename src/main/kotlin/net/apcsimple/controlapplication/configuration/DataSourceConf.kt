package net.apcsimple.controlapplication.configuration

import net.apcsimple.controlapplication.ControlServer
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import sklog.KotlinLogging
import java.io.File
import javax.sql.DataSource

private val logger = KotlinLogging.logger {}

@Configuration
class DataSourceConf {

    @Bean
    fun dataSource(): DataSource {
        val dataSourceBuilder = DataSourceBuilder.create()
        val osName = System.getProperty("os.name")
        val jarFile = ControlServer::class.java.protectionDomain.codeSource?.location?.toURI()?.toString()
            ?: ""
        /* Required for Mac OS JavaLauncher */
        val currentPath = if (osName.contains("Mac OS".toRegex()) && jarFile.contains("BOOT-INF".toRegex())) {
            """(\/.+\/).+\.jar""".toRegex().find(jarFile)?.groupValues?.get(1) ?: ""
        } else {
            ""
        }
        println("#################Current path: " + currentPath)
        dataSourceBuilder.driverClassName("org.sqlite.JDBC")
        dataSourceBuilder.url("jdbc:sqlite:${currentPath}database.db")
        return dataSourceBuilder.build()
    }
}