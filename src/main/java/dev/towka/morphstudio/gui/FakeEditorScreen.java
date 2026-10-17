package dev.towka.morphstudio.gui;

import dev.towka.morphstudio.FakeManager;
import dev.towka.morphstudio.FakeSettings;
import dev.towka.morphstudio.FakeSkins;
import dev.towka.morphstudio.Pose;
import dev.towka.morphstudio.ProfileStore;
import dev.towka.morphstudio.VisualProfile;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class FakeEditorScreen extends Screen {
    private int tab = 0;
    private int slot = 4;
    private int left, top, panelW, panelH, x, w;
    private TextFieldWidget field;
    private String info = "Right-click the fake actor in world to edit.";
    private static final String[] TABS={"Actor", "Pose / Size", "Equipment", "Skin"};
    private static final String[] SLOTS={"Head", "Chest", "Legs", "Feet", "Main hand", "Off hand"};

    public FakeEditorScreen() { super(Text.literal("Fake Player Editor")); }

    @Override protected void init() {
        panelW = Math.min(545,width-18);
        panelH = Math.min(328,height-14);
        left=(width-panelW)/2;
        top=(height-panelH)/2;
        x=left+16;
        w=panelW-32;
        int tabW=(w-9)/4;
        for (int i=0;i<TABS.length;i++) {
            final int which=i;
            addDrawableChild(ButtonWidget.builder(Text.literal((tab==i?"§b":"")+TABS[i]), b->{
                tab=which; clearAndInit();
            }).dimensions(x+i*(tabW+3),top+34,tabW,20).build());
        }
        switch (tab) {
            case 0 -> actorPage();
            case 1 -> posePage();
            case 2 -> equipmentPage();
            case 3 -> skinPage();
            default -> { }
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("< Players"), b->{
            ProfileStore.save();FakeManager.save();
            client.setScreen(new PlayerEditorScreen());
        }).dimensions(x,top+panelH-28,110,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b->close())
            .dimensions(left+panelW-100,top+panelH-28,84,20).build());
    }

    private void actorPage() {
        btn(0, "Spawn fake player in front", ()->{FakeManager.spawn(); info="Fake player spawned in front of you.";clearAndInit();});
        btn(1, "Move fake player in front", ()->{FakeManager.moveInFront(); info="Moved near your camera.";});
        btn(2, "Delete fake player", ()->{FakeManager.remove(); info="Fake player deleted.";clearAndInit();});
        btn(3, "How to edit: right-click fake player", ()->{});
        btn(4, "All effects are visible only on your client", ()->{});
    }

    private void posePage() {
        VisualProfile p=ProfileStore.edit(FakeManager.FAKE_UUID);
        btn(0,"< Previous pose",()->{p.pose=p.pose.previous();ProfileStore.save();clearAndInit();});
        btn(1,"Pose: "+p.pose.label+"   (click for next)",()->{p.pose=p.pose.next();ProfileStore.save();clearAndInit();});
        int sliderW=Math.min(245,w);
        for (int i=0; i<VisualProfile.KEYS.length;i++) {
            int col=i<4?0:1;
            int row=i<4?i:i-4;
            int sx=x+col*(w/2+5);
            int sy=top+120+row*25;
            addDrawableChild(new ScaleSlider(sx,sy,Math.min(sliderW,w/2-5),FakeManager.FAKE_UUID,i));
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset body & pose"), b->{
            ProfileStore.reset(FakeManager.FAKE_UUID);clearAndInit();
        }).dimensions(x,top+225,w,20).build());
    }

    private void equipmentPage() {
        FakeSettings s=FakeManager.SETTINGS;
        btn(0,"Cycle armor set: "+armorName(s.armorPreset),()->{FakeManager.cycleArmor();clearAndInit();});
        btn(1,"Selected slot: "+SLOTS[slot]+"   (click to change)",()->{slot=(slot+1)%6;clearAndInit();});
        field=new TextFieldWidget(textRenderer,x,top+140,w-104,20,Text.literal("Item ID"));
        field.setMaxLength(100);
        field.setText(s.getSlot(slot));
        addDrawableChild(field);
        addDrawableChild(ButtonWidget.builder(Text.literal("Apply item"),b->{
            String id=field.getText().trim();
            if (FakeManager.isValidItem(id)) {
                s.setSlot(slot,id);FakeManager.updateEquipment();FakeManager.save();
                info="Updated "+SLOTS[slot]+".";
            } else {
                info="Unknown ID: "+id;
            }
            clearAndInit();
        }).dimensions(x+w-98,top+140,98,20).build());
        btn(4,"Remove selected item",()->{
            s.setSlot(slot,"minecraft:air");FakeManager.updateEquipment();FakeManager.save();clearAndInit();
        });
    }

    private void skinPage() {
        FakeSettings s=FakeManager.SETTINGS;
        field=new TextFieldWidget(textRenderer,x,top+98,w-140,20,Text.literal("Online player skin name"));
        field.setMaxLength(40);
        field.setText(s.skinName);
        addDrawableChild(field);
        addDrawableChild(ButtonWidget.builder(Text.literal("Use skin"),b->{
            s.skinName=field.getText().trim().isEmpty()?"SELF":field.getText().trim();
            s.useLocalSkin=false;
            FakeManager.save();
            info="Skin: "+s.skinName+" (must be online; SELF works).";
        }).dimensions(x+w-134,top+98,134,20).build());
        btn(2,"Copy your own skin (SELF)",()->{
            s.skinName="SELF";s.useLocalSkin=false;FakeManager.save();clearAndInit();
        });
        btn(3,"Load config/morphstudio/skin.png",()->{
            boolean ok=FakeSkins.reloadPng();
            if (ok) {s.useLocalSkin=true;FakeManager.save();info="Local 64x64 PNG loaded.";}
            else info="No valid 64x64 PNG in config/morphstudio/skin.png";
            clearAndInit();
        });
        btn(4,"PNG model: "+(s.slimArms?"SLIM":"CLASSIC")+" (click to switch)",()->{
            s.slimArms=!s.slimArms;FakeManager.save();clearAndInit();
        });
    }

    private String armorName(int id) {
        return switch(id) {case 1->"Leather";case 2->"Iron";case 3->"Diamond";case 4->"Netherite";case 5->"Gold";default->"None";};
    }
    private void btn(int row,String label,Runnable click) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label),b->click.run())
            .dimensions(x,top+70+row*30,w,22).build());
    }
    @Override public void render(DrawContext context,int mx,int my,float delta) {
        renderBackground(context,mx,my,delta);
        context.fill(left-4,top-4,left+panelW+4,top+panelH+4,0xE3101925);
        context.fill(left-4,top-4,left+panelW+4,top-1,0xFF81C9DA);
        context.drawCenteredTextWithShadow(textRenderer,"MORPH STUDIO / FAKE PLAYER",width/2,top+10,0xFFE9F8FF);
        context.drawTextWithShadow(textRenderer,FakeManager.exists()?"FAKE PLAYER: SPAWNED":"FAKE PLAYER: NOT SPAWNED",x,top+58,0xFF91B9C5);
        context.drawTextWithShadow(textRenderer,info,x,top+panelH-46,0xFF9AB7C9);
        super.render(context,mx,my,delta);
    }
    @Override public void close() {
        ProfileStore.save();FakeManager.save();super.close();
    }
    @Override public boolean shouldPause() { return false; }
}
