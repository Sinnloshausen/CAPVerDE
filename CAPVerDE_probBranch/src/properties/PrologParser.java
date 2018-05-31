package properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import architecture.Action;
import architecture.Architecture;
import architecture.Component;
import architecture.Deduction;
import architecture.Dep;
import architecture.Equation;
import architecture.Variable;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.FloatTerm;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.Term;
import solver.PrologHandler;
import utils.FileHandler;

public class PrologParser extends Parser {

	private PrologHandler prolog;

	public PrologParser(Architecture arch) {
		super(arch);
		// TODO Auto-generated constructor stub
		System.out.println("Test. This should be visible...");
		this.prolog = new PrologHandler(arch, parseArch(arch));
	}

	@Override
	public boolean verifyStatement(Property statement, int recurseDepth) {
		// TODO Auto-generated method stub
		return prolog.verify(statement);
	}

	private String parseArch(Architecture arch) {
		// TODO all done?
		List<Term> facts = new ArrayList<Term>();
		// first create dummy facts for all possible relations
		facts.addAll(dummyFacts());
		// first add the left-hand-side relations of all equations
		for (Equation e : arch.getAllEquations()) {
			facts.add(parseEquation(e));
		}
		// then add the dependence relations, deduction capabilities and counter
		for (Component c : arch.getCompList()) {
			// add the counter value for all components
			for (Variable v : arch.getVariables()) {
				facts.add(parseVar(c, v));
			}
			for (Dep dep : c.getDepSet()) {
				// add the dependencies
				facts.add(parseDep(c, dep));
			}
			for (Deduction ded : c.getDeductionCapability()) {
				// add the deductions
				facts.add(parseDed(c, ded));
			}
		}
		// go through all actions and add them as facts
		for (Action a : arch.getAllActions()) {
			// parse the actions ins the architecture, one after the other
			facts.add(parseAction(a));
		}

		// sort the list to have all facts of same type bundled
		Collections.sort(facts, new Comparator<Term>() {
			@Override
			public int compare(Term o1, Term o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		String factsString = write2string(facts);
		FileHandler file = new FileHandler("C:/Users/kaiba/OneDrive/Dokumente/Prolog", "facts.pl");
		file.writeFile(factsString.getBytes());
		return "C:/Users/kaiba/OneDrive/Dokumente/Prolog/facts";
	}

	private List<Term> dummyFacts() {
		// TODO test this
		List<Term> list = new ArrayList<Term>();
		Term[] list2 = {AtomTerm.get("dummy1"), AtomTerm.get("dummy2")};
		Term[] list3 = {AtomTerm.get("dummy1"), AtomTerm.get("dummy2"), AtomTerm.get("dummy3")};
		Term[] list4 = {AtomTerm.get("dummy1"), AtomTerm.get("dummy2"), AtomTerm.get("dummy3"), AtomTerm.get("dummy4")};
		Term termHas = new CompoundTerm(AtomTerm.get("has"), list2);
		list.add(termHas);
		Term termReceive = new CompoundTerm(AtomTerm.get("receive"), list3);
		list.add(termReceive);
		Term termCompute = new CompoundTerm(AtomTerm.get("compute"), list2);
		list.add(termCompute);
		Term termLhs = new CompoundTerm(AtomTerm.get("lhs"), list2);
		list.add(termLhs);
		Term termDep = new CompoundTerm(AtomTerm.get("dep"), list4);
		list.add(termDep);
		Term termCheckA = new CompoundTerm(AtomTerm.get("checkA"), list2);
		list.add(termCheckA);
		Term termVerif = new CompoundTerm(AtomTerm.get("verif"), list2);
		list.add(termVerif);
		Term termProof = new CompoundTerm(AtomTerm.get("proof"), list2);
		list.add(termProof);
		Term termAttest = new CompoundTerm(AtomTerm.get("attest"), list2);
		list.add(termAttest);
		Term termContains = new CompoundTerm(AtomTerm.get("contains"), list2);
		list.add(termContains);
		Term termTrust = new CompoundTerm(AtomTerm.get("trust"), list2);
		list.add(termTrust);
		Term termDed = new CompoundTerm(AtomTerm.get("ded"), list4);
		list.add(termDed);
		Term termCounter = new CompoundTerm(AtomTerm.get("counter"), list3);
		list.add(termCounter);
		return list;
	}

	private Term parseVar(Component comp, Variable var) {
		// TODO complete this!
		Term[] args = {AtomTerm.get(comp.getName()), AtomTerm.get(var.getName()), new IntegerTerm(comp.getCounter(var))};
		// create a counter term for the component and the variable
		return new CompoundTerm(AtomTerm.get("counter"), args);
	}

	private Term parseDed(Component comp, Deduction ded) {
		// TODO test this!
		Term[] listArgs = new Term[ded.getPremises().size() + 1];
		int i = 0;
		for (Equation e : ded.getPremises()) {
			listArgs[i] = AtomTerm.get(e.getName());
			i++;
		}
		listArgs[i] = AtomTerm.get("[]");
		Term list = new CompoundTerm(AtomTerm.get("."), listArgs);
		Term[] args = {AtomTerm.get(comp.getName()), new FloatTerm(ded.getProb()), AtomTerm.get(ded.getConclusion().getName()), list};
		return new CompoundTerm(AtomTerm.get("ded"), args);
	}

	private Term parseDep(Component comp, Dep dep) {
		// TODO test this!
		Term[] listArgs = new Term[dep.getVarSet().size() + 1];
		int i = 0;
		for (Variable v : dep.getVarSet()) {
			listArgs[i] = AtomTerm.get(v.getName());
			i++;
		}
		listArgs[i] = AtomTerm.get("[]");
		Term list = new CompoundTerm(AtomTerm.get("."), listArgs);
		Term[] args = {AtomTerm.get(comp.getName()), new FloatTerm(dep.getProb()), AtomTerm.get(dep.getVar().getName()), list};
		return new CompoundTerm(AtomTerm.get("dep"), args);
	}

	private String write2string(List<Term> facts) {
		// TODO test this!
		String string = "";
		for (Term t : facts) {
			string += t.toString() + "." + System.lineSeparator();
		}
		return string;
	}

	private Term parseAction(Action a) {
		// TODO finish
		Term term = null;
		switch (a.getAction()) {
		case CHECK:
			//TODO test this
			Term[] eqargs = new Term[a.getEqSet().size() + 1];
			int i = 0;
			for (Equation e : a.getEqSet()) {
				eqargs[i] = AtomTerm.get(e.getName());
				i++;
			}
			eqargs[i] = AtomTerm.get("[]");
			Term checklist = new CompoundTerm(AtomTerm.get("."), eqargs);
			Term[] argsCh = {AtomTerm.get(a.getComponent().toString()), checklist};
			term = new CompoundTerm(AtomTerm.get("checkA"), argsCh);
			break;
		case COMPUTE:
			//TODO also add left-hand-side and contains as facts
			Term[] argsCo = {AtomTerm.get(a.getComponent().toString()), AtomTerm.get(a.getEq().getName())};
			term = new CompoundTerm(AtomTerm.get("compute"), argsCo);
			break;
		case DELETE:
			Term[] argsD = {AtomTerm.get(a.getComponent().toString()), AtomTerm.get(a.getVar().getName())};
			term = new CompoundTerm(AtomTerm.get("delete"), argsD);
			break;
		case HAS:
			Term[] argsH = {AtomTerm.get(a.getComponent().toString()), AtomTerm.get(a.getVar().getName())};
			term = new CompoundTerm(AtomTerm.get("has"), argsH);
			break;
		case RECEIVE:
			//TODO test this
			Term[] varargs = new Term[a.getVarSet().size() + 1];
			int j = 0;
			for (Variable v : a.getVarSet()) {
				varargs[j] = AtomTerm.get(v.toString());
				j++;
			}
			varargs[j] = AtomTerm.get("[]");
			Term varlist = new CompoundTerm(AtomTerm.get("."), varargs);
			Term[] argsR = {AtomTerm.get(a.getComponent().toString()), AtomTerm.get(a.getComPartner().toString()), varlist};
			term = new CompoundTerm(AtomTerm.get("receive"), argsR);
			break;
		case TRUST:
			Term[] argsT = {AtomTerm.get(a.getComponent().toString()), AtomTerm.get(a.getComPartner().toString())};
			term = new CompoundTerm(AtomTerm.get("trust"), argsT);
			break;
		case VERIF_A:
			Term[] argsVa = {AtomTerm.get(a.getComponent().toString()), AtomTerm.get(a.getAtt().getName())};
			term = new CompoundTerm(AtomTerm.get("verif"), argsVa);
			break;
		case VERIF_P:
			Term[] argsVp = {AtomTerm.get(a.getComponent().toString()), AtomTerm.get(a.getPro().getName())};
			term = new CompoundTerm(AtomTerm.get("verif"), argsVp);
			break;
		default:
			break;
		}

		return term;
	}

	private Term parseEquation(Equation e) {
		// TODO Auto-generated method stub
		Term[] args = {AtomTerm.get(e.getName()), AtomTerm.get(e.getLefthandSide().getName())};
		// create a lhs term for the equation that denotes the right hand side
		return new CompoundTerm(AtomTerm.get("lhs"), args);
	}

}
