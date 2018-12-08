package org.mo39.fmbh.common.enriched
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.util.Base64

import javax.imageio.ImageIO
import java.awt.image

import scala.language.implicitConversions

object BufferedImage {
  implicit def toEnrichedBufferedImage(img: image.BufferedImage): BufferedImage = BufferedImage(img)
}

/**
  * Enriched [[java.awt.image.BufferedImage]]
  *
  * @param img [[java.awt.image.BufferedImage]] instance
  */
case class BufferedImage(img: image.BufferedImage) {

  /**
    * Loads the image data from 2d int array into the [[img]]
    *
    * @param arr 2d array
    * @return
    */
  def from(arr: Array[Array[Int]]): image.BufferedImage = {
    for (i <- 0 until img.getWidth) {
      for (j <- 0 until img.getHeight) {
        val v = arr(i)(j)
        img.setRGB(j, i, new Color(v, v, v).getRGB)
      }
    }
    img
  }

  /**
    * Generates the base64 image url for the [[img]]
    *
    * @return
    */
  def toBase64Url: String = {
    val os: ByteArrayOutputStream = new ByteArrayOutputStream()
    ImageIO.write(img, "jpg", os)
    "data:image/jpeg;base64," + Base64.getEncoder.encodeToString(os.toByteArray)
  }

}
