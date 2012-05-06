package cymbol.model;

public class Util {

    public static String capitalize(String str) {
        String firstLetter = str.substring(0, 1);
        String rest = str.substring(1);
        return firstLetter.toUpperCase() + rest;
    }
}
