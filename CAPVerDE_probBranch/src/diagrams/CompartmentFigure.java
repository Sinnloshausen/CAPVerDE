package diagrams;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Insets;

/**
 * Compartments and the corresponding borders necessary for the architecture diagrams.
 */
public class CompartmentFigure extends Figure {

  /**
   * The constructor for compartment figures.
   */
  public CompartmentFigure() {
    ToolbarLayout layout = new ToolbarLayout();
    layout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
    layout.setStretchMinorAxis(false);
    layout.setSpacing(2);
    setLayoutManager(layout);
    setBorder(new CompartmentFigureBorder());
  }

  /**
   * The class responsible for the borders of compartment figures.
   */
  public class CompartmentFigureBorder extends AbstractBorder {

    @Override
	public void paint(IFigure figure, Graphics graphics, Insets insets) {
      graphics.drawLine(getPaintRectangle(figure, insets).getTopLeft(), tempRect.getTopRight());
    }
    
    // getter and setter methods
    @Override
	public Insets getInsets(IFigure figure) {
      return new Insets(1, 0, 0, 0);
    }
  }
}