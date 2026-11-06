-- Pede Fruki — produtos de referencia para a massa de 80 clientes
-- Rode no Supabase (SQL Editor) antes dos clientes, se tabela_preco estiver vazia.
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
