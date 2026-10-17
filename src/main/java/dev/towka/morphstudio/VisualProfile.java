package dev.towka.morphstudio;

/** Model-only sizes, not collision boxes or network values. */
public final class VisualProfile {
    public static final String[] KEYS = {"overall", "height", "width", "head", "torso", "arms", "legs"};
    public static final String[] LABELS = {"Overall", "Height", "Width", "Head", "Torso", "Arms", "Legs"};
    public float overall = 1f;
    public float height = 1f;
    public float width = 1f;
    public float head = 1f;
    public float torso = 1f;
    public float arms = 1f;
    public float legs = 1f;
    public Pose pose = Pose.DEFAULT;

    public float get(int i) {
        return switch (i) {
            case 0 -> overall;
            case 1 -> height;
            case 2 -> width;
            case 3 -> head;
            case 4 -> torso;
            case 5 -> arms;
            case 6 -> legs;
            default -> 1f;
        };
    }

    public void set(int i, float v) {
        float min = i <= 2 ? .3f : .45f;
        float max = i == 0 ? 3.5f : 2.8f;
        v = Math.max(min, Math.min(max, v));
        switch (i) {
            case 0 -> overall = v;
            case 1 -> height = v;
            case 2 -> width = v;
            case 3 -> head = v;
            case 4 -> torso = v;
            case 5 -> arms = v;
            case 6 -> legs = v;
            default -> { }
        }
    }
}
