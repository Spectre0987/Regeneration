package me.suff.mc.regen.client.screen;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;

import net.minecraft.client.gui.widget.button.Button.IPressable;
import net.minecraft.client.gui.widget.button.Button.ITooltip;

public class DescButton extends Button {

    private ArrayList< IReorderingProcessor > description = null;

    public DescButton(int x, int y, int width, int height, ITextComponent title, IPressable pressedAction) {
        super(x, y, width, height, title, pressedAction);
    }

    public DescButton(int x, int y, int width, int height, ITextComponent title, IPressable pressedAction, ITooltip onTooltip) {
        super(x, y, width, height, title, pressedAction, onTooltip);
    }

    public ArrayList< IReorderingProcessor > getDescription() {
        return description;
    }

    public void setDescription(TranslationTextComponent[] description) {
        ArrayList< IReorderingProcessor > reorderingProcessors = new ArrayList<>();
        for (TranslationTextComponent textComponent : description) {
            reorderingProcessors.add(textComponent.getVisualOrderText());
        }
        this.description = reorderingProcessors;
    }
}
