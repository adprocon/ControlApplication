package net.apcsimple.controlapplication

import net.apcsimple.controlapplication.utility.ImageLoader
import org.springframework.boot.context.event.ApplicationContextInitializedEvent
import org.springframework.boot.context.event.ApplicationFailedEvent
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.ContextRefreshedEvent
import sklog.KotlinLogging
import java.awt.*
import java.awt.desktop.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.Timer
import javax.swing.*
import kotlin.concurrent.schedule
import kotlin.system.exitProcess


private val logger = KotlinLogging.logger {}

class SwingUI {
    var frame: JFrame = JFrame()
    private var startButton: JButton = JButton("Start")
    private var stopButton: JButton = JButton("Stop")
    private var setButton: JButton = JButton("Set")
    private var exitButton: JButton = JButton("Exit")
    private var textField: JTextField = JTextField()
    var port: Int = 8080
    var running = false
    private val portLabel = JLabel("Port number: $port")
    val statusLabel = JLabel("Status: Stopped")

    private var controlServerLauncher: ControlServerLauncher? = null
    private lateinit var taskbar: Taskbar
    private var desktop: Desktop
    private var tray: SystemTray
    val statusListener = StatusListener(this)
    val trayStopped = ImageLoader.getImage("trayStopped.png")
    val trayRunning = ImageLoader.getImage("trayRunning.png")
    private var startItem: MenuItem
    private var stopItem: MenuItem
    private val consoleWindow = ConsoleWindow()

    private var testLabel: JLabel

