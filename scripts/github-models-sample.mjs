import OpenAI from "openai";

const token = process.env.GITHUB_TOKEN;
const endpoint = "https://models.github.ai/inference";
const model = "openai/gpt-4.1";

async function main() {
  if (!token) {
    console.error(
      "Missing GITHUB_TOKEN environment variable. In Codespaces, add it as a Codespaces secret, grant it access to this repo, then stop & restart the codespace."
    );
    process.exit(1);
  }

  const client = new OpenAI({ baseURL: endpoint, apiKey: token, timeout: 15000 });

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