package architecture;

import java.io.Serializable;
import java.util.Set;

/**
 * Objects describing proofs that are linked to a component
 * and contain either attestations or equations (or both).
 */
public class Proof implements Statement, Serializable {

	/**
	 * @serial Serial ID for storing architecture objects in files.
	 */
	private static final long serialVersionUID = -3838273420235570322L;

	@Override
	public String toString() {
		return "" + component + " proves: " + pSet;
	}

	// class fields
	private Component component;
	private Set<P> pSet;

	/**
	 * The constructor for proofs.
	 * @param component
	 * 			the proving component
	 * @param pSet
	 * 			the set of attestations and equations to prove
	 */
	public Proof(Component component, Set<P> pSet) {
		this.component = component;
		this.pSet = pSet;
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
		result = prime * result + ((pSet == null) ? 0 : pSet.hashCode());
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
		Proof other = (Proof) obj;
		if (component == null) {
			if (other.component != null) {
				return false;
			}
		} else if (!component.equals(other.component)) {
			return false;
		}
		if (pSet == null) {
			if (other.pSet != null) {
				return false;
			}
		} else if (!pSet.equals(other.pSet)) {
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

	public Set<P> getpSet() {
		return pSet;
	}

	public void setpSet(Set<P> pSet) {
		this.pSet = pSet;
	}

	public void addpSet(P p) {
		pSet.add(p);
	}
}
