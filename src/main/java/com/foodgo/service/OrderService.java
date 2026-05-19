package com.foodgo.service;

import com.foodgo.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.util.*;

/**
 * OrderService — manages order lifecycle and writes COD payment records.
 * OOP: Encapsulation, Single Responsibility
 */
@Service
public class OrderService {

    private static final String FILE_NAME     = "orders.txt";
    private static final String PAYMENTS_FILE = "payments.txt";

    @Autowired
    private FileStorageService fileStorage;

    private String filePath()        { return fileStorage.resolve(FILE_NAME); }
    private String paymentsPath()    { return fileStorage.resolve(PAYMENTS_FILE); }

    @PostConstruct
    public void init() throws IOException {
        File f1 = new File(filePath());
        if (!f1.exists()) { f1.getParentFile().mkdirs(); f1.createNewFile(); }
        File f2 = new File(paymentsPath());
        if (!f2.exists()) { f2.getParentFile().mkdirs(); f2.createNewFile(); }
    }

    // ── Create order and write payment record ─────────────────────────────────
    public String createOrder(Order order) throws IOException {
        if (order.getId() == null || order.getId().isEmpty())
            order.setId(String.valueOf(System.currentTimeMillis()));
        if (order.getStatus() == null || order.getStatus().isEmpty())
            order.setStatus("pending");
        long now = System.currentTimeMillis();
        if (order.getCreatedAt() == 0) order.setCreatedAt(now);
        if (order.getUpdatedAt() == 0) order.setUpdatedAt(now);

        // Write order
        try (BufferedWriter w = new BufferedWriter(new FileWriter(filePath(), true))) {
            w.write(order.toFileLine()); w.newLine();
        }

        // Write payment record immediately (COD or card)
        writePaymentRecord(order);

        return order.getId();
    }

    /** Write one payment record line: paymentId||orderId||userId||userName||amount||method||status||createdAt */
    private void writePaymentRecord(Order order) {
        try (BufferedWriter pw = new BufferedWriter(new FileWriter(paymentsPath(), true))) {
            String pid    = "PAY" + order.getId();
            String method = (order.getPaymentMethod() != null ? order.getPaymentMethod() : "cod").toLowerCase();
            // COD starts as pending; card treated as paid
            String status = method.equals("card") ? "paid" : "pending";
            String line   = pid + "||" + order.getId() + "||"
                          + (order.getUserId()   != null ? order.getUserId()   : "") + "||"
                          + (order.getUserName() != null ? order.getUserName() : "Customer") + "||"
                          + order.getTotal()     + "||"
                          + method + "||"
                          + status + "||"
                          + order.getCreatedAt();
            pw.write(line); pw.newLine();
        } catch (IOException e) {
            System.err.println("[OrderService] Could not write payment record: " + e.getMessage());
        }
    }

