const admin = require("../firebase/firebaseAdmin");

// Standard API response format
const apiResponse = (res, statusCode, success, message, data = null) => {
  return res.status(statusCode).json({
    success,
    message,
    ...(data && { data })
  });
};

const authMiddleware = async (req, res, next) => {
  try {
    const authHeader = req.headers.authorization;

    if (!authHeader || !authHeader.startsWith("Bearer ")) {
      return apiResponse(res, 401, false, "No token provided. Authorization required.");
    }

    const token = authHeader.split(" ")[1];
    
    if (!token) {
      return apiResponse(res, 401, false, "Invalid token format");
    }

    const decodedToken = await admin.auth().verifyIdToken(token);

    req.user = decodedToken;
    next();
  } catch (error) {
    console.error('Auth middleware error:', error);
    
    if (error.code === 'auth/id-token-expired') {
      return apiResponse(res, 401, false, "Token has expired");
    }
    if (error.code === 'auth/invalid-id-token') {
      return apiResponse(res, 401, false, "Invalid token");
    }
    if (error.code === 'auth/argument-error') {
      return apiResponse(res, 401, false, "Invalid token format");
    }
    
    return apiResponse(res, 401, false, "Authentication failed. Invalid or expired token.");
  }
};

module.exports = authMiddleware;
