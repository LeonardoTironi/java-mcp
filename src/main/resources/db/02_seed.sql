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


-- -----------------------------------------------------------------------------
-- Mais proprietários (own-005 a own-008)
-- -----------------------------------------------------------------------------
INSERT INTO "PropertyOwner" ("id", "name", "email", "phone") VALUES
('own-005', 'Carlos Henrique Dias',    'carlos.dias@example.com',   '(41) 99999-0005'),
('own-006', 'Fernanda Martins',        'fernanda.martins@example.com', '(43) 99999-0006'),
('own-007', 'Imobiliária Vale Verde',  'contato@valeverde.example', '(44) 3333-0007'),
('own-008', 'Ricardo Tanaka',          'ricardo.tanaka@example.com','(45) 99999-0008');

-- -----------------------------------------------------------------------------
-- Mais endereços (adr-019 a adr-068)
-- -----------------------------------------------------------------------------
INSERT INTO "Address" ("id", "street", "number", "extraInfo", "neighborhood",
                       "city", "state", "postalCode", "latitude", "longitude")
VALUES
-- Curitiba
('adr-019', 'Rua Presidente Carlos Cavalcanti', '1020', 'Apto 401',  'Bigorrilho',      'Curitiba', 'PR', '80730-000', -25.4290, -49.3005),
('adr-020', 'Rua Coronel Dulcídio',             '1540', 'Apto 92',   'Cabral',          'Curitiba', 'PR', '80035-050', -25.4190, -49.2650),
('adr-021', 'Rua Schiffer',                     '640',  NULL,        'Portão',          'Curitiba', 'PR', '80610-000', -25.4780, -49.3230),
('adr-022', 'Rua Oscar Marques Rocha',          'S/N',  'Lote 7',    'Mossunguê',       'Curitiba', 'PR', '81200-500', -25.4520, -49.3520),
('adr-023', 'Rua Brigadeiro Franco',            '2500', 'Loja 3',    'Alto da XV',      'Curitiba', 'PR', '80045-300', -25.4150, -49.2570),
('adr-024', 'Rua Padre Anchieta',               '1700', 'Apto 61',   'Juvevê',          'Curitiba', 'PR', '80540-000', -25.4300, -49.2650),
('adr-025', 'Rua Carlos de Carvalho',           '1190', 'Apto 1104', 'Água Verde',      'Curitiba', 'PR', '80610-120', -25.4530, -49.2790),
('adr-026', 'Rua Marechal Floriano Peixoto',    '320',  'Sala 5',    'Centro',          'Curitiba', 'PR', '80010-150', -25.4290, -49.2700),
('adr-027', 'Rua Tenente Pinto',                'S/N',  'Área rural','Ahú',             'Curitiba', 'PR', '80540-010', -25.4170, -49.2600),
('adr-028', 'Rua Gustavo Lange',                '980',  'Apto 1402', 'Batel',           'Curitiba', 'PR', '80420-010', -25.4430, -49.2900),
('adr-029', 'Rua Quintino Bocaiúva',            '455',  NULL,        'Santa Felicidade','Curitiba', 'PR', '82025-000', -25.3990, -49.3260),
('adr-030', 'Rua Emiliano Perneta',             '610',  'Apto 303',  'Batel',           'Curitiba', 'PR', '80420-050', -25.4420, -49.2910),
-- Londrina
('adr-031', 'Rua Hélio Chafick Ramadan',        '215',  NULL,        'Jardim Shangri-lá','Londrina', 'PR', '86060-000', -23.3290, -51.1700),
('adr-032', 'Rua Mato Grosso',                  '780',  NULL,        'Bela Suíça',      'Londrina', 'PR', '86050-410', -23.3220, -51.1520),
('adr-033', 'Rua Sergipe',                      '400',  'Apto 1001', 'Centro',          'Londrina', 'PR', '86020-030', -23.3100, -51.1610),
('adr-034', 'Rua Bahia',                        '1320', 'Apto 2202', 'Gleba Palhano',   'Londrina', 'PR', '86050-380', -23.3360, -51.1810),
('adr-035', 'Av. Higienópolis',                 '1010', 'Loja 2',    'Centro',          'Londrina', 'PR', '86020-040', -23.3120, -51.1640),
('adr-036', 'Estrada Rural da Warta',           'Km 12','Sítio',     'Zona Rural',      'Londrina', 'PR', '86100-000', -23.2550, -51.1420),
('adr-037', 'Rua Jorge Rodrigues Costa',        '590',  NULL,        'Aurora',          'Londrina', 'PR', '86010-290', -23.3050, -51.1480),
('adr-038', 'Rua Santos Dumont',                '1100', 'Apto 401',  'Higienópolis',    'Londrina', 'PR', '86015-010', -23.3150, -51.1670),
('adr-039', 'Rua Professor Hugo Rocha',         'S/N',  'Lote 22',   'Champagnat',      'Londrina', 'PR', '86060-210', -23.3180, -51.1790),
('adr-040', 'Av. Dez de Dezembro',              '2200', 'Apto 808',  'Champagnat',      'Londrina', 'PR', '86060-020', -23.3210, -51.1750),
-- Paranaguá
('adr-041', 'Rua Júlio Pereira',                '300',  NULL,        'Vila Operária',   'Paranaguá','PR', '83203-120', -25.5160, -48.5180),
('adr-042', 'Rua Coronel Macedo',               '150',  NULL,        'Ponta do Caju',   'Paranaguá','PR', '83203-300', -25.5390, -48.5060),
('adr-043', 'Rua Coronel Macedo',               'S/N',  'Lote 4',    'Ponta do Caju',   'Paranaguá','PR', '83203-300', -25.5400, -48.5050),
('adr-044', 'Rua General Carneiro',             '270',  'Loja A',    'Centro',          'Paranaguá','PR', '83203-020', -25.5200, -48.5100),
('adr-045', 'Rua Conselheiro Sinimbu',          '66',   'Apto 2',    'Centro',          'Paranaguá','PR', '83203-030', -25.5210, -48.5085),
-- Maringá
('adr-046', 'Avenida Tuiuti',                   '1450', 'Apto 605',  'Zona 7',          'Maringá',  'PR', '87020-000', -23.4150, -51.9330),
('adr-047', 'Rua Pernambuco',                   '1870', NULL,        'Zona 3',          'Maringá',  'PR', '87013-000', -23.4210, -51.9290),
('adr-048', 'Rua Sebastião Ferreira',           '720',  NULL,        'Zona 7',          'Maringá',  'PR', '87020-170', -23.4120, -51.9360),
('adr-049', 'Avenida Duque de Caxias',          '1200', 'Sala 12',   'Zona 5',          'Maringá',  'PR', '87050-000', -23.4280, -51.9400),
('adr-050', 'Rua Joubert de Carvalho',          '805',  'Lote 9',    'Jardim Liberdade','Maringá',  'PR', '87045-000', -23.4470, -51.9510),
('adr-051', 'Avenida Mauá',                     '1005', 'Apto 302',  'Zona 3',          'Maringá',  'PR', '87013-080', -23.4190, -51.9310),
('adr-052', 'Rua Getúlio Vargas',               '3100', 'Apto 1101', 'Zona 1',          'Maringá',  'PR', '87010-000', -23.4240, -51.9260),
-- Ponta Grossa
('adr-053', 'Rua XV de Novembro',               '1350', 'Apto 502',  'Centro',          'Ponta Grossa','PR','84010-200', -25.0940, -50.1600),
('adr-054', 'Rua Presidente Vargas',            '870',  NULL,        'Oficinas',        'Ponta Grossa','PR','84030-050', -25.0880, -50.1550),
('adr-055', 'Rua Dr. Coelho Neto',              '430',  NULL,        'Uvaranas',        'Ponta Grossa','PR','84030-290', -25.0250, -50.0520),
('adr-056', 'Rua Joaquim Nabuco',               'S/N',  'Lote 15',   'Olarias',         'Ponta Grossa','PR','84035-000', -25.1000, -50.1420),
('adr-057', 'Rua Balduíno Taques',              '210',  'Loja 1',    'Centro',          'Ponta Grossa','PR','84010-160', -25.0930, -50.1610),
('adr-058', 'Rua Onze de Maio',                 '515',  'Apto 203',  'Oficinas',        'Ponta Grossa','PR','84030-070', -25.0870, -50.1560),
-- Foz do Iguaçu
('adr-059', 'Avenida Juscelino Kubitschek',     '1500', 'Apto 1703', 'Centro',          'Foz do Iguaçu','PR','85851-000', -25.5470, -54.5880),
('adr-060', 'Rua Rio Grande do Sul',            '2330', NULL,        'Vila Yolanda',    'Foz do Iguaçu','PR','85853-150', -25.5230, -54.5790),
('adr-061', 'Rua Marechal Deodoro',             '1011', 'Apto 81',   'Centro',          'Foz do Iguaçu','PR','85851-010', -25.5440, -54.5860),
('adr-062', 'Estrada do Loteamento Cataratas',  'Km 3', 'Lote 31',   'Loteamento Cataratas','Foz do Iguaçu','PR','85856-000', -25.5800, -54.5300),
('adr-063', 'Avenida Brasil',                   '2800', 'Loja 6',    'Centro',          'Foz do Iguaçu','PR','85851-040', -25.5490, -54.5840),
('adr-064', 'Rua Padre Anchieta',               '640',  NULL,        'Vila Yolanda',    'Foz do Iguaçu','PR','85853-010', -25.5260, -54.5800),
-- Cascavel
('adr-065', 'Rua Rio Grande do Sul',            '1620', 'Apto 402',  'Centro',          'Cascavel', 'PR', '85801-020', -24.9560, -53.4550),
('adr-066', 'Rua Pedro Ferreira Neves',         '1875', NULL,        'Parque São Paulo','Cascavel', 'PR', '85806-010', -24.9400, -53.4700),
('adr-067', 'Rua Madre Leônia Milito',          '3010', NULL,        'Interlagos',      'Cascavel', 'PR', '85814-120', -24.9700, -53.4620),
('adr-068', 'Rua Projetada',                    'S/N',  'Lote 18',   'Universitário',   'Cascavel', 'PR', '85819-110', -24.9590, -53.4400);

