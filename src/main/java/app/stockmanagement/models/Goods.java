package app.stockmanagement.models;

import java.sql.Date;

/**
 * 
 * @author Nguyen Duc Manh
 *
 * @manhn
 */
public class Goods {

	private int id;
	private String code;
	private String name;
	private Date expiryDate;
	private int importedPrice;
	private int exportedPrice;
	private int inStock;
	private Category category;
	private Manufacturer manufacturer;
	private Stock stock;

	public Goods() {
	}

	public Goods(String code, String name, Date expiryDate, int importedPrice, int exportedPrice, int inStock,
			Category category, Manufacturer manufacturer, Stock stock) {
		super();
		this.code = code;
		this.name = name;
		this.expiryDate = expiryDate;
		this.importedPrice = importedPrice;
		this.exportedPrice = exportedPrice;
		this.inStock = inStock;
		this.category = category;
		this.manufacturer = manufacturer;
		this.stock = stock;
	}

	public Goods(int id, String code, String name, Date expiryDate, int importedPrice, int exportedPrice,
			int inStock, Category category, Manufacturer manufacturer, Stock stock) {
		super();
		this.id = id;
		this.code = code;
		this.name = name;
		this.expiryDate = expiryDate;
		this.importedPrice = importedPrice;
		this.exportedPrice = exportedPrice;
		this.inStock = inStock;
		this.category = category;
		this.manufacturer = manufacturer;
		this.stock = stock;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		if(code==null) {
			
		}
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public int getImportedPrice() {
		return importedPrice;
	}

	public void setImportedPrice(int importedPrice) {
		this.importedPrice = importedPrice;
	}

	public int getExportedPrice() {
		return exportedPrice;
	}

	public void setExportedPrice(int exportedPrice) {
		this.exportedPrice = exportedPrice;
	}

	public int getInStock() {
		return inStock;
	}

	public void setInStock(int inStock) {
		this.inStock = inStock;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Manufacturer getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(Manufacturer manufacturer) {
		this.manufacturer = manufacturer;
	}

	public Stock getStock() {
		return stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	@Override
	public String toString() {
		return "Goods [id=" + id + ", code=" + code + ", name=" + name + ", expiryDate=" + expiryDate
				+ ", importedPrice=" + importedPrice + ", exportedPrice=" + exportedPrice + ", inStock=" + inStock
				+ ", category=" + category + ", manufacturer=" + manufacturer + ", stock=" + stock + "]";
	}

}
