package com.campusswap.config;

import com.campusswap.model.Item;
import com.campusswap.model.User;
import com.campusswap.repository.ItemRepository;
import com.campusswap.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the demo account from the frontend README (student@campus.edu / Password123!)
 * plus a second student with a few listings, so the dashboard isn't empty on first run.
 * Only runs when the users table is empty. Disable with app.seed-demo-data=false.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;

    public DataSeeder(UserRepository userRepository,
                      ItemRepository itemRepository,
                      PasswordEncoder passwordEncoder,
                      @Value("${app.seed-demo-data:false}") boolean enabled) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
    }

    @Override
    public void run(String... args) {
        if (!enabled || userRepository.count() > 0) {
            return;
        }

        User abhay = createUser("Abhay Sharma", "student@campus.edu", "CS2023041",
                "Computer Science", "3rd Year", "+91 98765 43210");
        User riya = createUser("Riya Verma", "riya@campus.edu", "CS2023077",
                "Information Technology", "2nd Year", "+91 91234 56789");

        createItem(riya, "Data Structures & Algorithms Textbook", "Books",
                "Cormen, 3rd edition. Light highlighting in the first few chapters.",
                "Good", "Library Block, Gate 2", "Borrow");
        createItem(riya, "Casio fx-991EX Calculator", "Calculators",
                "Works perfectly, comes with the protective cover.",
                "Like New", "Main Canteen", "Sell");
        createItem(abhay, "Digital Electronics Lab Kit", "Lab Equipment",
                "Breadboard, ICs and jumper wires. Great for the 4th semester lab.",
                "Good", "Hostel B Common Room", "Exchange");

        log.info("Seeded demo data. Demo login: student@campus.edu / Password123!");
    }

    private User createUser(String name, String email, String collegeId,
                            String branch, String year, String phone) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode("Password123!"));
        u.setCollegeId(collegeId);
        u.setBranch(branch);
        u.setYear(year);
        u.setPhone(phone);
        return userRepository.save(u);
    }

    private void createItem(User owner, String name, String category, String description,
                            String condition, String location, String listingType) {
        Item i = new Item();
        i.setOwner(owner);
        i.setName(name);
        i.setCategory(category);
        i.setDescription(description);
        i.setCondition(condition);
        i.setLocation(location);
        i.setListingType(listingType);
        itemRepository.save(i);
    }
}
