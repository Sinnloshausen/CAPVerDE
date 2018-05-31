package properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import architecture.Action;
import architecture.Architecture;
import architecture.Attest;
import architecture.Component;
import architecture.Deduction;
import architecture.Dep;
import architecture.Equation;
import architecture.P;
import architecture.Variable;
import properties.Property.PropertyType;
import architecture.Action.ActionType;
import solver.SmtHandler;

/**
 * Parser that implements rules of inference to gather all Has, K and B
 * statements from an Architecture_Class object.
 */
public class RulesOfInferenceParserTopdown extends Parser implements Serializable {

	/**
	 * @serial Serial ID for storing architecture objects in files.
	 */
	private static final long serialVersionUID = -6741087571898715105L;

	// class fields
	private List<Property> propertyList;
	private SmtHandler smt;
	private int counter;
	private List<Action> actionLog;

	/**
	 * The constructor that already collects all properties and generates all
	 * necessary smt assertions.
	 * 
	 * @param arch
	 *          the architecture to parse
	 */
	public RulesOfInferenceParserTopdown(Architecture arch) {
		super(arch);
		counter = 0;
		propertyList = new ArrayList<Property>();
		actionLog = new ArrayList<Action>();
		// Apply the rules to fill the Has, K and B property list
		collectProperties();
		// set the list of properties for the architecture and trigger the
		// collection of equations
		arch.setAllProperties(propertyList);
		arch.collectEquations();
		arch.collectStatements();

		// already produce the statements based on the architecture
		// Apply the rules a second time to also apply the deduction properties
		// based on later added equations
		collectDeducProperties();
		// instantiate the SMT-handler
		smt = new SmtHandler(arch);
		for (Component comp : arch.getCompList()) {
			List<Property> compPropertyList = new ArrayList<Property>();
			// sub-list with properties of one component only
			for (Property prop : propertyList) {
				if (prop.getOwner().equals(comp)) {
					// add only the properties of the right component
					compPropertyList.add(prop);
				}
			}
			// add all necessary SMT-lines
			smt.generateSmtfromPropList(comp, compPropertyList);
		}
	}

	/**
	 * Method that generates the STM file from the property list and the provided
	 * statement.
	 * 
	 * @param statement
	 *          the statement to verify
	 * @param recurseDepth
	 *          the depth of recursion
	 * @return true, if the statement is satisfiable with the architecture
	 */
	@Override
	public boolean verifyStatement(Property statement, int recurseDepth) {
		counter++;
		// add the SMT-line for the provided statement to verify
		smt.generateSmtfromStatement(statement, counter);
		// do the verification
		return smt.verify(statement);
	}

