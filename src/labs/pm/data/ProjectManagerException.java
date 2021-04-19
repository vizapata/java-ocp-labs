package labs.pm.data;

public class ProjectManagerException extends Exception {
  public ProjectManagerException() {
    super();
  }

  public ProjectManagerException(String message) {
    super(message);
  }

  public ProjectManagerException(String message, Throwable cause) {
    super(message, cause);
  }
}
