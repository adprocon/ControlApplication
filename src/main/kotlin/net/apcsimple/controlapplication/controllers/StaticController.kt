package net.apcsimple.controlapplication.controllers

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import sklog.KotlinLogging

private var logger = KotlinLogging.logger {}

@RestController
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
class StaticController {

//    @GetMapping("*", "*/*", "*/*/*", "*/*/*/*")
    @GetMapping("/*", "/ui/**")
    fun getIndex(): String {
        logger.info("Static controller activated.")
        val classLoader = Thread.currentThread().contextClassLoader
        val inputStream = classLoader.getResourceAsStream("static/index.html")
        return inputStream.bufferedReader().use { it.readText() }
//        return "static/index.html"
    }


}