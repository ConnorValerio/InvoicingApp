package Java.Controllers;

import Java.Models.Contact;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ViewContactController {

	private Stage stage;

	@FXML
	private TextField contactNameField;

	@FXML
	private TextField addressOneField;

	@FXML
	private TextField addressTwoField;

	@FXML
	private TextField townField;

	@FXML
	private TextField countyField;

	@FXML
	private TextField postcodeField;

	@FXML
	private TextField emailField;

	@FXML
	private Button closeButton;

	void setStage(Stage stage) {
		this.stage = stage;
		// focus away from form fields so prompt text is visible
		closeButton.requestFocus();
	}

	void setContact(Contact contact) {
		contactNameField.setText(contact.getName());
		addressOneField.setText(contact.getAddress().getAddressOne());
		addressTwoField.setText(contact.getAddress().getAddressTwo());
		townField.setText(contact.getAddress().getTown());
		countyField.setText(contact.getAddress().getCounty());
		postcodeField.setText(contact.getAddress().getPostcode());
		emailField.setText(contact.getEmail());
	}

	@FXML
	void close(ActionEvent event) {
		stage.close();
	}

}
