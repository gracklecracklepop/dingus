import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StoreItem {
    public String id;
    public String name;
    public int price;

    // "hunger", "happiness", "energy", "cosmetic"
    public String effectType;

    // for cosmetic, this can be ignored or used later
    public int effectAmount;

    // cosmetic metadata
    public String slot;   // "hat" (future: "glasses", "bow", etc)
    public String glyph;  // e.g. "🎩"

    public StoreItem() {}

    public StoreItem(String id, String name, int price,
                     String effectType, int effectAmount,
                     String slot, String glyph) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.effectType = effectType;
        this.effectAmount = effectAmount;
        this.slot = slot;
        this.glyph = glyph;
    }
}