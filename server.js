import express from "express";
import OpenAI from "openai";

const app = express();
app.use(express.json({ limit: "32kb" }));

const apiKey = process.env.OPENAI_API_KEY;
if (!apiKey) {
  console.error("Missing OPENAI_API_KEY environment variable.");
  process.exit(1);
}
const client = new OpenAI({ apiKey });

app.get("/", (_req, res) => res.send("premi server is running"));

app.post("/chat", async (req, res) => {
  try {
    const messages = Array.isArray(req.body?.messages) ? req.body.messages : [];
    const safeMessages = messages
      .filter(m => ["user", "assistant"].includes(m?.role) && typeof m?.content === "string")
      .slice(-20);

    if (!safeMessages.length || safeMessages[safeMessages.length - 1].role !== "user") {
      return res.status(400).json({ error: "A user message is required." });
    }

    const response = await client.responses.create({
      model: "gpt-5.5",
      instructions: [
        "너의 이름은 premi다.",
        "한국어로 친구처럼 편하고 자연스럽게 대화한다.",
        "답변은 똑똑하고 정확하며 충분히 자세하게 한다.",
        "모르는 내용은 지어내지 말고 모른다고 말한다.",
        "상대방을 존중하고 안전한 도움을 제공한다."
      ].join("\\n"),
      input: safeMessages.map(m => ({
        role: m.role,
        content: [{ type: "input_text", text: m.content }]
      }))
    });

    res.json({ reply: response.output_text || "답변을 만들지 못했어. 다시 물어봐!" });
  } catch (error) {
    console.error("premi request failed:", error?.message || error);
    res.status(500).json({ error: "AI request failed." });
  }
});

const port = process.env.PORT || 3000;
app.listen(port, () => console.log(`premi server listening on ${port}`));
