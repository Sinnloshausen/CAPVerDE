package gui;

import architecture.Action;
import architecture.Action.ActionType;
import architecture.Architecture;
import architecture.Attest;
import architecture.Component;
import architecture.Deduction;
import architecture.DeductionCapability;
import architecture.Dep;
import architecture.DependenceRelation;
import architecture.Equation;
import architecture.Equation.Relation;
import architecture.Equation.Type;
import architecture.P;
import architecture.Proof;
import architecture.Statement;
import architecture.Term;
import architecture.Term.Operator;
import architecture.Term.OperatorType;
import architecture.Term.TermType;
import architecture.Trust;
import architecture.Variable;
import gui.Gui.MessageType;
import properties.Property;
import properties.RulesOfInferenceParserBottomup;
import properties.RulesOfInferenceParserTopdown;
import utils.SaveLoadArch;
import utils.TraceBuffer;
import utils.ArchLoader;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The class that is responsible for all architecture-related methods triggered by the GUI.
 */
public class ArchitectureFunctions implements Serializable {

	/**
	 * @serial Serial ID for storing architecture objects in files.
	 */
	private static final long serialVersionUID = 9132740232003863055L;

	/**
	 * The three different case studies.
	 * Electronic Toll Pricing, Smart Energy Metering,
	 * and Document Submission and Grading
	 */
	public static enum CaseStudy {
		SEM;

		/**
		 * Method to get the enum objects from a string.
		 * @param value
		 *          the name
		 * @return the corresponding enum object
		 */
		public static CaseStudy getEnum(String value) {
			if (value.equals("Smart Energy Metering")) {
				return SEM;
			} else {
				throw new IllegalArgumentException();
			}
		}

		@Override
		public String toString() {
			switch (this) {
			case SEM:
				return "Smart Energy Metering";
			default:
				return "";
			}
		}
	}

	// class fields
	private Set<Component> cSet;
	private Set<Variable> vSet;
	private Set<Term> tSet;
	private Set<Equation> eSet;
	private Set<Trust> trustSet;
	private Set<Action> aSet;
	private Set<Statement> stSet;
	private Set<DependenceRelation> dSet;
	private Set<DeductionCapability> dedSet;
	private Set<Deduction> deducs;
	private Architecture arch;
	private RulesOfInferenceParserTopdown parserTd;
	private RulesOfInferenceParserBottomup parserBu;
	private Set<Property> pSet;

	/**
	 * The constructor of the architecture functions.
	 */
	public ArchitectureFunctions() {
		cSet = new LinkedHashSet<Component>();
		vSet = new LinkedHashSet<Variable>();
		tSet = new LinkedHashSet<Term>();
		eSet = new LinkedHashSet<Equation>();
		trustSet = new LinkedHashSet<Trust>();
		aSet = new LinkedHashSet<Action>();
		stSet = new LinkedHashSet<Statement>();
		dSet = new LinkedHashSet<DependenceRelation>();
		dedSet = new LinkedHashSet<DeductionCapability>();
		deducs = new LinkedHashSet<Deduction>();
		pSet = new LinkedHashSet<Property>();
		createDefaultDeduc();
	}

	/**
	 * Method to verify a property.
	 * 
	 * @param prop
	 *          the name of the property to verify
	 * @return true, if the property holds
	 */
	public boolean verify(String prop) {
		// go through the Set and find the right property
		Property property = null;
		for (Property p : pSet) {
			if (prop != null && p.toString().equals(prop)) {
				property = p;
			}
		}
		// verify the property
		if (property != null) {
			if (!parserBu.verifyStatement(property, 0)) {
				return parserTd.verifyStatement(property, 0);
			} else {
				return true;
			}
		}
		// TODO
		return false;
	}