-- -----------------------------------------------------------------------------
-- Mais imóveis (IMV-0019 a IMV-0068)
-- -----------------------------------------------------------------------------
INSERT INTO "Property" ("id", "code", "title", "description", "extraInfo",
                        "type", "purpose", "status", "usageType",
                        "isFeatured", "showOnWebsite",
                        "minPrice", "maxPrice", "minArea", "maxArea",
                        "condominiumId", "addressId", "ownerId",
                        "publishedOn", "websiteLink", "videoUrl", "deletedAt")
VALUES
-- ---------------------------------- Curitiba ----------------------------------
('prop-019', 'IMV-0019', 'Apartamento 3 quartos no Bigorrilho',
 'Apartamento com suíte, varanda com churrasqueira e vaga coberta.',
 '{"bedrooms":3,"suites":1,"bathrooms":2,"parkingSpots":1}',
 'APARTMENT', 'SELL', 'AVAILABLE', 'RESIDENTIAL', true, true,
 780000, 780000, 115, 115, NULL, 'adr-019', 'own-005',
 '{site,zap}', NULL, NULL, NULL),

('prop-020', 'IMV-0020', 'Apartamento 2 quartos no Cabral para alugar',
 'Apartamento claro, próximo a escolas e ao transporte coletivo.',
 '{"bedrooms":2,"suites":0,"bathrooms":1,"parkingSpots":1,"petFriendly":true}',
 'APARTMENT', 'RENT', 'AVAILABLE', 'RESIDENTIAL', false, true,
 2200, 2200, 60, 60, NULL, 'adr-020', 'own-005',
 '{site}', NULL, NULL, NULL),

