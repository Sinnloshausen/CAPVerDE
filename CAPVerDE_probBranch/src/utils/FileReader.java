package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Class that reads a file into a string.
 */
public class FileReader {

  /**
   * Method that reads a file into a single string.
   * 
   * @param filePath
   *          the absolute path including the file name
   * @return the string
   * @throws IOException
   *           if file does not exist
   */
  public static String readFile(String filePath) throws IOException {
    // read the file as byte array
    byte[] encoded = Files.readAllBytes(Paths.get(filePath));
    // convert to string
    return new String(encoded, "UTF-8");
  }

}