	/**
	 * Method to finish the architecture creation. This will trigger the
	 * consistency check.
	 */
	public void finish() {
		// set dependence relations
		for (DependenceRelation dr : dSet) {
			// add the dep to the comp
			dr.getComp().addDependence(dr.getDep());
		}
		// add actions
		Set<Action> interComponentActions = new LinkedHashSet<Action>();
		for (Action ac : aSet) {
			switch (ac.getAction()) {
			case CHECK:
				// fall through
			case COMPUTE:
				// fall through
			case DELETE:
				// fall through
			case HAS:
				// fall through
			case TRUST:
				// fall through
			case VERIF_A:
				// fall through
			case VERIF_P:
				// add the action to the comp
				ac.getComponent().addAction(ac);
				break;
			case RECEIVE:
				interComponentActions.add(ac);
				break;
			default:
				break;
			}
		}
		// add deduction capabilities
		for (DeductionCapability ded : dedSet) {
			// add the deds to the comp
			ded.getComp().setDeducSet(ded.getDeducSet());
		}
		// create arch
		arch = new Architecture(cSet, interComponentActions, trustSet);
		// create the verifier
		parserTd = new RulesOfInferenceParserTopdown(arch);
		parserBu = new RulesOfInferenceParserBottomup(arch);
	}

	/**
	 * Method that presents the verification trace of a selected property.
	 * 
	 * @param property
	 *          the name of the property
	 */
	public void showTrace(String property) {
		// split the string to remove the [holds] or [does not hold] information
		String[] splits = property.split("] ");
		property = splits[1];
		String holdP = System.lineSeparator();
		// check if the verification was successful
		if (splits[0].equals("[holds")) {
			// add success to trace
			holdP += "Property successfully proven!";
		} else {
			holdP += "Property could not be proven!";
		}
		// get the right property
		for (Property p : pSet) {
			if (property != null && p.toString().equals(property)) {
				// TODO do something with the property p
				// get the trace
				// DEBUG
				System.out.println("Property to show trace for: " + p);
				// This should work
				Gui.showMessage(MessageType.LOG, TraceBuffer.getMessage(p) + holdP);
			}
		}
	}

	/**
	 * Method to load one of the hard-coded case studies.
	 * 
	 * @param example
	 *          the name of the case study
	 */
	public void load(String example) {
		// load the right case study
		try {
			ArchLoader.load(this, CaseStudy.getEnum(example));
		} catch (IllegalArgumentException e) {
			// no right example was selected
		}
	}

	/**
	 * Method to save a created architecture to disk.
	 * 
	 * @param name
	 *          the name of the file / architecture
	 */
	public void save2file(String name) {
		// TODO test this
		SaveLoadArch.saveArch(this, name);
	}

	/**
	 * Helper method to create the default deductions Reflexivity, Symmetry,
	 * Transitivity and Substitution.
	 */
	private void createDefaultDeduc() {
		// some variables and terms for the deduction equations
		Variable varT = new Variable("t");
		Term termT = new Term(TermType.ATOM, varT, true);
		Variable varU = new Variable("u");
		Term termU = new Term(TermType.ATOM, varU, true);
		Variable varV = new Variable("v");
		Term termV = new Term(TermType.ATOM, varV, true);
		Variable varX = new Variable("x");
		Variable varY = new Variable("y");
		Term termX = new Term(TermType.ATOM, varX, true);
		Term termY = new Term(TermType.ATOM, varY, true);
		Equation dedEq2 = new Equation("subst", Type.RELATION, Relation.EQUALITY, termT, termU);
		Equation dedEq3 = new Equation("dedEq3", Type.RELATION, Relation.EQUALITY, termX, termY);
		Set<Equation> dedEqSet1 = Set.of(dedEq2,
				new Equation("dedEq3", Type.RELATION, Relation.EQUALITY, termU, termV));
		Set<Equation> dedEqSet2 = Set.of(dedEq2, dedEq3);
		// the four default deductions
		Deduction deduc1 = new Deduction(Deduction.Type.ELSE, Collections.emptySet(),
				new Equation("dedEq1", Type.RELATION, Relation.EQUALITY, termT, termT), "Reflexivity", 1);
		Deduction deduc2 = new Deduction(Deduction.Type.ELSE, Collections.singleton(dedEq2),
				new Equation("dedEq3", Type.RELATION, Relation.EQUALITY, termU, termT), "Symmetry", 1);
		Deduction deduc3 = new Deduction(Deduction.Type.TRANS, new LinkedHashSet<Equation>(dedEqSet1),
				new Equation("dedEq4", Type.RELATION, Relation.EQUALITY, termT, termV), "Transitivity", 1);
		Deduction deduc4 = new Deduction(Deduction.Type.SUBST, new LinkedHashSet<Equation>(dedEqSet2), dedEq2, "Substitution", 1);
		// add the deductions to the Set
		deducs.add(deduc1);
		deducs.add(deduc2);
		deducs.add(deduc3);
		deducs.add(deduc4);
	}