('prop-021', 'IMV-0021', 'Casa com quintal no Portão para alugar',
 'Casa de 3 quartos com quintal amplo e garagem para 2 carros.',
 '{"bedrooms":3,"suites":0,"bathrooms":2,"parkingSpots":2,"amenities":["quintal","garagem"]}',
 'HOUSE', 'RENT', 'AVAILABLE', 'RESIDENTIAL', false, true,
 3200, 3200, 150, 150, NULL, 'adr-021', 'own-005',
 '{site,olx}', NULL, NULL, NULL),

('prop-022', 'IMV-0022', 'Terreno em Mossunguê',
 'Terreno plano, com 600 m², em região de alto padrão e acesso asfaltado.',
 '{"bedrooms":0,"frontage":20}',
 'LAND', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 690000, 690000, 600, 600, NULL, 'adr-022', 'own-003',
 '{site,vivareal}', NULL, NULL, NULL),

('prop-023', 'IMV-0023', 'Sala comercial no Alto da XV',
 'Loja com vitrine ampla e banheiro privativo, em avenida movimentada.',
 '{"bedrooms":0,"bathrooms":1,"parkingSpots":0}',
 'COMMERCIAL', 'RENT', 'AVAILABLE', 'COMMERCIAL', false, true,
 5500, 5500, 90, 90, NULL, 'adr-023', 'own-003',
 '{site}', NULL, NULL, NULL),

