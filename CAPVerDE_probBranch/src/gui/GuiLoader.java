package gui;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import solver.ConfigReader;

/**
 * The class that loads the GUI.
 * Also checks the platform and loads the corresponding SWT library.
 */
public class GuiLoader {

  /**
   * The different operating systems: linux, mac and windows.
   */
  private static enum OperatingSystem {
    WIN, LIN, MAC;
    @Override
    public String toString() {
      switch (this) {
        case LIN:
          return "linux";
        case MAC:
          return "osx";
        case WIN:
          return "win";
        default:
          // should not happen
          return null;
      }
    }
  }

  /**
   * The architecture: 32 or 64 bit.
   */
  private static enum OsArch {
    X64, X32;
    @Override
    public String toString() {
      switch (this) {
        case X32:
          return "32";
        case X64:
          return "64";
        default:
          // should not happen
          return null;
      }
    }
  }

  /**
   * The main function of the project.
   * This calls the {@link solver.ConfigReader#readConfig() readConfig()} method
   * and the {@link #checkPlatform() checkPLatform()} method. After having initialized,
   * the method {@link Gui#GUI() GUI()} starts the GUI.
   */
  public static void main(String[] args) {
    // start the program
    if (!ConfigReader.readConfig()) {
      // Could not find or read the config file
      System.err.println("Config file could not be read! Exiting...");
      System.exit(1);
    }
    if (!ConfigReader.platform.equals("windows64")) {
    	// this is a windows 64 version
        System.err.println("This is not the right platform! Trying dynamic loading...");
        if (!checkPlatform()) {
            // OS could not be detected
            System.err.println("Could not identify platform! Exiting...");
            System.exit(1);
          }
    }
    new Gui();
  }

  /**
   * Method to get the OS.
   * 
   * @return true for success, false if OS was not detected
   */
  private static boolean checkPlatform() {
    // TODO test this
    OperatingSystem os = null;
    OsArch oa = null;
    String osName = System.getProperty("os.name");
    // DEBUG
    System.out.println("Operating System: " + osName);
    if (osName == null) {
      // no name property set
      return false;
    } else {
      // make matching easier
      osName = osName.toLowerCase();
    }
    if (osName.contains("win")) {
      // windows detected
      os = OperatingSystem.WIN;
    } else if (osName.contains("mac")) {
      // mac os detected
      os = OperatingSystem.MAC;
    } else if (osName.contains("linux") || osName.contains("nix")) {
      // linux detected
      os = OperatingSystem.LIN;
    } else {
      // unknown operating system
      return false;
    }
    String osArch = System.getProperty("os.arch");
    // DEBUG
    System.out.println("Architecture: " + osArch);
    if (osArch != null && osArch.contains("64")) {
      // 64 bit architecture
      oa = OsArch.X64;
    } else {
      // probably 32 bit
      oa = OsArch.X32;
    }
    // create the file name for the needed swt library
    String jarFileName = "lib/swt";
    jarFileName += "_" + os.toString() + "_" + oa.toString() + ".jar";

    try {
      File swtJar = new File(jarFileName);
      URL url = swtJar.toURI().toURL();
      // load the swt lib in runtime
      //TODO java 9 approach
      //TODO okay, just make jarsfor each distribution, i guess
      ClassLoader parent = ClassLoader.getPlatformClassLoader();
      URLClassLoader loader = new URLClassLoader(new URL[] {url}, parent);
      //URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
      //Class<?> urlClass = Class.forName("SWT", true, loader);
      Class<?> urlClass = URLClassLoader.class;
      Method method = urlClass.getDeclaredMethod("addURL", new Class<?>[] { URL.class });
      method.setAccessible(true);
      method.invoke(loader, new Object[] { url });
    } catch (Throwable t) {
      // something went wrong
      t.printStackTrace();
      return false;
    }
    // the swt lib should be loaded now
    return true;
  }

}
