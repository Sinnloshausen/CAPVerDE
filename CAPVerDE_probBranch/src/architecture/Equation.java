package architecture;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Objects that represent equations as in a=b, a&ltb, etc.
 */
public class Equation implements P, Serializable {

	/**
	 * @serial Serial ID for storing architecture objects in files.
	 */
	private static final long serialVersionUID = -404282076637818760L;

	/**
	 * Type of equations: simple relations like 'a=b',
	 * or conjunctions like 'a=b and c&gtd'.
	 */
	public enum Type {
		RELATION, CONJUNCTION, EMPTY
	}

	/**
	 * Type of relations between two terms like equality or less than.
	 */
	public enum Relation {
		EQUALITY, INEQUALITY, LESSTHAN, GREATERTHAN, LESSEQUAL, GREATEREQUAL
	}

	// Class fields
	private String name;
	private Type type;
	private Relation rel;
	private Term op1;
	private Term op2;
	private Equation eq1;
	private Equation eq2;
	private Set<Term> termSet;
	private Variable lefthandSide;
	private int depth;
	private Set<Variable> allAtoms;

	/**
	 * The full constructor for an equation
	 * typically only called within this class by the specific constructors.
	 * 
	 * @param name
	 *          the name of the equation
	 * @param type
	 *          the type of equation: relation / conjunction
	 * @param rel
	 *          the explicit relation
	 * @param op1
	 *          the left-hand side of the relation
	 * @param op2
	 *          the right-hand side of the relation
	 * @param eq1
	 *          the first equation
	 * @param eq2
	 *          the second equation
	 */
	public Equation(String name, Type type, Relation rel, Term op1, Term op2, Equation eq1,
			Equation eq2) {
		// Default Constructor
		this.name = name;
		this.type = type;
		this.termSet = new LinkedHashSet<Term>();
		this.allAtoms = new LinkedHashSet<Variable>();
		if (type != Type.EMPTY) {
			switch (type) {
			case RELATION:
				this.rel = rel;
				this.op1 = op1;
				this.op2 = op2;
				if (rel.equals(Relation.EQUALITY)) {
					// Get the left-hand side of a standard equation
					lefthandSide = new Variable(op1.toString());
				}
				break;
			case CONJUNCTION:
				this.eq1 = eq1;
				this.eq2 = eq2;
				break;
			default:
				// error?
				break;
			}
			// Fill the term list
			collectTerms(this);
			collectAtoms();
		}
		// check the depth of the equation, i.e., how many nested functions it includes
		depth = checkDepth(this);
	}

	/**
	 * Constructor called for type = relation.
	 * 
	 * @param name
	 *          the name
	 * @param type
	 *          the type of equation: relation
	 * @param rel
	 *          the explicit relation
	 * @param op1
	 *          the left-hand side of the relation
	 * @param op2
	 *          the right-hand side of the relation
	 */
	public Equation(String name, Type type, Relation rel, Term op1, Term op2) {
		// Called if type=RELATION
		this(name, type, rel, op1, op2, null, null);
	}

	/**
	 * Constructor called for type = conjunction.
	 * 
	 * @param name
	 *          the name
	 * @param type
	 *          the type of equation: conjunction
	 * @param eq1
	 *          the first equation
	 * @param eq2
	 *          the second equation
	 */
	public Equation(String name, Type type, Equation eq1, Equation eq2) {
		// Called if type=CONJUNCTION
		this(name, type, null, null, null, eq1, eq2);
	}

	/**
	 * Constructor called for empty equations.
	 * 
	 * @param name
	 *          the name of the equation
	 */
	public Equation(String name) {
		this(name, Type.EMPTY, null, null, null, null, null);
	}

