package com.municipal.config;

import com.municipal.entity.*;
import com.municipal.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ComplaintStatusHistoryRepository historyRepository;

    @Autowired
    private ComplaintAssignmentRepository assignmentRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedCategories();
        seedDepartments();
        seedUsersAndComplaints();
    }

    private void seedCategories() {
        if (categoryRepository.count() == 0) {
            List<Category> categories = Arrays.asList(
                    new Category("Garbage & Sanitation", "Overflowing bins, illegal waste dumping, and cleaning needs", "Trash2"),
                    new Category("Roads & Potholes", "Damaged road surfaces, dangerous potholes, and road dividers", "Car"),
                    new Category("Drainage & Sewage", "Clogged storm drains, overflowing manholes, and sewage leaks", "Waves"),
                    new Category("Street Lighting", "Non-functional streetlights, broken poles, and exposed wiring", "Lightbulb"),
                    new Category("Water Supply", "Broken pipelines, contaminated drinking water, and low pressure", "Droplets"),
                    new Category("Public Maintenance", "Damaged park benches, broken playground equipment, and public garden upkeep", "Trees")
            );
            categoryRepository.saveAll(categories);
            logger.info("Default categories seeded.");
        }
    }

    private void seedDepartments() {
        if (departmentRepository.count() == 0) {
            List<Department> departments = Arrays.asList(
                    new Department("Solid Waste Management", "SWM", "waste.mgmt@municipal.gov", "+91 98765 43210"),
                    new Department("Roads & Public Infrastructure", "RPI", "roads.dept@municipal.gov", "+91 98765 43211"),
                    new Department("Water Supply & Drainage", "WSD", "water.drainage@municipal.gov", "+91 98765 43212"),
                    new Department("Electrical & Streetlights", "ELE", "streetlights@municipal.gov", "+91 98765 43213"),
                    new Department("Horticulture & Parks", "HORT", "parks.maint@municipal.gov", "+91 98765 43214"),
                    new Department("Sanitation & Health", "SAN", "health.sanitation@municipal.gov", "+91 98765 43215")
            );
            departmentRepository.saveAll(departments);
            logger.info("Default departments seeded.");
        }
    }

    private void seedUsersAndComplaints() {
        // Ensure Admin exists
        User admin = userRepository.findByEmail("admin@municipal.gov").orElseGet(() -> {
            User newAdmin = new User(
                    "City Municipal Commissioner",
                    "admin@municipal.gov",
                    passwordEncoder.encode("Admin@123"),
                    "+91 98765 00001",
                    "Municipal Corporation HQ, Civic Center, Sector 1",
                    Role.ROLE_ADMIN
            );
            return userRepository.save(newAdmin);
        });

        // Ensure Citizens exist
        User citizen1 = userRepository.findByEmail("rajesh.kumar@example.com").orElseGet(() -> {
            User newCitizen = new User(
                    "Rajesh Kumar",
                    "rajesh.kumar@example.com",
                    passwordEncoder.encode("Citizen@123"),
                    "+91 98765 11111",
                    "Flat 402, Shanti Niketan, Lake Road, Ward 12",
                    Role.ROLE_CITIZEN
            );
            return userRepository.save(newCitizen);
        });

        User citizen2 = userRepository.findByEmail("priya.sharma@example.com").orElseGet(() -> {
            User newCitizen = new User(
                    "Priya Sharma",
                    "priya.sharma@example.com",
                    passwordEncoder.encode("Citizen@123"),
                    "+91 98765 22222",
                    "House 15, Green Park Avenue, Ward 4",
                    Role.ROLE_CITIZEN
            );
            return userRepository.save(newCitizen);
        });

        // Seed sample complaints if table is empty
        if (complaintRepository.count() == 0) {
            Category roadCat = categoryRepository.findByName("Roads & Potholes").orElse(null);
            Category wasteCat = categoryRepository.findByName("Garbage & Sanitation").orElse(null);
            Category lightCat = categoryRepository.findByName("Street Lighting").orElse(null);
            Category drainCat = categoryRepository.findByName("Drainage & Sewage").orElse(null);
            Category waterCat = categoryRepository.findByName("Water Supply").orElse(null);

            Department roadsDept = departmentRepository.findByCode("RPI").orElse(null);
            Department wasteDept = departmentRepository.findByCode("SWM").orElse(null);
            Department lightDept = departmentRepository.findByCode("ELE").orElse(null);
            Department waterDept = departmentRepository.findByCode("WSD").orElse(null);

            // Complaint 1: Assigned
            Complaint c1 = new Complaint();
            c1.setComplaintNumber("MUNIC-2026-0001");
            c1.setTitle("Severe Pothole Cluster on 5th Main Crossroad");
            c1.setDescription("Deep potholes causing serious hazard for two-wheelers and pedestrians near the intersection of 5th Main and 8th Cross.");
            c1.setCategory(roadCat);
            c1.setDepartment(roadsDept);
            c1.setCitizen(citizen1);
            c1.setStatus(ComplaintStatus.ASSIGNED);
            c1.setPriority(ComplaintPriority.HIGH);
            c1.setLocality("Indiranagar");
            c1.setAddress("5th Main, 8th Cross Road, Ward 72");
            c1.setLandmark("Opposite Metro Station Pillar 140");
            Complaint savedC1 = complaintRepository.save(c1);

            historyRepository.save(new ComplaintStatusHistory(savedC1, null, ComplaintStatus.SUBMITTED, citizen1, "Complaint submitted"));
            historyRepository.save(new ComplaintStatusHistory(savedC1, ComplaintStatus.SUBMITTED, ComplaintStatus.ASSIGNED, admin, "Assigned to Roads & Infrastructure team"));
            assignmentRepository.save(new ComplaintAssignment(savedC1, roadsDept, "Inspector V. Murthy", "Inspection team dispatched for road patch work", admin));

            // Complaint 2: In Progress
            Complaint c2 = new Complaint();
            c2.setComplaintNumber("MUNIC-2026-0002");
            c2.setTitle("Overflowing Garbage Dump near Community Market");
            c2.setDescription("Waste has not been collected for three days, attracting stray animals and creating strong odor near the grocery market.");
            c2.setCategory(wasteCat);
            c2.setDepartment(wasteDept);
            c2.setCitizen(citizen1);
            c2.setStatus(ComplaintStatus.IN_PROGRESS);
            c2.setPriority(ComplaintPriority.URGENT);
            c2.setLocality("Koramangala");
            c2.setAddress("Block 4, 80 Feet Road, Near Vegetable Market");
            c2.setLandmark("Next to City Bank ATM");
            Complaint savedC2 = complaintRepository.save(c2);

            historyRepository.save(new ComplaintStatusHistory(savedC2, null, ComplaintStatus.SUBMITTED, citizen1, "Complaint submitted"));
            historyRepository.save(new ComplaintStatusHistory(savedC2, ComplaintStatus.SUBMITTED, ComplaintStatus.IN_PROGRESS, admin, "Waste collection truck deployed"));
            assignmentRepository.save(new ComplaintAssignment(savedC2, wasteDept, "Supervisor S. Patil", "Waste clearance in progress with hydraulic truck", admin));

            // Complaint 3: Resolved with Feedback
            Complaint c3 = new Complaint();
            c3.setComplaintNumber("MUNIC-2026-0003");
            c3.setTitle("Non-functional Streetlights on Park Avenue");
            c3.setDescription("Four consecutive streetlights have been completely dark for over a week, making the stretch unsafe for women and evening walkers.");
            c3.setCategory(lightCat);
            c3.setDepartment(lightDept);
            c3.setCitizen(citizen2);
            c3.setStatus(ComplaintStatus.RESOLVED);
            c3.setPriority(ComplaintPriority.MEDIUM);
            c3.setLocality("Jayanagar");
            c3.setAddress("Park Avenue, 3rd Block");
            c3.setLandmark("Near Shanti Park Gate 2");
            c3.setResolutionDetails("All 4 LED fixtures and damaged feeder lines replaced. Lights tested and verified functioning.");
            c3.setResolvedAt(LocalDateTime.now().minusDays(1));
            Complaint savedC3 = complaintRepository.save(c3);

            historyRepository.save(new ComplaintStatusHistory(savedC3, null, ComplaintStatus.SUBMITTED, citizen2, "Complaint submitted"));
            historyRepository.save(new ComplaintStatusHistory(savedC3, ComplaintStatus.SUBMITTED, ComplaintStatus.ASSIGNED, admin, "Assigned to Electrical maintenance"));
            historyRepository.save(new ComplaintStatusHistory(savedC3, ComplaintStatus.ASSIGNED, ComplaintStatus.RESOLVED, admin, "Issue resolved and lights tested"));
            assignmentRepository.save(new ComplaintAssignment(savedC3, lightDept, "Technician R. Nair", "Replaced LED drivers and restored illumination", admin));

            Feedback feedback = new Feedback(savedC3, citizen2, 5, "Very swift resolution within 24 hours. The area is bright and safe now. Thank you!");
            feedbackRepository.save(feedback);

            // Complaint 4: Submitted
            Complaint c4 = new Complaint();
            c4.setComplaintNumber("MUNIC-2026-0004");
            c4.setTitle("Clogged Stormwater Drain Causing Waterlogging");
            c4.setDescription("Plastic and silt clogging the main drain causing water accumulation outside residential buildings.");
            c4.setCategory(drainCat);
            c4.setCitizen(citizen2);
            c4.setStatus(ComplaintStatus.SUBMITTED);
            c4.setPriority(ComplaintPriority.HIGH);
            c4.setLocality("Whitefield");
            c4.setAddress("Inner Circle Road, Sector 3");
            c4.setLandmark("Opposite Heritage Complex");
            Complaint savedC4 = complaintRepository.save(c4);
            historyRepository.save(new ComplaintStatusHistory(savedC4, null, ComplaintStatus.SUBMITTED, citizen2, "Complaint submitted"));

            // Complaint 5: Under Review
            Complaint c5 = new Complaint();
            c5.setComplaintNumber("MUNIC-2026-0005");
            c5.setTitle("Low Drinking Water Pressure and Pipeline Leakage");
            c5.setDescription("Water pressure is extremely weak and clean drinking water is leaking from underground joint on the main pipeline.");
            c5.setCategory(waterCat);
            c5.setDepartment(waterDept);
            c5.setCitizen(citizen1);
            c5.setStatus(ComplaintStatus.UNDER_REVIEW);
            c5.setPriority(ComplaintPriority.MEDIUM);
            c5.setLocality("Malleshwaram");
            c5.setAddress("11th Cross, Margosa Road");
            c5.setLandmark("Near Post Office");
            Complaint savedC5 = complaintRepository.save(c5);
            historyRepository.save(new ComplaintStatusHistory(savedC5, null, ComplaintStatus.SUBMITTED, citizen1, "Complaint submitted"));
            historyRepository.save(new ComplaintStatusHistory(savedC5, ComplaintStatus.SUBMITTED, ComplaintStatus.UNDER_REVIEW, admin, "Under technical evaluation by pipeline division"));

            logger.info("Sample complaints and status histories seeded successfully.");
        }
    }
}
