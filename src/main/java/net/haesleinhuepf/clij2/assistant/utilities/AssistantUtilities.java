package net.haesleinhuepf.clij2.assistant.utilities;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Toolbar;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.util.StringUtils;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.assistant.AbstractAssistantGUIPlugin;
import net.haesleinhuepf.clij2.assistant.services.MenuService;
import net.haesleinhuepf.clij2.gui.InteractiveWindowPosition;
import net.haesleinhuepf.clij2.gui.InteractiveZoom;
import net.haesleinhuepf.clij2.plugins.*;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import net.haesleinhuepf.clij2.assistant.annotation.AnnotationTool;
import net.haesleinhuepf.clij2.assistant.options.AssistantOptions;
import net.haesleinhuepf.clij2.assistant.services.AssistantGUIPlugin;
import net.haesleinhuepf.clij2.assistant.services.SuggestionService;
import org.scijava.util.ProcessUtils;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;

public class AssistantUtilities {
    public static Comparator<? super String> niceNameComparator = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            o1 = niceName(o1);
            o2 = niceName(o2);
            return o1.compareTo(o2);
        }
    };

    public static String now() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        return sdf.format(cal.getTime());
    }

    public static String stamp(ClearCLBuffer buffer) {
        String timestamp = "" + System.currentTimeMillis();
        buffer.setName(timestamp);
        return timestamp;
    }
    public static boolean checkStamp(ClearCLBuffer buffer, String stamp) {
        return buffer.getName().compareTo(stamp) == 0 && stamp.length() > 0;
    }

    public static void transferCalibration(ImagePlus source, ImagePlus target) {
        target.getCalibration().pixelWidth = source.getCalibration().pixelWidth;
        target.getCalibration().pixelHeight = source.getCalibration().pixelHeight;
        target.getCalibration().pixelDepth = source.getCalibration().pixelDepth;

        target.getCalibration().setXUnit(source.getCalibration().getXUnit());
        target.getCalibration().setYUnit(source.getCalibration().getYUnit());
        target.getCalibration().setZUnit(source.getCalibration().getZUnit());
    }

    public static String shortName(String title) {
        if (title.length() < 25) {
            return title;
        }
        return title.substring(0, 25) + "...";
    }


    @Deprecated // use niceName instead
    public static String niceNameWithoutDimShape(String name) {

        //name = name.replace("3D", "");
        //name = name.replace("Box", "");

        return niceName(name);
    }

    public static void glasbey(ImagePlus imp) {
        String dir = IJ.getDirectory("imagej");
        if (!dir.contains("null") && dir.toLowerCase().contains("fiji")) {
            IJ.run(imp, "glasbey_on_dark", "");
            // ensure that the LUT is really applied: TODO: check if the following is really necessary
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            IJ.run(imp, "glasbey_on_dark", "");
                            imp.resetDisplayRange();
                        }
                    },
                    300
            );
        }
    }

    public static void fire(ImagePlus imp) {
        //System.out.println();
        String dir = IJ.getDirectory("imagej");
        if (!dir.contains("null")) {

            IJ.run(imp, "Green Fire Blue", "");

            // ensure that the LUT is really applied: TODO: check if the following is really necessary
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            IJ.run(imp, "Green Fire Blue", "");
                            imp.resetDisplayRange();
                        }
                    },
                    300
            );

        }
    }

    public static void hi(ImagePlus imp) {
        String dir = IJ.getDirectory("imagej");
        if (!dir.contains("null") && dir.toLowerCase().contains("fiji")) {

            IJ.run(imp, "hi", "");

            // ensure that the LUT is really applied: TODO: check if the following is really necessary
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            IJ.run(imp, "hi", "");
                            imp.resetDisplayRange();
                        }
                    },
                    300
            );
        }
    }


    public static ImagePlus openImage(String filename) {
        if (new File(filename).exists()) {
            return IJ.openImage(filename);
        }
        IJ.log("CLIJ-Assistance couldn't find file\n" + filename + "\n" +
                "Please select its location");
        return IJ.openImage();
    }


    public static boolean ignoreEvent = false;


    public static boolean isReasonable(CLIJMacroPlugin clijPlugin, AssistantGUIPlugin plugin) {
        if (plugin == null || plugin.getTarget() == null) {
            return false;
        }

        CLIJMacroPlugin predecessorPlugin = plugin.getCLIJMacroPlugin();
        if (clijPlugin instanceof HasClassifiedInputOutput && predecessorPlugin instanceof HasClassifiedInputOutput) {
            if (((HasClassifiedInputOutput) clijPlugin).getInputType().contains(((HasClassifiedInputOutput) predecessorPlugin).getOutputType())) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    public static boolean isSuitable(CLIJMacroPlugin clijPlugin, AssistantGUIPlugin plugin) {
        if (plugin == null || plugin.getTarget() == null) {
            return false;
        }

        if (clijPlugin instanceof OffersDocumentation) {
            boolean imageIs3D = plugin.getTarget().getNSlices() > 1;

            String supportedDimensionality = ((OffersDocumentation) clijPlugin).getAvailableForDimensions().replace(" ", "");
            boolean operationTakes3DImages = supportedDimensionality.contains("3D");
            boolean operationTakes2DImages = supportedDimensionality.compareTo("3D->2D") != 0 &&
                                             supportedDimensionality.contains("2D");

            if ((!operationTakes2DImages) || (!operationTakes3DImages)) {
                if (operationTakes3DImages && !imageIs3D ||
                        operationTakes2DImages && imageIs3D) {
                    return false; // image has wrong dimensionality
                }
            }
        }
        return true;
    }

    static ArrayList<Class> blocklist = new ArrayList<>();
    static ArrayList<Class> advanced_list = new ArrayList<>();
    public static boolean isIncubatablePlugin(CLIJMacroPlugin clijMacroPlugin) {
        if (clijMacroPlugin == null) {
            return false;
        }
        String parameters = clijMacroPlugin.getParameterHelpText();

        // white list
        if (clijMacroPlugin.getClass().getName().compareTo("WekaLabelClassifier") == 0) {
            return true;
        }

        //if (!clijMacroPlugin.getName().contains("makeIso")) {
        //    return false;
        //}

        while (parameters.contains(", ")) {
            parameters = parameters.replace(", ", ",");
        }
        if (parameters.contains(",ByRef String ")) {
            // contains String output parameters
            return false;
        }
        if (parameters.contains(",ByRef Number ")) {
            // contains numberic output parameters
            return false;
        }
        if (parameters.contains(",Array ") || parameters.contains(",ByRef Array ")) {
            // contains array parameters
            return false;
        }

        String[] parameterdefintions = parameters.split(",");
        if (parameterdefintions.length < 2) {
            if (!(clijMacroPlugin.getClass().getPackage().toString().contains(".clij2wrappers.")) &&
                    (clijMacroPlugin instanceof net.haesleinhuepf.clij2.plugins.PullToROIManager)
            ) {
                return true;
            }
            return false;
        }

        if (!parameterdefintions[0].startsWith("Image ")) {
            // first parameter is no input image
            //System.out.println("D");
            return false;
        }
        if (!parameterdefintions[1].startsWith("ByRef Image ") && !parameterdefintions[1].startsWith("Image ")) {
            // second parameters is no image
            //System.out.println("E");
            return false;
        }
        /*
        if (parameterdefintions.length > 2) {
            if (parameterdefintions[2].startsWith("Image ") || parameterdefintions[2].startsWith("ByRef Image ")) {
                // second parameters is no output image
                //System.out.println("E");
                return false;
            }
        }*/
        if (clijMacroPlugin.getClass().getName().contains(".clij2wrappers.")) {
            return false;
        }
        if (clijMacroPlugin.getClass().getName().contains(".tilor.")) {
            return false;
        }
        if (clijMacroPlugin.getClass().getName().contains(".macro.")) { // clij1
            return false;
        }
        if (clijMacroPlugin.getClass().getName().contains(".customconvolutionplugin.")) { // clij1
            return false;
        }

        // blacklist
        if (blocklist.size() == 0 || advanced_list.size() == 0) {
            initLists();
        }
        if (clijMacroPlugin.getClass().getPackage().toString().contains("clijx")) {
            return false;
        }

        if (blocklist.contains(clijMacroPlugin.getClass())) {
            return false;
        }
        if ((!AbstractAssistantGUIPlugin.show_advanced) && isAdvancedPlugin(clijMacroPlugin)) {
            return false;
        }

        //System.out.println("Z");

        return true;
    }

    public static boolean isAdvancedPlugin(CLIJMacroPlugin clijMacroPlugin) {
        return advanced_list.contains(clijMacroPlugin.getClass());
    }

    private static void initLists() {
        blocklist.add(net.haesleinhuepf.clij2.plugins.MedianSliceBySliceBox.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.NonzeroMinimumDiamond.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.Crop2D.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.Blur3DSliceBySlice.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.DetectMaximaSliceBySliceBox.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.NonzeroMinimumBox.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.CountTouchingNeighbors.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.Scale2D.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.Scale3D.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.MedianSliceBySliceSphere.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.Histogram.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.LaplaceDiamond.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.OnlyzeroOverwriteMaximumBox.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.LabelledSpotsToPointList.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.AdjacencyMatrixToTouchMatrix.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.MinimumOctagon.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.Downsample3D.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.DilateBoxSliceBySlice.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.Downsample2D.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.ErodeBoxSliceBySlice.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.ReplaceIntensity.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.Paste3D.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.SpotsToPointList.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.Copy.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.NonzeroMaximumDiamond.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.MultiplyImageAndCoordinate.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.Paste2D.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.PointlistToLabelledSpots.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.ResliceRadial.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.MeanSliceBySliceSphere.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.SumImageSliceBySlice.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.ResliceRadialTop.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.ShortestDistances.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.GenerateTouchMatrix.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.AverageDistanceOfNFarOffPoints.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.DilateSphereSliceBySlice.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.TouchMatrixToAdjacencyMatrix.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.WriteValuesToPositions.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.NonzeroMaximumBox.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.FloodFillDiamond.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.ReslicePolar.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.AverageDistanceOfNClosestPoints.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.NClosestPoints.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.GradientY.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.GradientZ.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.GradientX.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.Scale.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.MinimumSliceBySliceSphere.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.Threshold.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.Blur2D.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.DownsampleSliceBySliceHalfMedian.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.MaximumSliceBySliceSphere.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.CentroidsOfLabels.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.DetectMinimaSliceBySliceBox.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.NeighborsOfNeighbors.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.OnlyzeroOverwriteMaximumDiamond.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.DetectMaximaBox.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.RotateLeft.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.ErodeSphereSliceBySlice.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.Resample.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.Blur3D.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.ConnectedComponentsLabeling.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.GenerateTouchCountMatrix.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.Watershed.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.GenerateParametricImageFromResultsTableColumn.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.AffineTransform.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.PullToResultsTableColumn.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.ArgMaximumZProjection.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.AverageDistanceOfTouchingNeighbors.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.CentroidsOfBackgroundAndLabels.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.DepthColorProjection.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.DistanceMap.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.DistanceMatrixToMesh.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.ExcludeLabels.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.ExcludeLabelsOnSurface.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.ExcludeLabelsSubSurface.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.GenerateBinaryOverlapMatrix.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.GenerateDistanceMatrix.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.GenerateJaccardIndexMatrix.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.GenerateParametricImage.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.GetJaccardIndex.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.GetMeanOfMaskedPixels.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.GetMeanSquaredError.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.GetSorensenDiceCoefficient.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.JaccardIndex.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.LocalThreshold.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.MatrixEqual.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.MaximumOfMaskedPixels.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.MaximumOfTouchingNeighbors.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.MeanClosestSpotDistance.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.MeanOfMaskedPixels.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.MeanOfTouchingNeighbors.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.MeanSquaredError.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.MedianOfTouchingNeighbors.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.MinimumDistanceOfTouchingNeighbors.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.MinimumOfMaskedPixels.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.MinimumOfTouchingNeighbors.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.MultiplyMatrix.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.NClosestDistances.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.PointIndexListToMesh.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.ReplaceIntensities.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.ReplacePixelsIfZero.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.RotateRight.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.SetNonZeroPixelsToPixelIndex.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.SorensenDiceCoefficient.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.StandardDeviationOfMaskedPixels.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.StandardDeviationOfTouchingNeighbors.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.StatisticsOfBackgroundAndLabelledPixels.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.StatisticsOfLabelledPixels.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.TouchMatrixToMesh.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.UndefinedToZero.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.VarianceOfMaskedPixels.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.Tenengrad.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.TenengradSliceBySlice.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.SobelSliceBySlice.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.MaximumDistanceOfTouchingNeighbors.class);
        blocklist.add(net.haesleinhuepf.clij2.plugins.PullToResultsTableColumn.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.ModeOfTouchingNeighbors.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.GenerateProximalNeighborsMatrix.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.PointIndexListToTouchMatrix.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.GenerateNNearestNeighborsMatrix.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.SetNonZeroPixelsToPixelIndex.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.MinimumZProjectionBounded.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.CountTouchingNeighbors.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.ImageToStack.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.MinimumZProjectionThresholdedBounded.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.MaximumZProjectionBounded.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.OnlyzeroOverwriteMaximumBox.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.MaximumOctagon.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.LabelledSpotsToPointList.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.TouchMatrixToMesh.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.AdjacencyMatrixToTouchMatrix.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.NClosestDistances.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.StackToTiles.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.StatisticsOfBackgroundAndLabelledPixels.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.GenerateJaccardIndexMatrix.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.SpotsToPointList.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.MultiplyImageAndCoordinate.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.DistanceMatrixToMesh.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.PointlistToLabelledSpots.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.DistanceMap.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.ModeOfTouchingNeighbors.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.ReplaceIntensities.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.SumImageSliceBySlice.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.GenerateBinaryOverlapMatrix.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.ShortestDistances.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.GenerateTouchMatrix.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.AverageDistanceOfNFarOffPoints.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.TouchMatrixToAdjacencyMatrix.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.WriteValuesToPositions.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.FloodFillDiamond.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.LabelVoronoiOctagon.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.Sinus.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.Cosinus.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.StatisticsOfLabelledPixels.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.MedianOfTouchingNeighbors.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.AverageDistanceOfNClosestPoints.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.NClosestPoints.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.PointIndexListToMesh.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.MultiplyMatrix.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.MaximumDistanceOfTouchingNeighbors.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.GenerateNNearestNeighborsMatrix.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.PointIndexListToTouchMatrix.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.CentroidsOfLabels.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.MaximumOfTouchingNeighbors.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.NeighborsOfNeighbors.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.StandardDeviationOfTouchingNeighbors.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.MinimumDistanceOfTouchingNeighbors.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.OnlyzeroOverwriteMaximumDiamond.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.GenerateProximalNeighborsMatrix.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.MeanZProjectionBounded.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.GenerateDistanceMatrix.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.GenerateTouchCountMatrix.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.AverageDistanceOfNClosestPoints.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.AverageDistanceOfNFarOffPoints.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.AverageDistanceOfTouchingNeighbors.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.AverageNeighborDistanceMap.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.CentroidsOfBackgroundAndLabels.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.CentroidsOfLabels.class);
        advanced_list.add(net.haesleinhuepf.clij2.plugins.CloseIndexGapsInLabelMap.class);
    }

    public static void installTools() {
        if (AssistantUtilities.class.getPackage().toString().contains(".clij2.")) {
            if (AssistantUtilities.CLIJxAssistantInstalled()) {
                return;
            }
        }

        String tool = IJ.getToolName();
        ignoreEvent = true;
        //Toolbar.removeMacroTools();


        Toolbar.addPlugInTool(new AssistantStartingPointTool());
        Toolbar.addPlugInTool(new InteractiveZoom());
        Toolbar.addPlugInTool(new InteractiveWindowPosition());
        Toolbar.addPlugInTool(new AnnotationTool());

        ignoreEvent = false;

        IJ.setTool(tool);

        /*
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        new MemoryDisplay().run("");
                    }
                },
                1000
        );
      
         */
    }

    public static boolean resultIsBinaryImage(AssistantGUIPlugin abstractAssistantGUIPlugin) {
        if (abstractAssistantGUIPlugin.getCLIJMacroPlugin() instanceof HasClassifiedInputOutput) {
            if (((HasClassifiedInputOutput) abstractAssistantGUIPlugin.getCLIJMacroPlugin()).getOutputType().contains("Binary Image")) {
                return true;
            }
        }

        String name = abstractAssistantGUIPlugin.getName().toLowerCase();
        if (abstractAssistantGUIPlugin.getCLIJMacroPlugin() != null && abstractAssistantGUIPlugin.getCLIJMacroPlugin() instanceof IsCategorized) {
            name = name + "," + ((IsCategorized) abstractAssistantGUIPlugin.getCLIJMacroPlugin()).getCategories().toLowerCase();
        }

        return name.contains("threshold") ||
                name.contains("binary") ||
                name.contains("watershed") ||
                name.contains("greater") ||
                name.contains("smaller") ||
                name.contains("equal")
                ;
    }

    public static boolean resultIsLabelImage(AssistantGUIPlugin abstractAssistantGUIPlugin) {
        if (abstractAssistantGUIPlugin.getCLIJMacroPlugin() instanceof HasClassifiedInputOutput) {
            if (((HasClassifiedInputOutput) abstractAssistantGUIPlugin.getCLIJMacroPlugin()).getOutputType().contains("Label Image")) {
                return true;
            }
        }

        String name = abstractAssistantGUIPlugin.getName().toLowerCase();
        if (abstractAssistantGUIPlugin.getCLIJMacroPlugin() != null && abstractAssistantGUIPlugin.getCLIJMacroPlugin() instanceof IsCategorized) {
            name = name + "," + ((IsCategorized) abstractAssistantGUIPlugin.getCLIJMacroPlugin()).getCategories().toLowerCase();
        }

        return name.contains("label");
    }

    public static double parmeterNameToStepSizeSuggestion(String parameterName, boolean small_step) {
        if (parameterName.toLowerCase().contains("sigma")) {
            return small_step ? 0.5 : 2;
        }
        if (parameterName.toLowerCase().contains("gamma")) {
            return small_step ? 0.1 : 1;
        }
        if (parameterName.toLowerCase().contains("relative")) {
            return small_step ? 0.05 : 0.2;
        }
        if (parameterName.toLowerCase().contains("micron")) {
            return small_step ? 0.1 : 5;
        }
        if (parameterName.toLowerCase().contains("angles")) {
            return small_step ? 15 : 90;
        }
        if (parameterName.toLowerCase().contains("degree")) {
            return small_step ? 15 : 90;
        }
        if (parameterName.toLowerCase().contains("long range")) {
            return small_step ? 64 : 256;
        }
        if (parameterName.toLowerCase().contains("constant")) {
            return small_step ? 10 : 100;
        }
        if (parameterName.toLowerCase().contains("zoom")) {
            return small_step ? 0.1 : 1;
        }
        if (parameterName.toLowerCase().contains("size")) {
            return small_step ? 0.05 : 0.1;
        }
        if (parameterName.toLowerCase().contains("error")) {
            return small_step ? 0.01 : 0.1;
        }

        return small_step ? 1 : 10;
    }

    public static void addMenuAction(Menu menu, String label, ActionListener listener) {
        MenuItem submenu = new MenuItem(label);
        if (listener != null) {
            submenu.addActionListener(listener);
        }
        menu.add(submenu);
    }

    public static void execute(String directory, String... command) {
        PrintStream out = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                //IJ.log("" + b);
            }

            @Override
            public void write(byte[] b) throws IOException {
                //IJ.log(new String(b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                byte[] a = new byte[len];
                System.arraycopy(b, off, a, 0, len);
                //IJ.log("" + len);
                if (a.length > 2) {
                    IJ.log(new String(a));
                }
            }
        });

        try {
            ProcessUtils.exec(new File(directory), out, out, command);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public static String jarFromClass(Class klass) {
        return klass.getResource('/' + klass.getName().replace('.', '/') + ".class").toString().split("!")[0];
    }

    private static String has_online_reference = null;

    public static boolean hasOnlineReference(String plugin_name)
    {
        String function_name = pluginNameToFunctionName(plugin_name);
        if (has_online_reference == null) {
            InputStream resourceAsStream = SuggestionService.class.getClassLoader().getResourceAsStream("online_reference.config");
            try {
                has_online_reference = "\n" + StringUtils.streamToString(resourceAsStream, "UTF-8").replace("\r\n", "\n") + "\n";
            } catch (Exception e) {
                return false;
            }
        }
        //System.out.println("Checking " + function_name + " = " + new PyclesperantoGenerator(false).pythonize(function_name));
        return has_online_reference.contains("\n" + function_name + "\n");
    }
    public static void callOnlineReference(String plugin_name) {
        String function_name = pluginNameToFunctionName(plugin_name);
        try {
            Desktop.getDesktop().browse(new URI("https://clij.github.io/clij2-docs/reference_" + function_name));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static String pluginNameToFunctionName(String plugin_name) {
        return plugin_name.replace("CLIJ2_", "").replace("CLIJx_", "");
    }

    public static String getCompatibilityString(String function_name) {
        return "ijm" +
                (isJavaCompatible(function_name)?", java":"");
    }

    private static boolean isJavaCompatible(String function_name) {
        function_name = function_name.toLowerCase().trim();
        return !(
                function_name.startsWith("imagej") ||
                function_name.startsWith("morpholibj") ||
                function_name.startsWith("simpleitk") ||
                function_name.startsWith("bonej") ||
                function_name.startsWith("imglib2")
                );
    }

    public static String distributionName(Class klass) {

        String full_class_name = klass.toString().replace("class ", "");
        //System.out.println("PKG " + full_class_name);
        if (full_class_name.startsWith("net.clesperanto")) {
            return "clEsperanto";
        }
        if (full_class_name.startsWith("net.haesleinhuepf.clij.")) {
            return "CLIJ";
        }
        if (full_class_name.startsWith("net.haesleinhuepf.clij2.")) {
            return "CLIJ2";
        }
        if (full_class_name.startsWith("net.haesleinhuepf.clijx.weka.")) {
            return "CLIJxWEKA";
        }
        if (full_class_name.startsWith("net.haesleinhuepf.clijx.simpleitk.")) {
            return "SimpleITK";
        }
        if (full_class_name.startsWith("net.haesleinhuepf.clijx.morpholibj.")) {
            return "MorpholibJ";
        }
        if (full_class_name.startsWith("net.haesleinhuepf.clijx.imglib2.")) {
            return "Imglib2";
        }
        if (full_class_name.startsWith("net.haesleinhuepf.clijx.imagej3dsuite.")) {
            return "ImageJ 3D Suite";
        }
        if (full_class_name.startsWith("net.haesleinhuepf.clijx.imagej2.")) {
            return "ImageJ2";
        }
        if (full_class_name.startsWith("net.haesleinhuepf.clijx.bonej.")) {
            return "BoneJ";
        }
        if (full_class_name.startsWith("net.haesleinhuepf.clijx.imagej.")) {
            return "ImageJ";
        }
        if (full_class_name.startsWith("net.haesleinhuepf.clijx.")) {
            return "CLIJx";
        }
        return "unknown";
    }

    public static String niceName(String name) {

        //name = name.replace("3D", "");
        //name = name.replace("Box", "");

        String result = name;

        result = result.replace("SimpleITK", "");
        result = result.replace("ImageJ2", "");
        result = result.replace("imageJ2", "");
        result = result.replace("MorphoLibJ", "");
        result = result.replace("ImageJ3DSuite", "");
        result = result.replace("BoneJ", "");
        result = result.replace("simpleITK", "");
        result = result.replace("morphoLibJ", "");
        result = result.replace("imageJ3DSuite", "");
        result = result.replace("boneJ", "");
        result = result.replace("CLIJxWEKA", "");
        result = result.replace("CLIJx", "");
        result = result.replace("CLIJ2", "");
        result = result.replace("CLIJ", "");
        result = result.replace("ImageJ", "");
        result = result.replace("_", " ");
        result = result.replace("  ", " ");

        result = result.trim();

        name = result;
        result = "";
        for (int i = 0; i < name.length(); i++) {
            String ch = name.substring(i,i+1);
            if (!ch.toLowerCase().equals(ch)) {
                result = result + " ";
            }
            result = result + ch;
        }

        result = result.replace("C L", "CL");
        result = result.replace("2 D", "2D");
        result = result.replace("3 D", "3D");
        result = result.replace("X Y", "XY");
        result = result.replace("X Z", "XZ");
        result = result.replace("Y Z", "YZ");
        //result = result.replace("_ ", " ");
        result = result.replace("I J", "IJ");
        result = result.replace("Do G", "DoG");
        result = result.replace("Lo G", "LoG");
        result = result.replace("Cl Esperanto", "clEsperanto");
        result = result.replace("Morpho Lib J", "MorphoLibJ");
        result = result.replace("Simple I T K", "SimpleITK");
        result = result.replace("D Suite", "DSuite");
        result = result.replace("Bone J", "BoneJ");
        result = result.replace("CL IJ", "CLIJ");
        result = result.replace("R O I ", "ROI");
        result = result.replace("F F T", "FFT");
        result = result.replace("X Or", "XOr");
        result = result.replace("W E K A", "WEKA");

        result = result.substring(0, 1).toUpperCase() + result.substring(1);

        result = result.trim();

        //System.out.println("Name out: " + result);

        return result;
    }

    public static void main(String[] args) {
//        AbstractAssistantGUIPlugin.show_advanced = true;
//        System.out.println(isIncubatablePlugin(new GenerateTouchMatrix()));
        //System.out.println(niceName("CLIJx_SimpleITKWhateverFilter"));
        //System.out.println(isCleCompatible("thresholdOtsu"));
        /*
        System.out.println(isIncubatablePlugin(new SeededWatershed()));

        new ImageJ();
        CLIJx.getInstance("RTX");

        ImagePlus imp = IJ.openImage("C:/structure/data/blobs.tif");
        imp.show();

        AssistantGUIPlugin agp = new GenericAssistantGUIPlugin(new GaussianBlur2D());
        agp.run("");

        System.out.println(isSuitable(new SeededWatershed(), agp));
*/

        for (AssistantGUIPlugin p : MenuService.getInstance().getPluginsInCategory("All", new ThresholdOtsu())) {
            System.out.println(p.getName());
        }

    }

    public static void openIcyProtocol(String protocol_filename) {
        System.out.println("Opening ICY: " + protocol_filename);
        new Thread(new Runnable() {
            @Override
            public void run() {

                boolean isWindows = System.getProperty("os.name")
                        .toLowerCase().startsWith("windows");

                PrintStream out = new PrintStream(new OutputStream() {
                    @Override
                    public void write(int b) throws IOException {
                        //IJ.log("" + b);
                    }

                    @Override
                    public void write(byte[] b) throws IOException {
                        //IJ.log(new String(b));
                    }

                    @Override
                    public void write(byte[] b, int off, int len) throws IOException {
                        byte[] a = new byte[len];
                        System.arraycopy(b, off, a, 0, len);
                        //IJ.log("" + len);
                        if (a.length > 2) {
                            IJ.log(new String(a));
                        }
                    }
                });

                File directory = new File(protocol_filename).getParentFile();

                try {
                    ProcessUtils.exec(directory, out, out, AssistantOptions.getInstance().getIcyExecutable(),
                            "-x", "plugins.adufour.protocols.Protocols", "protocol=" + protocol_filename);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static File getNewSelfDeletingTempDir() {
        String location = System.getProperty("java.io.tmpdir") + "/temp" + System.currentTimeMillis() + "/";
        File dir = new File(location);
        dir.mkdirs();
        dir.deleteOnExit();

        return dir;
    }

    public static void attachCloseListener(ImagePlus my_target) {
        /*ImageWindow frame = my_target.getWindow();
        if (frame == null) {
            return;
        }

        WindowListener[] list = frame.getWindowListeners();
        for (WindowListener listener : list) {
            frame.removeWindowListener(listener);
        }*/
    }

    static Boolean isCLIJxAssistantInstalled = null;
    public static boolean CLIJxAssistantInstalled() {
        if (isCLIJxAssistantInstalled != null) {
            return isCLIJxAssistantInstalled;
        }
        isCLIJxAssistantInstalled = true;
        try {
            String dir = IJ.getDirectory("imagej");
            if (!dir.contains("null") && dir.toLowerCase().contains("fiji")) {
                // we're in a Fiji folder
                File plugins_dir = new File(dir + "/plugins");
                if (!jarExists(plugins_dir, "clijx-assistant_")) {
                    isCLIJxAssistantInstalled = false;
                }
            }
        }catch (Exception e) {
            System.out.println("Error while checking the CLIJ2 installation:");
            System.out.println(e.getMessage());
        }

        return isCLIJxAssistantInstalled;
    }

    private static boolean jarExists(File folder, String name) {
        return folder.list((dir, name1) -> name1.contains(name)).length > 0;
    }

/*
    class CloseListener implements WindowListener {

        @Override
        public void windowClosing(WindowEvent e) {

        }
    }*/
}
