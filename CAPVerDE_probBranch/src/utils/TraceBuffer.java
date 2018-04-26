package utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import properties.Property;

/**
 * Objects that represent a buffer for tracing and logging messages.
 */
public class TraceBuffer {

  /**
   * The type of log message: start, end, or info.
   */
  public enum LogType {
    START, END, INFO;
  }

  // class fields
  private static String[] buffer = new String[100];
  private static Map<Property, Integer> lookUp = new HashMap<Property, Integer>();
  private static int position = 0;

  /**
   * Method to get a logging information for a specific property.
   * 
   * @param property
   *          the property to get the message for
   * @return the logging message
   */
  public static String getMessage(Property property) {
    return buffer[lookUp.get(property)];
  }

  /**
   * Method to add a logging message to the buffer.
   * 
   * @param property
   *          the property the log is intended for
   * @param message
   *          the logging information
   * @param recurseDepth
   *          the depth of the recursion
   * @param type
   *          the kind of logging, e.g. start or end
   */
  public static void logMessage(Property property, String message, int recurseDepth, LogType type) {
    if (recurseDepth == 0 && type == LogType.START) {
      position++;
      buffer[position] = "";
    }
    String spacing = String.join("", Collections.nCopies(recurseDepth, "     "));
    buffer[position] += spacing + message + System.lineSeparator();
    if (recurseDepth == 0 && type == LogType.END) {
      if (lookUp.get(property) != null) {
        // entry already exists, so update the buffer of the value
        buffer[lookUp.get(property)] += System.lineSeparator()
            + buffer[position] + System.lineSeparator();
      } else {
        lookUp.put(property, position);
      }
    }
  }

}
