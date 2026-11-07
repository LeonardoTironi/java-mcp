-- =============================================================================
-- Dados de exemplo para o banco de imóveis
--
-- IDs e códigos fixos para que os testes de integração possam referenciá-los.
-- Inclui imóveis que NÃO devem aparecer nas buscas (DRAFT, SOLD, excluído e
-- oculto do site) para comprovar a filtragem.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Proprietários
-- -----------------------------------------------------------------------------
INSERT INTO "PropertyOwner" ("id", "name", "email", "phone") VALUES
('own-001', 'Maria Souza',             'maria.souza@example.com',  '(41) 99999-0001'),
('own-002', 'João Pereira',            'joao.pereira@example.com', '(43) 99999-0002'),
('own-003', 'Construtora Horizonte',   'contato@horizonte.example','(41) 3333-0003'),
('own-004', 'Ana Lima',                'ana.lima@example.com',     '(41) 99999-0004');

-- -----------------------------------------------------------------------------
-- Endereços
-- -----------------------------------------------------------------------------
INSERT INTO "Address" ("id", "street", "number", "extraInfo", "neighborhood",
                       "city", "state", "postalCode", "latitude", "longitude")
VALUES
-- Curitiba
('adr-001', 'Rua Bispo Dom José',         '1200', NULL,        'Batel',            'Curitiba',  'PR', '80440-080', -25.4419, -49.2914),
('adr-002', 'Av. República Argentina',    '850',  'Apto 1203', 'Água Verde',       'Curitiba',  'PR', '80240-210', -25.4512, -49.2869),
('adr-003', 'Rua XV de Novembro',         '400',  'Apto 802',  'Centro',           'Curitiba',  'PR', '80020-310', -25.4296, -49.2713),
('adr-004', 'Via Vêneto',                 '1500', NULL,        'Santa Felicidade', 'Curitiba',  'PR', '82015-010', -25.4040, -49.3290),
('adr-005', 'Rua Professor Nivaldo Braga','2300', NULL,        'Boqueirão',        'Curitiba',  'PR', '81730-000', -25.4983, -49.2391),
('adr-006', 'Rua Eduardo Sprada',         'S/N',  'Lote 12',   'Campo Comprido',   'Curitiba',  'PR', '81220-000', -25.4436, -49.3459),
('adr-007', 'Rua Marechal Deodoro',       '630',  'Sala 101',  'Centro',           'Curitiba',  'PR', '80010-010', -25.4321, -49.2681),
('adr-008', 'Rua Professor Pedro Viriato Parigot de Souza', '3000', NULL, 'Ecoville', 'Curitiba', 'PR', '81200-100', -25.4380, -49.3370),
('adr-009', 'Rua Martim Afonso',          '2100', NULL,        'Bigorrilho',       'Curitiba',  'PR', '80730-030', -25.4301, -49.3010),
('adr-010', 'Alameda Dom Pedro II',       '255',  'Apto 1501', 'Batel',            'Curitiba',  'PR', '80420-060', -25.4440, -49.2930),
-- Londrina
('adr-011', 'Rua Ulrico Zuínglio',        '900',  NULL,        'Gleba Palhano',    'Londrina',  'PR', '86050-350', -23.3330, -51.1840),
('adr-012', 'Av. Paraná',                 '320',  'Apto 504',  'Centro',           'Londrina',  'PR', '86010-390', -23.3105, -51.1596),
('adr-013', 'Rua Caracas',                '1150', 'Apto 1802', 'Gleba Palhano',    'Londrina',  'PR', '86050-070', -23.3350, -51.1790),
('adr-014', 'Estrada da Warta',           'Km 8', NULL,        'Zona Rural',       'Londrina',  'PR', '86100-000', -23.2400, -51.1500),
('adr-015', 'Rua Pernambuco',             '777',  NULL,        'Centro',           'Londrina',  'PR', '86020-120', -23.3090, -51.1620),
-- Paranaguá
('adr-016', 'Rua Manoel Bonifácio',       '410',  NULL,        'Costeira',         'Paranaguá', 'PR', '83203-410', -25.5300, -48.5150),
('adr-017', 'Rua XV de Novembro',         '88',   NULL,        'Centro',           'Paranaguá', 'PR', '83203-010', -25.5205, -48.5090),
-- endereço do condomínio Ecoville
('adr-018', 'Rua Professor Pedro Viriato Parigot de Souza', '3000', 'Condomínio', 'Ecoville', 'Curitiba', 'PR', '81200-100', -25.4380, -49.3370);

-- -----------------------------------------------------------------------------
-- Condomínios
-- -----------------------------------------------------------------------------
INSERT INTO "Condominium" ("id", "name", "addressId") VALUES
('cond-001', 'Residencial Parque Ecoville', 'adr-018'),
('cond-002', 'Edifício Palhano Tower',      NULL);