	/**
	 * Method that adds a component to the architecture's Set.
	 * @param name
	 *          the name of the component
	 */
	public void addComponent(String name) {
		cSet.add(new Component(name));
		// Debug
		System.out.println(cSet);
	}

	/**
	 * Method that adds a variable to the architecture's Set.
	 * @param name
	 *          the name of the variable
	 */
	public void addVariable(String name) {
		Variable var = new Variable(name, false);
		vSet.add(var);
		tSet.add(new Term(TermType.ATOM, var, false));
		// Debug
		System.out.println(vSet);
	}

	/**
	 * Method that adds a term to the architecture's Set.
	 * @param opType
	 *          the operator type
	 * @param op
	 *          the explicit operator
	 * @param funcName
	 *          the name of the function
	 * @param t1
	 *          the name of the first term
	 * @param t2
	 *          the name of the second term
	 * @param t3
	 *          the name of the third term
	 */
	public void addTerm(OperatorType opType, Operator op, String funcName,
			String t1, String t2, String t3) {
		Term term1 = null;
		Term term2 = null;
		Term term3 = null;
		// go through Set and identify the right term(s)
		for (Term t : tSet) {
			if (t.toString().equals(t1)) {
				term1 = t;
			} else if (t2 != null && t.toString().equals(t2)) {
				term2 = t;
			} else if (t3 != null && t.toString().equals(t3)) {
				term3 = t;
			}
		}
		switch (opType) {
		case BINARY:
			// add the new binary term
			if (term1 != null && term2 != null) {
				tSet.add(new Term(TermType.COMPOSITION, opType, op, funcName, term1, term2, false));
			}
			break;
		case UNARY:
			// add the new unary term
			if (term1 != null) {
				tSet.add(new Term(TermType.COMPOSITION, opType, op, funcName, term1, false));
			}
			break;
		case TERTIARY:
			// add the new tertiary term
			if (term1 != null && term2 != null && term3 != null) {
				tSet.add(new Term(TermType.COMPOSITION, opType, op,
						funcName, term1, term2, term3, false));
			}
			break;
		default:
			break;
		}
		// Debug
		System.out.println(tSet);
	}

	/**
	 * Method that adds an equation to the architecture's Set.
	 * @param name
	 *          the name of the equation
	 * @param type
	 *          the type of the equation
	 * @param e1
	 *          the name of the first equation
	 * @param e2
	 *          the name of the second equation
	 * @param t1
	 *          the name of the first term
	 * @param t2
	 *          the name of the second term
	 */
	public void addEquation(String name, Type type, String e1, String e2, String t1, String t2) {
		Equation eq1 = null;
		Equation eq2 = null;
		Term term1 = null;
		Term term2 = null;
		// go through Sets and identify the right objects
		for (Term t : tSet) {
			if (t1 != null && t.toString().equals(t1)) {
				term1 = t;
			} else if (t2 != null && t.toString().equals(t2)) {
				term2 = t;
			}
		}
		for (Equation e : eSet) {
			if (e1 != null && e.toString().equals(e1)) {
				eq1 = e;
			} else if (e2 != null && e.toString().equals(e2)) {
				eq2 = e;
			}
		}
		switch (type) {
		case CONJUNCTION:
			if (eq1 != null && eq2 != null) {
				eSet.add(new Equation(name, type, eq1, eq2));
			}
			break;
		case RELATION:
			if (term1 != null && term2 != null) {
				eSet.add(new Equation(name, type, Relation.EQUALITY, term1, term2));
			}
			break;
		default:
			break;
		}
		// Debug
		System.out.println(eSet);
	}

	/**
	 * Method that adds a trust relation to the architecture's Set.
	 * @param c1
	 *          the name of the trusting component
	 * @param c2
	 *          the name of the trusted component
	 */
	public void addTrust(String c1, String c2) {
		Component comp1 = null;
		Component comp2 = null;
		// go through Set and identify the right objects
		for (Component c : cSet) {
			if (c1 != null && c.toString().equals(c1)) {
				comp1 = c;
			} else if (c2 != null && c.toString().equals(c2)) {
				comp2 = c;
			}
		}
		if (comp1 != null && comp2 != null) {
			trustSet.add(new Trust(comp1, comp2));
		}
		// Debug
		System.out.println(trustSet);
	}

