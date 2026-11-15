import { test } from "node:test";
import assert from "node:assert/strict";
import worker, { systeem, telVandaag, DAG_MAX } from "../src/index.js";

const nepTeller = (begin = {}) => {
  const data = new Map(Object.entries(begin));
  return { data, get: async (k) => data.get(k) ?? null, put: async (k, v) => void data.set(k, v) };
};
const env = { APP_CODE: "verzonnen-code-123", ANTHROPIC_API_KEY: "nep", TELLER: nepTeller() };
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

test("daglimiet: telt op en stopt bij het maximum", async () => {
  const dag = new Date("2026-11-15T12:00:00Z");
  const teller = nepTeller();
  assert.equal(await telVandaag(teller, dag), true);
  assert.equal(teller.data.get("dag-2026-11-15"), "1");
  const vol = nepTeller({ "dag-2026-11-15": String(DAG_MAX) });
  assert.equal(await telVandaag(vol, dag), false);
  // Een nieuwe dag begint weer bij nul.
  assert.equal(await telVandaag(vol, new Date("2026-11-16T00:00:01Z")), true);
});

test("daglimiet bereikt = 429 en Claude wordt niet gevraagd", async () => {
  const vandaag = `dag-${new Date().toISOString().slice(0, 10)}`;
  let gevraagd = false;
  const client = () => ({ beta: { messages: { create: async () => { gevraagd = true; return { content: [] }; } } } });
  const r = await worker.fetch(verzoek({ tekst: "Hoi" }), { ...env, TELLER: nepTeller({ [vandaag]: String(DAG_MAX) }) }, null, client);
  assert.equal(r.status, 429);
  assert.deepEqual(await r.json(), { fout: "daglimiet bereikt" });
  assert.equal(gevraagd, false);
});

test("zonder teller geen vertaling", async () => {
  const r = await worker.fetch(verzoek({ tekst: "Hoi" }), { ...env, TELLER: undefined }, null, nepClient("Hi"));
  assert.equal(r.status, 500);
});

test("vraagt het goedkope model", async () => {
  let model;
  const client = () => ({ beta: { messages: { create: async (v) => { model = v.model; return { stop_reason: "end_turn", content: [{ type: "text", text: "Hi" }] }; } } } });
  await worker.fetch(verzoek({ tekst: "Hoi" }), { ...env, TELLER: nepTeller() }, null, client);
  assert.equal(model, "claude-haiku-5-5");
});
