package com.example.sharingapp;

import com.google.gson.Gson;
import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;

public class ItemRegressionTest {
    private Item example() {
        return new Item("Demo item", "Demo maker", "Synthetic fixture", new Dimensions("1", "2", "3"), null, "demo-id");
    }

    @Test public void newWrapperPreservesItemsSharedByActivities() {
        ItemList first = new ItemList();
        first.setItems(new ArrayList<Item>());
        first.addItem(example());
        ItemList second = new ItemList();
        assertEquals(1, second.getSize());
        assertEquals("demo-id", second.getItem(0).getId());
    }

    @Test public void setItemsCopiesCallerListAndAcceptsNull() {
        ItemList list = new ItemList();
        ArrayList<Item> caller = new ArrayList<Item>();
        caller.add(example()); list.setItems(caller); caller.clear();
        assertEquals(1, list.getSize());
        list.setItems(null);
        assertEquals(0, list.getSize());
    }

    @Test public void gsonRoundTripPreservesBorrowerAndItemIdentity() {
        Item item = example();
        item.setStatus("Borrowed");
        item.setBorrower(new Contact("Demo", "demo@example.test"));
        Gson gson = new Gson();
        Item loaded = gson.fromJson(gson.toJson(item), Item.class);
        assertEquals(item.getId(), loaded.getId());
        assertEquals("Borrowed", loaded.getStatus());
        assertEquals("Demo", loaded.getBorrower().getUsername());
        assertEquals("1 x 2 x 3", loaded.getDimensions().getDimensions());
    }
}
