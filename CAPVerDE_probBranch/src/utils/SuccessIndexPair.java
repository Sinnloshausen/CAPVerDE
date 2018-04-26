package utils;

/**
 * Objects that represent a value pair with a boolean that signals success and
 * an integer that indicates the index if no success and -1 else.
 */
public final class SuccessIndexPair {

  // class fields
  private final boolean success;
  private final int index;

  /**
   * The constructor that sets the index to -1 if the success is true.
   * 
   * @param success
   *          the success flag
   * @param index
   *          -1 if success, the index else
   */
  public SuccessIndexPair(boolean success, int index) {
    this.success = success;
    this.index = !success ? index : -1;
  }

  // getter methods
  public boolean isSuccess() {
    return success;
  }

  public int getIndex() {
    return index;
  }
}
