package uk.ac.ebi.owlapi.extension;

public class ManOBORelation {
	
	private String id;
	private String name;
	private String owldef;
	
	
	public ManOBORelation(String id, String name, String owldef) {
		this.id = id;
		this.name = name;
		this.owldef = owldef;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOwldef() {
		return owldef;
	}
	public void setOwldef(String owldef) {
		this.owldef = owldef;
	}
	
	
}