	/**
	 * Method that adds a 'has' event to the architecture's Set.
	 * @param comp
	 *          the name of the component
	 * @param var
	 *          the name of the obtained variable
	 */
	public void addHas(String comp, String var) {
		Component component = null;
		Variable variable = null;
		// go through the Sets and identify the right objects
		for (Component c : cSet) {
			if (comp != null && c.toString().equals(comp)) {
				component = c;
			}
		}
		for (Variable v : vSet) {
			if (var != null && v.toString().equals(var)) {
				variable = v;
			}
		}
		if (component != null && variable != null) {
			aSet.add(new Action(ActionType.HAS, component, variable));
		}
		// Debug
		System.out.println(aSet);
	}

	/**
	 * Method that adds a 'compute' event to the architecture's Set.
	 * @param comp
	 *          the name of the component
	 * @param eq
	 *          the name of the obtained equation
	 */
	public void addCompute(String comp, String eq) {
		Component component = null;
		Equation equation = null;
		// go through the Sets and identify the right objects
		for (Component c : cSet) {
			if (comp != null && c.toString().equals(comp)) {
				component = c;
			}
		}
		for (Equation e : eSet) {
			if (eq != null && e.toString().equals(eq)) {
				equation = e;
			}
		}
		if (component != null && equation != null) {
			aSet.add(new Action(ActionType.COMPUTE, component, equation));
		}
		// Debug
		System.out.println(aSet);
	}

	/**
	 * Method that adds a 'receive' event to the architecture's Set.
	 * @param comp1
	 *          the name of the receiving component
	 * @param comp2
	 *          the name of the sending component
	 * @param stateSet
	 *          a Set of the names of the statements to send
	 * @param varSet
	 *          a Set of the names of the variables to send
	 */
	public void addReceive(String comp1, String comp2, Set<String> stateSet, Set<String> varSet) {
		Component component1 = null;
		Component component2 = null;
		Set<Statement> statementSet = new LinkedHashSet<Statement>();
		Set<Variable> variableSet = new LinkedHashSet<Variable>();
		// go through the Sets and identify the right objects
		for (Component c : cSet) {
			if (comp1 != null && c.toString().equals(comp1)) {
				component1 = c;
			} else if (comp2 != null && c.toString().equals(comp2)) {
				component2 = c;
			}
		}
		for (String s : varSet) {
			for (Variable v : vSet) {
				if (v.toString().equals(s)) {
					variableSet.add(v);
					break;
				}
			}
		}
		for (String s : stateSet) {
			for (Statement st : stSet) {
				if (st.toString().equals(s)) {
					statementSet.add(st);
					break;
				}
			}
		}
		if (component1 != null && component2 != null && !(statementSet.isEmpty()
				&& variableSet.isEmpty())) {
			aSet.add(
					new Action(ActionType.RECEIVE, component1, component2, statementSet, variableSet));
		}
		// Debug
		System.out.println(aSet);
	}

	/**
	 * Method that adds a 'check' event to the architecture's Set.
	 * @param comp
	 *          the name of the component
	 * @param eqSet
	 *          a Set of the names of equations to check
	 */
	public void addCheck(String comp, Set<String> eqSet) {
		Component component = null;
		Set<Equation> equationSet = new LinkedHashSet<Equation>();
		// go through the Sets and identify the right objects
		for (Component c : cSet) {
			if (comp != null && c.toString().equals(comp)) {
				component = c;
				break;
			}
		}
		for (String e : eqSet) {
			for (Equation eq : eSet) {
				if (eq.toString().equals(e)) {
					equationSet.add(eq);
					break;
				}
			}
		}
		if (component != null && !equationSet.isEmpty()) {
			aSet.add(new Action(ActionType.CHECK, component, equationSet));
		}
		// Debug
		System.out.println(aSet);
	}

	/**
	 * Method that adds a 'delete' event to the architecture's Set.
	 * @param comp
	 *          the deleting component
	 * @param var
	 *          the variable to delete
	 */
	public void delete(String comp, String var) {
		Component component = null;
		Variable variable = null;
		// go through the Sets and identify the right objects
		for (Component c : cSet) {
			if (comp != null && c.toString().equals(comp)) {
				component = c;
			}
		}
		for (Variable v : vSet) {
			if (var != null && v.toString().equals(var)) {
				variable = v;
			}
		}
		if (component != null && variable != null) {
			aSet.add(new Action(ActionType.DELETE, component, variable));
		}
		// Debug
		System.out.println(aSet);
	}