('prop-024', 'IMV-0024', 'Apartamento 2 quartos na Juvevê',
 'Apartamento reformado, cozinha planejada e área de lazer no prédio.',
 '{"bedrooms":2,"suites":0,"bathrooms":1,"parkingSpots":1}',
 'APARTMENT', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 395000, 395000, 58, 58, NULL, 'adr-024', 'own-007',
 '{site,zap}', NULL, NULL, NULL),

('prop-025', 'IMV-0025', 'Apartamento 3 quartos na Água Verde',
 'Apartamento com suíte e sacada com vista para o bairro, portaria 24h.',
 '{"bedrooms":3,"suites":1,"bathrooms":2,"parkingSpots":2,"amenities":["portaria 24h","sacada"]}',
 'APARTMENT', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 640000, 640000, 98, 98, NULL, 'adr-025', 'own-006',
 '{site}', NULL, NULL, NULL),

('prop-026', 'IMV-0026', 'Loja no Centro de Curitiba',
 'Loja de rua com grande fluxo de pedestres, ideal para comércio ou escritório.',
 '{"bedrooms":0,"bathrooms":1,"parkingSpots":0}',
 'COMMERCIAL', 'SELL', 'AVAILABLE', 'COMMERCIAL', false, true,
 1250000, 1250000, 310, 310, NULL, 'adr-026', 'own-003',
 '{site,olx}', NULL, NULL, NULL),

('prop-027', 'IMV-0027', 'Terreno no Ahú',
 'Terreno com 360 m² em rua tranquila, próximo ao Bosque do Papa.',
 '{"bedrooms":0,"frontage":12}',
 'LAND', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 520000, 520000, 360, 360, NULL, 'adr-027', 'own-006',
 '{site}', NULL, NULL, NULL),

('prop-028', 'IMV-0028', 'Apartamento no Batel - venda ou locação',
 'Apartamento com 3 quartos, mobiliado para locação de temporada ou venda.',
 '{"bedrooms":3,"suites":1,"bathrooms":2,"parkingSpots":2,"rentPrice":4800}',
 'APARTMENT', 'BOTH', 'AVAILABLE', 'RESIDENTIAL', true, true,
 960000, 960000, 105, 105, NULL, 'adr-028', 'own-001',
 '{site,zap,vivareal}', NULL, NULL, NULL),

('prop-029', 'IMV-0029', 'Casa para alugar em Santa Felicidade',
 'Casa de 2 quartos com varanda, perto de restaurantes típicos.',
 '{"bedrooms":2,"suites":0,"bathrooms":2,"parkingSpots":2}',
 'HOUSE', 'RENT', 'AVAILABLE', 'RESIDENTIAL', false, true,
 2800, 2800, 120, 120, NULL, 'adr-029', 'own-006',
 '{site}', NULL, NULL, NULL),

('prop-030', 'IMV-0030', 'Kitnet mobiliada no Batel para alugar',
 'Kitnet com 1 quarto, mobiliada e com lavanderia no prédio.',
 '{"bedrooms":1,"suites":0,"bathrooms":1,"parkingSpots":0,"furnished":true}',
 'APARTMENT', 'RENT', 'AVAILABLE', 'RESIDENTIAL', false, true,
 3300, 3300, 50, 50, NULL, 'adr-030', 'own-001',
 '{site,olx}', NULL, NULL, NULL),

-- ---------------------------------- Londrina ----------------------------------
('prop-031', 'IMV-0031', 'Casa 3 quartos no Jardim Shangri-lá',
 'Casa térrea com quintal, churrasqueira e garagem coberta.',
 '{"bedrooms":3,"suites":1,"bathrooms":2,"parkingSpots":2,"amenities":["churrasqueira","quintal"]}',
 'HOUSE', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 690000, 690000, 180, 180, NULL, 'adr-031', 'own-002',
 '{site,zap}', NULL, NULL, NULL),

('prop-032', 'IMV-0032', 'Casa para alugar na Bela Suíça',
 'Casa com 3 quartos, ampla área externa e portão eletrônico.',
 '{"bedrooms":3,"suites":0,"bathrooms":2,"parkingSpots":2}',
 'HOUSE', 'RENT', 'AVAILABLE', 'RESIDENTIAL', false, true,
 2600, 2600, 130, 130, NULL, 'adr-032', 'own-002',
 '{site}', NULL, NULL, NULL),

