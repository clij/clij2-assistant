package net.haesleinhuepf.clij2.assistant.utilities;

import ij.IJ;
import ij.plugin.PlugIn;
import net.haesleinhuepf.clij2.assistant.services.SuggestionService;
import net.haesleinhuepf.clij2.assistant.services.UsageAnalyser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CustomSuggestions implements PlugIn {

    @Override
    public void run(String arg) {
        String folder = IJ.getDirectory("Folder with clij-macros");
        UsageAnalyser combinedUsageStats = new UsageAnalyser(
                folder);

        String output = combinedUsageStats.all();


        String target_file = IJ.getDirectory("imagej") + "/suggestions/";
        new File(target_file).mkdirs();

        target_file = target_file  + "suggestions_" + System.currentTimeMillis() + ".config" ;


        File outputTarget = new File(target_file);
        try {
            FileWriter writer = new FileWriter(outputTarget);
            writer.write(output);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SuggestionService.getInstance().invalidate();
        IJ.log("New suggestions written to " + target_file);
    }
}