	/**
	 * Method that adds a 'verify' event to the architecture's Set.
	 * @param comp
	 *          the acting component
	 * @param stmt
	 *          the statement to verify
	 * @param proof
	 *          flag to indicate proof or attestation
	 */
	public void addVerify(String comp, String stmt, boolean proof) {
		Component component = null;
		Statement statement = null;
		// go through the Sets and identify the right objects
		for (Component c : cSet) {
			if (comp != null && c.toString().equals(comp)) {
				component = c;
			}
		}
		for (Statement s : stSet) {
			if (stmt != null && s.toString().equals(stmt)) {
				statement = s;
			}
		}
		if (component != null && statement != null && proof) {
			aSet.add(new Action(ActionType.VERIF_P, component, (Proof) statement));
		} else if (component != null && statement != null && !proof) {
			aSet.add(new Action(ActionType.VERIF_A, component, (Attest) statement));
		}
		// Debug
		System.out.println(aSet);
	}

	/**
	 * Method that adds a dependence relation to the architecture's Set.
	 * @param comp
	 *          the name of the component
	 * @param var
	 *          the name of the variable that can be deduced
	 * @param varSet
	 *          a Set of the name of variables that have to be known
	 */
	public void addDep(String comp, String var, Set<String> varSet, String probability) {
		Component component = null;
		Variable variable = null;
		Set<Variable> variableSet = new LinkedHashSet<Variable>();
		double prob = 0;
		try {
			prob = Double.parseDouble(probability);
		} catch (NumberFormatException E) {
			E.printStackTrace();
			return;
		}
		// go through the Sets and identify the right objects
		for (Component c : cSet) {
			if (comp != null && c.toString().equals(comp)) {
				component = c;
				break;
			}
		}
		for (Variable v : vSet) {
			if (var != null && v.toString().equals(var)) {
				variable = v;
			}
			for (String s : varSet) {
				if (v.toString().equals(s)) {
					variableSet.add(v);
				}
			}
		}
		if (component != null && variable != null && !variableSet.isEmpty()) {
			dSet.add(new DependenceRelation(component, new Dep(variable, variableSet, prob)));
		}
		// Debug
		System.out.println(dSet);
	}

	/**
	 * Method that adds a deduction capability to the architecture's Set.
	 * @param comp
	 *          the name of the component
	 * @param dedSet
	 *          a Set of the names of deductions
	 */
	public void addDed(String comp, Set<String> dedSet) {
		Component component = null;
		Set<Deduction> deducSet = new LinkedHashSet<Deduction>();
		// go through the Sets and identify the right objects
		for (Component c : cSet) {
			if (comp != null && c.toString().equals(comp)) {
				component = c;
				break;
			}
		}
		for (String ded : dedSet) {
			for (Deduction d : deducs) {
				if (d.toString().equals(ded)) {
					deducSet.add(d);
				}
			}
		}
		if (component != null && !deducSet.isEmpty()) {
			this.dedSet.add(new DeductionCapability(component, deducSet));
		}
		// Debug
		System.out.println(this.dedSet);
	}

	/**
	 * Method that adds a deduction to the architecture's Set.
	 * @param name
	 *          the name of the deduction
	 * @param premises
	 *          a Set of the names of premise equations
	 * @param conclusion
	 *          the name of the conclusion equation
	 */
	public void addDeduc(String name, Set<String> premises, String conclusion, String probability) {
		Equation equation = null;
		Set<Equation> eqSet = new LinkedHashSet<Equation>();
		for (Equation eq : eSet) {
			if (eq.toString().equals(conclusion)) {
				equation = eq;
			}
			for (String e : premises) {
				if (eq.toString().equals(e)) {
					eqSet.add(eq);
				}
			}
		}
		double prob = 0;
		try {
			prob = Double.parseDouble(probability);
		} catch (NumberFormatException E) {
			E.printStackTrace();
			return;
		}
		if (equation != null && !eqSet.isEmpty()) {
			deducs.add(new Deduction(Deduction.Type.ELSE, eqSet, equation, name, prob));
		}
		// Debug
		System.out.println(deducs);
	}

