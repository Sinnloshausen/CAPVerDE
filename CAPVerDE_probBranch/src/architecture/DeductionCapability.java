package architecture;

import java.io.Serializable;
import java.util.Set;

/**
 * Objects that describe what a certain component is capable of deducing.
 */
public class DeductionCapability implements Serializable {

  /**
   * @serial Serial ID for storing architecture objects in files.
   */
  private static final long serialVersionUID = 8733619084788057300L;

  // class fields
  private Component comp;
  private Set<Deduction> deducSet;

  /**
   * The Constructor for deduction capabilities.
   * @param comp
   *          the component
   * @param deducSet
   *          the deductions the component is capable of
   */
  public DeductionCapability(Component comp, Set<Deduction> deducSet) {
    this.comp = comp;
    this.deducSet = deducSet;
  }

  @Override
  public String toString() {
    return "" + comp + ":" + deducSet;
  }

  // getter and setter functions
  public Component getComp() {
    return comp;
  }

  public Set<Deduction> getDeducSet() {
    return deducSet;
  }

}
