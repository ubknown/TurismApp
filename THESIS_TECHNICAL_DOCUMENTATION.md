# TurismApp - Technical Documentation for Thesis

## 1. Introduction

### 1.1 Project Overview
The Tourist Accommodation Management System (TurismApp) is a comprehensive web-based platform designed to facilitate the management and booking of tourist accommodations. The system serves three primary user roles: Clients (tourists), Property Owners, and Administrators, each with distinct functionalities and access levels.

### 1.2 Problem Statement
The traditional tourist accommodation booking process often involves:
- Limited search and filtering capabilities
- Manual booking approval processes
- Lack of centralized management for property owners
- Insufficient analytics and reporting tools
- Poor communication between stakeholders

### 1.3 Solution Approach
TurismApp addresses these challenges by providing:
- Advanced search and filtering mechanisms
- Automated booking workflows with approval systems
- Comprehensive property management tools
- Real-time analytics and profit tracking
- Integrated communication through email notifications
- AI-enhanced features for improved user experience

## 2. System Architecture

### 2.1 Overall Architecture
The system follows a three-tier architecture pattern:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Presentation  │    │    Business     │    │   Data Access   │
│     Layer       │◄──►│     Logic       │◄──►│     Layer       │
│   (React.js)    │    │  (Spring Boot)  │    │    (MySQL)      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 2.2 Frontend Architecture
- **Framework**: React.js with functional components and hooks
- **State Management**: React hooks (useState, useEffect, useContext)
- **Routing**: React Router for single-page application navigation
- **Styling**: CSS modules with responsive design principles
- **HTTP Client**: Axios for API communication

### 2.3 Backend Architecture
- **Framework**: Spring Boot with Java
- **Security**: Spring Security with JWT authentication
- **Data Access**: Spring Data JPA with Hibernate ORM
- **Email Service**: JavaMail API with SMTP configuration
- **Build Tool**: Maven for dependency management

### 2.4 Database Architecture
- **Database**: MySQL relational database
- **ORM**: Hibernate for object-relational mapping
- **Connection Pooling**: HikariCP for efficient database connections
- **Schema Management**: JPA annotations with automatic DDL generation

## 3. Detailed System Design

### 3.1 Entity Relationship Model

```
Users (1) ←→ (*) Bookings (*) ←→ (1) AccommodationUnits
  │                                        │
  │ (1)                                    │ (1)
  ↓                                        ↓
  (*) OwnerApplications                    (*) Reviews
                                           ↑
                                           │ (*)
                                         Users (1)
```

### 3.2 Core Entities

#### 3.2.1 User Entity
```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String email;
    
    private String password; // BCrypt hashed
    private String firstName;
    private String lastName;
    private String phoneNumber;
    
    @Enumerated(EnumType.STRING)
    private Role role; // CLIENT, OWNER, ADMIN
    
    // Relationships
    @OneToMany(mappedBy = "user")
    private List<Booking> bookings;
    
    @OneToMany(mappedBy = "owner")
    private List<AccommodationUnit> accommodationUnits;
}
```

#### 3.2.2 AccommodationUnit Entity
```java
@Entity
public class AccommodationUnit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String description;
    private String location;
    private String county;
    private BigDecimal pricePerNight;
    private Integer maxGuests;
    
    @Enumerated(EnumType.STRING)
    private AccommodationType type; // HOTEL, APARTMENT, VILLA, etc.
    
    private Boolean available = true;
    
    @ManyToOne
    private User owner;
    
    @OneToMany(mappedBy = "accommodationUnit")
    private List<Booking> bookings;
    
    @OneToMany(mappedBy = "accommodationUnit")
    private List<Review> reviews;
}
```

