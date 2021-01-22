<%@page import="java.sql.*" %>


<%-- these statements are just normal Java code, they need to be inside the <% %> brackets--%>
<%
    // setup the connection and execute the query
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedbexample?useSSL=false",
            "mytestuser", "mypassword");
    Statement select = connection.createStatement();
    ResultSet result = select.executeQuery("Select * from stars limit 20");
    ResultSetMetaData metadata = result.getMetaData();
%>

<%-- the following are HTML mixed with java code,
     you can see for loops are used to generate a dynamic table.
     normal Java code still needs to be in the <% %> tag

     <%= %> is the expression tag. the java code inside needs to be a value
     and that value will be directly write to the html, it's equivalent to out.print()
--%>

<h3> Star Results </h3>
<table border=1>
    <%-- generate table header: name and type of each row --%>
    <tr>
        <%-- iterate through metadata, column count starts at 1 --%>
        <% for (int i = 1; i <= metadata.getColumnCount(); i++) { %>
        <th><%= metadata.getColumnName(i) + ": " + metadata.getColumnTypeName(i) %>
        </th>
        <% } %>
    </tr>
    <%-- generate table content: for each row in result set, display a html table row --%>
    <% while (result.next()) { %>
    <tr>
        <%-- generate table row: iterate through the values for this column --%>
        <% for (int i = 1; i <= metadata.getColumnCount(); i++) { %>
        <th><%= result.getString(i) %>
        </th>
        <% } %>
    </tr>
    <% } %>
</table>
