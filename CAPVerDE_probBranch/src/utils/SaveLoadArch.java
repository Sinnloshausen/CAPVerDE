package utils;

import gui.ArchitectureFunctions;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Class that helps with the serialization of architectures.
 */
public class SaveLoadArch {

  /**
   * Static method that saves an architecture with all its objects for the GUI.
   * @param archFunc
   *          the architecture from the GUI
   * @param name
   *          a name to save the file as
   */
  public static void saveArch(ArchitectureFunctions archFunc, String name) {

    // save all objects into the 'saves' directory
    String path = "./saves/" + name + ".ser";

    // single line version
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
      // write the arch object to disk
      oos.writeObject(archFunc);

      // Debug
      System.out.println("Written to disk");

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    /*
     * // file and object stream FileOutputStream fout = null;
     * ObjectOutputStream oos = null;
     * 
     * try { // create filestream from path fout = new FileOutputStream(path);
     * oos = new ObjectOutputStream(fout); // write the architecture into file
     * oos.writeObject(arch);
     * 
     * // Debug System.out.println("Written to disk");
     * 
     * } catch (FileNotFoundException e) { // TODO Auto-generated catch block
     * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated
     * catch block e.printStackTrace(); } finally {
     * 
     * }
     */

  }

  /**
   * Static method that loads a saved architecture for the GUI.
   * @param fileName
   *          the name of the file to load from
   * @return the architecture for the GUI
   */
  public static ArchitectureFunctions loadArch(String fileName) {
    ArchitectureFunctions archFunc = null;
    String filePath = "./saves/" + fileName + ".ser";

    // single line version
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
      // read the object from file
      archFunc = (ArchitectureFunctions) ois.readObject();

      // Debug
      System.out.println("Read from file");

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return archFunc;
  }

}