	/**
	 * Method that adds an attestation to the architecture's Set.
	 * @param comp
	 *          the name of the attesting component
	 * @param eqs
	 *          a Set of the names of equations to be attested
	 */
	public void addAttest(String comp, Set<String> eqs) {
		Component component = null;
		Set<Equation> equations = new LinkedHashSet<Equation>();
		// go through the Sets and identify the right objects
		for (Component c : cSet) {
			if (comp != null && c.toString().equals(comp)) {
				component = c;
			}
		}

		for (String eq : eqs) {
			for (Equation e : eSet) {
				if (e.toString().equals(eq)) {
					equations.add(e);
					break;
				}
			}
		}
		if (component != null && !equations.isEmpty()) {
			stSet.add(new Attest(component, equations));
		}
		// Debug
		System.out.println(stSet);
	}

	/**
	 * Method that adds a proof to the architecture's Set.
	 * @param comp
	 *          the name of the component
	 * @param ps
	 *          a Set of the names of the attestations and equations
	 */
	public void addProof(String comp, Set<String> ps) {
		Component component = null;
		Set<P> pSet = new LinkedHashSet<P>();
		// go through the Sets and identify the right objects
		for (Component c : cSet) {
			if (comp != null && c.toString().equals(comp)) {
				component = c;
			}
		}
		for (String s : ps) {
			for (Equation e : eSet) {
				if (e.toString().equals(s)) {
					pSet.add(e);
					break;
				}
			}
			for (Statement a : stSet) {
				if (a instanceof Attest && a.toString().equals(s)) {
					pSet.add((Attest) a);
					break;
				}
			}
		}
		if (component != null && !pSet.isEmpty()) {
			stSet.add(new Proof(component, pSet));
		}
		// Debug
		System.out.println(stSet);
	}

	/**
	 * Method that adds a 'has' property to the architecture's Set.
	 * @param comp
	 *          the name of the component
	 * @param scope
	 *          the name of the scope all/one/none
	 * @param var
	 *          the name of the variable that the component should have
	 */
	public void addPropHas(String comp, String var, String probability) {
		Component component = null;
		Variable variable = null;
		// go through the Sets and identify the right objects
		for (Component c : cSet) {
			if (comp != null && c.toString().equals(comp)) {
				component = c;
			}
		}
		for (Variable v : vSet) {
			if (var != null && v.toString().equals(var)) {
				variable = v;
			}
		}
		double prob = 0;
		try {
			prob = Double.parseDouble(probability);
		} catch (NumberFormatException E) {
			E.printStackTrace();
			return;
		}
		if (component != null && variable != null) {
			pSet.add(new Property(Property.PropertyType.HAS, component, prob, variable));
		}
		// Debug
		System.out.println(pSet);
	}

	/**
	 * Method that adds a 'knows' property to the architecture's Set.
	 * @param comp
	 *          the name of the component
	 * @param eq
	 *          the name of the equation to be known
	 */
	public void addPropKnows(String comp, String eq, String probability) {
		Component component = null;
		Equation equation = null;
		// go through the Sets and identify the right objects
		for (Component c : cSet) {
			if (comp != null && c.toString().equals(comp)) {
				component = c;
			}
		}
		for (Equation e : eSet) {
			if (eq != null && e.toString().equals(eq)) {
				equation = e;
			}
		}
		double prob = 0;
		try {
			prob = Double.parseDouble(probability);
		} catch (NumberFormatException E) {
			E.printStackTrace();
			return;
		}
		if (component != null && equation != null) {
			pSet.add(new Property(Property.PropertyType.KNOWS, component, prob, equation));
		}
		// Debug
		System.out.println(pSet);
	}

	/**
	 * Method that adds a 'notShared' property to the architecture's Set.
	 * @param comp
	 *          the name of the component
	 * @param var
	 *          the name of the variable that should not be shared
	 */
	public void addPropNotShared(String comp, String var) {
		Component component = null;
		Variable variable = null;
		// go through the Sets and identify the right objects
		for (Component c : cSet) {
			if (comp != null && c.toString().equals(comp)) {
				component = c;
			}
		}
		for (Variable v : vSet) {
			if (var != null && v.toString().equals(var)) {
				variable = v;
			}
		}
		if (component != null && variable != null) {
			pSet.add(new Property(Property.PropertyType.NOTSHARED, component, variable));
		}
		// Debug
		System.out.println(pSet);
	}

