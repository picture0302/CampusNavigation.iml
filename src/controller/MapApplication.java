package controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Objects;

import DAO.UserDao;
import model.location;
import model.path;
import service.LocationService;
import service.PathService;
import service.UserService;

public class MapApplication {
    private JFrame mainFrame;
    private JFrame mapFrame;
    private JPanel mapPanel;
    private boolean isAdmin = false;
    private String currentUserString ;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MapApplication().initialize());
    }

    private void initialize() {
        createMainFrame();
    }

    private void createMainFrame() {
        mainFrame = new JFrame("地图导航系统");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(400, 300);
        mainFrame.setLayout(new GridLayout(4, 1));

        JLabel titleLabel = new JLabel("地图导航系统", SwingConstants.CENTER);
        titleLabel.setFont(new Font("宋体", Font.BOLD, 40));
        mainFrame.add(titleLabel);

        JButton adminLoginButton = new JButton("管理员登录");
        adminLoginButton.addActionListener(e -> showAdminLoginDialog());
        mainFrame.add(adminLoginButton);

        JButton adminRegisterButton = new JButton("管理员注册");
        adminRegisterButton.addActionListener(e -> showAdminRegisterDialog());
        mainFrame.add(adminRegisterButton);

        JButton guestButton = new JButton("游客登录");
        guestButton.addActionListener(e -> {
            isAdmin = false;
            showMapFrame();
        });
        mainFrame.add(guestButton);

        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    private void showAdminLoginDialog() {
        JDialog loginDialog = new JDialog(mainFrame, "管理员登录", true);
        loginDialog.setLayout(new GridLayout(3, 2));
        loginDialog.setSize(300, 150);

        JLabel userLabel = new JLabel("用户名:");
        JTextField userField = new JTextField();
        JLabel passLabel = new JLabel("密码:");
        JPasswordField passField = new JPasswordField();

        JButton loginButton = new JButton("登录");
        loginButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            if (UserService.login(username, password)) {
                currentUserString = username;
                isAdmin = true;
                loginDialog.dispose();
                showMapFrame();
            } else {
                JOptionPane.showMessageDialog(loginDialog, "用户名或密码错误", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> loginDialog.dispose());

        loginDialog.add(userLabel);
        loginDialog.add(userField);
        loginDialog.add(passLabel);
        loginDialog.add(passField);
        loginDialog.add(loginButton);
        loginDialog.add(cancelButton);

        loginDialog.setLocationRelativeTo(mainFrame);
        loginDialog.setVisible(true);
    }

    private void showAdminRegisterDialog() {
        JDialog registerDialog = new JDialog(mainFrame, "管理员注册", true);
        registerDialog.setLayout(new GridLayout(4, 2));
        registerDialog.setSize(300, 200);

        JLabel userLabel = new JLabel("用户名:");
        JTextField userField = new JTextField();
        JLabel passLabel = new JLabel("密码（至少6位）:");
        JPasswordField passField = new JPasswordField();
        JLabel keyLabel = new JLabel("管理员密钥:");
        JPasswordField keyField = new JPasswordField();

        JButton registerButton = new JButton("注册");
        registerButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            String adminKey = new String(keyField.getPassword());

            if (password.length() < 6) {
                JOptionPane.showMessageDialog(registerDialog, "密码至少需要6个字符", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (password.length() > 50) {
                JOptionPane.showMessageDialog(registerDialog, "密码不能超过50个字符", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!"0000".equals(adminKey)) {
                JOptionPane.showMessageDialog(registerDialog, "管理员密钥错误", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (UserService.register(username, password)) {
                JOptionPane.showMessageDialog(registerDialog, "注册成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                registerDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(registerDialog, "用户名已存在", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> registerDialog.dispose());

        registerDialog.add(userLabel);
        registerDialog.add(userField);
        registerDialog.add(passLabel);
        registerDialog.add(passField);
        registerDialog.add(keyLabel);
        registerDialog.add(keyField);
        registerDialog.add(registerButton);
        registerDialog.add(cancelButton);

        registerDialog.setLocationRelativeTo(mainFrame);
        registerDialog.setVisible(true);
    }

    private void showMapFrame() {
        mainFrame.setVisible(false);

        mapFrame = new JFrame("地图导航");
        mapFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mapFrame.setSize(800, 600);

        mapPanel = new MapPanel();
        mapFrame.add(new JScrollPane(mapPanel));

        JPanel pathPanel = new JPanel();
        JLabel startLabel = new JLabel("起点:");

        // 获取所有地点名称
        List<location> allLocations = LocationService.getAllLocation();
        String[] locationNames = allLocations.stream()
                .map(location::getName)
                .toArray(String[]::new);

        JPanel controlPanel = new JPanel();
        if (isAdmin) {
            JButton addLocationButton = new JButton("添加地点");
            addLocationButton.addActionListener(e -> showAddLocationDialog());
            controlPanel.add(addLocationButton);

            JButton manageButton = new JButton("管理地点");
            manageButton.addActionListener(e -> showManageDialog());
            controlPanel.add(manageButton);

        }

        JComboBox<String> startCombo = new JComboBox<>(locationNames);
        startCombo.setPreferredSize(new Dimension(150, 25));

        JLabel endLabel = new JLabel("终点:");
        JComboBox<String> endCombo = new JComboBox<>(locationNames);
        endCombo.setPreferredSize(new Dimension(150, 25));

        JButton findPathButton = new JButton("查找路径");
        findPathButton.addActionListener(e -> {
            String startName = (String)startCombo.getSelectedItem();
            String endName = (String)endCombo.getSelectedItem();

            if(startName.equals(endName)){
                JOptionPane.showMessageDialog(mapFrame, "起点和终点不能相同", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            location startLoc = LocationService.getLocation(startName);
            location endLoc = LocationService.getLocation(endName);

            List<Integer> path = PathService.findShortestPath(startLoc.getId(), endLoc.getId());
            if (path.isEmpty()) {
                JOptionPane.showMessageDialog(mapFrame, "找不到路径", "提示", JOptionPane.INFORMATION_MESSAGE);
            } else {
                ((MapPanel)mapPanel).highlightPath(path);
                double distance = PathService.getPathDistance(path);
                JOptionPane.showMessageDialog(mapFrame,
                        "最短路径: " + startName + " → " + endName +
                                "\n距离: " + String.format("%.2f", distance),
                        "路径信息", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton refreshButton = new JButton("刷新地点");
        refreshButton.addActionListener(e -> {
            List<location> updatedLocations = LocationService.getAllLocation();
            String[] updatedNames = updatedLocations.stream()
                    .map(location::getName)
                    .toArray(String[]::new);

            startCombo.setModel(new DefaultComboBoxModel<>(updatedNames));
            endCombo.setModel(new DefaultComboBoxModel<>(updatedNames));
        });

        pathPanel.add(startLabel);
        pathPanel.add(startCombo);
        pathPanel.add(endLabel);
        pathPanel.add(endCombo);
        pathPanel.add(findPathButton);
        pathPanel.add(refreshButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(controlPanel, BorderLayout.NORTH);
        bottomPanel.add(pathPanel, BorderLayout.SOUTH);

        mapFrame.add(bottomPanel, BorderLayout.SOUTH);
        mapFrame.setLocationRelativeTo(null);
        mapFrame.setVisible(true);
    }

    private void showAddLocationDialog() {
        JDialog addDialog = new JDialog(mapFrame, "添加地点", true);
        addDialog.setLayout(new GridLayout(5, 2));
        addDialog.setSize(300, 200);

        JLabel nameLabel = new JLabel("地点名称:");
        JTextField nameField = new JTextField();
        JLabel latLabel = new JLabel("纬度:");
        JTextField latField = new JTextField();
        JLabel lngLabel = new JLabel("经度:");
        JTextField lngField = new JTextField();
        JLabel infoLabel = new JLabel("地点信息:");
        JTextField infoField = new JTextField();

        // 模拟自动获取经纬度
        JButton autoLocButton = new JButton("自动获取位置");
        autoLocButton.addActionListener(e -> {
            // 这里应该是实际获取位置的代码，我们模拟随机生成
            double lat = Math.random() * 180 - 90;
            double lng = Math.random() * 360 - 180;
            latField.setText(String.format("%.6f", lat));
            lngField.setText(String.format("%.6f", lng));
        });

        JButton addButton = new JButton("添加");
        addButton.addActionListener(e -> {
            String name = nameField.getText();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(addDialog, "地点名称不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double lat = Double.parseDouble(latField.getText());
                double lng = Double.parseDouble(lngField.getText());
                String info = infoField.getText();

                location loc = new location();
                loc.setName(name);
                loc.setLatitude(lat);
                loc.setLongitude(lng);
                loc.setDescription(info);

                if (LocationService.addLocation(loc)) {
                    JOptionPane.showMessageDialog(addDialog, "添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    addDialog.dispose();
                    ((MapPanel)mapPanel).refreshLocations();
                } else {
                    JOptionPane.showMessageDialog(addDialog, "添加失败，名称或位置已存在", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(addDialog, "请输入有效的经纬度", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        addDialog.add(nameLabel);
        addDialog.add(nameField);
        addDialog.add(latLabel);
        addDialog.add(latField);
        addDialog.add(lngLabel);
        addDialog.add(lngField);
        addDialog.add(infoLabel);
        addDialog.add(infoField);
        addDialog.add(autoLocButton);
        addDialog.add(addButton);

        addDialog.setLocationRelativeTo(mapFrame);
        addDialog.setVisible(true);
    }

    private void showManageDialog() {
        List<location> locations = LocationService.getAllLocation();
        if (locations.isEmpty()) {
            JOptionPane.showMessageDialog(mapFrame, "没有可管理的地点", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] locationNames = locations.stream().map(location::getName).toArray(String[]::new);
        String selectedName = (String) JOptionPane.showInputDialog(
                mapFrame,
                "选择要管理的地点:",
                "管理地点",
                JOptionPane.PLAIN_MESSAGE,
                null,
                locationNames,
                locationNames[0]);

        if (selectedName != null) {
            location selectedLoc = LocationService.getLocation(selectedName);
            showLocationManagementDialog(selectedLoc);
        }
    }

    private void showLocationManagementDialog(location loc) {
        JDialog manageDialog = new JDialog(mapFrame, "管理地点: " + loc.getName(), true);
        manageDialog.setLayout(new GridLayout(5, 2));
        manageDialog.setSize(300, 200);

        JLabel nameLabel = new JLabel("地点名称:");
        JTextField nameField = new JTextField(loc.getName());
        JLabel latLabel = new JLabel("纬度:");
        JTextField latField = new JTextField(String.valueOf(loc.getLatitude()));
        JLabel lngLabel = new JLabel("经度:");
        JTextField lngField = new JTextField(String.valueOf(loc.getLongitude()));
        JLabel infoLabel = new JLabel("地点信息:");
        JTextField infoField = new JTextField(loc.getDescription());


        JButton updateButton = new JButton("更新");
        updateButton.addActionListener(e -> {
            try {
                loc.setName(nameField.getText());
                loc.setLatitude(Double.parseDouble(latField.getText()));
                loc.setLongitude(Double.parseDouble(lngField.getText()));
                loc.setDescription(infoField.getText());

                if (LocationService.updateLocation(loc)) {
                    JOptionPane.showMessageDialog(manageDialog, "更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    manageDialog.dispose();
                    ((MapPanel)mapPanel).refreshLocations();
                } else {
                    JOptionPane.showMessageDialog(manageDialog, "更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(manageDialog, "请输入有效的经纬度", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton deleteButton = new JButton("删除");
        deleteButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    manageDialog,
                    "确定要删除这个地点吗?",
                    "确认",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (LocationService.deleteLocation(loc)) {
                    JOptionPane.showMessageDialog(manageDialog, "删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    manageDialog.dispose();
                    ((MapPanel)mapPanel).refreshLocations();
                } else {
                    JOptionPane.showMessageDialog(manageDialog, "删除失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton pathButton = new JButton("管理路径");
        pathButton.addActionListener(e -> {
            manageDialog.dispose();
            showPathManagementDialog(loc);
        });

        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> manageDialog.dispose());

        manageDialog.add(nameLabel);
        manageDialog.add(nameField);
        manageDialog.add(latLabel);
        manageDialog.add(latField);
        manageDialog.add(lngLabel);
        manageDialog.add(lngField);
        manageDialog.add(infoLabel);
        manageDialog.add(infoField);
        manageDialog.add(updateButton);
        manageDialog.add(deleteButton);
        manageDialog.add(pathButton);
        manageDialog.add(closeButton);

        manageDialog.setLocationRelativeTo(mapFrame);
        manageDialog.setVisible(true);
    }

    private void showPathManagementDialog(location startLoc) {
        List<location> allLocations = LocationService.getAllLocation();
        allLocations.removeIf(l -> l.getId() == startLoc.getId());

        if (allLocations.isEmpty()) {
            JOptionPane.showMessageDialog(mapFrame, "没有其他地点可连接", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] locationNames = allLocations.stream()
                .map(location::getName)
                .toArray(String[]::new);

        String selectedName = (String) JOptionPane.showInputDialog(
                mapFrame,
                "选择要连接的地点:",
                "管理路径",
                JOptionPane.PLAIN_MESSAGE,
                null,
                locationNames,
                locationNames[0]);

        if (selectedName != null) {
            location endLoc = LocationService.getLocation(selectedName);
            List<path> existingPaths = PathService.getallPath(startLoc.getId());

            // 修复点：过滤空值并检查路径是否存在
            boolean pathExists = existingPaths.stream()
                    .filter(Objects::nonNull)  // 过滤null值
                    .anyMatch(p -> p.getStartid() == endLoc.getId() || p.getEndid() == endLoc.getId());

            if (pathExists) {
                int confirm = JOptionPane.showConfirmDialog(
                        mapFrame,
                        "路径已存在，是否删除?",
                        "管理路径",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    // 查找并删除路径（同样需要过滤null）
                    existingPaths.stream()
                            .filter(Objects::nonNull)
                            .filter(p -> p.getStartid() == endLoc.getId() || p.getEndid() == endLoc.getId())
                            .findFirst()
                            .ifPresent(p -> {
                                if (PathService.deletePath(p.getId())) {
                                    JOptionPane.showMessageDialog(mapFrame, "路径删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                                    ((MapPanel) mapPanel).refreshPaths();
                                } else {
                                    JOptionPane.showMessageDialog(mapFrame, "路径删除失败", "错误", JOptionPane.ERROR_MESSAGE);
                                }
                            });
                }
            } else {
                if (PathService.addPath(startLoc.getId(), endLoc.getId())) {
                    JOptionPane.showMessageDialog(mapFrame, "路径添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    ((MapPanel) mapPanel).refreshPaths();
                } else {
                    JOptionPane.showMessageDialog(mapFrame, "路径添加失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    class MapPanel extends JPanel {
        private List<location> locations;
        private List<path> paths;
        private List<Integer> highlightedPath;
        private double minLat = -90, maxLat = 90;
        private double minLng = -180, maxLng = 180;
        private path selectedPath = null;

        public MapPanel() {
            setPreferredSize(new Dimension(1200, 800));
            refreshLocations();
            refreshPaths();

            if (isAdmin) {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // 仅响应右键点击
                        if (SwingUtilities.isRightMouseButton(e)) {
                            // 检查是否点击了现有地点
                            boolean clickedOnLocation = false;
                            for (location loc : locations) {
                                int x = lonToX(loc.getLongitude());
                                int y = latToY(loc.getLatitude());

                                if (Math.abs(e.getX() - x) < 10 && Math.abs(e.getY() - y) < 10) {
                                    clickedOnLocation = true;
                                    showLocationManagementDialog(loc);
                                    break;
                                }
                            }

                            // 右键点击空白处添加地点
                            if (!clickedOnLocation) {
                                showAddLocationAtPosition(e.getX(), e.getY());
                            }
                        }
                    }

                });
            }
            if (isAdmin) {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // 右键点击逻辑保持不变
                        if (SwingUtilities.isRightMouseButton(e)) {
                            // 原有右键点击代码
                            return;
                        }

                        // 左键点击检测路径
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            selectedPath = findPathAtPosition(e.getX(), e.getY());
                            if (selectedPath != null) {
                                showPathContextMenu(e.getX(), e.getY());
                            }
                        }
                    }
                });
            }

        }
        private path findPathAtPosition(int x, int y) {
            final int CLICK_THRESHOLD = 5; // 点击容差范围

            for (path p : paths) {
                if (p == null) continue;

                location startLoc = findLocationById(p.getStartid());
                location endLoc = findLocationById(p.getEndid());

                if (startLoc != null && endLoc != null) {
                    int x1 = lonToX(startLoc.getLongitude());
                    int y1 = latToY(startLoc.getLatitude());
                    int x2 = lonToX(endLoc.getLongitude());
                    int y2 = latToY(endLoc.getLatitude());

                    // 计算点到线段的距离
                    double dist = distancePointToLine(x, y, x1, y1, x2, y2);
                    if (dist < CLICK_THRESHOLD) {
                        return p;
                    }
                }
            }
            return null;
        }
        private double distancePointToLine(int px, int py, int x1, int y1, int x2, int y2) {
            double A = px - x1;
            double B = py - y1;
            double C = x2 - x1;
            double D = y2 - y1;

            double dot = A * C + B * D;
            double len_sq = C * C + D * D;
            double param = (len_sq != 0) ? dot / len_sq : -1;

            double xx, yy;

            if (param < 0) {
                xx = x1;
                yy = y1;
            } else if (param > 1) {
                xx = x2;
                yy = y2;
            } else {
                xx = x1 + param * C;
                yy = y1 + param * D;
            }

            double dx = px - xx;
            double dy = py - yy;
            return Math.sqrt(dx * dx + dy * dy);
        }

        // 显示路径上下文菜单
        private void showPathContextMenu(int x, int y) {
            JPopupMenu popupMenu = new JPopupMenu();

            // 显示路径信息
            location startLoc = findLocationById(selectedPath.getStartid());
            location endLoc = findLocationById(selectedPath.getEndid());
            String info = String.format("%s → %s (距离: %.2f)",
                    startLoc.getName(),
                    endLoc.getName(),
                    selectedPath.getDistance());

            JMenuItem infoItem = new JMenuItem(info);
            infoItem.setEnabled(false);
            popupMenu.add(infoItem);
            popupMenu.addSeparator();

            // 删除路径选项
            JMenuItem deleteItem = new JMenuItem("删除路径");
            deleteItem.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                        MapPanel.this,
                        "确定要删除这条路径吗?",
                        "确认删除",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    if (PathService.deletePath(selectedPath.getId())) {
                        JOptionPane.showMessageDialog(
                                MapPanel.this,
                                "路径删除成功",
                                "成功",
                                JOptionPane.INFORMATION_MESSAGE);
                        refreshPaths();
                    } else {
                        JOptionPane.showMessageDialog(
                                MapPanel.this,
                                "路径删除失败",
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            popupMenu.add(deleteItem);

            popupMenu.show(this, x, y);
        }
        private void showAddLocationAtPosition(int x, int y) {
            double lat = yToLat(y);
            double lon = xToLon(x);

            JDialog dialog = new JDialog(mapFrame, "在此位置添加地点", true);
            dialog.setLayout(new GridLayout(3, 2));
            dialog.setSize(300, 150);

            JLabel nameLabel = new JLabel("地点名称:");
            JTextField nameField = new JTextField();
            JLabel infoLabel = new JLabel("地点信息:");
            JTextField infoField = new JTextField();

            JButton addButton = new JButton("添加");
            addButton.addActionListener(e -> {
                String name = nameField.getText();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "地点名称不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                location loc = new location();
                loc.setName(name);
                loc.setLatitude(lat);
                loc.setLongitude(lon);
                loc.setDescription(infoField.getText());

                if (LocationService.addLocation(loc)) {
                    JOptionPane.showMessageDialog(dialog, "添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    refreshLocations();
                } else {
                    JOptionPane.showMessageDialog(dialog, "添加失败，名称或位置已存在", "错误", JOptionPane.ERROR_MESSAGE);
                }
            });

            JButton cancelButton = new JButton("取消");
            cancelButton.addActionListener(e -> dialog.dispose());

            dialog.add(nameLabel);
            dialog.add(nameField);
            dialog.add(infoLabel);
            dialog.add(infoField);
            dialog.add(addButton);
            dialog.add(cancelButton);

            dialog.setLocationRelativeTo(mapFrame);
            dialog.setVisible(true);
        }

        public void refreshLocations() {
            locations = LocationService.getAllLocation();
            calculateBounds();
            repaint();
        }

        public void refreshPaths() {
            paths = PathService.getallPath();
            repaint();
        }

        public void highlightPath(List<Integer> path) {
            highlightedPath = path;
            repaint();
        }

        private void calculateBounds() {
            if (locations.isEmpty()) return;

            minLat = locations.get(0).getLatitude();
            maxLat = locations.get(0).getLatitude();
            minLng = locations.get(0).getLongitude();
            maxLng = locations.get(0).getLongitude();

            for (location loc : locations) {
                minLat = Math.min(minLat, loc.getLatitude());
                maxLat = Math.max(maxLat, loc.getLatitude());
                minLng = Math.min(minLng, loc.getLongitude());
                maxLng = Math.max(maxLng, loc.getLongitude());
            }

            // 添加一些边距
            double latMargin = (maxLat - minLat) * 0.1;
            double lngMargin = (maxLng - minLng) * 0.1;

            minLat -= latMargin;
            maxLat += latMargin;
            minLng -= lngMargin;
            maxLng += lngMargin;

            // 确保在有效范围内
            minLat = Math.max(minLat, -90);
            maxLat = Math.min(maxLat, 90);
            minLng = Math.max(minLng, -180);
            maxLng = Math.min(maxLng, 180);
        }

        private int lonToX(double longitude) {
            return (int) ((longitude - minLng) / (maxLng - minLng) * (getWidth() - 100)) + 50;
        }

        private int latToY(double latitude) {
            return (int) ((maxLat - latitude) / (maxLat - minLat) * (getHeight() - 100)) + 50;
        }

        private double xToLon(int x) {
            return minLng + (x - 50) * (maxLng - minLng) / (getWidth() - 100);
        }

        private double yToLat(int y) {
            return maxLat - (y - 50) * (maxLat - minLat) / (getHeight() - 100);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // 绘制经纬度网格
            drawGrid(g);

            // 绘制路径
            drawPaths(g);

            // 绘制高亮路径
            if (highlightedPath != null && !highlightedPath.isEmpty()) {
                drawHighlightedPath(g);
            }

            // 绘制地点
            drawLocations(g);
        }

        private void drawGrid(Graphics g) {
            g.setColor(Color.LIGHT_GRAY);

            // 绘制经线
            for (double lon = Math.ceil(minLng / 10) * 10; lon <= maxLng; lon += 10) {
                int x = lonToX(lon);
                g.drawLine(x, 50, x, getHeight() - 50);
                g.drawString(String.format("%.1f°", lon), x - 15, getHeight() - 30);
            }

            // 绘制纬线
            for (double lat = Math.ceil(minLat / 10) * 10; lat <= maxLat; lat += 10) {
                int y = latToY(lat);
                g.drawLine(50, y, getWidth() - 50, y);
                g.drawString(String.format("%.1f°", lat), 20, y + 5);
            }

            // 绘制边框
            g.setColor(Color.BLACK);
            g.drawRect(50, 50, getWidth() - 100, getHeight() - 100);
        }

        private void drawPaths(Graphics g) {
            for (path p : paths) {
                if(p == null) continue;

                location startLoc = findLocationById(p.getStartid());
                location endLoc = findLocationById(p.getEndid());

                if (startLoc != null && endLoc != null) {
                    int x1 = lonToX(startLoc.getLongitude());
                    int y1 = latToY(startLoc.getLatitude());
                    int x2 = lonToX(endLoc.getLongitude());
                    int y2 = latToY(endLoc.getLatitude());

                    // 如果是选中的路径，用不同颜色显示
                    if (p == selectedPath) {
                        g.setColor(Color.MAGENTA);
                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setStroke(new BasicStroke(3));
                        g2d.drawLine(x1, y1, x2, y2);
                        g2d.setStroke(new BasicStroke(1));
                    } else {
                        g.setColor(Color.BLUE);
                        g.drawLine(x1, y1, x2, y2);
                    }

                    // 绘制距离
                    int midX = (x1 + x2) / 2;
                    int midY = (y1 + y2) / 2;
                    g.setColor(Color.BLACK);
                    g.drawString(String.format("%.1f", p.getDistance()), midX, midY);
                }
            }
        }

        private void drawHighlightedPath(Graphics g) {
            g.setColor(Color.RED);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(3));

            for (int i = 0; i < highlightedPath.size() - 1; i++) {
                location startLoc = findLocationById(highlightedPath.get(i));
                location endLoc = findLocationById(highlightedPath.get(i + 1));

                if (startLoc != null && endLoc != null) {
                    int x1 = lonToX(startLoc.getLongitude());
                    int y1 = latToY(startLoc.getLatitude());
                    int x2 = lonToX(endLoc.getLongitude());
                    int y2 = latToY(endLoc.getLatitude());

                    g2d.drawLine(x1, y1, x2, y2);
                }
            }

            g2d.setStroke(new BasicStroke(1));
        }

        private void drawLocations(Graphics g) {
            for (location loc : locations) {
                int x = lonToX(loc.getLongitude());
                int y = latToY(loc.getLatitude());

                // 绘制地点标记
                g.setColor(isAdmin ? Color.GREEN : Color.BLUE);
                g.fillOval(x - 5, y - 5, 10, 10);

                // 绘制地点名称
                g.setColor(Color.BLACK);
                g.drawString(loc.getName(), x + 10, y + 5);

                // 如果是高亮路径的一部分，用红色标记
                if (highlightedPath != null && highlightedPath.contains(loc.getId())) {
                    g.setColor(Color.CYAN);
                    g.drawOval(x - 8, y - 8, 16, 16);
                }
            }
        }

        private location findLocationById(int id) {
            for (location loc : locations) {
                if (loc.getId() == id) {
                    return loc;
                }
            }
            return null;
        }
    }
}
