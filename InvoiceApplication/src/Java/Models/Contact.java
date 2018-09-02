package Java.Models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

//Tells JAXB you are annotating fields not getters/setters
@XmlAccessorType(XmlAccessType.FIELD)

// defines structure of xml file
@XmlType(name = "Contact", propOrder = {"name", "address",  "email"})

@XmlRootElement(name = "Contact")
public class Contact {

	@XmlElement(name = "Name")
	private String name;
	
	@XmlElement(name = "Address")
	private Address address;
	
	@XmlElement(name = "EmailAddress")
	private String email;

	// no arg constructor for JAXB
	public Contact() {

	}

	public Contact(String name, Address addr, String email) {
		this.name = name;
		this.address = addr;
		this.email = email;
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
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
}
