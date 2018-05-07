package properties;

import java.util.List;
import java.util.Set;

import architecture.Action;
import architecture.Architecture;
import solver.SmtHandler;

/**
 * A bottom-up parser based on SMT solver.
 */
public class SmtParser implements Parser {
	
	// class fields
	private Architecture arch;
	private Set<Property> properties;
	private SmtHandler smt;
	private List<Action> actionLog;
	
	public SmtParser(Architecture arch) {
		this.arch = arch;
		smt = new SmtHandler(arch);
	}

	@Override
	public boolean verifyStatement(Property statement, int recurseDepth) {
		// TODO
		return false;
	}

}
