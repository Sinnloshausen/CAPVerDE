package architecture;

import java.io.Serializable;
import java.util.Set;

/**
 * Objects describing attestations of equations or proofs.
 */
public class Attest implements P, Statement, Serializable {

	/**
	 * @serial Serial ID for storing architecture objects in files.
	 */
	private static final long serialVersionUID = 7509981787693014479L;

	// class fields
	private Component component;
	private Set<Equation> eqSet;

	/**
	 * The constructor for attestations.
	 * 
	 * @param component
	 *          the component attesting something
	 * @param eqSet
	 *          a list of equations that are attested
	 */
	public Attest(Component component, Set<Equation> eqSet) {
		this.component = component;
		this.eqSet = eqSet;
	}

	@Override
	public String toString() {
		// Return the equation as readable string
		return "" + component + " attests: " + eqSet;
	}

	@Override
	public String getName() {
		return toString().replaceAll("\\s", "");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((component == null) ? 0 : component.hashCode());
		result = prime * result + ((eqSet == null) ? 0 : eqSet.hashCode());
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
		Attest other = (Attest) obj;
		if (component == null) {
			if (other.component != null) {
				return false;
			}
		} else if (!component.equals(other.component)) {
			return false;
		}
		if (eqSet == null) {
			if (other.eqSet != null) {
				return false;
			}
		} else if (!eqSet.equals(other.eqSet)) {
			return false;
		}
		return true;
	}

	// Getter and setter methods
	public Component getComponent() {
		return component;
	}

	public void setComponent(Component component) {
		this.component = component;
	}

	public Set<Equation> getEqSet() {
		return eqSet;
	}

	public void setEqSet(Set<Equation> eqSet) {
		this.eqSet = eqSet;
	}
}
