package model;

public class Validations {


    public static boolean isPasswordValid(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).{8,}$");
    }

  
    public static boolean  isMobileNumberValid(String mobile){
    return mobile.matches("^07[012345678]{1}[0-9]{7}$");
    }

}