#### 3.2.3 Booking Entity
```java
@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private User user;
    
    @ManyToOne
    private AccommodationUnit accommodationUnit;
    
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfGuests;
    private BigDecimal totalPrice;
    
    @Enumerated(EnumType.STRING)
    private BookingStatus status; // PENDING, APPROVED, REJECTED, CANCELLED
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 3.3 Security Implementation

#### 3.3.1 JWT Authentication Flow
```
1. User Login → Authentication Controller
2. Validate Credentials → User Service
3. Generate JWT Token → JWT Utility
4. Return Token → Client
5. Include Token in Headers → All Subsequent Requests
6. Validate Token → JWT Authentication Filter
7. Set Security Context → Spring Security
```

#### 3.3.2 Security Configuration
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/owner/**").hasRole("OWNER")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

### 3.4 Advanced Filtering System

#### 3.4.1 Frontend Filtering Implementation
The filtering system in the frontend uses controlled components with debounced input to prevent excessive API calls:

```javascript
const UnitsListPage = () => {
    const [filters, setFilters] = useState({
        location: '',
        county: '',
        type: '',
        minPrice: '',
        maxPrice: '',
        minGuests: '',
        rating: ''
    });
    
    const [accommodationUnits, setAccommodationUnits] = useState([]);
    
    const handleSearch = () => {
        fetchAccommodationUnits(filters);
    };
    
    const fetchAccommodationUnits = async (filterParams) => {
        try {
            const params = new URLSearchParams();
            Object.entries(filterParams).forEach(([key, value]) => {
                if (value && value.toString().trim() !== '') {
                    params.append(key, value);
                }
            });
            
            const response = await api.get(`/accommodations/filter?${params.toString()}`);
            setAccommodationUnits(response.data);
        } catch (error) {
            console.error('Error fetching accommodation units:', error);
        }
    };
};
```

#### 3.4.2 Backend Filtering Implementation
The backend filtering uses dynamic query building with JPA Criteria API for flexible filtering:

```java
@Service
public class AccommodationUnitService {
    
    public List<AccommodationUnit> filterAccommodationUnits(
            String location, String county, String type, 
            BigDecimal minPrice, BigDecimal maxPrice, 
            Integer minGuests, Double rating) {
        
        logger.info("Filtering with parameters: location={}, county={}, type={}, " +
                   "minPrice={}, maxPrice={}, minGuests={}, rating={}", 
                   location, county, type, minPrice, maxPrice, minGuests, rating);
        
        return accommodationUnitRepository.findByFilters(
            location, county, type, minPrice, maxPrice, minGuests, rating);
    }
}

@Repository
public interface AccommodationUnitRepository extends JpaRepository<AccommodationUnit, Long> {
    
    @Query("SELECT DISTINCT au FROM AccommodationUnit au " +
           "LEFT JOIN au.reviews r " +
           "WHERE (:location IS NULL OR :location = '' OR LOWER(au.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "AND (:county IS NULL OR :county = '' OR LOWER(au.county) LIKE LOWER(CONCAT('%', :county, '%'))) " +
           "AND (:type IS NULL OR :type = '' OR au.type = :type) " +
           "AND (:minPrice IS NULL OR au.pricePerNight >= :minPrice) " +
           "AND (:maxPrice IS NULL OR au.pricePerNight <= :maxPrice) " +
           "AND (:minGuests IS NULL OR au.maxGuests >= :minGuests) " +
           "AND (:rating IS NULL OR au.id IN " +
           "    (SELECT r2.accommodationUnit.id FROM Review r2 " +
           "     GROUP BY r2.accommodationUnit.id " +
           "     HAVING AVG(r2.rating) >= :rating)) " +
           "AND au.available = true")
    List<AccommodationUnit> findByFilters(
        @Param("location") String location,
        @Param("county") String county, 
        @Param("type") String type,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("minGuests") Integer minGuests,
        @Param("rating") Double rating
    );
}
```

### 3.5 Booking System Architecture

#### 3.5.1 Booking Workflow
```
1. Client selects accommodation and dates
2. System checks availability
3. Booking request created with PENDING status
4. Email notification sent to property owner
5. Owner reviews and approves/rejects booking
6. Email notification sent to client
7. Booking status updated accordingly
8. Payment processing (if approved)
```

#### 3.5.2 Booking Service Implementation
```java
@Service
@Transactional
public class BookingService {
    
    public Booking createBooking(BookingDTO bookingDTO) {
        // Validate availability
        if (!isAccommodationAvailable(bookingDTO.getAccommodationUnitId(), 
                                     bookingDTO.getCheckInDate(), 
                                     bookingDTO.getCheckOutDate())) {
            throw new BookingException("Accommodation not available for selected dates");
        }
        
        // Create booking
        Booking booking = new Booking();
        booking.setUser(getCurrentUser());
        booking.setAccommodationUnit(getAccommodationUnit(bookingDTO.getAccommodationUnitId()));
        booking.setCheckInDate(bookingDTO.getCheckInDate());
        booking.setCheckOutDate(bookingDTO.getCheckOutDate());
        booking.setNumberOfGuests(bookingDTO.getNumberOfGuests());
        booking.setTotalPrice(calculateTotalPrice(booking));
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        
        Booking savedBooking = bookingRepository.save(booking);
        
        // Send notification emails
        emailService.sendBookingConfirmationToClient(savedBooking);
        emailService.sendBookingNotificationToOwner(savedBooking);
        
        return savedBooking;
    }
    
