package Java.Controllers;

import java.util.ArrayList;
import java.util.List;
import Java.Models.Product;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class AddProductController {

	Stage stage;
	MainController controller;

	@FXML
	private TextField productNameField;

	@FXML
	private TextArea productDescriptionField;

	@FXML
	private TextField productPriceField;

	@FXML
	private Button cancelButton;

	@FXML
	private Button submitButton;

	public void setStageAndController(Stage stage, MainController controller) {
		this.stage = stage;
		this.controller = controller;

		// focus away from form fields so prompt text is visible
		submitButton.requestFocus();
	}

	@FXML
	void cancel(ActionEvent event) {
		stage.close();
	}
	@FXML
	void submit(ActionEvent event) {

		// get product object
		Product product = isValidProduct();

		if (product != null) {
			// callback controller of main scene
			controller.addProduct(product);
			// close this stage
			stage.close();
		} else {

		}
	}

	private Product isValidProduct() {

		List<String> errors = new ArrayList<String>();

		String name = productNameField.getText();
		String description = productDescriptionField.getText();
		String strPrice = productPriceField.getText();

		/* Regex may need adjusting - not fully tested */

		if (!name.matches("([A-Z]?[a-z]{1,20}\\s?){1,3}")) {
			errors.add("Name");
		}

		if (!description.matches("[A-Za-z0-9\\s!?,.()*£/]{5,150}")) {
			errors.add("Description");
		}

		if (!strPrice.matches("[£]?[1-9]?[0-9]{1,3}(.[0-9]{2})?")) {
			errors.add("Price");
		}

		if (errors.size() > 0) {
			this.showAlert(errors);
			return null;
		}

		String formattedPrice = strPrice;
		if (strPrice.startsWith("£")) {
			formattedPrice = strPrice.substring(1);
		}

		return new Product(name, description, Float.parseFloat(formattedPrice));
	}

	private void showAlert(List<String> fields) {
		StringBuilder errorList = new StringBuilder();

		for (String field : fields) {
			errorList.append("-> " + field + "\n");
		}
		String msg = "You have entered incorrect information in the following field(s):    \n\n" + errorList.toString()
				+ "\n Please try again.";
		Alert alert = new Alert(AlertType.ERROR, msg, ButtonType.OK);
		alert.showAndWait();

	}

}
