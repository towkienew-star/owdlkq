package dev.towka.morphstudio.gui;

import dev.towka.morphstudio.FakeManager;
import dev.towka.morphstudio.ProfileStore;
import dev.towka.morphstudio.VisualProfile;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class PlayerEditorScreen extends Screen {
    private UUID selected;
    private int page;
    private List<PlayerListEntry> entries = List.of();
    private String selectedName = "";
    private int left, top, panelW, panelH, listW, contentX, contentW;

    public PlayerEditorScreen() { super(Text.literal("Morph Studio")); }

    @Override protected void init() {
        if (client == null) return;
        panelW = Math.min(510,width-20);
        panelH = Math.min(300,height-16);
        left=(width-panelW)/2;
        top=(height-panelH)/2;
        listW=146;
        contentX=left+listW+18;
        contentW=panelW-listW-28;
        if (client.getNetworkHandler() != null) {
            entries = new ArrayList<>(client.getNetworkHandler().getPlayerList());
            entries.sort(Comparator.comparing(x->x.getProfile().getName(),String.CASE_INSENSITIVE_ORDER));
        }
        if (selected == null && client.player != null) selected = client.player.getUuid();
        if (!entries.isEmpty() && entries.stream().noneMatch(p->p.getProfile().getId().equals(selected))) {
            selected = entries.get(0).getProfile().getId();
        }
        selectedName="";
        for (PlayerListEntry e:entries) if (e.getProfile().getId().equals(selected)) selectedName=e.getProfile().getName();

        int visible= Math.max(2, Math.min(7, (panelH-102)/24));
        page = Math.max(0,Math.min(page,Math.max(0,(entries.size()-1)/visible)));
        for (int i=page*visible; i<Math.min(entries.size(),(page+1)*visible);i++) {
            final PlayerListEntry entry=entries.get(i);
            final String name=entry.getProfile().getName();
            int y=top+43+(i-page*visible)*23;
            addDrawableChild(ButtonWidget.builder(Text.literal((entry.getProfile().getId().equals(selected)?"§b▶ ":"")+name), b->{
                selected=entry.getProfile().getId();
                clearAndInit();
            }).dimensions(left+10,y,listW-10,20).build());
        }
        int navY=top+46+visible*23;
        addDrawableChild(ButtonWidget.builder(Text.literal("<"), b->{if(page>0){page--;clearAndInit();}})
            .dimensions(left+10,navY,40,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal(">"), b->{if((page+1)*visible<entries.size()){page++;clearAndInit();}})
            .dimensions(left+listW-40,navY,40,20).build());

        if (selected != null) {
            for (int i=0;i<VisualProfile.KEYS.length;i++) {
                addDrawableChild(new ScaleSlider(contentX,top+40+i*24,contentW,selected,i));
            }
            addDrawableChild(ButtonWidget.builder(Text.literal("Reset player"), b->{
                ProfileStore.reset(selected);clearAndInit();
            }).dimensions(contentX,top+211,contentW,20).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("FAKE PLAYER >"), b->client.setScreen(new FakeEditorScreen()))
            .dimensions(contentX,top+panelH-48,contentW,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b->close())
            .dimensions(contentX,top+panelH-26,contentW,20).build());
    }

    @Override public void render(DrawContext context,int mx,int my,float delta) {
        renderBackground(context,mx,my,delta);
        context.fill(left-4,top-4,left+panelW+4,top+panelH+4,0xE30C1524);
        context.fill(left-4,top-4,left+panelW+4,top-1,0xFF84D8EE);
        context.fill(left+listW+8,top+28,left+listW+9,top+panelH-8,0xFF41566E);
        context.drawCenteredTextWithShadow(textRenderer,"MORPH STUDIO",width/2,top+9,0xFFD1F2FC);
        context.drawTextWithShadow(textRenderer,"PLAYERS ("+entries.size()+")",left+10,top+29,0xFFA7C4D7);
        context.drawTextWithShadow(textRenderer,selectedName.isEmpty()?"No player selected":selectedName,contentX,top+27,0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer,"LOCAL MODEL ONLY",left+10,top+panelH-17,0xFF829BAA);
        super.render(context,mx,my,delta);
    }
    @Override public void close() { ProfileStore.save();super.close(); }
    @Override public boolean shouldPause() { return false; }
}
