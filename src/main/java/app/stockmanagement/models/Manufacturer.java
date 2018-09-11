package app.stockmanagement.models;

/**
 * 
 * @author Nguyen Duc Manh
 *
 * @manhn
 */
public class Manufacturer {

	private int id;
	private String name;
	private boolean active;
	
	public Manufacturer() {
		// TODO Auto-generated constructor stub
	}

	public Manufacturer(String name) {
		super();
		this.name = name;
	}

	public Manufacturer(String name, boolean active) {
		super();
		this.name = name;
		this.active = active;
	}

	public Manufacturer(int id, String name, boolean active) {
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
		return "Manufacturer [id=" + id + ", name=" + name + "]";
	}
	
	

}
