# DATABASE VERIFICATION GUIDE

## To understand why date filtering might not be working, we need to check your database data.

### Step 1: Check your database structure

Run these SQL queries in your MySQL database to understand your data:

```sql
-- 1. Check how many accommodation units you have
SELECT COUNT(*) as total_units FROM accommodation_unit WHERE active = true;

-- 2. Check a sample of your accommodation units
SELECT id, name, location, active FROM accommodation_unit LIMIT 5;

-- 3. Check if you have any bookings/reservations
SELECT COUNT(*) as total_bookings FROM booking;
SELECT COUNT(*) as total_reservations FROM reservation;

-- 4. Check booking data structure
SELECT id, accommodation_unit_id, start_date, end_date, status 
FROM booking 
WHERE status = 'CONFIRMED' 
LIMIT 5;

-- 5. Check reservation data structure  
SELECT id, accommodation_unit_id, start_date, end_date, status 
FROM reservation 
WHERE status = 'CONFIRMED' 
LIMIT 5;

-- 6. Check for conflicts in July 2025 (the dates you're testing)
SELECT 
    au.id as unit_id,
    au.name as unit_name,
    b.start_date,
    b.end_date,
    b.status as booking_status
FROM accommodation_unit au
LEFT JOIN booking b ON au.id = b.accommodation_unit_id
WHERE b.start_date <= '2025-07-10' 
  AND b.end_date >= '2025-07-01'
  AND b.status = 'CONFIRMED';

-- 7. Check for conflicts in reservations table
SELECT 
    au.id as unit_id,
    au.name as unit_name,
    r.start_date,
    r.end_date,
    r.status as reservation_status
FROM accommodation_unit au
LEFT JOIN reservation r ON au.id = r.accommodation_unit_id
WHERE r.start_date <= '2025-07-10' 
  AND r.end_date >= '2025-07-01'
  AND r.status = 'CONFIRMED';
```

### Step 2: Understanding the results

**If you see:**
- `total_units` = 0 → No accommodation units in database
- `total_bookings` = 0 AND `total_reservations` = 0 → No reservations, all units should be available
- Results from queries 6 & 7 show units → Those units are unavailable for July 1-10
- No results from queries 6 & 7 → All units should be available for July 1-10

### Step 3: Sample data creation (if needed)

If you have no test bookings/reservations, create some test data:

```sql
-- Create a test booking for unit ID 23 (adjust the ID based on your data)
INSERT INTO booking (accommodation_unit_id, start_date, end_date, status, total_price, user_id, created_at)
VALUES (23, '2025-07-05', '2025-07-08', 'CONFIRMED', 300.00, 1, NOW());

-- Create another test booking for a different unit (adjust ID)
INSERT INTO booking (accommodation_unit_id, start_date, end_date, status, total_price, user_id, created_at)
VALUES (6, '2025-07-02', '2025-07-06', 'CONFIRMED', 250.00, 1, NOW());
```

### Step 4: Test again

After running the database queries and understanding your data:

1. Run `comprehensive-debug.bat` 
2. The debug endpoint should show:
   - `totalActiveUnits`: Total accommodation units
   - `availableUnitsAfterFilter`: Units available for your dates
   - If these numbers are different, filtering is working

### Step 5: Frontend testing

1. Open your React app
2. Go to units list page
3. Open browser DevTools (F12) → Console tab
4. Set check-in: 2025-08-01, check-out: 2025-08-05
5. Click "Apply Filters"
6. Check console logs for the debugging information we added

The console should show:
- Filter state
- API URL being called
- Response data
- Any errors

This will help us pinpoint exactly where the issue is!
