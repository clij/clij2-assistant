package net.haesleinhuepf.clij2.assistant.scriptgenerator;

import ij.plugin.PlugIn;
import net.haesleinhuepf.clij2.assistant.AssistantGUIStartingPoint;
import net.haesleinhuepf.clij2.assistant.services.AssistantGUIPlugin;

public class IcyProtocolGeneratorPlugin implements PlugIn {

    @Override
    public void run(String arg) {
        AssistantGUIPlugin plugin = AssistantGUIStartingPoint.getCurrentPlugin();
        plugin.generateScriptFile(new IcyProtocolGenerator());
    }
}
