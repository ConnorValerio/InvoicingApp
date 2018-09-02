package Java.Controllers;

import java.util.ArrayList;
import java.util.List;

import Java.Models.Address;
import Java.Models.Contact;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddContactController {

	MainController controller;
	Stage stage;

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
	private Button cancelButton;

	@FXML
	private Button submitButton;

	void setStageAndController(Stage stage, MainController controller) {
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

		// get contact object
		Contact contact = isValidContact();

		if (contact != null) {
			// callback controller of main scene
			controller.addContact(contact);
			// close this stage
			stage.close();
		} else {

		}
	}

	private Contact isValidContact() {

		List<String> errors = new ArrayList<String>();

		String name = contactNameField.getText();
		String addressOne = addressOneField.getText();
		String addressTwo = addressTwoField.getText();
		String town = townField.getText();
		String county = countyField.getText();
		String postcode = postcodeField.getText();
		String email = emailField.getText();
		
		/* Regex may need adjusting - not fully tested */

		if (!name.matches("[A-Z]?[a-z]{2,10}(\\s{1}[A-Z]?[a-z]{2,12}){0,2}")) {
			errors.add("Name");
		}

		if (!addressOne.matches("[0-9]{1,4}(\\s{1}[A-Z]?[a-z]{1,12}){1,3}")) {
			errors.add("Address Line 1");
		}

		if (!addressTwo.matches("[A-Za-z0-9\\s]{0,25}")) {
			errors.add("Address Line 2");
		}

		if (!town.matches("[A-Z]?[a-z]{2,12}(\\s{1}[A-Z]?[a-z]{2,12}){0,2}")) {
			errors.add("Town");
		}

		if (!county.matches("[A-Z]?[a-z]{2,12}(\\s{1}[A-Z]?[a-z]{0,12}){0,2}")) {
			errors.add("County");
		}

		if (!postcode.matches("[A-Za-z]{1,2}[0-9][0-9A-Za-z]?\\s?[0-9][A-Za-z]{2}")) {
			errors.add("Postcode");
		}

		if (!email.matches("[A-Za-z0-9._-]+@([A-Za-z.-])+[.]([A-Za-z]{2,4})")) {
			errors.add("Email Address");
		}

		if (errors.size() > 0) {
			this.showAlert(errors);
			return null;
		}

		return new Contact(name, new Address(addressOne, addressTwo, town, county, postcode), email);
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
