PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS usuario (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    login TEXT NOT NULL UNIQUE,
    perfil TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS tecnico (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id INTEGER NOT NULL UNIQUE,
    disponibilidade TEXT NOT NULL,

    FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
);

CREATE TABLE IF NOT EXISTS equipamento (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    descricao TEXT NOT NULL,
    tipo TEXT NOT NULL,
    identificador TEXT NOT NULL UNIQUE,
    status TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS ordem_servico (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    equipamento_id INTEGER NOT NULL,
    usuario_abertura_id INTEGER NOT NULL,
    tecnico_responsavel_id INTEGER,
    descricao_problema TEXT NOT NULL,
    criticidade TEXT NOT NULL,
    status TEXT NOT NULL,
    data_abertura TEXT NOT NULL,
    data_encerramento TEXT,

    FOREIGN KEY (equipamento_id)
        REFERENCES equipamento(id),

    FOREIGN KEY (usuario_abertura_id)
        REFERENCES usuario(id),

    FOREIGN KEY (tecnico_responsavel_id)
        REFERENCES tecnico(id)
);

CREATE TABLE IF NOT EXISTS diagnostico (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ordem_servico_id INTEGER NOT NULL UNIQUE,
    tecnico_id INTEGER NOT NULL,
    parecer TEXT NOT NULL,
    causa_raiz TEXT NOT NULL,
    data_hora TEXT NOT NULL,

    FOREIGN KEY (ordem_servico_id)
        REFERENCES ordem_servico(id),

    FOREIGN KEY (tecnico_id)
        REFERENCES tecnico(id)
);

CREATE TABLE IF NOT EXISTS intervencao (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ordem_servico_id INTEGER NOT NULL,
    tecnico_id INTEGER NOT NULL,
    descricao TEXT NOT NULL,
    horas_trabalhadas REAL NOT NULL,
    data_hora TEXT NOT NULL,

    FOREIGN KEY (ordem_servico_id)
        REFERENCES ordem_servico(id),

    FOREIGN KEY (tecnico_id)
        REFERENCES tecnico(id)
);

CREATE TABLE IF NOT EXISTS material_utilizado (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    intervencao_id INTEGER NOT NULL,
    descricao TEXT NOT NULL,
    quantidade REAL NOT NULL,
    unidade TEXT NOT NULL,

    FOREIGN KEY (intervencao_id)
        REFERENCES intervencao(id)
);

CREATE TABLE IF NOT EXISTS atribuicao_tecnico (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ordem_servico_id INTEGER NOT NULL,
    tecnico_id INTEGER NOT NULL,
    atribuido_por_id INTEGER NOT NULL,
    data_atribuicao TEXT NOT NULL,
    data_fim TEXT,

    FOREIGN KEY (ordem_servico_id)
        REFERENCES ordem_servico(id),

    FOREIGN KEY (tecnico_id)
        REFERENCES tecnico(id),

    FOREIGN KEY (atribuido_por_id)
        REFERENCES usuario(id)
);

CREATE TABLE IF NOT EXISTS historico_status_os (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ordem_servico_id INTEGER NOT NULL,
    status_anterior TEXT,
    status_novo TEXT NOT NULL,
    data_hora TEXT NOT NULL,
    usuario_id INTEGER NOT NULL,

    FOREIGN KEY (ordem_servico_id)
        REFERENCES ordem_servico(id),

    FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
);

CREATE TABLE IF NOT EXISTS fila_tecnico (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ordem_servico_id INTEGER NOT NULL UNIQUE,
    tecnico_id INTEGER NOT NULL,
    gestor_id INTEGER NOT NULL,
    data_entrada TEXT NOT NULL,

    FOREIGN KEY (ordem_servico_id)
        REFERENCES ordem_servico(id),

    FOREIGN KEY (tecnico_id)
        REFERENCES tecnico(id),

    FOREIGN KEY (gestor_id)
        REFERENCES usuario(id)
);

CREATE INDEX IF NOT EXISTS idx_fila_tecnico_ordem
    ON fila_tecnico (tecnico_id, id);

INSERT INTO usuario (nome, login, perfil) VALUES
    ('Mariana Ribeiro', 'demo.manutencao.gestor', 'GESTOR'),
    ('Rafael Almeida', 'demo.manutencao.tecnico', 'TECNICO'),
    ('Camila Souza', 'demo.manutencao.camila', 'TECNICO'),
    ('Bruno Costa', 'demo.manutencao.bruno', 'TECNICO')
ON CONFLICT(login) DO UPDATE SET nome = excluded.nome;

INSERT INTO tecnico (usuario_id, disponibilidade)
SELECT id,
       CASE WHEN login = 'demo.manutencao.bruno'
            THEN 'AUSENTE' ELSE 'DISPONIVEL' END
FROM usuario
WHERE perfil = 'TECNICO'
  AND login IN ('demo.manutencao.tecnico',
                'demo.manutencao.camila',
                'demo.manutencao.bruno')
ON CONFLICT(usuario_id) DO NOTHING;

INSERT INTO equipamento (nome, descricao, tipo, identificador, status) VALUES
    ('Quadro de distribuição QD-01',
     'Alimenta as bancadas e as tomadas da oficina.',
     'Quadro elétrico', 'DEMO-MANUT-001', 'ATIVO'),
    ('Compressor CP-02',
     'Compressor de ar da bancada de manutenção.',
     'Compressor', 'DEMO-MANUT-002', 'ATIVO'),
    ('Exaustor EX-03',
     'Exaustor da área de montagem, retirado de operação.',
     'Exaustor', 'DEMO-MANUT-003', 'INATIVO')
ON CONFLICT(identificador) DO UPDATE SET
    nome = excluded.nome,
    descricao = excluded.descricao,
    tipo = excluded.tipo;

WITH exemplos(antiga, nova) AS (
    VALUES
        ('[DEMO A] Falha na alimentação',
         'Disjuntor do circuito de tomadas desarma após alguns minutos de uso.'),
        ('[DEMO B] Sinalização intermitente',
         'Sinaleiro verde do quadro pisca durante o funcionamento normal.'),
        ('[DEMO C] Revisão preventiva',
         'Inspeção preventiva do quadro: verificar conexões e sinais de aquecimento.')
)
UPDATE ordem_servico
SET descricao_problema = (
    SELECT nova FROM exemplos WHERE antiga = ordem_servico.descricao_problema
)
WHERE equipamento_id = (
    SELECT id FROM equipamento WHERE identificador = 'DEMO-MANUT-001'
)
AND usuario_abertura_id = (
    SELECT id FROM usuario WHERE login = 'demo.manutencao.gestor'
)
AND descricao_problema IN (SELECT antiga FROM exemplos);

WITH exemplos(descricao, criticidade) AS (
    VALUES
        ('Disjuntor do circuito de tomadas desarma após alguns minutos de uso.', 'ALTA'),
        ('Sinaleiro verde do quadro pisca durante o funcionamento normal.', 'MEDIA'),
        ('Inspeção preventiva do quadro: verificar conexões e sinais de aquecimento.', 'BAIXA')
)
INSERT INTO ordem_servico (
    equipamento_id, usuario_abertura_id, descricao_problema,
    criticidade, status, data_abertura
)
SELECT e.id, u.id, x.descricao, x.criticidade, 'ABERTA',
       strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime')
FROM exemplos x
JOIN equipamento e ON e.identificador = 'DEMO-MANUT-001'
JOIN usuario u ON u.login = 'demo.manutencao.gestor'
WHERE e.status = 'ATIVO' AND u.perfil = 'GESTOR'
  AND NOT EXISTS (
      SELECT 1 FROM ordem_servico os
      WHERE os.equipamento_id = e.id
        AND os.usuario_abertura_id = u.id
        AND os.descricao_problema = x.descricao
  );