('prop-033', 'IMV-0033', 'Apartamento 2 quartos no Centro de Londrina - reformado',
 'Apartamento com piso novo, armários embutidos e vaga de garagem.',
 '{"bedrooms":2,"suites":0,"bathrooms":1,"parkingSpots":1}',
 'APARTMENT', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 350000, 350000, 62, 62, NULL, 'adr-033', 'own-002',
 '{site}', NULL, NULL, NULL),

('prop-034', 'IMV-0034', 'Apartamento 3 quartos e 2 suítes na Gleba Palhano',
 'Apartamento amplo, varanda gourmet, 2 vagas e área de lazer completa.',
 '{"bedrooms":3,"suites":2,"bathrooms":3,"parkingSpots":2,"amenities":["varanda gourmet","academia","piscina"]}',
 'APARTMENT', 'SELL', 'AVAILABLE', 'RESIDENTIAL', true, true,
 890000, 890000, 125, 125, 'cond-002', 'adr-034', 'own-002',
 '{site,zap,vivareal}', NULL, NULL, NULL),

('prop-035', 'IMV-0035', 'Sala comercial na Avenida Higienópolis',
 'Loja com 70 m², pé-direito alto e acesso para cargas.',
 '{"bedrooms":0,"bathrooms":1,"parkingSpots":1}',
 'COMMERCIAL', 'RENT', 'AVAILABLE', 'COMMERCIAL', false, true,
 3800, 3800, 70, 70, NULL, 'adr-035', 'own-007',
 '{site}', NULL, NULL, NULL),

('prop-036', 'IMV-0036', 'Chácara de 2 alqueires na Zona Rural',
 'Chácara com casa sede, pomar de citros, açude e nascente.',
 '{"bedrooms":2,"suites":0,"bathrooms":1,"parkingSpots":4,"hectares":4.84}',
 'FARM', 'SELL', 'AVAILABLE', 'OTHER', false, true,
 1200000, 1200000, 48400, 48400, NULL, 'adr-036', 'own-002',
 '{site}', NULL, NULL, NULL),

('prop-037', 'IMV-0037', 'Casa 3 quartos na Aurora',
 'Casa com varanda, quintal e cozinha ampla, em bairro residencial.',
 '{"bedrooms":3,"suites":0,"bathrooms":2,"parkingSpots":1}',
 'HOUSE', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 420000, 420000, 110, 110, NULL, 'adr-037', 'own-004',
 '{site,olx}', NULL, NULL, NULL),

('prop-038', 'IMV-0038', 'Apartamento 1 quarto na Higienópolis para alugar',
 'Apartamento compacto, próximo à universidade e ao comércio.',
 '{"bedrooms":1,"suites":0,"bathrooms":1,"parkingSpots":0}',
 'APARTMENT', 'RENT', 'AVAILABLE', 'RESIDENTIAL', false, true,
 1900, 1900, 55, 55, NULL, 'adr-038', 'own-004',
 '{site}', NULL, NULL, NULL),

('prop-039', 'IMV-0039', 'Terreno no Champagnat',
 'Terreno de 300 m² em região residencial consolidada.',
 '{"bedrooms":0,"frontage":10}',
 'LAND', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 260000, 260000, 300, 300, NULL, 'adr-039', 'own-007',
 '{site}', NULL, NULL, NULL),

('prop-040', 'IMV-0040', 'Apartamento 3 quartos no Champagnat',
 'Apartamento com 3 quartos, sacada e box de armários na cozinha.',
 '{"bedrooms":3,"suites":0,"bathrooms":2,"parkingSpots":1}',
 'APARTMENT', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 510000, 510000, 84, 84, NULL, 'adr-040', 'own-004',
 '{site,zap}', NULL, NULL, NULL),

-- --------------------------------- Paranaguá ----------------------------------
('prop-041', 'IMV-0041', 'Casa 2 quartos na Vila Operária',
 'Casa de alvenaria com quintal e garagem, a poucas quadras do porto.',
 '{"bedrooms":2,"suites":0,"bathrooms":1,"parkingSpots":1}',
 'HOUSE', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 275000, 275000, 120, 120, NULL, 'adr-041', 'own-004',
 '{site}', NULL, NULL, NULL),

