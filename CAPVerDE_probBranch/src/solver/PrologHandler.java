package solver;

import architecture.Architecture;
import gnu.prolog.io.TermWriter;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.FloatTerm;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.Interpreter.Goal;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;
import properties.Property;

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
		// TODO Auto-generated method stub
		return null;
	}

	private Term parseProperty(Property prop) {
		// TODO Auto-generated method stub
		return null;
	}

}
