import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PetStats {
    public int hunger    = 100;  // 0–100
    public int happiness = 100;
    public int energy    = 100;
    public int coins     = 0;
    public List<String> inventory = new ArrayList<>(); // item IDs owned

    // Convenience mutators — clamp values to 0–100
    public void addHunger(int delta)    { hunger    = clamp(hunger    + delta); }
    public void addHappiness(int delta) { happiness = clamp(happiness + delta); }
    public void addEnergy(int delta)    { energy    = clamp(energy    + delta); }
    public void addCoins(int amount)    { coins     = Math.max(0, coins + amount); }

    public boolean canAfford(int price) { return coins >= price; }

    public boolean buyItem(StoreItem item) {
        if (!canAfford(item.price) || inventory.contains(item.id)) return false;
        coins -= item.price;
        inventory.add(item.id);
        return true;
    }

    private int clamp(int v) { return Math.max(0, Math.min(100, v)); }
}