package dev.towka.morphstudio.gui;

import dev.towka.morphstudio.ProfileStore;
import dev.towka.morphstudio.VisualProfile;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import java.util.Locale;
import java.util.UUID;

public final class ScaleSlider extends SliderWidget {
    private final UUID selected;
    private final int index;

    public ScaleSlider(int x, int y, int width, UUID selected, int index) {
        super(x, y, width, 20, Text.empty(), getNormalized(selected, index));
        this.selected=selected;
        this.index=index;
        updateMessage();
    }
    private static float min(int index) { return index <= 2 ? .3f : .45f; }
    private static float max(int index) { return index == 0 ? 3.5f : 2.8f; }
    private static double getNormalized(UUID who, int which) {
        return Math.max(0D,Math.min(1D,(ProfileStore.edit(who).get(which)-min(which))/(max(which)-min(which))));
    }
    private float actual() { return (float)(min(index)+value*(max(index)-min(index))); }
    @Override protected void updateMessage() {
        setMessage(Text.literal(VisualProfile.LABELS[index]+": "+String.format(Locale.ROOT,"%.2fx",actual())));
    }
    @Override protected void applyValue() {
        ProfileStore.edit(selected).set(index,actual());
    }
}
