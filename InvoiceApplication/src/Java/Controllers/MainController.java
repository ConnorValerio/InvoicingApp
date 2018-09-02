package Java.Controllers;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.itextpdf.text.DocumentException;

import Java.Models.Company;
import Java.Models.Contact;
import Java.Models.Product;
import Java.Services.ApplicationHelper;
import Java.Services.EmailHelper;
import Java.Services.EmailHelper.EmailResponse;
import Java.Services.MyComparators;
import Java.Services.PDFHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainController {

	// load resources
	final Image yearFolderIcon = new Image(getClass().getResourceAsStream("/Resources/Images/folderIcon.png"), 22, 22, true, true);
	final Image monthFolderIcon = new Image(getClass().getResourceAsStream("/Resources/Images/folderIcon.png"), 18, 18, true, true);
	final Image pdfIcon = new Image(getClass().getResourceAsStream("/Resources/Images/pdfIcon.png"), 18, 18, true, true);

	// non-fxml elements
	private Stage stage;
	private Company company;
	private Map<String, File> filesToSearch;
	private ProgressBar invoiceProgressBar;
	private ProgressBar emailProgressBar;

	// Invoice Searching Panel
	@FXML
	private TextField searchField;
	@FXML
	private Button searchButton;
	@FXML
	private ComboBox<String> resultField;
	@FXML
	private Button viewButton;
	@FXML
	private TreeView<String> invoiceTree;

	// Invoice Display Panel
	@FXML
	private VBox midVBox;
	@FXML
	private ScrollPane scrollPane;
	@FXML
	private ImageView pdfViewer;

	// Contacts Panel
	@FXML
	private TableView<Contact> contactTable;
	@FXML
	private TableColumn<?, ?> contactNameColumn;
	@FXML
	private TableColumn<?, ?> contactEmailColumn;
	@FXML
	private Button addContactButton;
	@FXML
	private Button deleteContactButton;

	// Products Panel
	@FXML
	private TableView<Product> productTable;
	@FXML
	private TableColumn<Product, String> productNameColumn;
	@FXML
	private TableColumn<Product, String> productPriceColumn;
	@FXML
	private TableColumn<Product, String> productQuantityColumn;
	@FXML
	private Button addProductButton;
	@FXML
	private Button deleteProductButton;
	@FXML
	private Button viewProductButton;

	// CreateInvoiceButton
	@FXML
	private Button createInvoiceButton;

	@FXML
	public void initialize() throws IOException, DocumentException {

		// set up progress bars
		invoiceProgressBar = new ProgressBar();
		emailProgressBar = new ProgressBar();
		invoiceProgressBar.setPrefSize(450, 18);
		emailProgressBar.setPrefSize(450, 18);
		invoiceProgressBar.setStyle("-fx-accent: #808080;");
		emailProgressBar.setStyle("-fx-accent: #003a96;");
		invoiceProgressBar.setProgress(0.0f);
		emailProgressBar.setProgress(0.0f);

		// create bindings for contact table
		contactTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		contactNameColumn.setMaxWidth(1f * Integer.MAX_VALUE * 40); // 40%
		contactEmailColumn.setMaxWidth(1f * Integer.MAX_VALUE * 60); // 60%

		// create bindings for product table
		productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		productNameColumn.setMaxWidth(1f * Integer.MAX_VALUE * 50); // 70%
		productPriceColumn.setMaxWidth(1f * Integer.MAX_VALUE * 25); // 20%
		productQuantityColumn.setMaxWidth(1f * Integer.MAX_VALUE * 25); // 10%

		// xml file must exist
		if (new File("./Config/state.xml").exists()) {
			this.company = ApplicationHelper.loadState();
			this.loadInvoiceTree();
			this.loadInvoiceViewer();
			this.loadContactViewer();
			this.loadProductViewer();
		} else {
			System.exit(1);
		}
	}

	// called by Main to pass stage object
	public void setStage(Stage stage) {
		this.stage = stage;
	}

	/* Invoice Tree methods */

	// called on first instance
	private void createInvoiceTree() {

		// Create root folder
		File invoiceFolder = new File(".//Invoices");
		invoiceFolder.mkdir();

		// create root node
		TreeItem<String> root = new TreeItem<>();
		invoiceTree.setShowRoot(false);
		invoiceTree.setRoot(root);

		LocalDate date = LocalDate.now();

		// Prevents code duplication - add current month to tree structure
		this.addMonth(date.minusMonths(1));

		// add 12 months of folders to tree
		for (int i = 0; i < 12; i++) {
			date = addMonth(date);
		}

		// expand year nodes
		root.getChildren().forEach(new Consumer<TreeItem<String>>() {
			public void accept(TreeItem<String> node) {
				node.setExpanded(true);
			}
		});
	}

	// called when resuming state
	private void loadInvoiceTree() {

		filesToSearch = new HashMap<String, File>();

		// find current month node
		String year = String.valueOf(LocalDate.now().getYear());
		String month = DateFormatSymbols.getInstance().getMonths()[LocalDate.now().getMonthValue() - 1];

		File invoiceFolder = new File(".//Invoices");

		TreeItem<String> root = new TreeItem<>();
		invoiceTree.setShowRoot(false);
		invoiceTree.setRoot(root);

		// create new tree if file structure doesn't exist
		if (!invoiceFolder.exists()) {
			this.createInvoiceTree();
		} else {

			// load tree
			File[] yearFolders = invoiceFolder.listFiles();
			Arrays.sort(yearFolders, MyComparators.yearFolderComparator());

			File[] monthFolders = null;
			File[] pdfFiles = null;

			// create reusable objects
			TreeItem<String> yearNode = null;
			TreeItem<String> monthNode = null;
			TreeItem<String> fileNode = null;
			TreeItem<String> monthToExpandNode = null;

			for (int i = 0; i < yearFolders.length; i++) {
				// add year nodes
				yearNode = new TreeItem<String>(yearFolders[i].getName(), new ImageView(yearFolderIcon));
				root.getChildren().add(yearNode);

				// create and sort month folder
				monthFolders = yearFolders[i].listFiles();
				Arrays.sort(monthFolders, MyComparators.monthFolderComparator());

				for (int j = 0; j < monthFolders.length; j++) {
					// add month nodes
					monthNode = new TreeItem<String>(monthFolders[j].getName(), new ImageView(monthFolderIcon));
					root.getChildren().get(i).getChildren().add(monthNode);

					// try find current month node to expand later
					if (monthNode.getValue().equals(month) && monthNode.getParent().getValue().equals(year)) {
						monthToExpandNode = monthNode;
					}

					// filter for pdf files
					pdfFiles = monthFolders[j].listFiles(new FilenameFilter() {
						public boolean accept(File file, String fileName) {
							return fileName.endsWith(".pdf");
						}
					});

					// sort pdf files by invoice number
					Arrays.sort(pdfFiles, MyComparators.invoiceFolderComparator());

					// add pdf file nodes to tree
					for (int k = 0; k < pdfFiles.length; k++) {
						fileNode = new TreeItem<String>(pdfFiles[k].getName(), new ImageView(pdfIcon));
						root.getChildren().get(i).getChildren().get(j).getChildren().add(fileNode);

						// create URL String for searching purposes
						String yyyy = root.getChildren().get(i).getValue();
						String mm = root.getChildren().get(i).getChildren().get(j).getValue();
						String file = fileNode.getValue();
						String path = "./Invoices/" + yyyy + "/" + mm + "/" + file;

						File entry = new File(path);

						filesToSearch.put(entry.getName(), entry);
					}
				}
			}

			// checks there are at least 6 months of folders ahead
			this.checkIfTreeExtensionNeeded();

			// expand year nodes
			root.getChildren().forEach(new Consumer<TreeItem<String>>() {
				public void accept(TreeItem<String> node) {
					node.setExpanded(true);
				}
			});

			// expand current month node
			monthToExpandNode.setExpanded(true);

			// set selection to last invoice created
			ObservableList<TreeItem<String>> leafNodes = monthToExpandNode.getChildren();
			int lastLeaf = leafNodes.size() - 1;
			if (lastLeaf != -1) {
				invoiceTree.getSelectionModel().select(leafNodes.get(lastLeaf));
			}

			// allows opening pdf in web browser
			this.listenOnFileNodes();

		}
	}

	@FXML
	private void searchInvoiceTree() {

		// results to pass to combo box
		ArrayList<String> results = new ArrayList<String>();

		// called by search button, get value from textfield
		String search = searchField.getText();
		if (search.isEmpty()) {

			String msg = "You need to provide a search term!\n\nYou can search for invoices by an invoice number or a contact name."
					+ " The following formats are accepted:\n\n :<contact_name>\n\n#<invoice_number>";
			Alert alert = new Alert(AlertType.INFORMATION, msg, ButtonType.OK);
			alert.showAndWait();

			// set result field to empty
			resultField.getItems().clear();
			return;
		}

		// searching for invoice number
		if (search.startsWith("#")) {
			search = search.substring(1);

			// loop through files
			for (String fileName : filesToSearch.keySet()) {
				String[] fileSplit = fileName.split("-");
				String invNumb = fileSplit[1];
				if (invNumb.equals(search)) {
					results.add(fileName);
				}
			}
		}

		// searching by Contact name
		if (search.startsWith(":")) {
			search = search.substring(1);

			// loop through files
			for (String fileName : filesToSearch.keySet()) {
				String[] fileSplit = fileName.split("-");
				String contact = fileSplit[2];
				if (contact.contains(search)) {
					results.add(fileName);
				}
			}

		}

		if (results.size() > 0) {
			resultField.setItems(FXCollections.observableList(results));
			resultField.getSelectionModel().select(0);
		} else {
			String msg = "No Results Found!";
			Alert alert = new Alert(AlertType.INFORMATION, msg, ButtonType.OK);
			alert.showAndWait();

			// set search field to empty
			searchField.setText("");
			// set result field to empty
			resultField.getItems().clear();
		}
	}

	@FXML
	private void viewSearchResults() {
		String res = resultField.getSelectionModel().getSelectedItem();
		if (res != null) {
			try {
				URI uri = new File(filesToSearch.get(res).getCanonicalPath()).toURI();
				ApplicationHelper.OpenPDFinBrowser(uri);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			String msg = "You haven't selected an invoice to view!";
			Alert alert = new Alert(AlertType.ERROR, msg, ButtonType.OK);
			alert.showAndWait();
		}
	}

	private void listenOnFileNodes() {
		invoiceTree.setOnMouseClicked(event -> {

			// on double click
			if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() > 1) {
				TreeItem<String> item = invoiceTree.getSelectionModel().getSelectedItem();

				// if the node is a pdf file -> isLeaf won't work b/c empty folders
				if (item != null && item.getValue().endsWith(".pdf")) {

					// generate file URI
					String month = item.getParent().getValue();
					String year = item.getParent().getParent().getValue();
					try {

						// use canonical path to remove /./ directory
						URI uri = new File((new File("./Invoices/" + year + "/" + month + "/" + item.getValue()).getCanonicalPath()))
								.toURI();

						// try and open the URI in the default browser
						ApplicationHelper.OpenPDFinBrowser(uri);

					} catch (IOException e1) {
						// show alert -> perhaps the file has been deleted or moved
					}

				}
			}
		});
	}

	// make sure folder farthest in the future is >=6 months from now
	private void checkIfTreeExtensionNeeded() {

		TreeItem<String> root = invoiceTree.getRoot();
		ObservableList<TreeItem<String>> years = root.getChildren();
		TreeItem<String> maxYear = years.get(years.size() - 1);
		int yearValue = Integer.parseInt(maxYear.getValue());

		ObservableList<TreeItem<String>> months = maxYear.getChildren();
		TreeItem<String> maxMonth = months.get(months.size() - 1);
		List<String> monthList = Arrays.asList(DateFormatSymbols.getInstance().getMonths());
		int monthValue = monthList.indexOf(maxMonth.getValue()) + 1;

		LocalDate today = LocalDate.now();
		LocalDate maxFuture = LocalDate.of(yearValue, monthValue, 1);

		int monthDiff = (maxFuture.getYear() - today.getYear()) * 12;
		monthDiff += maxFuture.getMonthValue() - today.getMonthValue();

		while (monthDiff < 6) {
			maxFuture = this.addMonth(maxFuture);
			monthDiff++;
		}

	}

	// called when a new invoice has been created and the TreeView structure needs updating
	private void updateInvoiceTree() {
		invoiceTree.getRoot().getChildren().clear();
		this.loadInvoiceTree();
	}

	/* Used to create a new month folder/node */
	private LocalDate addMonth(LocalDate date) {
		LocalDate nextMonthDate = date.plusMonths(1);
		int year = nextMonthDate.getYear();
		int month = nextMonthDate.getMonthValue();

		// Folder structure
		String invoiceFolder = ".//Invoices";
		String yearFolder = invoiceFolder + "//" + year;

		// get list of years already in tree
		ObservableList<TreeItem<String>> yearList = invoiceTree.getRoot().getChildren();

		TreeItem<String> yearNode = null;
		// see if year exists in tree
		for (TreeItem<String> node : yearList) {
			if (node.getValue().equals(Integer.toString(year))) {
				yearNode = node;
				break;
			}
		}

		// year was not found in tree
		if (yearNode == null) {
			String yearString = Integer.toString(year);
			yearNode = new TreeItem<>(yearString, new ImageView(yearFolderIcon));
			invoiceTree.getRoot().getChildren().add(yearNode);

			// make folder for new year node
			new File(yearFolder).mkdir();
		}

		// create month node
		String nextMonthString = DateFormatSymbols.getInstance().getMonths()[month - 1];
		TreeItem<String> monthNode = new TreeItem<>(nextMonthString, new ImageView(monthFolderIcon));

		// add month folder to year folder
		String monthFolder = yearFolder + "//" + nextMonthString;
		new File(monthFolder).mkdir();

		// add month node to year node
		yearNode.getChildren().add(monthNode);

		return nextMonthDate;
	}

	/*************************************/

	/* Invoice Viewer Methods */

	// called on initialising
	private void loadInvoiceViewer() throws IOException, DocumentException {

		// load example pdf image
		String url = this.getClass().getResource("/Resources/Example/example.jpg").toString();
		scrollPane.setContent(pdfViewer);
		Image pdf = new Image(url, 700, 0, true, true);
		pdfViewer.setImage(pdf);

	}

	// called after creating an invoice - sets image of new pdf
	private void updateInvoiceViewer(String url) throws IOException, DocumentException {
		Image pdf = new Image("file:" + url, 700, 0, true, true);
		pdfViewer.setImage(pdf);
	}

	/*************************************/

	/* Contact View Methods */

	private void loadContactViewer() {

		// associate column names with field names
		contactNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		contactEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

		ArrayList<Contact> contacts = company.getContacts();
		ObservableList<Contact> contactList = FXCollections.observableArrayList();

		for (Contact contact : contacts) {
			contactList.add(contact);
		}

		contactTable.setItems(contactList);
	}

	@FXML
	void showAddContactView(ActionEvent event) throws IOException {

		// load add contact view
		Stage addContactStage = new Stage();
		addContactStage.initOwner(stage);
		addContactStage.initModality(Modality.WINDOW_MODAL);
		addContactStage.setResizable(false);

		URL url = getClass().getResource("/Resources/Views/addcontact.fxml");
		FXMLLoader loader = new FXMLLoader(url);
		Parent root = (Parent) loader.load();
		Scene addContactView = new Scene(root);

		// add CSS to the scene
		addContactView.getStylesheets().add(getClass().getResource("/Resources/CSS/main.css").toExternalForm());

		// get controller of the view
		AddContactController controller = (AddContactController) loader.getController();

		// pass the view a reference to it's own stage, and the main controller to callback
		controller.setStageAndController(addContactStage, this);

		// set scene and show
		addContactStage.setScene(addContactView);
		addContactStage.showAndWait();

	}

	/* called by AddContactController */
	void addContact(Contact contact) {
		company.addContact(contact);
		contactTable.getItems().add(contact);
	}

	@FXML
	void deleteContact(ActionEvent event) {

		// get selected contact
		Contact contact = contactTable.getSelectionModel().getSelectedItem();

		if (contact != null) {

			// are you sure alert box
			String msg = "Are you sure you want to delete the contact '" + contact.getName() + "'? This action cannot be undone.\n";
			Alert alert = new Alert(AlertType.WARNING, msg, ButtonType.NO, ButtonType.YES);
			Optional<ButtonType> res = alert.showAndWait();

			if (res.get() == ButtonType.YES) {
				// delete contact
				company.deleteContact(contact);
				contactTable.getItems().remove(contact);
			}

			// clear selection model
			contactTable.getSelectionModel().clearSelection();

		} else {
			// show alert box
			String msg = "You haven't selected a contact to delete!";
			Alert alert = new Alert(AlertType.ERROR, msg, ButtonType.OK);
			alert.showAndWait();
		}
	}

	@FXML
	void showViewContactView(ActionEvent event) throws IOException {

		Contact contact = contactTable.getSelectionModel().getSelectedItem();

		// check a contact has been selected
		if (contact == null) {
			// show alert box
			String msg = "You haven't selected a contact to view!";
			Alert alert = new Alert(AlertType.ERROR, msg, ButtonType.OK);
			alert.showAndWait();
			return;
		}

		// load add contact view
		Stage viewContactStage = new Stage();
		viewContactStage.initOwner(stage);
		viewContactStage.initModality(Modality.WINDOW_MODAL);
		viewContactStage.setResizable(false);

		URL url = getClass().getResource("/Resources/Views/viewcontact.fxml");
		FXMLLoader loader = new FXMLLoader(url);
		Parent root = (Parent) loader.load();
		Scene viewContactView = new Scene(root);

		// add CSS to the scene
		viewContactView.getStylesheets().add(getClass().getResource("/Resources/CSS/main.css").toExternalForm());

		// get controller of the view
		ViewContactController controller = (ViewContactController) loader.getController();

		// pass the view a reference to it's own stage
		controller.setStage(viewContactStage);

		// get selected contact and set it in the new view
		controller.setContact(contact);

		// set scene and show
		viewContactStage.setScene(viewContactView);
		viewContactStage.showAndWait();
	}

	/*************************************/

	/* Product View Methods */
	private void loadProductViewer() {

		// set selection model
		productTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		// set table to be editable
		productTable.setEditable(true);

		// associate column names with field names
		productNameColumn.setCellValueFactory(new PropertyValueFactory<Product, String>("name"));
		productPriceColumn.setCellValueFactory(new PropertyValueFactory<Product, String>("formattedPrice"));
		productQuantityColumn.setEditable(true);
		productQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		productQuantityColumn.setCellValueFactory(new PropertyValueFactory<Product, String>("quantity"));

		// get products and set the table view
		ArrayList<Product> products = company.getProducts();
		ObservableList<Product> productList = FXCollections.observableArrayList();

		for (Product product : products) {
			productList.add(product);
		}

		productTable.setItems(productList);

		// set on edit event for quantity cell
		productQuantityColumn.setOnEditCommit((CellEditEvent<Product, String> event) -> {
			Integer newValue = null;
			int row = event.getTablePosition().getRow();

			try {
				newValue = Integer.parseInt(event.getNewValue());
			} catch (NumberFormatException e) {

				// show alert box
				String msg = "The quantity field only accepts numbers!";
				Alert alert = new Alert(AlertType.INFORMATION, msg, ButtonType.OK);
				alert.showAndWait();

				// force refresh
				Product p = productTable.getItems().get(row);
				productTable.getItems().set(row, p);
				return;
			}

			// set quantity value in Product object
			Product p = productTable.getItems().get(row);
			p.setQuantity(Integer.toString(newValue));
			productTable.getItems().set(row, p);
		});
	}

	@FXML
	void showAddProductView(ActionEvent event) throws IOException {
		// load add product view
		Stage addProductStage = new Stage();
		addProductStage.initOwner(stage);
		addProductStage.initModality(Modality.WINDOW_MODAL);
		addProductStage.setResizable(false);

		URL url = getClass().getResource("/Resources/Views/addproduct.fxml");
		FXMLLoader loader = new FXMLLoader(url);
		Parent root = (Parent) loader.load();
		Scene addProductView = new Scene(root);

		// add CSS to the scene
		addProductView.getStylesheets().add(getClass().getResource("/Resources/CSS/main.css").toExternalForm());

		// get controller of the view
		AddProductController controller = (AddProductController) loader.getController();

		// pass the view a reference to it's own stage, and the main controller to callback
		controller.setStageAndController(addProductStage, this);

		// set scene and show
		addProductStage.setScene(addProductView);
		addProductStage.showAndWait();
	}

	/* called by AddProductController */
	void addProduct(Product product) {
		company.addProduct(product);
		productTable.getItems().add(product);
	}

	@FXML
	void deleteProduct(ActionEvent event) {

		// get selected products
		ObservableList<Product> selectedProducts = productTable.getSelectionModel().getSelectedItems();

		// if products have been selected
		if (selectedProducts != null && selectedProducts.size() > 0) {

			StringBuilder products = new StringBuilder();
			for (Product p : selectedProducts) {
				products.append("-> " + p.getName() + "\n");
			}

			// are you sure alert box
			String msg = "Are you sure you want to delete the following products? :\n\n" + products.toString()
					+ "\nThis action cannot be undone.\n";
			Alert alert = new Alert(AlertType.WARNING, msg, ButtonType.NO, ButtonType.YES);
			Optional<ButtonType> res = alert.showAndWait();

			if (res.get() == ButtonType.YES) {

				// remove products from company model
				for (Product p : selectedProducts) {
					company.deleteProduct(p);
				}
				// remove products from table view
				productTable.getItems().removeAll(selectedProducts);
			}

			// clear selection model
			productTable.getSelectionModel().clearSelection();

		} else {
			// show alert box
			String msg = "You haven't selected any products to delete!";
			Alert alert = new Alert(AlertType.ERROR, msg, ButtonType.OK);
			alert.showAndWait();
		}
	}

	@FXML
	void showViewProductView(ActionEvent event) throws IOException {

		ObservableList<Product> productList = productTable.getSelectionModel().getSelectedItems();

		// check a contact has been selected
		if (productList.size() == 0) {

			// show alert box
			String msg = "You haven't selected a product/service to view!";
			Alert alert = new Alert(AlertType.ERROR, msg, ButtonType.OK);
			alert.showAndWait();
			return;

			// multiple selection
		} else if (productList.size() > 1) {
			// show alert box
			String msg = "You can only view one product/service at a time!";
			Alert alert = new Alert(AlertType.ERROR, msg, ButtonType.OK);
			alert.showAndWait();
			return;
		}

		// load add Product view
		Stage viewProductStage = new Stage();
		viewProductStage.initOwner(stage);
		viewProductStage.initModality(Modality.WINDOW_MODAL);
		viewProductStage.setResizable(false);

		URL url = getClass().getResource("/Resources/Views/viewproduct.fxml");
		FXMLLoader loader = new FXMLLoader(url);
		Parent root = (Parent) loader.load();
		Scene viewProductView = new Scene(root);

		// add CSS to the scene
		viewProductView.getStylesheets().add(getClass().getResource("/Resources/CSS/main.css").toExternalForm());

		// get controller of the view
		ViewProductController controller = (ViewProductController) loader.getController();

		// pass the view a reference to it's own stage
		controller.setStage(viewProductStage);

		// get selected contact and set it in the new view
		controller.setProduct(productList.get(0));

		// set scene and show
		viewProductStage.setScene(viewProductView);
		viewProductStage.showAndWait();

	}

	/*************************************/

	/* Create Invoice Method */

	@FXML
	void createInvoice(ActionEvent event) {

		// get contact
		Contact contact = contactTable.getSelectionModel().getSelectedItem();

		// get products/services
		ArrayList<Product> products = new ArrayList<Product>(productTable.getSelectionModel().getSelectedItems());

		// validation checks
		if (contact == null && products.size() == 0) {
			String msg = "You haven't selected a contact or any products/services to create an invoice for!";
			Alert alert = new Alert(AlertType.ERROR, msg, ButtonType.OK);
			alert.showAndWait();
			return;
		}

		if (contact == null) {
			String msg = "You haven't selected a contact to create an invoice for!";
			Alert alert = new Alert(AlertType.ERROR, msg, ButtonType.OK);
			alert.showAndWait();
			return;
		}

		if (products.size() == 0) {
			String msg = "You haven't selected any products/services to create an invoice for!";
			Alert alert = new Alert(AlertType.ERROR, msg, ButtonType.OK);
			alert.showAndWait();
			return;
		}

		// create Alert to see if user wants to send invoice in an email
		String msg = "			After creating the invoice, do you want to send it?\n\n";
		ButtonType yes = new ButtonType("Yes, Create & Send.");
		ButtonType no = new ButtonType("No, Create Only.");
		ButtonType cancel = ButtonType.CANCEL;

		Alert alert = new Alert(AlertType.CONFIRMATION, msg, yes, no, cancel);
		Optional<ButtonType> res = alert.showAndWait();

		// set depending on whether the user wants to create the invoice, or create and send
		boolean sendEmail = (res.get().getText().equals(yes.getText())) ? true : false;

		// disable button
		createInvoiceButton.setDisable(true);

		// return if the user cancelled the action, clean up
		if (res.get() == ButtonType.CANCEL) {
			cleanUp();
			return;
		}

		// add invoice progress bar
		midVBox.getChildren().add(invoiceProgressBar);

		// generating pdf url
		LocalDate today = LocalDate.now();
		String year = String.valueOf(today.getYear());
		String month = DateFormatSymbols.getInstance().getMonths()[today.getMonthValue() - 1];

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy");
		String dateStamp = today.format(formatter);

		String filePath = "./Invoices/" + year + "/" + month + "/";
		String fileName = "INV-" + company.getInvoiceNumber() + "-" + contact.getName() + "-" + dateStamp;
		String pdfUrl = filePath + fileName + ".pdf";

		Thread invoiceThread = new Thread(new Task<Void>() {
			protected Void call() throws Exception {
				try {

					// create .pdf for the invoice
					PDFHelper.createInvoice(company, contact, products, pdfUrl);
					// update progress
					Platform.runLater(() -> invoiceProgressBar.setProgress(0.25f));
					// create .png for the invoice viewer
					PDFHelper.pdfToImage(pdfUrl);
					// update progress
					Platform.runLater(() -> invoiceProgressBar.setProgress(0.50f));

					// if successful, increment invoice number
					int incr = company.getInvoiceNumber() + 1;
					company.setInvoiceNumber(incr);

					// call to Application Thread to update GUI
					Platform.runLater(() -> {
						try {
							// update invoice viewer
							updateInvoiceViewer(filePath + fileName + ".jpg");
							// update progress
							invoiceProgressBar.setProgress(0.75f);
							// update invoice tree
							updateInvoiceTree();
							// update progress
							invoiceProgressBar.setProgress(1.0f);
						} catch (IOException | DocumentException e) {
							e.printStackTrace();
							return;
						}
					});

					// if sending an email
					if (sendEmail) {
						// pause thread to show progress bars swapping
						Thread.sleep(1000);

						// remove invoice progress bar and replace with email progress bar
						Platform.runLater(() -> {
							midVBox.getChildren().remove(invoiceProgressBar);
							midVBox.getChildren().add(emailProgressBar);
							emailProgressBar.setProgress(0.25f);
						});

						// do majority of work
						Enum<EmailResponse> e = EmailHelper.sendEmail(company, contact, pdfUrl);

						// call to Application Thread to update GUI
						Platform.runLater(() -> {

							// update progress
							emailProgressBar.setProgress(0.75f);

							AlertType type = null;
							String mailMsg = null;

							if (e == EmailResponse.EMAIL_SENT) {
								type = AlertType.CONFIRMATION;
								mailMsg = "The invoice was successfully sent to: " + contact.getEmail() + ".";
							} else if (e == EmailResponse.EMAIL_NOT_SENT) {
								type = AlertType.ERROR;
								mailMsg = "The email could not be sent. Please ensure you are using a gmail account with the correct password. "
										+ "These details can be changed in the 'state.xml' file.";
							} else if (e == EmailResponse.FILE_ERROR) {
								type = AlertType.ERROR;
								mailMsg = "The invoice .pdf file could not be located.";
							}

							// update progress
							emailProgressBar.setProgress(1.0f);
							// show confirmation
							Alert emailAlert = new Alert(type, mailMsg, ButtonType.OK);
							emailAlert.showAndWait();
						});

						return null;
					}

				} catch (IOException | DocumentException e) {

					// call to Application Thread to show alert
					Platform.runLater(() -> {
						// show alert box
						String errMsg = "Could not create invoice! :(";
						Alert errAlert = new Alert(AlertType.ERROR, errMsg, ButtonType.OK);
						errAlert.show();
					});

				} finally {

					// slow thread to show progress bar completed
					Thread.sleep(1000);
					Platform.runLater(() -> cleanUp());
				}
				return null;
			}
		});

		invoiceThread.setDaemon(true);
		invoiceThread.start();
	}

	// reset quantities and remove selections
	private void cleanUp() {

		// re-enable button
		createInvoiceButton.setDisable(false);

		ObservableList<Product> productList = FXCollections.observableArrayList(productTable.getItems());
		productList.forEach(product -> product.setQuantity("1"));
		productTable.getItems().clear();
		productTable.setItems(productList);

		// clear contact selection
		contactTable.getSelectionModel().clearSelection();

		// remove any progress bars left
		midVBox.getChildren().remove(invoiceProgressBar);
		midVBox.getChildren().remove(emailProgressBar);

		// reset progress bars
		invoiceProgressBar.setProgress(0.0f);
		emailProgressBar.setProgress(0.0f);

	}
}