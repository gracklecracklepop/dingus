public class PetStats {

    public int hunger = 100;
    public int happiness = 100;
    public int energy = 100;
    public int coins = 0;

    // Default constructor required for Jackson
    public PetStats() {}

    // ── Getters (required for Jackson deserialization) ──

    public int getHunger() { return hunger; }
    public int getHappiness() { return happiness; }
    public int getEnergy() { return energy; }
    public int getCoins() { return coins; }

    // ── Setters (required for Jackson deserialization) ──

    public void setHunger(int hunger) { this.hunger = clamp(hunger); }
    public void setHappiness(int happiness) { this.happiness = clamp(happiness); }
    public void setEnergy(int energy) { this.energy = clamp(energy); }
    public void setCoins(int coins) { this.coins = Math.max(0, coins); }

    // ── Modifiers ──

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