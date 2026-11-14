import { test } from "node:test";
import assert from "node:assert/strict";
import worker, { systeem } from "../src/index.js";

const env = { APP_CODE: "verzonnen-code-123", ANTHROPIC_API_KEY: "nep" };
const nepClient = (tekst, stop = "end_turn") => () => ({
  beta: { messages: { create: async (vraag) => ({ stop_reason: stop, content: [{ type: "text", text: tekst }], vraag }) } },
});
const verzoek = (body, code = env.APP_CODE, pad = "/vertaal") =>
  new Request(`https://voorbeeld.test${pad}`, {
    method: "POST",
    headers: { "X-App-Code": code, "content-type": "application/json" },
    body: typeof body === "string" ? body : JSON.stringify(body),
  });

test("vertaalt met de juiste code", async () => {
  const r = await worker.fetch(verzoek({ tekst: "Hoi", taal: "Engels" }), env, null, nepClient(" Hi "));
  assert.equal(r.status, 200);
  assert.deepEqual(await r.json(), { vertaling: "Hi" });
});

test("verkeerde code = geen toegang", async () => {
  const r = await worker.fetch(verzoek({ tekst: "Hoi" }, "fout"), env, null, nepClient("Hi"));
  assert.equal(r.status, 401);
});

test("zonder ingestelde code = geen toegang", async () => {
  const r = await worker.fetch(verzoek({ tekst: "Hoi" }, ""), { ...env, APP_CODE: "" }, null, nepClient("Hi"));
  assert.equal(r.status, 401);
});

test("te lange of lege tekst wordt geweigerd", async () => {
  for (const tekst of ["", "   ", "a".repeat(2001)]) {
    const r = await worker.fetch(verzoek({ tekst }), env, null, nepClient("x"));
    assert.equal(r.status, 400);
  }
});

test("ander pad of GET = niet gevonden", async () => {
  const r = await worker.fetch(verzoek({ tekst: "Hoi" }, env.APP_CODE, "/anders"), env, null, nepClient("x"));
  assert.equal(r.status, 404);
});

test("weigering van Claude geeft een fout", async () => {
  const r = await worker.fetch(verzoek({ tekst: "Hoi" }), env, null, nepClient("", "refusal"));
  assert.equal(r.status, 422);
});

test("opdracht hangt af van taal en soort", () => {
  assert.match(systeem("Duits", "Tekst"), /Duits/);
  assert.match(systeem("onzin", "Appje"), /Nederlands.*appje/s);
});
