import OpenAI from "openai";

// Require TOKEN_GITHUB (no fallbacks to avoid confusion)
const token = process.env.TOKEN_GITHUB;

// Allow endpoint override; default to GitHub Models inference API
const endpoint = process.env.MODELS_BASE_URL || "https://models.github.ai/inference";
const model = "openai/gpt-4.1";

async function main() {
  if (!token) {
    console.error(
      "❌ Missing token. Set TOKEN_GITHUB as an environment variable (e.g., export TOKEN_GITHUB=gho_xxx)"
    );
    process.exit(1);
  }

  const client = new OpenAI({
    baseURL: endpoint,
    apiKey: token,
  });

  const response = await client.chat.completions.create({
    model,
    messages: [
      { role: "system", content: "You are a helpful assistant." },
      { role: "user", content: "Say hello, then tell me today's vibe in 5 words." },
    ],
    temperature: 0.7,
  });

  console.log(response.choices?.[0]?.message?.content ?? "");
}

main().catch((err) => {
  console.error("❌ Request failed:", err?.message || err);
  process.exit(1);
});