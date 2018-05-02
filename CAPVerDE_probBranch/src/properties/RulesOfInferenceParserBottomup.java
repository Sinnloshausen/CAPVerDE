package properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import architecture.Action;
import architecture.Architecture;
import architecture.Attest;
import architecture.Component;
import architecture.Deduction;
import architecture.Dep;
import architecture.Equation;
import architecture.P;
import architecture.Term;
import architecture.Variable;
import gui.Gui;
import gui.Gui.MessageType;
import properties.Property.PropertyType;
import architecture.Action.ActionType;
import utils.SuccessIndexPair;
import utils.TraceBuffer;
import utils.TraceBuffer.LogType;

/**
 * Parser that implements rules of inference to gather all Has, K and B
 * properties from an Architecture_Class object.
 */
public class RulesOfInferenceParserBottomup implements Parser, Serializable {

	/**
	 * @serial Serial ID for storing architecture objects in files.
	 */
	private static final long serialVersionUID = -2920437147883611149L;

	// class fields
	private Architecture arch;
	private Map<Property, Boolean> resultHistory;
	private List<Property> callHistory;

	/**
	 * The constructor for this class. This already verifies the architecture for consistency.
	 * 
	 * @param arch
	 *          the architecture to parse
	 */
	public RulesOfInferenceParserBottomup(Architecture arch) {
		this.arch = arch;
		resultHistory = new HashMap<Property, Boolean>();
		callHistory = new ArrayList<Property>();
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
	 * Helper method that verifies if the architecture is consistent in itself.
	 * 
	 * @return true, if the architecture is consistent
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
				variablesOwned[compIndex][arch.getVariables().indexOf(
						action.getEq().getLefthandSide())] = true;
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

	/**
	 * Method that verifies if the given statement is consistent with the
	 * architecture.
	 * 
	 * @param statement
	 *          the statement to verify
	 * @param recurseDepth
	 *          the depth of the recursion
	 * @return true, if the statement is satisfiable with the architecture
	 */
	@Override
	public boolean verifyStatement(Property statement, int recurseDepth) {
		String spacing = String.join("", Collections.nCopies(recurseDepth, "  "));
		System.out.println(spacing + "Current property to prove: " + statement);
		TraceBuffer.logMessage(
				statement, "Current property to prove: " + statement, recurseDepth, LogType.START);
		// recursion optimization: do not check the same statement twice
		Boolean result = resultHistory.put(statement, null);
		if (result != null) {
			// return the cached value
			resultHistory.put(statement, result);
			String msg = "Current statement already checked: ";
			msg += result ? "successfully verified" : "not successfully verified";
			System.out.println(spacing + msg);
			TraceBuffer.logMessage(statement, msg, recurseDepth, LogType.END);
			return result;
		}
		if (!callHistory.contains(statement)) {
			callHistory.add(statement);
		} else { // break condition if in endless loop via substitution/transitivity
			// this statement was already input but did not properly terminate (yet)
			// thus it should not be evaluated again
			System.out.println(spacing + "Stopping recursive endless loop");
			TraceBuffer.logMessage(
					statement, "Stopping recursive endless loop", recurseDepth, LogType.END);
			return false;
		}
		// do the actual work and apply the rules of inference
		switch (statement.getType()) {
		case CONJUNCTION:
			// Rule I^
			System.out.println(spacing + "Rule I^ applied for statement: " + statement);
			System.out.println(spacing + "Therefore trying to verify new statements:");
			TraceBuffer.logMessage(
					statement, "Rule I^ applied for statement: ", recurseDepth, LogType.INFO);
			TraceBuffer.logMessage(
					statement, "Therefore trying to verify new statements:", recurseDepth, LogType.INFO);
			if (verifyStatement(statement.getSt1(), recurseDepth + 1)
					&& verifyStatement(statement.getSt2(), recurseDepth + 1)) {
				resultHistory.put(statement, true);
				System.out.println(spacing + "Rule I^ applied for statement: " + statement);
				TraceBuffer.logMessage(statement, "Rule I^ applied for statement: "
						+ statement, recurseDepth, LogType.END);
				return true;
			} else {
				resultHistory.put(statement, false);
				System.out.println(spacing + "Rule I^ not applied for statement: " + statement);
				TraceBuffer.logMessage(statement, "Rule I^ not applied for statement: "
						+ statement, recurseDepth, LogType.END);
				return false;
			}
		case NEGATION:
			// Rule I_neg
			System.out.println(spacing + "Rule I_neg applied for statement: " + statement);
			System.out.println(spacing + "Therefore trying to verify new statement:");
			TraceBuffer.logMessage(
					statement, "Rule I_neg applied for statement: ", recurseDepth, LogType.INFO);
			TraceBuffer.logMessage(
					statement, "Therefore trying to verify new statement:", recurseDepth, LogType.INFO);
			if (!verifyStatement(statement.getSt1(), recurseDepth + 1)) {
				resultHistory.put(statement, true);
				System.out.println(spacing + "Rule I_neg applied for statement: " + statement);
				TraceBuffer.logMessage(statement, "Rule I_neg  applied for statement: "
						+ statement, recurseDepth, LogType.END);
				return true;
			} else {
				resultHistory.put(statement, false);
				System.out.println(spacing + "Rule I_neg not applied for statement: " + statement);
				TraceBuffer.logMessage(statement, "Rule I_neg not applied for statement: "
						+ statement, recurseDepth, LogType.END);
				return false;
			}
		case HAS:
			// Rule H1
			System.out.println(spacing + "Trying Rule H1...");
			TraceBuffer.logMessage(statement, "Trying Rule H1...", recurseDepth, LogType.INFO);
			if (arch.getAllActions().contains(new Action(
					ActionType.HAS, statement.getOwner(), statement.getVar()))) {
				resultHistory.put(statement, true);
				System.out.println(spacing + "Rule H1 applied for statement: " + statement);
				TraceBuffer.logMessage(statement, "Rule H1 applied for statement: "
						+ statement, recurseDepth, LogType.END);
				return true;
			} else {
				System.out.println(spacing + "Rule H1 not applicable");
				TraceBuffer.logMessage(
						statement, "Rule H1 not applicable", recurseDepth, LogType.INFO);
			}
			// Rule H2
			System.out.println(spacing + "Trying Rule H2...");
			TraceBuffer.logMessage(statement, "Trying Rule H2...", recurseDepth, LogType.INFO);
			if (isContainedReceive(statement.getOwner(), statement.getVar())) {
				resultHistory.put(statement, true);
				System.out.println(spacing + "Rule H2 applied for statement: " + statement);
				TraceBuffer.logMessage(statement, "Rule H2 applied for statement: "
						+ statement, recurseDepth, LogType.END);
				return true;
			} else {
				System.out.println(spacing + "Rule H2 not applicable");
				TraceBuffer.logMessage(
						statement, "Rule H2 not applicable", recurseDepth, LogType.INFO);
			}
			// Rule H3
			System.out.println(spacing + "Trying Rule H3...");
			TraceBuffer.logMessage(statement, "Trying Rule H3...", recurseDepth, LogType.INFO);
			if (isContainedCompute(statement.getOwner(), statement.getVar())) {
				resultHistory.put(statement, true);
				System.out.println(spacing + "Rule H3 applied for statement: " + statement);
				TraceBuffer.logMessage(statement, "Rule H3 applied for statement: " 
						+ statement, recurseDepth, LogType.END);
				return true;
			} else {
				System.out.println(spacing + "Rule H3 not applicable");
				TraceBuffer.logMessage(
						statement, "Rule H3 not applicable", recurseDepth, LogType.INFO);
			}
			// Rule H4
			//TODO
			System.out.println(spacing + "Trying Rule H4...");
			TraceBuffer.logMessage(statement, "Trying Rule H4...", recurseDepth, LogType.INFO);
			if (isContainedDep(statement.getOwner(), statement.getVar(), statement.getProb(), recurseDepth)) {
				resultHistory.put(statement, true);
				System.out.println(spacing + "Rule H4 applied for statement: " + statement);
				TraceBuffer.logMessage(statement, "Rule H4 applied for statement: "
						+ statement, recurseDepth, LogType.END);
				return true;
			} else {
				System.out.println(spacing + "Rule H4 not applicable");
				TraceBuffer.logMessage(
						statement, "Rule H4 not applicable", recurseDepth, LogType.INFO);
			}
			break;
		case KNOWS:
			// Rule K1
			System.out.println(spacing + "Trying Rule K1...");
			TraceBuffer.logMessage(statement, "Trying Rule K1...", recurseDepth, LogType.INFO);
			if (arch.getAllActions().contains(new Action(
					ActionType.COMPUTE, statement.getOwner(), statement.getEq()))) {
				resultHistory.put(statement, true);
				System.out.println(spacing + "Rule K1 applied for statement: " + statement);
				TraceBuffer.logMessage(statement, "Rule K1 applied for statement: "
						+ statement, recurseDepth, LogType.END);
				return true;
			} else {
				System.out.println(spacing + "Rule K1 not applicable");
				TraceBuffer.logMessage(statement, "Rule K1 not applicable", recurseDepth, LogType.INFO);
			}
			// Rule K2
			System.out.println(spacing + "Trying Rule K2...");
			TraceBuffer.logMessage(statement, "Trying Rule K2...", recurseDepth, LogType.INFO);
			if (isContainedCheck(statement.getOwner(), statement.getEq())) {
				resultHistory.put(statement, true);
				System.out.println(spacing + "Rule K2 applied for statement: " + statement);
				TraceBuffer.logMessage(statement, "Rule K2 applied for statement: "
						+ statement, recurseDepth, LogType.END);
				return true;
			} else {
				System.out.println(spacing + "Rule K2 not applicable");
				TraceBuffer.logMessage(statement, "Rule K2 not applicable", recurseDepth, LogType.INFO);
			}
			// Rule K3
			System.out.println(spacing + "Trying Rule K3...");
			TraceBuffer.logMessage(statement, "Trying Rule K3...", recurseDepth, LogType.INFO);
			if (isContainedProof(statement.getOwner(), statement.getEq())) {
				resultHistory.put(statement, true);
				System.out.println(spacing + "Rule K3 applied for statement: " + statement);
				TraceBuffer.logMessage(
						statement, "Rule K3 applied for statement: " + statement, recurseDepth, LogType.END);
				return true;
			} else {
				System.out.println(spacing + "Rule K3 not applicable");
				TraceBuffer.logMessage(statement, "Rule K3 not applicable", recurseDepth, LogType.INFO);
			}
			// Rule K4
			System.out.println(spacing + "Trying Rule K4...");
			TraceBuffer.logMessage(statement, "Trying Rule K4...", recurseDepth, LogType.INFO);
			if (isContainedProAtt(statement.getOwner(), statement.getEq())) {
				resultHistory.put(statement, true);
				System.out.println(spacing + "Rule K4 applied for statement: " + statement);
				TraceBuffer.logMessage(
						statement, "Rule K4 applied for statement: " + statement, recurseDepth, LogType.END);
				return true;
			} else {
				System.out.println(spacing + "Rule K4 not applicable");
				TraceBuffer.logMessage(statement, "Rule K4 not applicable", recurseDepth, LogType.INFO);
			}
			// Rule K5
			System.out.println(spacing + "Trying Rule K5...");
			TraceBuffer.logMessage(statement, "Trying Rule K5...", recurseDepth, LogType.INFO);
			if (isContainedAttest(statement.getOwner(), statement.getEq())) {
				resultHistory.put(statement, true);
				System.out.println(spacing + "Rule K5 applied for statement: " + statement);
				TraceBuffer.logMessage(
						statement, "Rule K5 applied for statement: " + statement, recurseDepth, LogType.END);
				return true;
			} else {
				System.out.println(spacing + "Rule K5 not applicable.");
				TraceBuffer.logMessage(statement, "Rule K5 not applicable.", recurseDepth, LogType.INFO);
			}
			// Rule Kded
			//TODO
			System.out.println(spacing + "Trying Rule K deduc...");
			System.out.println(spacing + "Therefore trying to verify new statements:");
			TraceBuffer.logMessage(statement, "Trying Rule K deduc...", recurseDepth, LogType.INFO);
			TraceBuffer.logMessage(
					statement, "Therefore trying to verify new statements:", recurseDepth, LogType.INFO);
			if (isContainedDed(statement.getOwner(), statement.getEq(), statement.getProb(), recurseDepth)) {
				resultHistory.put(statement, true);
				System.out.println(spacing + "Rule K deduc applied for statement: " + statement);
				TraceBuffer.logMessage(statement, "Rule K deduc applied for statement: "
						+ statement, recurseDepth, LogType.END);
				return true;
			} else {
				System.out.println(spacing + "Rule K deduc not applicable");
				TraceBuffer.logMessage(
						statement, "Rule K deduc not applicable", recurseDepth, LogType.INFO);
			}
			break;
		case NOTSHARED:
			// Rule SH1
			System.out.println(spacing + "Trying Rule SH1...");
			TraceBuffer.logMessage(statement, "Trying Rule SH1...", recurseDepth, LogType.INFO);
			if (isContainedCompute(statement.getOwner(), statement.getVar())
					|| isContainedHas(statement.getOwner(), statement.getVar())) {
				resultHistory.put(statement, true);
				System.out.println(spacing + "Rule SH1 applied for statement: " + statement);
				TraceBuffer.logMessage(
						statement, "Rule SH1 applied for statement: " + statement, recurseDepth, LogType.END);
				return true;
			} else {
				System.out.println(spacing + "Rule SH1 not applicable");
				TraceBuffer.logMessage(statement, "Rule SH1 not applicable", recurseDepth, LogType.INFO);
			}
			// Rule SH2
			System.out.println(spacing + "Trying Rule SH2...");
			TraceBuffer.logMessage(statement, "Trying Rule SH2...", recurseDepth, LogType.INFO);
			if (!isContainedReceive2(statement.getOwner(), statement.getVar())) {
				resultHistory.put(statement, true);
				System.out.println(spacing + "Rule SH2 applied for statement: " + statement);
				TraceBuffer.logMessage(
						statement, "Rule SH2 applied for statement: " + statement, recurseDepth, LogType.END);
				return true;
			} else {
				System.out.println(spacing + "Rule SH2 not applicable");
				TraceBuffer.logMessage(statement, "Rule SH2 not applicable", recurseDepth, LogType.INFO);
			}
			break;
		case NOTSTORED:
			// Rule ST1
			System.out.println(spacing + "Trying Rule ST1...");
			TraceBuffer.logMessage(statement, "Trying Rule ST1...", recurseDepth, LogType.INFO);
			if (!isContainedReceive(statement.getOwner(), statement.getVar())) {
				resultHistory.put(statement, true);
				System.out.println(spacing + "Rule ST1 applied for statement: " + statement);
				TraceBuffer.logMessage(
						statement, "Rule ST1 applied for statement: " + statement, recurseDepth, LogType.END);
				return true;
			} else {
				System.out.println(spacing + "Rule ST1 not applicable");
				TraceBuffer.logMessage(statement, "Rule ST1 not applicable", recurseDepth, LogType.INFO);
			}
			// Rule ST2
			System.out.println(spacing + "Trying Rule ST2...");
			TraceBuffer.logMessage(statement, "Trying Rule ST2...", recurseDepth, LogType.INFO);
			if (counter(statement.getOwner(), statement.getVar()) < statement.getBound()) {
				resultHistory.put(statement, true);
				System.out.println(spacing + "Rule ST2 applied for statement: " + statement);
				TraceBuffer.logMessage(
						statement, "Rule ST2 applied for statement: " + statement, recurseDepth, LogType.END);
				return true;
			} else {
				System.out.println(spacing + "Rule ST2 not applicable");
				TraceBuffer.logMessage(statement, "Rule ST2 not applicable", recurseDepth, LogType.INFO);
			}
			//TODO
			break;
		default:
			break;
		}
		// no rule applied
		resultHistory.put(statement, false);
		System.out.println(spacing + "No Rule applicable for statement: " + statement);
		TraceBuffer.logMessage(
				statement, "No Rule applicable for statement: " + statement, recurseDepth, LogType.END);
		return false;
	}
	
	
	/**
	 * Method that returns the maximum number of events that a component accesses a variable before deleting it.
	 * @param owner
	 * 			the component
	 * @param var
	 * 			the variable
	 * @return the number of events before delete
	 */
	private int counter(Component owner, Variable var) {
		// TODO test this!
		int counter = 0;
		int maxCounter = 0;
		for (Action action : arch.getAllActions()) {
			// only count all actions of component with variable
			if (action.getComponent().equals(owner)) {
				switch (action.getAction()) {
				case CHECK:
					// fall through
				case COMPUTE:
					if (action.getEq().getAtoms().contains(var)) {
						// the var is used
						counter++;
						if (counter > maxCounter) {
							maxCounter = counter;
						}
					}
					break;
				case DELETE:
					if (action.getVar().equals(var)) {
						// the var gets deleted
						counter--;
					}
					break;
				default:
					break;
				}
			} else if (action.getComPartner() != null && action.getComPartner().equals(owner)) {
				if (action.getAction() == ActionType.RECEIVE && action.getVarSet().contains(var)) {
					// the component sends the variable to another comp
					counter++;
					if (counter > maxCounter) {
						maxCounter = counter;
					}
				}
			}

		}
		return maxCounter;
	}

	
	/**
	 * Helper method that returns a probability for which the has property holds.
	 * @param comp
	 * 			the component
	 * @param var
	 * 			the variable
	 * @param recurseDepth
	 * 			the depth of the recursion
	 * @return a probability for which the property holds, 0 if it does not
	 */
	private double verifyHasProb(Component comp, Variable var, int recurseDepth) {
		//TODO better approach
		double prob = 1;
		while (prob >= 0.000001) {
			if (verifyStatement(new Property(PropertyType.HAS, comp, prob, var), recurseDepth + 1)) {
				// test with what probability the property applies
				return prob;
			}
			prob = prob / 10;
		}
		return 0;
	}
	
	/**
	 * Helper method that returns a probability for which the knows property holds.
	 * @param comp
	 * 			the component
	 * @param eq
	 * 			the equation
	 * @param recurseDepth
	 * 			the depth of the recursion
	 * @return a probability for which the property holds, 0 if it does not
	 */
	private double verifyKnowsProb(Component comp, Equation eq, int recurseDepth) {
		//TODO better approach
		double prob = 1;
		while (prob >= 0.000001) {
			if (verifyStatement(new Property(PropertyType.KNOWS, comp, prob, eq), recurseDepth + 1)) {
				// test with what probability the property applies
				return prob;
			}
			prob = prob / 10;
		}
		return 0;
	}

	/**
	 * Helper method to check if there is a fitting receive in the action of the
	 * architecture.
	 * 
	 * @param comp
	 *          the receiving component
	 * @param var
	 *          the variable to look for
	 * @return true, if there is a receive that fits
	 */
	private boolean isContainedReceive(Component comp, Variable var) {
		for (Action action : arch.getAllActions()) {
			if (action.getAction() == ActionType.RECEIVE) {
				// check if the right acting component and if the variable is contained
				if (action.getComponent().equals(comp) && action.getVarSet().contains(var)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Helper method to check if there is a fitting receive in the action of the
	 * architecture.
	 * 
	 * @param comp
	 *          the sending component
	 * @param var
	 *          the variable to look for
	 * @return true, if there is a receive that fits
	 */
	private boolean isContainedReceive2(Component comp, Variable var) {
		for (Action action : arch.getAllActions()) {
			if (action.getAction() == ActionType.RECEIVE) {
				// check if the right acting component and of the variable is contained
				if (action.getComPartner().equals(comp) && action.getVarSet().contains(var)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Helper method to check if there is a fitting compute in the action of the
	 * architecture.
	 * 
	 * @param comp
	 *          the acting component
	 * @param var
	 *          the variable to look for
	 * @return true, if there is a compute that fits
	 */
	private boolean isContainedCompute(Component comp, Variable var) {
		for (Action action : arch.getAllActions()) {
			if (action.getAction() == ActionType.COMPUTE) {
				// check if the right acting component and of the variable is contained
				if (action.getComponent().equals(comp) && action.getEq().getLefthandSide().equals(var)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Helper method to check if there is a fitting has in the action of the
	 * architecture.
	 * 
	 * @param comp
	 *          the acting component
	 * @param var
	 *          the variable to look for
	 * @return true, if there is a has that fits
	 */
	private boolean isContainedHas(Component comp, Variable var) {
		for (Action action : arch.getAllActions()) {
			if (action.getAction() == ActionType.HAS) {
				// check if the right acting component and of the variable is contained
				if (action.getComponent().equals(comp) && action.getVar().equals(var)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Helper method to check if there is a fitting dep for the component.
	 * 
	 * @param comp
	 *          the acting component
	 * @param var
	 *          the variable to look for
	 * @param prob
	 *          the probability of the dep
	 * @param recurseDepth
	 *          the depth of recursion
	 * @return true, if there is a dep that fits
	 */
	private boolean isContainedDep(Component comp, Variable var, double prob, int recurseDepth) {
		// consider probabilities
		for (Dep dep : comp.getDepSet()) {
			if (dep.getVar().equals(var)) {
				// check if all required variables are possessed
				double allProbs = dep.getProb();
				for (Variable mustHave : dep.getVarSet()) {
					// multiply along the path
					allProbs *= verifyHasProb(comp, mustHave, recurseDepth + 1);
				}
				if (allProbs > prob) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Helper method to check if there is a fitting deduction for the component.
	 * 
	 * @param comp
	 *          the acting component
	 * @param eq
	 *          the equation to look for
	 * @param prob
	 *          the probability of the ded
	 * @param recurseDepth
	 *          the depth of recursion
	 * @return true, if there is a dep that fits
	 */
	private boolean isContainedDed(Component comp, Equation eq, double prob, int recurseDepth) {
		// consider probabilities
		for (Deduction ded : comp.getDeductionCapability()) {
			if (ded.getConclusion().equals(eq)) {
				// check if all required variables are possessed
				double allProbs = ded.getProb();
				for (Equation mustHave : ded.getPremises()) {
					// multiply along the path
					allProbs *= verifyKnowsProb(comp, mustHave, recurseDepth + 1);
				}
				if (allProbs > prob) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Helper method to check if there is a fitting check in the action of the
	 * architecture.
	 * 
	 * @param comp
	 *          the acting component
	 * @param eq
	 *          the equation to look for
	 * @return true, if there is a check that fits
	 */
	private boolean isContainedCheck(Component comp, Equation eq) {
		for (Action action : arch.getAllActions()) {
			if (action.getAction() == ActionType.CHECK) {
				// check if the right acting component and if the equation is contained
				if (action.getComponent().equals(comp) && action.getEqSet().contains(eq)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Helper method to check if there is a fitting verifP in the action of the
	 * architecture.
	 * 
	 * @param comp
	 *          the acting component
	 * @param eq
	 *          the equation to look for
	 * @return true, if there is a verifP that fits
	 */
	private boolean isContainedProof(Component comp, Equation eq) {
		for (Action action : arch.getAllActions()) {
			if (action.getAction() == ActionType.VERIF_P) {
				// check if the right acting component and if the equation is contained
				if (action.getComponent().equals(comp) && action.getPro().getpSet().contains(eq)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Helper method to check if there is a fitting verifP with an attest in the
	 * action of the architecture.
	 * 
	 * @param comp
	 *          the acting component
	 * @param eq
	 *          the equation to look for
	 * @return true, if there is a verifP containing a valid attest that fits
	 */
	private boolean isContainedProAtt(Component comp, Equation eq) {
		// different approach
		for (Action action : arch.getAllActions()) {
			if (action.getAction() == ActionType.VERIF_P) {
				// check if there is an attest in the proof that fits
				for (P p : action.getPro().getpSet()) {
					if (p instanceof Attest) {
						// only if the verifying component trusts the attesting one
						if (arch.trust(action.getComponent(), ((Attest) p).getComponent())) {
							// check of the equation is in the attest
							if (((Attest) p).getEqSet().contains(eq)) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Helper method to check if there is a fitting verifA in the actions of the
	 * architecture.
	 * 
	 * @param comp
	 *          the acting component
	 * @param eq
	 *          the equation to look for
	 * @return true, if there is a verifA containing a valid attest that fits
	 */
	private boolean isContainedAttest(Component comp, Equation eq) {
		for (Action action : arch.getAllActions()) {
			if (action.getAction() == ActionType.VERIF_A) {
				// check if the attesting component is "trustworthy"
				if (arch.trust(action.getComponent(), action.getAtt().getComponent())) {
					// check if the equation is contained
					if (action.getAtt().getEqSet().contains(eq)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// Setter and getter methods
	public Architecture getArch() {
		return arch;
	}

	public void setArch(Architecture arch) {
		this.arch = arch;
	}
}