    public Booking approveBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BookingNotFoundException("Booking not found"));
        
        booking.setStatus(BookingStatus.APPROVED);
        booking.setUpdatedAt(LocalDateTime.now());
        
        Booking savedBooking = bookingRepository.save(booking);
        
        emailService.sendBookingApprovalToClient(savedBooking);
        
        return savedBooking;
    }
}
```

### 3.6 Email Notification System

#### 3.6.1 Email Service Architecture
```java
@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${app.email.from}")
    private String fromEmail;
    
    public void sendBookingConfirmationToClient(Booking booking) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(booking.getUser().getEmail());
            helper.setSubject("Booking Confirmation - " + booking.getAccommodationUnit().getTitle());
            
            String htmlContent = buildBookingConfirmationEmail(booking);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
            logger.info("Booking confirmation email sent to: {}", booking.getUser().getEmail());
        } catch (Exception e) {
            logger.error("Failed to send booking confirmation email", e);
        }
    }
    
    private String buildBookingConfirmationEmail(Booking booking) {
        return """
            <html>
            <body>
                <h2>Booking Confirmation</h2>
                <p>Dear %s,</p>
                <p>Your booking request has been received and is pending approval.</p>
                <div style="border: 1px solid #ddd; padding: 15px; margin: 10px 0;">
                    <h3>Booking Details:</h3>
                    <p><strong>Accommodation:</strong> %s</p>
                    <p><strong>Location:</strong> %s</p>
                    <p><strong>Check-in:</strong> %s</p>
                    <p><strong>Check-out:</strong> %s</p>
                    <p><strong>Guests:</strong> %d</p>
                    <p><strong>Total Price:</strong> $%.2f</p>
                </div>
                <p>You will receive another email once the property owner reviews your request.</p>
                <p>Thank you for choosing TurismApp!</p>
            </body>
            </html>
            """.formatted(
                booking.getUser().getFirstName(),
                booking.getAccommodationUnit().getTitle(),
                booking.getAccommodationUnit().getLocation(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getNumberOfGuests(),
                booking.getTotalPrice()
            );
    }
}
```

### 3.7 Analytics and Reporting System

#### 3.7.1 Profit Analytics Implementation
```java
@Service
public class AnalyticsService {
    
    public ProfitAnalyticsDTO getOwnerProfitAnalytics(Long ownerId, int year) {
        List<Booking> approvedBookings = bookingRepository
            .findByOwnerAndStatusAndYear(ownerId, BookingStatus.APPROVED, year);
        
        Map<Month, BigDecimal> monthlyProfits = approvedBookings.stream()
            .collect(Collectors.groupingBy(
                booking -> booking.getCheckInDate().getMonth(),
                Collectors.reducing(BigDecimal.ZERO, 
                    Booking::getTotalPrice, 
                    BigDecimal::add)
            ));
        
        BigDecimal totalYearlyProfit = monthlyProfits.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return ProfitAnalyticsDTO.builder()
            .yearlyProfit(totalYearlyProfit)
            .monthlyProfits(monthlyProfits)
            .totalBookings(approvedBookings.size())
            .averageBookingValue(calculateAverageBookingValue(approvedBookings))
            .build();
    }
    
    public SystemAnalyticsDTO getSystemAnalytics() {
        return SystemAnalyticsDTO.builder()
            .totalUsers(userRepository.count())
            .totalAccommodations(accommodationUnitRepository.count())
            .totalBookings(bookingRepository.count())
            .totalRevenue(calculateTotalSystemRevenue())
            .averageRating(calculateAverageSystemRating())
            .topPerformingAccommodations(getTopPerformingAccommodations())
            .build();
    }
}
```

## 4. AI Features Implementation

### 4.1 Current AI Features

#### 4.1.1 Intelligent Search Ranking
```java
@Service
public class SearchRankingService {
    
    public List<AccommodationUnit> rankSearchResults(List<AccommodationUnit> accommodations, 
                                                    String searchQuery, User user) {
        return accommodations.stream()
            .map(acc -> new ScoredAccommodation(acc, calculateRelevanceScore(acc, searchQuery, user)))
            .sorted(Comparator.comparing(ScoredAccommodation::getScore).reversed())
            .map(ScoredAccommodation::getAccommodation)
            .collect(Collectors.toList());
    }
    
