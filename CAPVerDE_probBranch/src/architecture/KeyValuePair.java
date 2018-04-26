package architecture;

import java.util.List;

public class KeyValuePair {
	
	// class fields
	private Variable key;
	private List<Variable> values;
	
	/**
	 * The constructor of a key-value-pair.
	 * @param key
	 * 		  the key / index as variable
	 * @param values
	 * 		  the variables to store
	 */
	public KeyValuePair(Variable key, List<Variable> values) {
		this.key = key;
		this.values = values;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
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
		KeyValuePair other = (KeyValuePair) obj;
		if (key == null) {
			if (other.key != null) {
				return false;
			}
		} else if (!key.equals(other.key)) {
			return false;
		}
		if (values == null) {
			if (other.values != null) {
				return false;
			}
		} else if (!values.equals(other.values)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "(" + key + ", " + values;
	}
	
	// getter and setter methods
	public Variable getKey() {
		return key;
	}

	public void setKey(Variable key) {
		this.key = key;
	}

	public List<Variable> getValues() {
		return values;
	}

	public void setValues(List<Variable> values) {
		this.values = values;
	}

}