	/**
	 * The copy constructor for an equation.
	 * 
	 * @param original
	 *          the original equation to be copied
	 * @param name
	 *          the name for the new equation
	 */
	public Equation(Equation original, String name) {
		this.name = name;
		type = original.getType();
		termSet = new LinkedHashSet<Term>();
		if (type != Type.EMPTY) {
			switch (type) {
			case RELATION:
				rel = original.getRel();
				op1 = new Term(original.getOp1());
				op2 = new Term(original.getOp2());
				if (rel.equals(Relation.EQUALITY)) {
					// Get the left-hand side of a standard equation
					lefthandSide = new Variable(op1.toString());
				}
				break;
			case CONJUNCTION:
				eq1 = new Equation(original.getEq1(), original.getEq1().getName());
				eq2 = new Equation(original.getEq2(), original.getEq2().getName());
				break;
			default:
				// error?
				break;
			}
			// Fill the term list
			collectTerms(this);
			//collectAtoms();
		}
		depth = original.depth;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eq1 == null) ? 0 : eq1.hashCode());
		result = prime * result + ((eq2 == null) ? 0 : eq2.hashCode());
		result = prime * result + ((op1 == null) ? 0 : op1.hashCode());
		result = prime * result + ((op2 == null) ? 0 : op2.hashCode());
		result = prime * result + ((rel == null) ? 0 : rel.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Equation other = (Equation) obj;
		if (type == null || other.type == null) {
			// empty equation equals everything
			return true;
		}
		if (eq1 == null) {
			if (other.eq1 != null) {
				return false;
			}
		} else if (!eq1.equals(other.eq1)) {
			return false;
		}
		if (eq2 == null) {
			if (other.eq2 != null) {
				return false;
			}
		} else if (!eq2.equals(other.eq2)) {
			return false;
		}
		if (op1 == null) {
			if (other.op1 != null) {
				return false;
			}
		} else if (!op1.equals(other.op1)) {
			return false;
		}
		if (op2 == null) {
			if (other.op2 != null) {
				return false;
			}
		} else if (!op2.equals(other.op2)) {
			return false;
		}
		if (rel != other.rel) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		// Return the equation as readable string
		return eq2string(this);
	}

	/**
	 * Helper method to form a string describing the equation.
	 * 
	 * @param eq
	 *          the equation
	 * @return the string
	 */
	private String eq2string(Equation eq) {
		switch (eq.type) {
		case RELATION:
			String relation = "";
			switch (eq.rel) {
			case EQUALITY:
				relation = "=";
				break;
			case INEQUALITY:
				relation = "!=";
				break;
			case LESSTHAN:
				relation = "<";
				break;
			case GREATERTHAN:
				relation = ">";
				break;
			case LESSEQUAL:
				relation = "<=";
				break;
			case GREATEREQUAL:
				relation = ">=";
				break;
			default:
				break;
			}
			return eq.op1 + " " + relation + " " + eq.op2;
		case CONJUNCTION:
			return eq2string(eq.eq1) + " AND " + eq2string(eq.eq2);
		case EMPTY:
			return name;
		default:
			// error?
			return "";
		}
	}

	/**
	 * Helper method to collect all terms from an equation and put them into a list.
	 * 
	 * @param eq
	 *          the equation
	 */
	private void collectTerms(Equation eq) {
		// Put all terms into the list
		switch (eq.type) {
		case RELATION:
			addTerm(eq.op1);
			addTerm(eq.op2);
			break;
		case CONJUNCTION:
			// Recursively add the terms of sub-equations
			collectTerms(eq.eq1);
			collectTerms(eq.eq2);
			break;
		default:
			break;
		}
	}
	
	/**
	 * Helper method to collect all atom variables from the two operands of this equation.
	 */
	private void collectAtoms() {
		// collect the unique atoms from both sides of the equation
		allAtoms.addAll(op1.getAtomSet());
		allAtoms.addAll(op2.getAtomSet());
	}
	
	/**
	 * Helper method to get the complexity of an equation, i.e., how many nested functions
	 * @param eq
	 * 		  the equation
	 * @return
	 * 		  the depth
	 */
	private int checkDepth(Equation eq) {
		switch (eq.getType()) {
		case CONJUNCTION:
			return checkDepth(eq.getEq1()) + checkDepth(eq.getEq2());
		case RELATION:
			return eq.getOp1().getDepth() + eq.getOp2().getDepth();
		default:
			break;
		}
		return 0;
	}

	/**
	 * Method for pattern matching equations, recursive version, using
	 * Term.match() method
	 * 
	 * @param pattern
	 *          pattern equation, e.g. x=h(y)
	 * @return a list of equations representing the matches, e.g. x=cp, y=p
	 */
	public Set<Equation> match2(Equation pattern) {
		if (type == Type.CONJUNCTION) {
			//TODO currently not supported
			return Collections.emptySet();
		}
		if (!pattern.op1.isMatchVar()) {
			// explicit term, must be equal
			if (!op1.equals(pattern.op1)) {
				return Collections.emptySet();
			}
			// now check the second term
			return op2.match(pattern.op2);
		}
		// op1 is a matchVar
		if (!pattern.op2.isMatchVar()) {
			// explicit term must be equal
			if (!op2.equals(pattern.op2)) {
				return Collections.emptySet();
			}
			// just check the first term
			return op1.match(pattern.op1);
		}
		// default: both terms are matchVars
		Set<Equation> matches = new LinkedHashSet<Equation>();
		matches.addAll(op1.match(pattern.op1));
		int size = matches.size();
		if (size < 1) {
			// both sides must match
			return Collections.emptySet();
		}
		matches.addAll(op2.match(pattern.op2));
		if (matches.size() > size) {
			// only if both sides of the equation were a match
			return matches;
		}
		return Collections.emptySet();
	}

	/**
	 * Method to "pattern match" the equation with another given one it checks if
	 * the two equation are of the same structure, e.g. a = b, t = u
	 * 
	 * @param eq
	 *          the equation to compare with, e.g. t = u
	 * @return null, if not possible, else a list with two equations, e.g. t = a,
	 *         u = b
	 */
	public Set<Equation> match_old(Equation eq) {
		// TODO 2nd Op is no matchVar!!
		// if matches, return the match update as a list of simple equations

		// accepts only simple equations with equality
		if ((type != Type.RELATION) || (eq.getType() != Type.RELATION)) {
			return null;
		}

		// only match terms, that are marked as matchVars
		if (!eq.getOp1().isMatchVar()) {
			if (!eq.getOp1().equals(op1)) {
				// no match
				return null;
			}
			if (!eq.getOp2().isMatchVar()) {
				// does not make sense to compare with no match variables
				return null;
			}
			// match for op1
			switch (eq.getOp2().getType()) {
			case ATOM:
				// second match is atom: a = u
				// return only u = b
				return Set.of(new Equation("subst2", Type.RELATION, Relation.EQUALITY,
						eq.getOp2(), op2));
			case COMPOSITION:
				switch (eq.getOp2().getOpType()) {
				case UNARY:
					// second is a unary function, e.g. a = f(u)
					// only a match, if this equation also has the same function, e.g.
					// a = f(b)
					if (eq.getOp2().getFuncName().equals(op2.getFuncName())) {
						// return only u = b
						return Set.of(new Equation("subst2", Type.RELATION, Relation.EQUALITY,
								eq.getOp2().getT1(), op2.getT1()));
					}
					break;
				case BINARY:
					// second is a binary function, e.g. a = u + v
					// only a match, if this equation also has the same form, e.g. a =
					// b + c
					if (eq.getOp2().getOp().equals(op2.getOp())
							&& (eq.getOp2().getFuncName() == null
							|| eq.getOp2().getFuncName().equals(op2.getFuncName()))) {
						// return only u = b and v = c
						return Set.of(
								new Equation("subst2", Type.RELATION, Relation.EQUALITY,
										eq.getOp2().getT1(), op2.getT1()),
								new Equation("subst3", Type.RELATION, Relation.EQUALITY,
										eq.getOp2().getT2(), op2.getT2()));
					}
					break;
				case TERTIARY:
					// second i a tertiary function, e.g. a = f(u,v,w)
					// only a match, if this equation also has the same form, e.g. a =
					// f(b,c,d)
					if (eq.getOp2().getOp().equals(op2.getOp())
							&& (eq.getOp2().getFuncName() == null
							|| eq.getOp2().getFuncName().equals(op2.getFuncName()))) {
						// return only u = b, v = c and w = d
						return Set.of(
								new Equation("subst2", Type.RELATION, Relation.EQUALITY,
										eq.getOp2().getT1(), op2.getT1()),
								new Equation("subst3", Type.RELATION, Relation.EQUALITY,
										eq.getOp2().getT2(), op2.getT2()),
								new Equation("subst4", Type.RELATION, Relation.EQUALITY,
										eq.getOp2().getT3(), op2.getT3()));
					}
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}
		} else {
			switch (eq.getOp1().getType()) {
			case ATOM:
				// check if second op is matchVar
				if (!eq.getOp2().isMatchVar()) {
					// only match if identical
					if (!eq.getOp2().equals(op2)) {
						return null;
					}
					// atom and constant: match t = b
					// return only t = a
					return Set.of(new Equation("subst1", Type.RELATION, Relation.EQUALITY,
							eq.getOp1(), op1));
				}
				// both ops are matchVars
				// atom matches with everything
				switch (eq.getOp2().getType()) {
				case ATOM:
					// two atoms: match t = u
					// return t = a and u = b
					return Set.of(
							new Equation("subst1", Type.RELATION, Relation.EQUALITY,eq.getOp1(), op1),
							new Equation("subst2", Type.RELATION, Relation.EQUALITY, eq.getOp2(), op2));
				case COMPOSITION:
					switch (eq.getOp2().getOpType()) {
					case UNARY:
						// an atom and a unary function, e.g. t = f(u)
						// only a match, if this equation also has the same function,
						// e.g. a = f(b)
						if (eq.getOp2().getFuncName().equals(op2.getFuncName())) {
							// return t = a and u = b
							return Set.of(
									new Equation("subst1", Type.RELATION, Relation.EQUALITY, eq.getOp1(), op1),
									new Equation("subst2", Type.RELATION, Relation.EQUALITY,
											eq.getOp2().getT1(), op2.getT1()));
						}
						break;
					case BINARY:
						// an atom and a binary function, e.g. t = u + v
						// only a match, if this equation also has the same form, e.g.
						// a = b + c
						if (eq.getOp2().getOp().equals(op2.getOp())
								&& (eq.getOp2().getFuncName() == null
								|| eq.getOp2().getFuncName().equals(op2.getFuncName()))) {
							// return t = a and u = b and v = c
							return Set.of(
									new Equation("subst1", Type.RELATION, Relation.EQUALITY, eq.getOp1(), op1),
									new Equation("subst2", Type.RELATION, Relation.EQUALITY,
											eq.getOp2().getT1(), op2.getT1()),
									new Equation("subst3", Type.RELATION, Relation.EQUALITY,
											eq.getOp2().getT2(), op2.getT2()));
						}
						break;
					case TERTIARY:
						// an atom and a tertiary function, e.g. t = f(u,v,w)
						// only a match, if this equation also has the same form, e.g.
						// a = f(b,c,d)
						if (eq.getOp2().getOp().equals(op2.getOp())
								&& (eq.getOp2().getFuncName() == null
								|| eq.getOp2().getFuncName().equals(op2.getFuncName()))) {
							// return t = a and u = b, v = c and w = d
							return Set.of(
									new Equation("subst1", Type.RELATION, Relation.EQUALITY, eq.getOp1(), op1),
									new Equation("subst2", Type.RELATION, Relation.EQUALITY,
											eq.getOp2().getT1(),op2.getT1()),
									new Equation("subst3", Type.RELATION, Relation.EQUALITY,
											eq.getOp2().getT2(), op2.getT2()),
									new Equation("subst4", Type.RELATION, Relation.EQUALITY,
											eq.getOp2().getT3(), op2.getT3()));
						}
						break;
					default:
						break;
					}
					break;
				default:
					break;
				}
				break;
			case COMPOSITION:
				// first term is not an atom
				switch (eq.getOp1().getOpType()) {
				case UNARY:
					// first term is a function, e.g. f(t)
					// only a match if this equation is of a similar form
					if (eq.getOp1().getFuncName().equals(op1.getFuncName())) {
						// check if second op is matchVar
						if (!eq.getOp2().isMatchVar()) {
							// only match if identical
							if (!eq.getOp2().equals(op2)) {
								return null;
							}
							// function and constant: match f(t) = b
							// return t = a
							return Set.of(new Equation("subst1", Type.RELATION, Relation.EQUALITY,
									eq.getOp1(), op1));
						}
						// both ops are matchVars
						switch (eq.getOp2().getType()) {
						case ATOM:
							// second term is an atom: match f(t) = u
							// return t = a and u = b
							return Set.of(
									new Equation("subst1", Type.RELATION, Relation.EQUALITY,
											eq.getOp1().getT1(), op1.getT1()),
									new Equation("subst2", Type.RELATION, Relation.EQUALITY, eq.getOp2(), op2));
						case COMPOSITION:
							switch (eq.getOp2().getOpType()) {
							case UNARY:
								// second term is a unary function, e.g. f(t) = g(u)
								// only a match, if this equation also has the same
								// function, e.g. f(a) = g(b)
								if (eq.getOp2().getFuncName().equals(op2.getFuncName())) {
									// return t = a and u = b
									return Set.of(
											new Equation("subst1", Type.RELATION, Relation.EQUALITY,
													eq.getOp1().getT1(), op1.getT1()),
											new Equation("subst2", Type.RELATION, Relation.EQUALITY,
													eq.getOp2().getT1(), op2.getT1()));
								}
								break;
							case BINARY:
								// second term is a binary function, e.g. f(t) = u + v
								// only a match, if this equation also has the same
								// form, e.g. f(a) = b + c
								if (eq.getOp2().getOp().equals(op2.getOp())
										&& (eq.getOp2().getFuncName() == null
										|| eq.getOp2().getFuncName().equals(op2.getFuncName()))) {
									// return t = a and u = b and v = c
									return Set.of(
											new Equation("subst1", Type.RELATION, Relation.EQUALITY,
													eq.getOp1().getT1(), op1.getT1()),
											new Equation("subst2", Type.RELATION, Relation.EQUALITY,
													eq.getOp2().getT1(), op2.getT1()),
											new Equation("subst3", Type.RELATION, Relation.EQUALITY,
													eq.getOp2().getT2(), op2.getT2()));
								}
								break;
							case TERTIARY:
								// second term is a tertiary function, e.g. f(t) =
								// g(u,v,w)
								// only a match, if this equation also has the same
								// form, e.g. f(a) = g(b,c,d)
								if (eq.getOp2().getOp().equals(op2.getOp())
										&& (eq.getOp2().getFuncName() == null
										|| eq.getOp2().getFuncName().equals(op2.getFuncName()))) {
									// return t = a and u = b, v = c and w = d
									return Set.of(
											new Equation("subst1", Type.RELATION, Relation.EQUALITY,
													eq.getOp1().getT1(), op1.getT1()),
											new Equation("subst2", Type.RELATION, Relation.EQUALITY,
													eq.getOp2().getT1(), op2.getT1()),
											new Equation("subst3", Type.RELATION, Relation.EQUALITY,
													eq.getOp2().getT2(), op2.getT2()),
											new Equation("subst4", Type.RELATION, Relation.EQUALITY,
													eq.getOp2().getT3(), op2.getT3()));
								}
								break;
							default:
								break;
							}
							break;
						default:
							break;
						}
					}
					break;
				case BINARY:
					// first term is a binary function, e.g. t + s
					// only a match if this equation is of a similar form
					if (eq.getOp1().getOp().equals(op1.getOp())
							&& (eq.getOp1().getFuncName() == null
							|| eq.getOp1().getFuncName().equals(op1.getFuncName()))) {
						// check if op2 is matchVar
						if (!eq.getOp2().isMatchVar()) {
							// only match if identical
							if (!eq.getOp2().equals(op2)) {
								// no match
								return null;
							}
							// binary function and constant: match t + s = b
							// return only t = a and s = c
							return Set.of(
									new Equation("subst1", Type.RELATION, Relation.EQUALITY,
											eq.getOp1().getT1(), op1.getT1()),
									new Equation("subst2", Type.RELATION, Relation.EQUALITY,
											eq.getOp1().getT2(), op1.getT2()));
						}
						switch (eq.getOp2().getType()) {
						case ATOM:
							// second term is an atom: match t + s = u
							// return t = a and s = c and u = b
							return Set.of(
									new Equation("subst1", Type.RELATION, Relation.EQUALITY,
											eq.getOp1().getT1(), op1.getT1()),
									new Equation("subst2", Type.RELATION, Relation.EQUALITY,
											eq.getOp1().getT2(), op1.getT2()),
									new Equation("subst3", Type.RELATION, Relation.EQUALITY, eq.getOp2(), op2));
						case COMPOSITION:
							switch (eq.getOp2().getOpType()) {
							case UNARY:
								// second term is a unary function, e.g. t + s = f(u)
								// only a match, if this equation also has the same
								// function, e.g. a + c = f(b)
								if (eq.getOp2().getFuncName().equals(op2.getFuncName())) {
									// return t = a s = c and u = b
									return Set.of(
											new Equation("subst1", Type.RELATION, Relation.EQUALITY,
													eq.getOp1().getT1(), op1.getT1()),
											new Equation("subst2", Type.RELATION, Relation.EQUALITY,
													eq.getOp1().getT2(), op1.getT2()),
											new Equation("subst3", Type.RELATION, Relation.EQUALITY,
													eq.getOp2().getT1(), op2.getT1()));
								}
								break;
							case BINARY:
								// second term is a binary function, e.g. t + s = u * v
								// only a match, if this equation also has the same
								// form, e.g. a + c = b * d
								if (eq.getOp2().getOp().equals(op2.getOp())
										&& (eq.getOp2().getFuncName() == null
										|| eq.getOp2().getFuncName().equals(op2.getFuncName()))) {
									// return t = a and s = c and u = b and v = d
									return Set.of(
											new Equation("subst1", Type.RELATION, Relation.EQUALITY,
													eq.getOp1().getT1(), op1.getT1()),
											new Equation("subst2", Type.RELATION, Relation.EQUALITY,
													eq.getOp1().getT2(), op1.getT2()),
											new Equation("subst3", Type.RELATION, Relation.EQUALITY,
													eq.getOp2().getT1(), op2.getT1()),
											new Equation("subst4", Type.RELATION, Relation.EQUALITY,
													eq.getOp2().getT2(), op2.getT2()));
								}
								break;
							case TERTIARY:
								// second term is a tertiary function, e.g. t + s =
								// f(u,v,w)
								// only a match, if this equation also has the same
								// form, e.g. a + c = f(b,d,e)
								if (eq.getOp2().getOp().equals(op2.getOp())
										&& (eq.getOp2().getFuncName() == null
										|| eq.getOp2().getFuncName().equals(op2.getFuncName()))) {
									// return t = a and s = c and u = b, v = d and w = e
									return Set.of(
											new Equation("subst1", Type.RELATION, Relation.EQUALITY,
													eq.getOp1().getT1(), op1.getT1()),
											new Equation("subst2", Type.RELATION, Relation.EQUALITY,
													eq.getOp1().getT2(), op1.getT2()),
											new Equation("subst3", Type.RELATION, Relation.EQUALITY,
													eq.getOp2().getT1(), op2.getT1()),
											new Equation("subst4", Type.RELATION, Relation.EQUALITY,
													eq.getOp2().getT2(), op2.getT2()),
											new Equation("subst5", Type.RELATION, Relation.EQUALITY,
													eq.getOp2().getT3(), op2.getT3()));
								}
								break;
							default:
								break;
							}
							break;
						default:
							break;
						}
					}
					break;
				case TERTIARY:
					// first term is a tertiary function, e.g. f(t,s,r)
					// only a match if this equation is of a similar form
					if (eq.getOp1().getOp().equals(op1.getOp())
							&& (eq.getOp1().getFuncName() == null
							|| eq.getOp1().getFuncName().equals(op1.getFuncName()))) {
						// check if op2 is matchVar
						if (!eq.getOp2().isMatchVar()) {
							// only match if identical
							if (!eq.getOp2().equals(op2)) {
								// no match
								return null;
							}
							// tertiary function and constant: match f(t,s,r) = b
							// return only t = a, s = c and r = d
							return Set.of(
									new Equation("subst1", Type.RELATION, Relation.EQUALITY,
											eq.getOp1().getT1(), op1.getT1()),
									new Equation("subst2", Type.RELATION, Relation.EQUALITY,
											eq.getOp1().getT2(), op1.getT2()),
									new Equation("subst3", Type.RELATION, Relation.EQUALITY,
											eq.getOp1().getT3(), op1.getT3()));
						}
						switch (eq.getOp2().getType()) {
						case ATOM:
							// second term is an atom: match f(t,s,r) = u
							// return t = a and s = c, r = d and u = b
							return Set.of(
									new Equation("subst1", Type.RELATION, Relation.EQUALITY,
											eq.getOp1().getT1(), op1.getT1()),
									new Equation("subst2", Type.RELATION, Relation.EQUALITY,
											eq.getOp1().getT2(), op1.getT2()),
									new Equation("subst3", Type.RELATION, Relation.EQUALITY,
											eq.getOp1().getT3(), op1.getT3()),
									new Equation("subst4", Type.RELATION, Relation.EQUALITY,
											eq.getOp2(), op2));
						case COMPOSITION:
							switch (eq.getOp2().getOpType()) {
							case UNARY:
								// second term is a unary function, e.g. f(t,s,r) = f(u)
								// only a match, if this equation also has the same
								// function, e.g. f(a,c,d) = f(b)
								if (eq.getOp2().getFuncName().equals(op2.getFuncName())) {
									// return t = a, s = c, r = d and u = b
									return Set.of(
											new Equation("subst1", Type.RELATION, Relation.EQUALITY,
													eq.getOp1().getT1(), op1.getT1()),
											new Equation("subst2", Type.RELATION, Relation.EQUALITY,
													eq.getOp1().getT2(), op1.getT2()),
											new Equation("subst3", Type.RELATION, Relation.EQUALITY,
													eq.getOp1().getT3(), op1.getT3()),
											new Equation("subst4", Type.RELATION, Relation.EQUALITY,
													eq.getOp2().getT1(), op2.getT1()));
								}
								break;
							case BINARY:
								// second term is a binary function, e.g. f(t,s,r) = u *
								// v
								// only a match, if this equation also has the same
								// form, e.g. f(a,c,e) = b * d
								if (eq.getOp2().getOp().equals(op2.getOp())
										&& (eq.getOp2().getFuncName() == null
										|| eq.getOp2().getFuncName().equals(op2.getFuncName()))) {
									// return t = a, s = c, r = e, u = b and v = d
									return Set.of(
											new Equation("subst1", Type.RELATION, Relation.EQUALITY,
													eq.getOp1().getT1(), op1.getT1()),
											new Equation("subst2", Type.RELATION, Relation.EQUALITY,
													eq.getOp1().getT2(), op1.getT2()),
											new Equation("subst3", Type.RELATION, Relation.EQUALITY,
													eq.getOp1().getT3(), op1.getT3()),
											new Equation("subst4", Type.RELATION, Relation.EQUALITY,
													eq.getOp2().getT1(), op2.getT1()),
											new Equation("subst5", Type.RELATION, Relation.EQUALITY,
													eq.getOp2().getT2(), op2.getT2()));
								}
								break;
							case TERTIARY:
								// second term is a tertiary function, e.g. f(t,s,r) =
								// f(u,v,w)
								// only a match, if this equation also has the same
								// form, e.g. f(a,c,f) = f(b,d,e)
								if (eq.getOp2().getOp().equals(op2.getOp())
										&& (eq.getOp2().getFuncName() == null
										|| eq.getOp2().getFuncName().equals(op2.getFuncName()))) {
									// return t = a, s = c, r = f, u = b, v = d and w = e
									return Set.of(
											new Equation("subst1", Type.RELATION, Relation.EQUALITY,
													eq.getOp1().getT1(), op1.getT1()),
											new Equation("subst2", Type.RELATION, Relation.EQUALITY,
													eq.getOp1().getT2(), op1.getT2()),
											new Equation("subst3", Type.RELATION, Relation.EQUALITY,
													eq.getOp1().getT3(), op1.getT3()),
											new Equation("subst4", Type.RELATION, Relation.EQUALITY,
													eq.getOp2().getT1(), op2.getT1()),
											new Equation("subst5", Type.RELATION, Relation.EQUALITY,
													eq.getOp2().getT2(), op2.getT2()),
											new Equation("subst6", Type.RELATION, Relation.EQUALITY,
													eq.getOp2().getT3(), op2.getT3()));
								}
								break;
							default:
								break;
							}
							break;
						default:
							break;
						}
					}
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}
		}
		return null;
	}

	/**
	 * Method that changes this equation according to a substitution equation in the form of z:=x.
	 * @param substitution
	 *          the substitution equation
	 */
	public void update(Equation substitution) {
		// TODO update the equation according to the substitution
		// TODO for now only simple equations considered
		if (contains(this, substitution.getOp1())) {
			// only if the term to substitute is contained
			if (containsT(op1, substitution.getOp1())) {
				// TODO call by value?
				this.setOp1(updateT(this.op1, substitution));
			}
			if (containsT(op2, substitution.getOp1())) {
				// TODO call by value?
				this.setOp2(updateT(this.op2, substitution));
			}
			// now reset the collection of terms
			termSet = new LinkedHashSet<Term>();
			collectTerms(this);
			// update the complexity
			depth = checkDepth(this);
		}
	}

	private Term updateT(Term subeq, Equation substitution) {
		// TODO update a term according to the substitution
		Term tmp = new Term(subeq);
		if (tmp.equals(substitution.op1)) {
			// replace the term
			tmp = new Term(substitution.getOp2());
		} else {
			switch (tmp.getType()) {
			case ATOM:
				break;
			case COMPOSITION:
				switch (tmp.getOpType()) {
				case UNARY:
					// recursively check if a term needs to be updated
					tmp.setT1(updateT(tmp.getT1(), substitution));
					// also update the matchVar field from inside-out
					tmp.setMatchVar(tmp.getT1().isMatchVar());
					break;
				case BINARY:
					// recursively check if one of the two terms need to be updated
					tmp.setT1(updateT(tmp.getT1(), substitution));
					tmp.setT2(updateT(tmp.getT2(), substitution));
					// also update the matchVar field from inside-out
					tmp.setMatchVar(tmp.getT1().isMatchVar() || tmp.getT2().isMatchVar());
					break;
				case TERTIARY:
					// recursively check if one of the three terms need to be updated
					tmp.setT1(updateT(tmp.getT1(), substitution));
					tmp.setT2(updateT(tmp.getT2(), substitution));
					tmp.setT3(updateT(tmp.getT3(), substitution));
					// also update the matchVar field from inside-out
					tmp.setMatchVar(tmp.getT1().isMatchVar() || tmp.getT2().isMatchVar()
							|| tmp.getT3().isMatchVar());
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}
		}
		return tmp;
	}

	/**
	 * Helper method to check if a term is contained in an equation.
	 * 
	 * @param eq
	 *          the equation to look into
	 * @param term
	 *          the term looked for
	 * @return true, if there is a match
	 */
	private boolean contains(Equation eq, Term term) {
		// recursively check for a term in the equation
		// assumed to be simple, i.e. no conjunction of equations
		return (containsT(eq.getOp1(), term) || containsT(eq.getOp2(), term));
	}

	/**
	 * Helper method to check if a term is contained in another term.
	 * 
	 * @param subeq
	 *          the term to look into
	 * @param term
	 *          the term looked for
	 * @return true, if there is a match
	 */
	private boolean containsT(Term subeq, Term term) {
		if (subeq == null) {
			return false;
		}
		if (term.equals(subeq)) {
			return true;
		}
		switch (subeq.getType()) {
		case ATOM:
			return false;
		case COMPOSITION:
			switch (subeq.getOpType()) {
			case UNARY:
				return containsT(subeq.getT1(), term);
			case BINARY:
				return (containsT(subeq.getT1(), term) || containsT(subeq.getT2(), term));
			case TERTIARY:
				return (containsT(subeq.getT1(), term) || containsT(subeq.getT2(), term)
						|| containsT(subeq.getT3(), term));
			default:
				break;
			}
			break;
		default:
			break;
		}
		return false;
	}

	/**
	 * Method to check if this equation contains a match variable.
	 * 
	 * @return true, if there is a match variable
	 */
	public boolean containsMatchVar() {
		for (Term t : termSet) {
			// go through all terms and sub-terms
			if (t.isMatchVar()) {
				// only return true, if the term is a matchVar
				return true;
			}
		}
		// only return false, if there is no matchVar
		return false;
	}

	/**
	 * Check if this equation is reflexive, i.e., op1 = op2.
	 * 
	 * @return true, if the equation is of the form x = x
	 */
	public boolean isReflexive() {
		if (type.equals(Type.RELATION) && rel.equals(Relation.EQUALITY)) {
			if (op1.equals(op2)) {
				// only return true, if the two sides of the equation are equal
				return true;
			}
		}
		return false;
	}

	// Getter and setter methods
	public Set<Term> getTermSet() {
		return termSet;
	}

	/*public void setTermSet(Set<Term> termSet) {
		this.termSet = termSet;
	}*/

	public void addTerm(Term term) {
		this.termSet.add(term);
	}

	public Set<Variable> getAtoms() {
		return allAtoms;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Relation getRel() {
		return rel;
	}

	public void setRel(Relation rel) {
		this.rel = rel;
	}

	public Term getOp1() {
		return op1;
	}

	public void setOp1(Term op1) {
		this.op1 = op1;
	}

	public Term getOp2() {
		return op2;
	}

	public void setOp2(Term op2) {
		this.op2 = op2;
	}

	public Equation getEq1() {
		return eq1;
	}

	public void setEq1(Equation eq1) {
		this.eq1 = eq1;
		// update the complexity
		depth = checkDepth(this);
	}

	public Equation getEq2() {
		return eq2;
	}

	public void setEq2(Equation eq2) {
		this.eq2 = eq2;
		// update the complexity
		depth = checkDepth(this);
	}

	public Variable getLefthandSide() {
		return lefthandSide;
	}

	public void setLefthandSide(Variable lefthandSide) {
		this.lefthandSide = lefthandSide;
		// update the complexity
		depth = checkDepth(this);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDepth() {
		return depth;
	}

}
