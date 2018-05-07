package architecture;

/**
 * This is simply the interface to be implemented by Proof and Attest.
 * A statement is either one of those two.
 */
public interface Statement {
	
	/**
	 * Method that returns the name of the statement.
	 * @return the name
	 */
	public String getName();

}
