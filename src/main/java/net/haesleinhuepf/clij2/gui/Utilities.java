package net.haesleinhuepf.clij2.gui;

public class Utilities {
    public static String generateIconCodeString(String icon)
    {
        String[] positions = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "0"};

        String result = "C000";
        int x = 0;
        int y = 0;

        char empty = new String(" ").charAt(0);
        //DebugHelper.print(new Object(), "len: " + icon.length());
        for (int i = 0; i < icon.length(); i++)
        {
            //DebugHelper.print(new Object(), "|" + icon.charAt(i) + " == " + empty + "|");
            if (icon.charAt(i) != empty)
            {
                result = result.concat("D" + positions[x] + positions[y]);
            }

            x++;
            if (x > 15)
            {
                x = 0;
                y++;
            }
        }
        //DebugHelper.print(new Object(), result);
        return result;
    }
}