-- -----------------------------------------------------------------------------
-- Imóveis
-- -----------------------------------------------------------------------------
INSERT INTO "Property" ("id", "code", "title", "description", "extraInfo",
                        "type", "purpose", "status", "usageType",
                        "isFeatured", "showOnWebsite",
                        "minPrice", "maxPrice", "minArea", "maxArea",
                        "condominiumId", "addressId", "ownerId",
                        "publishedOn", "websiteLink", "videoUrl", "deletedAt")
VALUES
-- ===================== DISPONÍVEIS (aparecem nas buscas) =====================
('prop-001', 'IMV-0001', 'Casa com piscina no Batel',
 'Casa em rua tranquila, 3 quartos sendo 1 suíte, piscina, churrasqueira e jardim.',
 '{"bedrooms":3,"suites":1,"bathrooms":3,"parkingSpots":2,"amenities":["piscina","churrasqueira","jardim"]}',
 'HOUSE', 'SELL', 'AVAILABLE', 'RESIDENTIAL', true, true,
 850000, 850000, 180, 180, NULL, 'adr-001', 'own-001',
 '{site,zap}', 'https://imoveis.example/IMV-0001', 'https://youtu.be/exemplo0001', NULL),

('prop-002', 'IMV-0002', 'Apartamento 2 quartos na Água Verde',
 'Apartamento mobiliado, próximo ao comércio, com sacada e 1 vaga.',
 '{"bedrooms":2,"suites":0,"bathrooms":1,"parkingSpots":1,"furnished":true}',
 'APARTMENT', 'RENT', 'AVAILABLE', 'RESIDENTIAL', false, true,
 2800, 2800, 72, 72, NULL, 'adr-002', 'own-004',
 '{site}', NULL, NULL, NULL),

('prop-003', 'IMV-0003', 'Studio no Centro - venda ou locação',
 'Studio compacto ideal para estudantes, a duas quadras da Rua XV.',
 '{"bedrooms":1,"suites":0,"bathrooms":1,"parkingSpots":0,"rentPrice":1600}',
 'APARTMENT', 'BOTH', 'AVAILABLE', 'RESIDENTIAL', false, true,
 290000, 290000, 38, 38, NULL, 'adr-003', 'own-004',
 '{site,olx}', NULL, NULL, NULL),

('prop-004', 'IMV-0004', 'Casa de alto padrão em Santa Felicidade',
 'Sobrado com 4 suítes, home theater, área gourmet e terreno amplo.',
 '{"bedrooms":4,"suites":4,"bathrooms":5,"parkingSpots":4,"amenities":["piscina","area gourmet","home theater"]}',
 'HOUSE', 'SELL', 'AVAILABLE', 'RESIDENTIAL', true, true,
 1250000, 1250000, 260, 260, NULL, 'adr-004', 'own-001',
 '{site,zap,vivareal}', NULL, 'https://youtu.be/exemplo0004', NULL),

('prop-005', 'IMV-0005', 'Casa térrea para alugar no Boqueirão',
 'Casa com 3 quartos, quintal amplo e garagem para 2 carros. Aceita pet.',
 '{"bedrooms":3,"suites":0,"bathrooms":2,"parkingSpots":2,"petFriendly":true}',
 'HOUSE', 'RENT', 'AVAILABLE', 'RESIDENTIAL', false, true,
 3500, 3500, 140, 140, NULL, 'adr-005', 'own-002',
 '{site}', NULL, NULL, NULL),

('prop-006', 'IMV-0006', 'Terreno plano no Campo Comprido',
 'Terreno de esquina, plano, pronto para construir. Zoneamento residencial.',
 '{"bedrooms":0,"frontage":15}',
 'LAND', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 450000, 450000, 400, 400, NULL, 'adr-006', 'own-002',
 '{site}', NULL, NULL, NULL),

('prop-007', 'IMV-0007', 'Sala comercial no Centro',
 'Sala ampla em edifício comercial com recepção, 2 banheiros e copa.',
 '{"bedrooms":0,"bathrooms":2,"parkingSpots":1}',
 'COMMERCIAL', 'RENT', 'AVAILABLE', 'COMMERCIAL', false, true,
 8000, 8000, 220, 220, NULL, 'adr-007', 'own-003',
 '{site}', NULL, NULL, NULL),

('prop-008', 'IMV-0008', 'Lançamento Parque Ecoville - apartamentos de 2 a 3 quartos',
 'Empreendimento com unidades de 65 m² a 120 m², lazer completo e segurança 24h.',
 '{"bedrooms":2,"maxBedrooms":3,"suites":1,"bathrooms":2,"parkingSpots":1,"units":40}',
 'APARTMENT', 'SELL', 'AVAILABLE', 'RESIDENTIAL', true, true,
 520000, 890000, 65, 120, 'cond-001', 'adr-008', 'own-003',
 '{site,zap,vivareal}', 'https://imoveis.example/IMV-0008', NULL, NULL),

