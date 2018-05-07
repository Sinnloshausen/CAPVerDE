package architecture;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Objects that model a deduction a component can make.
 */
public class Deduction implements Serializable {

	/**
	 * @serial Serial ID for storing architecture objects in files.
	 */
	private static final long serialVersionUID = -5610502807395520482L;

	/**
	 * The type of deduction (e.g., transitivity).
	 */
	public enum Type {
		// TODO more to come
		TRANS, SUBST, HOMO, ELSE
	}

	// class fields
	private Type type;
	private Set<Equation> premises;
	private Equation conclusion;
	private String name;
	private double prob;

	/**
	 * The Constructor for deductions.
	 * @param type
	 * 			the type of deduction
	 * @param premises
	 *          the premises needed to deduce the conclusion
	 * @param conclusion
	 *          the conclusion that can be deduced with the given premises
	 * @param name
	 *          a name for the deduction
	 * @param prob
	 * 			the probability of this deduction
	 */
	public Deduction(Type type, Set<Equation> premises, Equation conclusion, String name, double prob) {
		this.type = type;
		this.premises = premises;
		this.conclusion = conclusion;
		this.name = name;
		this.prob = prob;
	}

	/**
	 * The copy constructor that creates deep copies for its equations.
	 * 
	 * @param original
	 *          the deduction to be copied
	 * @param name
	 *          the new name
	 */
	public Deduction(Deduction original, String name) {
		this.name = name;
		type = original.getType();
		premises = new LinkedHashSet<Equation>();
		prob = original.getProb();
		// deep copy for each equation
		for (Equation eq : original.getPremises()) {
			premises.add(new Equation(eq, eq.getName()));
		}
		conclusion = new Equation(original.getConclusion(), original.getConclusion().getName());
	}

	/**
	 * Method that reevaluates this deduction based on a list of substitutions.
	 * Calls {@link Equation#update(Equation) update(Equation)} method for premises and conclusion.
	 * @param substitutions
	 *          the substitutions
	 */
	public void update(Set<Equation> substitutions) {
		// TODO change the deduction according to the substitution
		// go through all substitutions and change the terms accordingly
		for (Equation substitution : substitutions) {
			for (Equation premise : premises) {
				// update each premise
				premise.update(substitution);
			}
			// also update the conclusion
			conclusion.update(substitution);
		}
	}

	@Override
	public String toString() {
		return name + "^" + prob + ": " + premises + " -> " + conclusion;
	}

	/**
	 * Checks if the deduction contains match variables and therefore is no valid
	 * "explicit" deduction.
	 * 
	 * @return true, if there is a match variable in this deduction
	 */
	public boolean containsMatchVar() {
		// check all equations for matchVars
		for (Equation eq : premises) {
			// go through the premises and check if an equation contains a matchVar
			if (eq.containsMatchVar()) {
				return true;
			}
		}
		if (conclusion.containsMatchVar()) {
			// then check the conclusion
			return true;
		}
		// only if neither premises nor conclusion contain such a matchVar, return
		// false
		return false;
	}

	/**
	 * Checks if the deduction conclusion is reflexive.
	 * 
	 * @return true, if the conclusion is of the form x = x
	 */
	public boolean isReflexive() {
		return conclusion.isReflexive();
	}

	/**
	 * Checks if the deduction conclusion is too complex.
	 * @return true, if the complexity is &gt; 3
	 */
	public boolean isTooComplex() {
		// check if the conclusion is complex, i.e., has a complexity greater than 3
		return conclusion.isComplex();
	}

	// getter and setter methods
	public Set<Equation> getPremises() {
		return premises;
	}

	public void setPremises(Set<Equation> premises) {
		this.premises = premises;
	}

	public Equation getConclusion() {
		return conclusion;
	}

	public void setConclusion(Equation conclusion) {
		this.conclusion = conclusion;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	public double getProb() {
		return prob;
	}
	
	public void setProb(double prob) {
		this.prob = prob;
	}

}
