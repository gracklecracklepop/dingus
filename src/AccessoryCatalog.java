import java.util.ArrayList;
import java.util.List;

public class AccessoryCatalog {

    // Keep IDs stable (they are saved in JSON)
    public static final StoreItem[] SHOP_ITEMS = {
            new StoreItem("hat_top",    "Top Hat",     30, "cosmetic", 0, "hat", "🎩"),
            new StoreItem("hat_crown",  "Crown",       80, "cosmetic", 0, "hat", "👑"),
            new StoreItem("hat_cap",    "Cap",         25, "cosmetic", 0, "hat", "🧢"),
            new StoreItem("hat_bow",    "Bow",         20, "cosmetic", 0, "hat", "🎀"),
            new StoreItem("hat_flower", "Flower",      15, "cosmetic", 0, "hat", "🌸"),
            new StoreItem("hat_headphones", "Headphones", 60, "cosmetic", 0, "hat", "🎧"),
    };

    public static List<StoreItem> ownedHats(PetStats stats) {
        List<StoreItem> hats = new ArrayList<>();
        for (StoreItem it : SHOP_ITEMS) {
            if (!"hat".equals(it.slot)) continue;
            if (stats.ownsAccessory(it.id)) hats.add(it);
        }
        return hats;
    }

    public static StoreItem byId(String id) {
        if (id == null) return null;
        for (StoreItem it : SHOP_ITEMS) {
            if (id.equals(it.id)) return it;
        }
        return null;
    }
}