('prop-009', 'IMV-0009', 'Casa moderna na Gleba Palhano',
 'Casa em condomínio fechado, 3 quartos, espaço gourmet e energia solar.',
 '{"bedrooms":3,"suites":1,"bathrooms":3,"parkingSpots":2,"amenities":["energia solar","espaco gourmet"]}',
 'HOUSE', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 720000, 720000, 160, 160, NULL, 'adr-011', 'own-002',
 '{site}', NULL, NULL, NULL),

('prop-010', 'IMV-0010', 'Apartamento 2 quartos no Centro de Londrina',
 'Apartamento reformado, próximo ao calçadão, com portaria 24h.',
 '{"bedrooms":2,"suites":0,"bathrooms":1,"parkingSpots":1}',
 'APARTMENT', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 410000, 410000, 68, 68, NULL, 'adr-012', 'own-002',
 '{site}', NULL, NULL, NULL),

('prop-011', 'IMV-0011', 'Apartamento alto padrão para alugar na Gleba Palhano',
 'Vista para o lago Igapó, 3 quartos, varanda gourmet e 2 vagas.',
 '{"bedrooms":3,"suites":1,"bathrooms":2,"parkingSpots":2,"amenities":["vista para o lago","varanda gourmet"]}',
 'APARTMENT', 'RENT', 'AVAILABLE', 'RESIDENTIAL', true, true,
 4200, 4200, 110, 110, 'cond-002', 'adr-013', 'own-002',
 '{site,zap}', NULL, NULL, NULL),

('prop-012', 'IMV-0012', 'Sítio com casa sede próximo a Londrina',
 'Sítio de 20 hectares com casa sede, açude, pomar e curral.',
 '{"bedrooms":3,"suites":1,"bathrooms":2,"parkingSpots":4,"hectares":20}',
 'FARM', 'SELL', 'AVAILABLE', 'OTHER', false, true,
 2500000, 2500000, 200000, 200000, NULL, 'adr-014', 'own-002',
 '{site}', NULL, NULL, NULL),

('prop-013', 'IMV-0013', 'Casa 2 quartos na Costeira',
 'Casa simples, perto do porto e do comércio local. Ótima para primeira moradia.',
 '{"bedrooms":2,"suites":0,"bathrooms":1,"parkingSpots":1}',
 'HOUSE', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 320000, 320000, 95, 95, NULL, 'adr-016', 'own-004',
 '{site}', NULL, NULL, NULL),

('prop-014', 'IMV-0014', 'Casa para alugar no Centro Histórico de Paranaguá',
 'Casa com 2 quartos no centro histórico, próxima à Rua da Praia.',
 '{"bedrooms":2,"suites":0,"bathrooms":1,"parkingSpots":0}',
 'HOUSE', 'RENT', 'AVAILABLE', 'RESIDENTIAL', false, true,
 1800, 1800, 80, 80, NULL, 'adr-017', 'own-004',
 '{site}', NULL, NULL, NULL),

-- ================ NÃO DISPONÍVEIS (NÃO devem aparecer nas buscas) ============
('prop-015', 'IMV-0015', 'Casa no Bigorrilho (rascunho)',
 'Anúncio ainda em elaboração.',
 '{"bedrooms":3,"suites":1,"bathrooms":2,"parkingSpots":2}',
 'HOUSE', 'SELL', 'DRAFT', 'RESIDENTIAL', false, true,
 980000, 980000, 170, 170, NULL, 'adr-009', 'own-001',
 '{}', NULL, NULL, NULL),

('prop-016', 'IMV-0016', 'Apartamento no Batel (vendido)',
 'Apartamento de 3 quartos já vendido.',
 '{"bedrooms":3,"suites":1,"bathrooms":2,"parkingSpots":2}',
 'APARTMENT', 'SELL', 'SOLD', 'RESIDENTIAL', false, true,
 1100000, 1100000, 130, 130, NULL, 'adr-010', 'own-001',
 '{site}', NULL, NULL, NULL),

('prop-017', 'IMV-0017', 'Casa no Centro de Londrina (excluída)',
 'Anúncio removido.',
 '{"bedrooms":3,"suites":0,"bathrooms":2,"parkingSpots":1}',
 'HOUSE', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 550000, 550000, 150, 150, NULL, 'adr-015', 'own-002',
 '{}', NULL, NULL, now()),

('prop-018', 'IMV-0018', 'Terreno no Ecoville (oculto do site)',
 'Terreno disponível apenas para negociação interna.',
 '{"bedrooms":0}',
 'LAND', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, false,
 600000, 600000, 360, 360, NULL, NULL, 'own-003',
 '{}', NULL, NULL, NULL);
