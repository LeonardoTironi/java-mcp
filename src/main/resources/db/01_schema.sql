-- =============================================================================
-- Banco de dados de imóveis (PostgreSQL 13+)
--
-- Espelha o modelo Prisma "Property"/"Address" fornecido. Os nomes seguem a
-- convenção do Prisma (tabelas e colunas entre aspas, em camelCase), portanto
-- devem ser escritos entre aspas nas consultas: "Property", "minPrice" etc.
--
-- Executado automaticamente pelo contêiner "postgres" do compose.yaml (pasta
-- montada em /docker-entrypoint-initdb.d), em ordem alfabética: 01, 02...
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Enumerações
-- -----------------------------------------------------------------------------
CREATE TYPE "PropertyType"       AS ENUM ('HOUSE', 'APARTMENT', 'LAND',
                                          'COMMERCIAL', 'FARM');
CREATE TYPE "Purpose"            AS ENUM ('SELL', 'RENT', 'BOTH');
CREATE TYPE "AvailabilityStatus" AS ENUM ('DRAFT', 'AVAILABLE', 'RESERVED',
                                          'SOLD', 'RENTED', 'INACTIVE');
CREATE TYPE "UsageType"          AS ENUM ('RESIDENTIAL', 'COMMERCIAL', 'OTHER');

-- -----------------------------------------------------------------------------
-- Funções auxiliares
-- -----------------------------------------------------------------------------

-- Tenant (organização) da sessão atual. Usada como DEFAULT de
-- "organizationId"; pode ser definida com: SET app.org_id = 'minha-org';
CREATE FUNCTION current_org_id() RETURNS text
LANGUAGE sql STABLE AS $$
   SELECT coalesce(nullif(current_setting('app.org_id', true), ''),
                   'default-org')
$$;

-- Código curto e legível do imóvel (substitui o cuid() do Prisma).
CREATE FUNCTION new_property_code() RETURNS text
LANGUAGE sql VOLATILE AS $$
   SELECT 'IMV-' || upper(substr(md5(random()::text || clock_timestamp()::text),
                                 1, 8))
$$;

-- Mantém "updatedAt" atualizado (equivalente ao @updatedAt do Prisma).
CREATE FUNCTION set_updated_at() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
   NEW."updatedAt" := now();
   RETURN NEW;
END
$$;

-- -----------------------------------------------------------------------------
-- Address
-- -----------------------------------------------------------------------------
CREATE TABLE "Address" (
   "id"             text             PRIMARY KEY DEFAULT gen_random_uuid()::text,
   "street"         text             NOT NULL,
   "number"         text             NOT NULL,
   "extraInfo"      text,
   "neighborhood"   text             NOT NULL,
   "city"           text             NOT NULL,
   "state"          varchar(2)       NOT NULL,
   "postalCode"     varchar(9)       NOT NULL,
   "latitude"       double precision,
   "longitude"      double precision,
   "organizationId" text             NOT NULL DEFAULT current_org_id()
);

CREATE INDEX "Address_organizationId_idx" ON "Address" ("organizationId");
-- apoio às buscas por localidade (cidade/bairro)
CREATE INDEX "Address_city_neighborhood_idx"
          ON "Address" ("state", "city", "neighborhood");

-- -----------------------------------------------------------------------------
-- PropertyOwner (mínimo necessário para a chave estrangeira de Property)
-- -----------------------------------------------------------------------------
CREATE TABLE "PropertyOwner" (
   "id"             text PRIMARY KEY DEFAULT gen_random_uuid()::text,
   "name"           text NOT NULL,
   "email"          text,
   "phone"          text,
   "organizationId" text NOT NULL DEFAULT current_org_id()
);

CREATE INDEX "PropertyOwner_organizationId_idx"
          ON "PropertyOwner" ("organizationId");

-- -----------------------------------------------------------------------------
-- Condominium (mínimo necessário para a chave estrangeira de Property)
-- -----------------------------------------------------------------------------
CREATE TABLE "Condominium" (
   "id"             text PRIMARY KEY DEFAULT gen_random_uuid()::text,
   "name"           text NOT NULL,
   "addressId"      text UNIQUE REFERENCES "Address" ("id") ON DELETE SET NULL,
   "organizationId" text NOT NULL DEFAULT current_org_id()
);

CREATE INDEX "Condominium_organizationId_idx"
          ON "Condominium" ("organizationId");

