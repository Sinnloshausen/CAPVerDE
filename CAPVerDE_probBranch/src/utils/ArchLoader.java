package utils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import architecture.Action;
import architecture.Component;
import architecture.Composition;
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
	//########## Smart Home #################
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

	//############## AccuWeather ###############
	// Components
	private static Component U = new Component("U");
	private static Component AW = new Component("AW");
	private static Component RM = new Component("RM");
	private static Set<Component> cSet2 = Stream.of(U, AW, RM).collect(Collectors.toCollection(LinkedHashSet::new));
	// Variables
	private static Variable location = new Variable("location");
	private static Variable wifi_info = new Variable("wifi_info");
	private static Variable weather = new Variable("weather");
	private static Set<Variable> vSet2 = Stream.of(location, wifi_info, weather).collect(Collectors.toCollection(LinkedHashSet::new));
	// Terms
	private static Term termLocation = new Term(TermType.ATOM, location, false);
	private static Term termWifi_info = new Term(TermType.ATOM, wifi_info, false);
	private static Term termWeather = new Term(TermType.ATOM, weather, false);
	private static Term termPhiWifi = new Term(
			TermType.COMPOSITION, OperatorType.UNARY, Operator.FUNC, "phi", termWifi_info, false);
	private static Set<Term> tSet2 = Stream.of(
			termLocation, termWifi_info, termWeather, termPhiWifi).collect(Collectors.toCollection(LinkedHashSet::new));
	// Equations
	private static Equation location_phi = new Equation(
			"location_phi", Type.RELATION, Relation.EQUALITY, termLocation, termPhiWifi);
	private static Set<Equation> eSet2 = Stream.of(location_phi).collect(Collectors.toCollection(LinkedHashSet::new));
	// Trusts
	private static Set<Trust> trustSet2 = new LinkedHashSet<Trust>();
	// Statements
	private static Set<architecture.Statement> stSet2 = new LinkedHashSet<architecture.Statement>();
	// Actions
	private static Action hasU_location = new Action(ActionType.HAS, U, location);
	private static Action hasU_wifi_info = new Action(ActionType.HAS, U, wifi_info);
	private static Action hasAW_weather = new Action(ActionType.HAS, AW, weather);
	private static Action computeRM_location = new Action(
			ActionType.COMPUTE, RM, location_phi);
	private static Action receiveAWU = new Action(
			ActionType.RECEIVE, AW, U, Collections.emptySet(), Set.of(wifi_info));
	private static Action receiveRMAW = new Action(
			ActionType.RECEIVE, RM, AW, Collections.emptySet(), Set.of(wifi_info));
	private static Action receiveUAW = new Action(
			ActionType.RECEIVE, U, AW, Collections.emptySet(), Set.of(weather));
	private static Set<Action> aSet2 = Stream.of(
			hasU_location, hasU_wifi_info, hasAW_weather, computeRM_location, receiveAWU, receiveRMAW, receiveUAW).collect(Collectors.toCollection(LinkedHashSet::new));
	// Dependencies
	private static Dep dep = new Dep(location, Set.of(wifi_info), 0.5);
	private static Set<DependenceRelation> dSet2 = Stream.of(
			new DependenceRelation(AW, dep), new DependenceRelation(RM, dep)).collect(Collectors.toCollection(LinkedHashSet::new));
	// Deductions
	private static DeductionCapability dc_U = new DeductionCapability(U, Set.of(deduc4));
	private static DeductionCapability dc_AW = new DeductionCapability(AW, Set.of(deduc4));
	private static DeductionCapability dc_RM = new DeductionCapability(RM, Set.of(deduc4));
	private static Set<DeductionCapability> dedSet2 = Stream.of(dc_U, dc_AW, dc_RM).collect(Collectors.toCollection(LinkedHashSet::new));
	// Statements
	private static Property property_accuracy = new Property(PropertyType.HAS, U, (double)1, weather);
	private static Property property_tmp = new Property(PropertyType.HAS, AW, 0.001, location);
	private static Property property_dataMinimisation = new Property(PropertyType.NEGATION, property_tmp);
	private static Property property_dataMinimisation2 = new Property(PropertyType.NOTSHARED, AW, wifi_info);
	//TODO 1 more?
	private static Set<Property> pSet2 = Stream.of(property_accuracy, property_dataMinimisation, property_dataMinimisation2).collect(Collectors.toCollection(LinkedHashSet::new));

	//########## Patient Data Register #################
	// Components
	private static Component M = new Component("M");
	private static Component CR = new Component("CR");
	private static Component RA = new Component("RA");
	private static Component CAi = new Component("CA", "i");
	private static Component TCi = new Component("TC", "i");
	private static Component Pj = new Component("P", "j");
	private static Component Rl = new Component("R", "l");
	private static Component MDik = new Component("MD", "ik");
	private static Component IDBi = new Component("IDB", "i");
	private static Component RDBi = new Component("RDB", "i");
	private static Set<Component> cSet3 = Stream.of(M, CR, RA, CAi, TCi, Pj, Rl, MDik, IDBi, RDBi).collect(Collectors.toCollection(LinkedHashSet::new));
	// Variables
	private static Variable cki = new Variable("ck", "i");
	private static Variable iki = new Variable("ik", "i");
	private static Variable pukik = new Variable("puk", "ik");
	private static Variable eckik = new Variable("eck", "ik");
	private static Variable eikik = new Variable("eik", "ik");
	private static Variable pDj = new Variable("pD", "j");
	private static Variable pIDj = new Variable("pID", "j");
	private static Variable mDj = new Variable("mD", "j");
	private static Variable eukik = new Variable("euk", "ik");
	private static Variable pubk = new Variable("pubk");
	private static Variable privk = new Variable("privk");
	private static Variable pwkik = new Variable("pwk", "ik");
	private static Variable mdkj = new Variable("mdk", "j");
	private static Variable ukik = new Variable("uk", "ik");
	private static Variable epDj = new Variable("epD", "j");
	private static Variable emDj = new Variable("emD", "j");
	private static Variable cemdkj = new Variable("cemdk", "j");
	private static Variable emdkj = new Variable("emdk", "j");
	private static Variable mk = new Variable("mk");
	private static Variable memdkj = new Variable("memdk", "j");
	private static Variable ipj = new Variable("ip", "j");
	private static Variable amDj = new Variable("amD", "j");
	private static Variable stats = new Variable("stats");
	private static Variable rand_seedj = new Variable("rand_seed", "j");
	private static Set<Variable> vSet3 = Stream.of(cki, iki, pukik, eckik, eikik, pDj, pIDj, mDj, eukik, pubk, privk, pwkik,
			mdkj, ukik, epDj, emDj, cemdkj, emdkj, mk, memdkj, ipj, amDj, stats, rand_seedj).collect(Collectors.toCollection(LinkedHashSet::new));
	// Terms
	private static Term termCki = new Term(TermType.ATOM, cki, false);
	private static Term termIki = new Term(TermType.ATOM, iki, false);
	private static Term termPukik = new Term(TermType.ATOM, pukik, false);
	private static Term termEckik = new Term(TermType.ATOM, eckik, false);
	private static Term termEikik = new Term(TermType.ATOM, eikik, false);
	private static Term termPDj = new Term(TermType.ATOM, pDj, false);
	private static Term termPIDj = new Term(TermType.ATOM, pIDj, false);
	private static Term termMDj = new Term(TermType.ATOM, mDj, false);
	private static Term termEukik = new Term(TermType.ATOM, eukik, false);
	private static Term termPubk = new Term(TermType.ATOM, pubk, false);
	private static Term termPrivk = new Term(TermType.ATOM, privk, false);
	private static Term termPwkik = new Term(TermType.ATOM, pwkik, false);
	private static Term termMdkj = new Term(TermType.ATOM, mdkj, false);
	private static Term termUkik = new Term(TermType.ATOM, ukik, false);
	private static Term termEpDj = new Term(TermType.ATOM, epDj, false);
	private static Term termEmDj = new Term(TermType.ATOM, emDj, false);
	private static Term termCemdkj = new Term(TermType.ATOM, cemdkj, false);
	private static Term termEmdkj = new Term(TermType.ATOM, emdkj, false);
	private static Term termMk = new Term(TermType.ATOM, mk, false);
	private static Term termMemdkj = new Term(TermType.ATOM, memdkj, false);
	private static Term termIpj = new Term(TermType.ATOM, ipj, false);
	private static Term termAmDj = new Term(TermType.ATOM, amDj, false);
	private static Term termStats = new Term(TermType.ATOM, stats, false);
	private static Term termRand = new Term(TermType.ATOM, rand_seedj, false);
	private static Term termEval = new Term(
			TermType.COMPOSITION, OperatorType.UNARY, Operator.FUNC, "eval", termAmDj, false);
	private static Term termDecEmDj = new Term(
			TermType.COMPOSITION, OperatorType.BINARY, Operator.FUNC, "dec", termEmDj, termMdkj, false);
	private static Term termAnon = new Term(
			TermType.COMPOSITION, OperatorType.UNARY, Operator.FUNC, "anon", termDecEmDj, false);
	private static Term termEncIki = new Term(
			TermType.COMPOSITION, OperatorType.BINARY, Operator.FUNC, "enc", termIki, termPukik, false);
	private static Term termEncCki = new Term(
			TermType.COMPOSITION, OperatorType.BINARY, Operator.FUNC, "enc", termCki, termPukik, false);
	private static Term termDecEukik = new Term(
			TermType.COMPOSITION, OperatorType.BINARY, Operator.FUNC, "dec", termEukik, termPwkik, false);
	private static Term termDecEckik = new Term(
			TermType.COMPOSITION, OperatorType.BINARY, Operator.FUNC, "dec", termEckik, termUkik, false);
	private static Term termDecEikik = new Term(
			TermType.COMPOSITION, OperatorType.BINARY, Operator.FUNC, "dec", termEikik, termUkik, false);
	private static Term termEncPIDj = new Term(
			TermType.COMPOSITION, OperatorType.TERTIARY, Operator.FUNC, "enc", termPIDj, termPDj, termIki, false);
	private static Term termEncMDj = new Term(
			TermType.COMPOSITION, OperatorType.BINARY, Operator.FUNC, "enc", termMDj, termMdkj, false);
	private static Term termEncMdkj1 = new Term(
			TermType.COMPOSITION, OperatorType.BINARY, Operator.FUNC, "enc", termMdkj, termCki, false);
	private static Term termEncMdkj2 = new Term(
			TermType.COMPOSITION, OperatorType.BINARY, Operator.FUNC, "enc", termMdkj, termPubk, false);
	private static Term termEncMdkj3 = new Term(
			TermType.COMPOSITION, OperatorType.BINARY, Operator.FUNC, "enc", termMdkj, termMk, false);
	private static Term termDecEmdkj = new Term(
			TermType.COMPOSITION, OperatorType.BINARY, Operator.FUNC, "dec", termEmdkj, termPrivk, false);
	private static Term termUniRand = new Term(
			TermType.COMPOSITION, OperatorType.UNARY, Operator.FUNC, "uni_rand", termRand, false);
	private static Set<Term> tSet3 = Stream.of(
			termCki, termIki, termPukik, termEckik, termEikik, termPDj, termPIDj, termMDj, termEukik, termPubk, termPrivk,
			termPwkik, termMdkj, termUkik, termEpDj, termEmDj, termCemdkj, termEmdkj, termMk, termMemdkj, termIpj, termAmDj,
			termStats, termRand, termEval, termDecEmDj, termAnon, termEncIki, termEncCki, termDecEukik, termDecEckik, termDecEikik,
			termEncPIDj, termEncMDj, termEncMdkj1, termEncMdkj2, termEncMdkj3, termDecEmdkj, termUniRand).collect(Collectors.toCollection(LinkedHashSet::new));
	// Equations
	private static Equation eck_enc = new Equation(
			"eck_enc", Type.RELATION, Relation.EQUALITY, termEckik, termEncCki);
	private static Equation eik_enc = new Equation(
			"eik_enc", Type.RELATION, Relation.EQUALITY, termEikik, termEncIki);
	private static Equation uk_dec = new Equation(
			"uk_dec", Type.RELATION, Relation.EQUALITY, termUkik, termDecEukik);
	private static Equation ck_dec = new Equation(
			"ck_dec", Type.RELATION, Relation.EQUALITY, termCki, termDecEckik);
	private static Equation ik_dec = new Equation(
			"ik_dec", Type.RELATION, Relation.EQUALITY, termIki, termDecEikik);
	private static Equation emD_enc = new Equation(
			"emD_enc", Type.RELATION, Relation.EQUALITY, termEmDj, termEncMDj);
	private static Equation epD_enc = new Equation(
			"epD_enc", Type.RELATION, Relation.EQUALITY, termEpDj, termEncPIDj);
	private static Equation cemdk_enc = new Equation(
			"cemdk_enc", Type.RELATION, Relation.EQUALITY, termCemdkj, termEncMdkj1);
	private static Equation emdk_enc = new Equation(
			"emdk_enc", Type.RELATION, Relation.EQUALITY, termEmdkj, termEncMdkj2);
	private static Equation memdk_enc = new Equation(
			"memdk_enc", Type.RELATION, Relation.EQUALITY, termMemdkj, termEncMdkj3);
	private static Equation ip_rand = new Equation(
			"ip_rand", Type.RELATION, Relation.EQUALITY, termIpj, termRand);
	private static Equation mdk_dec = new Equation(
			"mdk_dec", Type.RELATION, Relation.EQUALITY, termMdkj, termDecEmdkj);
	private static Equation amD_anon = new Equation(
			"amD_anon", Type.RELATION, Relation.EQUALITY, termAmDj, termAnon);
	private static Equation stats_eval = new Equation(
			"stats_eval", Type.RELATION, Relation.EQUALITY, termStats, termEval);
	private static Set<Equation> eSet3 = Stream.of(
			eck_enc, eik_enc, uk_dec, ck_dec, ik_dec, emD_enc, epD_enc, cemdk_enc, emdk_enc, memdk_enc, ip_rand, mdk_dec, amD_anon, stats_eval).collect(Collectors.toCollection(LinkedHashSet::new));
	// Trusts
	private static Set<Trust> trustSet3 = new LinkedHashSet<Trust>();
	// Compositions and Associations
	private static Composition TCiCAi = new Composition(TCi, CAi);
	private static Composition TCiMDik = new Composition(TCi, MDik);
	private static Composition CRRA = new Composition(CR, RA);
	private static Composition CRIDBi = new Composition(CR, IDBi); //TODO right now assoc = compos in both directions, maybe change?
	private static Composition CRRDBi = new Composition(CR, RDBi);
	private static Composition IDBiCR = new Composition(IDBi, CR);
	private static Composition RDBiCR = new Composition(RDBi, CR);
	private static Set<Composition> composSet = Stream.of(TCiCAi, TCiMDik, CRRA, CRIDBi, CRRDBi, IDBiCR, RDBiCR).collect(Collectors.toCollection(LinkedHashSet::new));
	// Statements
	private static Set<architecture.Statement> stSet3 = new LinkedHashSet<architecture.Statement>();
	// Actions
	private static Action hasCA_ck = new Action(ActionType.HAS, CAi, cki);
	private static Action hasCA_ik = new Action(ActionType.HAS, CAi, iki);
	private static Action hasCA_puk = new Action(ActionType.HAS, CAi, pukik);
	private static Action hasP_pD = new Action(ActionType.HAS, Pj, pDj);
	private static Action hasTC_pID = new Action(ActionType.HAS, TCi, pIDj);
	private static Action hasTC_mD = new Action(ActionType.HAS, TCi, mDj);
	private static Action hasTC_euk = new Action(ActionType.HAS, TCi, eukik);
	private static Action hasTC_pubk = new Action(ActionType.HAS, TCi, pubk);
	private static Action hasMD_pwk = new Action(ActionType.HAS, MDik, pwkik);
	private static Action hasMD_mdk = new Action(ActionType.HAS, MDik, mdkj);
	private static Action hasRA_mk = new Action(ActionType.HAS, RA, mk);
	private static Action hasRA_rand = new Action(ActionType.HAS, RA, rand_seedj);
	private static Action hasCR_privk = new Action(ActionType.HAS, CR, privk);
	private static Action computeCA_eck = new Action(ActionType.COMPUTE, CAi, eck_enc);
	private static Action computeCA_eik = new Action(ActionType.COMPUTE, CAi, eik_enc);
	private static Action computeMD_uk = new Action(ActionType.COMPUTE, MDik, uk_dec);
	private static Action computeMD_ck = new Action(ActionType.COMPUTE, MDik, ck_dec);
	private static Action computeMD_ik = new Action(ActionType.COMPUTE, MDik, ik_dec);
	private static Action computeMD_epD = new Action(ActionType.COMPUTE, MDik, epD_enc);
	private static Action computeMD_emD = new Action(ActionType.COMPUTE, MDik, emD_enc);
	private static Action computeMD_cemdk = new Action(ActionType.COMPUTE, MDik, cemdk_enc);
	private static Action computeMD_emdk = new Action(ActionType.COMPUTE, MDik, emdk_enc);
	private static Action computeRA_memdk = new Action(ActionType.COMPUTE, RA, memdk_enc);
	private static Action computeRA_ip = new Action(ActionType.COMPUTE, RA, ip_rand);
	private static Action computeCR_mdk = new Action(ActionType.COMPUTE, CR, mdk_dec);
	private static Action computeCR_amD = new Action(ActionType.COMPUTE, CR, amD_anon);
	private static Action computeR_stats = new Action(ActionType.COMPUTE, Rl, stats_eval);
	private static Action receiveMDCA = new Action(ActionType.RECEIVE, MDik, CAi, Collections.emptySet(), Set.of(eckik, eikik));
	private static Action receiveTCP = new Action(ActionType.RECEIVE, TCi, Pj, Collections.emptySet(), Set.of(pDj));
	private static Action receiveCRMD = new Action(ActionType.RECEIVE, CR, MDik, Collections.emptySet(), Set.of(emDj, emdkj, cemdkj, epDj));
	private static Action receiveRCR = new Action(ActionType.RECEIVE, Rl, CR, Collections.emptySet(), Set.of(amDj));
	private static Action receiveIDBRA = new Action(ActionType.RECEIVE, IDBi, RA, Collections.emptySet(), Set.of(ipj, cemdkj, memdkj, emDj));
	private static Action receiveRDBRA = new Action(ActionType.RECEIVE, RDBi, RA, Collections.emptySet(), Set.of(ipj, epDj));
	private static Action spotcheckMTC = new Action(ActionType.SPOTCHECK, M, TCi, Set.of(pDj, mDj));
	private static Action spotcheckMCR = new Action(ActionType.SPOTCHECK, M, CR, Set.of(epDj, emDj));
	private static Set<Action> aSet3 = Stream.of(
			hasCA_ck, hasCA_ik, hasCA_puk, hasP_pD, hasTC_pID, hasTC_mD, hasTC_euk, hasTC_pubk, hasMD_pwk, hasMD_mdk, hasRA_mk, hasRA_rand,
			hasCR_privk, computeCA_eck, computeCA_eik, computeMD_uk, computeMD_ck, computeMD_ik, computeMD_epD, computeMD_emD, computeMD_cemdk,
			computeMD_emdk, computeRA_memdk, computeRA_ip, computeCR_mdk, computeCR_amD, computeR_stats, receiveMDCA, receiveTCP, receiveCRMD,
			receiveRCR, receiveIDBRA, receiveRDBRA, spotcheckMTC, spotcheckMCR).collect(Collectors.toCollection(LinkedHashSet::new));
	// Dependencies
	//TODO more and more practical values?
	private static Dep patient_dep1 = new Dep(cki, Set.of(eckik, ukik), 1);
	private static Dep patient_dep2 = new Dep(iki, Set.of(eikik, ukik), 1);
	private static Dep patient_dep3 = new Dep(ukik, Set.of(eukik, pwkik), 1);
	private static Dep patient_dep4 = new Dep(mdkj, Set.of(emdkj, privk), 1);
	private static Dep patient_dep5 = new Dep(ipj, Set.of(rand_seedj), 1);
	private static Dep patient_dep6 = new Dep(pukik, Collections.emptySet(), 1);
	private static Dep patient_dep7 = new Dep(pubk, Collections.emptySet(), 1);
	private static Dep patient_dep8 = new Dep(mk, Collections.emptySet(), 0.0001);
	private static Dep patient_dep9 = new Dep(cki, Collections.emptySet(), 0.0001);
	private static Dep patient_dep10 = new Dep(iki, Collections.emptySet(), 0.0001);
	private static Dep patient_dep11 = new Dep(rand_seedj, Collections.emptySet(), 0.0001);
	private static Dep patient_dep12 = new Dep(pwkik, Collections.emptySet(), 0.0001);
	private static Dep patient_dep13 = new Dep(mdkj, Collections.emptySet(), 0.0001);
	private static Dep patient_dep14 = new Dep(privk, Collections.emptySet(), 0.0001);
	private static Set<DependenceRelation> dSet3 = Stream.of(
			new DependenceRelation(CAi, patient_dep1), new DependenceRelation(CAi, patient_dep2),
			new DependenceRelation(CAi, patient_dep3), new DependenceRelation(CAi, patient_dep4),
			new DependenceRelation(CAi, patient_dep5), new DependenceRelation(CAi, patient_dep6),
			new DependenceRelation(CAi, patient_dep7), new DependenceRelation(CAi, patient_dep8),
			new DependenceRelation(CAi, patient_dep9), new DependenceRelation(CAi, patient_dep10),
			new DependenceRelation(CAi, patient_dep11), new DependenceRelation(CAi, patient_dep12),
			new DependenceRelation(CAi, patient_dep13), new DependenceRelation(CAi, patient_dep14),
			new DependenceRelation(MDik, patient_dep1), new DependenceRelation(MDik, patient_dep2),
			new DependenceRelation(MDik, patient_dep3), new DependenceRelation(MDik, patient_dep4),
			new DependenceRelation(MDik, patient_dep5), new DependenceRelation(MDik, patient_dep6),
			new DependenceRelation(MDik, patient_dep7), new DependenceRelation(MDik, patient_dep8),
			new DependenceRelation(MDik, patient_dep9), new DependenceRelation(MDik, patient_dep10),
			new DependenceRelation(MDik, patient_dep11), new DependenceRelation(MDik, patient_dep12),
			new DependenceRelation(MDik, patient_dep13), new DependenceRelation(MDik, patient_dep14),
			new DependenceRelation(TCi, patient_dep1), new DependenceRelation(TCi, patient_dep2),
			new DependenceRelation(TCi, patient_dep3), new DependenceRelation(TCi, patient_dep4),
			new DependenceRelation(TCi, patient_dep5), new DependenceRelation(TCi, patient_dep6),
			new DependenceRelation(TCi, patient_dep7), new DependenceRelation(TCi, patient_dep8),
			new DependenceRelation(TCi, patient_dep9), new DependenceRelation(TCi, patient_dep10),
			new DependenceRelation(TCi, patient_dep11), new DependenceRelation(TCi, patient_dep12),
			new DependenceRelation(TCi, patient_dep13), new DependenceRelation(TCi, patient_dep14),
			new DependenceRelation(RA, patient_dep1), new DependenceRelation(RA, patient_dep2),
			new DependenceRelation(RA, patient_dep3), new DependenceRelation(RA, patient_dep4),
			new DependenceRelation(RA, patient_dep5), new DependenceRelation(RA, patient_dep6),
			new DependenceRelation(RA, patient_dep7), new DependenceRelation(RA, patient_dep8),
			new DependenceRelation(RA, patient_dep9), new DependenceRelation(RA, patient_dep10),
			new DependenceRelation(RA, patient_dep11), new DependenceRelation(RA, patient_dep12),
			new DependenceRelation(RA, patient_dep13), new DependenceRelation(RA, patient_dep14),
			new DependenceRelation(CR, patient_dep1), new DependenceRelation(CR, patient_dep2),
			new DependenceRelation(CR, patient_dep3), new DependenceRelation(CR, patient_dep4),
			new DependenceRelation(CR, patient_dep5), new DependenceRelation(CR, patient_dep6),
			new DependenceRelation(CR, patient_dep7), new DependenceRelation(CR, patient_dep8),
			new DependenceRelation(CR, patient_dep9), new DependenceRelation(CR, patient_dep10),
			new DependenceRelation(CR, patient_dep11), new DependenceRelation(CR, patient_dep12),
			new DependenceRelation(CR, patient_dep13), new DependenceRelation(CR, patient_dep14),
			new DependenceRelation(Rl, patient_dep1), new DependenceRelation(Rl, patient_dep2),
			new DependenceRelation(Rl, patient_dep3), new DependenceRelation(Rl, patient_dep4),
			new DependenceRelation(Rl, patient_dep5), new DependenceRelation(Rl, patient_dep6),
			new DependenceRelation(Rl, patient_dep7), new DependenceRelation(Rl, patient_dep8),
			new DependenceRelation(Rl, patient_dep9), new DependenceRelation(Rl, patient_dep10),
			new DependenceRelation(Rl, patient_dep11), new DependenceRelation(Rl, patient_dep12),
			new DependenceRelation(Rl, patient_dep13), new DependenceRelation(Rl, patient_dep14)).collect(Collectors.toCollection(LinkedHashSet::new));
	// Deductions
	private static DeductionCapability dc_CAi = new DeductionCapability(CAi, Set.of(deduc4));
	private static DeductionCapability dc_TCi = new DeductionCapability(TCi, Set.of(deduc4));
	private static DeductionCapability dc_MDik = new DeductionCapability(MDik, Set.of(deduc4));
	private static DeductionCapability dc_RA = new DeductionCapability(RA, Set.of(deduc4));
	private static DeductionCapability dc_CR = new DeductionCapability(CR, Set.of(deduc4));
	private static DeductionCapability dc_Rl = new DeductionCapability(Rl, Set.of(deduc4));
	private static Set<DeductionCapability> dedSet3 = Stream.of(dc_CAi, dc_TCi, dc_MDik, dc_RA, dc_CR, dc_Rl).collect(Collectors.toCollection(LinkedHashSet::new));
	// Statements
	private static Property patient_prop1 = new Property(PropertyType.HAS, Rl, (double)1, pDj);
	private static Property patient_prop2 = new Property(PropertyType.NEGATION, patient_prop1);
	//TODO more?
	private static Set<Property> pSet3 = Stream.of(patient_prop2).collect(Collectors.toCollection(LinkedHashSet::new));


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
			// first case study: smart energy metering
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
		case AW:
			// second case study: accuweather ios app
			archFunc.setcSet(cSet2);
			archFunc.setvSet(vSet2);
			archFunc.settSet(tSet2);
			archFunc.seteSet(eSet2);
			archFunc.settrustSet(trustSet2);
			archFunc.setstSet(stSet2);
			archFunc.setaSet(aSet2);
			archFunc.setdSet(dSet2);
			archFunc.setdedSet(dedSet2);
			archFunc.setpSet(pSet2);
			break;
		case PDR:
			// third case study: patient data register
			archFunc.setcSet(cSet3);
			archFunc.setvSet(vSet3);
			archFunc.settSet(tSet3);
			archFunc.seteSet(eSet3);
			archFunc.settrustSet(trustSet3);
			archFunc.setstSet(stSet3);
			archFunc.setaSet(aSet3);
			archFunc.setdSet(dSet3);
			archFunc.setdedSet(dedSet3);
			archFunc.setpSet(pSet3);
			archFunc.setcomposSet(composSet);
			break;
		default:
			break;
		}
	}

}
