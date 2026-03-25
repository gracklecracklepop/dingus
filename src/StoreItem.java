import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StoreItem {
    public String id;
    public String name;
    public int price;
    public String effectType;  // "hunger", "happiness", "energy", "cosmetic"
    public int effectAmount;

    // Required by Jackson for deserialization
    public StoreItem() {}

    public StoreItem(String id, String name, int price, String effectType, int effectAmount) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.effectType = effectType;
        this.effectAmount = effectAmount;
    }
}