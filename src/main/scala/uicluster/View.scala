package uicluster

import java.io.IOException
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafxml.core.{NoDependencyResolver, FXMLView}

object View extends JFXApp {
    val view = FXMLView(getClass.getResource("/uicluster.fxml"), NoDependencyResolver)

    stage = new PrimaryStage() {
        title = "Self-Tuning Spectral Clustering"
        resizable = false
        scene = new Scene(view)
    }
}
