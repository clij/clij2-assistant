package net.haesleinhuepf.clij2.assistant.io;

import ij.IJ;
import ij.WindowManager;
import ij.io.SaveDialog;
import ij.plugin.PlugIn;
import net.haesleinhuepf.clij2.assistant.AbstractAssistantGUIPlugin;
import net.haesleinhuepf.clij2.assistant.ScriptGenerator;
import net.haesleinhuepf.clij2.assistant.scriptgenerator.AssistantGroovyGenerator;
import net.haesleinhuepf.clij2.assistant.services.AssistantGUIPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SaveAssistantWorkflowPlugin implements PlugIn {

    @Override
    public void run(String arg) {

        AssistantGUIPlugin plugin = null;
        for (int id : WindowManager.getIDList()) {
            AssistantGUIPlugin temp = AbstractAssistantGUIPlugin.getPluginFromTargetImage(WindowManager.getImage(id));
            if (temp != null) {
                plugin = temp;
                break;
            }
        }

        if (plugin == null) {
            IJ.log("Error: No CLIJ2-Assistant workflow found!");
            return;
        }

        ScriptGenerator sg = new AssistantGroovyGenerator();
        String filename = SaveDialog.getPath(plugin.getSource(0), sg.fileEnding());
        String script = plugin.generateScript(sg);

        File outputTarget = new File(filename);

        try {
            FileWriter writer = new FileWriter(outputTarget);
            writer.write(script);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

    }
}
