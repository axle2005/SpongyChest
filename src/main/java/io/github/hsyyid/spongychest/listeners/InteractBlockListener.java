package io.github.hsyyid.spongychest.listeners;

import io.github.hsyyid.spongychest.SpongyChest;
import io.github.hsyyid.spongychest.data.isspongychest.IsSpongyChestData;
import io.github.hsyyid.spongychest.data.isspongychest.SpongeIsSpongyChestData;
import io.github.hsyyid.spongychest.data.itemchest.ItemChestData;
import io.github.hsyyid.spongychest.data.itemchest.SpongeItemChestData;
import io.github.hsyyid.spongychest.data.pricechest.PriceChestData;
import io.github.hsyyid.spongychest.data.pricechest.SpongePriceChestData;
import io.github.hsyyid.spongychest.data.uuidchest.SpongeUUIDChestData;
import io.github.hsyyid.spongychest.data.uuidchest.UUIDChestData;
import io.github.hsyyid.spongychest.utils.ChestShopModifier;
import io.github.hsyyid.spongychest.utils.ChestUtils;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.text.TextFormatting;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.block.trait.EnumTraits;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.serializer.TextFormatConfigSerializer;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;

import de.randombyte.holograms.api.HologramsService;
import de.randombyte.holograms.api.HologramsService.Hologram;

