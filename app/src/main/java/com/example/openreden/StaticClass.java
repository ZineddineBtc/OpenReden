package com.example.openreden;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StaticClass {

    public static int PICK_SINGLE_IMAGE = 1;
    public static String SHARED_PREFERENCES = "shared_preferences";
    public static String USERNAME = "username";
    public static String NAME = "name";
    public static String EMAIL = "email";
    public static String BIO = "bio";
    public static String PROFILE_ID = "profile";
    public static String QUIZ_ID = "quiz_id";
    public static String mySimpleDateFormat = "dd-MM-yyyy";
    public static String TO = "to";
    public static String FROM = "from";
    public static String NETWORK_ADAPTER = "network_adapter";
    public static String BACK_TO_ID = "back_to_id";
    public static String TIMELINE = "timeline";
    public static String PROFILE_FRAGMENT = "profile_fragment";
    public static String profilePhoto = "-profilePhoto";
    public static String hasPhoto_0 = "has photo 0";
    public static String hasPhoto_1 = "has photo 1";
    public static String hasPhoto_2 = "has photo 2";

    public static boolean isValidEmail(String email) {
        if(email.length()>4){
            String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
            Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(email);
            return matcher.matches();
        }else{
            return false;
        }

    }
    public static boolean containsDigit(String s) {
        if(s.length()>2){
            boolean containsDigit = false;
            for (char c : s.toCharArray()) {
                if (containsDigit = Character.isDigit(c)) {
                    break;
                }
            }
            return containsDigit;
        }else{
            return false;
        }
    }
    public static String getCurrentTime(){
        return new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
    }
}
