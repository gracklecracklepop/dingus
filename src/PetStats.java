import java.util.HashSet;
import java.util.Set;

public class PetStats {

    public int hunger = 67;
    public int happiness = 100;
    public int energy = 100;
    public int coins = 0;

    // --- WIZARD VARIABLES ---
    private String name = "Dingus";
    private String gender = "Robot";
    private String spriteColor = "Default (Orange)";
    private static long baseRam = 0;
    private double baseCpu = 0; // NEW: CPU baseline percentage

    // --- INVENTORY ---
    private Set<String> ownedAccessories = new HashSet<>();

    // Jackson requires empty constructor
    public PetStats() {}

    // --- Standard Getters & Setters ---

    public int getHunger() { return hunger; }
    public int getHappiness() { return happiness; }
    public int getEnergy() { return energy; }
    public int getCoins() { return coins; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getSpriteColor() { return spriteColor; }
    public void setSpriteColor(String spriteColor) { this.spriteColor = spriteColor; }

    public static long getBaseRam() { return baseRam; }
    public void setBaseRam(long baseRam) { this.baseRam = baseRam; }

    public double getBaseCpu() { return baseCpu; }
    public void setBaseCpu(double baseCpu) { this.baseCpu = baseCpu; }

    public void setHunger(int hunger) { this.hunger = clamp(hunger); }
    public void setHappiness(int happiness) { this.happiness = clamp(happiness); }
    public void setEnergy(int energy) { this.energy = clamp(energy); }
    public void setCoins(int coins) { this.coins = Math.max(0, coins); }

    // --- Inventory Getters/Setters ---
    public Set<String> getOwnedAccessories() { return ownedAccessories; }
    public void setOwnedAccessories(Set<String> ownedAccessories) { this.ownedAccessories = ownedAccessories; }

    public void addAccessory(String itemId) {
        this.ownedAccessories.add(itemId);
    }

    public boolean ownsAccessory(String itemId) {
        return this.ownedAccessories.contains(itemId);
    }

    // --- Action Adders ---

    public void addHunger(int amount) {
        hunger = clamp(hunger + amount);
    }

    public void addHappiness(int amount) {
        happiness = clamp(happiness + amount);
    }

    public void addEnergy(int amount) {
        energy = clamp(energy + amount);
    }

    public void addCoins(int amount) {
        coins = Math.max(0, coins + amount);
    }

    // ── Helper ──

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    public void printStats() {
        System.out.println("══════════════════════");
        System.out.println("Name:         " + name);
        System.out.println("Hunger:       " + hunger + "%");
        System.out.println("Happiness:    " + happiness + "%");
        System.out.println("Energy:       " + energy + "%");
        System.out.println("Coins:        " + coins);
        System.out.println("Base RAM:     " + baseRam + " MB");
        System.out.println("Base CPU:     " + String.format("%.1f", baseCpu) + "%");
        System.out.println("Accessories:  " + ownedAccessories.size() + " owned");
        System.out.println("══════════════════════");
    }
}