    init {

        val image = ImageLoader.getImage("icon.png")
        try {
            taskbar = Taskbar.getTaskbar()
            taskbar.iconImage = image
        } catch (e: Exception) {
        }

        /* Main application window */
        frame.defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
        val winWidth = 420
        val winHeight = 250
        frame.setSize(winWidth, winHeight)
//        frame.layout = null
        frame.setLocationRelativeTo(null)
        frame.isResizable = false
        frame.iconImage = image

        val mainPanel = JPanel()
        mainPanel.layout = null
        mainPanel.setBounds(10, 10, winWidth - 20, winHeight - 20)

        /* Elements of the main tab */
        val mainText = JTextPane()
        mainText.contentType = "text/html"
        mainText.text = "<html>" +
                "<div style=\"text-align: center; font-family: Arial, Helvetica, sans-serif;\">" +
                "Change port number, if needed, and start the application by pressing \"Start\" button." +
                "</div>" +
                "</html>"
        mainText.setBounds(10, 10, winWidth - 30, 40)
        mainText.border = null
        mainText.background = null
        mainPanel.add(mainText)

        portLabel.setBounds(winWidth - 140, 50, 120, 30)
        mainPanel.add(portLabel)

        statusLabel.setBounds(10, 50, 120, 30)
        mainPanel.add(statusLabel)

        val butY = 100
        val butX = winWidth / 10

        startButton.setBounds(10, butY, 80, 25)
        startButton.addActionListener {
            startAction()
        }
        mainPanel.add(startButton)

        stopButton.setBounds(100, butY, 80, 25)
        stopButton.addActionListener {
            stopAction()
        }
        mainPanel.add(stopButton)

        val portText = JLabel("Port:")
        portText.setBounds(winWidth - 220, butY, 30, 25)
        textField.setBounds(winWidth - 180, butY, 70, 25)
        textField.text = port.toString()
        textField.addActionListener {
            port = setPort(textField.text) ?: port
        }
        mainPanel.add(portText)
        mainPanel.add(textField)

        setButton.setBounds(winWidth - 100, butY, 80, 25)
        setButton.addActionListener {
            port = setPort(textField.text) ?: port
        }
        mainPanel.add(setButton)

        exitButton.setBounds(winWidth / 2 - 40, butY + 40, 80, 25)
        exitButton.addActionListener {
            exitApp()
        }
        mainPanel.add(exitButton)


        val consoleButton = JButton("Log")
        consoleButton.setBounds(winWidth - 70, butY + 40, 50, 25)
        consoleButton.addActionListener {
            consoleWindow.show()
        }
        mainPanel.add(consoleButton)

        /* For testing purposes */
        val osName = System.getProperty("os.name")
        var jarFile = ""
//        jarFile = ControlServer::class.java.protectionDomain.codeSource?.location?.toURI()?.toString()
//            ?: System.getProperty("user.dir")
//        if (osName.contains("Mac OS".toRegex())) {
//            jarFile = """(\/.+\/).+\.jar""".toRegex().find(jarFile)?.groupValues?.get(1) ?: ""
//        }
        testLabel = JLabel()
//        testLabel.lineWrap = true
        testLabel.setBounds(10, butY + 80, winWidth - 30, 25)
        mainPanel.add(testLabel)

        frame.setLocationRelativeTo(null)
        frame.add(mainPanel, BorderLayout.CENTER)

//        val desktop: Desktop
//        if (osName.startsWith("Mac")) {
        /* Handle dock icon click event */
        desktop = Desktop.getDesktop()
        desktop.addAppEventListener(object : AppReopenedListener {

            override fun appReopened(e: AppReopenedEvent?) {
//                logger.info("Application reopened.")
                frame.isVisible = true
            }
        })
//        }


        /* System tray part */
        tray = SystemTray.getSystemTray()
        val icon = ImageLoader.getImage("trayStopped.png")
        val popup = PopupMenu()
        startItem = MenuItem("Start")
        stopItem = MenuItem("Stop")
        val showItem = MenuItem("Show")
        val hideItem = MenuItem("Hide")
        val exitItem = MenuItem("Exit")
        popup.add(startItem)
//        popup.add(stopItem)
        popup.addSeparator()
        popup.add(showItem)
        popup.add(hideItem)
        popup.addSeparator()
        popup.add(exitItem)
        val trayIcon = TrayIcon(icon, "Control Application", popup)
        trayIcon.isImageAutoSize = true
        trayIcon.addActionListener {
            println("Open window on Action")
            show()
        }
        trayIcon.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.button == MouseEvent.BUTTON1) {
                    println("Open window on mouse click")
                    show()
                }
            }
        })
        tray.add(trayIcon)

        startItem.addActionListener {
            startAction()
        }

        stopItem.addActionListener {
            stopAction()
        }

        showItem.addActionListener {
            show()
        }

        hideItem.addActionListener {
            hide()
        }

        exitItem.addActionListener {
            exitApp()
        }

        logger.info("User Interface started.")
    }

    private fun setPort(text: String): Int? {
        var port: Int? = null
        try {
            port = text.toInt()
            portLabel.text = "Port number: $port"
            logger.info("Port number is $port.")
        } catch (ex: NumberFormatException) {
            logger.info("Wrong format for port number.")
        }
        return port
    }

    fun setPortFromArgs(text: String) {
        port = setPort(text) ?: port
        textField.text = port.toString()
    }

    fun show() {
        frame.isVisible = true
        frame.toFront()
    }

    fun hide() {
        frame.isVisible = false
    }

    private fun exitApp() {
        controlServerLauncher?.stop()
        exitProcess(0)
    }

    fun startAction() {
        try {
            if (controlServerLauncher == null) {
                testLabel.text = "Starting..."
                statusLabel.text = "Status: Starting..."
                Timer().schedule(1000) {
                    controlServerLauncher = ControlServerLauncher()
                    controlServerLauncher?.start(port, statusListener)
                }
            }
        } catch (e: Exception) {
            running = false
            logger.error("Application start failed.")
            statusLabel.text = "Status: Stopped"
            e.printStackTrace()
        }
    }

    private fun stopAction() {
        if (controlServerLauncher != null) {
            controlServerLauncher?.stop()
            controlServerLauncher = null
        }
    }

    fun setRunning() {
        running = true
        statusLabel.text = "Status: Running"
        testLabel.text = ""
        tray.trayIcons[0].image = trayRunning
        tray.trayIcons[0].popupMenu.remove(0)
        tray.trayIcons[0].popupMenu.insert(stopItem, 0)
    }

    fun setStopped() {
        running = false
        statusLabel.text = "Status: Stopped"
        testLabel.text = ""
        tray.trayIcons[0].image = trayStopped
        tray.trayIcons[0].popupMenu.remove(0)
        tray.trayIcons[0].popupMenu.insert(startItem, 0)
    }

    fun setError(error: String) {
        running = false
        stopAction()
        statusLabel.text = "Status: Error"
        testLabel.text = error
        tray.trayIcons[0].image = trayStopped
        tray.trayIcons[0].popupMenu.remove(0)
        tray.trayIcons[0].popupMenu.insert(startItem, 0)
    }

}

class StatusListener(val swingUI: SwingUI) : ApplicationListener<ApplicationEvent> {
    override fun onApplicationEvent(event: ApplicationEvent) {
        logger.error("Event is ${event}.")
        when (event) {
            is ContextRefreshedEvent -> {
                logger.warning("Application started.")
                swingUI.setRunning()
            }

            is ContextClosedEvent -> {
                logger.warning("Application stopped.")
                swingUI.setStopped()
            }

            is ApplicationContextInitializedEvent -> {
                logger.warning("Application starting.")
            }

            is ApplicationFailedEvent -> {
                logger.warning("Application starting failed.")
                swingUI.setError("Application starting failed. See Log for details.")
            }
        }
    }
}