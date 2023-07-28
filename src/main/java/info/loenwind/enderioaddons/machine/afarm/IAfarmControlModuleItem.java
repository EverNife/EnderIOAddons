package info.loenwind.enderioaddons.machine.afarm;

import net.minecraft.item.ItemStack;

import info.loenwind.enderioaddons.machine.afarm.module.IAfarmControlModule;

public interface IAfarmControlModuleItem {

    IAfarmControlModule getWorker(ItemStack stack);

}
