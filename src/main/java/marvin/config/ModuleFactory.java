package marvin.config;

import com.google.inject.Module;

public class ModuleFactory {

    private static Module module;

    public  static void init(Module m) {
        module = m;
    }

    public static Module getInstance() {
        return module;
    }
}
