package com.foodgo.service;

import com.foodgo.model.User;
import com.foodgo.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.util.*;

@Service
public class UserService {

    private static final String FILE_NAME = "users.txt";

    @Autowired
    private FileStorageService fileStorage;

    @Autowired
    private PasswordUtil passwordUtil;

    private String filePath() {
        return fileStorage.resolve(FILE_NAME);
    }

    @PostConstruct
    public void init() throws IOException {
        File file = new File(filePath());
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
    }

    public String registerUser(User user) throws IOException {
        if (findByEmail(user.getEmail()) != null) return "Email already registered!";
        user.setId(String.valueOf(System.currentTimeMillis()));
        user.setPassword(passwordUtil.hash(user.getPassword()));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath(), true))) {
            writer.write(user.toFileLine());
            writer.newLine();
        }
        return "success";
    }

    public List<User> getAllUsers() throws IOException {
        List<User> users = new ArrayList<>();
        File file = new File(filePath());
        if (!file.exists()) return users;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null)
                if (!line.trim().isEmpty()) users.add(User.fromFileLine(line));
        }
        return users;
    }

    public User findByEmail(String email) throws IOException {
        for (User user : getAllUsers())
            if (user.getEmail().equalsIgnoreCase(email)) return user;
        return null;
    }

    public User getUserById(String id) throws IOException {
        for (User user : getAllUsers())
            if (user.getId().equals(id)) return user;
        return null;
    }

    public boolean updateUserByEmail(String email, String newName, String newPhone) throws IOException {
        List<User> users = getAllUsers();
        boolean found = false;
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                u.setName(newName);
                u.setPhone(newPhone);
                found = true;
                break;
            }
        }
        if (found) saveAll(users);
        return found;
    }

    public boolean updateUser(String id, String newName, String newPhone) throws IOException {
        List<User> users = getAllUsers();
        boolean found = false;
        for (User u : users) {
            if (u.getId().equals(id)) {
                u.setName(newName);
                u.setPhone(newPhone);
                found = true;
                break;
            }
        }
        if (found) saveAll(users);
        return found;
    }

    public boolean deleteUserByEmail(String email) throws IOException {
        List<User> users = getAllUsers();
        boolean removed = users.removeIf(u -> u.getEmail().equalsIgnoreCase(email));
        if (removed) saveAll(users);
        return removed;
    }

    public boolean deleteUser(String id) throws IOException {
        List<User> users = getAllUsers();
        boolean removed = users.removeIf(u -> u.getId().equals(id));
        if (removed) saveAll(users);
        return removed;
    }

    public User loginUser(String email, String password) throws IOException {
        User user = findByEmail(email);
        if (user != null && passwordUtil.matches(password, user.getPassword())) return user;
        return null;
    }

    public String updatePassword(String email, String currentPassword, String newPassword) throws IOException {
        List<User> users = getAllUsers();
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                if (!passwordUtil.matches(currentPassword, u.getPassword())) return "wrong_password";
                u.setPassword(passwordUtil.hash(newPassword));
                saveAll(users);
                return "updated";
            }
        }
        return "not_found";
    }


    public String updateEmail(String id, String newEmail) throws IOException {
        // Check new email not already taken by another user
        User existing = findByEmail(newEmail);
        if (existing != null && !existing.getId().equals(id)) {
            return "email_taken";
        }
        List<User> users = getAllUsers();
        for (User u : users) {
            if (u.getId().equals(id)) {
                u.setEmail(newEmail.trim().toLowerCase());
                saveAll(users);
                return "updated";
            }
        }
        return "not_found";
    }

    private void saveAll(List<User> users) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath(), false))) {
            for (User u : users) {
                writer.write(u.toFileLine());
                writer.newLine();
            }
        }
    }
}
