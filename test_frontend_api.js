// Test API calls to verify axios configuration
// Run this in browser console when frontend is running

console.log('🧪 Testing API Configuration...');

// Test 1: Check if axios is configured correctly
const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Test 2: Test public endpoint (no auth required)
api.get('/api/units/public')
  .then(response => {
    console.log('✅ Public API working:', response.status);
    console.log('Data:', response.data);
  })
  .catch(error => {
    console.log('❌ Public API error:', error.message);
    if (error.response) {
      console.log('Status:', error.response.status);
      console.log('Data:', error.response.data);
    }
  });

// Test 3: Test auth endpoint
api.post('/api/auth/login', {
  email: 'test@example.com',
  password: 'wrongpassword'
})
  .then(response => {
    console.log('✅ Auth endpoint reachable');
  })
  .catch(error => {
    if (error.response && error.response.status === 401) {
      console.log('✅ Auth endpoint working (401 expected for wrong credentials)');
    } else {
      console.log('❌ Auth endpoint error:', error.message);
    }
  });

console.log('🔍 Check network tab for API calls...');
