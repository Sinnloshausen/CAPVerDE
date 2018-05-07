package utils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import architecture.Action;
import architecture.Component;
import architecture.Deduction;
import architecture.DeductionCapability;
import architecture.Dep;
import architecture.DependenceRelation;
import architecture.Equation;
import architecture.Term;
import architecture.Variable;
import properties.Property;
import properties.Property.PropertyType;
import architecture.Action.ActionType;
import architecture.Equation.Relation;
import architecture.Equation.Type;
import architecture.Term.Operator;
import architecture.Term.OperatorType;
import architecture.Term.TermType;
import architecture.Trust;
import gui.ArchitectureFunctions;
import gui.ArchitectureFunctions.CaseStudy;

/**
 * Class that helps loading the pre-existing case studies.
 */
public class ArchLoader {

	// class fields
	// Components
	private static Component SM = new Component("SM");
	private static Component MI = new Component("MI");
	private static Component Re = new Component("Re");
	private static Component HN = new Component("HN");
	private static Set<Component> cSet1 = Stream.of(SM, MI, Re, HN).collect(Collectors.toCollection(LinkedHashSet::new));
	// Variables
	private static Variable readings = new Variable("readings");
	private static Variable k = new Variable("k");
	private static Variable bill = new Variable("bill");
	private static Variable pw = new Variable("pw");
	private static Variable secret = new Variable("secret");
	private static Variable encBill = new Variable("encBill");
	private static Variable encR = new Variable("encR");
	private static Variable ppd = new Variable("ppd");
	private static Set<Variable> vSet1 = Stream.of(readings, k, bill, pw, secret, encBill, encR, ppd).collect(Collectors.toCollection(LinkedHashSet::new));
	// Terms
	private static Term termReadings = new Term(TermType.ATOM, readings, false);
	private static Term termK = new Term(TermType.ATOM, k, false);
	private static Term termBill = new Term(TermType.ATOM, bill, false);
	private static Term termPw = new Term(TermType.ATOM, pw, false);
	private static Term termSecret = new Term(TermType.ATOM, secret, false);
	private static Term termPpd = new Term(TermType.ATOM, ppd, false);
	private static Term termEncB = new Term(TermType.ATOM, encBill, false);
	private static Term termEncR = new Term(TermType.ATOM, encR, false);
	private static Term termBetaReadings = new Term(
			TermType.COMPOSITION, OperatorType.UNARY, Operator.FUNC, "beta", termReadings, false);
	private static Term termEncReadings = new Term(
			TermType.COMPOSITION, OperatorType.BINARY, Operator.FUNC, "Enc", termReadings, termK, false);
	private static Term termDecReadings = new Term(
			TermType.COMPOSITION, OperatorType.BINARY, Operator.FUNC, "Dec", termEncR, termK, false);
	private static Term termEncBill = new Term(
			TermType.COMPOSITION, OperatorType.BINARY, Operator.FUNC, "Enc", termBill, termK, false);
	private static Term termDecBill = new Term(
			TermType.COMPOSITION, OperatorType.BINARY, Operator.FUNC, "Dec", termEncB, termK, false);
	private static Term termPhiReadings = new Term(
			TermType.COMPOSITION, OperatorType.TERTIARY, Operator.FUNC, "phi", termReadings, termBill, termPw, false);
	private static Term termPhiInvReadings = new Term(
			TermType.COMPOSITION, OperatorType.BINARY, Operator.FUNC, "phi^-1", termPpd, termPw, false);
	private static Term termPhiInvBill = new Term(
			TermType.COMPOSITION, OperatorType.BINARY, Operator.FUNC, "phi^-1", termPpd, termPw, false);
	private static Set<Term> tSet1 = Stream.of(
			termReadings, termK, termBill, termPw, termSecret, termPpd, termEncB, termEncR,
			termBetaReadings, termEncReadings, termDecReadings, termEncBill, termDecBill,
			termPhiReadings, termPhiInvReadings, termPhiInvBill).collect(Collectors.toCollection(LinkedHashSet::new));
	// Equations
	private static Equation encR_enc = new Equation(
			"encR_enc", Type.RELATION, Relation.EQUALITY, termEncR, termEncReadings);
	private static Equation bill_dec = new Equation(
			"bill_dec", Type.RELATION, Relation.EQUALITY, termBill, termDecBill);
	private static Equation ppd_phi = new Equation(
			"ppd_phi", Type.RELATION, Relation.EQUALITY, termPpd, termPhiReadings);
	private static Equation readings_dec = new Equation(
			"readings_dec", Type.RELATION, Relation.EQUALITY, termReadings, termDecReadings);
	private static Equation bill_beta = new Equation(
			"bill_beta", Type.RELATION, Relation.EQUALITY, termBill, termBetaReadings);
	private static Equation encBill_enc = new Equation(
			"encBill_enc", Type.RELATION, Relation.EQUALITY, termEncB, termEncBill);
	private static Equation readings_phiInv = new Equation(
			"readings_phiInv", Type.RELATION, Relation.EQUALITY, termReadings, termPhiInvReadings);
	private static Equation bill_phiInv = new Equation(
			"bill_phiInv", Type.RELATION, Relation.EQUALITY, termBill, termPhiInvBill);
	private static Set<Equation> eSet1 = Stream.of(
			encR_enc, bill_dec, ppd_phi, readings_dec, bill_beta, encBill_enc, readings_phiInv, bill_phiInv).collect(Collectors.toCollection(LinkedHashSet::new));
	// Trusts
	private static Set<Trust> trustSet1 = new LinkedHashSet<Trust>();
	// Statements
	private static Set<architecture.Statement> stSet1 = new LinkedHashSet<architecture.Statement>();
	// Actions
	private static Action hasSM_readings = new Action(ActionType.HAS, SM, readings);
	private static Action hasSM_pw = new Action(ActionType.HAS, SM, pw);
	private static Action hasSM_k = new Action(ActionType.HAS, SM, k);
	private static Action hasMI_k = new Action(ActionType.HAS, MI, k);
	private static Action hasRe_pw = new Action(ActionType.HAS, Re, pw);
	private static Action hasRe_secret = new Action(ActionType.HAS, Re, secret);
	private static Action computeSM_encR = new Action(
			ActionType.COMPUTE, SM, encR_enc);
	private static Action computeSM_bill = new Action(
			ActionType.COMPUTE, SM, bill_dec);
	private static Action computeSM_ppd = new Action(
			ActionType.COMPUTE, SM, ppd_phi);
	private static Action computeMI_readings = new Action(
			ActionType.COMPUTE, MI, readings_dec);
	private static Action computeMI_bill = new Action(
			ActionType.COMPUTE, MI, bill_beta);
	private static Action computeMI_encBill = new Action(
			ActionType.COMPUTE, MI, encBill_enc);
	private static Action computeRe_readings = new Action(
			ActionType.COMPUTE, Re, readings_phiInv);
	private static Action computeRe_bill = new Action(
			ActionType.COMPUTE, Re, bill_phiInv);
	private static Action receiveHNSM1 = new Action(
			ActionType.RECEIVE, HN, SM, Collections.emptySet(), Set.of(encR));
	private static Action receiveSMHN = new Action(
			ActionType.RECEIVE, SM, HN, Collections.emptySet(), Set.of(encBill));
	private static Action receiveHNSM2 = new Action(
			ActionType.RECEIVE, HN, SM, Collections.emptySet(), Set.of(ppd));
	private static Action receiveMIHN = new Action(
			ActionType.RECEIVE, MI, HN, Collections.emptySet(), Set.of(encR));
	private static Action receiveHNMI = new Action(
			ActionType.RECEIVE, HN, MI, Collections.emptySet(), Set.of(encBill));
	private static Action receiveReHN = new Action(
			ActionType.RECEIVE, Re, HN, Collections.emptySet(), Set.of(ppd));
	private static Action checkRe = new Action(
			ActionType.CHECK, Re, Set.of(bill_beta));
	private static Set<Action> aSet1 = Stream.of(
			hasSM_readings, hasSM_pw, hasSM_k, hasMI_k, hasRe_pw, hasRe_secret, computeSM_encR, computeSM_bill,
			computeSM_ppd, computeMI_readings, computeMI_bill, computeMI_encBill, computeRe_readings,
			computeRe_bill, receiveHNSM1, receiveSMHN, receiveHNSM2, receiveMIHN, receiveHNMI, receiveReHN, checkRe).collect(Collectors.toCollection(LinkedHashSet::new));
	// Dependencies
	private static Dep dep1 = new Dep(pw, Collections.emptySet(), 0.001);
	private static Dep dep2 = new Dep(readings, Set.of(encR), 0.00001);
	private static Dep dep3 = new Dep(readings, Set.of(ppd, pw), 1);
	private static Dep dep4 = new Dep(secret, Collections.emptySet(), 0.01);
	private static Dep dep5 = new Dep(pw, Set.of(secret), 1);
	private static Set<DependenceRelation> dSet1 = Stream.of(
			new DependenceRelation(HN, dep1), new DependenceRelation(HN, dep2),
			new DependenceRelation(HN, dep3), new DependenceRelation(HN, dep4),
			new DependenceRelation(HN, dep5)).collect(Collectors.toCollection(LinkedHashSet::new));
	// Deductions
	private static Variable varT = new Variable("t");
	private static Term termT = new Term(TermType.ATOM, varT, true);
	private static Variable varU = new Variable("u");
	private static Term termU = new Term(TermType.ATOM, varU, true);
	private static Variable varX = new Variable("x");
	private static Variable varY = new Variable("y");
	private static Term termX = new Term(TermType.ATOM, varX, true);
	private static Term termY = new Term(TermType.ATOM, varY, true);
	private static Equation dedEq2 = new Equation(
			"subst", Type.RELATION, Relation.EQUALITY, termT, termU);
	private static Equation dedEq3 = new Equation(
			"dedEq3", Type.RELATION, Relation.EQUALITY, termX, termY);
	private static Set<Equation> dedEqSet2 = Set.of(dedEq2, dedEq3);
	private static Deduction deduc4 = new Deduction(
			Deduction.Type.SUBST, dedEqSet2, dedEq2, "Substitution", 1);
	private static DeductionCapability dc_SM = new DeductionCapability(SM, Set.of(deduc4));
	private static DeductionCapability dc_MI = new DeductionCapability(MI, Set.of(deduc4));
	private static DeductionCapability dc_Re = new DeductionCapability(Re, Set.of(deduc4));
	private static DeductionCapability dc_HN = new DeductionCapability(HN, Set.of(deduc4));
	private static Set<DeductionCapability> dedSet1 = Stream.of(dc_SM, dc_MI, dc_Re, dc_HN).collect(Collectors.toCollection(LinkedHashSet::new));
	// Statements
	private static Property statement1 = new Property(PropertyType.KNOWS, Re, (double)1, bill_beta);
	private static Property statement2_tmp = new Property(PropertyType.HAS, HN, 0.001, readings);
	private static Property statement2 = new Property(PropertyType.NEGATION, statement2_tmp);
	//TODO 1 more?
	private static Set<Property> pSet1 = Stream.of(statement1, statement2).collect(Collectors.toCollection(LinkedHashSet::new));

	/**
	 * Method to load one of the case studies.
	 * 
	 * @param archFunc
	 * 			the architecture functions object that holds all information about the architecture
	 * @param example
	 * 			the case study to load
	 */
	public static void load(ArchitectureFunctions archFunc, CaseStudy example) {
		// set all the necessary list of the right architecture
		switch (example) {
		case SEM:
			// second case study: smart energy metering
			archFunc.setcSet(cSet1);
			archFunc.setvSet(vSet1);
			archFunc.settSet(tSet1);
			archFunc.seteSet(eSet1);
			archFunc.settrustSet(trustSet1);
			archFunc.setstSet(stSet1);
			archFunc.setaSet(aSet1);
			archFunc.setdSet(dSet1);
			archFunc.setdedSet(dedSet1);
			archFunc.setpSet(pSet1);
			break;
		default:
			break;
		}
	}

}
