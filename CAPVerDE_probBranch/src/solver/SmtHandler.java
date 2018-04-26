package solver;

import java.io.Serializable;
import java.util.List;

import architecture.Architecture;
import architecture.Component;
import architecture.Equation;
import architecture.Variable;
import properties.Property;

/**
 * Class that handles all the SMT-syntax based work.
 */
public class SmtHandler implements Serializable {

  /**
   * @serial Serial ID for storing architecture objects in files.
   */
  private static final long serialVersionUID = -413821521920158094L;

  /**
   * Type of SMT command.
   */
  private enum Command {
    // TODO more commands?
    DECLARE, DEFINE, ASSERT, CHECK, OPTION, POP, PUSH, UNSAT
  }

  /**
   * Type of variable.
   */
  private enum VariableType {
    // TODO more types?
    BOOL, INT, EQ, FUNC, SORT;

    @Override
    public String toString() {
      switch (this) {
        case BOOL:
          return "Bool";
        case INT:
          return "Int";
        case EQ:
          return "Equation";
        default:
          return this.name();
      }
    }
  }

  /**
   * Type of collection, either variables or equations.
   */
  private enum Type {
    VAR, EQ
  }

  // class fields
  private String buffer;
  private Architecture arch;

  /**
   * Constructor that initializes the buffer and already add the first lines of SMT
   * code to declare necessary functions and variables.
   * @param arch
   *          the architecture
   */
  public SmtHandler(Architecture arch) {
    this.arch = arch;
    // initialize buffer
    buffer = "";
    // set options
    addLineSmt(Command.OPTION, null, null, null, null, null);
    // declare all the necessary variables and functions
    declareFunctions();
    declareVariables();
  }

  /**
   * Method that adds an SMT assertion to the existing file
   * to check for satisfiabilty.
   * @param property
   *          the poperty to verify
   * @return true if sat, false else
   */
  public boolean verify(Property property) {
    // add the line for the saturability check
    addLineSmt(Command.CHECK, null, null, null, null, null);

    // TODO debug
    // System.out.println(buffer);

    SolverHandler solv = new SolverHandler();
    if (!solv.runSolver(buffer, property)) {
      addLineSmt(Command.UNSAT, null, null, null, null, null);
      solv.runSolver(buffer, property);
      return false;
    }
    return true;
  }

  /**
   * Helper method to declare all necessary functions: has All/ONE/NONE, K, B for each
   * component and additionally declare the necessary type equation.
   */
  private void declareFunctions() {
    // first of all declare the equation type
    addLineSmt(Command.DECLARE, "Equation", VariableType.SORT, null, null, null);

    // go through all components and add their respective Has, K, B functions
    for (Component comp : arch.getCompList()) {
      // first has ALL
      addLineSmt(Command.DECLARE, "has" + comp.getName() + "all", VariableType.FUNC,
          null, VariableType.INT, VariableType.BOOL);
      // then has ONE
      addLineSmt(Command.DECLARE, "has" + comp.getName() + "one", VariableType.FUNC,
          null, VariableType.INT, VariableType.BOOL);
      // then has NONE
      addLineSmt(Command.DECLARE, "has" + comp.getName() + "none", VariableType.FUNC,
          null, VariableType.INT, VariableType.BOOL);
      // also K
      addLineSmt(Command.DECLARE, "k" + comp.getName(), VariableType.FUNC, null,
          VariableType.EQ, VariableType.BOOL);
      // and B
      addLineSmt(Command.DECLARE, "b" + comp.getName(), VariableType.FUNC, null,
          VariableType.EQ, VariableType.BOOL);
      // finally notShared
      addLineSmt(Command.DECLARE, "notShared" + comp.getName(), VariableType.FUNC,
          null, VariableType.INT, VariableType.BOOL);
      // finally notStored
      addLineSmt(Command.DECLARE, "notStored" + comp.getName(), VariableType.FUNC,
          null, VariableType.INT, VariableType.BOOL);
    }
  }

  /**
   * Helper method to declare all necessary variables and also declare all necessary
   * equations.
   */
  private void declareVariables() {
    // go through list and declare the variables
    for (Variable var : arch.getVariables()) {
      addLineSmt(Command.DECLARE, var.getName(), VariableType.INT, null, null, null);
    }
    // also go through list of equations and declare these
    for (Equation eq : arch.getAllEquations()) {
      addLineSmt(Command.DECLARE, eq.getName(), VariableType.EQ, null, null, null);
    }
  }

