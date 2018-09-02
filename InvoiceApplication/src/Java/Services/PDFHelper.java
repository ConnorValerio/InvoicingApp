package Java.Services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.FontSelector;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import Java.Models.Address;
import Java.Models.Company;
import Java.Models.Contact;
import Java.Models.Product;

public class PDFHelper {

	public static void createInvoice(Company company, Contact contact, ArrayList<Product> products, String url)
			throws IOException, DocumentException {

		// work out total price of invoice
		float total = 0;
		for (Product product : products) {
			total += product.getPrice() * Integer.parseInt(product.getQuantity());
		}

		// step 1
		Document document = new Document(PageSize.A4, 0, 0, 20, 20);

		// step 2
		PdfWriter.getInstance(document, new FileOutputStream(url));

		// step 3
		document.open();

		// step 4 - generate document
		document.add(createTopPanel(company));
		document.add(createMiddlePanel(company, contact, total));

		// line separator
		LineSeparator ls = new LineSeparator();
		ls.setPercentage(85);
		ls.setLineColor(new BaseColor(128, 128, 128));
		ls.setLineWidth(2);
		document.add(new Chunk(ls));

		document.add(createBottomPanel(company, contact));

		// create a panel for each product
		for (Product product : products) {
			document.add(addProductPanel(product, Integer.parseInt(product.getQuantity())));
		}

		// step 5
		document.close();
	}

	private static PdfPTable createTopPanel(Company company) throws DocumentException, MalformedURLException, IOException {
		// a table with 4 columns
		PdfPTable table = new PdfPTable(4);
		table.setWidthPercentage(100);
		table.setWidths(new int[]{3, 2, 2, 2});

		// the cell object
		PdfPCell cell;

		// re-usable objects
		FontSelector selector;
		Font font;
		Phrase phrase;
		Paragraph paragraph;
		StringBuilder sb;

		// Arial font
		BaseFont arialBase = BaseFont.createFont("C:/windows/fonts/arial.ttf", BaseFont.WINANSI, true);

		// Invoice label cell
		selector = new FontSelector();
		font = new Font(arialBase, 30, Font.BOLD);
		font.setColor(BaseColor.WHITE);
		selector.addFont(font);
		phrase = selector.process("INVOICE");

		cell = new PdfPCell(phrase);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setBackgroundColor(new BaseColor(128, 128, 128, 128));
		cell.setBorderColor(new BaseColor(128, 128, 128));
		cell.setBorderWidth(2);
		cell.setPaddingLeft(20);
		cell.setFixedHeight(120);
		table.addCell(cell);

		// Company details Cell
		sb = new StringBuilder();
		sb.append(company.getName() + "\n");
		sb.append(company.getTelephoneNumber() + "\n");
		sb.append(company.getWebAddress() + "\n");
		sb.append(company.getEmail() + "\n");

		font = new Font(arialBase, 9, Font.NORMAL);
		font.setColor(BaseColor.WHITE);
		paragraph = new Paragraph(sb.toString());
		paragraph.setFont(font);
		paragraph.setAlignment(Element.ALIGN_RIGHT);

		cell = new PdfPCell();
		cell.addElement(paragraph);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setBackgroundColor(new BaseColor(128, 128, 128));
		cell.setBorderColor(new BaseColor(128, 128, 128));
		cell.setBorderWidth(2);
		cell.setPaddingBottom(10);
		cell.setFixedHeight(120);
		table.addCell(cell);

		// Company Address details Cell
		sb = new StringBuilder();
		Address addr = company.getAddress();
		sb.append(addr.getAddressOne() + "\n");
		sb.append(addr.getAddressTwo() + "\n");
		sb.append(addr.getTown() + ", ");
		sb.append(addr.getCounty() + "\n");
		sb.append(addr.getPostcode() + "\n");

		font = new Font(arialBase, 9, Font.NORMAL);
		font.setColor(BaseColor.WHITE);
		paragraph = new Paragraph(sb.toString());
		paragraph.setFont(font);
		paragraph.setAlignment(Element.ALIGN_RIGHT);

		cell = new PdfPCell();
		cell.addElement(paragraph);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setBackgroundColor(new BaseColor(128, 128, 128));
		cell.setBorderColor(new BaseColor(128, 128, 128));
		cell.setBorderWidth(2);
		cell.setPaddingBottom(10);
		cell.setPaddingRight(20);
		cell.setFixedHeight(120);
		table.addCell(cell);

		// Company Logo Cell
		Image logo = Image.getInstance("./Config/logo.jpg");
		logo.setAlignment(Element.ALIGN_CENTER);
		logo.setScaleToFitLineWhenOverflow(true);
		cell = new PdfPCell(logo);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setBackgroundColor(BaseColor.WHITE);
		cell.setBorderColor(new BaseColor(128, 128, 128));
		cell.setBorderWidth(2);
		cell.setPadding(10);
		cell.setFixedHeight(120);
		table.addCell(cell);

		// add spacing after the table
		table.setSpacingAfter(60);

		return table;
	}

