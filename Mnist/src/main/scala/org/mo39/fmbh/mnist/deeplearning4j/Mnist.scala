package org.mo39.fmbh.mnist.deeplearning4j
import java.awt.image.BufferedImage
import java.io.File

import scala.collection.JavaConverters._
import com.typesafe.scalalogging.LazyLogging
import org.deeplearning4j.datasets.fetchers.MnistDataFetcher
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
import org.mo39.fmbh.common.ml._
import org.mo39.fmbh.common.util.JupyterDisplay

object Mnist extends App {
  val mnist: Mnist = new Mnist()
  val model: MultiLayerNetwork = mnist.buildModel
  model.fit(mnist.loadTrainSet)
  model.save(new File("models/deeplearning4j.Mnist"))
  mnist.accuracy(model, mnist.loadTestSet)
}

case class Mnist(
    @Config(description = "The width of a Mnist image")
    width: Int = 28,
    @Config(description = "The height of a Mnist image")
    height: Int = 28,
    @Config(description = "fixed random seed for reproducibility. No need to change.")
    seed: Int = 42,
    @Config(description = """
        The size of the input for a neutral network layer.
        For Mnist dataset, each image has 28 * 28 = 784 pixels.
      """)
    numInputs: Int = 28 * 28,
    @Config(description = "The hidden layer in the neutral network")
    numHidden: Int = 512,
    @Config(description = """
        The size of the output for a neutral network layer.
        For Mnist dataset, 10 outputs represents 0 - 9.
        For a image of hand written 7, it's presented similar to
        `Array(0, 0, 0, 0, 0, 0, 0, 1, 0, 0)`
      """)
    numOutputs: Int = 10,
    @Config
    learningRate: Double = 0.01,
    @Config(
      description = """
          The size of one batch.
          Train the number of batch size once at a time from the dataset."""
    )
    batchSize: Int = 128,
    @Config(description = "The number of times to train the full dataset")
    numEpochs: Int = 10,
    @Config(description = "The size of training dataset")
    trainSetSize: Int = MnistDataFetcher.NUM_EXAMPLES,
    @Config(description = "The size of testing dataset")
    testSetSize: Int = MnistDataFetcher.NUM_EXAMPLES_TEST
) extends LazyLogging
    with JupyterDisplay {

  // download and load the MNIST images as tensors
  def loadTrainSet: MnistDataSetIterator =
    new MnistDataSetIterator(batchSize, trainSetSize, false, true, true, seed)
  def loadTestSet: MnistDataSetIterator =
    new MnistDataSetIterator(batchSize, testSetSize, false, false, true, seed)

  def buildModel: MultiLayerNetwork = {
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
    model
  }

  /* Evaluate the accuracy of a MultiLayerNetwork on a dataset */
  def accuracy(model: MultiLayerNetwork, dataSetItor: DataSetIterator): Unit = {
    val evaluator = new Evaluation(numOutputs)
    dataSetItor.reset()
    for (dataSet <- dataSetItor.asScala) {
      val output = model.output(dataSet.getFeatures)
      evaluator.eval(dataSet.getLabels, output)
    }
    logger.info(s"Accuracy = ${evaluator.accuracy()}")
  }

  // display ------------------------------------------------------------

  override def featureToXml(a: Any): xml.Elem = {
    require(a.isInstanceOf[INDArray])
    val src = mkBase64ImgUrl(a.asInstanceOf[INDArray])
    <img src={src}></img>
  }

  override def labelToXml(a: Any): xml.Elem = {
    require(a.isInstanceOf[INDArray])
    val label = a.asInstanceOf[INDArray]
    require(label.rank() == 2)
    require(label.rows() == 1)
    require(label.columns() == 10)
    val num = (0 to 9).find(i => label.getInt(0, i) != 0).get
    <b>{num}</b>
  }

  private def mkBase64ImgUrl(feature: INDArray): String = {
    // Check array size
    require(feature.rank() == 2)
    require(feature.rows() == 1)
    require(feature.columns() == 28 * 28)
    // Convert to 28 * 28 array
    val arr = feature
      .reshape(28, 28)
      .toDoubleMatrix
      .map(_.map(v => (v * 255).toInt))
    // Build BufferedImage and get Base64 Url
    new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY)
      .from(arr)
      .toBase64Url
  }

}
