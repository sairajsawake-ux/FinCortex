const admin = require('../firebase/firebaseAdmin');
const axios = require('axios');
const { calculateRiskScore, generateTrustScore } = require('../ai/userIntelligence');

const db = admin.firestore();

// @desc    AI-Automated User Registration
// @route   POST /api/users/register
const registerUser = async (req, res) => {
  try {
    const { name, email, password } = req.body;

    // 1. AI Analysis: Calculate Risk Level before creation
    const riskLevel = calculateRiskScore(password, email);

    // 2. Firebase Auth: Create Identity
    const userRecord = await admin.auth().createUser({
      email,
      password,
      displayName: name,
    });

    // 3. Firestore: Store User Data + AI Metadata
    // We do NOT store the password in Firestore
    await db.collection('users').doc(userRecord.uid).set({
      name,
      email,
      riskLevel, // AI-assigned
      trustScore: generateTrustScore(riskLevel),
      createdAt: new Date().toISOString(),
      loginCount: 0
    });

    res.status(201).json({
      success: true,
      userId: userRecord.uid,
      riskLevel: riskLevel
    });

  } catch (error) {
    res.status(400).json({ success: false, message: error.message });
  }
};

// @desc    AI-Assisted Login
// @route   POST /api/users/login
const loginUser = async (req, res) => {
  try {
    const { email, password } = req.body;

    // 1. Authenticate using Firebase REST API (Backend-side login)
    const apiKey = process.env.FIREBASE_API_KEY;
    const loginUrl = `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${apiKey}`;
    
    const response = await axios.post(loginUrl, {
      email,
      password,
      returnSecureToken: true
    });

    const { idToken, localId } = response.data;

    // 2. AI Monitoring: Update login stats
    const userRef = db.collection('users').doc(localId);
    await userRef.update({
      loginCount: admin.firestore.FieldValue.increment(1),
      lastLogin: new Date().toISOString()
    });

    res.json({
      success: true,
      token: idToken,
      userId: localId
    });

  } catch (error) {
    res.status(401).json({ success: false, message: 'Invalid credentials or AI blocked access' });
  }
};

// @desc    Get Protected Profile with AI Insights
// @route   GET /api/users/profile
const getUserProfile = async (req, res) => {
  res.json({
    user: req.user,
    aiInsights: {
      accountStatus: req.user.riskLevel === 'HIGH' ? 'Under Review' : 'Active',
      trustScore: req.user.trustScore,
      recommendation: req.user.trustScore > 80 ? 'Eligible for Premium' : 'Verify Identity'
    }
  });
};

module.exports = {
  registerUser,
  loginUser,
  getUserProfile
};