	/**
	 * Helper method to collect all privacy property statements this method
	 * applies the inference rules in a top-down manner.
	 */
	private void collectProperties() {
		double one = 1;
		// Go through all actions of the architecture and apply the rules regarding
		// the actions
		for (Action action : arch.getAllActions()) {
			switch (action.getAction()) {
			case HAS:
				// Rule H1
				// add Has(i, 1, X)
				addProperty(new Property(
						PropertyType.HAS, action.getComponent(), one, action.getVar()));
				break;
			case RECEIVE:
				// Rule H2
				// add Has(i, 1, X), X in E
				for (Variable var : action.getVarSet()) {
					addProperty(new Property(PropertyType.HAS, action.getComponent(), one, var));
				}
				// Rule SH2
				// add isShared(i, X), X in E, Receive(j, k, S, E), Receive(k, i, S',
				// E'), X in E'
				for (Component comp : arch.getCompList()) {
					if (comp.equals(action.getComponent()) || comp.equals(action.getComPartner())) {
						continue;
					}
					for (Variable var : action.getVarSet()) {
						if (isContainedRec(action.getComPartner(), comp, var)) {
							// if this variable was shared more than once, it is considered
							// as "SHARED" with a third party for the (second time) sharing
							// component
							addProperty(new Property(PropertyType.SHARED, action.getComPartner(), var));
						}
					}
				}
				break;
			case COMPUTE:
				// Rule H3
				// add Has(i, 1, X), X left-hand side of E
				addProperty(
						new Property(PropertyType.HAS, action.getComponent(), one,
								action.getEq().getLefthandSide()));
				// Rule K1
				// add K(i, 1, Eq)
				addProperty(new Property(PropertyType.KNOWS, action.getComponent(), one, action.getEq()));
				break;
			case CHECK:
				// Rule K2
				// add K(i, 1, Eq), Eq in E
				for (Equation eq : action.getEqSet()) {
					addProperty(new Property(PropertyType.KNOWS, action.getComponent(), one, eq));
				}
				break;
			case VERIF_P:
				// Rule K3
				// add K(i, 1, Eq), Eq in Pro
				for (P p : action.getPro().getpSet()) {
					if (p instanceof Equation) {
						// TODO test and debug, if cast works
						addProperty(new Property(PropertyType.KNOWS, action.getComponent(), one, (Equation) p));
					} else if (p instanceof Attest) { // Rule K4
						// add K(i, 1, Eq), Eq in E', Attest(k, E') in E, Trust(i, k)
						// Only add the equation if the verifying component trusts the
						// attesting one
						if (arch.trust(action.getComponent(), ((Attest) p).getComponent())) {
							for (Equation eq : ((Attest) p).getEqSet()) {
								addProperty(new Property(PropertyType.KNOWS, action.getComponent(), one , eq));
							}
						}
					}
				}
				break;
			case VERIF_A:
				// Rule K5
				// add K(i, 1, Eq), Eq in E, Trust(i, j)
				if (arch.trust(action.getComponent(), action.getAtt().getComponent())) {
					// Only add the equations from the attest if the verifying component
					// trusts the attesting one
					for (Equation eq : action.getAtt().getEqSet()) {
						addProperty(new Property(PropertyType.KNOWS, action.getComponent(), one, eq));
					}
				}
				break;
			case DELETE:
				//TODO
				break;
			default:
				// Do nothing
				break;
			}
			// TODO good idea??
			actionLog.add(action);
		}

		// Then go through the collected properties and apply the remaining rules
		// for all components
		// TODO complete
		for (Component comp : arch.getCompList()) {
			// Rule H4
			// add Has(i, p, X), for all l in [1,n], Has(i, q, Xl), Dep(i, r, X, Xl), p=r*mult(q)
			//TODO
			for (Dep dep : comp.getDepSet()) {
				// for each dependence relation, check if all required variables are
				// already in the property list
				boolean all = true;
				for (Variable var : dep.getVarSet()) {
					// check if the variable is in the property list
					if (!isContainedVar(comp, var, dep.getProb())) {
						//TODO bullshit right here...
						// Only if all variables are
						all = false;
						break;
					}
				}
				if (all) {
					// add the variable
					addProperty(new Property(PropertyType.HAS, comp, dep.getProb(), dep.getVar()));
				}
			}
			for (Variable var : arch.getVariables()) {
				if (!isContainedReceive(comp, var)) {
					// Rule SH1
					// add notShared(i, X)
					addProperty(new Property(PropertyType.NOTSHARED, comp, var));
				}
				if (!isContainedReceive2(comp, var)) {
					// Rule SH2
					// add notShared(i, X)
					addProperty(new Property(PropertyType.NOTSHARED, comp, var));
				}
			}
			// Rule Kded
			// add K(i, Eq1), Dedution(E, Eq1), for all Eq in E, K(i, Eq)
			// TODO 
			for (Deduction ded : comp.getDeductionCapability()) {
				// only if the conclusion is something new
				if (!isContainedEq(comp, ded.getConclusion(), ded.getProb())) {
					boolean allK = true;
					for (Equation eq : ded.getPremises()) {
						// only if all equations are known
						if (!isContainedEq(comp, eq, ded.getProb())) {
							allK = false;
						}
						if (!(allK)) {
							break;
						}
					}
					if (allK) {
						// add the equation
						addProperty(new Property(PropertyType.KNOWS, comp, ded.getProb(), ded.getConclusion()));
					}
				}
			}
		}
	}

