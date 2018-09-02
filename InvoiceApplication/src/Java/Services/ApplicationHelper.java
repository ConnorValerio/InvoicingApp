package Java.Services;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import Java.Models.Company;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;

public class ApplicationHelper {

	/* XML methods */

	public static void saveState(Company company) {

		/*
		 * save in ./Config/state.xml -> Marshall Object using JAXB
		 */

		try {

			JAXBContext jc = JAXBContext.newInstance(Company.class);
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(company, new File("./Config/state.xml"));

		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	// extract from XML DOM
	public static Company loadState() {

		/*
		 * load from ./Config/state.xml -> Unmarshall Object using JAXB
		 */

		Company company = null;

		try {
			JAXBContext jc = JAXBContext.newInstance(Company.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			company = (Company) unmarshaller.unmarshal(new File("./Config/state.xml"));
		} catch (JAXBException e) {
			e.printStackTrace();
		}

		return company;
	}

	public static void OpenPDFinBrowser(URI pdfFile) {
		if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(pdfFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			String msg = "This action has been blocked by your operating system." + " You are unable to view the invoice via this method."
					+ " If you wish to view an invoice, please refer to the local 'Invoices' folder.";
			Alert alert = new Alert(AlertType.WARNING, msg, ButtonType.OK);
			alert.showAndWait();
		}
	}

	/* Logo resizing method */
	public static Image resizeImage(Image fxLogo, int width, int height) throws IOException {

		// create whitespace
		BufferedImage buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = buffImg.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);

		// position of logo in whitespace
		int x = (int) ((width - fxLogo.getWidth()) / 2);
		int y = (int) ((height - fxLogo.getHeight()) / 2);

		// draw awt logo onto whitespace
		java.awt.Image awtLogo = SwingFXUtils.fromFXImage(fxLogo, null);
		g.drawImage(awtLogo, x, y, null);
		g.dispose();

		// return fx logo
		return SwingFXUtils.toFXImage(buffImg, null);
	}
}