	/**
	 * Method that adds a 'notStored' property to the architecture's Set.
	 * @param comp
	 *          the name of the component
	 * @param var
	 *          the name of the variable that should not be stored
	 */
	public void addPropNotStored(String comp, String var, String b) {
		Component component = null;
		Variable variable = null;
		int bound = Integer.parseInt(b);
		// go through the Sets and identify the right objects
		for (Component c : cSet) {
			if (comp != null && c.toString().equals(comp)) {
				component = c;
			}
		}
		for (Variable v : vSet) {
			if (var != null && v.toString().equals(var)) {
				variable = v;
			}
		}
		if (component != null && variable != null) {
			pSet.add(new Property(Property.PropertyType.NOTSTORED, component, variable, bound));
		}
		// Debug
		System.out.println(pSet);
	}

	/**
	 * Method that adds a conjunctive property to the architecture's Set.
	 * @param prop1
	 *          the name of the first property
	 * @param prop2
	 *          the name of the second property
	 */
	public void addPropConj(String prop1, String prop2) {
		Property property1 = null;
		Property property2 = null;
		// go through the Set and identify the right objects
		for (Property p : pSet) {
			if (prop1 != null && p.toString().equals(prop1)) {
				property1 = p;
			} else if (prop2 != null && p.toString().equals(prop2)) {
				property2 = p;
			}
		}
		if (property1 != null && property2 != null) {
			pSet.add(new Property(Property.PropertyType.CONJUNCTION, property1, property2));
		}
		// Debug
		System.out.println(pSet);
	}
	
	/**
	 * Method that adds a negation property to the architecture's Set.
	 * @param prop1
	 *          the name of the first property
	 */
	public void addPropNeg(String prop1) {
		Property property1 = null;
		// go through the Set and identify the right objects
		for (Property p : pSet) {
			if (prop1 != null && p.toString().equals(prop1)) {
				property1 = p;
				break;
			}
		}
		if (property1 != null) {
			pSet.add(new Property(Property.PropertyType.NEGATION, property1));
		}
		// Debug
		System.out.println(pSet);
	}

	// remover methods
	/**
	 * Method that removes an event from the architecture's Set.
	 * @param act
	 *          the name of the event
	 */
	public void removeAction(String act) {
		Action action = null;
		// go through the Sets and identify the right object
		for (Action a : aSet) {
			if (act != null && a.toString().equals(act)) {
				action = a;
			}
		}

		if (action != null) {
			// remove the action
			aSet.remove(action);
		}
	}

	/**
	 * Method that removes a component from the architecture's Set.
	 * @param comp
	 *          the name of the component
	 */
	public void removeComponent(String comp) {
		Component component = null;
		// go through the Sets and identify the right object
		for (Component c : cSet) {
			if (comp != null && c.toString().equals(comp)) {
				component = c;
			}
		}

		if (component != null) {
			// remove the action
			cSet.remove(component);
		}
	}

	/**
	 * Method that removes an equation from the architecture's Set.
	 * @param eq
	 *          the name of the equation
	 */
	public void removeEquation(String eq) {
		Equation equation = null;
		// go through the Sets and identify the right object
		for (Equation e : eSet) {
			if (eq != null && e.toString().equals(eq)) {
				equation = e;
			}
		}

		if (equation != null) {
			// remove the action
			eSet.remove(equation);
		}
	}

	/**
	 * Method that removes a statement from the architecture's Set.
	 * @param stmt
	 *          the name of the statement
	 */
	public void removeStatement(String stmt) {
		Statement statement = null;
		// go through the Sets and identify the right object
		for (Statement s : stSet) {
			if (stmt != null && s.toString().equals(stmt)) {
				statement = s;
			}
		}

		if (statement != null) {
			// remove the action
			stSet.remove(statement);
		}
	}

	/**
	 * Method that removes a term from the architecture's Set.
	 * @param te
	 *          the name of the term
	 */
	public void removeTerm(String te) {
		Term term = null;
		// go through the Sets and identify the right object
		for (Term t : tSet) {
			if (te != null && t.toString().equals(te)) {
				term = t;
			}
		}

		if (term != null) {
			// remove the action
			tSet.remove(term);
		}
	}

