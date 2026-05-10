package automation.generic;

public class EnvironmentManager {
    private static final String DEFAULT_ENVIRONMENT = "agil2";
    private static final String DEFAULT_VERSION = "V2";
    private static String currentEnvironment = DEFAULT_ENVIRONMENT;
    private static String currentVersion = DEFAULT_VERSION;

    public  String getCurrentEnvironment() {
        return currentEnvironment;
    }

    public  void setEnvironment(String environment) {
        currentEnvironment = environment;
    }

    public  String getCurrentVersion() {
        return currentVersion;
    }

    public  void setVersion(String version) {
        currentVersion = version;
    }

    public  void resetToDefaults() {
        currentEnvironment = DEFAULT_ENVIRONMENT;
        currentVersion = DEFAULT_VERSION;
    }
}
