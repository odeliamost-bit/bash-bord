-- Kompas: sync tussen apparaten.
-- Plak dit één keer in Supabase: SQL Editor -> New query -> Run.
-- Het mag vaker: het past alleen aan wat nog niet bestaat.

-- Eén rij per onderdeel (bijv. "geld") per gebruiker. De inhoud is de JSON van de app.
create table if not exists public.kompas_data (
  user_id    uuid        not null default auth.uid() references auth.users (id) on delete cascade,
  onderdeel  text        not null,
  inhoud     text        not null,
  updated_at timestamptz not null default now(),
  primary key (user_id, onderdeel),
  -- Alleen deze onderdelen mogen online (gekozen door Odelia).
  -- Uren, cyclus, mail-hulp, advies, weer en thuis: NOOIT. Zelfde lijst als SyncRegels.kt.
  constraint alleen_toegestaan check (onderdeel in ('geld', 'onthoud', 'leren', 'wensen', 'recepten', 'mensen', 'fotos')),
  constraint niet_te_groot check (octet_length(inhoud) < 5000000)
);

-- De server zet de tijd (niet de telefoon), zodat "nieuwste wint" eerlijk is.
create or replace function public.kompas_tijd() returns trigger
language plpgsql set search_path = '' as $$
begin
  new.updated_at := now();
  return new;
end $$;

drop trigger if exists kompas_tijd on public.kompas_data;
create trigger kompas_tijd before insert or update on public.kompas_data
  for each row execute function public.kompas_tijd();

-- Het slot: alleen de ingelogde eigenaar ziet en verandert zijn eigen rijen.
alter table public.kompas_data enable row level security;

drop policy if exists "alleen eigen data" on public.kompas_data;
create policy "alleen eigen data" on public.kompas_data
  for all to authenticated
  using ((select auth.uid()) = user_id)
  with check ((select auth.uid()) = user_id);

-- Niet-ingelogde bezoekers mogen niks.
revoke all on public.kompas_data from anon;

-- Realtime: andere apparaten horen meteen dat er iets veranderd is.
do $$
begin
  if not exists (
    select 1 from pg_publication_tables
    where pubname = 'supabase_realtime' and schemaname = 'public' and tablename = 'kompas_data'
  ) then
    alter publication supabase_realtime add table public.kompas_data;
  end if;
end $$;
