const sendMessage = async (req, res) => {
  res.status(200).json({ message: "Chat endpoint is working" });
};

module.exports = {
  sendMessage
};