	private static PdfPTable createMiddlePanel(Company company, Contact contact, float total) throws DocumentException, IOException {

		// a table with 3 columns
		PdfPTable table = new PdfPTable(3);
		table.setWidthPercentage(85);
		table.setWidths(new int[]{3, 2, 4});

		// the cell object
		PdfPCell cell;

		// re-usable objects
		Font labelFont, paragraphFont, invoiceTotalFont;
		Phrase phrase;
		Paragraph paragraph;
		FontSelector selector;

		BaseFont arialBase = BaseFont.createFont("C:/windows/fonts/arial.ttf", BaseFont.WINANSI, true);

		// Font selector for top labels
		selector = new FontSelector();
		labelFont = new Font(arialBase, 14, Font.NORMAL);
		labelFont.setColor(new BaseColor(70, 70, 70));
		selector.addFont(labelFont);

		// Font for paragraphs below labels
		paragraphFont = new Font(arialBase, 12, Font.NORMAL);

		// Font for invoice total
		invoiceTotalFont = new Font(arialBase, 30, Font.NORMAL);
		invoiceTotalFont.setColor(new BaseColor(128, 128, 128));

		// Billed to column
		phrase = selector.process("Billed to:");
		cell = new PdfPCell(phrase);
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);

		// Invoice Number label column
		phrase = selector.process("Invoice Number");
		cell = new PdfPCell(phrase);
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);

		// 'Invoice Total' column
		phrase = selector.process("Invoice Total");
		cell = new PdfPCell(phrase);
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(cell);

		// Client Address cell
		StringBuilder sb = new StringBuilder();
		Address addr = contact.getAddress();
		sb.append(contact.getName() + "\n");
		sb.append(addr.getAddressOne() + ",\n");

		// only add Address Line 2 if present
		if (!addr.getAddressTwo().equals("") && addr.getAddressTwo() != null) {
			sb.append(addr.getAddressTwo() + ",\n");
		}

		sb.append(addr.getTown() + ", ");
		sb.append(addr.getCounty() + "\n");
		sb.append(addr.getPostcode() + "\n");

		paragraph = new Paragraph(sb.toString());
		paragraph.setFont(paragraphFont);

		cell = new PdfPCell();
		cell.setBorder(Rectangle.NO_BORDER);
		cell.addElement(paragraph);
		cell.setPaddingBottom(10);
		cell.setRowspan(10);
		table.addCell(cell);

		// Invoice # Column
		selector = new FontSelector();
		selector.addFont(paragraphFont);

		// get company invoice number
		int num = company.getInvoiceNumber();
		String numStr = String.valueOf(num);

		while (numStr.length() < 4) {
			numStr = "0" + numStr;
		}

		phrase = selector.process("#" + numStr);

		cell = new PdfPCell();
		cell.addElement(phrase);
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setPaddingTop(5);
		table.addCell(cell);

		// Total # column
		String totalString = "£" + String.format("%.2f", total);
		paragraph = new Paragraph(totalString);
		paragraph.setFont(invoiceTotalFont);
		paragraph.setAlignment(Element.ALIGN_RIGHT);

		cell = new PdfPCell();
		cell.setBorder(Rectangle.NO_BORDER);
		cell.addElement(paragraph);
		table.addCell(cell);

		// Date of issue label cell
		selector = new FontSelector();
		selector.addFont(labelFont);
		phrase = selector.process("Date of Issue");
		cell = new PdfPCell(phrase);
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setColspan(2);
		table.addCell(cell);

		// Date of issue cell
		selector = new FontSelector();
		selector.addFont(paragraphFont);
		phrase = selector.process(LocalDate.now().toString());

		cell = new PdfPCell(phrase);
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setPaddingTop(5);
		cell.setColspan(2);
		cell.setPaddingBottom(20);
		table.addCell(cell);

		// add spacing after the table
		table.setSpacingAfter(20);

		return table;
	}

	private static PdfPTable createBottomPanel(Company company, Contact contact) throws DocumentException, IOException {

		// a table with 3 columns
		PdfPTable table = new PdfPTable(4);
		table.setSpacingBefore(30);
		table.setWidthPercentage(85);
		table.setWidths(new int[]{7, 2, 2, 2});

		// the cell object
		PdfPCell cell;

		// re-usable objects
		Font font;
		Phrase phrase;

		BaseFont arialBase = BaseFont.createFont("C:/windows/fonts/arial.ttf", BaseFont.WINANSI, true);

		// Label font
		font = new Font(arialBase, 14, Font.NORMAL);
		font.setColor(new BaseColor(70, 70, 70));

		// 'Product/Service' column
		phrase = new Phrase("Description");
		phrase.setFont(font);

		cell = new PdfPCell();
		cell.addElement(phrase);
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);

		// Unit price column
		phrase = new Phrase("Unit Price");
		phrase.setFont(font);

		cell = new PdfPCell();
		cell.addElement(phrase);
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);

		// 'Invoice Number' column
		phrase = new Phrase("Quantity");
		phrase.setFont(font);

		cell = new PdfPCell();
		cell.addElement(phrase);
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);

		// 'Total' column
		phrase = new Phrase("Amount");
		phrase.setFont(font);

		cell = new PdfPCell();
		cell.addElement(phrase);
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);

		table.setSpacingAfter(5);

		return table;
	}

	private static PdfPTable addProductPanel(Product product, Integer quantity) throws DocumentException, IOException {

		// a table with 3 columns
		PdfPTable table = new PdfPTable(4);
		table.setSpacingBefore(10);
		table.setWidthPercentage(85);
		table.setWidths(new int[]{7, 2, 2, 2});

		// the cell object
		PdfPCell cell;

		// re-usable objects
		FontSelector selector;
		Font font, descriptionFont;
		Phrase phrase;

		BaseFont arialBase = BaseFont.createFont("C:/windows/fonts/arial.ttf", BaseFont.WINANSI, true);

		// label font
		font = new Font(arialBase, 12, Font.NORMAL);

		// description font;
		descriptionFont = new Font(arialBase, 10, Font.NORMAL);
		descriptionFont.setColor(new BaseColor(70, 70, 70));

		// selector for top 3 cells
		selector = new FontSelector();
		selector.addFont(font);

		// product name cell
		phrase = selector.process(product.getName());

		cell = new PdfPCell();
		cell.addElement(phrase);
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);

		// unit price cell
		String unitPrice = product.getFormattedPrice();
		phrase = selector.process(unitPrice);

		cell = new PdfPCell();
		cell.addElement(phrase);
		cell.setRowspan(2);
		cell.setBorder(Rectangle.BOTTOM);
		cell.setBorderColor(new BaseColor(128, 128, 128));
		table.addCell(cell);

		// quantity cell
		phrase = selector.process(Integer.toString(quantity));

		cell = new PdfPCell(phrase);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setRowspan(2);
		cell.setPaddingTop(5);
		cell.setBorder(Rectangle.BOTTOM);
		cell.setBorderColor(new BaseColor(128, 128, 128));
		table.addCell(cell);

		// amount cell
		float amount = product.getPrice() * quantity;
		String amountString = "£" + String.format("%.2f", amount);
		phrase = selector.process(amountString);

		cell = new PdfPCell();
		cell.addElement(phrase);
		cell.setRowspan(2);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setBorder(Rectangle.BOTTOM);
		cell.setBorderColor(new BaseColor(128, 128, 128));
		table.addCell(cell);

		// product description cell
		selector = new FontSelector();
		selector.addFont(descriptionFont);
		phrase = selector.process(product.getDescription());

		cell = new PdfPCell();
		cell.addElement(phrase);
		cell.setPaddingBottom(15);
		cell.setPaddingRight(15);
		cell.setBorder(Rectangle.BOTTOM);
		cell.setBorderColor(new BaseColor(128, 128, 128));
		table.addCell(cell);

		return table;
	}

	public static void pdfToImage(String pdfUrl) throws InvalidPasswordException, IOException {

		// initial target
		String initTarget = pdfUrl.substring(0, pdfUrl.length() - 4);
		String finalTarget = initTarget;

		/*
		 * Possible part-implementation for multiple page invoices int count = 1;
		 * 
		 * while (new File(finalTarget + ".jpg").exists()) { finalTarget = initTarget + "(" + count + ")"; count++; }
		 * 
		 */

		PDDocument document = PDDocument.load(new File(pdfUrl));
		PDFRenderer pdfRenderer = new PDFRenderer(document);
		for (int page = 0; page < document.getNumberOfPages(); page++) {
			BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
			ImageIOUtil.writeImage(bim, finalTarget + ".jpg", 300);
		}

		document.close();
	}
}