    private double calculateRelevanceScore(AccommodationUnit accommodation, 
                                         String searchQuery, User user) {
        double score = 0.0;
        
        // Text relevance (title, description, location)
        score += calculateTextRelevance(accommodation, searchQuery) * 0.3;
        
        // Rating weight
        score += accommodation.getAverageRating() * 0.2;
        
        // Price attractiveness
        score += calculatePriceScore(accommodation) * 0.2;
        
        // User preference history
        score += calculateUserPreferenceScore(accommodation, user) * 0.2;
        
        // Availability boost
        if (accommodation.getAvailable()) {
            score += 0.1;
        }
        
        return score;
    }
}
```

#### 4.1.2 Profit Prediction
```java
@Service
public class ProfitPredictionService {
    
    public ProfitPredictionDTO predictNextMonthProfit(Long ownerId) {
        List<Booking> historicalBookings = bookingRepository
            .findByOwnerAndStatusAndDateRange(ownerId, BookingStatus.APPROVED, 
                                            LocalDate.now().minusMonths(12), 
                                            LocalDate.now());
        
        // Calculate seasonal trends
        Map<Month, Double> seasonalFactors = calculateSeasonalFactors(historicalBookings);
        
        // Calculate growth trend
        double growthTrend = calculateGrowthTrend(historicalBookings);
        
        // Base prediction on historical average
        BigDecimal averageMonthlyProfit = calculateAverageMonthlyProfit(historicalBookings);
        
        Month nextMonth = LocalDate.now().plusMonths(1).getMonth();
        double seasonalFactor = seasonalFactors.getOrDefault(nextMonth, 1.0);
        
        BigDecimal predictedProfit = averageMonthlyProfit
            .multiply(BigDecimal.valueOf(seasonalFactor))
            .multiply(BigDecimal.valueOf(1 + growthTrend));
        
        return ProfitPredictionDTO.builder()
            .predictedProfit(predictedProfit)
            .confidenceLevel(calculateConfidenceLevel(historicalBookings.size()))
            .seasonalFactor(seasonalFactor)
            .growthTrend(growthTrend)
            .build();
    }
}
```

### 4.2 Planned AI Enhancements

#### 4.2.1 Dynamic Pricing Algorithm
```java
// Planned implementation for intelligent pricing
@Service
public class DynamicPricingService {
    
    public BigDecimal calculateOptimalPrice(AccommodationUnit accommodation, 
                                          LocalDate targetDate) {
        // Factors to consider:
        // - Historical demand for similar dates
        // - Competitor pricing in the area
        // - Seasonal demand patterns
        // - Local events and holidays
        // - Property-specific booking patterns
        
        // Machine learning model would be trained on:
        // - Historical booking data
        // - Pricing data
        // - External factors (weather, events, etc.)
        // - Market competition data
        
        return calculatePriceRecommendation(accommodation, targetDate);
    }
}
```

#### 4.2.2 Personalized Recommendations
```java
// Planned implementation for recommendation engine
@Service
public class RecommendationService {
    
    public List<AccommodationUnit> getPersonalizedRecommendations(User user) {
        // Analyze user's booking history
        // Consider user preferences and ratings
        // Factor in location preferences
        // Consider price range patterns
        // Apply collaborative filtering
        // Use content-based filtering
        
        return generateRecommendations(user);
    }
}
```

## 5. Testing Strategy

### 5.1 Backend Testing

#### 5.1.1 Unit Testing
```java
@ExtendWith(MockitoExtension.class)
class AccommodationUnitServiceTest {
    
    @Mock
    private AccommodationUnitRepository accommodationUnitRepository;
    
    @InjectMocks
    private AccommodationUnitService accommodationUnitService;
    
    @Test
    void testFilterAccommodationUnits_WithLocationFilter() {
        // Given
        String location = "Bucharest";
        List<AccommodationUnit> mockUnits = createMockAccommodationUnits();
        when(accommodationUnitRepository.findByFilters(eq(location), any(), any(), any(), any(), any(), any()))
            .thenReturn(mockUnits);
        
        // When
        List<AccommodationUnit> result = accommodationUnitService
            .filterAccommodationUnits(location, null, null, null, null, null, null);
        
        // Then
        assertThat(result).hasSize(2);
        verify(accommodationUnitRepository).findByFilters(eq(location), any(), any(), any(), any(), any(), any());
    }
}
```

#### 5.1.2 Integration Testing
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
class AccommodationUnitControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testFilterAccommodations_ReturnsFilteredResults() {
        // Given
        String url = "/api/accommodations/filter?location=Bucharest&minPrice=50&maxPrice=200";
        
        // When
        ResponseEntity<AccommodationUnit[]> response = restTemplate
            .getForEntity(url, AccommodationUnit[].class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);
    }
}
```

### 5.2 Frontend Testing

