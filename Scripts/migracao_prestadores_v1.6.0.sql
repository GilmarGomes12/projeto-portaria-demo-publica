-- =====================================================
-- SCRIPT DE MIGRAÇÃO DE DADOS - PRESTADORES v1.6.0
-- =====================================================
-- Objetivo: Migrar prestadores existentes para o novo sistema de perfis
-- Execução: Pode ser executado manualmente ou através do LimpezaPrestadoresService
-- Data: 09/01/2026
-- =====================================================

-- ETAPA 1: Criar perfis únicos baseados em RG dos prestadores existentes
-- =====================================================

INSERT INTO perfil_prestador (
    nome_empresa, 
    nome_prestador, 
    rg, 
    telefone, 
    categoria, 
    servico_padrao, 
    ultima_visita,
    total_visitas,
    ativo
)
SELECT DISTINCT 
    p.nome_empresa, 
    p.nome_prestador, 
    p.rg, 
    p.telefone,
    -- Inferir categoria baseado no campo servico
    CASE 
        WHEN LOWER(p.servico) LIKE '%jardin%' THEN 'PRESTADOR_CONDOMINIO'
        WHEN LOWER(p.servico) LIKE '%piscin%' THEN 'PRESTADOR_CONDOMINIO'
        WHEN LOWER(p.servico) LIKE '%zelador%' THEN 'FUNCIONARIO_FIXO'
        WHEN LOWER(p.servico) LIKE '%porteiro%' THEN 'FUNCIONARIO_FIXO'
        WHEN LOWER(p.servico) LIKE '%limpeza%' THEN 'FUNCIONARIO_EVENTUAL'
        WHEN LOWER(p.servico) LIKE '%faxina%' THEN 'FUNCIONARIO_EVENTUAL'
        WHEN LOWER(p.servico) LIKE '%diarista%' THEN 'FUNCIONARIO_EVENTUAL'
        WHEN LOWER(p.servico) LIKE '%pedreiro%' THEN 'PRESTADOR_MANUTENCAO'
        WHEN LOWER(p.servico) LIKE '%pintor%' THEN 'PRESTADOR_MANUTENCAO'
        WHEN LOWER(p.servico) LIKE '%eletric%' THEN 'PRESTADOR_MANUTENCAO'
        WHEN LOWER(p.servico) LIKE '%encanad%' THEN 'PRESTADOR_MANUTENCAO'
        WHEN LOWER(p.servico) LIKE '%hidraul%' THEN 'PRESTADOR_MANUTENCAO'
        WHEN LOWER(p.servico) LIKE '%marceneiro%' THEN 'PRESTADOR_MANUTENCAO'
        WHEN LOWER(p.servico) LIKE '%gás%' OR LOWER(p.servico) LIKE '%gas%' THEN 'PRESTADOR_ENTREGA'
        WHEN LOWER(p.servico) LIKE '%água%' OR LOWER(p.servico) LIKE '%agua%' THEN 'PRESTADOR_ENTREGA'
        WHEN LOWER(p.servico) LIKE '%delivery%' THEN 'PRESTADOR_ENTREGA'
        WHEN LOWER(p.servico) LIKE '%entrega%' THEN 'PRESTADOR_ENTREGA'
        ELSE 'OUTROS'
    END as categoria,
    p.servico as servico_padrao,
    (SELECT MAX(data_hora_entrada) FROM prestadores WHERE rg = p.rg) as ultima_visita,
    (SELECT COUNT(*) FROM prestadores WHERE rg = p.rg) as total_visitas,
    1 as ativo
FROM prestadores p
WHERE p.rg IS NOT NULL 
  AND p.rg != ''
  AND NOT EXISTS (SELECT 1 FROM perfil_prestador WHERE rg = p.rg)
GROUP BY p.rg
ORDER BY p.rg;

-- =====================================================
-- ETAPA 2: Popular perfil_prestador_id nos registros de prestadores
-- =====================================================

UPDATE prestadores
SET perfil_prestador_id = (
    SELECT TOP 1 id
    FROM perfil_prestador
    WHERE perfil_prestador.rg = prestadores.rg
)
WHERE rg IS NOT NULL
  AND rg != ''
  AND perfil_prestador_id IS NULL;

-- =====================================================
-- ETAPA 3: Validação de consistência
-- =====================================================

-- Query de validação 1: Verificar se todos registros têm perfil
SELECT 
    COUNT(*) as total_prestadores,
    SUM(CASE WHEN perfil_prestador_id IS NULL THEN 1 ELSE 0 END) as sem_perfil,
    SUM(CASE WHEN perfil_prestador_id IS NOT NULL THEN 1 ELSE 0 END) as com_perfil
FROM prestadores 
WHERE rg IS NOT NULL;

-- Query de validação 2: Verificar consistência de dados
SELECT 
    (SELECT COUNT(DISTINCT rg) FROM prestadores WHERE rg IS NOT NULL) as prestadores_unicos,
    (SELECT COUNT(*) FROM perfil_prestador) as perfis_criados,
    CASE 
        WHEN (SELECT COUNT(DISTINCT rg) FROM prestadores WHERE rg IS NOT NULL) = (SELECT COUNT(*) FROM perfil_prestador)
        THEN '✅ CONSISTENTE' 
        ELSE '❌ INCONSISTENTE' 
    END as status;

-- Query de validação 3: Verificar distribuição por categoria
SELECT 
    categoria,
    COUNT(*) as total_perfis
FROM perfil_prestador
GROUP BY categoria
ORDER BY total_perfis DESC;

-- =====================================================
-- NOTAS IMPORTANTES
-- =====================================================
-- 1. Este script é idempotente (pode ser executado múltiplas vezes)
-- 2. Não exclui dados existentes
-- 3. Perfis são criados apenas para RGs não cadastrados
-- 4. perfil_prestador_id é populado apenas se NULL
-- 5. Rollback: Basta executar:
--    UPDATE prestadores SET perfil_prestador_id = NULL;
--    DELETE FROM perfil_prestador;
-- =====================================================
