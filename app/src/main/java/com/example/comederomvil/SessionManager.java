package com.example.comederomvil;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Gson gson;
    private static final String PREF_NAME = "login_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String JWT = "JWT";


    public SessionManager(Context context) {
        // Inicializa las preferencias compartidas y el editor
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }


    public void createLoginSession(boolean isLoggedIn, String id_person, String name, String paternal_surname, String maternal_surname, String phone, String email, String rol, String token) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.putString("id_person", id_person);
        editor.putString("name", name);
        editor.putString("paternal_surname", paternal_surname);
        editor.putString("maternal_surname", maternal_surname);
        editor.putString("phone", phone);
        editor.putString("email", email);
        editor.putString("rol", rol);
        editor.putString("authToken", token);

        // Aplica los cambios
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getIdPerson() {
        return sharedPreferences.getInt("id_person", -1);
    }

    public String getName() {
        return sharedPreferences.getString("name", "");
    }

    public String getPaternalSurname() {
        return sharedPreferences.getString("paternal_surname", "");
    }

    public String getMaternalSurname() {
        return sharedPreferences.getString("maternal_surname", "");
    }

    public String getEmail() {
        return sharedPreferences.getString("email", null);
    }

    public String getAuthToken() {
        return sharedPreferences.getString("authToken", null);
    }

    public String getPhone() {
        return sharedPreferences.getString("phone", null);
    }

    public String getRol() {
        return sharedPreferences.getString("rol", null);
    }


    public void logout() {
        editor.clear();
        editor.apply();
    }
}
