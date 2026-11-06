-- ============================================================================
-- Pede Fruki — somente CLIENTES de teste (Supabase/PostgreSQL)
-- Use este arquivo quando o seed completo falhar por causa de tabela_preco.
-- Ids: 1001 a 1020 + 9001 (Loja Teste do painel).
-- ============================================================================

INSERT INTO cliente (cod_cliente, nome_fantasia, cnpj, cidade, uf, cod_hierarquia) VALUES
  (9001, 'Loja Teste', '11222333000181', 'Lajeado', 'RS', NULL),
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
  (1020, 'Distribuidora Joinville 1020', '10000020000116', 'Joinville', 'SC', NULL)
ON CONFLICT DO NOTHING;

-- Conferência
SELECT cod_cliente, nome_fantasia, cnpj, cidade, uf
FROM cliente
WHERE cod_cliente BETWEEN 1001 AND 1020 OR cod_cliente = 9001
ORDER BY cod_cliente;