	/**
	 * Helper method that only collects properties from deduction.
	 */
	private void collectDeducProperties() {
		//TODO
		for (Component comp : arch.getCompList()) {
			for (Deduction ded : comp.getDeductionCapability()) {
				// only if the conclusion is something new
				if (!isContainedEq(comp, ded.getConclusion(), ded.getProb())) {
					boolean allK = true;
					for (Equation eq : ded.getPremises()) {
						// only if all equations are known
						if (!isContainedEq(comp, eq, ded.getProb())) {
							allK = false;
						}
						if (!allK) {
							break;
						}
					}
					if (allK) {
						// add the equation
						addProperty(new Property(PropertyType.KNOWS, comp, ded.getProb(), ded.getConclusion()));
					}
				}
			}
		}
	}

	/**
	 * Method to check whether a variable is already in the property list.
	 * 
	 * @param comp
	 *          the owner of the variable
	 * @param var
	 *          the variable
	 * @param prob
	 *          the probability
	 * @return true, if the variable is already in the property list (with the
	 *         right owner)
	 */
	private boolean isContainedVar(Component comp, Variable var, double prob) {
		// TODO 
		// create temporary property to compare with list
		Property tmpStmt = new Property(PropertyType.HAS, comp, prob, var);
		for (Property stmt : propertyList) {
			if (stmt.equals(tmpStmt)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method to check whether an equation is already known.
	 * 
	 * @param comp
	 *          the owner of the statement
	 * @param eq
	 *          the equation
	 * @param prob
	 *          the probability
	 * @return true, if the equation is known by the owner in the
	 *         property list
	 */
	private boolean isContainedEq(Component comp, Equation eq, double prob) {
		// TODO
		// create temporary property to compare with list
		Property tmpStmt = new Property(PropertyType.KNOWS, comp, prob, eq);
		boolean contained = false;
		for (Property stmt : propertyList) {
			if (stmt.equals(tmpStmt)) {
				contained = true;
				break;
			}
		}
		return contained;
	}

	/**
	 * Method to check in the action log whether a variable was already
	 * transmitted between two components.
	 * 
	 * @param comp
	 *          the receiving component
	 * @param compPartner
	 *          the sending component
	 * @param var
	 *          the variable
	 * @return true, if such an action was already processed
	 */
	private boolean isContainedRec(Component comp, Component compPartner, Variable var) {
		for (Action action : actionLog) {
			if (action.getAction() == ActionType.RECEIVE) {
				// only consider the ones with the correct sender/receiver pair
				if (action.getComponent().equals(comp) && action.getComPartner().equals(compPartner)) {
					// now check if the variable is contained in the list sent
					if (action.getVarSet().contains(var)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Method to check in the action log whether a variable was already received
	 * by a component.
	 * 
	 * @param comp
	 *          the receiving component
	 * @param var
	 *          the variable
	 * @return true, if such an action was already processed
	 */
	private boolean isContainedReceive(Component comp, Variable var) {
		for (Action action : actionLog) {
			if (action.getAction() == ActionType.RECEIVE) {
				// only consider the ones with the correct sender and variable
				if (action.getComponent().equals(comp) && action.getVarSet().contains(var)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Method to check in the action log whether a variable was already sent by a
	 * component.
	 * 
	 * @param comp
	 *          the sending component
	 * @param var
	 *          the variable
	 * @return true, if such an action was already processed
	 */
	private boolean isContainedReceive2(Component comp, Variable var) {
		for (Action action : actionLog) {
			if (action.getAction() == ActionType.RECEIVE) {
				// only consider the ones with the correct sender and variable
				if (action.getComPartner().equals(comp) && action.getVarSet().contains(var)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Method that adds properties to the list.
	 * 
	 * @param prop
	 *          the property
	 */
	private void addProperty(Property prop) {
		if (!propertyList.contains(prop)) {
			propertyList.add(prop);
			// also update the list of equations of the arch
			arch.addEquation(prop.getEq());
		}
	}

	// Setter and getter methods
	public Architecture getArch() {
		return arch;
	}

	public void setArch(Architecture arch) {
		this.arch = arch;
	}

}
