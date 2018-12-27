package org.mo39.fmbh.mnist.deeplearning4j
import java.awt.image.BufferedImage

import scala.collection.JavaConverters._
import com.typesafe.scalalogging.LazyLogging
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.{ MultiLayerConfiguration, NeuralNetConfiguration }
import org.deeplearning4j.nn.conf.layers.{ DenseLayer, OutputLayer }
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.evaluation.classification.Evaluation
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.learning.config.Sgd
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction
import org.mo39.fmbh.common.enriched.BufferedImage._

object Mnist {

  val width: Int = 28
  val height: Int = 28

}

class Mnist extends LazyLogging {

  import Mnist._

  def mkBase64ImgUrl(array: INDArray): String = {
    // Check array size
    require(array.rank() == 2)
    require(array.rows() * array.columns() == width * height)
    // Convert to 28 * 28 array
    val arr = array
      .reshape(width, height)
      .toDoubleMatrix
      .map(_.map(v => 255 - (v * 255).toInt))
    // Build BufferedImage and get Base64 Url
    new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY).from(arr).toBase64Url
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

  // define the neural network architecture
  val conf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
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
