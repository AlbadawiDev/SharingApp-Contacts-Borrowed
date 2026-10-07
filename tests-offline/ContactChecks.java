import com.example.sharingapp.Contact;
import com.example.sharingapp.ContactList;
import java.util.HashSet;
import java.util.Locale;

/** Pure model checks. Android SDK jar is only a compile-time classpath. */
public class ContactChecks {
    private static int checks = 0;
    private static void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
        checks++;
    }
    public static void main(String[] args) {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            Contact upper = new Contact("I", "demo@example.test");
            Contact lower = new Contact("i", "other@example.test");
            check(upper.equals(lower), "case-insensitive identity");
            check(upper.hashCode() == lower.hashCode(), "locale-independent hash contract");
            HashSet<Contact> set = new HashSet<Contact>();
            set.add(upper); set.add(lower);
            check(set.size() == 1, "deduplicate contacts in hash collections");
            Contact dotted = new Contact("\u0130", "demo@example.test");
            check(dotted.equals(lower) && dotted.hashCode() == lower.hashCode(), "Unicode identity/hash contract");
            Contact contact = new Contact(" Alice ", " demo@example.test ");
            check(contact.getUsername().equals("Alice"), "username trim");
            check(contact.getEmail().equals("demo@example.test"), "email trim");
            ContactList list = new ContactList();
            check(list.addContact(contact), "first contact added");
            check(!list.isUsernameAvailable(" alice "), "trimmed duplicates rejected");
            check(!list.isUsernameAvailable("  "), "blank username rejected");
            check(!list.addContact(new Contact("ALICE", "other@example.test")), "case duplicate rejected");
            check(list.size() == 1, "duplicates preserve original");
            boolean rejected = false;
            try { new Contact("  ", "demo@example.test"); }
            catch (IllegalArgumentException expected) { rejected = true; }
            check(rejected, "empty contact rejected");
            System.out.println("PASS: " + checks + " offline model checks");
        } finally { Locale.setDefault(previous); }
    }
}
