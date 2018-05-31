package properties;

import java.util.List;
import java.util.Set;

import architecture.Action;
import architecture.Architecture;
import solver.SmtHandler;
import utils.SuccessIndexPair;

/**
 * A bottom-up parser based on SMT solver.
 */
public class SmtParser extends Parser {
	
	// class fields
	private Set<Property> properties;
	private SmtHandler smt;
	private List<Action> actionLog;
	
	public SmtParser(Architecture arch) {
		super(arch);
		smt = new SmtHandler(arch);
	}

	@Override
	public boolean verifyStatement(Property statement, int recurseDepth) {
		// TODO
		return false;
	}

}
