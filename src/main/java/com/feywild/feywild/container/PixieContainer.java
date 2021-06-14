package com.feywild.feywild.container;

import com.feywild.feywild.entity.util.FeyEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.swing.plaf.basic.BasicOptionPaneUI;

public class PixieContainer extends Container {

    private PlayerEntity playerEntity;
    private IItemHandler playerInventory;
    private FeyEntity entity;
    private int size = 256;

    public PixieContainer(int windowId, PlayerInventory playerInventory, PlayerEntity player, FeyEntity entity) {

        super(ModContainers.PIXIE_CONTAINER.get(), windowId);
        this.entity = entity;
        this.playerEntity = player;
        this.playerInventory = new InvWrapper(playerInventory);

        if (this.entity != null) {

            entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
                addSlot(new SlotItemHandler(h, 0, -(size/4) + 40, 140));
            });
        }
    }


    @Override //canInteractWith
    public boolean stillValid(PlayerEntity playerIn) {
        return true;

    }

}
