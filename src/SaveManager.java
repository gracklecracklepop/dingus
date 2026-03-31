
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SaveManager {


    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    // Saves to ~/.dingus/save_data.json so it survives across runs
    private static final Path SAVE_DIR  = Paths.get(System.getProperty("user.home"), ".dingus");
    private static final Path SAVE_FILE = SAVE_DIR.resolve("save_data.json");

    public static boolean saveExists() {
        return SAVE_FILE.toFile().exists();
    }


    public static void save(PetStats stats) {
        try {
            Files.createDirectories(SAVE_DIR);
            MAPPER.writeValue(SAVE_FILE.toFile(), stats);
            System.out.println("Saved to " + SAVE_FILE);
        } catch (IOException e) {
            System.err.println("Failed to save: " + e.getMessage());
        }
    }

    public static PetStats load() {
        File f = SAVE_FILE.toFile();
        if (!f.exists()) {
            System.out.println("No save file found — starting fresh.");
            return new PetStats();
        }
        try {
            return MAPPER.readValue(f, PetStats.class);
        } catch (IOException e) {
            System.err.println("Corrupt save — starting fresh: " + e.getMessage());
            return new PetStats();
        }
    }
    public boolean isReturningPet = SaveManager.saveExists();
}