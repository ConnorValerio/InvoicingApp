package Java.Controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.itextpdf.text.DocumentException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/* Author: Connor Valerio
 * Date: 14/08/18
 * 
 * Info: Simple invoicing application with integrated to-pdf and to-email functionalities.
 *  	 First time using JavaFX / fxml & SceneBuilder
 */

public class Main extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	public void start(Stage stage) throws IOException, DocumentException {

		// set logo for application
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/Resources/Images/application_logo.png")));

		// Set property for faster rendering pdf
		System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");

		// load state if possible, otherwise start new application
		File file = new File("./Config/state.xml");
		String fxmlFile = (file.exists()) ? "main.fxml" : "register.fxml";
		String cssFile = (file.exists()) ? "main.css" : "register.css";

		// Setting FXML/CSS
		URL url = getClass().getResource("/Resources/Views/" + fxmlFile);
		FXMLLoader loader = new FXMLLoader(url);
		Parent root = loader.load();
		Scene scene = new Scene(root);

		// set stylesheet
		scene.getStylesheets().add(getClass().getResource("/Resources/CSS/" + cssFile).toExternalForm());

		// load controller object and cast appropriately before setting stage
		Object controller = loader.getController();

		if (controller.getClass().equals(MainController.class)) {
			MainController mainController = (MainController) controller;
			mainController.setStage(stage);
		}

		if (controller.getClass().equals(RegisterController.class)) {
			RegisterController registerController = (RegisterController) controller;
			registerController.setStage(stage);
		}

		// setting the stage
		stage.setScene(scene);
		stage.setTitle("Invoicing Application");
		stage.show();
	}
}
