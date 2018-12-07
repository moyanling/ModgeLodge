package org.mo39.fmbh.mnist

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.Base64

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator
import org.nd4j.evaluation.classification.Evaluation
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.layers.{ DenseLayer, OutputLayer }
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.learning.config.Sgd
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction
import com.typesafe.scalalogging.LazyLogging
import javax.imageio.ImageIO
import org.nd4j.linalg.api.ndarray.INDArray

import scala.collection.JavaConverters._

object MNIST extends App with LazyLogging {

  def mkBase64ImgUrl(array: INDArray): String = {
    // Check array size
    require(array.rank() == 2)
    require(array.rows() * array.columns() == 28 * 28)
    // Convert to 28 * 28 array
    val arr = array
      .reshape(28, 28)
      .toDoubleMatrix
      .map(_.map(v => 255 - (v * 255).toInt))
    // Build BufferedImage
    val img = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY)
    for (i <- 0 until 28) {
      for (j <- 0 until 28) {
        val v = arr(i)(j)
        img.setRGB(j, i, new Color(v, v, v).getRGB)
      }
    }
    // BufferedImage to base64 string
    val os: ByteArrayOutputStream = new ByteArrayOutputStream()
    ImageIO.write(img, "jpg", os)
    "data:image/jpeg;base64," + Base64.getEncoder.encodeToString(os.toByteArray)
  }

  val seed = 42 // fixed random seed for reproducibility
  val numInputs = 28 * 28
  val numHidden = 512 // size (number of neurons) of our hidden layer
  val numOutputs = 10 // digits from 0 to 9
  val learningRate = 0.01
  val batchSize = 128
  val numEpochs = 10

//   download and load the MNIST images as tensors
  val mnistTrain = new MnistDataSetIterator(batchSize, true, seed)
  val mnistTest = new MnistDataSetIterator(batchSize, false, seed)

  val list = mnistTrain.asScala.toList
  val arr = list.head.getFeatures.getRow(0)
//  println(mkBase64ImgUrl(arr))
//  println(list.head.getLabels.getRow(0))

  val sec = list.head.getFeatures.getRow(1)
  println(mkBase64ImgUrl(sec))
  println(list.head.getLabels.getRow(1))

  // define the neural network architecture
  val conf = new NeuralNetConfiguration.Builder()
    .seed(seed)
    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
    .updater(new Sgd(learningRate))
    .weightInit(WeightInit.XAVIER) // random initialization of our weights
    .list // builder for creating stacked layers
    .layer(0,
           new DenseLayer.Builder() // define the hidden layer
             .nIn(numInputs)
             .nOut(numHidden)
             .activation(Activation.RELU)
             .build())
    .layer(1,
           new OutputLayer.Builder(LossFunction.MCXENT) // define loss and output layer
             .nIn(numHidden)
             .nOut(numOutputs)
             .activation(Activation.SOFTMAX)
             .build())
    .build()

  val model = new MultiLayerNetwork(conf)
  model.init()
  model.setListeners(new ScoreIterationListener(100)) // print the score every 100th iteration

  // train the model
  for (_ <- 0 until numEpochs) {
    model.fit(mnistTrain)
  }

  // evaluate model performance
  def accuracy(dataSet: DataSetIterator): Double = {
    val evaluator = new Evaluation(numOutputs)
    dataSet.reset()
    for (dataSet <- dataSet.asScala) {
      val output = model.output(dataSet.getFeatures)
      evaluator.eval(dataSet.getLabels, output)
    }
    evaluator.accuracy()
  }

  logger.info(s"Train accuracy = ${accuracy(mnistTrain)}")
  logger.info(s"Test accuracy = ${accuracy(mnistTest)}")
}
