public class PetStats {

    public int hunger = 100;
    public int happiness = 100;
    public int energy = 100;
    public int coins = 0;

    public PetStats() {}

    public int getHunger() { return hunger; }
    public int getHappiness() { return happiness; }
    public int getEnergy() { return energy; }
    public int getCoins() { return coins; }

    // --- NEW WIZARD VARIABLES ---
    private String name = "Dingus";
    private String gender = "Robot";
    private String spriteColor = "Default (Orange)";
    private long baseRam = 0;

    // Jackson requires empty constructor
    // --- Add Standard Getters & Setters for everything ---

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getSpriteColor() { return spriteColor; }
    public void setSpriteColor(String spriteColor) { this.spriteColor = spriteColor; }

    public long getBaseRam() { return baseRam; }
    public void setBaseRam(long baseRam) { this.baseRam = baseRam; }
    public void setHunger(int hunger) { this.hunger = clamp(hunger); }
    public void setHappiness(int happiness) { this.happiness = clamp(happiness); }
    public void setEnergy(int energy) { this.energy = clamp(energy); }
    public void setCoins(int coins) { this.coins = Math.max(0, coins); }


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
        System.out.println("Hunger:    " + hunger + "%");
        System.out.println("Happiness: " + happiness + "%");
        System.out.println("Energy:    " + energy + "%");
        System.out.println("Coins:     " + coins);
        System.out.println("══════════════════════");
    }
}