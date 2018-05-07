package architecture;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Objects that describe a trust relation between two components.
 * Either a blind trust, or just the trust that a defined set of variables can be shared.
 */
public class Trust {

	// class fields
	private Component truster;
	private Component trustee;
	private Set<Variable> vars;

	/**
	 * Constructor for trust. This creates a blind trust, if the variable list is empty.
	 * @param i
	 * 		  the trusting component
	 * @param j
	 * 		  the trusting component
	 * @param vars
	 * 		  the list of variables that j is trusted with
	 */
	public Trust(Component i, Component j, Set<Variable> vars) {
		this.truster = i;
		this.trustee = j;
		this.vars = vars;
	}
	
	/**
	 * Short Constructor for blind trust
	 * @param i
	 * 		  the trusting component
	 * @param j
	 * 		  the trusting component
	 */
	public Trust(Component i, Component j) {
		this.truster = i;
		this.trustee = j;
		this.vars = new LinkedHashSet<Variable>();
	}

	@Override
	public String toString() {
		if (vars.isEmpty()) {
			// blind trust
			return truster + " blindly trusts " + trustee;
		} else {
			// variable-related trust
			return truster + "trusts " + trustee + " with " + vars;
		}
	}

	/**
	 * Method that checks if this trust relation involves the two provided components.
	 * @param comp1
	 *          the first component
	 * @param comp2
	 *          the second component
	 * @param varSet
	 *          the (potentially empty) set of variables
	 * @return    true, if the four components are pairwise equal
	 */
	public boolean isEqual(Component comp1, Component comp2, Set<Variable> varSet) {
		return truster.equals(comp1) && trustee.equals(comp2) && containsVars(varSet);
	}

	/**
	 * Helper method that checks if a provided list of variables is contained in the variables of this trust.
	 * Also returns true, if the list of this trust is empty (blind trust).
	 * @param varSet
	 * 		  the list of variables
	 * @return
	 * 		  true, if the variable are trusted ones
	 */
	private boolean containsVars(Set<Variable> varSet) {
		// TODO test this
		if (vars.isEmpty()) {
			// blind trust
			return true;
		} else if (vars.containsAll(varSet)) {
			// all variables are trusted
			return true;
		}
		return false;
	}

	// getter and setter methods
	public Component getTruster() {
		return truster;
	}

	public void setTruster(Component i) {
		this.truster = i;
	}

	public Component getTrustee() {
		return trustee;
	}

	public void setTrustee(Component j) {
		this.trustee = j;
	}

	public Set<Variable> getVars() {
		return vars;
	}

	public void setVars(Set<Variable> vars) {
		this.vars = vars;
	}

}
