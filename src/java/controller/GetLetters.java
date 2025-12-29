/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author mohan
 */
@WebServlet(name = "GetLetters", urlPatterns = {"/GetLetters"})
public class GetLetters extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mobile = request.getParameter("mobile");
        
        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        Criteria criteria = session.createCriteria(User.class);
        criteria.add(Restrictions.eq("mobile", mobile));
        
        if(!criteria.list().isEmpty()){
        //user found
            
        User user = (User) criteria.uniqueResult();
        String letters = user.getFirst_name().charAt(0) + "" + user.getLast_name().charAt(0);
        responseJson.addProperty("letters", letters);
            
        }else{
        //user not found
        responseJson.addProperty("letters", "");
        }
        
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson));
        session.close();
    }
    

    
}
