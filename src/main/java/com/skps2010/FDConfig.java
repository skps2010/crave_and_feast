package com.skps2010;


import java.util.List;

public class FDConfig {
    public float cravingMultiplier = 2.0f;
    public String cravingDisplay = "Craving!";
    public long cravingChangeInterval = 36000L;
    public int recordingFoodCount = 32;
    public List<Rule> rules = List.of(
            new Rule(1, 2.0f, "never eaten"),
            new Rule(8, 1.5f, "rarely eaten"),
            new Rule(-1, 1.0f, "often eaten")
    );

    public static class Rule {
        public int maxCount;
        public float multiplier;
        public String display;
        public Rule() {}
        public Rule(int m, float k, String d) {
            maxCount = m; multiplier = k; display = d;
        }
    }
}