package solver;

import architecture.Architecture;
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

public class PrologHandler implements Handler {

	private Environment env;
	private Interpreter interpreter;

	public PrologHandler(Architecture arch, String facts) {
		// setup the environment for the gnu prolog java api
		env = new Environment();
		// load the fixed set of rules
		env.ensureLoaded(AtomTerm.get("C://Users/kaiba/OneDrive/Dokumente/Prolog/capverde_rules.pl"));
		// now create and load the set of fact representing the architecture
		env.ensureLoaded(AtomTerm.get(facts));
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
		} catch (PrologException | IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
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
