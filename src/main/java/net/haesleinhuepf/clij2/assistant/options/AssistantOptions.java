package net.haesleinhuepf.clij2.assistant.options;

import ij.IJ;
import ij.Prefs;

public class AssistantOptions {
    private static String ICY_EXECUTABLE = "C:/Programs/icy_all_2.0.3.0/icy.exe";

    private static AssistantOptions instance = null;
    public static synchronized AssistantOptions getInstance() {
        if (instance == null) {
            instance = new AssistantOptions();
        }
        return instance;
    }

    private AssistantOptions() {

        ICY_EXECUTABLE = Prefs.get("CLIJ2-assistant.icy", ICY_EXECUTABLE);
    }


    private void savePrefs() {
        Prefs.set("CLIJ2-assistant.icy", ICY_EXECUTABLE);
    }

    public String getIcyExecutable() {
        return ICY_EXECUTABLE;
    }

    void setIcyExecutable(String icy_executable) {
        AssistantOptions.ICY_EXECUTABLE = icy_executable;
        savePrefs();
    }
}
