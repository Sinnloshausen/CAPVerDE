package utils;

public class EnumHelper {

  /**
   * Static helper method that checks if a string is a valid enum option.
   * 
   * @param enumerator
   *          the enum class
   * @param value
   *          the string
   * @return true, if the string is a valid option
   */
  public static <T extends Enum<T>> boolean enumContains(Class<T> enumerator, String value) {
    value = value.toLowerCase();
    for (T c : enumerator.getEnumConstants()) {
      String cLower = c.name().toLowerCase();
      // compare the strings ignoring upper cases
      if (cLower.equals(value)) {
        // success
        return true;
      }
    }
    // no enum option matched the string
    return false;
  }

}
