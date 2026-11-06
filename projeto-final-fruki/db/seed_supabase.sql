-- ============================================================================
-- Pede Fruki — massa de teste para Supabase/PostgreSQL
-- ATENCAO: use somente em ambiente de teste/demonstracao. NAO rode em producao.
-- Ids de teste: 1001 a 1020 (nao colidem com 41/48/81/112).
-- Ordem: tabela_preco -> cliente -> fato_vendas.
-- ============================================================================

BEGIN;

-- 1) Produtos de referencia (id prefixado com seed- para nao colidir)
INSERT INTO tabela_preco (id, cod_produto, nome_produto, preco_base) VALUES
  ('seed-00001', '00001', 'Fruki Guaraná 2L', 7.49),
  ('seed-00002', '00002', 'Água da Pedra 500ml', 2.29),
  ('seed-00003', '00003', 'Elev Energy Drink 473ml', 5.99),
  ('seed-00004', '00004', 'Cerveja Bellavista 473ml', 5.90),
  ('seed-00005', '00005', 'Suco COM/TEM Laranja 1L', 12.90),
  ('seed-00006', '00006', 'Fruki Cola 2L', 7.29),
  ('seed-00007', '00007', 'Fruki Guaraná Zero 2L', 7.49),
  ('seed-00008', '00008', 'Água da Pedra com Gás 500ml', 2.49),
  ('seed-00009', '00009', 'Tônica Fruki 350ml', 3.49),
  ('seed-00010', '00010', 'Refresco Frukito Uva 2L', 5.49) ON CONFLICT DO NOTHING;

-- 2) Clientes de teste
INSERT INTO cliente (cod_cliente, nome_fantasia, cnpj, cidade, uf, cod_hierarquia) VALUES
  (1001, 'Mercado Litoral 1001', '10000001000190', 'Capão da Canoa', 'RS', NULL),
  (1002, 'Super Litoral 1002', '10000002000134', 'Tramandaí', 'RS', NULL),
  (1003, 'Mercado das Torres 1003', '10000003000189', 'Torres', 'RS', NULL),
  (1004, 'Super Praia 1004', '10000004000123', 'Balneário Camboriú', 'SC', NULL),
  (1005, 'Empório Serra 1005', '10000005000178', 'Gramado', 'RS', NULL),
  (1006, 'Conveniência Canela 1006', '10000006000112', 'Canela', 'RS', NULL),
  (1007, 'Mercado Caxias 1007', '10000007000167', 'Caxias do Sul', 'RS', NULL),
  (1008, 'Adega Bento 1008', '10000008000101', 'Bento Gonçalves', 'RS', NULL),
  (1009, 'Distribuidora Vale 1009', '10000009000156', 'Lajeado', 'RS', NULL),
  (1010, 'Mercado Estrela 1010', '10000010000180', 'Estrela', 'RS', NULL),
  (1011, 'Loja Teutônia 1011', '10000011000125', 'Teutônia', 'RS', NULL),
  (1012, 'Mercado Encantado 1012', '10000012000170', 'Encantado', 'RS', NULL),
  (1013, 'Mercado Central 1013', '10000013000114', 'Porto Alegre', 'RS', NULL),
  (1014, 'Super Canoas 1014', '10000014000169', 'Canoas', 'RS', NULL),
  (1015, 'Mercado Santa Cruz 1015', '10000015000103', 'Santa Cruz do Sul', 'RS', NULL),
  (1016, 'Empório NH 1016', '10000016000158', 'Novo Hamburgo', 'RS', NULL),
  (1017, 'Mercado São Leopoldo 1017', '10000017000100', 'São Leopoldo', 'RS', NULL),
  (1018, 'Super Gravataí 1018', '10000018000147', 'Gravataí', 'RS', NULL),
  (1019, 'Mercado Ilha 1019', '10000019000191', 'Florianópolis', 'SC', NULL),
  (1020, 'Distribuidora Joinville 1020', '10000020000116', 'Joinville', 'SC', NULL) ON CONFLICT DO NOTHING;

-- 3) Compras de teste (2 por cliente) para o bot/Historico
DELETE FROM fato_vendas WHERE cod_cliente BETWEEN 1001 AND 1020;
INSERT INTO fato_vendas (cod_cliente, data_venda, valor_total) VALUES
  (1001, DATE '2026-10-02', 27.48),
  (1001, DATE '2026-11-03', 35.94),
  (1002, DATE '2026-10-03', 32.06),
  (1002, DATE '2026-11-01', 23.96),
  (1003, DATE '2026-10-04', 36.64),
  (1003, DATE '2026-11-02', 29.95),
  (1004, DATE '2026-10-05', 41.22),
  (1004, DATE '2026-11-03', 35.94),
  (1005, DATE '2026-10-06', 59.00),
  (1005, DATE '2026-11-01', 51.60),
  (1006, DATE '2026-10-07', 70.80),
  (1006, DATE '2026-11-02', 64.50),
  (1007, DATE '2026-10-08', 82.60),
  (1007, DATE '2026-11-03', 77.40),
  (1008, DATE '2026-10-09', 94.40),
  (1008, DATE '2026-11-01', 51.60),
  (1009, DATE '2026-10-10', 134.82),
  (1009, DATE '2026-11-02', 11.45),
  (1010, DATE '2026-10-11', 74.90),
  (1010, DATE '2026-11-03', 13.74),
  (1011, DATE '2026-10-12', 89.88),
  (1011, DATE '2026-11-01', 9.16),
  (1012, DATE '2026-10-13', 104.86),
  (1012, DATE '2026-11-02', 11.45),
  (1013, DATE '2026-10-14', 119.84),
  (1013, DATE '2026-11-03', 43.74),
  (1014, DATE '2026-10-15', 134.82),
  (1014, DATE '2026-11-01', 29.16),
  (1015, DATE '2026-10-16', 74.90),
  (1015, DATE '2026-11-02', 36.45),
  (1016, DATE '2026-10-17', 89.88),
  (1016, DATE '2026-11-03', 43.74),
  (1017, DATE '2026-10-18', 104.86),
  (1017, DATE '2026-11-01', 29.16),
  (1018, DATE '2026-10-19', 119.84),
  (1018, DATE '2026-11-02', 36.45),
  (1019, DATE '2026-10-20', 41.22),
  (1019, DATE '2026-11-03', 35.94),
  (1020, DATE '2026-10-01', 74.90),
  (1020, DATE '2026-11-01', 9.16);

COMMIT;

-- Conferencia:
-- SELECT cod_cliente, nome_fantasia, cidade, uf FROM cliente WHERE cod_cliente BETWEEN 1001 AND 1020 ORDER BY cod_cliente;
-- SELECT cod_cliente, COUNT(*) FROM fato_vendas WHERE cod_cliente BETWEEN 1001 AND 1020 GROUP BY cod_cliente ORDER BY cod_cliente;

-- Limpeza (descomente quando quiser remover a massa):
-- DELETE FROM fato_vendas WHERE cod_cliente BETWEEN 1001 AND 1020;
-- DELETE FROM cliente WHERE cod_cliente BETWEEN 1001 AND 1020;
-- DELETE FROM tabela_preco WHERE id LIKE 'seed-%';
