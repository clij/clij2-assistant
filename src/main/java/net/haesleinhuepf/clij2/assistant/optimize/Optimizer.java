package net.haesleinhuepf.clij2.assistant.optimize;

import net.haesleinhuepf.clij2.assistant.utilities.Logger;
import org.apache.commons.math3.analysis.MultivariateFunction;

public interface Optimizer {
    double[] optimize(double[] current, Workflow workflow, int[] parameter_index_map, MultivariateFunction fitness, Logger logger);
}