-- -----------------------------------------------------------------------------
-- Property
-- -----------------------------------------------------------------------------
CREATE TABLE "Property" (
   "id"             text                 PRIMARY KEY
                                         DEFAULT gen_random_uuid()::text,
   "title"          text                 NOT NULL,
   "description"    text,
   -- dados complementares: bedrooms, bathrooms, suites, parkingSpots,
   -- rentPrice (quando purpose = BOTH), amenities...
   "extraInfo"      jsonb,

   -- Filtros
   "type"           "PropertyType"       NOT NULL,
   "purpose"        "Purpose"            NOT NULL,
   "code"           text                 NOT NULL DEFAULT new_property_code(),
   "status"         "AvailabilityStatus" NOT NULL DEFAULT 'DRAFT',

   "isFeatured"     boolean              NOT NULL DEFAULT false,
   "showOnWebsite"  boolean              NOT NULL DEFAULT true,

   "websiteLink"    text,
   "createdBy"      text,
   "publishedOn"    text[]               NOT NULL DEFAULT '{}',

   -- Residencial, comercial ou outro ("para morar ou para trabalhar?")
   "usageType"      "UsageType"          NOT NULL DEFAULT 'RESIDENTIAL',

   -- Link de vídeo do anúncio (YouTube/Vimeo)
   "videoUrl"       text,

   -- Faixa de preço e de área (um imóvel com várias unidades possui faixa;
   -- com uma única unidade, mínimo = máximo). Valores em R$ e m².
   "minPrice"       numeric(14,2),
   "maxPrice"       numeric(14,2),
   "minArea"        numeric(10,2),
   "maxArea"        numeric(10,2),

   "createdAt"      timestamp(3)         NOT NULL DEFAULT now(),
   "updatedAt"      timestamp(3)         NOT NULL DEFAULT now(),
   "deletedAt"      timestamp(3),

   "condominiumId"  text REFERENCES "Condominium" ("id") ON DELETE SET NULL,
   "addressId"      text UNIQUE
                         REFERENCES "Address" ("id") ON DELETE SET NULL,
   "ownerId"        text NOT NULL REFERENCES "PropertyOwner" ("id"),

   -- Tenant
   "organizationId" text NOT NULL DEFAULT current_org_id(),

   CONSTRAINT "Property_organizationId_code_key"
              UNIQUE ("organizationId", "code"),
   CONSTRAINT "Property_price_range_chk"
              CHECK ("minPrice" IS NULL OR "maxPrice" IS NULL
                     OR "minPrice" <= "maxPrice"),
   CONSTRAINT "Property_area_range_chk"
              CHECK ("minArea" IS NULL OR "maxArea" IS NULL
                     OR "minArea" <= "maxArea")
);

CREATE TRIGGER "Property_set_updated_at"
       BEFORE UPDATE ON "Property"
       FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX "Property_organizationId_idx"
          ON "Property" ("organizationId");
CREATE INDEX "Property_organizationId_status_idx"
          ON "Property" ("organizationId", "status");
CREATE INDEX "Property_organizationId_deletedAt_idx"
          ON "Property" ("organizationId", "deletedAt");
-- listagem filtrada por (organizationId, deletedAt) e ordenada por data
CREATE INDEX "Property_organizationId_deletedAt_createdAt_idx"
          ON "Property" ("organizationId", "deletedAt", "createdAt");
CREATE INDEX "Property_organizationId_deletedAt_updatedAt_idx"
          ON "Property" ("organizationId", "deletedAt", "updatedAt");
-- ordenação por preço e área
CREATE INDEX "Property_organizationId_deletedAt_minPrice_idx"
          ON "Property" ("organizationId", "deletedAt", "minPrice");
CREATE INDEX "Property_organizationId_deletedAt_maxPrice_idx"
          ON "Property" ("organizationId", "deletedAt", "maxPrice");
CREATE INDEX "Property_organizationId_deletedAt_minArea_idx"
          ON "Property" ("organizationId", "deletedAt", "minArea");
CREATE INDEX "Property_organizationId_deletedAt_maxArea_idx"
          ON "Property" ("organizationId", "deletedAt", "maxArea");
CREATE INDEX "Property_purpose_type_status_idx"
          ON "Property" ("purpose", "type", "status");
CREATE INDEX "Property_status_idx"    ON "Property" ("status");
CREATE INDEX "Property_deletedAt_idx" ON "Property" ("deletedAt");