('prop-042', 'IMV-0042', 'Casa para alugar na Ponta do Caju',
 'Casa de 3 quartos, a 5 minutos da praia, com área de lazer.',
 '{"bedrooms":3,"suites":0,"bathrooms":2,"parkingSpots":2,"amenities":["área de lazer"]}',
 'HOUSE', 'RENT', 'AVAILABLE', 'RESIDENTIAL', false, true,
 1300, 1300, 90, 90, NULL, 'adr-042', 'own-004',
 '{site}', NULL, NULL, NULL),

('prop-043', 'IMV-0043', 'Terreno na Ponta do Caju',
 'Terreno de 450 m², plano, a poucos minutos do centro histórico.',
 '{"bedrooms":0,"frontage":15}',
 'LAND', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 180000, 180000, 450, 450, NULL, 'adr-043', 'own-007',
 '{site,olx}', NULL, NULL, NULL),

('prop-044', 'IMV-0044', 'Loja para alugar no Centro de Paranaguá',
 'Loja com 60 m², frente para a rua e banheiro.',
 '{"bedrooms":0,"bathrooms":1,"parkingSpots":0}',
 'COMMERCIAL', 'RENT', 'AVAILABLE', 'COMMERCIAL', false, true,
 2200, 2200, 60, 60, NULL, 'adr-044', 'own-007',
 '{site}', NULL, NULL, NULL),

('prop-045', 'IMV-0045', 'Apartamento 2 quartos no Centro de Paranaguá',
 'Apartamento simples, com vaga e bom custo-benefício para primeira moradia.',
 '{"bedrooms":2,"suites":0,"bathrooms":1,"parkingSpots":1}',
 'APARTMENT', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 240000, 240000, 65, 65, NULL, 'adr-045', 'own-004',
 '{site}', NULL, NULL, NULL),

-- ---------------------------------- Maringá -----------------------------------
('prop-046', 'IMV-0046', 'Apartamento 2 quartos na Zona 7 de Maringá',
 'Apartamento com sacada, 1 vaga e prédio com portaria.',
 '{"bedrooms":2,"suites":0,"bathrooms":1,"parkingSpots":1}',
 'APARTMENT', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 420000, 420000, 75, 75, NULL, 'adr-046', 'own-008',
 '{site,zap}', NULL, NULL, NULL),

('prop-047', 'IMV-0047', 'Casa 3 quartos e suíte na Zona 3 de Maringá',
 'Casa com varanda, piscina, churrasqueira e garagem para 2 carros.',
 '{"bedrooms":3,"suites":1,"bathrooms":3,"parkingSpots":2,"amenities":["piscina","churrasqueira"]}',
 'HOUSE', 'SELL', 'AVAILABLE', 'RESIDENTIAL', true, true,
 650000, 650000, 200, 200, NULL, 'adr-047', 'own-008',
 '{site,zap,vivareal}', 'https://imoveis.example/IMV-0047', NULL, NULL),

('prop-048', 'IMV-0048', 'Casa para alugar na Zona 7 de Maringá',
 'Casa de 3 quartos com quintal e área gourmet.',
 '{"bedrooms":3,"suites":0,"bathrooms":2,"parkingSpots":2}',
 'HOUSE', 'RENT', 'AVAILABLE', 'RESIDENTIAL', false, true,
 2400, 2400, 110, 110, NULL, 'adr-048', 'own-008',
 '{site}', NULL, NULL, NULL),

('prop-049', 'IMV-0049', 'Sala comercial na Zona 5 de Maringá',
 'Sala comercial com 240 m², banheiros e estacionamento para clientes.',
 '{"bedrooms":0,"bathrooms":2,"parkingSpots":4}',
 'COMMERCIAL', 'SELL', 'AVAILABLE', 'COMMERCIAL', false, true,
 890000, 890000, 240, 240, NULL, 'adr-049', 'own-007',
 '{site}', NULL, NULL, NULL),

('prop-050', 'IMV-0050', 'Terreno no Jardim Liberdade',
 'Terreno de 360 m² em loteamento fechado, pronto para construir.',
 '{"bedrooms":0,"frontage":12}',
 'LAND', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 310000, 310000, 360, 360, NULL, 'adr-050', 'own-008',
 '{site}', NULL, NULL, NULL),

