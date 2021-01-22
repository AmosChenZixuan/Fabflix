package main.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class Cart {
    private ArrayList<CartItem> items;
    private int size;
    private double total;

    Cart(){
        items = new ArrayList<CartItem>();
        size = 0;
        total = 0;
    }

    public boolean isPaid(){
        return size > 0 && items.get(0).saleId > 0;
    }

    public ArrayList<CartItem> getItems() {
        return items;
    }

    public JsonArray toJsonArray(){
        JsonArray itemsArray = new JsonArray();
        for (CartItem c: items){
            itemsArray.add(c.toJsonObject());
        }
        return itemsArray;
    }

    public void add(String id, String title){
        size += 1;
        for (CartItem c: items){
            if (c.id.equals(id)) {
                c.setQuantity(c.getQuantity() + 1);
                total += c.price;
                return;
            }
        }
        CartItem c = new CartItem(id, title);
        total += c.price;
        items.add(c);
    }

    public void remove(int index){
        CartItem c = items.get(index);
        size -= c.getQuantity();
        total -= c.getQuantity() * c.price;
        items.remove(index);
    }

    public void update(int index, int quantity){
        if (quantity > 0){
            CartItem c = items.get(index);
            size += quantity - c.getQuantity();
            total += (quantity - c.getQuantity()) * c.price;
            c.setQuantity(quantity);
        }
        else
            this.remove(index);
    }

    public int size(){
        return size;
    }

    public double getTotal(){
        return total;
    }


    class CartItem{
        private String id, title;
        private int quantity, saleId;
        private double price;
        CartItem(String id, String title){
            this.id = id;
            this.title = title;
            this.quantity = 1;
            this.price = title.length();
            this.saleId = -1; // -1 for unsold
        }

        public JsonObject toJsonObject(){
            JsonObject item = new JsonObject();
            item.addProperty("sale_id", saleId);
            item.addProperty("movie_id", id);
            item.addProperty("movie_title", title);
            item.addProperty("movie_price", price);
            item.addProperty("quantity", quantity);
            return item;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public void setSaleId(int saleId) {
            this.saleId = saleId;
        }

        public int getSaleId() {
            return saleId;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}
