package architecture;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import utils.SmtCleanup;

/**
 * Objects that describe a component in an architecture with all its actions and
 * equations, and so on.
 */
public class Component implements Serializable {

	/**
	 * @serial Serial ID for storing architecture objects in files.
	 */
	private static final long serialVersionUID = 5617976824731976353L;

	// Class fields
	private String name;
	private Set<Action> actions;
	private Set<Variable> varSet;
	private Set<Dep> depSet;
	private Set<Deduction> deducSet;
	private Set<Deduction> deductionCapability;
	private Set<Equation> eqSet;
	private Map<Variable, Integer> counter;
	private Component composition;
	private String instanceIndex;

	/**
	 * The full Constructor typically invoked for an already complete component
	 * or by the empty constructor in this class.
	 * @param name
	 * 			the name of the component
	 * @param actions
	 * 			a set of all actions this component can perform
	 * @param depSet
	 * 			a set of all dependence relations of this component
	 * @param deducSet
	 * 			a set of all deductions of this component
	 * @param composition
	 * 			the component that is composed of this one
	 * @param hasInstances
	 * 			a flag that indicates multiple instances for this component
	 */
	public Component(String name, Set<Action> actions, Set<Dep> depSet,
			Set<Deduction> deducSet, Component composition, String instanceIndex) {
		this.name = name;
		this.actions = new LinkedHashSet<Action>(actions);
		this.depSet = new LinkedHashSet<Dep>(depSet);
		this.deducSet = new LinkedHashSet<Deduction>(deducSet);
		this.composition = composition;
		this.instanceIndex = instanceIndex;
		varSet = new LinkedHashSet<Variable>();
		eqSet = new LinkedHashSet<Equation>();
		deductionCapability = new LinkedHashSet<Deduction>();
		counter = new HashMap<Variable, Integer>();
		collectVars();
		//makeCounter();
	}
	
	/**
	 * The Constructor for components with only a name and the instances flag.
	 * 
	 * @param name
	 *          the name of the component
	 * @param hasInstances
	 * 			the flag indicating instances
	 */
	public Component(String name, String instanceIndex) {
		this(name, new LinkedHashSet<Action>(), new LinkedHashSet<Dep>(), new LinkedHashSet<Deduction>(), null, instanceIndex);
	}

	/**
	 * The Constructor for empty components.
	 * 
	 * @param name
	 *          the name of the component
	 */
	public Component(String name) {
		this(name, new LinkedHashSet<Action>(), new LinkedHashSet<Dep>(), new LinkedHashSet<Deduction>(), null, null);
	}

	/**
	 * Helper method to collect all the variables that the component encounters.
	 */
	private void collectVars() {
		// go through all actions and collect all the variables
		for (Action action : actions) {
			switch (action.getAction()) {
			case HAS:
				if (action.getVar() != null) {
					// add the variable that is owned by the component
					varSet.add(action.getVar());
				}
				break;
			case COMPUTE:
				// only add the newly computed variable
				varSet.add(action.getEq().getLefthandSide());
				break;
			default:
				// nothing to do
			}
		}
	}

	/**
	 * Helper method that makes an explicit list of deduction from the variable ones.
	 */
	private void makeDeduction() {
		// go through deducSet and make explicit deduction with all known equations
		for (Deduction deduc : deducSet) {
			for (int index1 = 0; index1 < eqSet.size(); index1++) {
				Equation eq1 = new ArrayList<Equation>(eqSet).get(index1);
				Deduction tmpDeduc1 = new Deduction(deduc, deduc.getName() + "Explicit");
				// for each equation that matches the first premise
				Set<Equation> match1 = eq1.match2(new ArrayList<Equation>(deduc.getPremises()).get(0));
				if (!match1.isEmpty()) {
					// update the deduction with the real variables
					tmpDeduc1.update(match1);
					// go through the remaining premise and look for matches
					if (deduc.getPremises().size() > 1) {
						for (int index2 = 0; index2 < eqSet.size(); index2++) {
							Equation eq2 = new ArrayList<Equation>(eqSet).get(index2);
							if (eq2 == eq1) {
								continue;
							}
							Deduction tmpDeduc2 = new Deduction(tmpDeduc1, tmpDeduc1.getName() + "Final");
							Set<Equation> match2 = eq2.match2(new ArrayList<Equation>(tmpDeduc1.getPremises()).get(1));
							if (!match2.isEmpty()) {
								// further update the deduction with real values
								tmpDeduc2.update(match2);
								// TODO currently max two premises assumed

								// TODO does this line destroy other deductions??
								// here only the conclusion is updated with the second premise
								tmpDeduc2.getConclusion().update(new ArrayList<Equation>(tmpDeduc2.getPremises()).get(1));

								// add the explicit deduction to the list
								tmpDeduc2.getConclusion().setName(tmpDeduc2.getConclusion().getName() + "_"
										+ SmtCleanup.removeParantheses(tmpDeduc2.getConclusion().getOp1().toString()));
								if (!tmpDeduc2.getPremises().contains(tmpDeduc2.getConclusion())) {
									// only add the new deduction, if it is not too complex
									// i.e., do not add: conclusions like -> f(g(f(g(y))))=y
									addDeductionCapability(tmpDeduc2);
								}
							}
						}
					} else {
						// add the deduction with only one premise
						tmpDeduc1.getConclusion().setName(tmpDeduc1.getConclusion().getName() + "_"
								+ SmtCleanup.removeParantheses(tmpDeduc1.getConclusion().getOp1().toString()));
						// only add deduction that have something new to offer
						if (!tmpDeduc1.getPremises().contains(tmpDeduc1.getConclusion())) {
							addDeductionCapability(tmpDeduc1);
						}
					}
				}
			}

		}
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
		Component other = (Component) obj;
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
		return name;
	}

