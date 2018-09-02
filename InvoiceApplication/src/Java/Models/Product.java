package Java.Models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

//Tells JAXB you are annotating fields not getters/setters
@XmlAccessorType(XmlAccessType.FIELD)

// defines structure of xml file
@XmlType(name = "Product", propOrder = {"name", "description", "price", "formattedPrice", "quantity"})

@XmlRootElement(name = "Product")
public class Product {

	@XmlElement(name = "Name")
	private String name;

	@XmlElement(name = "Description")
	private String description;

	@XmlElement(name = "Price")
	private float price;

	@XmlElement(name = "FormattedPrice")
	private String formattedPrice;

	@XmlElement(name = "Quantity")
	private String quantity; // to catch parse exceptions in TableView

	// no arg constructor for JAXB
	public Product() {

	}

	public Product(String name, String description, float price) {
		this.name = name;
		this.description = description;
		this.price = price;
		this.formattedPrice = "£" + String.format("%.2f", price);
		this.quantity = String.valueOf(1);
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFormattedPrice() {
		return this.formattedPrice;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}
}
