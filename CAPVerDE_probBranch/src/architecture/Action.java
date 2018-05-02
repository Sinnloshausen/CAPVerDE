package architecture;

import java.io.Serializable;
import java.util.Set;

/**
 * Objects describing an action that a component can perform.
 */
public class Action implements Serializable {

	/**
	 * @serial Serial ID for storing architecture objects in files.
	 */
	private static final long serialVersionUID = -1426405675744686408L;

	/**
	 * All possible actions that a component can perform.
	 */
	public enum ActionType {
		HAS, COMPUTE, RECEIVE, CHECK, VERIF_P, VERIF_A, DELETE, TRUST
	}

	// Class fields
	private ActionType action;
	private Component component;
	private Variable var;
	private Equation eq;
	private Component comPartner;
	private Set<Variable> varSet;
	private Set<Equation> eqSet;
	private Set<Statement> stSet;
	private Proof pro;
	private Attest att;

	/**
	 * The full Constructor of an Action that is typically only invoked in this class
	 * by the other constructors.
	 * 
	 * @param action
	 *          the action to be performed
	 * @param component
	 *          the component that executes the action
	 * @param var
	 *          a variable that is possessed or transmitted
	 * @param eq
	 *          an equation that computed or transmitted
	 * @param comPartner
	 *          the component that is interacted with
	 * @param varSet
	 * 			a set of variables that is transmitted
	 * @param eqSet
	 *          a set of equations that is transmitted
	 * @param stSet
	 *          a set of statements that is transmitted
	 * @param eqSet
	 *          a set of equations that is transmitted
	 * @param pro
	 *          a proof that is transmitted
	 * @param att
	 *          an attestation that is transmitted
	 */
	public Action(ActionType action, Component component, Variable var, Equation eq,
			Component comPartner, Set<Variable> varSet, Set<Equation> eqSet,
			Set<Statement> stSet, Proof pro, Attest att) {
		this.action = action;
		this.component = component;
		this.var = var;
		this.eq = eq;
		this.comPartner = comPartner;
		this.varSet = varSet;
		this.eqSet = eqSet;
		this.stSet = stSet;
		this.pro = pro;
		this.att = att;
	}

	/**
	 * Constructor called for Action = HAS or DELETE.
	 * 
	 * @param action
	 *          the action to be performed (has | delete)
	 * @param component
	 *          the component that executes the action
	 * @param var
	 *          the variable that is possessed / deleted
	 */
	public Action(ActionType action, Component component, Variable var) {
		this(action, component, var, null, null, null, null, null, null, null);
	}

	/**
	 * Constructor called for Action = RECEIVE.
	 * 
	 * @param action
	 *          the action to be performed (receive)
	 * @param component
	 *          the receiving component
	 * @param comPartner
	 *          the sending component
	 * @param stSet
	 *          a list of statements (can be empty)
	 * @param varSet
	 *          a list of variables
	 */
	public Action(ActionType action, Component component, Component comPartner,
			Set<Statement> stSet, Set<Variable> varSet) {
		this(action, component, null, null, comPartner, varSet, null, stSet, null, null);
	}

	/**
	 * Constructor called for Action = COMPUTE.
	 * 
	 * @param action
	 *          the action to perform (compute)
	 * @param component
	 *          the component that executes the action
	 * @param eq
	 *          the equation that is computed
	 */
	public Action(ActionType action, Component component, Equation eq) {
		this(action, component, null, eq, null, null, null, null, null, null);
	}

	/**
	 * Constructor called for Action = CHECK.
	 * 
	 * @param action
	 *          the action to be performed (check)
	 * @param component
	 *          the component that executes the action
	 * @param eqSet
	 *          a list of equations to check
	 */
	public Action(ActionType action, Component component, Set<Equation> eqSet) {
		this(action, component, null, null, null, null, eqSet, null, null, null);
	}

	/**
	 * Constructor called for Action = Verif_P.
	 * 
	 * @param action
	 *          the action to perform (verify proof)
	 * @param component
	 *          the component that executes the action
	 * @param pro
	 *          a proof to verify
	 */
	public Action(ActionType action, Component component, Proof pro) {
		this(action, component, null, null, null, null, null, null, pro, null);
	}

	/**
	 * Constructor called for Action = Verif_A.
	 * 
	 * @param action
	 *          the action to perform (verify attest)
	 * @param component
	 *          the component that executes the action
	 * @param att
	 *          an attest to verify
	 */
	public Action(ActionType action, Component component, Attest att) {
		this(action, component, null, null, null, null, null, null, null, att);
	}

