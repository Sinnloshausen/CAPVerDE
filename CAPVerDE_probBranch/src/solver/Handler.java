package solver;

import properties.Property;

/**
 * Interface that is implemented by the smt-handler and the prolog-handler.
 */
public interface Handler {
	
	/**
	 * Generic verify method to be implemented by the smt and prolog classes.
	 * @param prop
	 * 			the property to verify
	 * @return true, if the property holds
	 */
	public boolean verify(Property prop);

}
