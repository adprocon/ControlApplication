package net.apcsimple.controlapplication

import net.apcsimple.controlapplication.utility.ImageLoader
import java.awt.BorderLayout
import java.awt.Font
import java.awt.GridLayout
import java.io.OutputStream
import java.io.PrintStream
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.WindowConstants

class ConsoleWindow {

    val frame = JFrame()

    init {

        val image = ImageLoader.getImage("icon.png")

        /* Main application window */
        frame.defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
        val winWidth = 1500
        val winHeight = 600
        frame.setSize(winWidth, winHeight)
//        frame.layout = null
        frame.setLocationRelativeTo(null)
        frame.isResizable = true
        frame.iconImage = image

        /* Console */
        val console = JTextArea()
        console.lineWrap = true
        console.font = Font("Mono", 0, 9)
        console.isEditable = false
        val inStream = System.`in`
        val outStream = PrintStream(ConsoleOutputStream(console))
        System.setIn(inStream)
        System.setOut(outStream)
        System.setErr(outStream)
        val scrollPane = JScrollPane(console)
        frame.add(scrollPane, BorderLayout.CENTER)
    }

    fun show() {
        frame.isVisible = true;
    }

    fun hide() {
        frame.isVisible = false;
    }
}

class ConsoleOutputStream(private val console: JTextArea) : OutputStream() {
    override fun write(b: Int) {
        console.append(b.toChar().toString())
    }
}