	/**
	 * Constructor called for Action = Trust.
	 * 
	 * @param action
	 *          the action to perform (trust)
	 * @param component
	 *          the component that executes the action
	 * @param comPartner
	 *          a component that the executing component trusts
	 */
	public Action(ActionType action, Component component, Component comPartner) {
		this(action, component, null, null, comPartner, null, null, null, null, null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + ((att == null) ? 0 : att.hashCode());
		result = prime * result + ((comPartner == null) ? 0 : comPartner.hashCode());
		result = prime * result + ((component == null) ? 0 : component.hashCode());
		result = prime * result + ((eq == null) ? 0 : eq.hashCode());
		result = prime * result + ((eqSet == null) ? 0 : eqSet.hashCode());
		result = prime * result + ((pro == null) ? 0 : pro.hashCode());
		result = prime * result + ((stSet == null) ? 0 : stSet.hashCode());
		result = prime * result + ((var == null) ? 0 : var.hashCode());
		result = prime * result + ((varSet == null) ? 0 : varSet.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Action other = (Action) obj;
		if (action != other.action)
			return false;
		if (att == null) {
			if (other.att != null)
				return false;
		} else if (!att.equals(other.att))
			return false;
		if (comPartner == null) {
			if (other.comPartner != null)
				return false;
		} else if (!comPartner.equals(other.comPartner))
			return false;
		if (component == null) {
			if (other.component != null)
				return false;
		} else if (!component.equals(other.component))
			return false;
		if (eq == null) {
			if (other.eq != null)
				return false;
		} else if (!eq.equals(other.eq))
			return false;
		if (eqSet == null) {
			if (other.eqSet != null)
				return false;
		} else if (!eqSet.equals(other.eqSet))
			return false;
		if (pro == null) {
			if (other.pro != null)
				return false;
		} else if (!pro.equals(other.pro))
			return false;
		if (stSet == null) {
			if (other.stSet != null)
				return false;
		} else if (!stSet.equals(other.stSet))
			return false;
		if (var == null) {
			if (other.var != null)
				return false;
		} else if (!var.equals(other.var))
			return false;
		if (varSet == null) {
			if (other.varSet != null)
				return false;
		} else if (!varSet.equals(other.varSet))
			return false;
		return true;
	}

	@Override
	public String toString() {
		switch (action) {
		case CHECK:
			return "Check_" + component + "(" + eqSet + ")";
		case COMPUTE:
			return "Compute_" + component + "(" + eq + ")";
		case HAS:
			return "Has_" + component + "(" + var + ")";
		case RECEIVE:
			return "Receive_" + component + "," + comPartner + "(" + stSet + "," + varSet + ")";
		case TRUST:
			return "Trust_" + component + "," + comPartner;
		case VERIF_A:
			return "VerifA_" + component + "(" + att + ")";
		case VERIF_P:
			return "VerifP_" + component + "(" + pro + ")";
		case DELETE:
			return "Delete_" + component + "(" + var + ")";
		default:
			break;
		}
		return "Action []";
	}

	// Getter and Setter methods
	public ActionType getAction() {
		return action;
	}

	public void setAction(ActionType action) {
		this.action = action;
	}

	public Component getComponent() {
		return component;
	}

	public void setComponent(Component component) {
		this.component = component;
	}

	public Variable getVar() {
		return var;
	}

	public void setVar(Variable var) {
		this.var = var;
	}

	public Equation getEq() {
		return eq;
	}

	public void setEq(Equation eq) {
		this.eq = eq;
	}

	public Component getComPartner() {
		return comPartner;
	}

	public void setComPartner(Component comPartner) {
		this.comPartner = comPartner;
	}

	public Set<Equation> getEqSet() {
		return eqSet;
	}

	/*public void setEqSet(Set<Equation> eqSet) {
		this.eqSet = eqSet;
	}*/

	public Set<Variable> getVarSet() {
		return varSet;
	}

	/*public void setVarSet(Set<Variable> varSet) {
		this.varSet = varSet;
	}*/

	public Set<Statement> getStSet() {
		return stSet;
	}

	/*public void setStSet(Set<Statement> stSet) {
		this.stSet = stSet;
	}*/

	public Proof getPro() {
		return pro;
	}

	public void setPro(Proof pro) {
		this.pro = pro;
	}

	public Attest getAtt() {
		return att;
	}

	public void setAtt(Attest att) {
		this.att = att;
	}

}
