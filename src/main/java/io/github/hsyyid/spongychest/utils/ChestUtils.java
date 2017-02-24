package io.github.hsyyid.spongychest.utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public class ChestUtils {
	public static boolean containsItem(TileEntityChest chest, ItemStackSnapshot snapshot) {
		int foundItems = 0;
		Item item = Item.getByNameOrId(snapshot.getType().getId());

		if (item != null) {
			for (int i = 0; i < chest.getSizeInventory(); i++) {
				ItemStack stack = chest.getStackInSlot(i);
				if (stack != null && stack.getItem().equals(item) && (equal(snapshot.createStack(), convertToSponge(stack)))) 
				{

					// getStackSize
					foundItems += stack.stackSize;

					if (foundItems >= snapshot.getCount()) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public static void removeItems(TileEntityChest chest, ItemStackSnapshot snapshot) {
		int neededItems = snapshot.getCount();
		int foundItems = 0;
		Item item = Item.getByNameOrId(snapshot.getType().getId());

		if (item != null) {
			for (int i = 0; i < chest.getSizeInventory(); i++) {
				ItemStack stack = chest.getStackInSlot(i);

				if (stack != null && stack.getItem().equals(item) && (equal(snapshot.createStack(), convertToSponge(stack))))
				{
					if (neededItems >= foundItems + stack.stackSize) {
						chest.removeStackFromSlot(i);
						foundItems += stack.stackSize;
					} else {
						int amount = (foundItems + stack.stackSize) - neededItems;
						stack.stackSize = amount;
						foundItems = neededItems;
					}
				}

				if (foundItems == neededItems) {
					return;
				}
			}
		}

	}

	public static org.spongepowered.api.item.inventory.ItemStack convertToSponge(
			net.minecraft.item.ItemStack itemStack) {
		return (org.spongepowered.api.item.inventory.ItemStack) (Object) itemStack;
	}

	public static net.minecraft.item.ItemStack convertFromSponge(
			org.spongepowered.api.item.inventory.ItemStack itemStack) {
		return (net.minecraft.item.ItemStack) (Object) itemStack;
	}

	public static boolean equal(org.spongepowered.api.item.inventory.ItemStack stack1,
			org.spongepowered.api.item.inventory.ItemStack stack2) {
		org.spongepowered.api.item.inventory.ItemStack first = stack1.copy();
		org.spongepowered.api.item.inventory.ItemStack second = stack2.copy();

		first.setQuantity(1);
		second.setQuantity(1);

		return first.equalTo(second);
	}

}
