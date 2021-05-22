package net.haesleinhuepf.clij2.assistant.interactive.handcrafted;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.frame.RoiManager;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.assistant.AbstractAssistantGUIPlugin;
import net.haesleinhuepf.clij2.assistant.services.AssistantGUIPlugin;
import net.haesleinhuepf.clij2.assistant.utilities.AssistantUtilities;
import net.haesleinhuepf.spimcat.io.CLIJxVirtualStack;
import org.scijava.plugin.Plugin;

import java.awt.*;

@Plugin(type = AssistantGUIPlugin.class)
public class PullToROIManager extends AbstractAssistantGUIPlugin {

    Choice choice = null;
    String[] titles = null;

    public PullToROIManager() {
        super(new net.haesleinhuepf.clij2.plugins.PullToROIManager());
    }

    @Override
    protected GenericDialog buildNonModalDialog(Frame parent) {
        titles = WindowManager.getImageTitles();
        GenericDialog gd = new GenericDialog(AssistantUtilities.niceNameWithoutDimShape(this.getClass().getSimpleName()));
        gd.addChoice("", titles, titles[titles.length - 1] );

        choice = (Choice) gd.getChoices().get(0);

        return gd;
    }

    public synchronized void refresh()
    {
        ClearCLBuffer[][] pushed = CLIJxVirtualStack.imagePlusesToBuffers(my_sources);
        if (pushed[0][0].getDepth() > 1 || pushed[0].length > 1) {
            IJ.log("Warning: Show in ROIM Manager is only supported for single channel 2D Images.");
        }

        // we just do that so that the recorder has something to analyse
        net.haesleinhuepf.clij2.plugins.PullToROIManager plugin = (net.haesleinhuepf.clij2.plugins.PullToROIManager) getCLIJMacroPlugin();
        args = new Object[] {
                pushed[0]
        };
        plugin.setArgs(args);


        CLIJ2 clij2 = CLIJ2.getInstance();


        checkResult();

        if (result == null) {
            result = new ClearCLBuffer[]{clij2.create(new long[]{pushed[0][0].getWidth(), pushed[0][0].getHeight()}, pushed[0][0].getNativeType())};
        }

        RoiManager rm = RoiManager.getInstance();
        if (rm == null) {
            rm = new RoiManager();
        }

        rm.reset();
        clij2.pullLabelsToROIManager(pushed[0][0], rm);

        ImagePlus view = null;
        if (choice != null) {
            view = WindowManager.getImage(choice.getSelectedItem());
            if (view != null) {
                ClearCLBuffer buffer = clij2.pushCurrentSlice(view);
                clij2.copy(buffer, result[0]);
                buffer.close();
            }
        }

        cleanup(my_sources, pushed);

        setTarget(CLIJxVirtualStack.bufferToImagePlus(result));
        my_target.setTitle("ROIs of " + my_sources[0].getTitle());
        rm.runCommand(my_target, "Show all");
        enhanceContrast();

    }

}
