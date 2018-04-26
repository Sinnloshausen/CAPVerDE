package properties;

/**
 * The Parser Interface for both types of parsers: bottom up and top down.
 */
public interface Parser {

  /**
   * Generic parser method to be implemented by the implementing classes.
   * @param statement
   *          the property to verify
   * @param recurseDepth
   *          an integer indicating the depth of recursion for tracing purposes
   * @return success
   */
  boolean verifyStatement(Property statement, int recurseDepth);

}
