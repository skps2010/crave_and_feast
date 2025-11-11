package com.skps2010;


import java.util.List;

public class FDConfig {
    public float cravingMultiplier = 2.0f;
    public String cravingDisplay = "cf.craving";
    public long cravingChangeInterval = 24000L;
    public int recordingFoodCount = 32;
    public int cravingMaxCount = 8;
    public int hudX = 10;
    public int hudY = 10;
    public List<Rule> rules = List.of(
            new Rule(0, 2.0f, "cf.rule.never"),
            new Rule(7, 1.5f, "cf.rule.rare"),
            new Rule(-1, 1.0f, "cf.rule.often")
    );

    public static class Rule {
        public int maxCount;
        public float multiplier;
        public String display;

        public Rule(int m, float k, String d) {
            maxCount = m;
            multiplier = k;
            display = d;
        }
    }
}