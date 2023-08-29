package net.haesleinhuepf.clij2.assistant.interactive.handcrafted;

import ij.gui.GenericDialog;
import net.haesleinhuepf.clij2.assistant.AbstractAssistantGUIPlugin;
import net.haesleinhuepf.clij2.assistant.services.AssistantGUIPlugin;
import org.scijava.plugin.Plugin;

import java.awt.*;
import java.util.Arrays;

@Plugin(type = AssistantGUIPlugin.class)
public class SubStack extends AbstractAssistantGUIPlugin {

    public SubStack() {
        super(new net.haesleinhuepf.clij2.plugins.SubStack());
    }


    @Override
    public void refreshView() {}

    GenericDialog my_dialog = null;

    @Override
    protected GenericDialog buildNonModalDialog(Frame parent) {
         my_dialog = super.buildNonModalDialog(parent);
         return my_dialog;
    }

    @Override
    protected void checkResult() {
        if (result == null || my_dialog == null) {
            return;
        }
        //
        TextField start_x_slider = (TextField) my_dialog.getNumericFields().get(0);
        TextField end_x_slider = (TextField) my_dialog.getNumericFields().get(1);

        int start_x;
        int end_x;

        try {
            start_x = (int) (Double.parseDouble(start_x_slider.getText()));
            end_x = (int) (Double.parseDouble(end_x_slider.getText()));
        } catch (Exception e) {
            System.out.println("Error parsing text (SubStack)");
            return;
        }

        long[] new_dimensions = null;
        new_dimensions = new long[]{result[0].getWidth(), result[0].getHeight(), end_x - start_x + 1};
        System.out.println("Size: " + Arrays.toString(new_dimensions));

        invalidateResultsIfDimensionsChanged(new_dimensions);
    }
}
