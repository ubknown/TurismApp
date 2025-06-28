package com.licentarazu.turismapp.config;

import com.licentarazu.turismapp.model.*;
import com.licentarazu.turismapp.repository.AccommodationUnitRepository;
import com.licentarazu.turismapp.repository.ReservationRepository;
import com.licentarazu.turismapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private AccommodationUnitRepository accommodationUnitRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        // Validate database schema first
        validateSchema();

        // Only seed data if database is empty
        if (userRepository.count() == 0 && accommodationUnitRepository.count() == 0) {
            System.out.println("🌱 Seeding database with realistic test data...");
            seedRealisticData();
            System.out.println("✅ Database seeded successfully with production-like data!");
        } else {
            System.out.println("📋 Database already contains data. Skipping seeding.");
        }
    }

    private void validateSchema() {
        try {
            // Try to access reviews to validate the schema
            System.out.println("🔍 Validating database schema...");

            // This will fail if the schema is incorrect
            userRepository.count(); // Should work
            accommodationUnitRepository.count(); // Should work

            System.out.println("✅ Database schema validation passed!");
        } catch (Exception e) {
            System.err.println("❌ Database schema validation failed: " + e.getMessage());
            System.err.println("💡 Please run the fix_reviews_schema.sql script or enable ddl-auto=update");
            throw new RuntimeException("Database schema validation failed", e);
        }
    }

    private void seedRealisticData() {
        System.out.println("👥 Creating guest users...");
        List<User> guests = createGuestUsers();

        System.out.println("🏠 Creating property owners...");
        List<User> owners = createPropertyOwners();

        System.out.println("🏨 Creating accommodation units...");
        List<AccommodationUnit> units = createAccommodationUnits(owners);

        System.out.println("📅 Creating reservations...");
        createReservations(guests, units);

        System.out.println("📊 Seeding complete - Created:");
        System.out.println("  • " + guests.size() + " guest users");
        System.out.println("  • " + owners.size() + " property owners");
        System.out.println("  • " + units.size() + " accommodation units");
        System.out.println("  • " + reservationRepository.count() + " reservations");
    }

    private List<User> createGuestUsers() {
        String[][] guestData = {
                { "ana.popescu@gmail.com", "Ana", "Popescu" },
                { "ion.gheorghe@yahoo.com", "Ion", "Gheorghe" },
                { "maria.ionescu@outlook.com", "Maria", "Ionescu" },
                { "alexandru.dumitru@gmail.com", "Alexandru", "Dumitru" },
                { "elena.mihai@yahoo.com", "Elena", "Mihai" },
                { "cristian.stefan@gmail.com", "Cristian", "Stefan" },
                { "diana.radu@outlook.com", "Diana", "Radu" },
                { "andrei.popa@gmail.com", "Andrei", "Popa" },
                { "laura.vasile@yahoo.com", "Laura", "Vasile" },
                { "marius.tudor@gmail.com", "Marius", "Tudor" }
        };

        List<User> guests = new ArrayList<>();
        for (String[] data : guestData) {
            User guest = new User();
            guest.setEmail(data[0]);
            guest.setFirstName(data[1]);
            guest.setLastName(data[2]);
            guest.setPassword(passwordEncoder.encode("password123"));
            guest.setRole(Role.GUEST);
            guest.setEnabled(true);
            guest.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(30) + 1));
            guests.add(userRepository.save(guest));
        }
        return guests;
    }

    private List<User> createPropertyOwners() {
        String[][] ownerData = {
                { "victor.constantin@turism.ro", "Victor", "Constantin" },
                { "mihaela.dobre@accomodation.ro", "Mihaela", "Dobre" },
                { "radu.marinescu@properties.ro", "Radu", "Marinescu" },
                { "carmen.georgescu@hotels.ro", "Carmen", "Georgescu" },
                { "daniel.florea@villas.ro", "Daniel", "Florea" }
        };

        List<User> owners = new ArrayList<>();
        for (String[] data : ownerData) {
            User owner = new User();
            owner.setEmail(data[0]);
            owner.setFirstName(data[1]);
            owner.setLastName(data[2]);
            owner.setPassword(passwordEncoder.encode("owner123"));
            owner.setRole(Role.OWNER);
            owner.setEnabled(true);
            owner.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(60) + 30));
            owners.add(userRepository.save(owner));
        }
        return owners;
    }

    private List<AccommodationUnit> createAccommodationUnits(List<User> owners) {
        String[][] unitData = {
                // Owner 1 - Victor Constantin (5 units)
                { "Casa Traditionala Bucuresti",
                        "Casa traditionala renovata in centrul istoric al Bucurestiului cu amenajari moderne",
                        "Strada Lipscani 25", "Bucuresti", "HOUSE", "6", "180.0" },
                { "Apartament Luxury Herastrau", "Apartament de lux cu vedere la parcul Herastrau",
                        "Bulevardul Aviatorilor 40", "Bucuresti", "APARTMENT", "4", "250.0" },
                { "Studio Modern Calea Victoriei", "Studio modern si elegant pe Calea Victoriei", "Calea Victoriei 120",
                        "Bucuresti", "STUDIO", "2", "120.0" },
                { "Vila Primaverii", "Vila eleganta in zona Primaverii cu gradina privata", "Strada Primaverii 15",
                        "Bucuresti", "VILLA", "8", "400.0" },
                { "Penthouse Pipera", "Penthouse cu terasa mare in zona Pipera", "Strada Dimitrie Pompei 10", "Ilfov",
                        "APARTMENT", "6", "320.0" },

                // Owner 2 - Mihaela Dobre (4 units)
                { "Cabana Poiana Brasov", "Cabana rustica cu priveliste la munte in Poiana Brasov",
                        "Strada Poiana Doamnei 8", "Brasov", "COTTAGE", "8", "160.0" },
                { "Apartament Centrul Vechi Cluj", "Apartament in centrul vechi al Cluj-Napocii",
                        "Strada Memorandumului 5", "Cluj", "APARTMENT", "4", "140.0" },
                { "Casa de Vacanta Predeal", "Casa de vacanta ideala pentru iarna si vara", "Strada Cioplea 22",
                        "Brasov", "HOUSE", "10", "200.0" },
                { "Studio Mamaia Nord", "Studio modern la 50m de plaja Mamaia", "Bulevardul Mamaia 200", "Constanta",
                        "STUDIO", "3", "100.0" },

                // Owner 3 - Radu Marinescu (3 units)
                { "Vila Sinaia Centru", "Vila eleganta in centrul Sinaiei, aproape de castel",
                        "Strada Octavian Goga 12", "Prahova", "VILLA", "12", "300.0" },
                { "Apartament Brasov Centru", "Apartament spatios in centrul Brasovului", "Strada Republicii 45",
                        "Brasov", "APARTMENT", "6", "130.0" },
                { "Pensiunea Bucovina", "Pensiune traditionala in inima Bucovinei",
                        "Strada Principala 18, Gura Humorului", "Suceava", "HOUSE", "14", "80.0" },

                // Owner 4 - Carmen Georgescu (4 units)
                { "Hotel Boutique Sibiu", "Hotel boutique in centrul istoric Sibiu", "Piata Mare 8", "Sibiu",
                        "APARTMENT", "4", "170.0" },
                { "Casa Delta Dunarii", "Casa traditionala in Delta Dunarii", "Sat Mila 23", "Tulcea", "HOUSE", "8",
                        "90.0" },
                { "Apartament Timisoara Centru", "Apartament modern in centrul Timisoarei", "Piata Victoriei 12",
                        "Timis", "APARTMENT", "5", "110.0" },
                { "Vila Bran Castle View", "Vila cu priveliste la Castelul Bran", "Strada Castelului 30", "Brasov",
                        "VILLA", "10", "220.0" },

                // Owner 5 - Daniel Florea (3 units)
                { "Pensiunea Maramures", "Pensiune traditionala maramureseana", "Strada Centrala 45, Sighetu Marmatiei",
                        "Maramures", "HOUSE", "12", "85.0" },
                { "Apartament Oradea Centru", "Apartament elegant in centrul Oradiei", "Strada Republicii 25", "Bihor",
                        "APARTMENT", "4", "95.0" },
                { "Cabana Retezat", "Cabana montana in Parcul National Retezat", "Sat Rau de Mori", "Hunedoara",
                        "COTTAGE", "6", "70.0" }
        };

        List<AccommodationUnit> units = new ArrayList<>();
        int ownerIndex = 0;
        int unitsPerOwner = 0;
        int[] maxUnitsPerOwner = { 5, 4, 3, 4, 3 }; // Match the data above

        for (String[] data : unitData) {
            AccommodationUnit unit = new AccommodationUnit();
            unit.setName(data[0]);
            unit.setDescription(data[1]);
            unit.setLocation(data[2] + ", " + data[3]);
            unit.setCapacity(Integer.parseInt(data[5]));
            unit.setPricePerNight(Double.parseDouble(data[6]));
            unit.setType(data[4]);
            unit.setOwner(owners.get(ownerIndex));
            unit.setAvailable(random.nextBoolean() || random.nextBoolean()); // 75% chance available
            unit.setCreatedAt(LocalDate.now().minusDays(random.nextInt(180) + 30)); // 1-6 months ago

            units.add(accommodationUnitRepository.save(unit));

            unitsPerOwner++;
            if (unitsPerOwner >= maxUnitsPerOwner[ownerIndex]) {
                ownerIndex++;
                unitsPerOwner = 0;
            }
        }

        return units;
    }

    private void createReservations(List<User> guests, List<AccommodationUnit> units) {
        List<Reservation> reservations = new ArrayList<>();

        // Create 25-30 realistic reservations
        for (int i = 0; i < 28; i++) {
            Reservation reservation = new Reservation();

            // Random guest and unit
            User guest = guests.get(random.nextInt(guests.size()));
            AccommodationUnit unit = units.get(random.nextInt(units.size()));

            // Random dates (mix of past, current, and future)
            LocalDate baseDate;
            if (i < 10) {
                // Past reservations (1-60 days ago)
                baseDate = LocalDate.now().minusDays(random.nextInt(60) + 1);
            } else if (i < 18) {
                // Current/near future (next 30 days)
                baseDate = LocalDate.now().plusDays(random.nextInt(30));
            } else {
                // Future reservations (1-120 days from now)
                baseDate = LocalDate.now().plusDays(random.nextInt(120) + 30);
            }

            int duration = random.nextInt(6) + 2; // 2-7 nights
            LocalDate startDate = baseDate;
            LocalDate endDate = startDate.plusDays(duration);

            reservation.setUnit(unit);
            reservation.setUser(guest);
            reservation.setStartDate(startDate);
            reservation.setEndDate(endDate);
            reservation.setNumberOfGuests(random.nextInt(unit.getCapacity() - 1) + 1);

            // Realistic status distribution
            if (startDate.isBefore(LocalDate.now())) {
                // Past reservations - mostly confirmed, some cancelled
                reservation.setStatus(
                        random.nextDouble() < 0.85 ? ReservationStatus.CONFIRMED : ReservationStatus.CANCELLED);
            } else {
                // Future reservations - mix of all statuses
                double statusRand = random.nextDouble();
                if (statusRand < 0.7) {
                    reservation.setStatus(ReservationStatus.CONFIRMED);
                } else if (statusRand < 0.85) {
                    reservation.setStatus(ReservationStatus.PENDING);
                } else {
                    reservation.setStatus(ReservationStatus.CANCELLED);
                }
            }

            reservations.add(reservation);
        }

        reservationRepository.saveAll(reservations);
    }
}
