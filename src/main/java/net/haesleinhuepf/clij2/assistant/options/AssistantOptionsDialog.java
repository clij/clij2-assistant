package net.haesleinhuepf.clij2.assistant.options;

import fiji.util.gui.GenericDialogPlus;
import ij.plugin.PlugIn;

public class AssistantOptionsDialog implements PlugIn {
    @Override
    public void run(String arg) {
        AssistantOptions ao = AssistantOptions.getInstance();

        GenericDialogPlus gdp = new GenericDialogPlus("Assistant options");

        gdp.addFileField("Icy executable", ao.getIcyExecutable());

        gdp.showDialog();

        if (gdp.wasCanceled()) {
            return;
        }

        ao.setIcyExecutable(gdp.getNextString());
    }
}
