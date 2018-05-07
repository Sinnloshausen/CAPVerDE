package gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionEndpointLocator;
import org.eclipse.draw2d.FanRouter;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MidpointLocator;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import architecture.Action;
import architecture.Architecture;
import architecture.Attest;
import architecture.Component;
import architecture.Deduction;
import architecture.DeductionCapability;
import architecture.DependenceRelation;
import architecture.Equation;
import architecture.Statement;
import architecture.Term;
import architecture.Trust;
import architecture.Variable;
import architecture.Equation.Type;
import architecture.Proof;
import architecture.Term.Operator;
import architecture.Term.OperatorType;
import diagrams.ComponentFigure;
import gui.ArchitectureFunctions.CaseStudy;
import properties.Property;
import utils.FileReader;
import utils.SaveLoadArch;

/**
 * GUI object that opens a shell and displays all necessary composites.
 * Also updates all tables, etc with the ArchitectureFunctions lists.
 */
public class Gui {

  /**
   * The types of message boxes.
   * {@link #INF INF} displays an info box that can only be acknowledged
   * {@link #ERR ERR} displays an error message that leads to a reset
   * {@link #LOG LOG} displays the trace of a verification
   * {@link #WARN WARN} displays a warning that informs of a reset
   */
  public static enum MessageType {
    INF, ERR, LOG, WARN;
  }

  /**
   * The different types of objects, like components, variables, or properties.
   */
  private enum ObjectType {
    COMP, VAR, TERM, EQ, TRUST, STMT, ACT, DEP, DED, PROP;
  }

  // class fields
  private static ArchitectureFunctions archFunc = new ArchitectureFunctions();
  public static Display display = new Display();
  public Shell shell = new Shell(display);

