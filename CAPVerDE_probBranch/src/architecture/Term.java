package architecture;

import architecture.Equation.Relation;
import architecture.Equation.Type;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * Object describes a term containing more terms and/or atoms.
 * A term can be composed of 0-3 subterms.
 */
public class Term implements Serializable {

	/**
	 * @serial Serial ID for storing architecture objects in files.
	 */
	private static final long serialVersionUID = -1365683991583515982L;

	/**
	 * The type of term (atom or composition).
	 */
	public enum TermType {
		ATOM, COMPOSITION
	}

	/**
	 * The type of operator unary/binary/tertiary.
	 */
	public enum OperatorType {
		BINARY, UNARY, TERTIARY
	}

	/**
	 * The explicit binary operators (elementary arithmetic operators or functions).
	 */
	public enum Operator {
		ADD, MULT, SUB, DIV, FUNC
	}

	// CLass fields
	private TermType type;
	private OperatorType opType;
	private Operator op;
	private String funcName;
	private Variable atom;
	private Term t1;
	private Term t2;
	private Term t3;
	private Set<Variable> atomSet;
	private Set<Variable> completeSet;
	private String callHistory;
	private boolean matchVar;
	private int depth;

	/**
	 * The full constructor for a term.
	 * Typically only invoked inside this class by the specific constructors.
	 * 
	 * @param type
	 *          the type of term: atom/composition
	 * @param opType
	 *          the type of operator: unary/binary
	 * @param op
	 *          the explicit binary operator
	 * @param funcName
	 *          the name of the unary operator
	 * @param atom
	 *          the atom
	 * @param t1
	 *          the first term
	 * @param t2
	 *          the second term
	 * @param t3
	 *          the third term
	 * @param matchVar
	 *          a flag that indicates if the term is only a variable for pattern
	 *          matching
	 */
	public Term(TermType type, OperatorType opType, Operator op, String funcName, Variable atom,
			Term t1, Term t2, Term t3, boolean matchVar) {
		// default Constructor
		this.type = type;
		this.opType = opType;
		this.op = op;
		this.funcName = funcName;
		this.atom = atom;
		this.t1 = t1;
		this.t2 = t2;
		this.t3 = t3;
		this.atomSet = new LinkedHashSet<Variable>();
		this.callHistory = "";
		this.matchVar = matchVar;

		// Collect all atom in this term
		collectAtoms(this);
		// Also collect the function-composed term, like f(x)
		this.completeSet = new LinkedHashSet<Variable>(atomSet);
		collectFuncs(this);
		// calculate the nested functions
		if (!matchVar) {
			depth = checkDepth(this);
		} else {
			depth = 0;
		}
	}

	/**
	 * The copy constructor of a term.
	 * 
	 * @param original
	 *          the term to copy
	 */
	public Term(Term original) {
		this(original.getType(), original.getOpType(), original.getOp(), original.getFuncName(),
				original.getAtom(), original.getT1(), original.getT2(), original.getT3(),
				original.isMatchVar());
	}

	/**
	 * Constructor called for type = atom.
	 * 
	 * @param type
	 *          the type of term: atom
	 * @param atom
	 *          the atom
	 * @param matchVar
	 *          a flag indicating if this variable is only used for pattern
	 *          matching
	 */
	public Term(TermType type, Variable atom, boolean matchVar) {
		// When type = ATOM
		this(type, null, null, null, atom, null, null, null, matchVar);
	}

	/**
	 * Constructor called for type = unary.
	 * 
	 * @param type
	 *          the type of term: composition
	 * @param opType
	 *          the type of operator: unary
	 * @param op
	 *          the explicit binary operator
	 * @param funcName
	 *          the name of the unary operator
	 * @param t1
	 *          the sub-term
	 * @param matchVar
	 *          a flag indicating if this variable is only used for pattern
	 *          matching
	 */
	public Term(TermType type, OperatorType opType, Operator op, String funcName, Term t1,
			boolean matchVar) {
		// When operatorType = UNARY
		// Here is a custom function name needed, e.g. 'f'
		// It should be defined whether the custom function is invertible
		this(type, opType, op, funcName, null, t1, null, null, matchVar);
	}

	/**
	 * Constructor called for type = binary.
	 * 
	 * @param type
	 *          the type of term: composition
	 * @param opType
	 *          the type of operator: binary
	 * @param op
	 *          the explicit binary operator
	 * @param funcName
	 *          the name of the function
	 * @param t1
	 *          the first term
	 * @param t2
	 *          the second term
	 * @param matchVar
	 *          a flag indicating if this variable is only used for pattern
	 *          matching
	 */
	public Term(TermType type, OperatorType opType, Operator op, String funcName, Term t1, Term t2,
			boolean matchVar) {
		// When operatorType = BINARY
		// Binary operators are assumed to be non-invertible
		this(type, opType, op, funcName, null, t1, t2, null, matchVar);
	}

