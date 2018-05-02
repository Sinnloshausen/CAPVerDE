package architecture;

import java.io.Serializable;
import java.util.Set;

/**
 * Objects that represent Dependence Relations.
 * These state that a variable can be deduced from a list of variables.
 */
public class Dep implements Serializable {

  /**
   * @serial Serial ID for storing architecture objects in files.
   */
  private static final long serialVersionUID = -2460546602613659288L;

  // class fields
  private Variable var;
  private Set<Variable> varSet;
  private double prob;

  /**
   * The Constructor for dependence relations.
   * 
   * @param var
   *          the variable that can be derived
   * @param varSet
   *          the list of necessary variables for the relation
   * @param prob
   * 			the probability of the dependence relation
   */
  public Dep(Variable var, Set<Variable> varSet, double prob) {
    this.var = var;
    this.varSet = varSet;
    this.prob = prob;
  }

  @Override
  public String toString() {
    return "Dep^" + prob + "(" + var + ", " + varSet + ")";
  }

  // getter and setter methods
  public Variable getVar() {
    return var;
  }

  public void setVar(Variable var) {
    this.var = var;
  }

  public Set<Variable> getVarSet() {
    return varSet;
  }

  public void setVarSet(Set<Variable> varSet) {
    this.varSet = varSet;
  }
  
  public double getProb() {
	  return prob;
  }
  
  public void setProb(double prob) {
	  this.prob = prob;
  }

}