    /** Update payment status when order is delivered / cancelled */
    public void syncPaymentStatus(String orderId, String orderStatus) {
        try {
            List<String> lines = new ArrayList<>();
            File file = new File(paymentsPath());
            if (!file.exists()) return;
            try (BufferedReader r = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = r.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split("\\|\\|", -1);
                    // parts[1] = orderId
                    if (parts.length > 6 && parts[1].equals(orderId)) {
                        String method = parts.length > 5 ? parts[5] : "cod";
                        String newStatus;
                        if ("delivered".equalsIgnoreCase(orderStatus)) {
                            newStatus = "paid";
                        } else if ("cancelled".equalsIgnoreCase(orderStatus)) {
                            newStatus = "failed";
                        } else {
                            newStatus = parts[6]; // keep existing
                        }
                        parts[6] = newStatus;
                        line = String.join("||", parts);
                    }
                    lines.add(line);
                }
            }
            try (BufferedWriter w = new BufferedWriter(new FileWriter(file, false))) {
                for (String l : lines) { w.write(l); w.newLine(); }
            }
        } catch (IOException e) {
            System.err.println("[OrderService] syncPaymentStatus error: " + e.getMessage());
        }
    }

    /** Read all payment records */
    public List<Map<String,String>> getAllPayments() throws IOException {
        List<Map<String,String>> list = new ArrayList<>();
        File file = new File(paymentsPath());
        if (!file.exists()) return list;
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split("\\|\\|", -1);
                Map<String,String> m = new LinkedHashMap<>();
                m.put("id",           p.length>0 ? p[0] : "");
                m.put("orderId",      p.length>1 ? p[1] : "");
                m.put("userId",       p.length>2 ? p[2] : "");
                m.put("customerName", p.length>3 ? p[3] : "Customer");
                m.put("amount",       p.length>4 ? p[4] : "0");
                m.put("method",       p.length>5 ? p[5] : "cod");
                m.put("status",       p.length>6 ? p[6] : "pending");
                m.put("createdAt",    p.length>7 ? p[7] : "0");
                list.add(m);
            }
        }
        list.sort((a,b) -> Long.compare(
            parseLong(b.get("createdAt")), parseLong(a.get("createdAt"))));
        return list;
    }

    private long parseLong(String s) { try { return Long.parseLong(s==null?"0":s.trim()); } catch(Exception e){ return 0; } }

    // ── Orders CRUD ───────────────────────────────────────────────────────────
    public List<Order> getAllOrders() throws IOException {
        List<Order> orders = new ArrayList<>();
        File file = new File(filePath());
        if (!file.exists()) return orders;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null)
                if (!line.trim().isEmpty()) orders.add(Order.fromFileLine(line));
        }
        return orders;
    }

    public Order getOrderById(String id) throws IOException {
        for (Order o : getAllOrders()) if (o.getId().equals(id)) return o;
        return null;
    }

    public List<Order> getOrdersByUserId(String userId) throws IOException {
        List<Order> orders = new ArrayList<>();
        for (Order o : getAllOrders()) if (o.getUserId().equals(userId)) orders.add(o);
        orders.sort((a,b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
        return orders;
    }

    public List<Order> getOrdersByRiderId(String riderId) throws IOException {
        List<Order> orders = new ArrayList<>();
        for (Order o : getAllOrders()) if (riderId.equals(o.getAssignedRiderId())) orders.add(o);
        orders.sort((a,b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
        return orders;
    }

    public List<Order> getAvailableOrders() throws IOException {
        List<Order> orders = new ArrayList<>();
        for (Order o : getAllOrders()) {
            String status = o.getStatus() != null ? o.getStatus().toLowerCase() : "";
            String rider  = o.getAssignedRiderId();
            if ("pending".equals(status) && (rider == null || rider.isEmpty()))
                orders.add(o);
        }
        return orders;
    }

    public List<Order> getOrdersByStatus(String status) throws IOException {
        List<Order> orders = new ArrayList<>();
        for (Order o : getAllOrders())
            if (o.getStatus().equalsIgnoreCase(status)) orders.add(o);
        return orders;
    }

    public boolean updateOrderStatus(String id, String newStatus) throws IOException {
        List<Order> orders = getAllOrders();
        boolean found = false;
        for (Order o : orders) {
            if (o.getId().equals(id)) { o.setStatus(newStatus); found = true; break; }
        }
        if (found) {
            saveAllOrders(orders);
            syncPaymentStatus(id, newStatus); // keep payment in sync
        }
        return found;
    }

    public boolean deleteOrder(String id) throws IOException {
        List<Order> orders = getAllOrders();
        boolean removed = orders.removeIf(o -> o.getId().equals(id));
        if (removed) saveAllOrders(orders);
        return removed;
    }

    public boolean assignRider(String orderId, String riderId) throws IOException {
        List<Order> orders = getAllOrders();
        boolean found = false;
        for (Order o : orders) {
            if (o.getId().equals(orderId)) {
                o.setAssignedRiderId(riderId);
                o.setStatus("accepted");
                found = true;
                break;
            }
        }
        if (found) saveAllOrders(orders);
        return found;
    }

    private void saveAllOrders(List<Order> orders) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath(), false))) {
            for (Order o : orders) { writer.write(o.toFileLine()); writer.newLine(); }
        }
    }
}