('prop-051', 'IMV-0051', 'Apartamento 2 quartos na Zona 3 para alugar',
 'Apartamento com varanda e vaga, a duas quadras do terminal.',
 '{"bedrooms":2,"suites":0,"bathrooms":1,"parkingSpots":1,"petFriendly":true}',
 'APARTMENT', 'RENT', 'AVAILABLE', 'RESIDENTIAL', false, true,
 1700, 1700, 62, 62, NULL, 'adr-051', 'own-008',
 '{site,olx}', NULL, NULL, NULL),

('prop-052', 'IMV-0052', 'Apartamento na Zona 1 - venda ou locação',
 'Apartamento de 3 quartos no centro de Maringá, aceita financiamento.',
 '{"bedrooms":3,"suites":0,"bathrooms":2,"parkingSpots":1,"rentPrice":2600}',
 'APARTMENT', 'BOTH', 'AVAILABLE', 'RESIDENTIAL', false, true,
 530000, 530000, 88, 88, NULL, 'adr-052', 'own-008',
 '{site,zap}', NULL, NULL, NULL),

-- ------------------------------ Ponta Grossa ----------------------------------
('prop-053', 'IMV-0053', 'Apartamento 2 quartos no Centro de Ponta Grossa',
 'Apartamento com sacada e vaga coberta, a poucos passos da praça central.',
 '{"bedrooms":2,"suites":0,"bathrooms":1,"parkingSpots":1}',
 'APARTMENT', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 380000, 380000, 70, 70, NULL, 'adr-053', 'own-004',
 '{site,zap}', NULL, NULL, NULL),

('prop-054', 'IMV-0054', 'Casa 3 quartos na Oficinas',
 'Casa com 3 quartos, sendo 1 suíte, e quintal com churrasqueira.',
 '{"bedrooms":3,"suites":1,"bathrooms":2,"parkingSpots":2,"amenities":["churrasqueira"]}',
 'HOUSE', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 540000, 540000, 160, 160, NULL, 'adr-054', 'own-006',
 '{site,olx}', NULL, NULL, NULL),

('prop-055', 'IMV-0055', 'Casa para alugar no Uvaranas',
 'Casa de 2 quartos em bairro tranquilo, perto da universidade.',
 '{"bedrooms":2,"suites":0,"bathrooms":1,"parkingSpots":1,"petFriendly":true}',
 'HOUSE', 'RENT', 'AVAILABLE', 'RESIDENTIAL', false, true,
 1500, 1500, 95, 95, NULL, 'adr-055', 'own-006',
 '{site}', NULL, NULL, NULL),

('prop-056', 'IMV-0056', 'Terreno nas Olarias',
 'Terreno de 300 m² em rua asfaltada, com toda infraestrutura.',
 '{"bedrooms":0,"frontage":10}',
 'LAND', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 150000, 150000, 300, 300, NULL, 'adr-056', 'own-004',
 '{site}', NULL, NULL, NULL),

('prop-057', 'IMV-0057', 'Loja no Centro de Ponta Grossa para alugar',
 'Loja com 100 m², mezanino e depósito nos fundos.',
 '{"bedrooms":0,"bathrooms":1,"parkingSpots":0}',
 'COMMERCIAL', 'RENT', 'AVAILABLE', 'COMMERCIAL', false, true,
 2900, 2900, 100, 100, NULL, 'adr-057', 'own-006',
 '{site}', NULL, NULL, NULL),

('prop-058', 'IMV-0058', 'Apartamento 1 quarto na Oficinas para alugar',
 'Apartamento compacto e mobiliado, ideal para profissionais.',
 '{"bedrooms":1,"suites":0,"bathrooms":1,"parkingSpots":0,"furnished":true}',
 'APARTMENT', 'RENT', 'AVAILABLE', 'RESIDENTIAL', false, true,
 1200, 1200, 48, 48, NULL, 'adr-058', 'own-006',
 '{site,olx}', NULL, NULL, NULL),

-- ------------------------------- Foz do Iguaçu --------------------------------
('prop-059', 'IMV-0059', 'Apartamento 3 quartos no Centro de Foz do Iguaçu',
 'Apartamento com 1 suíte, varanda e 1 vaga, a 10 min das Cataratas.',
 '{"bedrooms":3,"suites":1,"bathrooms":2,"parkingSpots":1}',
 'APARTMENT', 'SELL', 'AVAILABLE', 'RESIDENTIAL', true, true,
 460000, 460000, 95, 95, NULL, 'adr-059', 'own-001',
 '{site,zap,vivareal}', NULL, NULL, NULL),

