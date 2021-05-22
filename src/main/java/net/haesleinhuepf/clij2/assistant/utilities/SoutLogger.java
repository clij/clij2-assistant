package net.haesleinhuepf.clij2.assistant.utilities;

public class SoutLogger implements Logger {
    @Override
    public void log(String text) {
        System.out.println(text);
    }
}
