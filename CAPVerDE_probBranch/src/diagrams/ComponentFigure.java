package diagrams;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * Objects that represent a component in the architecture diagrams.
 */
public class ComponentFigure extends Figure {
  
  // class fields
  public static Color classColor = new Color(null, 255, 255, 206);
  private CompartmentFigure methodFigure = new CompartmentFigure();

  /**
   * The constructor of a component figure.
   * @param name
   *          the name of the component as a {@link org.eclipse.draw2d.Label#Label() Label}
   */
  public ComponentFigure(Label name) {
    ToolbarLayout layout = new ToolbarLayout();
    setLayoutManager(layout);
    setBorder(new LineBorder(ColorConstants.black, 1));
    setBackgroundColor(classColor);
    setOpaque(true);
    setFont(new Font(null, "Arial", 12, SWT.NONE));

    add(name);
    add(methodFigure);
  }

  // getter and setter methods
  public CompartmentFigure getMethodsCompartment() {
    return methodFigure;
  }
}