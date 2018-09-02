package Java.Models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

//Tells JAXB you are annotating fields not getters/setters
@XmlAccessorType(XmlAccessType.FIELD)

// defines structure of xml file
@XmlType(name = "Address", propOrder = {"addressOne", "addressTwo", "town", "county", "postcode"})

@XmlRootElement(name = "Address")
public class Address {

	@XmlElement(name = "AddressLine1")
	private String addressOne;

	@XmlElement(name = "AddressLine2")
	private String addressTwo;

	@XmlElement(name = "Town")
	private String town;

	@XmlElement(name = "County")
	private String county;

	@XmlElement(name = "Postcode")
	private String postcode;

	// no arg constructor for JAXB
	public Address() {

	}

	public Address(String addressOne, String addressTwo, String town, String county, String postcode) {
		this.addressOne = addressOne;
		this.addressTwo = addressTwo;
		this.town = town;
		this.county = county;
		this.postcode = postcode;
	}
	
	public Address(int i) {
		this.addressOne = "AddressLineOne-"+i;
		this.addressTwo = "AddressLineTwo-"+i;
		this.town = "Town-"+i;
		this.county = "County-"+i;
		this.postcode = "Postcode-"+i;
	}

	public String getAddressOne() {
		return addressOne;
	}

	public void setAddressOne(String addressOne) {
		this.addressOne = addressOne;
	}

	public String getAddressTwo() {
		return addressTwo;
	}

	public void setAddressTwo(String addressTwo) {
		this.addressTwo = addressTwo;
	}

	public String getTown() {
		return town;
	}

	public void setTown(String town) {
		this.town = town;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

}
