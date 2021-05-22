package net.haesleinhuepf.clij2.assistant.io;

import ij.IJ;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import weka.gui.scripting.GroovyScript;

import java.io.File;

public class LoadAssistantWorkflowPlugin implements PlugIn {

    @Override
    public void run(String args) {

        String filename = new OpenDialog("Open CLIJ2-Assistant workflow", IJ.getDirectory("current"), "*.groovy").getPath();

        new GroovyScript().run(new File(filename), new String[]{});
    }
}
