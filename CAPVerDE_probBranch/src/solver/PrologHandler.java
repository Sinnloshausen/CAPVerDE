package solver;

import java.util.ArrayList;
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
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;
import properties.Property;
import utils.FileHandler;

public class PrologHandler implements Handler {

	private Environment env;
	private Interpreter interpreter;

	public PrologHandler(Architecture arch) {
		// setup the environment for the gnu prolog java api
		env = new Environment();
		// load the fixed set of rules
		env.ensureLoaded(AtomTerm.get("C://Users/kaiba/OneDrive/Dokumente/Prolog/capverde_rules.pl"));
		// now create and load the set of fact representing the architecture
		env.ensureLoaded(AtomTerm.get(parseArch(arch)));
		// finish the setup of the environment
		interpreter = env.createInterpreter();
		env.runInitialization(interpreter);	
	}

	@Override
	public boolean verify(Property prop) {
		// TODO finish
		// generate the right term from the property
		Term goalTerm = parseProperty(prop);
		// run the prolog goal
		int rc;
		try {
			rc = interpreter.runOnce(goalTerm);
			if (rc == PrologCode.SUCCESS || rc == PrologCode.SUCCESS_LAST) {
				return true;
			}
		} catch (PrologException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private String parseArch(Architecture arch) {
		// TODO finish
		List<Term> facts = new ArrayList<Term>();
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

		//TODO replace generic hard-coded return
		String factsString = write2string(facts);
		FileHandler file = new FileHandler("C:/Users/kaiba/OneDrive/Dokumente/Prolog", "facts.pl");
		file.writeFile(factsString.getBytes());
		return "C:/Users/kaiba/OneDrive/Dokumente/Prolog/facts";
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

	private Term parseProperty(Property prop) {
		// TODO test
		Term term = null;
		switch (prop.getType()) {
		case CONJUNCTION:
			//TODO test this!
			Term[] properties = {parseProperty(prop.getSt1()), parseProperty(prop.getSt2())};
			term = new CompoundTerm(AtomTerm.get(","), properties);
			break;
		case HAS:
			Term[] argsH = {AtomTerm.get(prop.getOwner().toString()), AtomTerm.get(prop.getVar().toString()), new FloatTerm(prop.getProb())};
			term = new CompoundTerm(AtomTerm.get("hasProp"), argsH);
			break;
		case KNOWS:
			Term[] argsK = {AtomTerm.get(prop.getOwner().toString()), AtomTerm.get(prop.getEq().getName()), new FloatTerm(prop.getProb())};
			term = new CompoundTerm(AtomTerm.get("kProp"), argsK);
			break;
		case NEGATION:
			//TODO test this!
			Term[] positive = {parseProperty(prop.getSt1())};
			term = new CompoundTerm(AtomTerm.get("\\+"), positive);
			break;
		case NOTSHARED:
			Term[] argsSh = {AtomTerm.get(prop.getOwner().toString()), AtomTerm.get(prop.getVar().toString())};
			term = new CompoundTerm(AtomTerm.get("notShared"), argsSh);
			break;
		case NOTSTORED:
			Term[] argsSt = {AtomTerm.get(prop.getOwner().toString()), AtomTerm.get(prop.getVar().toString()), new IntegerTerm(prop.getBound())};
			term = new CompoundTerm(AtomTerm.get("notStored"), argsSt);
			break;
		default:
			break;
		}
		return term;
	}

}
