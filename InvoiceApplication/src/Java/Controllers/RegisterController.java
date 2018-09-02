package Java.Controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import Java.Models.Address;
import Java.Models.Company;
import Java.Services.ApplicationHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class RegisterController {

	private Stage stage;
	private Company company;

	@FXML
	private TextField companyNameField;

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
	private TextField phoneField;

	@FXML
	private TextField emailField;

	@FXML
	private PasswordField passwordField;

	@FXML
	private TextField websiteField;

	@FXML
	private Button chooseLogoButton;

	@FXML
	private ImageView thumbnail;

	@FXML
	private Text chooseLogoText;

	@FXML
	private Button registerButton;

	@FXML
	public void initialize() {
		// create company model
		company = new Company();

		// create directory for saving company xml file
		new File("./Config").mkdir();

		// set logo thumbnail placeholder
		String imageUrl = getClass().getResource("/Resources/Images/placeholder.png").toString();
		thumbnail.setImage(new Image(imageUrl, 100, 100, true, false));
	}

	// called by Main to pass stage object
	public void setStage(Stage stage) {
		this.stage = stage;

		// focus away from form fields so prompt text is visible
		registerButton.requestFocus();
	}

	@FXML
	private void chooseLogo(ActionEvent event) throws IOException {

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose Your Logo");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png"));
		File chosenFile = fileChooser.showOpenDialog(chooseLogoButton.getScene().getWindow());

		if (chosenFile != null) {

			// save Image to config file

			FileInputStream in = null;
			FileOutputStream out = null;

			try {
				in = new FileInputStream(chosenFile);
				out = new FileOutputStream(new File("./Config/logo.jpg"));
				byte[] buff = new byte[1024];
				int bytesRead;

				while ((bytesRead = in.read(buff)) > 0) {
					out.write(buff, 0, bytesRead);
				}

			} finally {
				in.close();
				out.close();
			}

			// get path of chosen logo
			String chosenFilePath = chosenFile.toURI().toString();

			// resize and for using in pdf header
			Image logo = ApplicationHelper.resizeImage(new Image(chosenFilePath, 100, 100, true, true), 100, 100);

			// reduce url size
			String fn = chosenFile.getName();
			int fnlen = fn.length();

			if (fn.length() >= 20) {
				String start = fn.substring(0, 8);
				String end = fn.substring(fnlen - 12, fnlen - 4);
				String suffix = fn.substring(fnlen - 4, fnlen);
				fn = start + "..." + end + suffix;
			}

			chooseLogoText.setText(fn);
			thumbnail.setPreserveRatio(true);
			thumbnail.setImage(logo);

		}

	}

	@FXML
	private void registerCompany(ActionEvent event) throws IOException {

		// validate input

		List<String> errors = new ArrayList<String>();

		String name = companyNameField.getText();
		String addressOne = addressOneField.getText();
		String addressTwo = addressTwoField.getText();
		String town = townField.getText();
		String county = countyField.getText();
		String postcode = postcodeField.getText();

		String phone = phoneField.getText();
		String email = emailField.getText();
		String password = passwordField.getText();
		String website = websiteField.getText();

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

		// 11 digits no whitespace
		if (!phone.matches("0[0-9]{10}")) {
			errors.add("Telephone");
		}

		if (!email.matches("[A-Za-z0-9._-]+@([A-Za-z.-])+[.]([A-Za-z]{2,4})")) {
			errors.add("Email Address");
		}

		// allow anything, gmail password
		if (password.equals("")) {
			errors.add("Password");
		}

		// forces .co.uk / .com
		if (!website.matches("^(www\\.)[a-zA-Z0-9_/\\&\\?\\=\\-\\.\\~\\%]*(\\.com|\\.co\\.uk)$")) {
			errors.add("Website Address");
		}

		if (!new File("./Config/logo.jpg").exists()) {
			errors.add("Upload Logo");
		}

		if (errors.size() > 0) {
			this.showAlert(errors);
			return;
		}

		// Build Address Model for the company
		Address address = new Address(addressOne, addressTwo, town, county, postcode);

		// Set Company properties
		company.setName(name);
		company.setAddress(address);
		company.setTelephoneNumber(phone);
		company.setEmail(email);
		company.setPassword(password);
		company.setWebAddress(website);

		// save Company to file
		ApplicationHelper.saveState(company);

		// load main view
		URL url = getClass().getResource("/Resources/Views/main.fxml");
		Parent root = (Parent) FXMLLoader.load(url);

		Scene mainScene = new Scene(root);
		mainScene.getStylesheets().add(getClass().getResource("/Resources/CSS/main.css").toExternalForm());

		stage.setScene(mainScene);
		stage.centerOnScreen();
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
