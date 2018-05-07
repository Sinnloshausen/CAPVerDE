package utils;

/**
 * Class that "cleans" string for SMT use, i.e., replaces () by &lt;&gt;.
 */
public class SmtCleanup {

  /**
   * Empty private constructor to avoid instantiation.
   */
  private SmtCleanup() {
  }

  /**
   * Method to remove round brackets and replaces these with {@code <>} due to the
   * nature of SMT's reserved characters.
   * 
   * @param input
   *          the input string
   * @return the "cleaned" string
   */
  public static String removeParantheses(String input) {
    input = input.replace('(', '<');
    return input.replace(')', '>');
  }

}
