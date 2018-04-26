package architecture;

import java.io.Serializable;

/**
 * Objects that map a dependence relation to a component.
 */
public class DependenceRelation implements Serializable {

  /**
   * @serial Serial ID for storing architecture objects in files.
   */
  private static final long serialVersionUID = -2275538357265292398L;

  @Override
  public String toString() {
    return "" + comp + ": " + dep;
  }

  // class fields
  private Component comp;
  private Dep dep;

  /**
   * The constructor of component's dependence relations.
   * @param comp
   *          the component
   * @param dep
   *          the dependence relations the component has the computational power for
   */
  public DependenceRelation(Component comp, Dep dep) {
    this.comp = comp;
    this.dep = dep;
  }

  // getter and setter methods
  public Component getComp() {
    return comp;
  }

  public Dep getDep() {
    return dep;
  }
}
