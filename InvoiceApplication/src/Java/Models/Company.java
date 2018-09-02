package Java.Models;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import Java.Services.ApplicationHelper;

// Tells JAXB you are annotating fields not getters/setters
@XmlAccessorType(XmlAccessType.FIELD)

// defines structure of xml file
@XmlType(name = "Company", propOrder = {"name", "address", "telephoneNumber", "email", "password", "webAddress", "invoiceNumber", "contacts",
		"products"})

@XmlRootElement(name = "Company")
public class Company {

	@XmlElement(name = "Name")
	private String name;

	@XmlElement(name = "Address")
	private Address address;

	@XmlElement(name = "TelephoneNumber")
	private String telephoneNumber;

	@XmlElement(name = "EmailAddress")
	private String email;

	@XmlElement(name = "EmailPassword")
	private String password;

	@XmlElement(name = "WebsiteAddress")
	private String webAddress;

	@XmlElement(name = "InvoiceCount")
	private int invoiceNumber;

	// stores contacts to send invoices to
	@XmlElementWrapper(name = "ContactList", required = true)
	private ArrayList<Contact> contacts;

	// stores products to create invoices for
	@XmlElementWrapper(name = "ProductList", required = true)
	private ArrayList<Product> products;

	public Company() {
		this.invoiceNumber = 1;
		products = new ArrayList<Product>();
		contacts = new ArrayList<Contact>();
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	public String getWebAddress() {
		return webAddress;
	}
	public void setWebAddress(String webAddress) {
		this.webAddress = webAddress;
	}
	public String getTelephoneNumber() {
		return telephoneNumber;
	}
	public void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public ArrayList<Product> getProducts() {
		return products;
	}
	public void setProducts(ArrayList<Product> products) {
		this.products = products;
	}
	public void addProduct(Product product) {
		this.products.add(product);
		this.saveState();
	}

	public void deleteProduct(Product product) {
		this.products.remove(product);
		this.saveState();
	}

	public ArrayList<Contact> getContacts() {
		return contacts;
	}
	public void setContacts(ArrayList<Contact> contacts) {
		this.contacts = contacts;
	}
	public void addContact(Contact contact) {
		this.contacts.add(contact);
		this.saveState();
	}

	public void deleteContact(Contact contact) {
		this.contacts.remove(contact);
		this.saveState();
	}

	public int getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(int invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
		this.saveState();
	}

	private void saveState() {
		ApplicationHelper.saveState(this);
	}

}
