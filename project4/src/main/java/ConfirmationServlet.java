package main.java;

import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name="ConfirmationServlet", urlPatterns = "/api/confirmation")
public class ConfirmationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        JsonObject resultData = new JsonObject();
        HttpSession session = req.getSession();

        Cart cart = Helper.getCart(session);

        // tell js to redirect to main-page if empty or unpaid cart
        resultData.addProperty("isPaid", cart.isPaid());

        Helper.writeCartToData(resultData, cart, session);
        resp.getWriter().write(resultData.toString());

        // clear cart
        if (cart.isPaid())
            session.setAttribute("cart", new Cart());
    }
}
