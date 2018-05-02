package properties;

import architecture.Component;
import architecture.Equation;
import architecture.Variable;

import java.io.Serializable;

/**
 * Objects describing requirements in the Architecture Language.
 */
public class Property implements Serializable {

  /**
   * @serial Serial ID for storing architecture objects in files.
   */
  private static final long serialVersionUID = 784191785953938061L;

  /**
   * Type of property.
   */
  public enum PropertyType {
    HAS, KNOWS, NOTSHARED, CONJUNCTION, NOTSTORED, SHARED, NEGATION
  }

  // Class fields
  private PropertyType type;
  private Component owner;
  private Double prob;
  private Integer bound;
  private Variable var;
  private Equation eq;
  private Property st1;
  private Property st2;

  @Override
public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((bound == null) ? 0 : bound.hashCode());
	result = prime * result + ((eq == null) ? 0 : eq.hashCode());
	result = prime * result + ((owner == null) ? 0 : owner.hashCode());
	result = prime * result + ((prob == null) ? 0 : prob.hashCode());
	result = prime * result + ((st1 == null) ? 0 : st1.hashCode());
	result = prime * result + ((st2 == null) ? 0 : st2.hashCode());
	result = prime * result + ((type == null) ? 0 : type.hashCode());
	result = prime * result + ((var == null) ? 0 : var.hashCode());
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
	Property other = (Property) obj;
	if (bound == null) {
		if (other.bound != null)
			return false;
	} else if (!bound.equals(other.bound))
		return false;
	if (eq == null) {
		if (other.eq != null)
			return false;
	} else if (!eq.equals(other.eq))
		return false;
	if (owner == null) {
		if (other.owner != null)
			return false;
	} else if (!owner.equals(other.owner))
		return false;
	if (prob == null) {
		if (other.prob != null)
			return false;
	} else if (!prob.equals(other.prob))
		return false;
	if (st1 == null) {
		if (other.st1 != null)
			return false;
	} else if (!st1.equals(other.st1))
		return false;
	if (st2 == null) {
		if (other.st2 != null)
			return false;
	} else if (!st2.equals(other.st2))
		return false;
	if (type != other.type)
		return false;
	if (var == null) {
		if (other.var != null)
			return false;
	} else if (!var.equals(other.var))
		return false;
	return true;
}

  /**
   * The full constructor, typically only called from within this class
   * by the specific constructors.
   * 
   * @param type
   *          the type of property
   * @param owner
   *          the component the property is about
   * @param prob
   *          the probability of the property (HAS/KNOWS)
   * @param bound
   *          the bound of the property (NOTSTORED)
   * @param var
   *          the variable of the property (HAS/NOTSTORED/NOTSHARED)
   * @param eq
   *          the equation of the KNOWS/BELIEVES property
   * @param st1
   *          the first property of the conjunction or negation
   * @param st2
   *          the second property of the conjunction
   */
  public Property(PropertyType type, Component owner, Double prob, Integer bound,
      Variable var, Equation eq, Property st1, Property st2) {
    // Default Constructor
    this.type = type;

    switch (type) {
      case HAS:
        this.owner = owner;
        this.prob = prob;
        this.var = var;
        break;
      case KNOWS:
        this.owner = owner;
        this.prob = prob;
        this.eq = eq;
        break;
      case CONJUNCTION:
        this.st1 = st1;
        this.st2 = st2;
        break;
      case NEGATION:
          this.st1 = st1;
          break;
      case NOTSHARED:
        this.owner = owner;
        this.var = var;
        break;
      case SHARED:
        this.owner = owner;
        this.var = var;
        break;
      case NOTSTORED:
        this.owner = owner;
        this.var = var;
        this.bound = bound;
        break;
      default:
        // do nothing
    }
  }

  /**
   * Constructor called for type = has.
   * 
   * @param type
   *          the type of property
   * @param owner
   *          the component the property is about
   * @param prob
   *          the probability of the HAS property
   * @param var
   *          the variable of the HAS property
   */
  public Property(PropertyType type, Component owner, Double prob, Variable var) {
    // This is called for type=HAS
    this(type, owner, prob, null, var, null, null, null);
  }

  /**
   * Constructor called for type = knows.
   * 
   * @param type
   *          the type of property
   * @param owner
   *          the component the property is about
   * @param prob
   *          the probability of the KNOWS property
   * @param eq
   *          the equation of the KNOWS property
   */
  public Property(PropertyType type, Component owner, Double prob, Equation eq) {
    // This is called for type=KNOWS/BELIEVES
    this(type, owner, prob, null, null, eq, null, null);
  }

  /**
   * Constructor called for type = conjunction.
   * 
   * @param type
   *          the type of property
   * @param st1
   *          the first property of the conjunction
   * @param st2
   *          the second property of the conjunction
   */
  public Property(PropertyType type, Property st1, Property st2) {
    // This is called for type=CONJUNCTION
    this(type, null, null, null, null, null, st1, st2);
  }
  
  /**
   * Constructor called for type = negation.
   * 
   * @param type
   *          the type of property
   * @param st1
   *          the first property of the conjunction
   */
  public Property(PropertyType type, Property st1) {
    // This is called for type=NEGATION
    this(type, null, null, null, null, null, st1, null);
  }

  /**
   * Constructor called for type = notshared.
   * 
   * @param type
   *          the type of property
   * @param owner
   *          the component the property is about
   * @param var
   *          the variable involved
   */
  public Property(PropertyType type, Component owner, Variable var) {
    // This is called for type = NOTSHARED
    this(type, owner, null, null, var, null, null, null);
  }
  
  /**
   * Constructor called for type = notstored.
   * 
   * @param type
   *          the type of property
   * @param owner
   *          the component the property is about
   * @param var
   *          the variable involved
   * @param bound
   * 		  the timer-bound for the variable (how often can it be used before deletion)
   */
  public Property(PropertyType type, Component owner, Variable var, int bound) {
    // This is called for type = NOTSTORED
    this(type, owner, null, bound, var, null, null, null);
  }

  @Override
  public String toString() {
    return arch2string(this);
  }

  /**
   * Helper method to form a string describing the architecture property.
   * 
   * @param arch
   *          the architecture property
   * @return the string
   */
  private String arch2string(Property arch) {
    switch (arch.type) {
      case HAS:
        return "Has_" + arch.owner + "^" + arch.prob + "(" + arch.var.getName() + ")";
      case KNOWS:
        return "Knows_" + arch.owner + "^" + arch.prob + "(" + arch.eq.toString() + ")";
      case CONJUNCTION:
        return arch2string(arch.st1) + " AND " + arch2string(arch.st2);
      case NEGATION:
          return "NOT " + arch2string(arch.st1);
      case NOTSHARED:
        return "notShared_" + arch.owner + "(" + arch.var.getName() + ")";
      case SHARED:
        return "Shared(" + arch.owner + ", " + arch.var.getName() + ")";
      case NOTSTORED:
        return "notStored_" + arch.owner + "(" + arch.var.getName() + ", " + arch.bound + ")";
      default:
        return "";
    }
  }

  // Getter and Setter methods
  public PropertyType getType() {
    return type;
  }

  public void setType(PropertyType type) {
    this.type = type;
  }

  public Component getOwner() {
    return owner;
  }

  public void setOwner(Component owner) {
    this.owner = owner;
  }

  public Double getProb() {
    return prob;
  }

  public void setProb(Double prob) {
    this.prob = prob;
  }

  public Variable getVar() {
    return var;
  }

  public void setVar(Variable var) {
    this.var = var;
  }

  public Equation getEq() {
    return eq;
  }

  public void setEq(Equation eq) {
    this.eq = eq;
  }

  public Property getSt1() {
    return st1;
  }

  public void setSt1(Property st1) {
    this.st1 = st1;
  }

  public Property getSt2() {
    return st2;
  }

  public void setSt2(Property st2) {
    this.st2 = st2;
  }
  
  public Integer getBound() {
	  return bound;
  }
  
  public void setBound(Integer bound) {
	  this.bound = bound;
  }

}
