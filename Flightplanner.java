import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Flightplanner extends JFrame {
    private final Map<String, Point> cities = new HashMap<>();
    private final Map<String, java.util.List<Edge>> graph = new HashMap<>();
    private final DrawPanel drawPanel = new DrawPanel();

    private final JComboBox<String> fromBox = new JComboBox<>();
    private final JComboBox<String> toBox = new JComboBox<>();
    private final JTextArea outputArea = new JTextArea(5, 30);

    private java.util.List<String> shortestPath = new java.util.ArrayList<>();

    public Flightplanner() {
        setTitle("Flight Route Planner");
        setSize(900, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        
        Color bgColor = new Color(255, 255, 255); 
        Color panelColor = new Color(255, 223, 186); 
        Color btnColor = new Color(34, 193, 195); 
        Color hoverBtnColor = new Color(253, 187, 45); 
        Color routeColor = new Color(255, 87, 34); 
        Color selectedRouteColor = new Color(33, 150, 243); 
        Color textColor = Color.WHITE;

    
        JPanel inputPanel = new JPanel(new GridLayout(2, 1));
        inputPanel.setBackground(bgColor);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        
        JPanel cityPanel = createRoundedPanel(panelColor);
        JPanel flightPanel = createRoundedPanel(panelColor);

        
        JTextField cityName = new JTextField(10);
        JButton addCityBtn = createRoundedButton("Add City", btnColor, hoverBtnColor, textColor);
        addCityBtn.addActionListener(e -> {
            String name = cityName.getText().trim();
            if (!name.isEmpty() && !cities.containsKey(name)) {
                Point location = new Point((int)(Math.random() * 700), (int)(Math.random() * 500));
                cities.put(name, location);
                graph.put(name, new java.util.ArrayList<>());
                fromBox.addItem(name);
                toBox.addItem(name);
                cityName.setText("");
                drawPanel.repaint();
            }
        });

        JTextField fromField = new JTextField(5);
        JTextField toField = new JTextField(5);
        JTextField distanceField = new JTextField(5);
        JButton addFlightBtn = createRoundedButton("Add Flight", btnColor, hoverBtnColor, textColor);
        addFlightBtn.addActionListener(e -> {
            String from = fromField.getText().trim();
            String to = toField.getText().trim();
            try {
                int dist = Integer.parseInt(distanceField.getText().trim());
                if (!from.equals(to) && graph.containsKey(from) && graph.containsKey(to)) {
                    if (graph.get(from).stream().noneMatch(edge -> edge.destination.equals(to))) {
                        graph.get(from).add(new Edge(to, dist));
                        graph.get(to).add(new Edge(from, dist));
                        outputArea.append("Added route: " + from + " -> " + to + " = " + dist + "\n");
                        fromField.setText("");
                        toField.setText("");
                        distanceField.setText("");
                        drawPanel.repaint();
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid distance!");
            }
        });

        cityPanel.add(new JLabel("City:"));
        cityPanel.add(cityName);
        cityPanel.add(addCityBtn);

        flightPanel.add(new JLabel("From:"));
        flightPanel.add(fromField);
        flightPanel.add(new JLabel("To:"));
        flightPanel.add(toField);
        flightPanel.add(new JLabel("Distance:"));
        flightPanel.add(distanceField);
        flightPanel.add(addFlightBtn);

        inputPanel.add(cityPanel);
        inputPanel.add(flightPanel);

        
        JPanel routePanel = new JPanel();
        routePanel.setBorder(BorderFactory.createTitledBorder("Find Shortest Route"));
        routePanel.setBackground(panelColor);
        routePanel.setPreferredSize(new Dimension(200, 0));
        JButton findRouteBtn = createRoundedButton("Find Route", btnColor, hoverBtnColor, textColor);
        findRouteBtn.addActionListener(e -> {
            String start = (String) fromBox.getSelectedItem();
            String end = (String) toBox.getSelectedItem();
            shortestPath = dijkstra(start, end);
            if (!shortestPath.isEmpty()) {
                int totalDist = 0;
                for (int i = 0; i < shortestPath.size() - 1; i++) {
                    String from = shortestPath.get(i);
                    String to = shortestPath.get(i + 1);
                    totalDist += graph.get(from).stream()
                            .filter(edge -> edge.destination.equals(to))
                            .map(edge -> edge.distance)
                            .findFirst()
                            .orElse(0);
                }
                outputArea.append("\nShortest path: " + String.join(" -> ", shortestPath) +
                        "\nTotal Distance: " + totalDist + "\n");
            } else {
                outputArea.append("\nNo path found between " + start + " and " + end + "\n");
            }
            drawPanel.repaint();
        });
        routePanel.add(new JLabel("From:"));
        routePanel.add(fromBox);
        routePanel.add(new JLabel("To:"));
        routePanel.add(toBox);
        routePanel.add(findRouteBtn);

        
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setBackground(new Color(240, 248, 255));
        outputArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));

    
        add(inputPanel, BorderLayout.NORTH);
        add(drawPanel, BorderLayout.CENTER);
        add(new JScrollPane(outputArea), BorderLayout.SOUTH);
        add(routePanel, BorderLayout.WEST);


        String[] initialCities = {
                "Dhaka", "Chittagong", "Khulna", "Rajshahi", "Sylhet", "Barisal", "Rangpur",
                "Comilla", "Jessore", "Mymensingh", "Cox's Bazar", "Bogura", "Narsingdi",
                "Tangail", "Noakhali", "Feni", "Brahmanbaria", "Chapainawabganj", "Jhalokathi", "Meherpur"
        };
        for (String city : initialCities) {
            Point location = new Point((int)(Math.random() * 700), (int)(Math.random() * 500));
            cities.put(city, location);
            graph.put(city, new java.util.ArrayList<>());
            fromBox.addItem(city);
            toBox.addItem(city);
        }


        addEdge("Dhaka", "Chittagong", 250);
        addEdge("Dhaka", "Khulna", 220);
        addEdge("Dhaka", "Rajshahi", 250);
        addEdge("Dhaka", "Sylhet", 210);
        addEdge("Khulna", "Barisal", 120);
        addEdge("Rajshahi", "Rangpur", 160);
        addEdge("Sylhet", "Chittagong", 300);
        addEdge("Barisal", "Chittagong", 180);
        addEdge("Rangpur", "Dhaka", 270);
        addEdge("Dhaka", "Mymensingh", 110);
        addEdge("Chittagong", "Cox's Bazar", 150);
        addEdge("Rajshahi", "Bogura", 80);
        addEdge("Khulna", "Jessore", 60);
        addEdge("Comilla", "Dhaka", 90);
    }

    private void addEdge(String from, String to, int distance) {
        graph.get(from).add(new Edge(to, distance));
        graph.get(to).add(new Edge(from, distance));
    }

    private class DrawPanel extends JPanel {

        private final Color selectedRouteColor = new Color(33, 150, 243);

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            setBackground(Color.WHITE);


            for (String from : graph.keySet()) {
                for (Edge edge : graph.get(from)) {
                    Point p1 = cities.get(from);
                    Point p2 = cities.get(edge.destination);
                    if (p1 != null && p2 != null) {
                        g2.setColor(Color.LIGHT_GRAY);
                        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                        int midX = (p1.x + p2.x) / 2;
                        int midY = (p1.y + p2.y) / 2;
                        g2.setColor(Color.DARK_GRAY);
                        g2.drawString(String.valueOf(edge.distance), midX, midY);
                    }
                }
            }


            g2.setStroke(new BasicStroke(3));
            g2.setColor(selectedRouteColor);
            for (int i = 0; i < shortestPath.size() - 1; i++) {
                Point p1 = cities.get(shortestPath.get(i));
                Point p2 = cities.get(shortestPath.get(i + 1));
                if (p1 != null && p2 != null)
                    g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            }


            for (String city : cities.keySet()) {
                Point p = cities.get(city);
                g2.setColor(new Color(33, 150, 243));
                g2.fillOval(p.x - 6, p.y - 6, 12, 12);
                g2.setColor(Color.BLACK);
                g2.drawString(city, p.x + 8, p.y);
            }
        }
    }

    private java.util.List<String> dijkstra(String start, String end) {
        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<Map.Entry<String, Integer>> pq = new PriorityQueue<>(Map.Entry.comparingByValue());

        for (String city : graph.keySet())
            dist.put(city, Integer.MAX_VALUE);
        dist.put(start, 0);
        pq.add(new AbstractMap.SimpleEntry<>(start, 0));

        while (!pq.isEmpty()) {
            String current = pq.poll().getKey();
            if (current.equals(end)) break;
            for (Edge edge : graph.get(current)) {
                int alt = dist.get(current) + edge.distance;
                if (alt < dist.get(edge.destination)) {
                    dist.put(edge.destination, alt);
                    prev.put(edge.destination, current);
                    pq.add(new AbstractMap.SimpleEntry<>(edge.destination, alt));
                }
            }
        }

        java.util.List<String> path = new java.util.ArrayList<>();
        for (String at = end; at != null; at = prev.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        if (!path.get(0).equals(start)) return new java.util.ArrayList<>();
        return path;
    }


    private JPanel createRoundedPanel(Color color) {
        JPanel panel = new JPanel();
        panel.setBackground(color);
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        return panel;
    }


    private JButton createRoundedButton(String text, Color bgColor, Color hoverColor, Color textColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Flightplanner().setVisible(true));
    }

    private static class Edge {
        String destination;
        int distance;

        Edge(String destination, int distance) {
            this.destination = destination;
            this.distance = distance;
        }
    }
}
