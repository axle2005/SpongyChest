package io.github.hsyyid.spongychest.data.pricechest;

import io.github.hsyyid.spongychest.SpongyChest;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;

import java.util.Optional;

public class PriceChestDataBuilder implements DataManipulatorBuilder<PriceChestData, ImmutablePriceChestData>
{
	@Override
	public PriceChestData create()
	{
		return new SpongePriceChestData();
	}

	@Override
	public Optional<PriceChestData> createFrom(DataHolder dataHolder)
	{
		return create().fill(dataHolder);
	}

	@Override
	public Optional<PriceChestData> build(DataView container)
	{
		if (!container.contains(SpongyChest.PRICE_CHEST.getQuery()))
		{
			return Optional.empty();
		}

		PriceChestData data = new SpongePriceChestData((Double) container.get(SpongyChest.PRICE_CHEST.getQuery()).get());
		return Optional.of(data);
	}
}