  /**
   * The constructor of the GUI that initializes the shell and its content,
   * displays the GUI and also contains the program loop.
   */
  public Gui() {
    shell.setText("CAPVerDE: Computer-Aided Privacy Verification and Design Engineering Tool");
    shell.setLayout(new FillLayout());
    // shell.setLayout(new GridLayout(2, true));
    
    // event listener
    final MouseListener mouseListener = new MouseAdapter() {
      @Override
	public void mouseDown(final MouseEvent e) {
        Text t = (Text) e.widget;
        t.selectAll();
      }
    };

    // ################################## tabs
    // ########################################
    // scrollable parent
    ScrolledComposite sc = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
    sc.setLayout(new FillLayout(SWT.HORIZONTAL));
    sc.setExpandHorizontal(true);
    sc.setExpandVertical(true);

    Composite everything = new Composite(sc, SWT.BORDER);
    everything.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    everything.setLayout(new GridLayout(1, false));
    everything.setBackground(display.getSystemColor(SWT.COLOR_GRAY));

    // the top line for all extra options like save,load,edit,finish,draw
    Composite top = new Composite(everything, SWT.BORDER);
    top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    top.setLayout(new GridLayout(5, false));
    top.setBackground(display.getSystemColor(SWT.COLOR_GRAY));

    // finish
    Group finish = new Group(top, SWT.SHADOW_IN);
    finish.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
    finish.setLayout(new GridLayout(1, false));
    finish.setText("Finish Architecture Creation");

    Button finishButton = new Button(finish, SWT.PUSH);
    finishButton.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    finishButton.setText("Finish");
    finishButton.setToolTipText(
        "The architecture will be considered created and will be verified for consistency.");
    finishButton.addListener(SWT.Selection, event -> archFunc.finish());
    finishButton.addListener(SWT.Selection, event -> finishButton.setEnabled(false));

    // load
    Group load = new Group(top, SWT.SHADOW_IN);
    load.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
    load.setLayout(new GridLayout(2, false));
    load.setText("Load Example Architectures");

    Combo examples = new Combo(load, SWT.DROP_DOWN | SWT.READ_ONLY);
    examples.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    examples.setText("Example Case Studies");
    examples.add(CaseStudy.SEM.toString());
    // examples.addListener(SWT.DROP_DOWN, event -> updateCaseStudies(archFunc,
    // examples));

    Button loadButton = new Button(load, SWT.PUSH);
    loadButton.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    loadButton.setText("Load");
    loadButton.setToolTipText(
        "Instead of modeling a new architecture, one of the example case studies can be loaded.");
    /*
    loadButton.addListener(SWT.Selection, event -> archFunc.load(examples.getText()));
    loadButton.addListener(SWT.Selection, event -> archFunc.finish());
    loadButton.addListener(SWT.Selection, event -> loadButton.setEnabled(false));
    loadButton.addListener(SWT.Selection, event -> finishButton.setEnabled(false));*/
    //finishButton.addListener(SWT.Selection, event -> loadButton.setEnabled(false));

    // save/load
    Group saveload = new Group(top, SWT.SHADOW_IN);
    saveload.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
    saveload.setLayout(new GridLayout(5, false));
    saveload.setText("Save/Load Architectures");

    Text archName = new Text(saveload, SWT.SINGLE);
    archName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
    archName.setText("name");
    archName.setToolTipText("The name of the architecture to save or load");
    archName.addMouseListener(mouseListener);

    Button saveButton = new Button(saveload, SWT.PUSH);
    saveButton.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    saveButton.setText("Save");
    saveButton.setToolTipText("Save a created architecture to disk.");
    saveButton.addListener(SWT.Selection, event -> archFunc.save2file(archName.getText()));

    Button loadButton2 = new Button(saveload, SWT.PUSH);
    loadButton2.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    loadButton2.setText("Load");
    loadButton2.setToolTipText("Load a previously saved architecture from disk.");
    /*
    loadButton.addListener(SWT.Selection, event -> saveButton.setEnabled(false));
    loadButton.addListener(SWT.Selection, event -> loadButton2.setEnabled(false));*/

    // edit
    Group reset = new Group(top, SWT.SHADOW_IN);
    reset.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
    reset.setLayout(new GridLayout(2, false));
    reset.setText("Remodeling of Architecture");

    Button editBtn = new Button(reset, SWT.PUSH);
    editBtn.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    editBtn.setText("Edit");
    editBtn.setToolTipText("Go back to architecture modeling.");
    // editBtn.addListener(SWT.Selection, event -> left.setEnabled(true));
    // //folder.getTabList()[0]
    // editBtn.addListener(SWT.Selection, event -> left2.setEnabled(false));
    // //folder.getTabList()[2]
    editBtn.addListener(SWT.Selection, event -> finishButton.setEnabled(true));
    editBtn.addListener(SWT.Selection, event -> loadButton.setEnabled(true));
    editBtn.addListener(SWT.Selection, event -> saveButton.setEnabled(true));
    editBtn.addListener(SWT.Selection, event -> loadButton2.setEnabled(true));

    Button resetBtn = new Button(reset, SWT.PUSH);
    resetBtn.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    resetBtn.setText("Reset");
    resetBtn.setToolTipText("Reset the architecture and start from scratch.");

    // draw
    Group diagram = new Group(top, SWT.SHADOW_IN);
    diagram.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
    diagram.setLayout(new GridLayout(1, false));
    diagram.setText("Architecture Diagram");

    Button drawButton = new Button(diagram, SWT.PUSH);
    drawButton.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    drawButton.setText("Draw");
    drawButton.setToolTipText("Show a graphical representation of the architecture.");
    drawButton.addListener(SWT.Selection, event -> showDiagram());
    drawButton.setEnabled(false);
    editBtn.addListener(SWT.Selection, event -> drawButton.setEnabled(false));
    finishButton.addListener(SWT.Selection, event -> drawButton.setEnabled(true));

    // tab folder
    TabFolder folder = new TabFolder(everything, SWT.NONE);
    folder.setLayout(new GridLayout(1, false));

    finishButton.addListener(SWT.Selection, event -> folder.setSelection(1));

    // first tab
    TabItem tab1 = new TabItem(folder, SWT.NONE);
    tab1.setText("Architecture Design");

    // left and right sides
    Composite bothSides1 = new Composite(folder, SWT.NONE);
    bothSides1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    bothSides1.setLayout(new GridLayout(2, true));
    Composite left = new Composite(bothSides1, SWT.BORDER);
    left.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    left.setLayout(new GridLayout(1, true));
    left.setBackground(display.getSystemColor(SWT.COLOR_GRAY));
    Composite right = new Composite(bothSides1, SWT.BORDER);
    right.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    right.setLayout(new GridLayout(1, true));

    // left side
    // first line
    Group components = new Group(left, SWT.SHADOW_IN);
    components.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    components.setLayout(new GridLayout(5, true));
    components.setText("Components");

    Text compName = new Text(components, SWT.SINGLE);
    compName.setText("name");
    compName.setToolTipText("The name of the component to add");
    compName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 4, 1));
    compName.addMouseListener(mouseListener);

    Button compAdd = new Button(components, SWT.PUSH);
    compAdd.setText("Add");
    compAdd.addListener(SWT.Selection, event -> archFunc.addComponent(compName.getText()));

    // second line
    Group variables = new Group(left, SWT.SHADOW_IN);
    variables.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    variables.setLayout(new GridLayout(5, true));
    variables.setText("Variables");

    Text varName = new Text(variables, SWT.SINGLE);
    varName.setText("name");
    varName.setToolTipText("The name of the variable to add");
    varName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 4, 1));
    varName.addMouseListener(mouseListener);

    Button varAdd = new Button(variables, SWT.PUSH);
    varAdd.setText("Add");
    varAdd.addListener(SWT.Selection, event -> archFunc.addVariable(varName.getText()));

    // third line
    Group subterms = new Group(left, SWT.SHADOW_IN);
    subterms.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    subterms.setLayout(new GridLayout(31, false));
    subterms.setText("Sub-Terms");

    Group opType = new Group(subterms, SWT.SHADOW_ETCHED_OUT);
    opType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    opType.setLayout(new GridLayout(3, true));
    opType.setText("Type of operator");
    Button unary = new Button(opType, SWT.RADIO);
    unary.setText("unary");
    unary.setSelection(true);
    Button binary = new Button(opType, SWT.RADIO);
    binary.setText("binary");
    Button tertiary = new Button(opType, SWT.RADIO);
    tertiary.setText("tertiary");

    Combo operator = new Combo(subterms, SWT.DROP_DOWN | SWT.READ_ONLY);
    operator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    operator.setText("Operator");
    operator.add("FUNC");
    operator.add("ADD");
    operator.add("MULT");
    operator.add("SUB");
    operator.add("DIV");
    operator.select(0);
    operator.addListener(SWT.DROP_DOWN, event -> updateOperators(operator, binary));

    Text funcName = new Text(subterms, SWT.SINGLE);
    funcName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
    funcName.setText("function name");
    funcName.setToolTipText("The name of the custom function");
    funcName.setEnabled(true);
    operator.addSelectionListener(new SelectionAdapter() {
      @Override
	public void widgetSelected(SelectionEvent e) {
        if (operator.getText().equals("FUNC")) {
          funcName.setEnabled(true);
        } else {
          funcName.setEnabled(false);
        }
      }
    });
    funcName.addMouseListener(mouseListener);

    Combo term1 = new Combo(subterms, SWT.DROP_DOWN | SWT.READ_ONLY);
    term1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 2));
    term1.setText("first Term");
    term1.addListener(SWT.DROP_DOWN, event -> updateTerms(term1));

    Combo term2 = new Combo(subterms, SWT.DROP_DOWN | SWT.READ_ONLY);
    term2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 2));
    term2.setText("second Term");
    term2.setEnabled(false);
    term2.addListener(SWT.DROP_DOWN, event -> updateTerms(term2));
    unary.addListener(SWT.Selection, event -> term2.setEnabled(false));
    binary.addListener(SWT.Selection, event -> term2.setEnabled(true));
    tertiary.addListener(SWT.Selection, event -> term2.setEnabled(true));

    Combo term3 = new Combo(subterms, SWT.DROP_DOWN | SWT.READ_ONLY);
    term3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    term3.setText("third Term");
    term3.setEnabled(false);
    term3.addListener(SWT.DROP_DOWN, event -> updateTerms(term3));
    unary.addListener(SWT.Selection, event -> term3.setEnabled(false));
    binary.addListener(SWT.Selection, event -> term3.setEnabled(false));
    tertiary.addListener(SWT.Selection, event -> term3.setEnabled(true));

    Button termAdd = new Button(subterms, SWT.PUSH);
    termAdd.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    termAdd.setText("Add");
    termAdd.addListener(SWT.Selection, event
        -> handleTerms(unary, binary, operator, funcName, term1, term2, term3));

    // fourth line
    Group equations = new Group(left, SWT.SHADOW_IN);
    equations.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    equations.setLayout(new GridLayout(12, false));
    equations.setText("Equations");

    Group eqType = new Group(equations, SWT.SHADOW_ETCHED_OUT);
    eqType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    eqType.setLayout(new GridLayout(2, true));
    eqType.setText("Type of equation");
    Button conjunc = new Button(eqType, SWT.RADIO);
    conjunc.setText("conjunction");
    Button equal = new Button(eqType, SWT.RADIO);
    equal.setText("equality");
    equal.setSelection(true);

    Text eqName = new Text(equations, SWT.SINGLE);
    eqName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
    eqName.setText("equation name");
    eqName.setToolTipText("The name of the equation");
    eqName.addMouseListener(mouseListener);

    Combo eq1 = new Combo(equations, SWT.DROP_DOWN | SWT.READ_ONLY);
    eq1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    eq1.setText("first Equation");
    eq1.setEnabled(false);
    eq1.addListener(SWT.DROP_DOWN, event -> updateEquations(eq1));
    conjunc.addListener(SWT.Selection, event -> eq1.setEnabled(true));
    equal.addListener(SWT.Selection, event -> eq1.setEnabled(false));

    Label and = new Label(equations, SWT.CENTER);
    and.setText("and");

    Combo eq2 = new Combo(equations, SWT.DROP_DOWN | SWT.READ_ONLY);
    eq2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    eq2.setText("second Equation");
    eq2.setEnabled(false);
    eq2.addListener(SWT.DROP_DOWN, event -> updateEquations(eq2));
    conjunc.addListener(SWT.Selection, event -> eq2.setEnabled(true));
    equal.addListener(SWT.Selection, event -> eq2.setEnabled(false));

    Label comma = new Label(equations, SWT.CENTER);
    comma.setText(",");

    Combo t1 = new Combo(equations, SWT.DROP_DOWN | SWT.READ_ONLY);
    t1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    t1.setText("first Term");
    t1.addListener(SWT.DROP_DOWN, event -> updateTerms(t1));
    conjunc.addListener(SWT.Selection, event -> t1.setEnabled(false));
    equal.addListener(SWT.Selection, event -> t1.setEnabled(true));

    Label equals = new Label(equations, SWT.CENTER);
    equals.setText("=");

    Combo t2 = new Combo(equations, SWT.DROP_DOWN | SWT.READ_ONLY);
    t2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    t2.setText("second Term");
    t2.addListener(SWT.DROP_DOWN, event -> updateTerms(t2));
    conjunc.addListener(SWT.Selection, event -> t2.setEnabled(false));
    equal.addListener(SWT.Selection, event -> t2.setEnabled(true));

    Button eqAdd = new Button(equations, SWT.PUSH);
    eqAdd.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    eqAdd.setText("Add");
    eqAdd.addListener(SWT.Selection, event -> handleEquations(conjunc, eqName, eq1, eq2, t1, t2));

    // fifth line
    Group trusts = new Group(left, SWT.SHADOW_IN);
    trusts.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    trusts.setLayout(new GridLayout(4, false));
    trusts.setText("Trust Relations");

    Combo c1 = new Combo(trusts, SWT.DROP_DOWN | SWT.READ_ONLY);
    c1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    c1.setText("Component1");
    c1.addListener(SWT.DROP_DOWN, event -> updateComponents(c1));

    Label trust = new Label(trusts, SWT.CENTER);
    trust.setText("blindly trusts");

    Combo c2 = new Combo(trusts, SWT.DROP_DOWN | SWT.READ_ONLY);
    c2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    c2.setText("Component2");
    c2.addListener(SWT.DROP_DOWN, event -> updateComponents(c2));

    Button trustAdd = new Button(trusts, SWT.PUSH);
    trustAdd.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    trustAdd.setText("Add");
    trustAdd.addListener(SWT.Selection, event -> archFunc.addTrust(c1.getText(), c2.getText()));

    // sixth line
    Group statements = new Group(left, SWT.SHADOW_IN);
    statements.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    statements.setLayout(new GridLayout(27, false));
    statements.setText("Statements");

    Group stType = new Group(statements, SWT.SHADOW_ETCHED_OUT);
    stType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    stType.setLayout(new GridLayout(2, true));
    stType.setText("Type of statement");
    Button att = new Button(stType, SWT.RADIO);
    att.setText("attest");
    att.setSelection(true);
    Button pro = new Button(stType, SWT.RADIO);
    pro.setText("proof");

    Combo c3 = new Combo(statements, SWT.DROP_DOWN | SWT.READ_ONLY);
    c3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    c3.setText("Component");
    c3.addListener(SWT.DROP_DOWN, event -> updateComponents(c3));

    Label stLabel = new Label(statements, SWT.CENTER);
    stLabel.setText("attests/proves");

    Table eq3 = new Table(statements, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    eq3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 5));
    eq3.setToolTipText("Equations");
    // Debug
    eq3.addListener(SWT.Selection, new Listener() {
      @Override
	public void handleEvent(Event event) {
        if (event.detail == SWT.CHECK) {
          System.out.println("You checked " + event.item);
        } else {
          System.out.println("You selected " + event.item);
          System.out.println(eq3.getSelectionIndices());
          // TableItem ti = (TableItem)event.item;
          // ti.setChecked(!ti.getChecked());
        }
      }
    });
    eqAdd.addListener(SWT.Selection, event -> updateEquationsTab(eq3));

    Label orLabel = new Label(statements, SWT.CENTER);
    orLabel.setText("or");

    Table att1 = new Table(statements, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    att1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 5));
    att1.setToolTipText("Attestations");
    att1.setEnabled(false);
    att.addListener(SWT.Selection, event -> att1.setEnabled(false));
    pro.addListener(SWT.Selection, event -> att1.setEnabled(true));

    Button stAdd = new Button(statements, SWT.PUSH);
    stAdd.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    stAdd.setText("Add");
    stAdd.addListener(SWT.Selection, event -> handleStatement(att, c3, eq3, att1));
    stAdd.addListener(SWT.Selection, event -> updateAttestsTab(att1));

    // seventh line +
    Group actions = new Group(left, SWT.SHADOW_IN);
    actions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    actions.setLayout(new GridLayout(1, false));
    actions.setText("Component Actions");

    // line has
    Group has = new Group(actions, SWT.SHADOW_IN);
    has.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    has.setLayout(new GridLayout(4, false));
    has.setText("Has");

    Combo comp = new Combo(has, SWT.DROP_DOWN | SWT.READ_ONLY);
    comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    comp.setText("Component");
    comp.addListener(SWT.DROP_DOWN, event -> updateComponents(comp));

    Label hasLab = new Label(has, SWT.CENTER);
    hasLab.setText("has");

    Combo var = new Combo(has, SWT.DROP_DOWN | SWT.READ_ONLY);
    var.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    var.setText("Variable");
    var.addListener(SWT.DROP_DOWN, event -> updateVariables(var));

    Button hasAdd = new Button(has, SWT.PUSH);
    hasAdd.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    hasAdd.setText("Add");
    hasAdd.addListener(SWT.Selection, event -> archFunc.addHas(comp.getText(), var.getText()));

    // line compute
    Group compute = new Group(actions, SWT.SHADOW_IN);
    compute.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    compute.setLayout(new GridLayout(4, false));
    compute.setText("Compute");

    Combo comp1 = new Combo(compute, SWT.DROP_DOWN | SWT.READ_ONLY);
    comp1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    comp1.setText("Component");
    comp1.addListener(SWT.DROP_DOWN, event -> updateComponents(comp1));

    Label computeLab = new Label(compute, SWT.CENTER);
    computeLab.setText("computes");

    Combo eq = new Combo(compute, SWT.DROP_DOWN | SWT.READ_ONLY);
    eq.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    eq.setText("new Equation");
    eq.addListener(SWT.DROP_DOWN, event -> updateEquations(eq));

    Button computeAdd = new Button(compute, SWT.PUSH);
    computeAdd.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    computeAdd.setText("Add");
    computeAdd.addListener(
        SWT.Selection, event -> archFunc.addCompute(comp1.getText(), eq.getText()));

    // line receive
    Group receive = new Group(actions, SWT.SHADOW_IN);
    receive.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    receive.setLayout(new GridLayout(75, false));
    receive.setText("Receive");

    Combo comp2 = new Combo(receive, SWT.DROP_DOWN | SWT.READ_ONLY);
    comp2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    comp2.setText("Component");
    comp2.addListener(SWT.DROP_DOWN, event -> updateComponents(comp2));

    Label receiveLab = new Label(receive, SWT.CENTER);
    receiveLab.setText("receives");

    Table stTable = new Table(receive, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    stTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 40, 10));
    stTable.setToolTipText("Statements");
    stAdd.addListener(SWT.Selection, event -> updateStatementsTab(stTable));

    Label andLab = new Label(receive, SWT.CENTER);
    andLab.setText("and");

    Table varTable = new Table(receive, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    varTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 20, 10));
    varTable.setToolTipText("Variables");
    varAdd.addListener(SWT.Selection, event -> updateVarsTab(varTable));

    Label fromLab = new Label(receive, SWT.CENTER);
    fromLab.setText("from");

    Combo comp3 = new Combo(receive, SWT.DROP_DOWN | SWT.READ_ONLY);
    comp3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    comp3.setText("Component");
    comp3.addListener(SWT.DROP_DOWN, event -> updateComponents(comp3));

    Button receiveAdd = new Button(receive, SWT.PUSH);
    receiveAdd.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    receiveAdd.setText("Add");
    receiveAdd.addListener(SWT.Selection, event -> handleReceive(comp2, comp3, stTable, varTable));

    // line check
    Group check = new Group(actions, SWT.SHADOW_IN);
    check.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    check.setLayout(new GridLayout(150, false));
    check.setText("Check");

    Combo comp4 = new Combo(check, SWT.DROP_DOWN | SWT.READ_ONLY);
    comp4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    comp4.setText("Component");
    comp4.addListener(SWT.DROP_DOWN, event -> updateComponents(comp4));

    Label checksLab = new Label(check, SWT.CENTER);
    checksLab.setText("checks");

    Table eqTable = new Table(check, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    eqTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 40, 10));
    eqTable.setToolTipText("Equations");
    eqAdd.addListener(SWT.Selection, event -> updateEquationsTab(eqTable));

    Button checkAdd = new Button(check, SWT.PUSH);
    checkAdd.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    checkAdd.setText("Add");
    checkAdd.addListener(SWT.Selection, event -> handleCheck(comp4, eqTable));
    
    // line verify
    Group verify = new Group(actions, SWT.SHADOW_IN);
    verify.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    verify.setLayout(new GridLayout(75, false));
    verify.setText("Verify");
    
    Group verifType = new Group(verify, SWT.SHADOW_ETCHED_OUT);
    verifType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    verifType.setLayout(new GridLayout(2, true));
    verifType.setText("Type of verification");
    Button attest = new Button(verifType, SWT.RADIO);
    attest.setText("attest");
    Button proof = new Button(verifType, SWT.RADIO);
    proof.setText("proof");
    proof.setSelection(true);
    
    Combo comp10 = new Combo(verify, SWT.DROP_DOWN | SWT.READ_ONLY);
    comp10.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    comp10.setText("Component");
    comp10.addListener(SWT.DROP_DOWN, event -> updateComponents(comp10));
    
    Label verifLab = new Label(verify, SWT.CENTER);
    verifLab.setText("verifies");
    
    Combo proofs = new Combo(verify, SWT.DROP_DOWN | SWT.READ_ONLY);
    proofs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    proofs.setText("Proofs");
    proofs.addListener(SWT.DROP_DOWN, event -> updateProofs(proofs));
    attest.addListener(SWT.Selection, event -> proofs.setEnabled(false));
    proof.addListener(SWT.Selection, event -> proofs.setEnabled(true));

    Label orLabel2 = new Label(verify, SWT.CENTER);
    orLabel2.setText("or");

    Combo attests = new Combo(verify, SWT.DROP_DOWN | SWT.READ_ONLY);
    attests.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    attests.setText("Attestations");
    attests.addListener(SWT.DROP_DOWN, event -> updateAttests(attests));
    attests.setEnabled(false);
    attest.addListener(SWT.Selection, event -> attests.setEnabled(true));
    proof.addListener(SWT.Selection, event -> attests.setEnabled(false));

    Button verifAdd = new Button(verify, SWT.PUSH);
    verifAdd.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    verifAdd.setText("Add");
    verifAdd.addListener(SWT.Selection, event -> handleVerify(attest, comp10, proofs, attests));

    // line delete
    Group delete = new Group(actions, SWT.SHADOW_IN);
    delete.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    delete.setLayout(new GridLayout(75, false));
    delete.setText("Delete");
    
    Combo comp7 = new Combo(delete, SWT.DROP_DOWN | SWT.READ_ONLY);
    comp7.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    comp7.setText("Component");
    comp7.addListener(SWT.DROP_DOWN, event -> updateComponents(comp7));

    Label deleteLab = new Label(delete, SWT.CENTER);
    deleteLab.setText("deletes");
    
    Combo var1 = new Combo(delete, SWT.DROP_DOWN | SWT.READ_ONLY);
    var1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    var1.setText("Variable");
    var1.addListener(SWT.DROP_DOWN, event -> updateVariables(var1));
    
    Button deleteAdd = new Button(delete, SWT.PUSH);
    deleteAdd.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    deleteAdd.setText("Add");
    deleteAdd.addListener(SWT.Selection, event -> archFunc.delete(comp7.getText(), var1.getText()));
    
    // first line
    Group deps = new Group(left, SWT.SHADOW_IN);
    deps.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    deps.setLayout(new GridLayout(86, false));
    deps.setText("Dependence Relations");

    Combo comp9 = new Combo(deps, SWT.DROP_DOWN | SWT.READ_ONLY);
    comp9.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    comp9.setText("Component");
    comp9.addListener(SWT.DROP_DOWN, event -> updateComponents(comp9));

    Label canLab = new Label(deps, SWT.CENTER);
    canLab.setText("has the computational power to arrive");

    Combo var3 = new Combo(deps, SWT.DROP_DOWN | SWT.READ_ONLY);
    var3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    var3.setText("Variable");
    var3.addListener(SWT.DROP_DOWN, event -> updateVariables(var3));

    Label fromLab2 = new Label(deps, SWT.CENTER);
    fromLab2.setText("from");

    Table varTable3 = new Table(deps, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    varTable3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 20, 10));
    varTable3.setToolTipText("Variables");
    varAdd.addListener(SWT.Selection, event -> updateVarsTab(varTable3));
    
    Label withLab = new Label(deps, SWT.CENTER);
    withLab.setText("with the probability of ");

    Text prob1 = new Text(deps, SWT.SINGLE);
    prob1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
    prob1.setText("probability");
    prob1.setToolTipText("The probability of the dependece relation");
    prob1.addMouseListener(mouseListener);

    Button depAdd = new Button(deps, SWT.PUSH);
    depAdd.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    depAdd.setText("Add");
    depAdd.addListener(SWT.Selection, event -> handleDep(comp9, var3, varTable3, prob1));

    // second line
    Group mydeds = new Group(left, SWT.SHADOW_IN);
    mydeds.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    mydeds.setLayout(new GridLayout(88, false));
    mydeds.setText("Custom Deductions");

    Text dedName = new Text(mydeds, SWT.SINGLE);
    dedName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
    dedName.setText("name");
    dedName.setToolTipText("The name of the deduction");
    dedName.addMouseListener(mouseListener);

    Table premiseTable = new Table(mydeds, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    premiseTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 40, 10));
    premiseTable.setToolTipText("Premise Equations");
    eqAdd.addListener(SWT.Selection, event -> updateEquationsTab(premiseTable));

    Label dedLabel = new Label(mydeds, SWT.CENTER);
    dedLabel.setText("can be used to deduce this equation:");

    Combo conclusion = new Combo(mydeds, SWT.DROP_DOWN | SWT.READ_ONLY);
    conclusion.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    conclusion.setText("Conclusion Equation");
    conclusion.addListener(SWT.DROP_DOWN, event -> updateEquations(conclusion));
    
    Label withLab2 = new Label(mydeds, SWT.CENTER);
    withLab2.setText("with the probability of ");

    Text prob2 = new Text(mydeds, SWT.SINGLE);
    prob2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
    prob2.setText("probability");
    prob2.setToolTipText("The probability of the custom deduction");
    prob2.addMouseListener(mouseListener);

    Button mydedAdd = new Button(mydeds, SWT.PUSH);
    mydedAdd.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    mydedAdd.setText("Add");
    mydedAdd.addListener(SWT.Selection, event -> handlemyDed(dedName, premiseTable, conclusion, prob2));

    // third line
    Group deds = new Group(left, SWT.SHADOW_IN);
    deds.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    deds.setLayout(new GridLayout(64, false));
    deds.setText("Deductions");

    Combo comp8 = new Combo(deds, SWT.DROP_DOWN | SWT.READ_ONLY);
    comp8.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    comp8.setText("Component");
    comp8.addListener(SWT.DROP_DOWN, event -> updateComponents(comp8));

    Label can2Lab = new Label(deds, SWT.CENTER);
    can2Lab.setText("can deduce equations using");

    Table dedTable = new Table(deds, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    dedTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 20, 10));
    dedTable.setToolTipText("Standard Deductions");
    for (Deduction d : archFunc.getDeducs()) {
      TableItem item = new TableItem(dedTable, SWT.None);
      item.setText(d.toString());
    }
    mydedAdd.addListener(SWT.Selection, event -> updateDedTab(dedTable));

    Button dedAdd = new Button(deds, SWT.PUSH);
    dedAdd.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    dedAdd.setText("Add");
    dedAdd.addListener(SWT.Selection, event -> handleDed(comp8, dedTable));

    // ##################### right side ##########################
    // first line
    Group components2 = new Group(right, SWT.SHADOW_IN);
    components2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    components2.setLayout(new GridLayout(1, true));
    components2.setText("Components");

    Table compTable = new Table(components2, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    compTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 5));
    compTable.setToolTipText("List of all components");
    compAdd.addListener(SWT.Selection, event -> updateCompsTab(compTable));

    Button compRemove = new Button(components2, SWT.PUSH);
    compRemove.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    compRemove.setText("Remove");
    compRemove.addListener(SWT.Selection, event -> handleRemove(ObjectType.COMP, compTable));

    // second line
    Group variables2 = new Group(right, SWT.SHADOW_IN);
    variables2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    variables2.setLayout(new GridLayout(1, true));
    variables2.setText("Variables");

    Table variableTable = new Table(
        variables2, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    variableTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 5));
    variableTable.setToolTipText("List of all variables");
    varAdd.addListener(SWT.Selection, event -> updateVarsTab(variableTable));

    Button varRemove = new Button(variables2, SWT.PUSH);
    varRemove.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    varRemove.setText("Remove");
    varRemove.addListener(SWT.Selection, event -> handleRemove(ObjectType.VAR, variableTable));

    // third line
    Group terms2 = new Group(right, SWT.SHADOW_IN);
    terms2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    terms2.setLayout(new GridLayout(1, true));
    terms2.setText("Terms");

    Table termTable = new Table(terms2, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    termTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 5));
    termTable.setToolTipText("List of all terms");
    termAdd.addListener(SWT.Selection, event -> updateTermsTab(termTable));
    varAdd.addListener(SWT.Selection, event -> updateTermsTab(termTable));

    Button termRemove = new Button(terms2, SWT.PUSH);
    termRemove.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    termRemove.setText("Remove");
    termRemove.addListener(SWT.Selection, event -> handleRemove(ObjectType.TERM, termTable));

    // fourth line
    Group equations2 = new Group(right, SWT.SHADOW_IN);
    equations2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    equations2.setLayout(new GridLayout(1, true));
    equations2.setText("Equations");

    Table equationTable = new Table(
        equations2, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    equationTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 5));
    equationTable.setToolTipText("List of all equations");
    eqAdd.addListener(SWT.Selection, event -> updateEquationsTab(equationTable));

    Button eqRemove = new Button(equations2, SWT.PUSH);
    eqRemove.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    eqRemove.setText("Remove");
    eqRemove.addListener(SWT.Selection, event -> handleRemove(ObjectType.EQ, equationTable));

    // fifth line
    Group trusts2 = new Group(right, SWT.SHADOW_IN);
    trusts2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    trusts2.setLayout(new GridLayout(1, true));
    trusts2.setText("Trust Relations");

    Table trustTable = new Table(trusts2, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    trustTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 5));
    trustTable.setToolTipText("List of all trust relations");
    trustAdd.addListener(SWT.Selection, event -> updateTrustTab(trustTable));

    Button trustRemove = new Button(trusts2, SWT.PUSH);
    trustRemove.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    trustRemove.setText("Remove");
    trustRemove.addListener(SWT.Selection, event -> handleRemove(ObjectType.TRUST, trustTable));

    // sixth line
    Group statements2 = new Group(right, SWT.SHADOW_IN);
    statements2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    statements2.setLayout(new GridLayout(1, true));
    statements2.setText("Statements");

    Table stmtTable = new Table(statements2, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    stmtTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 5));
    stmtTable.setToolTipText("List of all proofs and attestations");
    stAdd.addListener(SWT.Selection, event -> updateStatementTab(stmtTable));

    Button stRemove = new Button(statements2, SWT.PUSH);
    stRemove.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    stRemove.setText("Remove");
    stRemove.addListener(SWT.Selection, event -> handleRemove(ObjectType.STMT, stmtTable));

    // seventh line
    Group actions2 = new Group(right, SWT.SHADOW_IN);
    actions2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    actions2.setLayout(new GridLayout(1, true));
    actions2.setText("Actions");

    Table actionTable = new Table(actions2, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    actionTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 5));
    actionTable.setToolTipText("List of all actions");
    hasAdd.addListener(SWT.Selection, event -> updateActionsTab(actionTable));
    computeAdd.addListener(SWT.Selection, event -> updateActionsTab(actionTable));
    receiveAdd.addListener(SWT.Selection, event -> updateActionsTab(actionTable));
    checkAdd.addListener(SWT.Selection, event -> updateActionsTab(actionTable));
    deleteAdd.addListener(SWT.Selection, event -> updateActionsTab(actionTable));
    verifAdd.addListener(SWT.Selection, event -> updateActionsTab(actionTable));

    Button actRemove = new Button(actions2, SWT.PUSH);
    actRemove.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    actRemove.setText("Remove");
    actRemove.addListener(SWT.Selection, event -> handleRemove(ObjectType.ACT, actionTable));

    // eighth line
    Group deps2 = new Group(right, SWT.SHADOW_IN);
    deps2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    deps2.setLayout(new GridLayout(1, true));
    deps2.setText("Dependence Relations");

    Table depTable = new Table(deps2, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    depTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 5));
    depTable.setToolTipText("List of all dependece relations");
    depAdd.addListener(SWT.Selection, event -> updateDepsTab(depTable));

    Button depRemove = new Button(deps2, SWT.PUSH);
    depRemove.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    depRemove.setText("Remove");
    depRemove.addListener(SWT.Selection, event -> handleRemove(ObjectType.DEP, depTable));

    // ninth line
    Group deds2 = new Group(right, SWT.SHADOW_IN);
    deds2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    deds2.setLayout(new GridLayout(1, true));
    deds2.setText("Deduction Capabilities");

    Table dedTable2 = new Table(deds2, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    dedTable2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 10));
    dedTable2.setToolTipText("List of all dependece relations");
    dedAdd.addListener(SWT.Selection, event -> updateDedsTab(dedTable2));

    Button dedRemove = new Button(deds2, SWT.PUSH);
    dedRemove.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    dedRemove.setText("Remove");
    dedRemove.addListener(SWT.Selection, event -> handleRemove(ObjectType.DED, dedTable2));

    // set the first tab
    tab1.setControl(bothSides1);

    // -------------------------- second tab -----------------------
    TabItem tab2 = new TabItem(folder, SWT.NONE);
    tab2.setText("Property Verification");

    // left and right sides
    Composite bothSides2 = new Composite(folder, SWT.NONE);
    bothSides2.setLayout(new GridLayout(2, true)); //false
    Composite left2 = new Composite(bothSides2, SWT.BORDER);
    left2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    left2.setLayout(new GridLayout(1, true));
    left2.setBackground(display.getSystemColor(SWT.COLOR_GRAY));
    Composite right2 = new Composite(bothSides2, SWT.BORDER);
    right2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    right2.setLayout(new GridLayout(1, true));

    finishButton.addListener(SWT.Selection, event -> recursiveSetEnabled(left2, true));
    finishButton.addListener(SWT.Selection, event -> recursiveSetEnabled(right2, true));
    finishButton.addListener(SWT.Selection, event -> recursiveSetEnabled(left, false));
    finishButton.addListener(SWT.Selection, event -> recursiveSetEnabled(right, false));
    editBtn.addListener(SWT.Selection, event -> recursiveSetEnabled(right, true));
    editBtn.addListener(SWT.Selection, event -> recursiveSetEnabled(left, true));
    editBtn.addListener(SWT.Selection, event -> recursiveSetEnabled(right2, false));
    editBtn.addListener(SWT.Selection, event -> recursiveSetEnabled(left2, false));

    // left side
    // first line
    Group properties = new Group(left2, SWT.SHADOW_IN);
    properties.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    properties.setLayout(new GridLayout(1, false));
    properties.setText("Properties");

    // line has
    Group hasProp = new Group(properties, SWT.SHADOW_IN);
    hasProp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    hasProp.setLayout(new GridLayout(7, false));
    hasProp.setText("Has");

    Combo compProp = new Combo(hasProp, SWT.DROP_DOWN | SWT.READ_ONLY);
    compProp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    compProp.setText("Component");
    compProp.addListener(SWT.DROP_DOWN, event -> updateComponents(compProp));

    Label hasLabProp = new Label(hasProp, SWT.CENTER);
    hasLabProp.setText("has with probability ");
    
    Text prob3 = new Text(hasProp, SWT.SINGLE);
    prob3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
    prob3.setText("probability");
    prob3.setToolTipText("The minimal probability of the has property");
    prob3.addMouseListener(mouseListener);

    Combo varProp = new Combo(hasProp, SWT.DROP_DOWN | SWT.READ_ONLY);
    varProp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    varProp.setText("Variable");
    varProp.addListener(SWT.DROP_DOWN, event -> updateVariables(varProp));

    Button hasPropAdd = new Button(hasProp, SWT.PUSH);
    hasPropAdd.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    hasPropAdd.setText("Add");
    hasPropAdd.addListener(SWT.Selection,
        event -> archFunc.addPropHas(compProp.getText(), varProp.getText(), prob3.getText()));

    // line knows
    Group knowsProp = new Group(properties, SWT.SHADOW_IN);
    knowsProp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    knowsProp.setLayout(new GridLayout(7, false));
    knowsProp.setText("Knows");

    Combo compProp1 = new Combo(knowsProp, SWT.DROP_DOWN | SWT.READ_ONLY);
    compProp1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    compProp1.setText("Component");
    compProp1.addListener(SWT.DROP_DOWN, event -> updateComponents(compProp1));

    Label knowsLabProp = new Label(knowsProp, SWT.CENTER);
    knowsLabProp.setText("knows with probability ");
    
    Text prob4 = new Text(knowsProp, SWT.SINGLE);
    prob4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
    prob4.setText("probability");
    prob4.setToolTipText("The minimal probability of the knows property");
    prob4.addMouseListener(mouseListener);

    Combo eqProp = new Combo(knowsProp, SWT.DROP_DOWN | SWT.READ_ONLY);
    eqProp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    eqProp.setText("Equation");
    eqProp.addListener(SWT.DROP_DOWN, event -> updateEquations(eqProp));

    Button knowsPropAdd = new Button(knowsProp, SWT.PUSH);
    knowsPropAdd.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    knowsPropAdd.setText("Add");
    knowsPropAdd.addListener(
        SWT.Selection, event -> archFunc.addPropKnows(compProp1.getText(), eqProp.getText(), prob4.getText()));

    // line notshared
    Group notsharedProp = new Group(properties, SWT.SHADOW_IN);
    notsharedProp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    notsharedProp.setLayout(new GridLayout(5, false));
    notsharedProp.setText("NotShared");

    Combo compProp3 = new Combo(notsharedProp, SWT.DROP_DOWN | SWT.READ_ONLY);
    compProp3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    compProp3.setText("Component");
    compProp3.addListener(SWT.DROP_DOWN, event -> updateComponents(compProp3));

    Label notSharedLabProp = new Label(notsharedProp, SWT.CENTER);
    notSharedLabProp.setText("does not share");

    Combo varProp1 = new Combo(notsharedProp, SWT.DROP_DOWN | SWT.READ_ONLY);
    varProp1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    varProp1.setText("Variable");
    varProp1.addListener(SWT.DROP_DOWN, event -> updateVariables(varProp1));

    Label notSharedLabProp1 = new Label(notsharedProp, SWT.CENTER);
    notSharedLabProp1.setText("with a third party");

    Button notSharedPropAdd = new Button(notsharedProp, SWT.PUSH);
    notSharedPropAdd.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    notSharedPropAdd.setText("Add");
    notSharedPropAdd.addListener(SWT.Selection,
        event -> archFunc.addPropNotShared(compProp3.getText(), varProp1.getText()));

    // line notstored
    Group notstoredProp = new Group(properties, SWT.SHADOW_IN);
    notstoredProp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    notstoredProp.setLayout(new GridLayout(6, false));
    notstoredProp.setText("NotStored");

    Combo compProp4 = new Combo(notstoredProp, SWT.DROP_DOWN | SWT.READ_ONLY);
    compProp4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    compProp4.setText("Component");
    compProp4.addListener(SWT.DROP_DOWN, event -> updateComponents(compProp4));

    Label notStoredLabProp = new Label(notstoredProp, SWT.CENTER);
    notStoredLabProp.setText("does not store");

    Combo varProp2 = new Combo(notstoredProp, SWT.DROP_DOWN | SWT.READ_ONLY);
    varProp2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    varProp2.setText("Variable");
    varProp2.addListener(SWT.DROP_DOWN, event -> updateVariables(varProp2));

    Label notStoredLabProp2 = new Label(notstoredProp, SWT.CENTER);
    notStoredLabProp2.setText("with bound");
    
    Combo boundProp = new Combo(notstoredProp, SWT.DROP_DOWN | SWT.READ_ONLY);
    boundProp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
    boundProp.setText("Bound");
    for (int i=1; i<10; i++) {
    	boundProp.add("" + i);
    }
    //boundProp.addListener(SWT.DROP_DOWN, event -> updateComponents(compProp4));

    Button notStoredPropAdd = new Button(notstoredProp, SWT.PUSH);
    notStoredPropAdd.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    notStoredPropAdd.setText("Add");
    notStoredPropAdd.addListener(SWT.Selection,
        event -> archFunc.addPropNotStored(compProp4.getText(), varProp2.getText(), boundProp.getText()));

    // line negation
    Group negProp = new Group(properties, SWT.SHADOW_IN);
    negProp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    negProp.setLayout(new GridLayout(84, false));
    negProp.setText("Negation");
    
    Label notLabProp = new Label(negProp, SWT.CENTER);
    notLabProp.setText("NOT");

    Combo propProp = new Combo(negProp, SWT.DROP_DOWN | SWT.READ_ONLY);
    propProp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 2));
    propProp.setText("Property");
    propProp.addListener(SWT.DROP_DOWN, event -> updateProps(propProp));

    Button negPropAdd = new Button(negProp, SWT.PUSH);
    negPropAdd.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    negPropAdd.setText("Add");
    negPropAdd.addListener(
        SWT.Selection, event -> archFunc.addPropNeg(propProp.getText()));
    
    // line composition
    Group conjProp = new Group(properties, SWT.SHADOW_IN);
    conjProp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    conjProp.setLayout(new GridLayout(84, false));
    conjProp.setText("Composition");

    Combo propProp1 = new Combo(conjProp, SWT.DROP_DOWN | SWT.READ_ONLY);
    propProp1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 2));
    propProp1.setText("Property1");
    propProp1.addListener(SWT.DROP_DOWN, event -> updateProps(propProp1));

    Label andLabProp = new Label(conjProp, SWT.CENTER);
    andLabProp.setText("AND");

    Combo propProp2 = new Combo(conjProp, SWT.DROP_DOWN | SWT.READ_ONLY);
    propProp2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 2));
    propProp2.setText("Property2");
    propProp2.addListener(SWT.DROP_DOWN, event -> updateProps(propProp2));

    Button conjPropAdd = new Button(conjProp, SWT.PUSH);
    conjPropAdd.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    conjPropAdd.setText("Add");
    conjPropAdd.addListener(
        SWT.Selection, event -> archFunc.addPropConj(propProp1.getText(), propProp2.getText()));

    // second line
    Group verification = new Group(left2, SWT.SHADOW_IN);
    verification.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    verification.setLayout(new GridLayout(122, false));
    verification.setText("Verification");

    Combo prop = new Combo(verification, SWT.DROP_DOWN | SWT.H_SCROLL | SWT.READ_ONLY);
    prop.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 2));
    prop.setText("Property");
    prop.addListener(SWT.DROP_DOWN, event -> updateProps(prop));

    Button verifyBtn = new Button(verification, SWT.PUSH);
    verifyBtn.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    verifyBtn.setText("Verify");

    // third line
    Group verified = new Group(left2, SWT.SHADOW_IN);
    verified.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    verified.setLayout(new GridLayout(122, false));
    verified.setText("Verified Properties");

    Table verifiedProps = new Table(verified, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    verifiedProps.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 15));
    verifiedProps.setToolTipText("List of all verified properties");
    verifiedProps.addListener(SWT.Selection, event -> updateVerifTab(event, verifiedProps));
    verifyBtn.addListener(SWT.Selection, event -> verifyProp(prop.getText(), verifiedProps));

    Button inspect = new Button(verified, SWT.PUSH);
    inspect.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    inspect.setText("Inspect Proof");
    inspect.addListener(SWT.Selection, event -> showTrace(verifiedProps));

    // ################## right side ##################################
    // first line
    Group props = new Group(right2, SWT.SHADOW_IN);
    props.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    props.setLayout(new GridLayout(1, true));
    props.setText("Properties");

    Table propTable = new Table(props, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    propTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 5));
    propTable.setToolTipText("List of all architecture properties to verify");
    hasPropAdd.addListener(SWT.Selection, event -> updatePropsTab(propTable));
    knowsPropAdd.addListener(SWT.Selection, event -> updatePropsTab(propTable));
    notSharedPropAdd.addListener(SWT.Selection, event -> updatePropsTab(propTable));
    notStoredPropAdd.addListener(SWT.Selection, event -> updatePropsTab(propTable));
    conjPropAdd.addListener(SWT.Selection, event -> updatePropsTab(propTable));
    negPropAdd.addListener(SWT.Selection, event -> updatePropsTab(propTable));

    Button propRemove = new Button(props, SWT.PUSH);
    propRemove.setLayoutData(new GridData(SWT.MIN, SWT.FILL, false, false, 1, 1));
    propRemove.setText("Remove");
    propRemove.addListener(SWT.Selection, event -> handleRemove(ObjectType.PROP, propTable));

    // set second tab
    tab2.setControl(bothSides2);

    // initially disable the second tab
    recursiveSetEnabled(left2, false);
    recursiveSetEnabled(right2, false);

    // load/save/reset events
    resetBtn.addSelectionListener(new SelectionAdapter() {
      @Override
	public void widgetSelected(SelectionEvent event) {
        if (showMessage(MessageType.WARN, "Should the architecture really be reset?")) {
          reset();
          verifiedProps.removeAll();
          finishButton.setEnabled(true);
          loadButton.setEnabled(true);
          saveButton.setEnabled(true);
          loadButton2.setEnabled(true);
          folder.setSelection(0);
          drawButton.setEnabled(false);
          recursiveSetEnabled(right, true);
          recursiveSetEnabled(right2, false);
          recursiveSetEnabled(left, true);
          recursiveSetEnabled(left2, false);
        }
      }
    });
    loadButton2.addSelectionListener(new SelectionAdapter() {
      @Override
	public void widgetSelected(SelectionEvent event) {
        if (showMessage(MessageType.WARN,
            "The current architecture will be overwritten by the load. Continue?")) {
          archFunc = SaveLoadArch.loadArch(archName.getText());
          verifiedProps.removeAll();
        }
      }
    });
    loadButton.addSelectionListener(new SelectionAdapter() {
      @Override
	public void widgetSelected(SelectionEvent event) {
        if (showMessage(MessageType.WARN,
            "The current architecture will be overwritten by the load. Continue?")) {
          archFunc.load(examples.getText());
          archFunc.finish();
          verifiedProps.removeAll();
          //loadButton.setEnabled(false);
          finishButton.setEnabled(false);
          folder.setSelection(1);
          drawButton.setEnabled(true);
          recursiveSetEnabled(right, false);
          recursiveSetEnabled(right2, true);
          recursiveSetEnabled(left, false);
          recursiveSetEnabled(left2, true);
        }
      }
    });
    // events triggered by reset and loads
    resetBtn.addListener(SWT.Selection, event -> updatePropsTab(propTable));
    //resetBtn.addListener(SWT.Selection, event -> updateVerifiedTab(verifiedProps));
    resetBtn.addListener(SWT.Selection, event -> updateDedsTab(dedTable2));
    resetBtn.addListener(SWT.Selection, event -> updateDepsTab(depTable));
    resetBtn.addListener(SWT.Selection, event -> updateDedsTab(dedTable));
    resetBtn.addListener(SWT.Selection, event -> updateEquationsTab(premiseTable));
    resetBtn.addListener(SWT.Selection, event -> updateVarsTab(varTable3));
    resetBtn.addListener(SWT.Selection, event -> updateActionsTab(actionTable));
    resetBtn.addListener(SWT.Selection, event -> updateStatementsTab(stmtTable));
    resetBtn.addListener(SWT.Selection, event -> updateTrustTab(trustTable));
    resetBtn.addListener(SWT.Selection, event -> updateEquationsTab(equationTable));
    resetBtn.addListener(SWT.Selection, event -> updateTermsTab(termTable));
    resetBtn.addListener(SWT.Selection, event -> updateVarsTab(variableTable));
    resetBtn.addListener(SWT.Selection, event -> updateCompsTab(compTable));
    //resetBtn.addListener(SWT.Selection, event -> updateEquationsTab(eqTable2));
    resetBtn.addListener(SWT.Selection, event -> updateEquationsTab(eqTable));
    resetBtn.addListener(SWT.Selection, event -> updateVarsTab(varTable));
    resetBtn.addListener(SWT.Selection, event -> updateStatementsTab(stTable));
    resetBtn.addListener(SWT.Selection, event -> updateAttestsTab(att1));
    resetBtn.addListener(SWT.Selection, event -> updateEquationsTab(eq3));
    loadButton.addListener(SWT.Selection, event -> updatePropsTab(propTable));
    loadButton2.addListener(SWT.Selection, event -> updatePropsTab(propTable));
    //loadButton.addListener(SWT.Selection, event -> updateVerifiedTab(verifiedProps));
    //loadButton2.addListener(SWT.Selection, event -> updateVerifiedTab(verifiedProps));
    loadButton.addListener(SWT.Selection, event -> updateDedsTab(dedTable2));
    loadButton2.addListener(SWT.Selection, event -> updateDedsTab(dedTable2));
    loadButton.addListener(SWT.Selection, event -> updateDepsTab(depTable));
    loadButton2.addListener(SWT.Selection, event -> updateDepsTab(depTable));
    loadButton.addListener(SWT.Selection, event -> updateActionsTab(actionTable));
    loadButton2.addListener(SWT.Selection, event -> updateActionsTab(actionTable));
    loadButton.addListener(SWT.Selection, event -> updateStatementTab(stmtTable));
    loadButton2.addListener(SWT.Selection, event -> updateStatementTab(stmtTable));
    loadButton.addListener(SWT.Selection, event -> updateTrustTab(trustTable));
    loadButton2.addListener(SWT.Selection, event -> updateTrustTab(trustTable));
    loadButton.addListener(SWT.Selection, event -> updateEquationsTab(equationTable));
    loadButton2.addListener(SWT.Selection, event -> updateEquationsTab(equationTable));
    loadButton.addListener(SWT.Selection, event -> updateTermsTab(termTable));
    loadButton2.addListener(SWT.Selection, event -> updateTermsTab(termTable));
    loadButton.addListener(SWT.Selection, event -> updateVarsTab(variableTable));
    loadButton2.addListener(SWT.Selection, event -> updateVarsTab(variableTable));
    loadButton.addListener(SWT.Selection, event -> updateCompsTab(compTable));
    loadButton2.addListener(SWT.Selection, event -> updateCompsTab(compTable));
    loadButton.addListener(SWT.Selection, event -> updateDedTab(dedTable));
    loadButton2.addListener(SWT.Selection, event -> updateDedTab(dedTable));
    loadButton.addListener(SWT.Selection, event -> updateVarsTab(varTable3));
    loadButton2.addListener(SWT.Selection, event -> updateVarsTab(varTable3));
    loadButton.addListener(SWT.Selection, event -> updateEquationsTab(premiseTable));
    loadButton2.addListener(SWT.Selection, event -> updateEquationsTab(premiseTable));
    //loadButton.addListener(SWT.Selection, event -> updateEquationsTab(eqTable2));
    //loadButton2.addListener(SWT.Selection, event -> updateEquationsTab(eqTable2));
    loadButton.addListener(SWT.Selection, event -> updateEquationsTab(eqTable));
    loadButton2.addListener(SWT.Selection, event -> updateEquationsTab(eqTable));
    loadButton.addListener(SWT.Selection, event -> updateVarsTab(varTable));
    loadButton2.addListener(SWT.Selection, event -> updateVarsTab(varTable));
    loadButton.addListener(SWT.Selection, event -> updateStatementsTab(stTable));
    loadButton2.addListener(SWT.Selection, event -> updateStatementsTab(stTable));
    loadButton.addListener(SWT.Selection, event -> updateAttestsTab(att1));
    loadButton2.addListener(SWT.Selection, event -> updateAttestsTab(att1));
    loadButton.addListener(SWT.Selection, event -> updateEquationsTab(eq3));
    loadButton2.addListener(SWT.Selection, event -> updateEquationsTab(eq3));

    // finishing touch
    sc.setContent(everything);
    sc.setMinSize(everything.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    sc.setExpandVertical(true);
    sc.setExpandHorizontal(true);

    shell.setMaximized(true);
    shell.open();

    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    display.dispose();
  }

  /**
   * Helper method to dis/enable controls recursively in a composite chain.
   * 
   * @param ctrl
   *          the start element
   * @param enabled
   *          true or false
   */
  private void recursiveSetEnabled(Control ctrl, boolean enabled) {
    if (ctrl instanceof Composite) {
      // for composites make recursive call
      Composite comp = (Composite) ctrl;
      for (Control c : comp.getChildren()) {
        recursiveSetEnabled(c, enabled);
      }
    } else {
      // dis/enable all 'leaf' controls
      ctrl.setEnabled(enabled);
    }
  }

  /**
   * Method to reset the architecture and the properties.
   */
  private static void reset() {
    // reset the architecture objects and all properties
    archFunc = new ArchitectureFunctions();
  }

  /**
   * Method to open a new shell with a diagram representing the modeled
   * architecture.
   */
  public static void showDiagram() {
    // some initializing
    final Shell shell = new Shell(display);
    shell.setMaximized(true);
    shell.setText("Architecture Diagram");
    final LightweightSystem lws = new LightweightSystem(shell);
    Figure contents = new Figure();
    XYLayout contentsLayout = new XYLayout();
    contents.setLayoutManager(contentsLayout);

    Font classFont = new Font(null, "Arial", 18, SWT.BOLD);
    Font regularFont = new Font(null, "Arial", 12, SWT.NONE);

    // go through architecture and add components and relations
    Architecture arch2draw = archFunc.getArch();
    List<ComponentFigure> components = new ArrayList<ComponentFigure>();
    for (Component c : arch2draw.getCompList()) {
      // create a class object for the component
      org.eclipse.draw2d.Label classLabel = new org.eclipse.draw2d.Label(c.getName());
      classLabel.setFont(classFont);
      final ComponentFigure classFigure = new ComponentFigure(classLabel);
      // add labels for each action
      for (Action a : c.getActions()) {
        org.eclipse.draw2d.Label action = new org.eclipse.draw2d.Label(a.toString());
        classFigure.getMethodsCompartment().add(action);
      }
      contents.add(classFigure);
      components.add(classFigure);
    }

    // fixed positions
    // TODO more than 4?
    org.eclipse.swt.graphics.Rectangle shellBounds = display.getBounds();
    // DEBUG
    Monitor primary = display.getPrimaryMonitor();
    System.out.println("Display bounds: " + shellBounds.width + ", " + shellBounds.height);
    System.out.println(
        "Primary monitor bounds: " + primary.getBounds().width + ", " + primary.getBounds().height);
    int right = primary.getBounds().width - 850; // 500
    int bottom = primary.getBounds().height - 350; // - 350
    int topLeft = 50; // 100
    Rectangle rectTl = new Rectangle(topLeft, topLeft, -1, -1);
    Rectangle rectTr = new Rectangle(right, topLeft, -1, -1);
    Rectangle rectBl = new Rectangle(topLeft, bottom, -1, -1);
    Rectangle rectBr = new Rectangle(right, bottom, -1, -1);
    if (arch2draw.getCompList().size() == 2) {
      contentsLayout.setConstraint(components.get(0), rectTl);
      contentsLayout.setConstraint(components.get(1), rectTr);
    } else if (arch2draw.getCompList().size() == 3) {
      contentsLayout.setConstraint(components.get(0), rectTl);
      contentsLayout.setConstraint(components.get(1), rectTr);
      contentsLayout.setConstraint(components.get(2), rectBl);
    } else if (arch2draw.getCompList().size() == 4) {
      contentsLayout.setConstraint(components.get(0), rectTl);
      contentsLayout.setConstraint(components.get(1), rectTr);
      contentsLayout.setConstraint(components.get(2), rectBl);
      contentsLayout.setConstraint(components.get(3), rectBr);
    }

    // go through the inter-component actions and add the connections
    List<PolylineConnection> connections = new ArrayList<PolylineConnection>();
    for (Action a : arch2draw.getInterComp_Actions()) {
      Component c1 = null;
      Component c2 = null;
      switch (a.getAction()) {
        case RECEIVE:
          // source and destination have to be defined
          c1 = a.getComPartner();
          c2 = a.getComponent();
          break;
        default:
          // should not happen
          break;
      }
      PolylineConnection connection = new PolylineConnection();
      connection.setFont(regularFont);
      connection.setLineWidth(2);
      connection.setConnectionRouter(new FanRouter());
      connection.setSourceAnchor(
          new ChopboxAnchor(components.get(new ArrayList<Component>(arch2draw.getCompList()).indexOf(c1))));
      connection.setTargetAnchor(
          new ChopboxAnchor(components.get(new ArrayList<Component>(arch2draw.getCompList()).indexOf(c2))));
      // adding the arrow-head
      PolygonDecoration arrow = new PolygonDecoration();
      arrow.setTemplate(PolygonDecoration.TRIANGLE_TIP);
      arrow.setScale(20, 10);
      connection.setTargetDecoration(arrow);
      // adding the description
      ConnectionEndpointLocator relationshipLocator2 = new ConnectionEndpointLocator(
          connection, true);
      org.eclipse.draw2d.Label relationshipLabel2 = new org.eclipse.draw2d.Label(a.toString());
      // check if already a connection exists
      PolylineConnection oldConnection = existsConnection(connection, connections);
      if (oldConnection != null) {
        // add new label to existing one
        String oldText = ((org.eclipse.draw2d.Label) oldConnection.getChildren().get(1)).getText();
        ((org.eclipse.draw2d.Label) oldConnection.getChildren().get(1))
        .setText(oldText + System.lineSeparator() + a.toString());
      } else {
        connection.add(relationshipLabel2, relationshipLocator2);
        // put the finished connection onto the plane
        contents.add(connection);
        connections.add(connection);
      }
    }
    // also add trust relations
    for (Trust trust : arch2draw.getTrusts()) {
      PolylineConnection connection = new PolylineConnection();
      connection.setLineWidth(2);
      connection.setFont(regularFont);
      connection.setConnectionRouter(new FanRouter());
      connection.setSourceAnchor(
          new ChopboxAnchor(components.get(new ArrayList<Component>(arch2draw.getCompList()).indexOf(trust.getTruster()))));
      connection.setTargetAnchor(
          new ChopboxAnchor(components.get(new ArrayList<Component>(arch2draw.getCompList()).indexOf(trust.getTrustee()))));
      // adding the arrow-head
      PolygonDecoration arrow = new PolygonDecoration();
      arrow.setTemplate(PolygonDecoration.TRIANGLE_TIP);
      arrow.setScale(20, 10);
      connection.setTargetDecoration(arrow);
      // adding the description
      MidpointLocator relationshipLocator = new MidpointLocator(connection, 0);
      org.eclipse.draw2d.Label relationshipLabel = new org.eclipse.draw2d.Label(trust.toString());
      connection.add(relationshipLabel, relationshipLocator);
      // put the finished connection onto the plane
      contents.add(connection);
    }

    lws.setContents(contents);
    shell.open();
    // main loop
    while (!shell.isDisposed()) {
      while (!display.readAndDispatch()) {
        display.sleep();
      }
    }
  }

  /**
   * Method to check if a connection already exists between two components.
   * 
   * @param connection
   *          the new connection
   * @param connections
   *          the existing connections
   * @return the match if found, null else
   */
  private static PolylineConnection existsConnection(PolylineConnection connection,
      List<PolylineConnection> connections) {
    // go through all existing connections end check if there already is one
    // with the same start and end
    for (PolylineConnection currentConnection : connections) {
      if (currentConnection.getSourceAnchor().getOwner().equals(
          connection.getSourceAnchor().getOwner()) && currentConnection.getTargetAnchor()
          .getOwner().equals(connection.getTargetAnchor().getOwner())) {
        // TODO DEBUG
        System.out.println("found");
        // return the existing connection
        return currentConnection;
      }
    }
    // no existing connection found
    return null;
  }

  /**
   * Method for user directed output and logs like verification traces.
   * 
   * @param type
   *          the type of message
   * @param message
   *          the message itself
   * @return false, if the message box was canceled
   */
  public static boolean showMessage(MessageType type, String message) {
    switch (type) {
      case ERR:
        MessageBox errBox = new MessageBox(
            display.getActiveShell(), SWT.ICON_ERROR | SWT.RETRY | SWT.CANCEL);
        errBox.setText("Error");
        errBox.setMessage(message);
        int buttonId = errBox.open();
        switch (buttonId) {
          case SWT.CANCEL:
            // do nothing
            return false;
          case SWT.RETRY:
            reset();
            return true;
          default:
            break;
        }
        break;
      case INF:
        MessageBox infBox = new MessageBox(display.getActiveShell(), SWT.ICON_INFORMATION | SWT.OK);
        infBox.setText("Info");
        infBox.setMessage(message);
        infBox.open();
        return true;
      case LOG:
        // open a new dialog to show a scollable text field
        Shell dialog = new Shell(display.getActiveShell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.setMaximized(true);
        dialog.setText("Trace");
        dialog.setLayout(new FillLayout());

        Composite comp = new Composite(dialog, SWT.NONE);
        comp.setLayout(new GridLayout(2, true));
        comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

        Text trace = new Text(
            comp, SWT.MULTI | SWT.READ_ONLY | SWT.LEFT | SWT.V_SCROLL | SWT.H_SCROLL);
        trace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        trace.setText(message);
        trace.setToolTipText("The verification trace of the selected property.");

        Text rules = new Text(
            comp, SWT.MULTI | SWT.READ_ONLY | SWT.LEFT | SWT.V_SCROLL | SWT.H_SCROLL);
        rules.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        try {
          rules.setText(FileReader.readFile("./configs/roi.txt"));
        } catch (IOException e) {
          e.printStackTrace();
        }
        rules.setToolTipText("The rules of inference.");

        dialog.open();
        // main loop
        while (!dialog.isDisposed()) {
          if (!display.readAndDispatch()) {
            display.sleep();
          }
        }
        return true;
      case WARN:
        MessageBox warnBox = new MessageBox(
            display.getActiveShell(), SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
        warnBox.setText("Warning");
        warnBox.setMessage(message);
        int btnId = warnBox.open();
        switch (btnId) {
          case SWT.OK:
            // continue with reset
            System.out.println("OK selected");
            return true;
          case SWT.CANCEL:
            // do nothing
            System.out.println("Cancel selected");
            return false;
          default:
            break;
        }
        break;
      default:
        break;
    }
    return false;
  }

  /**
   * Helper method that calls the {@link ArchitectureFunctions#showTrace(String) showTrace(String)}
   * method for the selected proof.
   * @param verifiedProps
   *          the table with the proof items
   */
  private void showTrace(Table verifiedProps) {
    // show the verification trace of a property
    for (TableItem i : verifiedProps.getItems()) {
      if (i.getChecked()) {
        archFunc.showTrace(i.getText());
      }
    }
  }

  /**
   * Helper method that calls the {@link ArchitectureFunctions#verify(String) verify(String)}
   * method for the selected property. Also updates the table for verified properties.
   * @param property
   *          the name of the property to verify
   * @param verifiedProps
   *          the table with the verified property items
   */
  private void verifyProp(String property, Table verifiedProps) {
    // verify the property
    if (archFunc.verify(property)) {
      // property successfully verified
      TableItem item = new TableItem(verifiedProps, SWT.NONE);
      item.setText("[holds] " + property);
    } else {
      // property not verified
      TableItem item = new TableItem(verifiedProps, SWT.NONE);
      item.setText("[does not hold] " + property);
    }
  }

  /**
   * Helper method that handles the selection via checkboxes.
   * @param e
   *          the event
   * @param verifiedProps
   *          the table to update
   */
  private void updateVerifTab(Event e, Table verifiedProps) {
    // handle the selection and deselection of table items
    if (e.detail == SWT.CHECK) {
      for (TableItem i : verifiedProps.getItems()) {
        if (i != e.item) {
          // only one item should be checked at a time
          i.setChecked(false);
        }
      }
    }
  }

  /**
   * Helper method that syncs a combo with the list of terms
   * {@link ArchitectureFunctions#gettList() tList}.
   * @param term
   *          the term combo
   */
  private void updateTerms(Combo term) {
    term.removeAll();
    for (Term t : archFunc.gettSet()) {
      term.add(t.toString());
    }
    term.setListVisible(true);
  }

  /**
   * Helper method that syncs a combo with the list of equations
   * {@link ArchitectureFunctions#geteList() eList}.
   * @param eq
   *          the equation combo
   */
  private void updateEquations(Combo eq) {
    eq.removeAll();
    for (Equation e : archFunc.geteSet()) {
      eq.add(e.toString());
    }
    eq.setListVisible(true);
  }

  /**
   * Helper method that syncs a table with the list of equations
   * {@link ArchitectureFunctions#geteList() eList}.
   * @param eq
   *          the equation table
   */
  private void updateEquationsTab(Table eq) {
    eq.removeAll();
    for (Equation e : archFunc.geteSet()) {
      TableItem item = new TableItem(eq, SWT.NONE);
      item.setText(e.toString());
    }
  }

  /**
   * Helper method that syncs a combo with the list of components
   * {@link ArchitectureFunctions#getcList() cList}.
   * @param comp
   *          the component combo
   */
  private void updateComponents(Combo comp) {
    comp.removeAll();
    for (Component c : archFunc.getcSet()) {
      comp.add(c.toString());
    }
    comp.setListVisible(true);
  }

  /**
   * Helper method that syncs a table with the list of components
   * {@link ArchitectureFunctions#getcList() cList}.
   * @param compTable
   *          the component table
   */
  private void updateCompsTab(Table compTable) {
    compTable.removeAll();
    for (Component c : archFunc.getcSet()) {
      TableItem item = new TableItem(compTable, SWT.NONE);
      item.setText(c.toString());
    }
  }

  /**
   * Helper method that syncs a table with the list of trust relations
   * {@link ArchitectureFunctions#gettrustList() trustList}.
   * @param trustTable
   *          the trust relations table
   */
  private void updateTrustTab(Table trustTable) {
    trustTable.removeAll();
    for (Trust t : archFunc.gettrustSet()) {
      TableItem item = new TableItem(trustTable, SWT.NONE);
      item.setText(t.toString());
    }
  }

  /**
   * Helper method that syncs a table with the list of statements
   * {@link ArchitectureFunctions#getstList() stList}.
   * @param stmtTable
   *          the statement table
   */
  private void updateStatementTab(Table stmtTable) {
    stmtTable.removeAll();
    for (Statement s : archFunc.getstSet()) {
      TableItem item = new TableItem(stmtTable, SWT.NONE);
      item.setText(s.toString());
    }
  }
  
  /**
   * Helper method that syncs a combo with the list of statements
   * {@link ArchitectureFunctions#getstList() stList}.
   * @param proofs
   *          the proof combo
   */
  private void updateProofs(Combo proofs) {
    proofs.removeAll();
    for (Statement s : archFunc.getstSet()) {
      if (s instanceof Proof) {
        proofs.add(s.toString());
      }
    }
    proofs.setListVisible(true);
  }
  
  /**
   * Helper method that syncs a combo with the list of statements
   * {@link ArchitectureFunctions#getstList() stList}.
   * @param attests
   *          the attest combo
   */
  private void updateAttests(Combo attests) {
    attests.removeAll();
    for (Statement s : archFunc.getstSet()) {
      if (s instanceof Attest) {
        attests.add(s.toString());
      }
    }
    attests.setListVisible(true);
  }

  /**
   * Helper method that syncs a table with the list of events
   * {@link ArchitectureFunctions#getaList() aList}.
   * @param actTable
   *          the actions table
   */
  private void updateActionsTab(Table actTable) {
    actTable.removeAll();
    for (Action a : archFunc.getaSet()) {
      TableItem item = new TableItem(actTable, SWT.NONE);
      item.setText(a.toString());
    }
  }

  /**
   * Helper method that syncs a table with the list of dependence relations
   * {@link ArchitectureFunctions#getdList() dList}.
   * @param depTable
   *          the dependence relations table
   */
  private void updateDepsTab(Table depTable) {
    depTable.removeAll();
    for (DependenceRelation d : archFunc.getdSet()) {
      TableItem item = new TableItem(depTable, SWT.NONE);
      item.setText(d.toString());
    }
  }

  /**
   * Helper method that syncs a table with the list of deduction capabilities
   * {@link ArchitectureFunctions#getdedList() dedList}.
   * @param dedTable
   *          the deduction capabilities table
   */
  private void updateDedsTab(Table dedTable) {
    dedTable.removeAll();
    for (DeductionCapability d : archFunc.getdedSet()) {
      TableItem item = new TableItem(dedTable, SWT.NONE);
      item.setText(d.toString());
    }
  }

  /**
   * Helper method that syncs a combo with the list of properties
   * {@link ArchitectureFunctions#getpList() pList}.
   * @param prop
   *          the properties combo
   */
  private void updateProps(Combo prop) {
    prop.removeAll();
    for (Property p : archFunc.getpSet()) {
      prop.add(p.toString());
    }
    prop.setListVisible(true);
  }

  /**
   * Helper method that syncs a combo with the list of variables
   * {@link ArchitectureFunctions#getvList() vList}.
   * @param var
   *          the variables combo
   */
  private void updateVariables(Combo var) {
    var.removeAll();
    for (Variable v : archFunc.getvSet()) {
      var.add(v.toString());
    }
    var.setListVisible(true);
  }

  /**
   * Helper method that syncs a table with the list of variables
   * {@link ArchitectureFunctions#getvList() vList}.
   * @param var
   *          the variables table
   */
  private void updateVarsTab(Table var) {
    var.removeAll();
    for (Variable v : archFunc.getvSet()) {
      TableItem item = new TableItem(var, SWT.None);
      item.setText(v.toString());
    }
  }

  /**
   * Helper method that syncs a table with the list of terms
   * {@link ArchitectureFunctions#gettList() tList}.
   * @param term
   *          the terms table
   */
  private void updateTermsTab(Table term) {
    term.removeAll();
    for (Term t : archFunc.gettSet()) {
      TableItem item = new TableItem(term, SWT.None);
      item.setText(t.toString());
    }
  }

  /**
   * Helper method that syncs a table with the list of properties
   * {@link ArchitectureFunctions#getpList() pList}.
   * @param propTable
   *          the properties table
   */
  private void updatePropsTab(Table propTable) {
    propTable.removeAll();
    for (Property p : archFunc.getpSet()) {
      TableItem item = new TableItem(propTable, SWT.None);
      item.setText(p.toString());
    }
  }

  /**
   * Helper method that syncs a table with the list of deductions
   * {@link ArchitectureFunctions#getDeducs() deducs}.
   * @param dedTable
   *          the deductions table
   */
  private void updateDedTab(Table dedTable) {
    dedTable.removeAll();
    for (Deduction d : archFunc.getDeducs()) {
      TableItem item = new TableItem(dedTable, SWT.None);
      item.setText(d.toString());
    }
  }

  /**
   * Helper method that fills a combo with the operators depending on the state of a button.
   * @param operator
   *          the operator combo
   * @param binary
   *          the button
   */
  private void updateOperators(Combo operator, Button binary) {
    operator.removeAll();
    operator.add("FUNC");
    if (binary.getSelection()) {
      operator.add("ADD");
      operator.add("SUB");
      operator.add("MULT");
      operator.add("DIV");
    }
    operator.setListVisible(true);
  }

  /**
   * Helper method that syncs a table with the list of attestations that are
   * a subet of {@link ArchitectureFunctions#getstList() stList}.
   * @param att
   *          the attestations table
   */
  private void updateAttestsTab(Table att) {
    att.removeAll();
    for (Statement st : archFunc.getstSet()) {
      if (st instanceof Attest) {
        TableItem item = new TableItem(att, SWT.NONE);
        item.setText(st.toString());
      }
    }
  }

  /**
   * Helper method that syncs a combo with the list of statements
   * {@link ArchitectureFunctions#getstList() stList}.
   * @param stTab
   *          the statements table
   */
  private void updateStatementsTab(Table stTab) {
    stTab.removeAll();
    for (Statement st : archFunc.getstSet()) {
      TableItem item = new TableItem(stTab, SWT.NONE);
      item.setText(st.toString());
    }
  }

  /**
   * Helper method that handles the removal of checked items from a table.
   * @param type
   *          the type of object like variable or statement
   * @param table
   *          the table to remove the items from
   */
  private void handleRemove(ObjectType type, Table table) {
    // remove items from a table
    for (TableItem i : table.getItems()) {
      if (i.getChecked()) {
        switch (type) {
          case ACT:
            archFunc.removeAction(i.getText());
            break;
          case COMP:
            archFunc.removeComponent(i.getText());
            break;
          case EQ:
            archFunc.removeEquation(i.getText());
            break;
          case STMT:
            archFunc.removeStatement(i.getText());
            break;
          case TERM:
            archFunc.removeTerm(i.getText());
            break;
          case TRUST:
            archFunc.removeTrust(i.getText());
            break;
          case VAR:
            archFunc.removeVariable(i.getText());
            break;
          case DED:
            archFunc.removeDed(i.getText());
            break;
          case DEP:
            archFunc.removeDep(i.getText());
            break;
          case PROP:
            archFunc.removeProp(i.getText());
            break;
          default:
            // nothing
            break;
        }
        table.remove(table.indexOf(i));
      }
    }
  }

  /**
   * Helper method that calls the method
   * {@link ArchitectureFunctions#addTerm(OperatorType, Operator, String, String, String, String)
   * addTerm(OperatorType, Operator, String, String, String, String)} with the correct input
   * from the combos.
   * @param unary
   *          the radio button option unary
   * @param binary
   *          the radio button option binary
   * @param operator
   *          the operator combo
   * @param funcName
   *          the 'name of the function' text field
   * @param term1
   *          the first term combo
   * @param term2
   *          the second term combo
   * @param term3
   *          the third term combo
   */
  private void handleTerms(Button unary, Button binary, Combo operator,
      Text funcName, Combo term1, Combo term2, Combo term3) {
    // prepare the right data types
    OperatorType opType = unary.getSelection() ? OperatorType.UNARY
        : (binary.getSelection() ? OperatorType.BINARY : OperatorType.TERTIARY);
    if (operator.getText().equals("")) {
      // no item selected
      return;
    }
    Operator op = Operator.valueOf(operator.getText());
    String fctName = funcName.isEnabled() ? funcName.getText() : null;
    String t1 = term1.getText();
    String t2 = term2.isEnabled() ? term2.getText() : null;
    String t3 = term3.isEnabled() ? term3.getText() : null;
    // create the term and add it to the list
    archFunc.addTerm(opType, op, fctName, t1, t2, t3);
  }

  /**
   * Helper method that calls the method
   * {@link ArchitectureFunctions#addEquation(String, Type, String, String, String, String)
   * addEquations(String, Type, String, String, String, String)} with the correct input
   * from the combos.
   * @param conjunc
   *          the radio button option conjunction
   * @param eqName
   *          the 'name of the equation' text field
   * @param e1
   *          the first equation combo
   * @param e2
   *          the second equation combo
   * @param t1
   *          the first term combo
   * @param t2
   *          the second term combo
   */
  private void handleEquations(Button conjunc, Text eqName,
      Combo e1, Combo e2, Combo t1, Combo t2) {
    // prepare the right data types
    Type type = conjunc.getSelection() ? Type.CONJUNCTION : Type.RELATION;
    String eq1 = e1.getText();
    String eq2 = e2.getText();
    String term1 = t1.getText();
    String term2 = t2.getText();
    // create the equation and add it to the list
    archFunc.addEquation(eqName.getText(), type, eq1, eq2, term1, term2);
  }

  /**
   * Helper method that calls the method
   * {@link ArchitectureFunctions#addReceive(String, String, List, List)
   * addReceive(String, String, List, List)} with the correct input from the combos.
   * @param comp1
   *          the first component combo
   * @param comp2
   *          the second component combo
   * @param stTable
   *          the table containing the statements
   * @param varTable
   *          the table containing the variables
   */
  private void handleReceive(Combo comp1, Combo comp2, Table stTable, Table varTable) {
    // prepare the right data types
    Set<String> stSet = new LinkedHashSet<String>();
    Set<String> varSet = new LinkedHashSet<String>();
    for (TableItem i : stTable.getItems()) {
      if (i.getChecked()) {
        stSet.add(i.getText());
      }
    }
    for (TableItem i : varTable.getItems()) {
      if (i.getChecked()) {
        varSet.add(i.getText());
      }
    }
    // create the receive action and add it to the list
    archFunc.addReceive(comp1.getText(), comp2.getText(), stSet, varSet);
  }

  /**
   * Helper method that calls the method
   * {@link ArchitectureFunctions#addCheck(String, List) addCheck(String, List)}
   * with the correct input from the combos.
   * @param comp
   *          the component combo
   * @param eqTable
   *          the equations table
   */
  private void handleCheck(Combo comp, Table eqTable) {
    // prepare the right data types
    Set<String> eqSet = new LinkedHashSet<String>();
    for (TableItem i : eqTable.getItems()) {
      if (i.getChecked()) {
        eqSet.add(i.getText());
      }
    }
    // create the check action and add it to the list
    archFunc.addCheck(comp.getText(), eqSet);
  }
  
  /**
   * Helper method that calls the method
   * {@link ArchitectureFunctions#addVerify(String, String, boolean)
   * addVerify(String, String, boolean)} with the correct input from the combos.
   * @param att
   *          the radio button that indicates type of statement
   * @param comp
   *          the component combo
   * @param pList
   *          the proof combo
   * @param aList
   *          the attest combo
   */
  private void handleVerify(Button att, Combo comp, Combo pList, Combo aList) {
    if (att.getSelection()) {
      archFunc.addVerify(comp.getText(), aList.getText(), false);
    } else {
      archFunc.addVerify(comp.getText(), pList.getText(), true);
    }
  }

  /**
   * Helper method that calls the method
   * {@link ArchitectureFunctions#addDep(String, String, List) addDep(String, String, List)}
   * with the correct input from the combos.
   * @param comp
   *          the component combo
   * @param var
   *          the variable combo
   * @param varTable
   *          the variables table
   */
  private void handleDep(Combo comp, Combo var, Table varTable, Text prob) {
    // prepare the right data types
    Set<String> varSet = new LinkedHashSet<String>();
    for (TableItem i : varTable.getItems()) {
      if (i.getChecked()) {
        varSet.add(i.getText());
      }
    }
    // create the dependence relation and add it to the list
    archFunc.addDep(comp.getText(), var.getText(), varSet, prob.getText());
  }

  /**
   * Helper method that calls the method
   * {@link ArchitectureFunctions#addDed(String, List) addDed(String, List)}
   * with the correct input from the combos.
   * @param comp
   *          the component combo
   * @param dedTable
   *          the deductions table
   */
  private void handleDed(Combo comp, Table dedTable) {
    // prepare the right data types
    Set<String> dedSet = new LinkedHashSet<String>();
    for (TableItem i : dedTable.getItems()) {
      if (i.getChecked()) {
        dedSet.add(i.getText());
      }
    }
    // create the deduction and add it to the list
    archFunc.addDed(comp.getText(), dedSet);
  }

  /**
   * Helper method that calls the method
   * {@link ArchitectureFunctions#addDeduc(String, List, String) addDeduc(String, List, String)}
   * with the correct input from the combos.
   * @param dedName
   *          the 'name of the deduction' text
   * @param premiseTable
   *          the premises table
   * @param conclusion
   *          the conclusion combo
   */
  private void handlemyDed(Text dedName, Table premiseTable, Combo conclusion, Text prob) {
    // prepare the right data types
    Set<String> eqSet = new LinkedHashSet<String>();
    for (TableItem i : premiseTable.getItems()) {
      if (i.getChecked()) {
        eqSet.add(i.getText());
      }
    }
    // create the check action and add it to the list
    archFunc.addDeduc(dedName.getText(), eqSet, conclusion.getText(), prob.getText());
  }

  /**
   * Helper method that either calls {@link ArchitectureFunctions#addAttest(String, List)
   * addAttest(String, List)} or {@link ArchitectureFunctions#addProof(String, List)
   * addProof(String, List)} depending on the type of statement.
   * @param att
   *          the radio button option attestation
   * @param comp
   *          the component combo
   * @param eqTable
   *          the equations table
   * @param attTable
   *          the attestations table
   */
  private void handleStatement(Button att, Combo comp, Table eqTable, Table attTable) {
    // prepare the right data types
    Set<String> pSet = new LinkedHashSet<String>();
    for (TableItem i : eqTable.getItems()) {
      if (i.getChecked()) {
        pSet.add(i.getText());
      }
    }
    for (TableItem i : attTable.getItems()) {
      if (i.getChecked()) {
        pSet.add(i.getText());
      }
    }
    if (att.getSelection()) {
      archFunc.addAttest(comp.getText(), pSet);
    } else {
      archFunc.addProof(comp.getText(), pSet);
    }
  }
}