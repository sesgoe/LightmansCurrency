package io.github.lightman314.lightmanscurrency.util;

import net.minecraft.util.math.BlockPos;

public class NumberUtil {

    public static boolean IsInteger(String text)
    {
        if(text == null)
            return false;
        try
        {
            @SuppressWarnings("unused")
            int i = Integer.parseInt(text);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    public static int GetIntegerValue(String text, int defaultValue)
    {
        if(IsInteger(text))
            return Integer.parseInt(text);
        return defaultValue;
    }

    public static String toShortString(BlockPos pos) { return "" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ(); }

}
