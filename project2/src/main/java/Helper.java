package main.java;

import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class Helper {
    /*
    * Collections of static helper functions that can be used in multiple java classes
    */
    public static void getUserName(JsonObject resultData, HttpServletRequest request){
        Customer current_user = (Customer) request.getSession().getAttribute("user");
        if (current_user != null)
            resultData.addProperty("userName", current_user.getName());
    }

    public static void getUserName(JsonObject resultData, HttpSession session){
        Customer current_user = (Customer) session.getAttribute("user");
        if (current_user != null)
            resultData.addProperty("userName", current_user.getName());
    }

    public static Cart getCart(HttpSession session) {
        Cart cart = (Cart)session.getAttribute("cart");
        if (cart == null){
            cart = new Cart();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    public static void writeCartToData(JsonObject resultData, Cart cart, HttpSession session){
        resultData.addProperty("cart_size", cart.size());
        resultData.addProperty("cart_total", cart.getTotal());
        resultData.add("cart_array", cart.toJsonArray());
        Helper.getUserName(resultData, session);
    }

    public static void writeCartSize(JsonObject resultData, HttpSession session){
        // only write cart size. for displaying cart size at main/single pages
        Cart cart = getCart(session);
        resultData.addProperty("cart_size", cart.size());
    }
}
