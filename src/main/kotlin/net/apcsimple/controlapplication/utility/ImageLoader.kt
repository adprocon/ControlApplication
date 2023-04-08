package net.apcsimple.controlapplication.utility

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class ImageLoader {

    companion object {
        fun getImage(name: String): BufferedImage? {
            return try {
                ImageIO.read(Companion::class.java.classLoader.getResource(name))
            } catch (e: Exception) {
                null
            }
        }
    }
}