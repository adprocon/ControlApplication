package net.apcsimple.controlapplication

import net.apcsimple.controlapplication.model.communication.InterfaceList
import net.apcsimple.controlapplication.model.datapoints.TagList
import net.apcsimple.controlapplication.model.processcontrollers.ControllersList
import net.apcsimple.controlapplication.model.processmodels.ModelsList
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import sklog.KotlinLogging
import java.util.Timer
import kotlin.concurrent.timerTask

private val logger = KotlinLogging.logger {}

/**
 * The main application that handles required functionality.
 */
@SpringBootApplication
class ControlServer(
    val tagList: TagList,
    val interfaceList: InterfaceList,
    val modelsList: ModelsList,
    val controllersList: ControllersList
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        tagList.load()
        for (tag in tagList.list.values) {
            logger.info(tag.toString())
        }
        interfaceList.load()
        for (intr in interfaceList.list) {
            logger.info(intr.toString())
        }
        modelsList.load()
        for (mdl in modelsList.list) {
            logger.info(mdl.toString())
        }
        controllersList.load()
        for (ctrl in controllersList.list) {
            logger.info(ctrl.toString())
        }
    }
}

fun main(args: Array<String>) {

    /* Do not put app to the dock - DO NOT UNCOMMENT*/
//    System.setProperty("apple.awt.UIElement", "true")

    val swingUI = SwingUI()

    /* Handle parameters */
    var hidden = false
    var autostart = false
    if (args.isNotEmpty()) {
        if (args.contains("-port")) {
            val i = args.indexOf("-port")
            if (args.size >= i) {
                swingUI.setPortFromArgs(args[i + 1])
            }
        }
        if (args.contains("-minimized")) {
            hidden = true
        }
        if (args.contains("-autostart")) {
            autostart = true
        }
    }

    if (!hidden) {
        swingUI.show()
    }

    if (autostart) {
        Timer().schedule(timerTask {
            swingUI.startAction()
        }, 2000)
    }

//    runApplication<ControlServer>(*args)

}