package uicluster

import javafx.scene.{chart => jfxsc}
import scalafx.scene.Node
import scalafx.scene.control.TextField
import scalafx.scene.control.Button
import scalafx.event.ActionEvent
import scalafxml.core.macros.sfxml
import scalafxml.core.{NoDependencyResolver, FXMLLoader}
import scalafx.scene.layout.AnchorPane
import scalafx.scene.chart._
import scalafx.scene.image.ImageView
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert._

import java.awt.{Color, Paint}
import scala.collection.mutable.ListBuffer
import scala.util.control._
import org.jfree.chart.axis.{NumberTickUnit, TickUnits}

import breeze.linalg._
import breeze.numerics._
import breeze.stats._
import breeze.plot._
import java.io.File
import stsc._

import java.awt.image.BufferedImage
import java.awt.Graphics2D
import scalafx.embed.swing.SwingFXUtils

@sfxml
class Controller(private val root: AnchorPane, private val min: TextField, private val max: TextField, private val dataset: ImageView, private val clusters: ImageView) {

    private var displayedDataset = DenseMatrix.zeros[Double](0, 0)

    def cluster(event: ActionEvent) {
        var minClusters = toInt(min.text.value).getOrElse(0)
        var maxClusters = toInt(max.text.value).getOrElse(0)
        var ready = true

        if (ready && minClusters > maxClusters) {
            showAlert("Min has to be the minimum", "The minimum number of clusters must be less than the maximum.")
            ready = false
        }

        if (ready && minClusters < 2) {
            showAlert("Min has to be more than 2", "Having less than 2 clusters is not really interesting...")
            ready = false
        }

        if (ready && maxClusters > 10) {
            showAlert("Max has to be less than 10", "We only cluster small datasets in this app!")
            ready = false
        }

        if (displayedDataset.rows == 0) {
            showAlert("Needs a dataset", "TSelect a dataset before clustering it.")
            ready = false
        }

        if (ready) {
            val (_, correctClusters) = stsc.cluster(displayedDataset)
            val colors = List(Color.RED, Color.GREEN, Color.BLUE, Color.BLACK, Color.MAGENTA, Color.CYAN, Color.YELLOW)

            val f = Figure()
            f.visible = false
            f.width = 400
            f.height = 400
            val p = f.subplot(0)
            p.title = "Clusters"

            p += scatter(displayedDataset(::, 0), displayedDataset(::, 1), {(_:Int) => 0.01}, {(pos:Int) => colors(correctClusters(pos))}) // Display the observations.
            clusters.image = SwingFXUtils.toFXImage(imageToFigure(f), null)
        }
    }

    def selectDataset(event: ActionEvent) {
        val fileChooser = new FileChooser {
            title = "Choose dataset"
            extensionFilters.add(new ExtensionFilter("CSV Files", "*.csv"))
        }
        val selectedFile = fileChooser.showOpenDialog(root.getScene().getWindow())
        if (selectedFile != null) {
            val dataset = breeze.linalg.csvread(selectedFile)
            if (dataset.cols > 2) {
                showAlert("Too many dimensions", "There are " + dataset.cols + "in your CSV, we only need 2.")
            } else {
                displayedDataset = dataset
                showDataset()
            }
        }
    }

    private def toInt(s: String): Option[Int] = {
        try {
            Some(s.toInt)
        } catch {
            case e: Exception => None
        }
    }

    private def showAlert(header: String, content: String) {
        new Alert(AlertType.Error) {
            title = "Error"
            headerText = header
            contentText = content
        }.showAndWait()
    }

    private def showDataset() {
        val f = Figure()
        f.visible = false
        f.width = 400
        f.height = 400
        val p = f.subplot(0)
        p.title = "Dataset"
        p += scatter(displayedDataset(::, 0), displayedDataset(::, 1), {(_:Int) => 0.01}, {(pos:Int) => Color.BLACK}) // Display the observations.
        dataset.image = SwingFXUtils.toFXImage(imageToFigure(f), null)
    }

    private def imageToFigure(f: Figure): BufferedImage = {
        val image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB)
        val g2d = image.createGraphics()
        f.drawPlots(g2d)
        g2d.dispose
        return image
    }
}
