package Java.Controllers;

import Java.Models.Product;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ViewProductController {

	Stage stage;

	@FXML
	private TextField productNameField;

	@FXML
	private TextArea productDescriptionField;

	@FXML
	private TextField productPriceField;

	@FXML
	private Button closeButton;

	public void setStage(Stage stage) {
		this.stage = stage;
		// focus away from form fields so prompt text is visible
		closeButton.requestFocus();
	}

	public void setProduct(Product product) {
		productNameField.setText(product.getName());
		productDescriptionField.setText(product.getDescription());
		productPriceField.setText(String.format("%.2f", product.getPrice()));
	}

	@FXML
	void close(ActionEvent event) {
		stage.close();
	}
}
