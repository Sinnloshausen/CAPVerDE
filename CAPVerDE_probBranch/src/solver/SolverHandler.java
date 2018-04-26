package solver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import properties.Property;
import utils.FileHandler;
import utils.TraceBuffer;
import utils.TraceBuffer.LogType;

/**
 * Class that takes care of the running of the solver.
 */
public class SolverHandler {

  // class fields
  private String solverPath;
  private String solverName;
  private String filePath;
  private String fileName;

  /**
   * The full constructor with all parameters
   * that is is typically only called by the empty constructor in this class.
   * 
   * @param solverPath
   *          the path where the solver (mathsat) is to find
   * @param solverName
   *          the name of the solver to exec
   * @param filePath
   *          the path where the SMT2 file should be saved
   * @param fileName
   *          the name of the file to save as
   */
  public SolverHandler(String solverPath, String solverName, String filePath, String fileName) {
    this.solverPath = solverPath;
    this.solverName = solverName;
    this.filePath = filePath;
    this.fileName = fileName;
  }

  /**
   * Empty constructor with default values from config file.
   */
  public SolverHandler() {
    this(ConfigReader.solverPath, ConfigReader.solverName,
        ConfigReader.filePath, ConfigReader.fileName);
  }

  /**
   * Method that runs the solver on the content of a string.
   * 
   * @param buffer
   *          the string containing valid SMT lines
   * @param property
   *          the property that is verified by this run
   * @return true, if successful
   */
  public boolean runSolver(String buffer, Property property) {
    // create file handler
    FileHandler file = new FileHandler(filePath, fileName);
    byte[] lines = (buffer).getBytes();

    // write into the file
    if (!file.writeFile(lines)) {
      return false;
    }

    String line = "";
    String lastLine = "";
    List<String> history = new ArrayList<String>();
    // run the solver on the file
    try {
      Process mathsat;
      mathsat = Runtime.getRuntime().exec(
          solverPath + solverName + " -input=smt2 " + filePath + fileName);
      mathsat.waitFor();
      BufferedReader bufReader = new BufferedReader(
          new InputStreamReader(mathsat.getInputStream()));
      while ((line = bufReader.readLine()) != null) {
        // go through all lines
        history.add(line);
        lastLine = line;
      }
      if (lastLine.equals("sat")) {
        // Trace: pass on output
        System.out.println(lastLine);
        System.out.println("Property and Architecture are satisfiable");
        System.out.println("Property successfully proven!");
        return true;
      } else if (lastLine.equals("unsat")) {
        // Trace: pass on output
        System.out.println(lastLine);
        System.out.println("Property and Architecture are not satisfiable");
        return false;
      }
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
      return false;
    }
    // Give more information
    System.out.println(
        "The result was probably 'unsat', therefore the two conflicting statements are in:");
    System.out.println(history.subList(history.size() - 2, history.size()));
    System.out.println("Property and Architecture are probably not satisfiable");
    System.out.println("Property not proven!");
    // TODO trace
    TraceBuffer.logMessage(
        property, "There is a conflict between an assertion and the property to prove: "
        + property, 0, LogType.START);
    TraceBuffer.logMessage(property, buffer, 0, LogType.INFO);
    TraceBuffer.logMessage(property, "Unsat core: " + history.subList(history.size() - 2,
        history.size()), 0, LogType.END);
    return false;
  }

  // getter and setter methods
  public String getSolverPath() {
    return solverPath;
  }

  public void setSolverPath(String solverPath) {
    this.solverPath = solverPath;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

}
