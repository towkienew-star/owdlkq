package dev.towka.morphstudio;

public enum Pose {
    DEFAULT("Stand"),
    T_POSE("T-Pose"),
    ARMS_CROSSED("Folded arms"),
    WAVE("Wave"),
    DAB("Dab"),
    POINT("Point"),
    HANDS_UP("Hands up"),
    SIT("Sitting"),
    CROUCH("Crouching"),
    LOOK_DOWN("Look down"),
    DANCE("Dance");

    public final String label;
    Pose(String label) { this.label = label; }
    public Pose next() { return values()[(ordinal() + 1) % values().length]; }
    public Pose previous() { return values()[(ordinal() + values().length - 1) % values().length]; }
}
