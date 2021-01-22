package main.java;

import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name="ShoppingCartServlet", urlPatterns = "/api/shopping-cart")
public class ShoppingCartServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        JsonObject resultData = new JsonObject();
        HttpSession session = req.getSession();

        Cart cart = Helper.getCart(session);

        Helper.writeCartToData(resultData, cart, session);
        resp.getWriter().write(resultData.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //resp.setContentType("application/json");
        JsonObject resultData = new JsonObject();
        HttpSession session = req.getSession();

        //System.out.println("Action: "+req.getParameter("action"));
        Cart cart = Helper.getCart(session);
        String action = req.getParameter("action");
        switch(action){
            case "add":
                doAdd(cart, req, session);break;
            case "update":
                doUpdate(cart, req, session);break;
            case "delete":
                doDelete(cart, req, session);break;
        }

        //Helper.writeCartToData(resultData, cart, session);
        //resp.getWriter().write(resultData.toString());
    }

    private void doAdd(Cart cart, HttpServletRequest req, HttpSession session){
        String movie_id = req.getParameter("movie_id"),
                movie_title = req.getParameter("movie_title");

        if (cart == null){
            cart = new Cart();
            cart.add(movie_id, movie_title);
            session.setAttribute("cart", cart);
        }
        else{
            synchronized (cart) {
                cart.add(movie_id, movie_title);
            }
        }
    }

    private void doUpdate(Cart cart, HttpServletRequest req, HttpSession session){
        int index = Integer.parseInt(req.getParameter("index")),
            quantity = Integer.parseInt(req.getParameter("quantity"));
        synchronized (cart){
            cart.update(index, quantity);
        }
    }

    private void doDelete(Cart cart, HttpServletRequest req, HttpSession session){
        int index = Integer.parseInt(req.getParameter("index"));
        synchronized (cart){
            cart.remove(index);
        }
    }
}
