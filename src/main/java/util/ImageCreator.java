package util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ImageCreator {
    
    public static void createAllImages() {
        System.out.println("Creating images for Pet Adoption System...");
        
        try {
            // Create directories
            Files.createDirectories(Paths.get("src/main/resources/images"));
            Files.createDirectories(Paths.get("src/main/resources/images/pets"));
            
            // Create logo/icon
            createLogoImage();
            
            // Create pet images
            createPetImages();
            
            // Create UI images
            createUIImages();
            
            System.out.println("✅ All images created successfully!");
            
        } catch (IOException e) {
            System.err.println("Error creating images: " + e.getMessage());
        }
    }
    
    private static void createLogoImage() throws IOException {
        BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Draw background
        GradientPaint gradient = new GradientPaint(0, 0, new Color(79, 70, 229), 
                                                 256, 256, new Color(124, 58, 237));
        g2d.setPaint(gradient);
        g2d.fillRoundRect(0, 0, 256, 256, 40, 40);
        
        // Draw paw print
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 140));
        
        // Center the paw emoji
        FontMetrics fm = g2d.getFontMetrics();
        String paw = "🐾";
        int x = (256 - fm.stringWidth(paw)) / 2;
        int y = (256 - fm.getHeight()) / 2 + fm.getAscent();
        
        g2d.drawString(paw, x, y);
        
        // Draw text
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.setColor(Color.WHITE);
        String text = "NexusFauna";
        FontMetrics fmText = g2d.getFontMetrics();
        int textX = (256 - fmText.stringWidth(text)) / 2;
        int textY = 230;
        g2d.drawString(text, textX, textY);
        
        g2d.dispose();
        
        // Save image
        File output = new File("src/main/resources/images/pet-icon.png");
        ImageIO.write(image, "PNG", output);
        System.out.println("Created: pet-icon.png");
    }
    
    private static void createPetImages() throws IOException {
        String[] petNames = {"dog1", "dog2", "cat1", "cat2", "bird1", "rabbit1"};
        String[] petTypes = {"Dog", "Dog", "Cat", "Cat", "Bird", "Rabbit"};
        Color[] colors = {
            new Color(255, 182, 193), // Light Pink
            new Color(173, 216, 230), // Light Blue
            new Color(144, 238, 144), // Light Green
            new Color(255, 218, 185), // Peach
            new Color(221, 160, 221), // Plum
            new Color(255, 228, 196)  // Bisque
        };
        
        for (int i = 0; i < petNames.length; i++) {
            BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw background
            g2d.setColor(colors[i]);
            g2d.fillRoundRect(0, 0, 200, 200, 30, 30);
            
            // Draw border
            g2d.setColor(new Color(0, 0, 0, 50));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRoundRect(2, 2, 196, 196, 30, 30);
            
            // Draw pet type icon
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.setFont(new Font("Arial", Font.BOLD, 80));
            
            String icon = getPetIcon(petTypes[i]);
            FontMetrics fm = g2d.getFontMetrics();
            int x = (200 - fm.stringWidth(icon)) / 2;
            int y = (200 - fm.getHeight()) / 2 + fm.getAscent();
            
            g2d.drawString(icon, x, y);
            
            // Draw pet name
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            FontMetrics fmName = g2d.getFontMetrics();
            String name = petTypes[i];
            int nameX = (200 - fmName.stringWidth(name)) / 2;
            int nameY = 180;
            g2d.drawString(name, nameX, nameY);
            
            g2d.dispose();
            
            // Save image
            File output = new File("src/main/resources/images/pets/" + petNames[i] + ".png");
            ImageIO.write(image, "PNG", output);
            System.out.println("Created: pets/" + petNames[i] + ".png");
        }
    }
    
    private static void createUIImages() throws IOException {
        // Create login background
        createLoginBackground();
        
        // Create dashboard icon
        createDashboardIcon();
        
        // Create adoption icon
        createAdoptionIcon();
    }
    
    private static void createLoginBackground() throws IOException {
        BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Create gradient background
        GradientPaint gradient = new GradientPaint(0, 0, new Color(102, 126, 234), 
                                                 800, 600, new Color(118, 75, 162));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, 800, 600);
        
        // Add pattern
        g2d.setColor(new Color(255, 255, 255, 20));
        for (int i = 0; i < 800; i += 40) {
            for (int j = 0; j < 600; j += 40) {
                g2d.fillOval(i, j, 10, 10);
            }
        }
        
        g2d.dispose();
        
        File output = new File("src/main/resources/images/login-background.png");
        ImageIO.write(image, "PNG", output);
        System.out.println("Created: login-background.png");
    }
    
    private static void createDashboardIcon() throws IOException {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw icon background
        g2d.setColor(new Color(79, 70, 229));
        g2d.fillRoundRect(0, 0, 64, 64, 15, 15);
        
        // Draw dashboard grid
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        
        // Vertical lines
        g2d.drawLine(20, 20, 20, 44);
        g2d.drawLine(32, 20, 32, 44);
        g2d.drawLine(44, 20, 44, 44);
        
        // Horizontal lines
        g2d.drawLine(20, 20, 44, 20);
        g2d.drawLine(20, 32, 44, 32);
        g2d.drawLine(20, 44, 44, 44);
        
        g2d.dispose();
        
        File output = new File("src/main/resources/images/dashboard-icon.png");
        ImageIO.write(image, "PNG", output);
        System.out.println("Created: dashboard-icon.png");
    }
    
    private static void createAdoptionIcon() throws IOException {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw icon background
        g2d.setColor(new Color(16, 185, 129)); // Green
        g2d.fillRoundRect(0, 0, 64, 64, 15, 15);
        
        // Draw heart
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 36));
        g2d.drawString("❤", 18, 45);
        
        // Draw plus sign
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(40, 25, 40, 35); // Vertical line
        g2d.drawLine(35, 30, 45, 30); // Horizontal line
        
        g2d.dispose();
        
        File output = new File("src/main/resources/images/adoption-icon.png");
        ImageIO.write(image, "PNG", output);
        System.out.println("Created: adoption-icon.png");
    }
    
    private static String getPetIcon(String type) {
        switch (type.toLowerCase()) {
            case "dog": return "🐕";
            case "cat": return "🐈";
            case "bird": return "🐦";
            case "rabbit": return "🐇";
            case "fish": return "🐠";
            default: return "🐾";
        }
    }
    
    public static void main(String[] args) {
        createAllImages();
    }
}