	/**
	 * Constructor called for type = tertiary.
	 * 
	 * @param type
	 *          the type of term: composition
	 * @param opType
	 *          the type of operator: binary
	 * @param op
	 *          the explicit binary operator
	 * @param funcName
	 *          the name of the function
	 * @param t1
	 *          the first term
	 * @param t2
	 *          the second term
	 * @param t3
	 *          the third term
	 * @param matchVar
	 *          a flag indicating if this variable is only used for pattern
	 *          matching
	 */
	public Term(TermType type, OperatorType opType, Operator op, String funcName,
			Term t1, Term t2, Term t3, boolean matchVar) {
		// When operatorType = BINARY
		// Binary operators are assumed to be non-invertible
		this(type, opType, op, funcName, null, t1, t2, t3, matchVar);
	}

	@Override
	public String toString() {
		return term2string(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((atom == null) ? 0 : atom.hashCode());
		result = prime * result + ((funcName == null) ? 0 : funcName.hashCode());
		result = prime * result + (matchVar ? 1231 : 1237);
		result = prime * result + ((op == null) ? 0 : op.hashCode());
		result = prime * result + ((opType == null) ? 0 : opType.hashCode());
		result = prime * result + ((t1 == null) ? 0 : t1.hashCode());
		result = prime * result + ((t2 == null) ? 0 : t2.hashCode());
		result = prime * result + ((t3 == null) ? 0 : t3.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Term other = (Term) obj;
		if (atom == null) {
			if (other.atom != null)
				return false;
		} else if (!atom.equals(other.atom))
			return false;
		if (funcName == null) {
			if (other.funcName != null)
				return false;
		} else if (!funcName.equals(other.funcName))
			return false;
		if (matchVar != other.matchVar)
			return false;
		if (op != other.op)
			return false;
		if (opType != other.opType)
			return false;
		if (t1 == null) {
			if (other.t1 != null)
				return false;
		} else if (!t1.equals(other.t1))
			return false;
		if (t2 == null) {
			if (other.t2 != null)
				return false;
		} else if (!t2.equals(other.t2))
			return false;
		if (t3 == null) {
			if (other.t3 != null)
				return false;
		} else if (!t3.equals(other.t3))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	/**
	 * Helper method to form a string describing the term.
	 * 
	 * @param term
	 *          the term
	 * @return the string
	 */
	private String term2string(Term term) {
		String string = "";
		switch (term.type) {
		case ATOM:
			string = term.atom.getName();
			break;
		case COMPOSITION:
			switch (term.op) {
			case ADD:
				string = term2string(term.t1) + " + " + term2string(term.t2);
				break;
			case MULT:
				string = term2string(term.t1) + " * " + term2string(term.t2);
				break;
			case SUB:
				string = term2string(term.t1) + " - " + term2string(term.t2);
				break;
			case DIV:
				string = term2string(term.t1) + " / " + term2string(term.t2);
				break;
			case FUNC:
				switch (term.opType) {
				case BINARY:
					string = term.funcName + "(" + term2string(term.t1)
					+ ", " + term2string(term.t2) + ")";
					break;
				case TERTIARY:
					string = term.funcName + "(" + term2string(term.t1)
					+ ", " + term2string(term.t2) + ", " + term2string(term.t3) + ")";
					break;
				case UNARY:
					string = term.funcName + "(" + term2string(term.t1) + ")";
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
		return string;
	}

	/**
	 * Helper method to collect all atoms from a term and put them into its list of atoms.
	 * 
	 * @param term
	 *          the term
	 */
	private void collectAtoms(Term term) {
		// Put all atom into the list
		switch (term.type) {
		case ATOM:
			addAtom(term.atom);
			break;
		case COMPOSITION:
			switch (term.opType) {
			case UNARY:
				collectAtoms(term.t1);
				break;
			case BINARY:
				collectAtoms(term.t1);
				collectAtoms(term.t2);
				break;
			case TERTIARY:
				collectAtoms(term.t1);
				collectAtoms(term.t2);
				collectAtoms(term.t3);
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Helper method to collect all sub-terms that are unary function calls and put them
	 * into a list.
	 * 
	 * @param term
	 *          the term
	 */
	private void collectFuncs(Term term) {
		// Collect all function-composed terms, e.g. xf for f(x)
		switch (term.type) {
		case ATOM:
			// Add the call history to the variable name
			Variable tmp = new Variable(term.atom.getName() + term.callHistory);
			addComplete(tmp);
			break;
		case COMPOSITION:
			switch (term.opType) {
			case UNARY:
				// Add function name to call history
				term.t1.callHistory = term.funcName + term.callHistory;
				// Recursive call
				collectFuncs(term.t1);
				break;
			case BINARY:
				// Pass on call history
				term.t1.callHistory = term.callHistory;
				term.t2.callHistory = term.callHistory;
				// Recursive call
				collectFuncs(term.t1);
				collectFuncs(term.t2);
				break;
			case TERTIARY:
				// Pass on call history
				term.t1.callHistory = term.callHistory;
				term.t2.callHistory = term.callHistory;
				term.t3.callHistory = term.callHistory;
				// Recursive call
				collectFuncs(term.t1);
				collectFuncs(term.t2);
				collectFuncs(term.t3);
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Helper method to get the complexity of a term, i.e., the depth of nested functions.
	 * @param term
	 * 		  the term
	 * @return
	 * 		  the depth
	 */
	private int checkDepth(Term term) {
		int d = 0;
		if (term == null) {
			return d;
		}
		switch (term.type) {
		case ATOM:
			return d;
		case COMPOSITION:
			return d + 1 + Math.max(Math.max(checkDepth(term.getT1()), checkDepth(term.getT2())),
					checkDepth(term.getT3()));
		default:
			break;
		}
		return -1;
	}

	/**
	 * Method that recursively matches a term and a pattern term.
	 * 
	 * @param pattern
	 *          the pattern to match this equation with
	 * @return a list of equations representing the matches
	 */
	public Set<Equation> match(Term pattern) {
		// pattern term is matchVar
		if (type != pattern.type) {
			if (type == TermType.ATOM) {
				return Collections.emptySet();
			}
			if (pattern.type == TermType.ATOM) {
				return Set.of(
						new Equation("subst", Type.RELATION, Relation.EQUALITY, pattern, this));
			}
			switch (opType) {
			case BINARY:
				// recursively match the sub-terms
				Set<Equation> matches = new LinkedHashSet<Equation>();
				matches.addAll(t1.match(pattern));
				matches.addAll(t2.match(pattern));
				return matches;
			case UNARY:
				// recursively match the sub-term
				return t1.match(pattern);
			case TERTIARY:
				// recursively match the sub-terms
				Set<Equation> matches2 = new LinkedHashSet<Equation>();
				matches2.addAll(t1.match(pattern));
				matches2.addAll(t2.match(pattern));
				matches2.addAll(t3.match(pattern));
				return matches2;
			default:
				break;
			}
		}
		// recursively check the rest
		switch (type) {
		case ATOM:
			return Set.of(
					new Equation("subst", Type.RELATION, Relation.EQUALITY, pattern, this));
		case COMPOSITION:
			switch (opType) {
			case BINARY:
				// recursively match the sub-terms
				Set<Equation> matches = new LinkedHashSet<Equation>();
				matches.addAll(t1.match(pattern.t1));
				matches.addAll(t2.match(pattern.t2));
				return matches;
			case UNARY:
				// recursively match the sub-term
				return t1.match(pattern.t1);
			case TERTIARY:
				// recursively match the sub-terms
				Set<Equation> matches2 = new LinkedHashSet<Equation>();
				matches2.addAll(t1.match(pattern.t1));
				matches2.addAll(t2.match(pattern.t2));
				matches2.addAll(t3.match(pattern.t3));
				return matches2;
			default:
				break;
			}
			break;
		default:
			break;
		}
		return null;
	}

	// Getter and setter methods
	public Set<Variable> getAtomSet() {
		return atomSet;
	}

	/*public void setAtomSet(Set<Variable> atomSet) {
		this.atomSet = atomSet;
	}*/

	public void addAtom(Variable atom) {
		this.atomSet.add(atom);
	}

	public TermType getType() {
		return type;
	}

	public void setType(TermType type) {
		this.type = type;
	}

	public OperatorType getOpType() {
		return opType;
	}

	public void setOpType(OperatorType opType) {
		this.opType = opType;
	}

	public Variable getAtom() {
		return atom;
	}

	public void setAtom(Variable atom) {
		this.atom = atom;
	}

	public Term getT1() {
		return t1;
	}

	public void setT1(Term t1) {
		this.t1 = t1;
		// update the complexity
		depth = checkDepth(this);
	}

	public Term getT2() {
		return t2;
	}

	public void setT2(Term t2) {
		this.t2 = t2;
		// update the complexity
		depth = checkDepth(this);
	}

	public void setT3(Term t3) {
		this.t3 = t3;
		// update the complexity
		depth = checkDepth(this);
	}

	public Term getT3() {
		return t3;
	}

	public Operator getOp() {
		return op;
	}

	public void setOp(Operator op) {
		this.op = op;
	}

	public String getFuncName() {
		return funcName;
	}

	public void setFuncName(String funcName) {
		this.funcName = funcName;
	}

	public Set<Variable> getCompleteSet() {
		return completeSet;
	}

	/*public void setCompleteSet(Set<Variable> completeSet) {
		this.completeSet = completeSet;
	}*/

	public void addComplete(Variable var) {
		this.completeSet.add(var);
	}

	public boolean isMatchVar() {
		return matchVar;
	}

	public void setMatchVar(boolean matchVar) {
		this.matchVar = matchVar;
	}

	public int getDepth() {
		return depth;
	}

}
