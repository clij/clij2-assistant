package net.haesleinhuepf.clij2.assistant.interactive.generic;

import net.haesleinhuepf.clij.macro.AbstractCLIJPlugin;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij2.assistant.AbstractAssistantGUIPlugin;
import net.haesleinhuepf.clij2.assistant.services.AssistantGUIPlugin;
import net.haesleinhuepf.clij2.assistant.services.CLIJMacroPluginService;
import net.haesleinhuepf.clij2.assistant.utilities.AssistantUtilities;
import net.haesleinhuepf.clij2.plugins.Copy;
import org.scijava.plugin.Plugin;


@Plugin(type = AssistantGUIPlugin.class, priority = -1)
public class GenericAssistantGUIPlugin extends AbstractAssistantGUIPlugin {


    @Override
    public void run(String arg) {
        //IJ.log("ARG: " + arg);
        if (arg != null && arg.length() > 0) {
            CLIJMacroPlugin plugin = CLIJMacroPluginService.getInstance().getService().getCLIJMacroPlugin(arg);
            new GenericAssistantGUIPlugin(plugin).run("");
        } else {
            super.run(arg);
        }
    }

    public GenericAssistantGUIPlugin(CLIJMacroPlugin plugin) {
        super(plugin);
    }

    public GenericAssistantGUIPlugin() {
        super(new Copy());
    }

    public GenericAssistantGUIPlugin(AbstractCLIJPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean canManage(CLIJMacroPlugin plugin) {
        return AssistantUtilities.isIncubatablePlugin(plugin);
    }
}
