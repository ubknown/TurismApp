# DataSeeder Documentation

## Overview
The `DataSeeder` class automatically populates the database with realistic, production-like test data when the application starts up. It only seeds data if the database is empty (no users and no accommodation units).

## Seeded Data

### 👥 Guest Users (10 users)
Realistic Romanian names and email addresses:
- `ana.popescu@gmail.com` - Ana Popescu
- `ion.gheorghe@yahoo.com` - Ion Gheorghe
- `maria.ionescu@outlook.com` - Maria Ionescu
- `alexandru.dumitru@gmail.com` - Alexandru Dumitru
- `elena.mihai@yahoo.com` - Elena Mihai
- `cristian.stefan@gmail.com` - Cristian Stefan
- `diana.radu@outlook.com` - Diana Radu
- `andrei.popa@gmail.com` - Andrei Popa
- `laura.vasile@yahoo.com` - Laura Vasile
- `marius.tudor@gmail.com` - Marius Tudor

**Credentials:** All guests use password `password123`

### 🏠 Property Owners (5 users)
Professional-looking emails for business accounts:
- `victor.constantin@turism.ro` - Victor Constantin (5 properties)
- `mihaela.dobre@accomodation.ro` - Mihaela Dobre (4 properties)
- `radu.marinescu@properties.ro` - Radu Marinescu (3 properties)
- `carmen.georgescu@hotels.ro` - Carmen Georgescu (4 properties)
- `daniel.florea@villas.ro` - Daniel Florea (3 properties)

**Credentials:** All owners use password `owner123`

### 🏨 Accommodation Units (19 units)
Realistic properties distributed across Romania:

#### Victor Constantin's Properties (5 units):
1. **Casa Traditionala Bucuresti** - Traditional house in historic Bucharest center
2. **Apartament Luxury Herastrau** - Luxury apartment with Herastrau Park view
3. **Studio Modern Calea Victoriei** - Modern studio on Calea Victoriei
4. **Vila Primaverii** - Elegant villa in Primaverii area with private garden
5. **Penthouse Pipera** - Penthouse with large terrace in Pipera

#### Mihaela Dobre's Properties (4 units):
1. **Cabana Poiana Brasov** - Rustic cabin with mountain view in Poiana Brasov
2. **Apartament Centrul Vechi Cluj** - Apartment in Cluj-Napoca old center
3. **Casa de Vacanta Predeal** - Holiday house ideal for winter and summer
4. **Studio Mamaia Nord** - Modern studio 50m from Mamaia beach

#### Radu Marinescu's Properties (3 units):
1. **Vila Sinaia Centru** - Elegant villa in Sinaia center, near castle
2. **Apartament Brasov Centru** - Spacious apartment in Brasov center
3. **Pensiunea Bucovina** - Traditional guesthouse in the heart of Bucovina

#### Carmen Georgescu's Properties (4 units):
1. **Hotel Boutique Sibiu** - Boutique hotel in Sibiu historic center
2. **Casa Delta Dunarii** - Traditional house in Danube Delta
3. **Apartament Timisoara Centru** - Modern apartment in Timisoara center
4. **Vila Bran Castle View** - Villa with Bran Castle view

#### Daniel Florea's Properties (3 units):
1. **Pensiunea Maramures** - Traditional Maramures guesthouse
2. **Apartament Oradea Centru** - Elegant apartment in Oradea center
3. **Cabana Retezat** - Mountain cabin in Retezat National Park

### 📅 Reservations (28 reservations)
Realistic reservations with:
- **Past reservations** (10): 1-60 days ago, mostly confirmed (85%), some cancelled
- **Current/near future** (8): Next 30 days, mixed statuses
- **Future reservations** (10): 30-150 days from now, various statuses

**Status distribution:**
- 70% Confirmed
- 15% Pending
- 15% Cancelled

**Duration:** 2-7 nights per reservation
**Guests:** 1 to unit capacity

## Features

### 🔍 Schema Validation
- Validates database schema before seeding
- Checks access to users and accommodation units tables
- Provides helpful error messages if schema is incorrect

### 🚫 Conditional Seeding
- Only seeds if both users and accommodation units tables are empty
- Prevents duplicate data on restart
- Safe for production environments

### 📊 Production-Like Data
- Realistic Romanian names, addresses, and locations
- Proper price ranges (70-400 RON per night)
- Various property types (APARTMENT, HOUSE, VILLA, STUDIO, COTTAGE)
- Random availability states (75% available)
- Realistic capacity ranges (2-14 guests)
- Creation dates spread over 1-6 months ago

### 🎲 Randomization
- Random availability for properties
- Random creation dates
- Random reservation dates and durations
- Random guest counts within capacity limits
- Realistic status distributions

## Usage

### Automatic Seeding
The seeder runs automatically when the Spring Boot application starts. Monitor the console for seeding messages:

```
🌱 Seeding database with realistic test data...
👥 Creating guest users...
🏠 Creating property owners...
🏨 Creating accommodation units...
📅 Creating reservations...
📊 Seeding complete - Created:
  • 10 guest users
  • 5 property owners
  • 19 accommodation units
  • 28 reservations
✅ Database seeded successfully with production-like data!
```

### Manual Testing
Use the provided batch files:

1. **Start backend with seeder:**
   ```bash
   test-seeder.bat
   ```

2. **Verify seeded data:**
   ```bash
   verify-seeded-data.bat
   ```

### API Endpoints to Test
- `GET /api/units/debug/health` - Check database health
- `GET /api/units/debug/count` - Get total units count
- `GET /api/units` - List all units
- `GET /api/units/search?location=Bucharest` - Search by location

## Database Schema Requirements

The seeder requires the following tables to exist:
- `users` (with role, email, password fields)
- `accommodation_units` (with all property fields)
- `reservations` (with foreign keys to users and units)

If you get schema validation errors, run:
```sql
-- Apply the fix_reviews_schema.sql if needed
-- Or ensure spring.jpa.hibernate.ddl-auto=update in application.properties
```

## Benefits for Testing

### UI Testing
- Realistic data for frontend components
- Various property types for filtering tests
- Different price ranges for search functionality
- Multiple owners for role-based access testing

### Performance Testing
- Sufficient data volume for pagination testing
- Multiple reservations for profit analytics
- Various dates for calendar functionality

### Business Logic Testing
- Owner-property relationships
- Guest-reservation relationships  
- Status transitions and validation
- Location uniqueness constraints

## Customization

To modify the seeded data:

1. **Update user data:** Modify the arrays in `createGuestUsers()` and `createPropertyOwners()`
2. **Add more properties:** Extend the `unitData` array in `createAccommodationUnits()`
3. **Change reservation count:** Modify the loop count in `createReservations()`
4. **Adjust randomization:** Change the probability values for statuses and availability

## Security Notes

- All passwords are properly hashed using BCrypt
- Guest password: `password123`
- Owner password: `owner123`
- Users are created with `enabled=true` for immediate testing
- No sensitive data is hardcoded beyond test credentials
