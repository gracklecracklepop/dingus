public class measureing {
    public static void main(String[] args) {
        boolean saveExists = SaveManager.saveExists();
        System.out.println("Save file exists: " + saveExists);
    }
}