('prop-060', 'IMV-0060', 'Casa 4 quartos na Vila Yolanda',
 'Casa ampla com 2 suítes, piscina, área gourmet e garagem para 3 carros.',
 '{"bedrooms":4,"suites":2,"bathrooms":3,"parkingSpots":3,"amenities":["piscina","área gourmet"]}',
 'HOUSE', 'SELL', 'AVAILABLE', 'RESIDENTIAL', true, true,
 720000, 720000, 210, 210, NULL, 'adr-060', 'own-001',
 '{site,zap}', 'https://imoveis.example/IMV-0060', NULL, NULL),

('prop-061', 'IMV-0061', 'Apartamento 2 quartos no Centro de Foz para alugar',
 'Apartamento mobiliado, próximo a restaurantes e à rodoviária.',
 '{"bedrooms":2,"suites":0,"bathrooms":1,"parkingSpots":1,"furnished":true}',
 'APARTMENT', 'RENT', 'AVAILABLE', 'RESIDENTIAL', false, true,
 2000, 2000, 65, 65, NULL, 'adr-061', 'own-001',
 '{site}', NULL, NULL, NULL),

('prop-062', 'IMV-0062', 'Terreno no Loteamento Cataratas',
 'Lote de 420 m² em loteamento com segurança e área verde.',
 '{"bedrooms":0,"frontage":14}',
 'LAND', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 240000, 240000, 420, 420, NULL, 'adr-062', 'own-007',
 '{site,olx}', NULL, NULL, NULL),

('prop-063', 'IMV-0063', 'Sala comercial na Avenida Brasil',
 'Sala de 130 m² com recepção, copa e 2 banheiros.',
 '{"bedrooms":0,"bathrooms":2,"parkingSpots":2}',
 'COMMERCIAL', 'RENT', 'AVAILABLE', 'COMMERCIAL', false, true,
 4200, 4200, 130, 130, NULL, 'adr-063', 'own-007',
 '{site}', NULL, NULL, NULL),

('prop-064', 'IMV-0064', 'Casa 3 quartos para alugar na Vila Yolanda',
 'Casa com 3 quartos, quintal e garagem coberta, perto do comércio.',
 '{"bedrooms":3,"suites":0,"bathrooms":2,"parkingSpots":2}',
 'HOUSE', 'RENT', 'AVAILABLE', 'RESIDENTIAL', false, true,
 2700, 2700, 150, 150, NULL, 'adr-064', 'own-001',
 '{site}', NULL, NULL, NULL),

-- ---------------------------------- Cascavel ----------------------------------
('prop-065', 'IMV-0065', 'Apartamento 2 quartos no Centro de Cascavel',
 'Apartamento com sacada, armários planejados e 1 vaga.',
 '{"bedrooms":2,"suites":0,"bathrooms":1,"parkingSpots":1}',
 'APARTMENT', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 350000, 350000, 66, 66, NULL, 'adr-065', 'own-008',
 '{site,zap}', NULL, NULL, NULL),

('prop-066', 'IMV-0066', 'Casa 3 quartos no Parque São Paulo',
 'Casa com suíte, churrasqueira e quintal, em bairro residencial.',
 '{"bedrooms":3,"suites":1,"bathrooms":2,"parkingSpots":2,"amenities":["churrasqueira"]}',
 'HOUSE', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 430000, 430000, 140, 140, NULL, 'adr-066', 'own-008',
 '{site,olx}', NULL, NULL, NULL),

('prop-067', 'IMV-0067', 'Casa para alugar no Interlagos',
 'Casa de 3 quartos com varanda, perto de escolas e parques.',
 '{"bedrooms":3,"suites":0,"bathrooms":2,"parkingSpots":1}',
 'HOUSE', 'RENT', 'AVAILABLE', 'RESIDENTIAL', false, true,
 1600, 1600, 100, 100, NULL, 'adr-067', 'own-008',
 '{site}', NULL, NULL, NULL),

('prop-068', 'IMV-0068', 'Terreno no bairro Universitário',
 'Terreno de 360 m², plano, em região em expansão.',
 '{"bedrooms":0,"frontage":12}',
 'LAND', 'SELL', 'AVAILABLE', 'RESIDENTIAL', false, true,
 195000, 195000, 360, 360, NULL, 'adr-068', 'own-008',
 '{site}', NULL, NULL, NULL);