  /**
   * Method to generate assert SMT statement for the provided properties of one
   * component.
   * @param comp
   *          the component
   * @param compPropList
   *          the list of properties regarding this component
   */
  public void generateSmtfromPropList(Component comp, List<Property> compPropList) {
    // TODO test everything
    // have bool vectors for each category
    int numVars = arch.getVariables().size();
    boolean[] hasAll = new boolean[numVars];
    boolean[] hasOne = new boolean[numVars];
    boolean[] hasNone = new boolean[numVars];
    boolean[] notShared = new boolean[numVars];
    boolean[] notStored = new boolean[numVars];
    // Arrays.fill(notShared, true);
    int indexVar = 0;
    for (Property prop : compPropList) {
      switch (prop.getType()) {
        case HAS:
          indexVar = arch.getVariables().indexOf(prop.getVar());
              hasAll[indexVar] = true;
        case KNOWS:
          arch.addEquation(prop.getEq());
          break;
        case NOTSHARED:
          indexVar = arch.getVariables().indexOf(prop.getVar());
          notShared[indexVar] = true;
          break;
        case NOTSTORED:
          indexVar = arch.getVariables().indexOf(prop.getVar());
          notStored[indexVar] = true;
          break;
        default:
          // do nothing
          break;
      }
    }

    int numEq = arch.getAllEquations().size();
    boolean[] k = new boolean[numEq];
    boolean[] b = new boolean[numEq];
    int indexEq = 0;
    for (Property prop : compPropList) {
      // go through properties a second time
      switch (prop.getType()) {
        case KNOWS:
          indexEq = arch.getAllEquations().indexOf(prop.getEq());
          // set knowledge flag
          k[indexEq] = true;
          break;
        default:
          // do nothing
          break;
      }
    }
    // assert the 6 types of statements
    addLineSmt(Command.ASSERT, "HA" + comp.getName(), null,
        generateExpressionFromBools(hasAll, "has" + comp.getName() + "all", Type.VAR), null, null);
    addLineSmt(Command.ASSERT, "HO" + comp.getName(), null,
        generateExpressionFromBools(hasOne, "has" + comp.getName() + "one", Type.VAR), null, null);
    addLineSmt(Command.ASSERT, "HN" + comp.getName(), null,
        generateExpressionFromBools(
            hasNone, "has" + comp.getName() + "none", Type.VAR), null, null);
    addLineSmt(Command.ASSERT, "K" + comp.getName(), null,
        generateExpressionFromBools(k, "k" + comp.getName(), Type.EQ), null, null);
    addLineSmt(Command.ASSERT, "B" + comp.getName(), null,
        generateExpressionFromBools(b, "b" + comp.getName(), Type.EQ), null, null);
    addLineSmt(Command.ASSERT, "SH" + comp.getName(), null,
        generateExpressionFromBools(notShared, "notShared" + comp.getName(), Type.VAR), null, null);
    addLineSmt(Command.ASSERT, "ST" + comp.getName(), null,
        generateExpressionFromBools(notStored, "notStored" + comp.getName(), Type.VAR), null, null);
  }

  /**
   * Helper method to generate a conjunction of booleans from variables.
   * 
   * @param boolArray
   *          the boolean array
   * @param funcName
   *          the name of the function applied to the variables
   * @param type
   *          variables or equations
   * @return the conjunction as string
   */
  private String generateExpressionFromBools(boolean[] boolArray, String funcName, Type type) {
    // TODO test
    String expr = "";
    String obj = "";
    for (int i = 0; i < boolArray.length; i++) {
      // is it a variable or an equation?
      switch (type) {
        case VAR:
          obj = arch.getVariables().get(i).getName();
          break;
        case EQ:
          obj = arch.getAllEquations().get(i).getName();
          break;
        default:
          break;
      }
      if (boolArray[i]) {
        expr = expr + " (" + funcName + " " + obj + ")";
      } else {
        expr = expr + " (not (" + funcName + " " + obj + "))";
      }
    }

    return "(and" + expr + ")";
  }

  /**
   * Method that generates an assert from a given privacy property.
   * 
   * @param statement
   *          the privacy property
   * @param counter
   *          the property counter
   */
  public void generateSmtfromStatement(Property statement, int counter) {
    // call helper method to handle recursion
    String expression = generateExpressionFromStatement(statement);
    if (counter > 1) {
      // pop the previous assert, if this is not the first
      addLineSmt(Command.POP, null, null, null, null, null);
    }
    // set a reset point
    addLineSmt(Command.PUSH, null, null, null, null, null);
    // assert the expression
    addLineSmt(Command.ASSERT, "PROP" + counter, null, expression, null, null);
  }

