/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import model.Validations;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 * SignIn Servlet for handling user authentication.
 */
@WebServlet(name = "SignIn", urlPatterns = {"/SignIn"})
public class SignIn extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", false); // Default to failure

    
        JsonObject requestJson = gson.fromJson(request.getReader(), JsonObject.class);
        String mobile = requestJson.get("mobile").getAsString();
        String password = requestJson.get("password").getAsString();

   
        if (mobile.isEmpty()) {
            responseJson.addProperty("message", "Please fill the mobile number");
        } else if (!Validations.isMobileNumberValid(mobile)) {
            responseJson.addProperty("message", "Invalid mobile number");
        } else if (password.isEmpty()) {
            responseJson.addProperty("message", "Please fill the password");
        } else if (!Validations.isPasswordValid(password)) {
            responseJson.addProperty("message", "Invalid password");
        } else {
         
            Session session = HibernateUtil.getSessionFactory().openSession();
            Criteria criteria = session.createCriteria(User.class);
            criteria.add(Restrictions.eq("mobile", mobile));
            criteria.add(Restrictions.eq("password", password));

            if (!criteria.list().isEmpty()) {
                
                // User found
                User user = (User) criteria.uniqueResult();
                responseJson.addProperty("success", true);
                responseJson.addProperty("message", "Sign In Success");

              
                JsonObject userJson = new JsonObject();
                userJson.addProperty("id", user.getId());
                userJson.addProperty("firstName", user.getFirst_name());
                userJson.addProperty("lastName", user.getLast_name());
                userJson.addProperty("mobile", user.getMobile());
                
               
                responseJson.add("user", userJson);  

                System.out.println(user.getFirst_name() + " " + user.getLast_name());
                
            } else {
                // User not found
                responseJson.addProperty("message", "Invalid Credentials!");
            }
            session.close();
        }

        // Send response back to the client
        response.setContentType("application/json");
        response.getWriter().write(responseJson.toString());
    }
}
