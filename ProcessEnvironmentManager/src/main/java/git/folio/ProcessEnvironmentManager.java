package git.folio;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

public class ProcessEnvironmentManager {

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

    public static Map<String, String> getEnvironmentVariables() {
        return System.getenv();
    }

    public static String getEnvironmentVariable(String name) {
        return System.getenv(name);
    }

    public static void setEnvironmentVariable(String name, String value) throws Exception {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.put(name, value);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.put(name, value);
        } catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for (Class cl : classes) {
                if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.put(name, value);
                    break;
                }
            }
        }
    }

    public static void removeEnvironmentVariable(String name) throws Exception {
        setEnvironmentVariable(name, null);
    }

    public static ProcessBuilder createProcessBuilderWithEnv(String... command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Map<String, String> env = processBuilder.environment();
        env.putAll(System.getenv());
        return processBuilder;
    }

    public static void setEnvironmentVariableForSubprocess(ProcessBuilder processBuilder, String name, String value) {
        Map<String, String> env = processBuilder.environment();
        env.put(name, value);
    }

    public static void removeEnvironmentVariableForSubprocess(ProcessBuilder processBuilder, String name) {
        Map<String, String> env = processBuilder.environment();
        env.remove(name);
    }

    public static void printEnvironmentVariables() {
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            System.out.format("%s=%s%n", envName, env.get(envName));
        }
    }

    public static void main(String[] args) {
        try {
            // Example usage
            System.out.println("Current PATH: " + getEnvironmentVariable("PATH"));

            setEnvironmentVariable("MY_CUSTOM_VAR", "Hello, World!");
            System.out.println("MY_CUSTOM_VAR: " + getEnvironmentVariable("MY_CUSTOM_VAR"));

            ProcessBuilder pb = createProcessBuilderWithEnv("echo", "Hello from subprocess");
            setEnvironmentVariableForSubprocess(pb, "SUBPROCESS_VAR", "Subprocess specific");

            Process process = pb.start();
            process.waitFor();

            removeEnvironmentVariable("MY_CUSTOM_VAR");
            System.out.println("MY_CUSTOM_VAR after removal: " + getEnvironmentVariable("MY_CUSTOM_VAR"));

            System.out.println("\nAll environment variables:");
            printEnvironmentVariables();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}