	/**
	 * Method that removes a trust relation from the architecture's Set.
	 * @param tru
	 *          the name of the trust relation
	 */
	public void removeTrust(String tru) {
		Trust trust = null;
		// go through the Sets and identify the right object
		for (Trust t : trustSet) {
			if (tru != null && t.toString().equals(tru)) {
				trust = t;
			}
		}

		if (trust != null) {
			// remove the action
			trustSet.remove(trust);
		}
	}

	/**
	 * Method that removes a variable from the architecture's Set.
	 * @param var
	 *          the name of the variable
	 */
	public void removeVariable(String var) {
		Variable variable = null;
		// go through the Sets and identify the right object
		for (Variable v : vSet) {
			if (var != null && v.toString().equals(var)) {
				variable = v;
			}
		}

		if (variable != null) {
			// remove the action
			vSet.remove(variable);
		}
	}

	/**
	 * Method that removes a dependence relation from the architecture's Set.
	 * @param dep
	 *          the name of the dependence relation
	 */
	public void removeDep(String dep) {
		DependenceRelation depend = null;
		// go through the Sets and identify the right object
		for (DependenceRelation d : dSet) {
			if (dep != null && d.toString().equals(dep)) {
				depend = d;
			}
		}

		if (depend != null) {
			// remove the action
			dSet.remove(depend);
		}
	}

	/**
	 * Method that removes a deduction capability from the architecture's Set.
	 * @param ded
	 *          the deduction capability
	 */
	public void removeDed(String ded) {
		DeductionCapability deduc = null;
		// go through the Sets and identify the right object
		for (DeductionCapability d : dedSet) {
			if (ded != null && d.toString().equals(ded)) {
				deduc = d;
			}
		}

		if (deduc != null) {
			// remove the action
			dedSet.remove(deduc);
		}
	}

	/**
	 * Method that removes a property from the architecture's Set.
	 * @param prop
	 *          the name of the property
	 */
	public void removeProp(String prop) {
		Property property = null;
		// go through the Sets and identify the right object
		for (Property p : pSet) {
			if (prop != null && p.toString().equals(prop)) {
				property = p;
			}
		}

		if (property != null) {
			// remove the action
			pSet.remove(property);
		}
	}


	/**
	 * Method that sets the architecture's Set of properties to a given Set.
	 * @param props
	 *          a Set of properties
	 */
	public void setpSet(Set<Property> props) {
		// This Set must be expendable, hence create new Set from elements
		pSet = new LinkedHashSet<Property>();
		for (Property p : props) {
			pSet.add(p);
		}
	}

	// getter and setter methods
	public Set<Component> getcSet() {
		return cSet;
	}

	public void setcSet(Set<Component> cSet) {
		this.cSet = cSet;
	}

	public Set<Variable> getvSet() {
		return vSet;
	}

	public void setvSet(Set<Variable> vSet) {
		this.vSet = vSet;
	}

	public Set<Term> gettSet() {
		return tSet;
	}

	public void settSet(Set<Term> tSet) {
		this.tSet = tSet;
	}

	public Set<Equation> geteSet() {
		return eSet;
	}

	public void seteSet(Set<Equation> eSet) {
		this.eSet = eSet;
	}

	public Set<Trust> gettrustSet() {
		return trustSet;
	}

	public void settrustSet(Set<Trust> trusts) {
		this.trustSet = trusts;
	}

	public Set<Action> getaSet() {
		return aSet;
	}

	public void setaSet(Set<Action> actions) {
		this.aSet = actions;
	}

	public Set<Statement> getstSet() {
		return stSet;
	}

	public void setstSet(Set<Statement> statements) {
		this.stSet = statements;
	}

	public Set<DependenceRelation> getdSet() {
		return dSet;
	}

	public void setdSet(Set<DependenceRelation> deps) {
		this.dSet = deps;
	}

	public Set<DeductionCapability> getdedSet() {
		return dedSet;
	}

	public void setdedSet(Set<DeductionCapability> deds) {
		this.dedSet = deds;
	}

	public Set<Deduction> getDeducs() {
		return deducs;
	}

	public Set<Property> getpSet() {
		return pSet;
	}

	public Architecture getArch() {
		return arch;
	}
}