  /**
   * Helper method to generate an expression as string from a statement.
   * 
   * @param statement
   *          the statement
   * @return the SMT expression as string
   */
  private String generateExpressionFromStatement(Property statement) {
    String expression = "";
    // check for the type of property
    switch (statement.getType()) {
      case HAS:
            expression = expression + "(has" + statement.getOwner() + " " + statement.getProb() + " "
              + statement.getVar().getName() + ")";
        break;
      case KNOWS:
        // TODO unique names are important
        expression = expression + "(k" + statement.getOwner() + " " + statement.getProb() + " "
          + nameEquation(statement.getEq()) + ")";
        break;
      case NOTSHARED:
        expression = expression + "(notShared" + statement.getOwner() + " "
          + statement.getVar().getName() + ")";
        break;
      case NOTSTORED:
        expression = expression + "(notStored" + statement.getOwner() + " "
          + statement.getVar().getName() + ")";
        break;
      case CONJUNCTION:
        // recursion (reverse of Rule "I^")
        expression = expression + "(and " + generateExpressionFromStatement(statement.getSt1())
          + " " + generateExpressionFromStatement(statement.getSt2()) + ")";
        break;
      case NEGATION:
          // recursion (reverse of Rule "Ineg")
          expression = expression + "(not " + generateExpressionFromStatement(statement.getSt1()) + ")";
          break;
      default:
        // TODO handle other types
        break;
    }
    return expression;
  }

  /**
   * Helper method to make sure that unique, but consistent equation names are
   * used.
   * 
   * @param eq
   *          the equation to name
   * @return the unique and consistent name
   */
  private String nameEquation(Equation equation) {
    // go through all the equations of the arch
    for (Equation eq : arch.getAllEquations()) {
      if (eq.equals(equation)) {
        // use the name of the equivalent equation that already exists
        return eq.getName();
      }
    }
    // use a new name
    return equation.getName();
  }

  /**
   * Method to write an SMT line into the buffer.
   * 
   * @param cmd
   *          the type of SMT command
   * @param varName
   *          the name of the variable to declare
   * @param varType
   *          the type of variable to declare
   * @param expression
   *          the expression for a define
   */
  private void addLineSmt(Command cmd, String varName, VariableType varType, String expression,
      VariableType in, VariableType out) {
    // Method used to add a line to the buffer
    switch (cmd) {
      case DECLARE:
        // declare a variables based in its type
        switch (varType) {
          case BOOL:
            addBuffer("( declare-fun " + varName + " () Bool )" + System.lineSeparator());
            break;
          case INT:
            addBuffer("( declare-fun " + varName + " () Int )" + System.lineSeparator());
            break;
          case FUNC:
            addBuffer("( declare-fun " + varName + " (" + in.toString() + ") "
                + out.toString() + " )" + System.lineSeparator());
            break;
          case SORT:
            addBuffer("( declare-sort " + varName + " 0 )" + System.lineSeparator());
            break;
          case EQ:
            addBuffer("( declare-fun " + varName + " () Equation )" + System.lineSeparator());
            break;
          default:
            break;
        }
        break;
      case DEFINE:
        // define a variables based in its type
        switch (varType) {
          case BOOL:
            addBuffer("( define-fun " + varName + " () Bool " + expression
                + " )" + System.lineSeparator());
            // also assert the defined bool
            addLineSmt(Command.ASSERT, varName, null, null, null, null);
            break;
          case INT:
            addBuffer("( define-fun " + varName + " () Int " + expression
                + " )" + System.lineSeparator());
            break;
          case FUNC:
            addBuffer("( define-fun " + varName + " (" + in.toString() + ") "
                + out.toString() + " " + expression + " )" + System.lineSeparator());
            break;
          case SORT:
            addBuffer("( define-sort " + varName + " () Bool " + expression
                + " )" + System.lineSeparator());
            break;
          case EQ:
            addBuffer("( define-fun " + varName + " () Equation " + expression
                + " )" + System.lineSeparator());
            break;
          default:
            break;
        }
        break;
      case ASSERT:
        if (expression == null) {
          addBuffer("( assert " + varName + " )" + System.lineSeparator());
        } else {
          addBuffer("( assert (! " + expression + " :named " + varName
              + ") )" + System.lineSeparator());
        }
        break;
      case CHECK:
        // TODO more options?
        addBuffer("( check-sat )" + System.lineSeparator());
        break;
      case UNSAT:
        addBuffer("( get-unsat-core )" + System.lineSeparator());
        break;
      case OPTION:
        // TODO different options
        addBuffer("( set-option :produce-models true )" + System.lineSeparator());
        addBuffer("( set-option :produce-unsat-cores true )" + System.lineSeparator());
        break;
      case PUSH:
        addBuffer("( push 1 )" + System.lineSeparator());
        break;
      case POP:
        addBuffer("( pop 1 )" + System.lineSeparator());
        break;
      default:
        break;
    }
  }

  private void addBuffer(String buffer) {
    this.buffer += buffer;
  }

  public String getBuffer() {
    return buffer;
  }

}
