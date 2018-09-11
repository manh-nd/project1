package app.stockmanagement.models;

/**
 * 
 * @author Nguyen Duc Manh
 *
 * @manhn
 */
public class Category {

	private int id;
	private String name;
	private boolean active;
	
	public Category() {
	}

	public Category(String name) {
		super();
		this.name = name;
	}

	public Category(String name, boolean active) {
		super();
		this.name = name;
		this.active = active;
	}
	
	public Category(int id, String name, boolean active) {
		super();
		this.id = id;
		this.name = name;
		this.active = active;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public String toString() {
		return "Category [id=" + id + ", name=" + name + "]";
	}
	
	

}
