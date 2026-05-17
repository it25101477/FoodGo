package com.foodgo.util;

import com.foodgo.model.Rider;
import com.foodgo.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class UserProfile {

    private UserProfile() {}

    public static Map<String, Object> fromUser(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        map.put("phone", user.getPhone());
        return map;
    }

    public static Map<String, Object> fromRider(Rider rider) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", rider.getId());
        map.put("name", rider.getName());
        map.put("email", rider.getEmail());
        map.put("phone", rider.getPhone());
        return map;
    }

    public static List<Map<String, Object>> fromUsers(List<User> users) {
        return users.stream().map(UserProfile::fromUser).collect(Collectors.toList());
    }

    public static List<Map<String, Object>> fromRiders(List<Rider> riders) {
        return riders.stream().map(UserProfile::fromRider).collect(Collectors.toList());
    }
}