import java.math.BigDecimal;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InteractBlockListener
{
	private SpongyChest plugin;
    public InteractBlockListener(SpongyChest plugin) {
		this.plugin=plugin;
	}
	private <E extends Event & Cancellable> void purchase(E event, Chest chest, Player player) {
		ItemStackSnapshot item = chest.get(ItemChestData.class).get().itemStackSnapshot().get();
		double price = chest.get(PriceChestData.class).get().price().get();
		UUID ownerUuid = chest.get(UUIDChestData.class).get().uuid().get();
		TileEntityChest realChest = (TileEntityChest) chest;

		/*if (player.getUniqueId().equals(ownerUuid))
		{
			return;
		}*/

		if (ChestUtils.containsItem(realChest, item))
		{
			UniqueAccount ownerAccount = SpongyChest.economyService.getOrCreateAccount(ownerUuid).get();
			UniqueAccount userAccount = SpongyChest.economyService.getOrCreateAccount(player.getUniqueId()).get();

			if (userAccount.transfer(ownerAccount, SpongyChest.economyService.getDefaultCurrency(), new BigDecimal(price), Cause.of(NamedCause.source(player))).getResult() == ResultType.SUCCESS)
			{
				ChestUtils.removeItems(realChest, item);
				InventoryTransactionResult result = player.getInventory().offer(item.createStack());
				Collection<ItemStackSnapshot> rejectedItems = result.getRejectedItems();

				player.sendMessage(Text.of(TextColors.BLUE, "[SpongyChest]: ", TextColors.GREEN, "Purchased item(s)."));

				if (rejectedItems.size() > 0) {
                    Location<World> location = player.getLocation();
					World world = location.getExtent();
					PluginContainer pluginContainer = Sponge.getPluginManager().getPlugin("spongychest").get();

					for (ItemStackSnapshot rejectedSnapshot : rejectedItems) {
                        Item rejectedItem = (Item) world.createEntity(EntityTypes.ITEM, location.getPosition());

                        rejectedItem.offer(Keys.REPRESENTED_ITEM, rejectedSnapshot);
                        //rejectedItem.item().set(rejectedSnapshot);

						Cause cause = Cause.source(EntitySpawnCause.builder().entity(rejectedItem).type(SpawnTypes.PLUGIN).build())
								.owner(pluginContainer)
								.notifier(event.getCause())
								.build();

						world.spawnEntity(rejectedItem, cause);
					}

					player.sendMessage(Text.of(TextColors.BLUE, "[SpongyChest]: ", TextColors.YELLOW, "Some of the items could not be added to your inventory, so they have been thrown on the ground instead."));
				}
			}
			else
			{
				player.sendMessage(Text.of(TextColors.BLUE, "[SpongyChest]: ", TextColors.RED, "You don't have enough money to use this shop."));
			}
		}
		else
		{
			player.sendMessage(Text.of(TextColors.BLUE, "[SpongyChest]: ", TextColors.RED, "This shop is out of stock."));
		}

		event.setCancelled(true);
	}
	@Listener
	public void onPlayerInteractBlock(InteractBlockEvent.Secondary event, @Root Player player)
	{
		if (event.getTargetBlock().getLocation().isPresent() && event.getTargetBlock().getState().getType() == BlockTypes.CHEST)
		{
			Chest chest = (Chest) event.getTargetBlock().getLocation().get().getTileEntity().get();

			if (chest.get(IsSpongyChestData.class).isPresent() && chest.get(IsSpongyChestData.class).get().isSpongyChest().get())
			{
				purchase(event, chest, player);
			}
			else if (player.hasPermission("spongychest.shop.create"))
			{
				Optional<ChestShopModifier> chestShopModifier = SpongyChest.chestShopModifiers.stream().filter(m -> m.getUuid().equals(player.getUniqueId())).findAny();

				if (chestShopModifier.isPresent())
				{
					chest.offer(new SpongeIsSpongyChestData(true));
					chest.offer(new SpongeItemChestData(chestShopModifier.get().getItem()));
					chest.offer(new SpongePriceChestData(chestShopModifier.get().getPrice().doubleValue()));
					chest.offer(new SpongeUUIDChestData(chestShopModifier.get().getUuid()));
					SpongyChest.chestShopModifiers.remove(chestShopModifier.get());

					Optional<Direction> dir = chest.getBlock().get(Keys.DIRECTION);
					List<Text> list = new ArrayList<Text>(Arrays.asList());
					Location<World> holoLocation = chest.getLocation().getBlockRelative(dir.get());
							//.add(0.5, 1, 0.5);
					ItemStack frameStack = chestShopModifier.get().getItem().createStack();

					
					holoLocation.setBlockType(BlockTypes.WALL_SIGN,
							Cause.source(Sponge.getPluginManager().fromInstance(plugin).get()).build());
					
					TileEntity tileEntity = holoLocation.getTileEntity().get();
					setLines(tileEntity,Text.of(TextColors.BLUE,"Buy"),Text.of(TextColors.BLUE, frameStack.getItem().getName()),Text.of(TextColors.BLUE, frameStack.getQuantity()),Text.of(TextColors.BLUE, SpongyChest.economyService.getDefaultCurrency().getSymbol().toPlain(), chestShopModifier.get().getPrice()) );
					
					//Hologram Code
					/*
					//list.add(Text.of(TextColors.GREEN, "  Item  "));
					list.add(Text.of(TextColors.GOLD, frameStack.getItem().getName()," x",frameStack.getQuantity()));
					//list.add(Text.of(TextColors.GREEN, "  Amount  "));
					//list.add(Text.of(TextColors.BLUE, frameStack.getQuantity()));
					//list.add(Text.of(TextColors.GREEN, "  Price  "));
					list.add(Text.of(TextColors.GOLD, SpongyChest.economyService.getDefaultCurrency().getSymbol().toPlain(), chestShopModifier.get().getPrice()));
					Optional<List<Hologram>> hologramOptional = plugin.hologramService
					        .createMultilineHologram(holoLocation, list, 0.2);
		*/
					
					
					player.sendMessage(Text.of(TextColors.BLUE, "[SpongyChest]: ", TextColors.GREEN, "Created shop."));
					event.setCancelled(true);
				}
			}
		}
	}

	//@Listener
	public void onPlayerInteractEntity(InteractEntityEvent.Secondary event, @First Player player)
	{
		if (event.getTargetEntity().getType() == EntityTypes.ITEM_FRAME)
		{
			ItemFrame frame = (ItemFrame) event.getTargetEntity();
			Optional<TileEntity> tileEntity = frame.getWorld().getTileEntity(frame.getLocation().getBlockPosition().add(0, -1, 0));
			Entity en = (Entity) event.getTargetEntity();
			
			if (tileEntity.isPresent() && tileEntity.get() instanceof Chest)
			{
				Chest chest = (Chest) tileEntity.get();

				if (chest.get(IsSpongyChestData.class).isPresent() && chest.get(IsSpongyChestData.class).get().isSpongyChest().get())
				{
					purchase(event, chest, player);
				}
			}
		}
	}
	
	public boolean setLines(TileEntity entity, Text line0, Text line1, Text line2, Text line3) {
        SignData sign = entity.get(SignData.class).get();
        if (line0!=null) sign = sign.set(sign.getValue(Keys.SIGN_LINES).get().set(0, line0));
        if (line1!=null) sign = sign.set(sign.getValue(Keys.SIGN_LINES).get().set(1, line1));
        if (line2!=null) sign = sign.set(sign.getValue(Keys.SIGN_LINES).get().set(2, line2));
        if (line3!=null) sign = sign.set(sign.getValue(Keys.SIGN_LINES).get().set(3, line3));
        entity.offer(sign);
        return true;
    }
}
