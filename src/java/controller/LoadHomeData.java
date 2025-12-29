/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.Chat;
import entity.User;
import entity.User_Status;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author mohan
 */
@WebServlet(name = "LoadHomeData", urlPatterns = {"/LoadHomeData"})
public class LoadHomeData extends HttpServlet {

    @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        Gson gson = new Gson();
        Session session = null;

        try {
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("status", false);

            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction(); // Start transaction

            String userId = request.getParameter("id");
            System.out.println("User ID received: " + userId); // Debugging line

            User user = (User) session.get(User.class, Integer.parseInt(userId));
            System.out.println("User found: " + user.getFirst_name() + " " + user.getLast_name()); // Debugging line

            User_Status usrStatus = (User_Status) session.get(User_Status.class, 1);
            user.setUser_Status(usrStatus);
            session.update(user); // Update user status

            Criteria criteria = session.createCriteria(User.class);
            criteria.add(Restrictions.ne("id", user.getId()));
            List<User> otherUserList = criteria.list();

            JsonArray jsonArray = new JsonArray();
            for (User otherUser : otherUserList) {
                // Get chat list
                Criteria criteria2 = session.createCriteria(Chat.class);
                criteria2.add(
                        Restrictions.or(
                                Restrictions.and(
                                        Restrictions.eq("from_user", user),
                                        Restrictions.eq("to_user", otherUser)
                                ),
                                Restrictions.and(
                                        Restrictions.eq("from_user", otherUser),
                                        Restrictions.eq("to_user", user)
                                )
                        )
                );
                criteria2.addOrder(Order.desc("id"));
                criteria2.setMaxResults(1);

                // Create chat item JSON
                JsonObject jsonChatItem = new JsonObject();
                jsonChatItem.addProperty("other_user_id", otherUser.getId());
                jsonChatItem.addProperty("other_user_name", otherUser.getFirst_name() + " " + otherUser.getLast_name());
                jsonChatItem.addProperty("other_user_mobile", otherUser.getMobile());
                jsonChatItem.addProperty("other_user_status", otherUser.getUser_Status().getId());

                // Check avatar image
                String applicationPath = request.getServletContext().getRealPath("");
                String newApplicationPath = applicationPath.replace("//build//web", "web");

                String otherUserAvatarImagePath = newApplicationPath + File.separator + "Avatarimages" + File.separator + otherUser.getMobile() + ".png";
                File otherUserAvatarImageFile = new File(otherUserAvatarImagePath);

                if (otherUserAvatarImageFile.exists()) {
                    jsonChatItem.addProperty("avatar_image_found", true);
                } else {
                    jsonChatItem.addProperty("avatar_image_found", false);
                    jsonChatItem.addProperty("avatar_user_avatar_letters", otherUser.getFirst_name().charAt(0) + "" + otherUser.getLast_name().charAt(0));
                }

                // Get chat list
                List<Chat> dbChatList = criteria2.list();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy, MMM dd hh:mm a");

                if (dbChatList.isEmpty()) {
                    jsonChatItem.addProperty("message", "Say hi to your new friend!");
                    jsonChatItem.addProperty("dateTime", dateFormat.format(user.getRegistered_date_time()));
                    jsonChatItem.addProperty("chat_status_id", 1);
                } else {
                    jsonChatItem.addProperty("message", dbChatList.get(0).getMessage());
                    jsonChatItem.addProperty("dateTime", dateFormat.format(dbChatList.get(0).getDate_time()));
                    jsonChatItem.addProperty("chat_status_id", dbChatList.get(0).getChat_Status().getId());
                }

                jsonArray.add(jsonChatItem);
            }

            responseJson.addProperty("status", true);
            responseJson.add("jsonArray", gson.toJsonTree(jsonArray));

            response.setContentType("application/json");
            response.getWriter().write(gson.toJson(responseJson));

            session.getTransaction().commit(); // Commit transaction
        } catch (Exception e) {
            if (session != null) {
                session.getTransaction().rollback(); // Rollback on error
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close(); // Ensure session closure
            }
        }
    }
}
