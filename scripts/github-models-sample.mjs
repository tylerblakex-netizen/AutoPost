import OpenAI from "openai";

// Prefer GITHUB_TOKEN, fallback to TOKEN_GITHUB if the former isn't set
const token = process.env.GITHUB_TOKEN || process.env.TOKEN_GITHUB;
const endpoint = "https://models.github.ai/inference";
const model = "openai/gpt-4.1";

async function main() {
  if (!token) {
    console.error(
      "Missing token. Set GITHUB_TOKEN or TOKEN_GITHUB as an environment variable (e.g., export GITHUB_TOKEN=...)"
    );
    process.exit(1);
  }

  const client = new OpenAI({ baseURL: endpoint, apiKey: token });

  const response = await client.chat.completions.create({
    model,
    messages: [
      { role: "system", content: "You are a helpful assistant." },
      { role: "user", content: "What is the capital of France?" },
    ],
    temperature: 1.0,
    top_p: 1.0,
  });

  console.log(response.choices?.[0]?.message?.content ?? "");
}

main().catch((err) => {
  console.error("Sample encountered an error:", err?.message || err);
  process.exit(1);
});