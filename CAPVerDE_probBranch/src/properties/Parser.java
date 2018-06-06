package properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import architecture.Action;
import architecture.Architecture;
import architecture.Component;
import architecture.Composition;
import architecture.Equation;
import architecture.Term;
import architecture.Variable;
import gui.Gui;
import gui.Gui.MessageType;
import utils.SuccessIndexPair;

/**
 * The Parser Interface for both types of parsers: bottom up and top down.
 */
public class Parser {

	// class fields
	protected Architecture arch;

	/**
	 * Super constructor for all parsers that already checks the architecture for consistency.
	 * @param arch
	 * 			the architecture
	 */
	public Parser(Architecture arch) {
		this.arch = arch;
		SuccessIndexPair result = verifyArchitecture();
		if (!result.isSuccess()) {
			// error?
			System.err.println("Warning: architecture not consistent!");
			System.err.println("At least this action is not valid: "
					+ new ArrayList<Action>(arch.getAllActions()).get(result.getIndex()));
			System.err.println("All verification based on this architecture are not meaningful...");
			Gui.showMessage(MessageType.ERR,
					"Architecture is not consistent!\n" + "At least this action is not valid: "
							+ new ArrayList<Action>(arch.getAllActions()).get(result.getIndex()) + "\n"
							+ "All verification based on this architecture are not meaningful...");
		} else {
			// architecture is consistent
			System.out.println(
					"Architecture is consistent and can be used for verification of privacy properties");
			Gui.showMessage(MessageType.INF,
					"Architecture is consistent and can be used for verification of privacy properties");
		}
	}

	/**
	 * Generic parser method to be overwritten by the implementing classes.
	 * @param statement
	 *          the property to verify
	 * @param recurseDepth
	 *          an integer indicating the depth of recursion for tracing purposes
	 * @return success
	 */
	public boolean verifyStatement(Property statement, int recurseDepth) {
		//TODO what to do here?
		System.out.println("This message should not be visible, because this method is to be overwritten by inherited classed...");
		return false;
	}

	/**
	 * Parser method to verify an architecture for consistency.
	 * @param arch
	 * 			the architecture to verify (mainly check for consistency)
	 * @return
	 * 			success or else the index of the problematic action
	 */
	private SuccessIndexPair verifyArchitecture() {
		// check if there is an order for the actions that is consistent
		List<Action> orderedActions = new ArrayList<Action>(arch.getAllActions());
		SuccessIndexPair result = isConsistent(orderedActions);
		if (!result.isSuccess()) {
			// try the default order:
			// has, compute, receive, check, verify, delete
			sortActions(orderedActions);
		} else {
			return result;
		}
		result = isConsistent(orderedActions);
		int counter = 0;
		int bound = orderedActions.size() * (int) (Math.floor(Math.log(orderedActions.size())));
		while (!result.isSuccess()) {
			if (counter > bound) {
				// break off if too many steps were taken
				return result;
			}
			// place the failing action on the last spot
			Action action = orderedActions.get(result.getIndex());
			orderedActions.remove(result.getIndex());
			orderedActions.add(action);
			// increment counter
			counter++;
			// update the result
			result = isConsistent(orderedActions);
		}
		return result;
	}

	/**
	 * Helper method to check if the actions performed in the given order are
	 * consistent, i.e. no variable is used before receiving/computing it.
	 * 
	 * @param actions
	 *          the ordered list of actions
	 * @return true, if the order is consistent
	 */
	private SuccessIndexPair isConsistent(List<Action> actions) {
		// keep track of the owned variables/DBs for each component
		boolean[][] variablesOwned = new boolean[arch.getCompList().size()][arch.getVariables().size()];
		// go through list of actions and add owned variables and check used ones
		for (Action action : actions) {
			int actionIndex = actions.indexOf(action);
			int compIndex = new ArrayList<Component>(arch.getCompList()).indexOf(action.getComponent());
			switch (action.getAction()) {
			case CHECK:
				for (Equation eq : action.getEqSet()) {
					for (Term term : eq.getTermSet()) {
						for (Variable var : term.getAtomSet()) {
							if (new ArrayList<Variable>(arch.getVariables()).indexOf(var) < 0
									|| !variablesOwned[compIndex][new ArrayList<Variable>(arch.getVariables()).indexOf(var)]) {
								// one of the used variables is not yet possessed by the
								// component
								return new SuccessIndexPair(false, actionIndex);
							}
						}
					}
				}
				break;
			case COMPUTE:
				for (Variable var : action.getEq().getOp2().getAtomSet()) {
					if (arch.getVariables().indexOf(var) < 0
							|| !variablesOwned[compIndex][arch.getVariables().indexOf(var)]) {
						// one of the used variables is not yet possessed by the component
						return new SuccessIndexPair(false, actionIndex);
					}
				}
				// the variable is now owned
				variablesOwned[compIndex][arch.getVariables().indexOf(action.getEq().getLefthandSide())] = true;
				// also own variables from composition
				for (Composition compos : arch.getCompositions()) {
					if (compos.getContainer().equals(action.getComponent())) {
						// the composed component also has access to the variable
						variablesOwned[arch.getCompList().indexOf(compos.getComponent())][arch.getVariables().indexOf(action.getEq().getLefthandSide())] = true;
					}
				}
				break;
			case DELETE:
				// variable
				if (arch.getVariables().indexOf(action.getVar()) < 0
						|| !variablesOwned[compIndex][arch.getVariables().indexOf(action.getVar())]) {
					// the used variable is not yet possessed by the component
					return new SuccessIndexPair(false, actionIndex);
				}
				break;
			case HAS:
				// the variable is now owned
				variablesOwned[compIndex][arch.getVariables().indexOf(action.getVar())] = true;
				// also own variables from composition
				for (Composition compos : arch.getCompositions()) {
					if (compos.getContainer().equals(action.getComponent())) {
						// the composed component also has access to the variable
						variablesOwned[arch.getCompList().indexOf(compos.getComponent())][arch.getVariables().indexOf(action.getVar())] = true;
					}
				}
				break;
			case RECEIVE:
				// variables
				for (Variable var : action.getVarSet()) {
					if (arch.getVariables().indexOf(var) < 0
							|| !variablesOwned[arch.getCompList().indexOf(
									action.getComPartner())][arch.getVariables()
									                         .indexOf(var)]) {
						// one of the used variables is not yet possessed by the sending
						// component
						return new SuccessIndexPair(false, actionIndex);
					}
					// the variable is now owned
					variablesOwned[compIndex][arch.getVariables().indexOf(var)] = true;
					// also own variables from composition
					for (Composition compos : arch.getCompositions()) {
						if (compos.getContainer().equals(action.getComponent())) {
							// the composed component also has access to the variable
							variablesOwned[arch.getCompList().indexOf(compos.getComponent())][arch.getVariables().indexOf(var)] = true;
						}
					}
				}
				break;
			case TRUST:
				break;
			case VERIF_A:
				break;
			case VERIF_P:
				break;
			default:
				break;
			}
		}

		return new SuccessIndexPair(true, -1);
	}

	/**
	 * Helper method to sort a list of actions by its enum.
	 * 
	 * @param actions
	 *          the list of actions
	 */
	private void sortActions(List<Action> actions) {
		// sort by the enum ACTION (has, compute, receive, check, spotcheck, verify,
		// delete, trust
		Collections.sort(actions, new Comparator<Action>() {
			@Override
			public int compare(Action a1, Action a2) {
				return a1.getAction().compareTo(a2.getAction());
			}
		});
	}

}
