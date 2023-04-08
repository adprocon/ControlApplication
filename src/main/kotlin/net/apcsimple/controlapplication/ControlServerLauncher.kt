package net.apcsimple.controlapplication

import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext


class ControlServerLauncher() {
    var app: SpringApplication? = null
    private var appContext: ConfigurableApplicationContext? = null

    fun start(port: Int, statusListener: StatusListener) {
        val properties = mapOf(
            "--server.port" to port
        )
        app = SpringApplication(ControlServer::class.java)
        app!!.addListeners(statusListener)
        appContext = app?.run(*arrayOf(), *properties.entries.map { "${it.key}=${it.value}" }.toTypedArray())
    }

    fun stop() {
        appContext?.close()
    }
}
