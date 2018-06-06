package architecture;

import java.io.Serializable;

/**
 * Objects representing variables.
 */
public class Variable implements Serializable {

	/**
	 * @serial Serial ID for storing architecture objects in files.
	 */
	private static final long serialVersionUID = 8777118707453787356L;

	// Member variables
	private String name;
	private String instanceIndex;

	/**
	 * The complete constructor for variables.
	 * 
	 * @param name
	 *          the name of the variable
	 */
	public Variable(String name, String instanceIndex) {
		// default constructor
		this.name = name;
		this.instanceIndex = instanceIndex;
	}
	
	/**
	 * The small constructor for variables without instance index.
	 * @param name
	 * 			the name of the variable
	 */
	public Variable(String name) {
		// default constructor
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Variable other = (Variable) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		// Return the name of the variable
		return name;
	}

	/**
	 * Method to compare two variable by their name.
	 * 
	 * @param name
	 *          the name of the variable to compare with
	 * @return true / false
	 */
	public boolean hasName(String name) {
		// Check whether the Variable has the provided name
		return name.equals(this.name);
	}

	// Getter and setter methods
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getIndex() {
		return instanceIndex;
	}

	public void setIndex(String instanceIndex) {
		this.instanceIndex = instanceIndex;
	}
}
