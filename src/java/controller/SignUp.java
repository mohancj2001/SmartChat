package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import entity.User_Status;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import model.HibernateUtil;
import model.Validations;
import org.hibernate.Session;

@WebServlet(name = "SignUp", urlPatterns = {"/SignUp"})
@MultipartConfig
public class SignUp extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

// Prepare JSON response
        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", false);

// Extract form data
        String mobile = request.getParameter("mobile");
        String fname = request.getParameter("firstName");
        String lname = request.getParameter("lastName");
        String password = request.getParameter("password");
        Part image = request.getPart("image");  // Image part

        // Log form data for debugging
//        System.out.println("Mobile: " + mobile);
//        System.out.println("First Name: " + fname);
//        System.out.println("Last Name: " + lname);
//        System.out.println("Password: " + password);
        if (mobile.isEmpty()) {

            //mobile number is blank
            responseJson.addProperty("message", "Please Fill Mobile Number");

        } else if (!Validations.isMobileNumberValid(mobile)) {

            //invalid mobile
            responseJson.addProperty("message", "Invalid Mobile Number");
        }else if(fname.isEmpty()){
            responseJson.addProperty("message", "Please Fill First Name");
        }else if(lname.isEmpty()){
            responseJson.addProperty("message", "Please Fill Last Name");
        }else if(password.isEmpty()){
            responseJson.addProperty("message", "Please Fill Password");
        }else if(!Validations.isPasswordValid(password)){
                    responseJson.addProperty("message", "Invalid Password");
        }else{
            Session session = HibernateUtil.getSessionFactory().openSession();
            
            User user = new User();
            user.setFirst_name(fname);
            user.setLast_name(lname);
            user.setMobile(mobile);
            user.setPassword(password);
            user.setRegistered_date_time(new Date());
            
            User_Status user_Status = (User_Status) session.get(User_Status.class,2);
            user.setUser_Status(user_Status);
            
            session.save(user);
            session.beginTransaction().commit();
            
         // Handle image upload
        if (image != null && image.getSize() > 0) {
            String serverPath = request.getServletContext().getRealPath("");
            String imageDirPath = serverPath + File.separator + "Avatarimages";

            // Ensure the directory exists
            File imageDir = new File(imageDirPath);
            if (!imageDir.exists()) {
                imageDir.mkdirs();  // Create the directory if it doesn't exist
            }

            // Construct the image file path
            String imagePath = imageDirPath + File.separator + mobile + ".png";
            File file = new File(imagePath);

            // Save the image file
            Files.copy(image.getInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
//            System.out.println("Image saved at: " + imagePath);
        }
        
        responseJson.addProperty("success", true);
        responseJson.addProperty("success", "Registration Complete");
        session.close();
        }
        

       

        // Prepare JSON response
//        Gson gson = new Gson();
//        JsonObject responseJson = new JsonObject();
//        responseJson.addProperty("message", "Server: Hello!");

        // Send the response
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson));
    }
}
