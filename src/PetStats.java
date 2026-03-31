import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PetStats {
    public int hunger    = 100;  // 0–100
    public int happiness = 100;
    public int energy    = 100;
    public int coins     = 0;
    public List<String> inventory = new ArrayList<>();

    public void addHunger(int delta)    { hunger    = clamp(hunger    + delta); }
    public void addHappiness(int delta) { happiness = clamp(happiness + delta); }
    public void addEnergy(int delta)    { energy    = clamp(energy    + delta); }
    public void addCoins(int amount)    { coins     = Math.max(0, coins + amount); }

    public boolean canAfford(int price) { return coins >= price; }

    public boolean buyItem(StoreItem item) {
        if (!canAfford(item.price) || inventory.contains(item.id)) return false;
        coins -= item.price;
        inventory.add(item.id);
        printStats();
        return true;
    }

    /** Prints current stats as a JSON object to stdout. */
    public void printStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"hunger\": ")    .append(hunger)    .append(",\n");
        sb.append("  \"happiness\": ") .append(happiness) .append(",\n");
        sb.append("  \"energy\": ")    .append(energy)    .append(",\n");
        sb.append("  \"coins\": ")     .append(coins)     .append(",\n");
        sb.append("  \"inventory\": [");
        for (int i = 0; i < inventory.size(); i++) {
            sb.append("\"").append(inventory.get(i)).append("\"");
            if (i < inventory.size() - 1) sb.append(", ");
        }
        sb.append("]\n}");
        System.out.println(sb);
    }

    private int clamp(int v) { return Math.max(0, Math.min(100, v)); }
}