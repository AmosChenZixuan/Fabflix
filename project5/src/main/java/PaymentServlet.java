package main.java;

import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet(name="PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource_local;

    @Resource(name = "jdbc/master")
    private DataSource dataSource_master;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        JsonObject resultData = new JsonObject();
        HttpSession session = req.getSession();

        //double total = Helper.getCart(session).getTotal();
        //resultData.addProperty("cart_total", total);
        Cart cart = Helper.getCart(session);
        Helper.writeCartToData(resultData, cart, session);
        JsonObject payment = (JsonObject) session.getAttribute("payment");
        if (payment == null){
            payment = new JsonObject();
            session.setAttribute("payment", payment);
        }
        resultData.add("last_result", payment);
        session.setAttribute("payment", new JsonObject()); // clear last payment status
        Helper.getUserInfo(resultData, session);

        resp.getWriter().write(resultData.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JsonObject resultData = new JsonObject();

        String fName = req.getParameter("firstName"),
                lName = req.getParameter("lastName"),
                cNum = req.getParameter("cardNum"),
                expDate = req.getParameter("expDate");
                //userCid = ((Customer)req.getSession().getAttribute("user")).ccid;
        try{
            Connection connection_local = dataSource_local.getConnection();
            Connection connection_master = dataSource_master.getConnection();
            HttpSession session = req.getSession();

            //int status = (cNum.equals(userCid))? checkCardInfo(connection, fName, lName, cNum, expDate) : 0;
            int status = checkCardInfo(connection_local, fName, lName, cNum, expDate);
            resultData.addProperty("status", status);
            JsonObject payment = (JsonObject) session.getAttribute("payment");
            payment.addProperty("status", status);
            if (status == 0) {
                payment.addProperty("message", "Incorrect Card Information");
            }
            else {
                // record to db
                synchronized (connection_master) {
                    int saleId = getLastSaleId(connection_local);
                    recordPayment(connection_master, session, saleId);
                }
            }
            //Helper.getUserName(resultData, req);
            connection_local.close();
            connection_master.close();
        }
        catch(Exception e){
            resultData.addProperty("status", -1);
            resultData.addProperty("message", e.getMessage());
            System.out.println(e.getMessage());
        }
        out.write(resultData.toString());
        out.close();
    }

    private void recordPayment(Connection connection, HttpSession session, int saleId) throws SQLException {
        Customer user = (Customer) session.getAttribute("user");
        Cart cart = Helper.getCart(session);
        String userId = user.id,
                date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        connection.setAutoCommit(false);
        String query = String.format("insert into  sales(customerId, movieId, saleDate, quantity) value(%s, ?, '%s', ?)", userId, date);
        PreparedStatement ps = connection.prepareStatement(query);
        for (Cart.CartItem item: cart.getItems()){
            ps.setString(1, item.getId());
            ps.setInt(2, item.getQuantity());
            ps.addBatch();
            //
            item.setSaleId(++saleId);
        }
        ps.executeBatch();
        connection.commit();

        ps.close();
    }

    private int getLastSaleId(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();

        ResultSet rs = statement.executeQuery(SqlQuery.getMaxSaleId);
        int id = 0;
        if (rs.next())
            id = rs.getInt("lastId");
        rs.close();
        statement.close();
        return id;
    }

    private int checkCardInfo(Connection connection, String fName, String lName, String cNum, String expDate)
            throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SqlQuery.checkCardInfo);
        statement.setString(1, cNum);
        statement.setString(2, fName);
        statement.setString(3, lName);
        statement.setString(4, expDate);

        ResultSet rs = statement.executeQuery();
        int status = 0;
        if (rs.next())
            status = 1;
        rs.close();
        statement.close();
        return status;
    }
}