	/**
	 * Method that adds an action to the list (and also subsequently call the
	 * {@link #collectVars() collectVars()} method).
	 * @param action
	 *          the action to add
	 */
	public void addAction(Action action) {
		actions.add(action);
		// reevaluate the involved variables
		collectVars();
		// also update the counter values
		//makeCounter();
	}

	/**
	 * Setter method for the equation list that subsequently calls the
	 * {@link #makeDeduction() makeDeduction()} method.
	 * @param eqSet
	 *          the list of equation to set
	 */
	public void setEqSet(Set<Equation> eqSet) {
		// make copy to avoid equation sharing between components
		this.eqSet = eqSet;
		// also trigger the deduction capability
		makeDeduction();
	}

	/**
	 * Method that add a deduction to the deduction capability.
	 * Also the conclusion is added to the list of known equations.
	 * @param deduction
	 *          the deduction to add
	 */
	public void addDeductionCapability(Deduction deduction) {
		if (!deduction.containsMatchVar()) {
			if (!deduction.isReflexive() && !deduction.isTooComplex()) {
				// also add the conclusion to the list of equations
				eqSet.add(deduction.getConclusion());
				// regardless of the result, add the deduction
				deductionCapability.add(deduction);
			}
		}
	}

	/*
	private void makeCounter() {
		// TODO test this!
		for (Variable var : varSet) {
			int count = 0;
			int maxCount = 0;
			for (Action a : actions) {
				switch (a.getAction()) {
				case CHECK:
					for (Equation e : a.getEqSet()) {
						if (e.getAtoms().contains(var)) {
							count++;
						}
					}
					break;
				case COMPUTE:
					if (a.getEq().getAtoms().contains(var)) {
						count++;
					}
					break;
				case DELETE:
					if (a.getVar().equals(var)) {
						count = 0;
					}
					break;
				default:
					break;
				}
				if (count > maxCount) {
					maxCount = count;
				}
			}
			counter.put(var, maxCount);
		}
	}*/

	// Getter and Setter methods
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Action> getActions() {
		return actions;
	}

	public void setActions(Set<Action> actions) {
		this.actions = actions;
	}

	public Set<Variable> getVarSet() {
		return varSet;
	}

	public void setVarSet(Set<Variable> varSet) {
		this.varSet = varSet;
	}

	public Set<Dep> getDepSet() {
		return depSet;
	}

	public void setDepSet(Set<Dep> depSet) {
		this.depSet = depSet;
	}

	public void addDependence(Dep dep) {
		depSet.add(dep);
	}

	public Set<Deduction> getDeducSet() {
		return deducSet;
	}

	public void setDeducSet(Set<Deduction> deducSet) {
		this.deducSet = deducSet;
	}

	public void addDeduction(Deduction deduc) {
		deducSet.add(deduc);
	}

	public Set<Equation> getEqSet() {
		return eqSet;
	}

	public Set<Deduction> getDeductionCapability() {
		return deductionCapability;
	}

	public void setDeductionCapability(Set<Deduction> deductionCapability) {
		this.deductionCapability = deductionCapability;
	}

	public int getCounter(Variable var) {
		return counter.get(var);
	}

	public void setCounter(Variable var, int val) {
		counter.put(var, val);
	}
	
	public Component getAssociate() {
		return composition;
	}
	
	public void setAssociate(Component composition) {
		this.composition = composition;
	}
	
	public String getInstance() {
		return instanceIndex;
	}
	
	public void setInstanced(String instanceIndex) {
		this.instanceIndex = instanceIndex;
	}
}
