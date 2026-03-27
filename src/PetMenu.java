import javax.swing.*;
import java.awt.*;
import java.awt.Desktop;

public class PetMenu {

    static final int WIDTH = 150;

    private final JPanel panel;
    private final PetStats stats;

    // Grass images for the Play button
    private static final String[] GRASS_URLS = {
            "https://www.thisoldhouse.com/wp-content/uploads/2020/08/iStock_511747120-scaled.jpg",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSw15kIqSjCMWOaDlOx-rG_3J1J7oXXIRZCoA&s",
            "https://media.istockphoto.com/id/161820827/photo/morning-dew-drops-on-green-leafs.jpg?s=612x612&w=0&k=20&c=qxLfhCiF7yuICQr_Nm9c1ORLl3HDaSk8_F-xs_P5S-w=",
            "https://www.monrovia.com/media/catalog/product/cache/7b381462074fb6871f01f9faa7ed2e11/r/e/rest_2_2_2226.webp",
            "https://plantsexpress.com/cdn/shop/products/Mexican-Feather-Grass-3.jpg?v=1684511963",
            "https://www.marthastewart.com/thmb/BCdVOmdJCZ0v61Qd2wtyqctPbOY=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/most-common-types-grasses-fine-fescue-getty-0423-1711845ef74b4c10a20579384a5eb1be.jpg",
            "https://www.pennington.com/all-products/grass-seed/resources/-/media/Project/OneWeb/Pennington/Images/blog/seed/Your-First-Lawn--Start-Here/lawn_slice.jpg",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/Spelt_grass_grown_outdoors._With_a_deeper_green_color_than_wheat.jpg/250px-Spelt_grass_grown_outdoors._With_a_deeper_green_color_than_wheat.jpg",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRcxBAIn7Yzwr8bWSIPPnFhCW4zLE87cNETZQ&s",
            "https://media.istockphoto.com/id/2177658794/photo/home-backyard-with-green-grass-and-sunrays.jpg?s=612x612&w=0&k=20&c=en1jkzChxMkainb7d3hzQoGgLrLrJtMMsH4-okMIY7c=",
            "https://cdn.britannica.com/40/120840-004-BDE9C7D7/Bermuda-grass.jpg",
            "https://cdn.create.vista.com/api/media/small/285879962/stock-photo-selective-focus-trees-green-grass-park-summertime"
    };

    public PetMenu(PetStats stats, JDialog dialog) {
        this.stats = stats;
        this.panel = buildPanel(dialog);
    }

    public JPanel getPanel() { return panel; }

    // ── Panel construction ───────────────────────────────────────

    private JPanel buildPanel(JDialog dialog) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40, 40, 40, 240));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("🐱 Menu");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(title);
        p.add(Box.createVerticalStrut(10));

        addButton(p, "🍖 Feed",      () -> { stats.addHunger(20);  stats.addHappiness(5);  stats.addCoins(10);  save(); });
        addButton(p, "🎾 Play",      () -> { stats.addHappiness(20); stats.addEnergy(-10); stats.addCoins(15); save(); openRandomGrass(); });
        addButton(p, "😴 Sleep",     () -> { stats.addEnergy(50);   save(); });
        addButton(p, "⚙️ Settings",  () -> System.out.println("Opening settings!"));
        addButton(p, "👁 Hide",      () -> PetTray.hide(dialog));
        addButton(p, "❌ Exit",      () -> { save(); PetTray.remove(); System.exit(0); });

        return p;
    }

    private void addButton(JPanel parent, String label, Runnable action) {
        parent.add(makeButton(label, action));
        parent.add(Box.createVerticalStrut(5));
    }

    private JButton makeButton(String text, Runnable action) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed()  ? new Color(80,80,80)
                        : getModel().isRollover() ? new Color(100,100,100)
                        : new Color(60,60,60);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()  - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(WIDTH - 30, 30));
        btn.setMaximumSize(new Dimension(WIDTH - 30, 30));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    // ── Helpers ──────────────────────────────────────────────────

    private void save() { SaveManager.save(stats); }

    private void openRandomGrass() {
        String url = GRASS_URLS[(int)(Math.random() * GRASS_URLS.length)];
        try { Desktop.getDesktop().browse(new java.net.URI(url)); }
        catch (Exception e) { e.printStackTrace(); }
    }
}