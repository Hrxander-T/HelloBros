package util;

import java.util.Random;

public class NameGenerator {
    private static final String[] ADJECTIVES = {
        "Cool", "Happy", "Swift", "Brave", "Wild", "Lucky",
        "Bold", "Bright", "Calm", "Clever", "Daring", "Eager",
        "Fancy", "Gentle", "Hearty", "Jolly", "Keen", "Lively"
    };

    private static final String[] NOUNS = {
        "Fox", "Bear", "Eagle", "Wolf", "Tiger", "Hawk",
        "Lion", "Hawk", "Raven", "Falcon", "Otter", "Lynx",
        "Panda", "Koala", "Bison", "Moose", "Heron", "Ibis"
    };

    private static final Random random = new Random();

    public static String generate() {
        String adj = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[random.nextInt(NOUNS.length)];
        int num = random.nextInt(1000);
        return adj + noun + num;
    }
}