#### 5.2.1 Component Testing
```javascript
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { rest } from 'msw';
import { setupServer } from 'msw/node';
import UnitsListPage from '../pages/UnitsListPage';

const server = setupServer(
    rest.get('/api/accommodations/filter', (req, res, ctx) => {
        return res(ctx.json([
            {
                id: 1,
                title: 'Test Accommodation',
                location: 'Bucharest',
                pricePerNight: 100
            }
        ]));
    })
);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

test('should filter accommodations when search button is clicked', async () => {
    render(<UnitsListPage />);
    
    // Fill in filter inputs
    fireEvent.change(screen.getByPlaceholderText('Location'), {
        target: { value: 'Bucharest' }
    });
    
    // Click search button
    fireEvent.click(screen.getByText('Search'));
    
    // Wait for results
    await waitFor(() => {
        expect(screen.getByText('Test Accommodation')).toBeInTheDocument();
    });
});
```

## 6. Performance Considerations

### 6.1 Database Optimization
- **Indexing Strategy**: Indexes on frequently queried columns (location, county, pricePerNight)
- **Query Optimization**: Efficient JPA queries with proper fetch strategies
- **Connection Pooling**: HikariCP for optimal database connections

### 6.2 Frontend Optimization
- **Code Splitting**: Lazy loading of components
- **Memoization**: React.memo for expensive components
- **Debouncing**: Debounced search inputs to prevent excessive API calls

### 6.3 Caching Strategy
- **Application Level**: Spring Cache for frequently accessed data
- **Database Level**: Query result caching
- **Frontend Level**: Browser caching for static assets

## 7. Security Considerations

### 7.1 Authentication Security
- **JWT Tokens**: Secure token-based authentication
- **Password Hashing**: BCrypt with salt for password storage
- **Token Expiration**: Configurable token expiration times

### 7.2 Authorization Security
- **Role-Based Access**: Granular permissions based on user roles
- **Method Security**: @PreAuthorize annotations for method-level security
- **Resource Protection**: Owner-only access to own resources

### 7.3 Data Security
- **Input Validation**: Comprehensive validation on all inputs
- **SQL Injection Prevention**: Parameterized queries
- **XSS Prevention**: Proper output encoding
- **CORS Configuration**: Controlled cross-origin requests

## 8. Deployment Architecture

### 8.1 Development Environment
```
Frontend (React) → http://localhost:3000
Backend (Spring Boot) → http://localhost:8080
Database (MySQL) → localhost:3306
```

### 8.2 Production Environment (Planned)
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Load Balancer │    │   Web Server    │    │   App Server    │
│    (Nginx)      │───▶│    (Nginx)      │───▶│ (Spring Boot)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                                                        ▼
                                               ┌─────────────────┐
                                               │    Database     │
                                               │    (MySQL)      │
                                               └─────────────────┘
```

## 9. Conclusion

The TurismApp system represents a comprehensive solution for tourist accommodation management, incorporating modern web development practices, robust security measures, and innovative AI features. The system successfully addresses the identified problems in traditional booking systems while providing a scalable foundation for future enhancements.

### 9.1 Key Achievements
- **Complete Functionality**: All core requirements implemented and tested
- **Modern Architecture**: Scalable and maintainable system design
- **Security Implementation**: Robust authentication and authorization
- **User Experience**: Intuitive interfaces for all user roles
- **Performance Optimization**: Efficient queries and responsive design
- **AI Integration**: Initial AI features with planned enhancements

### 9.2 Future Enhancements
- **Advanced AI Features**: Machine learning for pricing and recommendations
- **Mobile Application**: Native mobile apps for iOS and Android
- **Payment Integration**: Secure payment processing
- **Multi-language Support**: Internationalization for global use
- **Advanced Analytics**: Business intelligence and reporting tools

The system demonstrates the successful application of modern software engineering principles and provides a solid foundation for a commercial tourist accommodation management platform.

---

**Technical Specifications**:
- **Frontend**: React.js 18+, JavaScript ES6+
- **Backend**: Spring Boot 3.0+, Java 17+
- **Database**: MySQL 8.0+
- **Security**: JWT, Spring Security, BCrypt
- **Build Tools**: Maven, npm
- **Testing**: JUnit 5, Mockito, React Testing Library
- **Documentation**: Comprehensive technical and user documentation

**System Requirements**:
- **Development**: 8GB RAM, 50GB storage
- **Production**: 16GB RAM, 100GB storage, load balancer support
- **Database**: MySQL 8.0+ with InnoDB storage engine
- **JVM**: Java 17+ with optimized heap settings
