package architecture;

public class Composition {
	
	// class fields
	private Component container;
	private Component component;
	
	/**
	 * The constructor for composition realtions.
	 * @param container
	 * 			the component that is composed of the other
	 * @param component
	 * 			the component that composes the other
	 */
	public Composition(Component container, Component component) {
		this.container = container;
		this.component = component;
	}
	
	@Override
	public String toString() {
			return container + " is composed of " + component;
	}
	
	// getter and setter methods
	public Component getContainer() {
		return container;
	}
	
	public Component getComponent() {
		return component;
	}

}
