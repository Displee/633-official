package com.rs.game.player;

import com.rs.game.item.FloorItem;
import com.rs.game.item.Item;
import com.rs.game.item.ItemWeights;
import com.rs.game.item.ItemsContainer;
import com.rs.game.map.WorldTile;
import com.rs.utilities.ItemExamines;
import com.rs.utilities.Utility;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class Inventory {

	private ItemsContainer<Item> items;

	private transient Player player;
	
	private transient double inventoryWeight;

	public static final int INVENTORY_INTERFACE = 149;

	public Inventory() {
		items = new ItemsContainer<Item>(28, false);
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void init() {
		player.getPackets().sendItems(93, items);
	}

	public void unlockInventoryOptions() {
		player.getPackets().sendIComponentSettings(INVENTORY_INTERFACE, 0, 0,
				27, 4554126);
		player.getPackets().sendIComponentSettings(INVENTORY_INTERFACE, 0, 28,
				55, 2097152);
	}

	public void reset() {
		items.reset();
		init(); // as all slots reseted better just send all again
	}

	public void refresh(int... slots) {
		player.getPackets().sendUpdateItems(93, items, slots);
		refreshConfigs(false);
	}

	public boolean addItemDrop(int itemId, int amount, WorldTile tile) {
		if (itemId < 0
				|| amount < 0
				|| !Utility.itemExists(itemId)
				|| player.getMapZoneManager().execute(player, controller -> !controller.canAddInventoryItem(player, itemId, amount)))
			return false;
		Item[] itemsBefore = items.getItemsCopy();
		if (!items.add(new Item(itemId, amount)))
			FloorItem.addGroundItem(new Item(itemId, amount), tile, player, true, 180);
		else
			refreshItems(itemsBefore);
		return true;
	}

	public boolean addItemDrop(int itemId, int amount) {
		return addItemDrop(itemId, amount, new WorldTile(player));
	}

	public boolean addItem(int itemId, int amount) {
		if (itemId < 0
				|| amount < 0
				|| !Utility.itemExists(itemId)
				|| player.getMapZoneManager().execute(player, controller -> !controller.canAddInventoryItem(player, itemId, amount)))
			return false;
		Item[] itemsBefore = items.getItemsCopy();
		if (!items.add(new Item(itemId, amount))) {
			items.add(new Item(itemId, items.getFreeSlots()));
			player.getPackets().sendGameMessage(
					"Not enough space in your inventory.");
			refreshItems(itemsBefore);
			return false;
		}
		refreshItems(itemsBefore);
		return true;
	}
	
	public boolean addItem(Item item) {
		if (item.getId() < 0
				|| item.getAmount() < 0
				|| !Utility.itemExists(item.getId())
				|| player.getMapZoneManager().execute(player, controller -> !controller.canAddInventoryItem(player, item.getId(), item.getAmount())))
			return false;
		Item[] itemsBefore = items.getItemsCopy();
		if (!items.add(item)) {
			items.add(new Item(item.getId(), items.getFreeSlots()));
			player.getPackets().sendGameMessage(
					"Not enough space in your inventory.");
			refreshItems(itemsBefore);
			return false;
		}
		refreshItems(itemsBefore);
		return true;
	}

	public void deleteItem(int slot, Item item) {
		if (player.getMapZoneManager().execute(player, controller -> !controller.canDeleteInventoryItem(player, item.getId(),
				item.getAmount()))) {
			return;
		}
		Item[] itemsBefore = items.getItemsCopy();
		items.remove(slot, item);
		refreshItems(itemsBefore);
	}

	public boolean removeItems(Item... list) {
		for (Item item : list) {
			if (item == null)
				continue;
			deleteItem(item);
		}
		return true;
	}

	public boolean removeItems(ObjectArrayList<Item> list) {
		for (Item item : list) {
			if (item == null)
				continue;
			deleteItem(item);
		}
		return true;
	}

	public void deleteItem(int itemId, int amount) {
		if (player.getMapZoneManager().execute(player, controller -> !controller.canDeleteInventoryItem(player, itemId, amount))) {
			return;
		}
		Item[] itemsBefore = items.getItemsCopy();
		items.remove(new Item(itemId, amount));
		refreshItems(itemsBefore);
	}

	public void deleteItem(Item item) {
		if (player.getMapZoneManager().execute(player, controller -> !controller.canDeleteInventoryItem(player, item.getId(),
				item.getAmount()))) {
			return;
		}
		Item[] itemsBefore = items.getItemsCopy();
		items.remove(item);
		refreshItems(itemsBefore);
	}

	/*
	 * No refresh needed its client to who does it :p
	 */
	public void switchItem(int fromSlot, int toSlot) {
		Item[] itemsBefore = items.getItemsCopy();
		Item fromItem = items.get(fromSlot);
		Item toItem = items.get(toSlot);
		items.set(fromSlot, toItem);
		items.set(toSlot, fromItem);
		refreshItems(itemsBefore);
	}

	public void refreshItems(Item[] itemsBefore) {
		int[] changedSlots = new int[itemsBefore.length];
		int count = 0;
		for (int index = 0; index < itemsBefore.length; index++) {
			if (itemsBefore[index] != items.getItems()[index])
				changedSlots[count++] = index;
		}
		int[] finalChangedSlots = new int[count];
		System.arraycopy(changedSlots, 0, finalChangedSlots, 0, count);
		refresh(finalChangedSlots);
	}

	public ItemsContainer<Item> getItems() {
		return items;
	}

	public boolean hasFreeSlots() {
		return items.getFreeSlot() != -1;
	}

	public int getFreeSlots() {
		return items.getFreeSlots();
	}

	public int getAmountOf(int itemId) {
		return items.getNumberOf(itemId);
	}

	public Item getItem(int slot) {
		return items.get(slot);
	}

	public int getItemsContainerSize() {
		return items.getSize();
	}

	public boolean containsListItems(ObjectArrayList<Item> list) {
		for (Item item : list)
			if (!items.contains(item))
				return false;
		return true;
	}

	public boolean containsItems(Item[] item) {
		for (int i = 0; i < item.length; i++)
			if (!items.contains(item[i]))
				return false;
		return true;
	}

	public boolean containsItems(int[] itemIds, int[] ammounts) {
		int size = itemIds.length > ammounts.length ? ammounts.length
				: itemIds.length;
		for (int i = 0; i < size; i++)
			if (!items.contains(new Item(itemIds[i], ammounts[i])))
				return false;
		return true;
	}

	public boolean containsItem(int itemId, int ammount) {
		return items.contains(new Item(itemId, ammount));
	}
	
	public boolean containsItem(Item item) {
		return items.contains(item);
	}

	public int getCoinsAmount() {
		int coins = items.getNumberOf(995);
		return coins < 0 ? Integer.MAX_VALUE : coins;
	}

	public boolean containsOneItem(int... itemIds) {
		for (int itemId : itemIds) {
			if (items.containsOne(new Item(itemId, 1)))
				return true;
		}
		return false;
	}

	public void sendExamine(int slotId) {
		if (slotId >= getItemsContainerSize())
			return;
		Item item = items.get(slotId);
		if (item == null)
			return;
		player.getPackets().sendGameMessage(ItemExamines.getExamine(item));
	}

	public void refresh() {
		player.getPackets().sendItems(93, items);
		refreshConfigs(true);
	}

	public void replaceItem(int id, int amount, int slot) {
		Item item = items.get(slot);
		if (item == null)
			return;
		item.setId(id);
		item.setAmount(amount);
		refresh(slot);
	}
	
	public boolean addItems(Item... list) {
		for (Item item : list) {
			if (item == null)
				continue;
			addItem(item);
		}
		return true;
	}

	public boolean canRemove(int id, int amount) {
		if (getAmountOf(id) >= amount) {
			deleteItem(id, amount);
			return true;
		}
		return getAmountOf(id) >= amount;
	}
	
	public void refreshConfigs(boolean init) {
		double w = 0;
		for (Item item : items.getItems()) {
			if (item == null)
				continue;
			w += ItemWeights.getWeight(item, false);
		}
		inventoryWeight = w;
//		player.getPackets().refreshWeight();
	}

	public double getInventoryWeight() {
		return inventoryWeight;
	}

	/**
	 * Determines if this container contains any {@code identifiers}.
	 * @param identifiers The identifiers to check this container for.
	 * @return {@code true} if this container has any {@code identifiers}, {@code false} otherwise.
	 */
	public final boolean containsAny(int... identifiers) {
		for(int id : identifiers) {
			for(Item item : items.getItems()) {
				if(item == null)
					continue;
				if(item.getId() == id)
					return true;
			}
		}
		return false;
	}

    public int getNumberOf(int itemId) {
        return items.getNumberOf(itemId);
    }

}